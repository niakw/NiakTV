# Development & validation / Développement et validation

**[English](#english) · [Français](#français)**

## English

### Build

Requires JDK 17, Android SDK 35 and Build Tools 35.0.0. Point `ANDROID_HOME` or an ignored `local.properties` file at the SDK. The Gradle wrapper pins Gradle 8.13 with a SHA256 check; AGP is 8.9.2.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
node --check app/src/main/assets/i18n.js
node --check app/src/main/assets/ui.js
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. A local `.signing/niaktv-debug.keystore`, if present, is used for debug builds and must never be committed. Keep the signing identity stable for updates. CI builds are development artifacts, not production-signed releases.

### Checks

Unit tests cover app ordering, removed/new apps, movement, daily wallpaper rotation and installed-only streaming fallbacks. `scripts/check-ui.mjs` checks the installed debug WebView, including navigation, optional content and language changes. Use a reference device without private media when publishing screenshots or test output.

```sh
adb -s TV_IP:5555 shell pidof tv.niak.launcher
# Replace PID with the number returned above.
adb -s TV_IP:5555 forward tcp:39222 localabstract:webview_devtools_remote_PID
node scripts/check-ui.mjs
```

The device smoke checks use fixture models for missing-app cases; they do not uninstall the user's apps. WebView debugging is enabled only in debuggable Android builds.

### Runtime boundaries

- Local virtual origin `https://niaktv.local/`; blocked remote navigation, WebView network, cookies, file/content access and DOM storage. Native networking is restricted to approved artwork hosts.
- Nuvio data comes only from Android TV programme tables with permission. No access to private Nuvio files. Resume links must explicitly target Nuvio.
- At most 32 recent programmes and 16 cached artwork JPEGs. The first three illustrations are prefetched; further downloads wait for stable focus.
- Hiding My selection stops programme reads, its content observer and artwork requests. Performance mode also forces a solid background and prevents wallpaper refreshes. An already-running bounded request may complete when settings change.
- Solid/gradient backgrounds use CSS without fetching film images. Cinema refreshes occur only in the foreground; offline failures preserve the last cached backdrop.
- Optional Home redirection observes Google home window changes only. Google home stays available. Some firmwares clear accessibility activation after force-stop; re-enable the service if needed.

### Evidence and limits

The cinema interface has been exercised on Android TV 12, including real Nuvio deep links, HDMI launch, original-home recovery and WebView renderer recovery. A short development-build navigation sample measured a 32 ms median frame and 46 ms 95th percentile; this does **not** establish sustained 60 fps. No controlled performance comparison against Google home is claimed. Performance mode reduces content work; its speedup is not quantified.

Not validated across all TV brands: electrical reboot, multi-day midnight rotation and long playback sessions. Manufacturer input and Home behaviour varies. The live TMDB catalogue depends on TMDB's current now-playing metadata and regional availability.

## Français

### Compiler

JDK 17, SDK Android 35 et Build Tools 35.0.0 requis. Faites pointer `ANDROID_HOME` ou un fichier `local.properties` ignoré vers le SDK. Le wrapper fixe Gradle 8.13 avec vérification SHA256 ; AGP est en version 8.9.2.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
node --check app/src/main/assets/i18n.js
node --check app/src/main/assets/ui.js
```

APK : `app/build/outputs/apk/debug/app-debug.apk`. Une clé locale `.signing/niaktv-debug.keystore`, si présente, sert aux versions debug et ne doit jamais être versionnée. Conservez la même signature pour les mises à jour. Les builds CI sont des artefacts de développement, pas des versions signées pour la production.

### Vérifier

Les tests unitaires couvrent l’ordre des apps, les ajouts/suppressions, les déplacements, la rotation quotidienne et le choix d’une app de streaming réellement installée. `scripts/check-ui.mjs` vérifie la WebView debug installée, dont la navigation, les contenus facultatifs et les langues. Utilisez un appareil sans médias personnels pour publier des captures ou des résultats.

```sh
adb -s TV_IP:5555 shell pidof tv.niak.launcher
# Remplacez PID par le nombre obtenu ci-dessus.
adb -s TV_IP:5555 forward tcp:39222 localabstract:webview_devtools_remote_PID
node scripts/check-ui.mjs
```

Les contrôles sur appareil utilisent des modèles fictifs pour les cas d’app absente ; ils ne désinstallent aucune app. Le débogage WebView n’est actif que dans les builds Android débogables.

### Limites de fonctionnement

- Origine virtuelle locale `https://niaktv.local/` ; navigation distante, réseau WebView, cookies, accès fichier/contenu et stockage DOM bloqués. Le réseau natif est limité aux hôtes d’illustrations autorisés.
- Les reprises Nuvio proviennent uniquement des programmes Android TV autorisés, sans accès aux fichiers privés. Les liens doivent cibler explicitement Nuvio.
- Au maximum 32 programmes récents et 16 illustrations JPEG en cache. Trois images préchargées ; les suivantes attendent un focus stable.
- Masquer Ma sélection coupe la lecture des programmes, leur observateur et les demandes d’illustrations. Le mode performance impose aussi un fond uni et empêche les actualisations de fond. Une requête bornée déjà en cours peut se terminer lors du changement.
- Les fonds unis/dégradés utilisent du CSS sans télécharger d’image de film. Le cinéma s’actualise uniquement au premier plan ; une panne réseau conserve le fond en cache.
- La redirection Accueil facultative observe uniquement les changements de fenêtre de Google. L’accueil Google reste accessible. Certains firmwares retirent l’activation du service après un arrêt forcé ; réactivez-le si nécessaire.

### Résultats et limites

L’interface cinéma a été essayée sur Android TV 12 : liens Nuvio réels, ouverture HDMI, retour à l’accueil d’origine et récupération du renderer WebView. Un court échantillon de navigation debug a mesuré 32 ms par image en médiane et 46 ms au 95e percentile ; cela ne valide **pas** un 60 fps constant. Aucune comparaison contrôlée avec Google n’est revendiquée. Le mode performance réduit le travail lié aux contenus ; son gain n’est pas chiffré.

Non validés sur toutes les marques : redémarrage électrique, rotation à minuit sur plusieurs jours et lecture prolongée. Les entrées et le bouton Accueil dépendent du fabricant. Le catalogue TMDB suit les métadonnées « à l’affiche » et leur disponibilité régionale.
