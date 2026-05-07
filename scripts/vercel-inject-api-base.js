/**
 * Vercel build: writes src/main/resources/static/assets/api-base.js
 * from env MEBODY_API_BASE (e.g. https://api.yourdomain.com — no trailing slash).
 * Local / Spring: use committed default api-base.js (empty string = same-origin).
 */
const fs = require('fs');
const path = require('path');

const base = (process.env.MEBODY_API_BASE || '').trim();
const outDir = path.join(__dirname, '..', 'src', 'main', 'resources', 'static', 'assets');
const file = path.join(outDir, 'api-base.js');
const content = `window.__MEBODY_API_BASE__ = ${JSON.stringify(base)};\n`;

fs.mkdirSync(outDir, { recursive: true });
fs.writeFileSync(file, content, 'utf8');
console.log('[vercel-build] api-base.js ← MEBODY_API_BASE', base || '(empty = same-origin)');
