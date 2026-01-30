/**
 * HTTP client for radiusApi with token management.
 * - Login on first use or when token is invalid (500, code 50000).
 * - All requests carry x-token; one retry after re-login on auth failure.
 */
const axios = require('axios');
const { RADIUS_API_URL, ADMIN_USERNAME, ADMIN_PASSWORD } = require('./config');

let cachedToken = null;

async function login() {
  const { data } = await axios.post(`${RADIUS_API_URL}/admin/login`, {
    username: ADMIN_USERNAME,
    password: ADMIN_PASSWORD,
  }, {
    headers: { 'Content-Type': 'application/json' },
    validateStatus: () => true,
  });
  if (data && data.data && data.data.token) {
    cachedToken = data.data.token;
    return cachedToken;
  }
  throw new Error(data?.message || 'Login failed');
}

function getToken() {
  return cachedToken;
}

async function ensureToken() {
  if (cachedToken) return cachedToken;
  return login();
}

/**
 * Call radiusApi with automatic token and retry on 50000.
 * @param {'GET'|'POST'|'PUT'|'DELETE'} method
 * @param {string} path - e.g. /AccountInfo
 * @param {object} [options] - { params, data }
 * @returns {Promise<{ success: boolean, code: number, message: string, data?: any }>}
 */
async function request(method, path, options = {}) {
  const doRequest = async (token) => {
    const url = path.startsWith('http') ? path : `${RADIUS_API_URL}${path}`;
    const config = {
      method,
      url,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'x-token': token } : {}),
      },
      validateStatus: () => true,
    };
    if (options.params) config.params = options.params;
    if (options.data !== undefined) config.data = options.data;
    const res = await axios(config);
    const body = res.data;
    if (res.status === 500 && body && (body.code === '50000' || body.msg === 'token verify fail')) {
      return { authFailed: true, response: body };
    }
    return { authFailed: false, response: body, status: res.status };
  };

  let token = await ensureToken();
  let result = await doRequest(token);
  if (result.authFailed) {
    cachedToken = null;
    token = await login();
    result = await doRequest(token);
  }
  if (result.authFailed) {
    throw new Error('radiusApi auth failed after re-login');
  }
  return result.response;
}

module.exports = { request, login, getToken, ensureToken };
