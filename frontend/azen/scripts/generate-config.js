const fs = require('fs');
const path = require('path');
const dotenv = require('dotenv');

const envPath = path.join(
  process.cwd(),
  'deployment',
  '.env'
);

const result = dotenv.config({
  path: envPath
});

if (result.error) {
  throw new Error(
    `Could not load ${envPath}`
  );
}

const apiUrl = process.env.AZEN_API_URL;

if (!apiUrl) {
  throw new Error(
    'AZEN_API_URL is not defined.'
  );
}

const config = `window.__AZEN_CONFIG__ = {
  apiUrl: '${apiUrl}'
};
`;

const outputPath = path.join(
  process.cwd(),
  'public',
  'config.js'
);

fs.writeFileSync(outputPath, config);

console.log(`Generated ${outputPath}`);