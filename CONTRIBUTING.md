# Contributing / Contribuer

[English](#english) · [Français](#francais) · [NiakTV](README.md)

<a id="english"></a>

## English

### Start with the right conversation

Use [Discussions](https://github.com/niakw/NiakTV/discussions) for setup questions and early ideas. Use the [issue templates](https://github.com/niakw/NiakTV/issues/new/choose) for reproducible bugs, accessibility barriers or a concrete feature proposal. English and French are equally welcome. Report vulnerabilities through [Security](SECURITY.md), rather than a public issue.

For a substantial change, describe the user problem and intended behaviour before starting a large implementation. Small fixes can go straight to a pull request.

### Build locally

Install JDK 17, Android SDK Platform 35 and Build Tools 35.0.0. Point `ANDROID_HOME` or an untracked `local.properties` at the SDK. The wrapper supplies Gradle 8.13 with a SHA256 check; the project uses AGP 8.9.2.

```sh
./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug
node --check app/src/main/assets/i18n.js
node --check app/src/main/assets/ui.js
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

The debug APK is `app/build/outputs/apk/debug/app-debug.apk`. Follow the [installation guide](INSTALL.md) to try it on a TV. Local debug builds use `.signing/niaktv-debug.keystore` if present; keep it private and retain the signing key for future updates.

### Publish an installer

End users install the signed APK attached to [Releases](https://github.com/niakw/NiakTV/releases), not a CI artifact. Build and test the exact version, retain its signing key privately for future updates, then attach the versioned APK and `SHA256SUMS.txt` to a release targeting the verified source commit. Include EN/FR notes and update both README download links and installation guides. Never publish signing keys, local preferences or personal credentials. CI debug artifacts are for development and may use temporary signing keys.

### Rebuild the font assets

The APK bundles WOFF2 fonts. Original TTF files are kept in `tools/font-sources/`; their OFL notices remain in `app/src/main/assets/fonts/`. With `fonttools[woff]==4.60.1` installed in a local Python environment, run `python tools/compress_fonts.py`. The conversion keeps all glyphs, metrics and variable weight axes and checks them against the source. It does not subset characters.

### Find the relevant code

| Area                                                 | Location                                                     |
| ---------------------------------------------------- | ------------------------------------------------------------ |
| Home structure, visual states and navigation         | `app/src/main/assets/index.html`, `ui.css`, `ui.js`          |
| English interface strings                            | `app/src/main/assets/i18n.js`                                |
| Android discovery, settings and local WebView bridge | `MainActivity.java` in `app/src/main/java/tv/niak/launcher/` |
| TV programmes and cinema backgrounds                 | `ResumeStore.java`, `WallpaperStore.java`                    |
| Installed-app fallback and ordering                  | `DefaultApps.java`, `Order.java`                             |
| Optional Home redirection                            | `HomeRedirectService.java`                                   |

### Keep the experience coherent

- Keep every action reachable with arrows, OK and Back. Test focus and scrolling with the selection visible and hidden; include hover and pressed states for pointer input.
- Update French and English together, including native dialogs and accessibility labels. Names supplied by apps or programmes are content, not interface translations.
- Keep solid/gradient and Performance mode usable without a TMDB key or Nuvio. An absent app must not leave a broken launch button.
- Avoid permanent animation, image downloads during rapid navigation and full-page redraws for an artwork update. Measure performance claims on a device and name the build/mode tested.
- Keep the WebView local, native actions validated and the original home reachable. Do not introduce a requirement to disable system components.

Run checks appropriate to your change. UI changes also need the [on-device checks](VALIDATION.md#english); documentation-only changes need link, language and privacy checks rather than an APK rebuild. CI performs the Android build, unit tests, lint and JavaScript syntax checks.

### Send a reviewable pull request

Explain the problem, resulting behaviour and verification. For a UI change, include a screenshot or recording using neutral content when practical. State any device-specific limitation. Do not include credentials, signing keys, device serials, local addresses or personal viewing history in commits, logs or images. Keep both README languages and affected guides in sync.

The project uses [GNU GPL v3](LICENSE); keep third-party notices with the assets they cover. Participation follows the [Code of conduct](CODE_OF_CONDUCT.md).

<a id="francais"></a>

## Français

### Choisir le bon échange

Utilisez [Discussions](https://github.com/niakw/NiakTV/discussions) pour les questions d’installation et les premières idées. Les [modèles d’issues](https://github.com/niakw/NiakTV/issues/new/choose) servent aux anomalies reproductibles, obstacles d’accessibilité et propositions concrètes. Le français et l’anglais sont les bienvenus. Signalez les vulnérabilités par le canal [Sécurité](SECURITY.md#francais), pas dans une issue publique.

Pour une évolution importante, décrivez le besoin utilisateur et le comportement envisagé avant de lancer une grosse implémentation. Une petite correction peut faire directement l’objet d’une pull request.

### Compiler localement

Installez JDK 17, Android SDK Platform 35 et Build Tools 35.0.0. Faites pointer `ANDROID_HOME` ou un `local.properties` non versionné vers le SDK. Le wrapper fournit Gradle 8.13 avec vérification SHA256 ; le projet utilise AGP 8.9.2.

```sh
./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug
node --check app/src/main/assets/i18n.js
node --check app/src/main/assets/ui.js
```

Sous Windows, utilisez `gradlew.bat` à la place de `./gradlew`.

L’APK debug se trouve dans `app/build/outputs/apk/debug/app-debug.apk`. Suivez le [guide d’installation](INSTALL.fr.md) pour l’essayer sur une TV. Si `.signing/niaktv-debug.keystore` existe, les builds debug locaux l’utilisent ; gardez cette clé privée et conservez-la pour les mises à jour.

### Publier un installateur

Les utilisateurs installent l’APK signé joint aux [Releases](https://github.com/niakw/NiakTV/releases), pas un artefact CI. Compilez et vérifiez la version exacte, conservez sa clé de signature privée pour les mises à jour, puis joignez l’APK versionné et `SHA256SUMS.txt` à une release ciblant le commit source vérifié. Ajoutez des notes EN/FR et actualisez les liens des deux README et les guides d’installation. Ne publiez jamais de clé de signature, préférences locales ou identifiants personnels. Les artefacts debug CI servent au développement et peuvent utiliser des clés temporaires.

### Régénérer les polices

L’APK embarque les polices WOFF2. Les sources TTF restent dans `tools/font-sources/` et les notices OFL dans `app/src/main/assets/fonts/`. Avec `fonttools[woff]==4.60.1` installé dans un environnement Python local, lancez `python tools/compress_fonts.py`. La conversion conserve et vérifie les glyphes, métriques et axes de graisses variables. Aucun caractère n’est retiré.

### Trouver le code concerné

| Partie                                              | Emplacement                                                    |
| --------------------------------------------------- | -------------------------------------------------------------- |
| Structure de l’accueil, états visuels et navigation | `app/src/main/assets/index.html`, `ui.css`, `ui.js`            |
| Traductions anglaises de l’interface                | `app/src/main/assets/i18n.js`                                  |
| Découverte Android, réglages et pont WebView local  | `MainActivity.java` dans `app/src/main/java/tv/niak/launcher/` |
| Programmes TV et fonds cinéma                       | `ResumeStore.java`, `WallpaperStore.java`                      |
| App de remplacement et ordre des apps               | `DefaultApps.java`, `Order.java`                               |
| Redirection Accueil facultative                     | `HomeRedirectService.java`                                     |

### Garder une expérience cohérente

- Toute action doit rester accessible avec les flèches, OK et Retour. Vérifiez focus et défilement avec la sélection visible et masquée ; prévoyez survol et état pressé pour le pointeur.
- Modifiez ensemble le français et l’anglais, y compris les dialogues natifs et les libellés d’accessibilité. Les noms fournis par les apps et programmes sont du contenu, pas des traductions d’interface.
- Les fonds unis/dégradés et le mode performance doivent fonctionner sans clé TMDB ni Nuvio. Une app absente ne doit pas laisser de bouton d’ouverture inutilisable.
- Évitez les animations permanentes, les téléchargements pendant une navigation rapide et la reconstruction de page à chaque illustration reçue. Mesurez les gains annoncés sur appareil et précisez le build et le mode testés.
- Gardez une WebView locale, des actions natives validées et un retour à l’accueil d’origine. N’introduisez pas de désactivation obligatoire de composants système.

Exécutez les contrôles adaptés au changement. Une modification d’interface demande aussi les [vérifications sur appareil](VALIDATION.md#francais) ; une modification documentaire demande des contrôles de liens, de langues et de confidentialité plutôt qu’une recompilation de l’APK. La CI compile Android et exécute tests unitaires, lint et vérification syntaxique JavaScript.

### Proposer une pull request facile à relire

Expliquez le problème, le comportement obtenu et les vérifications. Pour une évolution visuelle, joignez si possible une capture ou une vidéo avec du contenu neutre. Précisez les limites propres à l’appareil. N’incluez ni identifiants, clés de signature, numéros de série, adresses locales ou historique de visionnage personnel dans les commits, logs et images. Gardez les deux README et les guides concernés synchronisés.

Le projet utilise [GNU GPL v3](LICENSE) ; conservez les notices tierces avec leurs ressources. La participation suit le [Code de conduite](CODE_OF_CONDUCT.md#francais).
