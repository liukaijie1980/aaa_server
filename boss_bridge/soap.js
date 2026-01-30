/**
 * SOAP request parsing and response building for BOSS interface.
 * Request: soap:Envelope -> soap:Body -> {methodName} (namespace http://service.whjf.hsoft.com) with child elements as params.
 * Response: {methodName}Result with return code as text.
 */
const { XMLParser, XMLBuilder } = require('fast-xml-parser');

const BOSS_NS = 'http://service.whjf.hsoft.com';
const SOAP_NS = 'http://schemas.xmlsoap.org/soap/envelope/';

const parser = new XMLParser({
  ignoreAttributes: false,
  attributeNamePrefix: '@_',
  parseTagValue: true,
  trimValues: true,
});

/**
 * Parse SOAP request body and extract method name and params (flat object).
 * @param {string} xmlBody - Raw SOAP XML string
 * @returns {{ method: string, params: Record<string, string|number> }}
 */
function parseSoapRequest(xmlBody) {
  const doc = parser.parse(xmlBody);
  const envelope = doc['soap:Envelope'] || doc['Envelope'];
  if (!envelope) {
    throw new Error('Invalid SOAP: no Envelope');
  }
  const body = envelope['soap:Body'] || envelope.Body;
  if (!body) {
    throw new Error('Invalid SOAP: no Body');
  }
  // Body has one element: the method name (e.g. IsExist, Register)
  const keys = Object.keys(body).filter((k) => !k.startsWith('@_'));
  if (keys.length === 0) {
    throw new Error('Invalid SOAP: no method in Body');
  }
  const methodName = keys[0];
  const methodNode = body[methodName];
  if (!methodNode || typeof methodNode !== 'object') {
    return { method: methodName, params: {} };
  }
  const params = {};
  for (const [key, value] of Object.entries(methodNode)) {
    if (key.startsWith('@_')) continue;
    if (value !== undefined && value !== null) {
      params[key] = typeof value === 'object' && value !== null && !Array.isArray(value) && Object.keys(value).length === 0
        ? ''
        : String(value);
    }
  }
  return { method: methodName, params };
}

/**
 * Build SOAP response with method result (return code).
 * @param {string} methodName - e.g. IsExist
 * @param {number|string} returnCode - BOSS return code (0, -2, etc.)
 * @returns {string} SOAP XML string
 */
function buildSoapResponse(methodName, returnCode) {
  const resultTag = `${methodName}Result`;
  const responseTag = `${methodName}Response`;
  const code = String(returnCode);
  const xml = `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="${SOAP_NS}">
  <soap:Body>
    <${responseTag} xmlns="${BOSS_NS}">
      <${resultTag}>${escapeXml(code)}</${resultTag}>
    </${responseTag}>
  </soap:Body>
</soap:Envelope>`;
  return xml;
}

function escapeXml(s) {
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}

module.exports = { parseSoapRequest, buildSoapResponse, BOSS_NS, SOAP_NS };
