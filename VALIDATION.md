# Validation & limits / Validation et limites

[English](#english) · [Français](#francais) · [NiakTV](README.md)

<a id="english"></a>

## English

### What each check establishes

| Check                | Coverage                                                                                                            | Does not establish                                   |
| -------------------- | ------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------- |
| Android CI           | Build, Android Lint, unit tests and frontend syntax                                                                 | Correct behaviour on every TV or firmware            |
| Unit tests           | App ordering/movement, changing app inventories, daily rotation and installed-only streaming fallbacks              | Network availability or manufacturer integration     |
| On-device UI checks  | Section order, viewport fit, fonts, visible focus, row navigation, drawers, EN/FR and missing-app fixture cases     | TalkBack support or an accessibility audit           |
| Manual device checks | Nuvio deep links, HDMI launch, original-home recovery, renderer recovery and persisted appearance/language settings | Multi-brand compatibility or long-duration stability |

[Android workflow results](https://github.com/niakw/NiakTV/actions/workflows/android.yml) identify the exact commit built. [Contributing](CONTRIBUTING.md#english) contains build prerequisites and commands. Documentation-only updates do not require reinstalling the launcher.

### Run the installed UI checks

Use a debuggable APK, an authorised ADB connection and Node.js 22 or later. Replace `TV_IP` with the connected device address and `PID` with the process number returned by the first command.

```sh
adb -s TV_IP:5555 shell pidof tv.niak.launcher
adb -s TV_IP:5555 forward tcp:39222 localabstract:webview_devtools_remote_PID
node scripts/check-ui.mjs
adb -s TV_IP:5555 forward --remove tcp:39222
```

The script temporarily applies fixture models to the local page for absent-app and language cases, then restores the native model. It does not uninstall apps or change their data. Avoid interacting with the launcher while it runs. WebView inspection is available only in debuggable builds.

### Device evidence and performance

The cinema interface and recovery paths have been exercised on Android TV 12. Appearance, language and Performance mode persistence have been checked after an app restart. Real Nuvio links opened the associated programme/source-selection flow; this does not validate a complete playback session.

An earlier cinema-build sample of 48 navigation frames measured a **32 ms median** and **46 ms 95th percentile**; all 48 exceeded the Android frame deadline. This is a short development-build observation, not a current benchmark of every mode or evidence of sustained 60 fps. No controlled speed comparison with Google home is claimed, and Performance mode’s gain has not been quantified. WebView uses a separate renderer, so host-process memory alone is not total launcher memory.

Electrical reboot, multi-day midnight rotation, extended playback and broad manufacturer coverage remain unverified. No personal device address, watch history or raw device screenshot is needed in this document.

### Implementation limits that affect the experience

- Programme support is currently specific to Nuvio’s Android TV publications. A fallback streaming shortcut does not provide that app’s watch history.
- At most 32 recent programmes are exposed. Programme artwork has a bounded cache; the first three images are prefetched and other images wait for stable focus.
- Hiding My selection stops programme reads, its observer and further artwork requests. Performance mode also forces a solid background and prevents cinema refreshes. An already-running bounded request can finish after a mode change or leaving the home screen.
- The cinema catalogue uses TMDB’s France now-playing feed. Metadata, regional availability and usable artwork depend on TMDB. Daily refresh happens when the home screen is active, not through a permanent background downloader.
- Home compatibility targets Google Android TV home. The current live-TV shortcut targets a manufacturer-specific activity; other firmware may require the TV’s own input selector. Some firmwares clear NiakTV’s accessibility activation after a force-stop.
- Accessibility beyond directional-remote checks needs dedicated evaluation; see [Accessibility](ACCESSIBILITY.md#english).

<a id="francais"></a>

## Français

### Ce que chaque contrôle établit

| Contrôle                  | Couverture                                                                                                             | Ne permet pas d’affirmer                                          |
| ------------------------- | ---------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------- |
| CI Android                | Compilation, Android Lint, tests unitaires et syntaxe de l’interface                                                   | Un fonctionnement correct sur toutes les TV et tous les firmwares |
| Tests unitaires           | Ordre/déplacement des apps, inventaires qui évoluent, rotation quotidienne et app de remplacement réellement installée | La disponibilité réseau ou l’intégration fabricant                |
| Contrôles UI sur appareil | Ordre des blocs, cadrage, polices, focus, navigation, panneaux, EN/FR et cas d’app absente simulés                     | La prise en charge de TalkBack ou un audit d’accessibilité        |
| Essais manuels            | Liens Nuvio, HDMI, retour à l’accueil d’origine, récupération du renderer et conservation des réglages                 | Une compatibilité multimarque ou une stabilité de longue durée    |

Les [résultats du workflow Android](https://github.com/niakw/NiakTV/actions/workflows/android.yml) identifient le commit compilé. [Contribution](CONTRIBUTING.md#francais) contient les prérequis et commandes de compilation. Une mise à jour documentaire ne demande pas de réinstaller le lanceur.

### Exécuter les contrôles de l’interface installée

Utilisez un APK débogable, une connexion ADB autorisée et Node.js 22 ou plus récent. Remplacez `TV_IP` par l’adresse de l’appareil connecté et `PID` par le numéro de processus retourné par la première commande.

```sh
adb -s TV_IP:5555 shell pidof tv.niak.launcher
adb -s TV_IP:5555 forward tcp:39222 localabstract:webview_devtools_remote_PID
node scripts/check-ui.mjs
adb -s TV_IP:5555 forward --remove tcp:39222
```

Le script applique temporairement des modèles de contrôle à la page locale pour les cas d’app absente et les langues, puis rétablit le modèle natif. Il ne désinstalle aucune app et ne modifie pas leurs données. Évitez d’utiliser le lanceur pendant son exécution. L’inspection WebView n’est disponible que dans les builds débogables.

### Essais sur appareil et performances

L’interface cinéma et les mécanismes de récupération ont été essayés sur Android TV 12. La conservation de l’apparence, de la langue et du mode performance a été vérifiée après redémarrage de l’app. Les liens Nuvio réels ont ouvert le programme et son choix de sources ; cela ne valide pas une session de lecture complète.

Un échantillon antérieur de 48 images de navigation en mode cinéma a mesuré **32 ms en médiane** et **46 ms au 95e percentile** ; les 48 images dépassaient l’échéance de rendu Android. C’est une observation courte sur un build de développement, pas un benchmark actuel de tous les modes ni la preuve d’un 60 fps constant. Aucune comparaison contrôlée avec Google n’est revendiquée et le gain du mode performance n’est pas chiffré. WebView utilise un renderer séparé : la mémoire du processus hôte ne représente pas toute la mémoire du lanceur.

Le redémarrage électrique, la rotation à minuit sur plusieurs jours, la lecture prolongée et une large couverture de fabricants restent non vérifiés. Ce document n’a besoin d’aucune adresse personnelle d’appareil, historique de visionnage ou capture brute de TV.

### Limites de l’implémentation visibles à l’usage

- La lecture des programmes est actuellement propre aux publications Android TV de Nuvio. Le raccourci vers une autre app ne donne pas accès à son historique de lecture.
- Au maximum 32 programmes récents sont exposés. Le cache d’illustrations est borné ; trois images sont préchargées et les suivantes attendent un focus stable.
- Masquer Ma sélection coupe la lecture des programmes, leur observateur et les nouvelles demandes d’illustrations. Le mode performance impose aussi un fond uni et empêche les actualisations cinéma. Une requête bornée déjà en cours peut se terminer après un changement de mode ou la sortie de l’accueil.
- Le catalogue cinéma utilise le flux TMDB des films à l’affiche en France. Métadonnées, disponibilité régionale et illustrations exploitables dépendent de TMDB. L’actualisation quotidienne se fait quand l’accueil est actif, sans téléchargeur permanent en arrière-plan.
- La compatibilité Accueil cible Google Android TV. Le raccourci TV en direct actuel cible une activité fabricant ; un autre firmware peut demander d’utiliser son propre sélecteur d’entrées. Certains firmwares retirent l’activation du service NiakTV après un arrêt forcé.
- L’accessibilité au-delà de la télécommande directionnelle demande une évaluation dédiée ; voir [Accessibilité](ACCESSIBILITY.md#francais).
