/**
 * BOSS method handlers: map BOSS params to radiusApi calls and return BOSS return codes.
 * AreaGroupID -> realm = '@' + AreaGroupID; Account -> UserName/username.
 */
const { request } = require('../radiusClient');
const config = require('../config');

const BOSS = {
  SUCCESS: 0,
  OFFLINE: 1,
  ONLINE: 3,
  DB_ERROR: -1,
  ACCOUNT_NOT_EXIST: -2,
  DISABLED: -3,
  SUBSYS_ERROR: -4,
  AMOUNT_OVERFLOW: -5,
  ACCOUNT_EXISTS: -6,
  ALREADY_ONLINE: -7,
  ACCOUNT_NOT_OPEN: -8,
  UNKNOWN: -9,
  HAS_DEBT: -10,
  PARAM_EMPTY: -13,
  PARAM_INVALID: -16,
  FORCE_OFFLINE_FAIL: -17,
  PASSWORD_ERROR: -18,
};

function realm(p) {
  const raw = p.AreaGroupID != null ? String(p.AreaGroupID).trim() : (p.realm != null ? String(p.realm).trim() : '');
  if (!raw) return '';
  return raw.startsWith('@') ? raw : '@' + raw;
}

function account(p) {
  return p.Account != null ? String(p.Account) : (p.Account ?? '');
}

function ok(res) {
  return res && res.success === true && (res.code === 20000 || res.code === undefined);
}

function records(res) {
  const d = res && res.data;
  if (!d) return [];
  const page = d.data !== undefined ? d.data : d;
  return (page && Array.isArray(page.records)) ? page.records : (Array.isArray(page) ? page : []);
}

/** GET 当前账户，若不存在返回 null。用于 PUT 前合并现有字段，避免 qos_profile 等非空列被写成 null。 */
async function getExistingAccount(r, acc) {
  const res = await request('GET', '/AccountInfo', {
    params: { name: acc, realm: r, pageNo: 1, pageSize: 1 },
  });
  if (!ok(res)) return null;
  const recs = records(res);
  return recs.length > 0 ? recs[0] : null;
}

/** 将现有记录转为 PUT 可用的 body（统一 camelCase，保证非空字段有默认值） */
function existingToPutBody(existing) {
  if (!existing) return {};
  const body = {
    userName: existing.userName ?? existing.UserName ?? '',
    realm: existing.realm ?? '',
    userPassword: existing.userPassword ?? existing.UserPassword ?? '',
    isFrozen: existing.isFrozen ?? existing.IsFrozen ?? false,
    adminName: existing.adminName ?? existing.AdminName ?? '',
    qosProfile: existing.qosProfile ?? existing.QosProfile ?? '',
    inboundCar: existing.inboundCar ?? existing.InboundCar ?? 0,
    outboundCar: existing.outboundCar ?? existing.OutboundCar ?? 0,
  };
  if (existing.expireDate != null) body.expireDate = existing.expireDate;
  if (existing.validDate != null) body.validDate = existing.validDate;
  return body;
}

async function IsExist(params) {
  const r = realm(params);
  const acc = account(params);
  const res = await request('GET', '/AccountInfo', {
    params: { name: acc, realm: r, pageNo: 1, pageSize: 1 },
  });
  if (!ok(res)) return BOSS.UNKNOWN;
  const recs = records(res);
  return recs.length > 0 ? BOSS.SUCCESS : BOSS.ACCOUNT_NOT_EXIST;
}

async function ForceOffline(params) {
  const r = realm(params);
  const acc = account(params);
  const getRes = await request('GET', '/OnlineUser', {
    params: { name: acc, realm: r, pageNo: 1, pageSize: 100 },
  });
  if (!ok(getRes)) return BOSS.FORCE_OFFLINE_FAIL;
  const recs = records(getRes);
  if (recs.length === 0) return BOSS.OFFLINE;
  const now = new Date().toISOString().replace('T', ' ').substring(0, 19);
  for (const rec of recs) {
    const putRes = await request('PUT', '/OnlineUser', {
      data: {
        username: rec.username,
        realm: rec.realm,
        acctsessionid: rec.acctsessionid,
        acctstoptime: now,
      },
    });
    if (!ok(putRes)) return BOSS.FORCE_OFFLINE_FAIL;
  }
  return BOSS.SUCCESS;
}

async function CheckPassword(params) {
  const r = realm(params);
  const acc = account(params);
  const pwd = params.Password != null ? String(params.Password) : '';
  if (!acc || !pwd) return BOSS.PARAM_EMPTY;
  const res = await request('GET', '/AccountInfo', {
    params: { name: acc, realm: r, pageNo: 1, pageSize: 1 },
  });
  if (!ok(res)) return BOSS.UNKNOWN;
  const recs = records(res);
  if (recs.length === 0) return BOSS.ACCOUNT_NOT_EXIST;
  const storedPwd = recs[0].UserPassword || recs[0].userPassword || '';
  return storedPwd === pwd ? BOSS.SUCCESS : BOSS.PASSWORD_ERROR;
}

async function SearchCustomerStatus(params) {
  const r = realm(params);
  const acc = account(params);
  const accRes = await request('GET', '/AccountInfo', {
    params: { name: acc, realm: r, pageNo: 1, pageSize: 1 },
  });
  if (!ok(accRes)) return BOSS.UNKNOWN;
  const accRecs = records(accRes);
  if (accRecs.length === 0) return BOSS.ACCOUNT_NOT_EXIST;
  const onlineRes = await request('GET', '/OnlineUser', {
    params: { name: acc, realm: r, pageNo: 1, pageSize: 1 },
  });
  if (!ok(onlineRes)) return BOSS.UNKNOWN;
  const onlineRecs = records(onlineRes);
  return onlineRecs.length > 0 ? BOSS.ONLINE : BOSS.OFFLINE;
}

function adminName() {
  return config.ADMIN_USERNAME || '';
}

function mapRegisterToAccountInfo(params) {
  const r = realm(params);
  const acc = account(params);
  const body = {
    userName: acc,
    realm: r,
    userPassword: params.Password != null ? String(params.Password) : '',
    isFrozen: false,
    adminName: adminName(),
  };
  if (params.EndTime) body.expireDate = params.EndTime;
  // INPUT_SPEED_LIMIT → inbound_car, OUTPUT_SPEED_LIMIT → outbound_car (BOSS 也可能用 UpLoadBandWidth/DownLoadBandWidth)
  const inboundVal = params.INPUT_SPEED_LIMIT ?? params.UpLoadBandWidth ?? params.UpLoadBandwidth;
  const outboundVal = params.OUTPUT_SPEED_LIMIT ?? params.DownLoadBandWidth;
  if (inboundVal != null && inboundVal !== '') body.inboundCar = parseInt(inboundVal, 10) || 0;
  if (outboundVal != null && outboundVal !== '') body.outboundCar = parseInt(outboundVal, 10) || 0;
  return body;
}

async function Register(params) {
  const body = mapRegisterToAccountInfo(params);
  const res = await request('POST', '/AccountInfo', { data: body });
  if (ok(res)) return BOSS.SUCCESS;
  const msg = (res && res.message) ? String(res.message).toLowerCase() : '';
  if (msg.includes('exist') || msg.includes('duplicate') || msg.includes('已存在')) return BOSS.ACCOUNT_EXISTS;
  return BOSS.UNKNOWN;
}

async function IpoeRegister(params) {
  const r = realm(params);
  const macList = (params.MacList != null ? String(params.MacList) : '').split(',').map((s) => s.trim()).filter(Boolean);
  if (macList.length === 0) return BOSS.PARAM_INVALID;
  const pwd = params.Password != null ? String(params.Password) : '';
  for (const mac of macList) {
    const body = {
      userName: mac,
      realm: r,
      userPassword: pwd,
      isFrozen: false,
      adminName: adminName(),
    };
    const res = await request('POST', '/AccountInfo', { data: body });
    if (!ok(res)) return BOSS.UNKNOWN;
  }
  return BOSS.SUCCESS;
}

async function EraseCustomer(params) {
  const r = realm(params);
  const acc = account(params);
  const res = await request('DELETE', '/AccountInfo', {
    params: { UserName: acc, realm: r },
  });
  if (ok(res)) return BOSS.SUCCESS;
  return BOSS.ACCOUNT_NOT_EXIST;
}

async function IpoeEraseCustomer(params) {
  const r = realm(params);
  const mac = params.Mac != null ? String(params.Mac).trim() : '';
  if (!mac) return BOSS.PARAM_EMPTY;
  const res = await request('DELETE', '/AccountInfo', {
    params: { UserName: mac, realm: r },
  });
  if (ok(res)) return BOSS.SUCCESS;
  return BOSS.ACCOUNT_NOT_EXIST;
}

function mapChangeUserInfoToAccountInfo(params) {
  const r = realm(params);
  const acc = account(params);
  const body = {
    userName: acc,
    realm: r,
    adminName: adminName(),
  };
  if (params.EndTime !== undefined && params.EndTime !== '') body.expireDate = params.EndTime;
  // INPUT_SPEED_LIMIT → inbound_car, OUTPUT_SPEED_LIMIT → outbound_car
  const inboundVal = params.INPUT_SPEED_LIMIT ?? params.UpLoadBandwidth ?? params.UpLoadBandWidth;
  const outboundVal = params.OUTPUT_SPEED_LIMIT ?? params.DownLoadBandWidth;
  if (inboundVal != null && inboundVal !== '') body.inboundCar = parseInt(inboundVal, 10) || 0;
  if (outboundVal != null && outboundVal !== '') body.outboundCar = parseInt(outboundVal, 10) || 0;
  return body;
}

async function ChangeUserInfo(params) {
  const r = realm(params);
  const acc = account(params);
  const existing = await getExistingAccount(r, acc);
  if (!existing) return BOSS.ACCOUNT_NOT_EXIST;
  const body = { ...existingToPutBody(existing), ...mapChangeUserInfoToAccountInfo(params), userName: acc, realm: r, adminName: adminName() };
  const res = await request('PUT', '/AccountInfo', { data: body });
  if (ok(res)) return BOSS.SUCCESS;
  return BOSS.ACCOUNT_NOT_EXIST;
}

async function ChangeUserPassword(params) {
  const r = realm(params);
  const acc = account(params);
  const pwd = params.Password != null ? String(params.Password) : '';
  if (!pwd) return BOSS.PARAM_EMPTY;
  const existing = await getExistingAccount(r, acc);
  if (!existing) return BOSS.ACCOUNT_NOT_EXIST;
  const body = { ...existingToPutBody(existing), userName: acc, realm: r, userPassword: pwd, adminName: adminName() };
  const res = await request('PUT', '/AccountInfo', { data: body });
  if (ok(res)) return BOSS.SUCCESS;
  return BOSS.ACCOUNT_NOT_EXIST;
}

async function EnableAccount(params) {
  const r = realm(params);
  const acc = account(params);
  const enable = params.Enable === 1 || params.Enable === '1';
  const existing = await getExistingAccount(r, acc);
  if (!existing) return BOSS.ACCOUNT_NOT_EXIST;
  const body = { ...existingToPutBody(existing), userName: acc, realm: r, isFrozen: !enable, adminName: adminName() };
  const res = await request('PUT', '/AccountInfo', { data: body });
  if (ok(res)) return BOSS.SUCCESS;
  return BOSS.ACCOUNT_NOT_EXIST;
}

const handlers = {
  IsExist,
  ForceOffline,
  CheckPassword,
  SearchCustomerStatus,
  Register,
  IpoeRegister,
  EraseCustomer,
  IpoeEraseCustomer,
  ChangeUserInfo,
  ChangeUserPassword,
  EnableAccount,
};

module.exports = { handlers, BOSS };
