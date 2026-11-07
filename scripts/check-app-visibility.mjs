import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { createRequire } from "node:module";

const require = createRequire(import.meta.url);
const { inTvDock: visible } = require("../app/src/main/assets/ui.js");
const code = readFileSync("app/src/main/assets/ui.js", "utf8");

const tv = { id: "tv.app", tvDefault: true, system: false };
const ordinary = { id: "phone.app", tvDefault: false, system: false };
const oldFixture = { id: "old.app" };
const model = { addedApps: [], hiddenApps: [] };

assert.equal(visible(tv, model), true, "TV apps show by default");
assert.equal(visible(ordinary, model), false, "Non-TV apps require explicit addition");
assert.equal(visible(oldFixture, model), true, "Older preview models remain compatible");
assert.equal(visible(ordinary, { addedApps: [ordinary.id], hiddenApps: [] }), true, "Adding an app shows it");
assert.equal(visible(tv, { addedApps: [], hiddenApps: [tv.id] }), false, "Hidden TV apps stay hidden");
assert.equal(visible(ordinary, { addedApps: [ordinary.id], hiddenApps: [ordinary.id] }), false, "Hidden overrides added");
assert.equal(visible(tv, { addedApps: [], hiddenApps: [], favorites: [tv.id] }), true, "Favorites do not change dock membership");
assert.equal(visible(tv, { addedApps: [], hiddenApps: [tv.id], favorites: [tv.id] }), false, "Removing from dock does not change favorites");
assert.equal(visible(tv, {}), true, "Missing state is handled");

const translations = readFileSync("app/src/main/assets/i18n.js", "utf8");
for (const name of [
  "Ajouter des applications",
  "Ouvrir l’application",
  "Forcer l’arrêt",
  "Vider le cache",
  "Informations sur l’application",
  "Désinstaller l’application",
  "Retirer l’application des Apps TV",
  "Accueil d’origine temporaire",
  "Accueil d’origine permanent"
]) assert.ok(translations.includes(JSON.stringify(name) + ":"),
  "Missing English translation: " + name);

for (const id of ["io.gh.reisxd.tizentube.cobalt", "com.goodtoolapps.zeus",
  "com.vewd.core.browserui", "org.droidtv.nettvbrowser"])
  assert.ok(code.includes('"' + id + '"'), "Missing real package mapping: " + id);
assert.ok(code.includes('button:not(:disabled)'), "Disabled actions must not receive remote focus");
assert.ok(code.includes('contextmenu'), "Mouse right click must open the app menu");
const css = readFileSync("app/src/main/assets/ui.css", "utf8");
assert.ok(css.includes(".app.add-app .appmark svg") && css.includes("fill: none;"),
  "Add button must preserve its outlined plus icon");
const nativeSource = readFileSync("app/src/main/java/tv/niak/launcher/MainActivity.java", "utf8");
const manifest = readFileSync("app/src/main/AndroidManifest.xml", "utf8");
for (const action of ["system-apps", "system-permissions-all", "system-permissions-app",
  "system-startup-scan", "system-storage", "system-cache-clear",
  "system-developer", "system-wifi", "system-display", "system-about"]) {
  assert.ok(code.includes('"' + action + '"'), "Missing UI action " + action);
  assert.ok(nativeSource.includes('case "' + action + '"'), "Missing native action " + action);
}
assert.ok(nativeSource.includes("isNiakCacheFile(entry.getName())"),
  "Cache deletion must be limited to local approved file names");
assert.ok(nativeSource.includes(".setPositiveButton("),
  "Cache cleaning requires confirmation");
assert.ok(!/\.killBackgroundProcesses\s*\(|\.forceStopPackage\s*\(|Runtime\.getRuntime\s*\(/.test(nativeSource),
  "No global force-stop or shell execution in the native bridge");
assert.ok(!manifest.includes("android.permission.QUERY_ALL_PACKAGES"),
  "Do not request broad package visibility");
assert.ok(manifest.includes("android.intent.action.BOOT_COMPLETED"),
  "Startup discovery must be explicitly declared");
for (const title of ["Applications et système", "Autorisations des applications",
  "Applications au démarrage", "Stockage", "Mémoire vive (RAM)"])
  assert.ok(translations.includes(JSON.stringify(title) + ":"),
    "Missing system menu translation: " + title);
console.log("NiakTV app visibility and menu contracts: passed");
