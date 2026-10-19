# Installer NiakTV

[English](INSTALL.md) · **Français** · [Retour à NiakTV](README.fr.md)

## Avant de commencer

Il vous faut un appareil Android TV sous Android 8 ou plus récent, un ordinateur avec [Android Platform Tools](https://developer.android.com/tools/releases/platform-tools), et une TV qui autorise l’installation d’APK et le débogage ADB. Les réglages et le bouton Accueil dépendent du fabricant. Les versions actuelles sont des APK de développement.

Nuvio et une clé TMDB sont facultatifs. Commencez sans ces services si vous voulez simplement vos apps et vos entrées.

## 1. Récupérer l’APK

Ouvrez le [workflow Android](https://github.com/niakw/NiakTV/actions/workflows/android.yml), choisissez une compilation **réussie** et téléchargez l’artefact **niaktv-debug**. Extrayez `app-debug.apk` du ZIP. Une compilation en échec ou encore en cours peut ne pas proposer d’APK.

## 2. Connecter et installer

Activez les options développeur et le débogage ADB sur la TV. Si le firmware propose ADB par réseau sur le port 5555, reliez la TV et l’ordinateur au même réseau de confiance. Remplacez `TV_IP` par l’adresse de votre TV ; acceptez la demande d’autorisation sur le téléviseur.

```sh
adb connect TV_IP:5555
adb devices
adb -s TV_IP:5555 install -r app-debug.apk
adb -s TV_IP:5555 shell am start -n tv.niak.launcher/.MainActivity
```

L’appareil doit apparaître comme `device`, et non `unauthorized` ou `offline`. Certains appareils Android TV utilisent un appairage ou un autre moyen de débogage : suivez la procédure proposée par l’appareil plutôt que de supposer que le port 5555 est ouvert.

## 3. Configurer le bouton Accueil

Si Android propose de choisir l’application d’accueil par défaut, sélectionnez NiakTV. Sur certaines TV qui imposent Google, **Réglages → Bouton Accueil** ouvre les paramètres d’accessibilité : activez **NiakTV — bouton Accueil**, puis testez la touche Accueil.

Ce service facultatif cible le lanceur Google Android TV. Il ne remplace pas universellement tous les lanceurs fabricants. Il ne lit pas le contenu des fenêtres et ne nécessite aucune désactivation d’app système.

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

## Mises à jour

Utilisez `adb install -r` avec un APK signé par **la même clé** que la version installée. Les builds debug CI et locaux peuvent employer des clés différentes ; une signature stable n’est pas garantie entre deux builds CI. `INSTALL_FAILED_UPDATE_INCOMPATIBLE` indique une incompatibilité de signature : récupérez un APK construit avec la clé d’origine. Désinstaller n’est pas une mise à jour transparente : cela supprime les préférences et la clé TMDB enregistrée.

## Revenir à l’accueil d’origine

Ouvrez **Réglages → Accueil d’origine** pour couper la redirection NiakTV et ouvrir Google. Pour un retour durable, désactivez uniquement le service **NiakTV — bouton Accueil** dans les paramètres d’accessibilité Android, puis sélectionnez l’accueil d’origine si la TV propose ce choix. Cette action cible actuellement Google Android TV ; un autre accueil fabricant peut nécessiter une sélection dans les paramètres Android.

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
