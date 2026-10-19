# Security / Sécurité

[English](#english) · [Français](#francais) · [NiakTV](README.md)

<a id="english"></a>

## English

### Report a vulnerability privately

Use **[Report a vulnerability](https://github.com/niakw/niaktv/security/advisories/new)** in the repository’s Security tab. Private vulnerability reporting is enabled. Describe the affected version or commit, impact, prerequisites and a minimal reproduction using dummy data. Share the smallest log excerpt needed; never include a real API key, signing key or personal viewing history.

Ordinary crashes, UI bugs and setup questions belong in [Issues](https://github.com/niakw/niaktv/issues/new/choose) or [Discussions](https://github.com/niakw/niaktv/discussions). Please keep exploit details out of public issues until maintainers and the reporter have agreed on disclosure. Response and fix times depend on the issue and maintainer availability; there is no guaranteed turnaround.

### Scope and maintenance

Security fixes target the current code on `main`. Development APKs are not a maintained long-term release line. Android firmware, WebView and third-party apps have their own update and reporting channels.

The areas most relevant to NiakTV are the native WebView bridge, local resource routing, artwork downloads, launch-intent validation and private credential storage.

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

Utilisez **[Report a vulnerability](https://github.com/niakw/niaktv/security/advisories/new)** dans l’onglet Security du dépôt. Le signalement privé est activé. Précisez la version ou le commit concerné, l’impact, les conditions nécessaires et une reproduction minimale avec des données fictives. Limitez les logs à l’extrait utile ; ne joignez jamais de vraie clé API, clé de signature ou historique de visionnage personnel.

Les plantages ordinaires, anomalies d’interface et questions d’installation vont dans les [Issues](https://github.com/niakw/niaktv/issues/new/choose) ou [Discussions](https://github.com/niakw/niaktv/discussions). Gardez les détails d’exploitation hors des issues publiques jusqu’à un accord de divulgation entre maintenance et auteur du signalement. Les délais de réponse et de correction dépendent du problème et des disponibilités ; aucun délai fixe n’est garanti.

### Périmètre et maintenance

Les corrections de sécurité visent le code courant sur `main`. Les APK de développement ne constituent pas une branche de versions maintenue à long terme. Le firmware Android, WebView et les apps tierces ont leurs propres mises à jour et canaux de signalement.

Les parties particulièrement concernées dans NiakTV sont le pont WebView natif, l’accès aux ressources locales, les téléchargements d’illustrations, la validation des intents et le stockage privé des identifiants.

### Données conservées et connexions

- Favoris, ordre des apps, apparence et préférences Accueil restent dans le stockage Android privé de l’app.
- La clé TMDB configurée est conservée séparément dans des préférences privées, exclues des sauvegardes et jamais transmises à l’interface JavaScript. Le dialogue TMDB permet de l’oublier.
- Les programmes Nuvio sont lus via le fournisseur Android TV, avec autorisation. NiakTV ne lit ni les fichiers privés ni les identifiants Nuvio.
- Les téléchargements facultatifs vont vers TMDB et les hôtes d’images pris en charge. NiakTV n’a aucun service d’analyse ou de publicité. La WebView locale ne navigue pas vers des pages distantes.
- Les builds debug permettent l’inspection WebView par une connexion ADB autorisée. L’accès au débogage et les clés de signature restent des accès privés de développement.

Les limites réseau/cache et les essais réalisés sont décrits dans [Validation](VALIDATION.md#francais). Les permissions du service Accueil sont expliquées dans [Accessibilité](ACCESSIBILITY.md#francais).
