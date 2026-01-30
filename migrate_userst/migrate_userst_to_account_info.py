#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
USERST (Oracle) -> account_info (MySQL) 迁移脚本。
支持配置文件 migrate.json + 环境变量覆盖；已存在 (user_name, realm) 则更新。
"""
from __future__ import print_function

import json
import os
import sys
import uuid
from datetime import datetime
from pathlib import Path

# 依赖：oracledb, pymysql（见 requirements.txt）
import oracledb
import pymysql

# 默认配置文件路径（脚本同目录下的 migrate.json）
SCRIPT_DIR = Path(__file__).resolve().parent
DEFAULT_CONFIG_PATH = SCRIPT_DIR / "migrate.json"
BATCH_SIZE = 5000

# Oracle 需查询的 USERST 列（与 account_info 映射相关）
ORACLE_COLS = [
    "REVEAL_USERNAME", "AGENT_CODE", "PASSWD", "CREATE_DATE", "LIMIT_DATE",
    "INPUT_SPEED_LIMIT", "OUTPUT_SPEED_LIMIT", "STATUS", "SIMULTANEOUS",
    "LEFTTIME", "TIMEOUT", "INTERVAL", "PWD_CHANGE_DATE", "LAST_LOGIN_DATE",
]


def load_config(config_path=None):
    """从 JSON 配置文件和环境变量合并连接参数。"""
    path = config_path or DEFAULT_CONFIG_PATH
    if not isinstance(path, Path):
        path = Path(path)
    cfg = {}
    if path.exists():
        with open(path, "r", encoding="utf-8") as f:
            cfg = json.load(f)
    # 环境变量覆盖（大写、下划线）
    env_map = {
        "oracle": {
            "host": "ORACLE_HOST",
            "port": "ORACLE_PORT",
            "service_name": "ORACLE_SERVICE_NAME",
            "user": "ORACLE_USER",
            "password": "ORACLE_PASSWORD",
        },
        "mysql": {
            "host": "MYSQL_HOST",
            "port": "MYSQL_PORT",
            "database": "MYSQL_DATABASE",
            "user": "MYSQL_USER",
            "password": "MYSQL_PASSWORD",
        },
    }
    for db, keys in env_map.items():
        if db not in cfg:
            cfg[db] = {}
        for key, evar in keys.items():
            val = os.environ.get(evar)
            if val is not None:
                if key == "port":
                    cfg[db][key] = int(val)
                else:
                    cfg[db][key] = val
    return cfg


def get_oracle_dsn(cfg):
    host = cfg.get("oracle", {}).get("host") or "localhost"
    port = int(cfg.get("oracle", {}).get("port") or 1521)
    sn = cfg.get("oracle", {}).get("service_name") or "ORCL"
    return f"{host}:{port}/{sn}"


def _safe_int(val, default=0):
    if val is None:
        return default
    if isinstance(val, (int, float)):
        return int(val)
    s = str(val).strip()
    if not s:
        return default
    try:
        return int(float(s))
    except (ValueError, TypeError):
        return default


def _safe_str(val, default=""):
    if val is None:
        return default
    s = str(val).strip()
    return s if s else default


def _safe_datetime(val):
    """Oracle DATE -> datetime 或 None。"""
    if val is None:
        return None
    if isinstance(val, datetime):
        return val
    return None


def row_to_account_info(row, col_index):
    """将 Oracle USERST 的一行转为 account_info 的一行（用于 INSERT）。"""
    def idx(name):
        return col_index[name]
    r = row
    user_name = _safe_str(r[idx("REVEAL_USERNAME")])
    realm = _safe_str(r[idx("AGENT_CODE")])
    # (user_name, realm) 唯一键，缺省用空串
    pw = _safe_str(r[idx("PASSWD")])
    create_date = _safe_datetime(r[idx("CREATE_DATE")])
    limit_date = _safe_datetime(r[idx("LIMIT_DATE")])
    status = r[idx("STATUS")]
    is_frozen = 0 if (status and str(status).strip() == "0") else 1
    sim = _safe_int(r[idx("SIMULTANEOUS")])
    lefttime = _safe_int(r[idx("LEFTTIME")])
    timeout = _safe_int(r[idx("TIMEOUT")])
    interval_val = _safe_int(r[idx("INTERVAL")])
    pwd_change = _safe_datetime(r[idx("PWD_CHANGE_DATE")])
    last_login = _safe_datetime(r[idx("LAST_LOGIN_DATE")])
    in_speed = _safe_int(r[idx("INPUT_SPEED_LIMIT")])
    out_speed = _safe_int(r[idx("OUTPUT_SPEED_LIMIT")])

    # modify_date: 优先 PWD_CHANGE_DATE / LAST_LOGIN_DATE，否则当前时间
    modify_date = pwd_change or last_login
    if modify_date is None:
        modify_date = datetime.now()

    return {
        "id": str(uuid.uuid4()),
        "user_name": user_name,
        "realm": realm,
        "user_password": pw,
        "auth_mode": 0,
        "is_frozen": is_frozen,
        "admin_name": "",
        "valid_date": create_date,
        "expire_date": limit_date,
        "modify_date": modify_date,
        "simual_use_limit": sim,
        "byte_remain": 0,
        "second_remain": lefttime,
        "max_session_timeout": timeout,
        "inbound_car": in_speed,
        "outbound_car": out_speed,
        "qos_profile": "",
        "update_interval": interval_val,
    }


def run_migration(config_path=None):
    cfg = load_config(config_path)
    oc = cfg.get("oracle") or {}
    mc = cfg.get("mysql") or {}
    for name, val in [("oracle.user", oc.get("user")), ("oracle.password", oc.get("password")),
                      ("mysql.host", mc.get("host")), ("mysql.database", mc.get("database")),
                      ("mysql.user", mc.get("user")), ("mysql.password", mc.get("password"))]:
        if not val:
            print("配置缺失: 请设置 {}（migrate.json 或环境变量）".format(name), file=sys.stderr)
            return 1

    dsn = get_oracle_dsn(cfg)
    oracle_user = oc["user"]
    oracle_password = oc["password"]
    mysql_host = mc.get("host", "localhost")
    mysql_port = int(mc.get("port") or 3306)
    mysql_db = mc["database"]
    mysql_user = mc["user"]
    mysql_password = mc["password"]
    mysql_charset = mc.get("charset") or "utf8mb4"

    # Oracle 表名（若带 schema 如 PORTAL.USERST，可在此或配置中指定）
    oracle_table = oc.get("table") or "USERST"
    select_sql = "SELECT {} FROM {}".format(
        ", ".join(ORACLE_COLS),
        oracle_table,
    )

    try:
        conn_o = oracledb.connect(user=oracle_user, password=oracle_password, dsn=dsn, encoding="UTF-8")
        conn_o.cursor().execute("SELECT 1 FROM DUAL")
    except Exception as e:
        print("Oracle 连接或连通性检查失败: {}".format(e), file=sys.stderr)
        return 1
    try:
        conn_m = pymysql.connect(
            host=mysql_host, port=mysql_port, user=mysql_user, password=mysql_password,
            database=mysql_db, charset=mysql_charset, autocommit=False,
        )
        with conn_m.cursor() as c:
            c.execute("SELECT 1")
    except Exception as e:
        print("MySQL 连接或连通性检查失败: {}".format(e), file=sys.stderr)
        conn_o.close()
        return 1

    col_index = {c: i for i, c in enumerate(ORACLE_COLS)}
    ins_sql = """
    INSERT INTO account_info (
        id, user_name, realm, user_password, auth_mode, is_frozen, admin_name,
        valid_date, expire_date, modify_date, simual_use_limit, byte_remain,
        second_remain, max_session_timeout, inbound_car, outbound_car,
        qos_profile, update_interval
    ) VALUES (
        %(id)s, %(user_name)s, %(realm)s, %(user_password)s, %(auth_mode)s, %(is_frozen)s, %(admin_name)s,
        %(valid_date)s, %(expire_date)s, %(modify_date)s, %(simual_use_limit)s, %(byte_remain)s,
        %(second_remain)s, %(max_session_timeout)s, %(inbound_car)s, %(outbound_car)s,
        %(qos_profile)s, %(update_interval)s
    ) ON DUPLICATE KEY UPDATE
        user_password = VALUES(user_password),
        auth_mode = VALUES(auth_mode),
        is_frozen = VALUES(is_frozen),
        admin_name = VALUES(admin_name),
        valid_date = VALUES(valid_date),
        expire_date = VALUES(expire_date),
        modify_date = VALUES(modify_date),
        simual_use_limit = VALUES(simual_use_limit),
        byte_remain = VALUES(byte_remain),
        second_remain = VALUES(second_remain),
        max_session_timeout = VALUES(max_session_timeout),
        inbound_car = VALUES(inbound_car),
        outbound_car = VALUES(outbound_car),
        qos_profile = VALUES(qos_profile),
        update_interval = VALUES(update_interval)
    """

    total_ok = 0
    total_err = 0
    cur_o = conn_o.cursor()
    cur_o.execute(select_sql)
    start = 0

    try:
        while True:
            rows = cur_o.fetchmany(BATCH_SIZE)
            if not rows:
                break
            batch = []
            for row in rows:
                rec = row_to_account_info(row, col_index)
                # MySQL 要求 datetime 或 None
                for k in ("valid_date", "expire_date", "modify_date"):
                    v = rec.get(k)
                    if isinstance(v, datetime):
                        rec[k] = v.strftime("%Y-%m-%d %H:%M:%S")
                batch.append(rec)
            try:
                with conn_m.cursor() as cur_m:
                    for rec in batch:
                        try:
                            cur_m.execute(ins_sql, rec)
                            total_ok += 1
                        except Exception as e:
                            total_err += 1
                            print("行写入失败 user_name={} realm={}: {}".format(
                                rec.get("user_name"), rec.get("realm"), e), file=sys.stderr)
                conn_m.commit()
            except Exception as e:
                conn_m.rollback()
                print("批次提交失败: {}".format(e), file=sys.stderr)
                total_err += len(batch)
            start += len(batch)
            print("已处理 {} 条".format(start))
    finally:
        cur_o.close()
        conn_o.close()
        conn_m.close()

    print("完成: 成功 {} 条, 失败 {} 条".format(total_ok, total_err))
    return 0 if total_err == 0 else 1


if __name__ == "__main__":
    import argparse
    p = argparse.ArgumentParser(description="USERST -> account_info 迁移")
    p.add_argument("--config", "-c", default=None, help="配置文件路径，默认脚本同目录下 migrate.json")
    args = p.parse_args()
    sys.exit(run_migration(args.config))
