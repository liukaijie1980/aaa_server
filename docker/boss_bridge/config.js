/**
 * Bridge service configuration.
 * Override via environment variables: RADIUS_API_URL, ADMIN_USERNAME, ADMIN_PASSWORD, PORT.
 */
const RADIUS_API_URL = process.env.RADIUS_API_URL || 'http://localhost:8088';
const ADMIN_USERNAME = process.env.ADMIN_USERNAME || 'admin';
const ADMIN_PASSWORD = process.env.ADMIN_PASSWORD || 'admin';
const PORT = parseInt(process.env.PORT || '8090', 10);

module.exports = {
  RADIUS_API_URL: RADIUS_API_URL.replace(/\/$/, ''),
  ADMIN_USERNAME,
  ADMIN_PASSWORD,
  PORT,
};
