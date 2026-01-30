/**
 * BOSS bridge server: exposes SOAP endpoint compatible with index.html client,
 * forwards to radiusApi and returns BOSS return codes in SOAP response.
 */
require('dotenv').config();
const express = require('express');
const { parseSoapRequest, buildSoapResponse } = require('./soap');
const { handlers } = require('./handlers');
const config = require('./config');

const app = express();

// Raw body for SOAP XML (text/xml)
app.use(express.raw({ type: ['text/xml', 'application/xml', 'application/soap+xml'], limit: '1mb' }));
app.use(express.json({ type: () => false }));

// Health / info
app.get('/', (req, res) => {
  res.type('text/plain').send('BOSS bridge to radiusApi. POST SOAP to this path.');
});

// Minimal WSDL for "test connection" from index.html (GET ...?wsdl)
app.get('/services/IServiceUopBossToTvManager', (req, res) => {
  if (req.query.wsdl === '' || req.query.wsdl) {
    res.type('application/xml').send(getMinimalWsdl());
    return;
  }
  res.type('text/plain').send('BOSS bridge. Use POST with SOAP body or ?wsdl');
});

function getMinimalWsdl() {
  const base = `http://localhost:${config.PORT}`;
  return `<?xml version="1.0" encoding="UTF-8"?>
<wsdl:definitions xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:tns="http://service.whjf.hsoft.com" targetNamespace="http://service.whjf.hsoft.com">
  <wsdl:service name="IServiceUopBossToTvManager">
    <wsdl:port name="IServiceUopBossToTvManagerPort" binding="tns:IServiceUopBossToTvManagerBinding">
      <soap:address location="${base}/services/IServiceUopBossToTvManager"/>
    </wsdl:port>
  </wsdl:service>
</wsdl:definitions>`;
}

// SOAP endpoint: same path as client default path
app.post('/services/IServiceUopBossToTvManager', async (req, res) => {
  let body = req.body;
  if (Buffer.isBuffer(body)) body = body.toString('utf8');
  if (!body || typeof body !== 'string') {
    res.status(400).type('text/xml').send(buildSoapFault('Missing or invalid SOAP body'));
    return;
  }

  let method;
  let params;
  try {
    const parsed = parseSoapRequest(body);
    method = parsed.method;
    params = parsed.params;
  } catch (e) {
    console.error('SOAP parse error:', e.message);
    res.status(400).type('text/xml').send(buildSoapFault('Invalid SOAP: ' + e.message));
    return;
  }

  const handler = handlers[method];
  if (!handler) {
    console.error('Unknown method:', method);
    res.status(400).type('text/xml').send(buildSoapFault('Unknown method: ' + method));
    return;
  }

  let returnCode;
  try {
    returnCode = await handler(params);
  } catch (e) {
    console.error('Handler error:', method, e.message);
    returnCode = -9; // UNKNOWN
  }

  const xml = buildSoapResponse(method, returnCode);
  res.type('text/xml; charset=utf-8').send(xml);
});

function buildSoapFault(message) {
  return `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <soap:Fault>
      <faultcode>Client</faultcode>
      <faultstring>${escapeXml(message)}</faultstring>
    </soap:Fault>
  </soap:Body>
</soap:Envelope>`;
}

function escapeXml(s) {
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}

// Allow POST on root path too (so any path config works when base is root)
app.post('/', async (req, res) => {
  let body = req.body;
  if (Buffer.isBuffer(body)) body = body.toString('utf8');
  if (!body || typeof body !== 'string') {
    res.status(400).type('text/xml').send(buildSoapFault('Missing or invalid SOAP body'));
    return;
  }
  let method, params;
  try {
    const parsed = parseSoapRequest(body);
    method = parsed.method;
    params = parsed.params;
  } catch (e) {
    res.status(400).type('text/xml').send(buildSoapFault('Invalid SOAP: ' + e.message));
    return;
  }
  const handler = handlers[method];
  if (!handler) {
    res.status(400).type('text/xml').send(buildSoapFault('Unknown method: ' + method));
    return;
  }
  let returnCode;
  try {
    returnCode = await handler(params);
  } catch (e) {
    console.error('Handler error:', method, e.message);
    returnCode = -9;
  }
  res.type('text/xml; charset=utf-8').send(buildSoapResponse(method, returnCode));
});

const server = app.listen(config.PORT, () => {
  console.log(`BOSS bridge listening on port ${config.PORT}`);
  console.log(`  SOAP endpoint: http://localhost:${config.PORT}/services/IServiceUopBossToTvManager`);
  console.log(`  radiusApi:     ${config.RADIUS_API_URL}`);
});

server.on('error', (err) => {
  if (err.code === 'EADDRINUSE') {
    console.error(`Port ${config.PORT} is already in use. Stop the other process or set PORT to another value (e.g. PORT=8091 npm start).`);
  } else {
    console.error(err);
  }
  process.exit(1);
});

module.exports = app;
