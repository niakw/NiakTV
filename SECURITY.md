# Security / Sécurité

[English](#english) · [Français](#francais) · [NiakTV](README.md)

<a id="english"></a>

## English

### Report a vulnerability privately

Use **[Report a vulnerability](https://github.com/niakw/NiakTV/security/advisories/new)** in the repository’s Security tab. Private vulnerability reporting is enabled. Describe the affected version or commit, impact, prerequisites and a minimal reproduction using dummy data. Share the smallest log excerpt needed; never include a real API key, signing key or personal viewing history.

Ordinary crashes, UI bugs and setup questions belong in [Issues](https://github.com/niakw/NiakTV/issues/new/choose) or [Discussions](https://github.com/niakw/NiakTV/discussions). Please keep exploit details out of public issues until maintainers and the reporter have agreed on disclosure. Response and fix times depend on the issue and maintainer availability; there is no guaranteed turnaround.

### Scope and maintenance

Security fixes target the current code on `main`. Development APKs are not a maintained long-term release line. Android firmware, WebView and third-party apps have their own update and reporting channels.

The areas most relevant to NiakTV are the native WebView bridge, local resource routing, artwork downloads, launch-intent validation and private credential storage.

### Build dependencies and the installed APK

The 52 Dependabot alerts reviewed on 21 October 2026 concerned transitive dependencies of the Android build and device-test tooling (AGP/lint/UTP), recorded in the submitted Gradle graph. They were not 52 separate libraries shipped with the launcher. Inspection of the published 0.3.1 APK’s defined DEX classes found none from Netty, Bouncy Castle, Protobuf, JDOM, jose4j or Commons Compress. This scoped check is not a general security audit of the app or Android.

`build.gradle` sets patched build-classpath versions: Netty 4.1.139.Final and Protobuf 3.25.9 through their BOMs, Bouncy Castle 1.86, Commons Compress 1.28.0, JDOM 2.0.6.1 and jose4j 0.9.7. Separate AGP device-test configurations are aligned to the same Netty/Protobuf versions. These rules do not add runtime libraries to the app. The Android workflow builds, runs unit tests and lint, and uploads a `dependency-report` showing the resolved build classpath and app runtime classpath. Dependabot checks Gradle and GitHub Actions weekly; its live alert list remains the reference for newly disclosed issues. Alerts must close through corrected dependency resolution, not blanket dismissal.

### What the app stores and contacts

- Favourites, app order, appearance and Home preferences stay in private Android app storage.
- A configured TMDB credential is stored separately in private preferences, excluded from backup and never passed to the JavaScript interface. The user can remove it in the TMDB dialog.
- Nuvio programmes are read through Android TV’s provider with permission. NiakTV does not read Nuvio’s private files or credentials.
- Optional artwork requests go to TMDB and supported image hosts. NiakTV has no analytics or advertising endpoint. The local WebView cannot browse remote pages.
- Debug builds expose WebView inspection through an authorised ADB connection. Treat debugging access and APK signing keys as private development access.

For network/cache limits and what has actually been exercised, see [Validation](VALIDATION.md#english). For Home service permissions, see [Accessibility](ACCESSIBILITY.md#english).

<a id="francais"></a>

## Français

### Signaler une vulnérabilité en privé

Utilisez **[Report a vulnerability](https://github.com/niakw/NiakTV/security/advisories/new)** dans l’onglet Security du dépôt. Le signalement privé est activé. Précisez la version ou le commit concerné, l’impact, les conditions nécessaires et une reproduction minimale avec des données fictives. Limitez les logs à l’extrait utile ; ne joignez jamais de vraie clé API, clé de signature ou historique de visionnage personnel.

Les plantages ordinaires, anomalies d’interface et questions d’installation vont dans les [Issues](https://github.com/niakw/NiakTV/issues/new/choose) ou [Discussions](https://github.com/niakw/NiakTV/discussions). Gardez les détails d’exploitation hors des issues publiques jusqu’à un accord de divulgation entre maintenance et auteur du signalement. Les délais de réponse et de correction dépendent du problème et des disponibilités ; aucun délai fixe n’est garanti.

### Périmètre et maintenance

Les corrections de sécurité visent le code courant sur `main`. Les APK de développement ne constituent pas une branche de versions maintenue à long terme. Le firmware Android, WebView et les apps tierces ont leurs propres mises à jour et canaux de signalement.

Les parties particulièrement concernées dans NiakTV sont le pont WebView natif, l’accès aux ressources locales, les téléchargements d’illustrations, la validation des intents et le stockage privé des identifiants.

### Dépendances de compilation et APK installé

Les 52 alertes Dependabot examinées le 21 octobre 2026 concernaient les dépendances transitives des outils de compilation et de tests Android (AGP/lint/UTP), enregistrées dans le graphe Gradle soumis. Elles ne correspondaient pas à 52 bibliothèques distinctes embarquées dans le lanceur. Le contrôle des classes DEX définies dans l’APK 0.3.1 publié n’a trouvé aucune classe Netty, Bouncy Castle, Protobuf, JDOM, jose4j ou Commons Compress. Ce contrôle ciblé ne constitue pas un audit général de l’app ou d’Android.

`build.gradle` définit des versions corrigées sur le classpath de compilation : Netty 4.1.139.Final et Protobuf 3.25.9 via leurs BOM, Bouncy Castle 1.86, Commons Compress 1.28.0, JDOM 2.0.6.1 et jose4j 0.9.7. Les configurations séparées des tests sur appareils AGP utilisent les mêmes versions Netty/Protobuf. Ces règles n’ajoutent aucune bibliothèque d’exécution à l’app. Le workflow Android compile, exécute les tests unitaires et le lint, puis publie un `dependency-report` montrant les dépendances résolues des outils et de l’app. Dependabot contrôle Gradle et GitHub Actions chaque semaine ; sa liste d’alertes reste la référence pour les nouvelles vulnérabilités. Les alertes doivent se fermer grâce aux versions corrigées, sans classement en masse comme ignorées.

### Données conservées et connexions

- Favoris, ordre des apps, apparence et préférences Accueil restent dans le stockage Android privé de l’app.
- La clé TMDB configurée est conservée séparément dans des préférences privées, exclues des sauvegardes et jamais transmises à l’interface JavaScript. Le dialogue TMDB permet de l’oublier.
- Les programmes Nuvio sont lus via le fournisseur Android TV, avec autorisation. NiakTV ne lit ni les fichiers privés ni les identifiants Nuvio.
- Les téléchargements facultatifs vont vers TMDB et les hôtes d’images pris en charge. NiakTV n’a aucun service d’analyse ou de publicité. La WebView locale ne navigue pas vers des pages distantes.
- Les builds debug permettent l’inspection WebView par une connexion ADB autorisée. L’accès au débogage et les clés de signature restent des accès privés de développement.

Les limites réseau/cache et les essais réalisés sont décrits dans [Validation](VALIDATION.md#francais). Les permissions du service Accueil sont expliquées dans [Accessibilité](ACCESSIBILITY.md#francais).
