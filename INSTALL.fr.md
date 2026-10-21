# Installer NiakTV

[English](INSTALL.md) · **Français** · [Retour à NiakTV](README.fr.md)

## Avant de commencer

Il vous faut un appareil Android TV sous Android 8 ou plus récent qui autorise l’installation d’APK. Un ordinateur avec [Android Platform Tools](https://developer.android.com/tools/releases/platform-tools) est nécessaire uniquement pour la méthode ADB facultative. Les réglages et le bouton Accueil dépendent du fabricant. Les versions actuelles sont des APK de développement.

Nuvio et une clé TMDB sont facultatifs. Commencez sans ces services si vous voulez simplement vos apps et vos entrées.

## 1. Récupérer l’APK

Téléchargez directement **[niaktv-0.3.1.apk](https://github.com/niakw/NiakTV/releases/latest/download/niaktv-0.3.1.apk)** depuis la [dernière release](https://github.com/niakw/NiakTV/releases/latest). L’installateur signé est prêt à utiliser : aucun outil de compilation, compte GitHub ou ZIP à extraire. La release contient aussi `SHA256SUMS.txt` pour vérifier le fichier téléchargé.

## 2. Installer sur la TV

Transférez l’APK sur la TV avec une clé USB ou votre app de transfert habituelle. Ouvrez-le avec un gestionnaire de fichiers Android TV, autorisez **Installer des applications inconnues** pour cette source si Android le demande, puis confirmez l’installation. Ouvrez NiakTV depuis vos apps. La prise en charge USB et du gestionnaire de fichiers dépend de l’appareil.

### Autre méthode : depuis un ordinateur avec ADB

Activez les options développeur et le débogage ADB sur la TV. Si le firmware propose ADB par réseau sur le port 5555, reliez la TV et l’ordinateur au même réseau de confiance. Remplacez `TV_IP` par l’adresse de votre TV ; acceptez la demande d’autorisation sur le téléviseur.

```sh
adb connect TV_IP:5555
adb devices
adb -s TV_IP:5555 install -r niaktv-0.3.1.apk
adb -s TV_IP:5555 shell am start -n tv.niak.launcher/.MainActivity
```

L’appareil doit apparaître comme `device`, et non `unauthorized` ou `offline`. Certains appareils Android TV utilisent un appairage ou un autre moyen de débogage : suivez la procédure proposée par l’appareil plutôt que de supposer que le port 5555 est ouvert.

## 3. Configurer le bouton Accueil

Si Android propose de choisir l’application d’accueil par défaut, sélectionnez NiakTV. Sur certaines TV qui imposent Google, **Réglages → Bouton Accueil** ouvre les paramètres d’accessibilité : activez **NiakTV — bouton Accueil**, puis testez la touche Accueil.

Ce service facultatif cible le lanceur Google Android TV. Il ne remplace pas universellement tous les lanceurs fabricants. Il ne lit pas le contenu des fenêtres et ne nécessite aucune désactivation d’app système.

Le sous-titre de **Réglages → Bouton Accueil** distingue l’accueil Android natif, le relais connecté et la redirection désactivée dans NiakTV. Si le relais est déconnecté, ouvrez ce réglage et vérifiez le service NiakTV dans l’accessibilité Android. Un service désactivé par le système doit être réactivé par l’utilisateur ; NiakTV ne peut pas s’accorder cette autorisation. Évitez l’arrêt forcé du lanceur. Google reste installé et le retour à l’accueil d’origine reste disponible.

<a id="advanced-home"></a>

### Accueil natif sur un firmware qui privilégie Google

**Configuration avancée facultative :** installez et ouvrez d’abord NiakTV avec succès. Désactiver le lanceur Google modifie l’accueil système et les écrans propres à ce lanceur ; le package et ses données restent présents. Ce choix doit être explicite. L’app n’exécute pas ces commandes à votre place.

```sh
adb -s TV_SERIAL shell am start -n tv.niak.launcher/.MainActivity
adb -s TV_SERIAL shell pm disable-user --user 0 com.google.android.tvlauncher
adb -s TV_SERIAL shell cmd package set-home-activity --user 0 tv.niak.launcher/.MainActivity
adb -s TV_SERIAL shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```

Remplacez `TV_SERIAL` par l’appareil listé par `adb devices`. Vérifiez que le résolveur indique `tv.niak.launcher/.MainActivity`, puis testez Accueil depuis NiakTV et une autre app. Dans ce mode, Accueil ouvre directement NiakTV sans nécessiter le relais de compatibilité.

Pour rétablir Google : **Réglages → Accueil d’origine → Ouvrir les réglages → Activer**, puis appuyez sur Accueil. La redirection NiakTV est coupée et les réglages Android de Google s’ouvrent. Solution ADB de secours : `adb -s TV_SERIAL shell pm default-state --user 0 com.google.android.tvlauncher`, puis choisissez **Accueil d’origine** dans NiakTV. Aucune donnée d’app n’est effacée.

## 4. Choisir son ambiance

| Vous souhaitez…                                            | Choisissez…                                                                                             |
| ---------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| Vos apps et entrées avec moins de travail lié aux contenus | **Réglages → Mode performance**                                                                         |
| Un fond simple                                             | **Style du fond → Uni** ou **Dégradé**, puis **Palette**                                                |
| Des visuels cinéma                                         | **Style du fond → Cinéma**, puis **Films à l’affiche · TMDB** pour saisir votre clé ou jeton de lecture |
| Aucun bloc de sélection                                    | **Bloc Ma sélection → Masqué**                                                                          |
| Une autre langue                                           | **Langue / Language → Français, English ou Langue du téléviseur**                                       |

Le mode performance conserve vos choix précédents de fond et de bloc pour les rétablir à sa désactivation. Choisir un fond ou modifier la visibilité du bloc quitte ce mode. Changer la palette ou la langue le laisse actif.

Avec Nuvio installé, **Reprises Nuvio** peut demander l’accès aux programmes. Sans Nuvio, la carte principale devient un raccourci vers une app de streaming compatible déjà installée, ou disparaît si aucune n’est trouvée. L’historique des autres services n’est pas importé.

Les affiches des reprises sont conservées dans un cache privé limité à 64 fichiers et 24 Mo. Par défaut, seules les trois reprises les plus récentes sont affichées et préchargées. **Réglages → Nombre de reprises** fait défiler **3, 5, 8 ou Toutes (32 maximum)** sur une seule ligne. Le nombre choisi limite aussi les téléchargements automatiques, sans modifier l’historique Nuvio. Les images servies par NiakTV (affiches et fond) utilisent WebP à qualité 80 ; les anciens fichiers sont convertis localement lorsque réutilisés. Les icônes d’apps utilisent des SVG compacts ou des monogrammes intégrés, sans image raster à charger. Un appui long sur un raccourci de Ma sélection permet de le déplacer dans cette rangée ; Apps TV conserve son ordre indépendant. Les formats suivent automatiquement le classement : bannière jusqu’à 1 200 × 308 px pour la première reprise, petites cartes jusqu’à 288 × 332 px pour les suivantes, recadrées avant compression et sans agrandissement artificiel. Le cache distingue l’URL de l’œuvre et sa variante ; un changement de position génère la variante manquante ou réutilise celle déjà prête. Les sources non recadrées et les variantes partagent la même limite de cache. Les affiches déjà en cache sont rattachées avant le rendu et les cartes inchangées restent en place au retour. Le fond cinéma préparé est réutilisé au démarrage. Le mode performance continue de couper les reprises et leurs téléchargements. Android peut vider son cache : un nouveau téléchargement sera alors nécessaire.

**Application de Ma sélection** permet de choisir Automatique ou une app installée (YouTube, Apple TV, Netflix, etc.). Les reprises Nuvio ne sont lues et préchargées que lorsque Nuvio est l’app effective du bloc. Une autre app affiche son raccourci. Si l’app choisie est désinstallée, NiakTV revient automatiquement à une app présente, ou masque la carte si aucune n’est trouvée.

## Mises à jour

Utilisez `adb install -r` avec un APK signé par **la même clé** que la version installée. L’APK 0.3.1 publié utilise la clé de signature locale conservée par le projet. Installez les APK des releases ; les artefacts GitHub Actions servent aux contrôles de développement et peuvent utiliser une autre clé temporaire. `INSTALL_FAILED_UPDATE_INCOMPATIBLE` indique une incompatibilité de signature : récupérez un APK construit avec la clé d’origine. Désinstaller n’est pas une mise à jour transparente : cela supprime les préférences et la clé TMDB enregistrée.

## Revenir à l’accueil d’origine

Ouvrez **Réglages → Accueil d’origine** pour couper la redirection NiakTV et ouvrir Google. Si Google a été désactivé avec la configuration avancée ci-dessus, NiakTV vous guide vers ses paramètres Android pour choisir **Activer** au préalable. Pour un retour durable, désactivez uniquement le service **NiakTV — bouton Accueil** dans les paramètres d’accessibilité Android, puis sélectionnez l’accueil d’origine si la TV propose ce choix. Cette action cible actuellement Google Android TV ; un autre accueil fabricant peut nécessiter une sélection dans les paramètres Android.

## Dépannage

| Ce que vous observez                   | À vérifier                                                                                                                                                                                        |
| -------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| La connexion ADB échoue                | Le débogage est-il actif, l’adresse et le moyen de connexion corrects, et l’ordinateur autorisé sur la TV ?                                                                                       |
| Accueil revient sur Google             | Vérifiez le service d’accessibilité NiakTV. Certains firmwares le désactivent après un arrêt forcé.                                                                                               |
| Aucun titre Nuvio                      | Affichez Ma sélection, désactivez le mode performance, puis vérifiez la publication Nuvio et l’autorisation via **Reprises Nuvio**. Un flux vide n’est pas remplacé par une progression inventée. |
| TMDB conserve l’ancien fond            | Vérifiez le mode Cinéma, le changement quotidien et votre clé personnelle. Les tentatives automatiques en échec sont espacées ; le fond précédent reste en cache.                                 |
| Un raccourci TV ou HDMI ne s’ouvre pas | L’intégration dépend du fabricant. Utilisez le sélecteur d’entrées de la TV et précisez le raccourci concerné dans un signalement.                                                                |

Après l’installation, vous pouvez vous déconnecter avec `adb disconnect TV_IP:5555` et désactiver le débogage lorsque vous n’en avez plus besoin.

**La suite :** [NiakTV](README.fr.md) · [Accessibilité](ACCESSIBILITY.md#francais) · [Signaler une anomalie](https://github.com/niakw/NiakTV/issues/new/choose)
