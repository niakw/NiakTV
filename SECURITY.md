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

### Automated source analysis

GitHub's built-in CodeQL default setup already scans the repository, including Java and JavaScript, without adding a duplicate Actions workflow. Extended queries and local-source threat modeling have been requested. Java no-build analysis can be less complete than an instrumented Android build; manual review and Android lint remain necessary.

### Current open alerts and application-management safeguards (7 November 2026)

The repository had **six Dependabot alerts** for Bouncy Castle (three), Apache HttpClient (one), Commons Lang (one), and Guava (one), all from transitive build-tool dependencies. On 7 November 2026, GitHub marked all six as **fixed** (not dismissed) after the Gradle dependency graph refreshed. The existing CI report verified selected patched versions for the first five; Guava was upgraded to 33.7.2-jre and its version checks were extended. Future alerts still require a fresh resolved dependency graph and investigation, not blanket dismissal. At the time of this review GitHub's code-scanning and secret-scanning endpoints report no open alerts; this is not a certificate of code safety.

NiakTV never calls \`pm clear\` or an equivalent command to delete another app's data. Unsupported force-stop and clear-cache actions delegate to Android Settings. Hiding an app modifies only NiakTV private preferences and does not disable its Android package. Uninstall uses the OS confirmation interface and checks the selected package against the installed launchable-app inventory. Temporary Google Home launch does not silently disable system apps or remove NiakTV's persistent redirection configuration.

### System tools threat model

System status relies on public Storage/ActivityManager APIs; it does not inspect other apps' private storage or their processes. Startup detection declares a narrow package-visibility query for BOOT_COMPLETED receivers, never QUERY_ALL_PACKAGES, and results cannot be treated as proof an app has started. Permission changes, app management and other system controls are performed by Android Settings, not by a WebView-originated shell command. Clearing cache is limited to filename-allowlisted artwork and generated files under NiakTV's own private getCacheDir(), and requires an explicit confirmation. No mass force-stop, background-process killer, root request, shell bridge or silent cross-app cache deletion is present.

### What the app stores and contacts### What the app stores and contacts

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

### Analyse automatique du code source

La configuration CodeQL native de GitHub analyse déjà le dépôt, notamment Java et JavaScript, sans ajouter de workflow redondant. Les requêtes étendues et le modèle de menace prenant en compte les sources locales ont été demandés. L’analyse Java sans compilation peut être moins complète qu’une compilation Android instrumentée ; elle ne dispense pas d’une revue manuelle ni d’Android Lint.

### Alertes ouvertes et garde-fous de gestion des apps (7 novembre 2026)

Le dépôt a compté **six alertes Dependabot** : Bouncy Castle (trois), Apache HttpClient (une), Commons Lang (une) et Guava (une), sur des dépendances transitives d’outils de compilation. Au 7 novembre 2026, GitHub a marqué ces six alertes comme **corrigées** (et non ignorées) après actualisation du graphe Gradle. Le rapport de CI antérieur confirmait les versions corrigées pour les cinq premières ; Guava est passée à 33.7.2-jre et le contrôle des versions a été renforcé. Toute nouvelle alerte devra être examinée sur le graphe réellement résolu, sans classement global comme ignorée. Les API *code scanning* et *secret scanning* ne renvoient aucune alerte ouverte à cette date ; cela ne prouve pas l’absence de vulnérabilités.

NiakTV n’exécute jamais \`pm clear\` ni de commande équivalente qui supprimerait les données privées d’une application tierce. Les actions Forcer l’arrêt et Vider le cache non autorisées renvoient aux paramètres Android. Masquer une app change uniquement les préférences privées NiakTV et ne désactive aucun package Android. La désinstallation passe par la confirmation Android après vérification de l’app dans l’inventaire. Le retour temporaire à Google ne désactive aucun composant système et préserve la configuration de redirection durable.

### Modèle de menace des outils système

Les mesures proviennent d’API publiques de stockage et d’ActivityManager ; NiakTV ne lit ni les fichiers privés ni les processus internes des autres applications. Le détecteur de démarrage déclare uniquement une requête ciblée des récepteurs BOOT_COMPLETED, sans QUERY_ALL_PACKAGES : les résultats ne prouvent pas une exécution. Les permissions et commandes protégées passent par les réglages Android, sans pont shell vers la WebView. Le nettoyage confirmé est limité aux fichiers d’illustration et aux fichiers générés dans le seul getCacheDir() privé de NiakTV, avec noms autorisés explicitement. Aucun forçage d’arrêt global, outil root ou effacement silencieux des caches tiers n’est ajouté.

### Données conservées et connexions### Données conservées et connexions

- Favoris, ordre des apps, apparence et préférences Accueil restent dans le stockage Android privé de l’app.
- La clé TMDB configurée est conservée séparément dans des préférences privées, exclues des sauvegardes et jamais transmises à l’interface JavaScript. Le dialogue TMDB permet de l’oublier.
- Les programmes Nuvio sont lus via le fournisseur Android TV, avec autorisation. NiakTV ne lit ni les fichiers privés ni les identifiants Nuvio.
- Les téléchargements facultatifs vont vers TMDB et les hôtes d’images pris en charge. NiakTV n’a aucun service d’analyse ou de publicité. La WebView locale ne navigue pas vers des pages distantes.
- Les builds debug permettent l’inspection WebView par une connexion ADB autorisée. L’accès au débogage et les clés de signature restent des accès privés de développement.

Les limites réseau/cache et les essais réalisés sont décrits dans [Validation](VALIDATION.md#francais). Les permissions du service Accueil sont expliquées dans [Accessibilité](ACCESSIBILITY.md#francais).
