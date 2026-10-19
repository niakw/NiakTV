<div align="center">
  <img src="assets/branding/niaktv-logo.svg" alt="NiakTV — Votre TV. Votre espace." width="800">
  <p><a href="README.md">English</a> · <strong>Français</strong></p>
  <h3>Un accueil qui vous ressemble.</h3>
  <p><kbd>Android TV 8+</kbd> <kbd>Sans publicité</kbd> <kbd>Cinéma ou minimal</kbd> <kbd>EN / FR</kbd></p>
  <p>Vos apps. Vos entrées. Votre prochaine séance.<br>Une interface TV soignée, avec la liberté de ne garder que l’essentiel.</p>
</div>

<p align="center"><a href="#-bienvenue-chez-vous"><strong>Expérience</strong></a> · <a href="#-installation-en-quelques-minutes"><strong>Installer</strong></a> · <a href="#-à-votre-façon"><strong>Personnaliser</strong></a> · <a href="VALIDATION.md"><strong>Développement et validation</strong></a></p>

---

## ✨ Bienvenue chez vous

| Votre accueil                  | Votre choix                                                                                                                                                                                                                         |
| ------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Une ambiance cinéma**        | Des fonds TMDB quotidiens issus des films à l’affiche, avec le titre original du film discrètement crédité en bas à gauche. Avec votre propre clé API.                                                                              |
| **Une alternative sobre**      | Un fond uni ou un dégradé statique, en Menthe, Violet, Bleu ou Graphite. Aucun téléchargement de fond dans ces modes.                                                                                                               |
| **Votre prochaine séance**     | Les vraies reprises publiées par Nuvio, avec illustrations et progression lorsqu’elles existent. Sans Nuvio, une app de streaming reconnue déjà installée devient un raccourci ; sans app compatible, aucune carte de remplacement. |
| **Seulement l’essentiel**      | Masquez entièrement **Ma sélection**. Le **mode performance** passe aussi sur un fond uni, coupe le chargement des reprises et des images et simplifie les effets de focus.                                                         |
| **Pensé pour la télécommande** | Focus menthe/violet visible, navigation directionnelle, pictogrammes dédiés, favoris et dock réorganisable. Un appui long sur OK permet d’organiser une app.                                                                        |
| **Votre langue**               | Interface intégralement en français et en anglais. Suivez la langue du téléviseur ou choisissez-la dans les réglages. Les noms des apps et programmes conservent leurs libellés d’origine.                                          |

**Ma sélection → Entrées → Apps TV.** Aucune ligne sponsorisée, bande-annonce automatique ou télémétrie. L’interface et les polices sont stockées sur la TV. Les téléchargements facultatifs ont lieu lorsque l’accueil est au premier plan.

## ⚡ Installation en quelques minutes

1. Téléchargez l’artefact APK **niaktv-debug** d’une [exécution Android réussie](https://github.com/niakw/niaktv/actions/workflows/android.yml), puis extrayez `app-debug.apk`. Il s’agit de versions de développement.
2. Sur la TV, activez les options développeur et le débogage ADB. Reliez l’ordinateur et la TV au même réseau et acceptez la demande de débogage sur la TV.
3. Avec [Android Platform Tools](https://developer.android.com/tools/releases/platform-tools) installé, remplacez `TV_IP` par l’adresse de votre téléviseur :

```sh
adb connect TV_IP:5555
adb -s TV_IP:5555 install -r app-debug.apk
adb -s TV_IP:5555 shell am start -n tv.niak.launcher/.MainActivity
```

4. Ouvrez **Réglages → Bouton Accueil**. Choisissez NiakTV comme accueil si la TV le permet. Sur les modèles compatibles qui imposent Google, activez le service d’accessibilité facultatif **NiakTV — bouton Accueil**.

**Gardez une porte de sortie :** **Réglages → Accueil d’origine** coupe la redirection et ouvre Google. Vous pouvez aussi désactiver le service NiakTV dans les réglages d’accessibilité Android. Aucun composant système ne doit être supprimé ou désactivé. Le comportement dépend du fabricant.

Une mise à jour doit utiliser la même clé de signature que l’APK installé. Les artefacts CI et les versions signées localement peuvent avoir des clés différentes ; Android refusera une mise à jour incompatible.

## 🎛 À votre façon

- **Cinéma :** Réglages → Style du fond → Cinéma, puis configurez votre clé API TMDB ou votre jeton de lecture personnel. Activez le changement quotidien ou gardez le fond actuel. **Fond suivant** demande un autre film.
- **Minimal :** activez le **mode performance** pour conserver entrées et apps sur un fond uni. Le désactiver rétablit vos choix précédents de sélection et de fond.
- **Votre équilibre :** masquez **Ma sélection** indépendamment, ou choisissez un dégradé en gardant vos favoris. Choisissez votre palette et votre langue.
- **Reprises :** avec Nuvio installé, autorisez l’accès aux programmes Android TV lorsque demandé. NiakTV affiche uniquement ce que Nuvio publie. L’ouverture suit le lien fourni par Nuvio et peut afficher son choix de sources.

Votre clé TMDB reste dans le stockage Android privé de l’app, exclu des sauvegardes. Elle n’est jamais intégrée à l’APK, envoyée à l’interface JavaScript ou versionnée dans ce dépôt. **Aucun compte TMDB ni aucune installation de Nuvio n’est nécessaire pour utiliser le lanceur.**

## 🛠 Conçu pour rester léger

HTML/CSS/JavaScript local et pont Android natif. Aucun framework web, page distante, SDK publicitaire ou outil d’analyse. Cache d’images borné, illustrations différées et actualisations au premier plan. Le service Accueil facultatif observe uniquement les changements de fenêtre de l’accueil Google, sans accès au contenu des fenêtres.

Le mode performance réduit le travail de l’accueil ; il ne constitue pas une optimisation globale d’Android ni une garantie de 60 fps. Consultez [la validation, les instructions de compilation et les limites connues](VALIDATION.md).

## Crédits

Ce produit utilise l’API TMDB sans être approuvé ni certifié par TMDB. Les visuels de films restent la propriété de leurs ayants droit. NiakTV est indépendant de TMDB, de Nuvio et des marques d’applications représentées dans son interface.

Manrope — Mikhail Sharanda, Mirko Velimirovic. Instrument Serif — Instrument. Les deux polices sont distribuées sous SIL Open Font License ; les licences accompagnent les fichiers de polices. Le fond cinéma de secours a été créé pour NiakTV.
