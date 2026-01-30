/**
 * 调试脚本：测试与 radiusApi 的连通性（登录 + 拉取账户列表）。
 * 用法：在 boss_bridge 目录下执行 node scripts/debug-radius.js
 */
require('dotenv').config();
const axios = require('axios');

const baseUrl = process.env.RADIUS_API_URL || 'http://localhost:8088';
const username = process.env.ADMIN_USERNAME || 'admin';
const password = process.env.ADMIN_PASSWORD || 'admin';

async function main() {
  console.log('radiusApi 地址:', baseUrl);
  console.log('管理员账号:', username);
  console.log('');

  // 1. 登录
  console.log('1. 登录 POST /admin/login ...');
  let loginRes;
  try {
    loginRes = await axios.post(
      `${baseUrl}/admin/login`,
      { username, password },
      { headers: { 'Content-Type': 'application/json' }, timeout: 10000, validateStatus: () => true }
    );
  } catch (err) {
    console.error('  请求失败:', err.code === 'ECONNREFUSED' ? '连接被拒绝，请确认 radiusApi 已启动且地址正确' : err.message);
    process.exit(1);
  }

  if (loginRes.status !== 200) {
    console.error('  登录失败 HTTP', loginRes.status, loginRes.data);
    process.exit(1);
  }
  const data = loginRes.data;
  if (!data || !data.data || !data.data.token) {
    console.error('  登录失败，未返回 token:', data && data.message ? data.message : data);
    process.exit(1);
  }
  const token = data.data.token;
  console.log('  登录成功，已获得 token');

  // 2. 拉取账户列表（带 token）
  console.log('2. 拉取账户 GET /AccountInfo (带 x-token) ...');
  let accRes;
  try {
    accRes = await axios.get(`${baseUrl}/AccountInfo`, {
      params: { name: '', realm: '', pageNo: 1, pageSize: 3 },
      headers: { 'x-token': token },
      timeout: 10000,
      validateStatus: () => true,
    });
  } catch (err) {
    console.error('  请求失败:', err.message);
    process.exit(1);
  }

  if (accRes.status !== 200) {
    console.error('  请求失败 HTTP', accRes.status, accRes.data);
    process.exit(1);
  }
  const accData = accRes.data;
  if (!accData || accData.success !== true) {
    console.error('  接口返回失败:', accData && accData.message ? accData.message : accData);
    process.exit(1);
  }
  const page = accData.data && accData.data.data ? accData.data.data : (accData.data || {});
  const records = page.records || [];
  console.log('  成功，当前页账户数:', records.length);
  if (records.length > 0) {
    const first = records[0];
    console.log('  示例账户:', (first.userName || first.UserName || first.user_name) + '@' + (first.realm || ''));
  }
  console.log('');
  console.log('radiusApi 连通性正常，桥接服务可据此配置 .env 中的 RADIUS_API_URL、ADMIN_USERNAME、ADMIN_PASSWORD。');
}

main();
