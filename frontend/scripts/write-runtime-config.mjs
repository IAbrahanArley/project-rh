import { mkdir, writeFile } from "node:fs/promises";
import { dirname, resolve } from "node:path";

const apiBaseUrl = process.argv[2] ?? "/api";
const outputPath = resolve("dist/rh-frontend/browser/assets/runtime-config.js");
const content = `window.RH_RUNTIME_CONFIG = {\n  apiBaseUrl: ${JSON.stringify(apiBaseUrl)},\n};\n`;

await mkdir(dirname(outputPath), { recursive: true });
await writeFile(outputPath, content, "utf8");
