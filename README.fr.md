<div align="center">
  <img src="assets/branding/niaktv-logo.svg" alt="NiakTV — Votre TV. Votre espace." width="800">
  <p><a href="README.md">English</a> · <strong>Français</strong></p>
  <h3>Moins de bruit. Plus de place pour votre TV.</h3>
  <p>Votre prochaine séance, vos apps favorites et vos appareils connectés.<br>Un accueil Android TV personnel, de l’ambiance cinéma à l’essentiel.</p>
  <p><kbd>Android TV 8+</kbd> <kbd>Sans publicité</kbd> <kbd>Sans télémétrie</kbd> <kbd>EN / FR</kbd></p>
  <p><a href="https://github.com/niakw/NiakTV/actions/workflows/android.yml"><img src="https://github.com/niakw/NiakTV/actions/workflows/android.yml/badge.svg" alt="Compilation Android"></a></p>
</div>

<p align="center"><a href="#experience"><strong>Expérience</strong></a> · <a href="#install"><strong>Installer</strong></a> · <a href="#customise"><strong>Personnaliser</strong></a> · <a href="#documentation"><strong>Documentation</strong></a></p>

<p align="center">
  <img src="assets/screenshots/home-fr.png" alt="Accueil NiakTV : Ma sélection, entrées et dock des apps TV" width="960">
  <br><sub>Aperçu de l’interface avec une sélection d’apps de démonstration et un flux de reprises vide. Aucun historique de visionnage personnel n’est affiché.</sub>
</p>

---

<a id="experience"></a>

## ✨ Une place pour ce que vous utilisez vraiment

**Ma sélection → Entrées → Apps TV.** Une interface pensée pour la télécommande, des pictogrammes dédiés, un focus visible et une typographie qui laisse respirer les contenus.

| À votre façon                | Ce que vous retrouvez                                                                                                                                                                                                         |
| ---------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Un peu de cinéma**         | Des fonds TMDB quotidiens issus du catalogue des films à l’affiche. Le titre original du film et sa date de sortie restent discrètement crédités en bas à gauche.                                                                                     |
| **Votre prochaine séance**   | Les vraies reprises Nuvio, leurs illustrations et leur progression lorsqu’elles sont publiées par l’app. Trois reprises par défaut, avec nombre configurable. Nuvio est facultatif : une app de streaming compatible déjà installée peut prendre sa place sous forme de raccourci. |
| **Vos propres raccourcis**   | Réorganisez vos favoris et le dock des apps indépendamment par un appui long sur OK. Les entrées HDMI restent à portée de main.                                                                                                            |
| **La liberté de simplifier** | Masquez Ma sélection, choisissez un fond uni ou un dégradé statique, puis une palette Menthe, Violet, Bleu ou Graphite.                                                                                                       |
| **Un accueil plus léger**    | Le mode performance masque Ma sélection, utilise un fond uni et coupe le chargement des programmes et des images de contenu. Affiches WebP adaptées aux cartes, icônes vectorielles compactes et polices WOFF2 allègent l’interface. Le focus télécommande reste visible.                                                             |
| **Votre langue**             | Français, anglais ou langue du téléviseur. Les noms d’apps et de programmes gardent leurs libellés d’origine.                                                                                                                 |

Aucune ligne sponsorisée, bande-annonce automatique ou analyse d’usage. L’interface et les polices sont stockées sur la TV. Les fonctions principales du lanceur ne nécessitent ni Nuvio ni compte TMDB.

<a id="install"></a>

## ⚡ Prendre ses marques

**[Télécharger l’APK NiakTV](https://github.com/niakw/NiakTV/releases/latest/download/niaktv-0.3.1.apk)** · [Notes de version](https://github.com/niakw/NiakTV/releases/latest)

L’APK signé est prêt à installer : aucune compilation, aucun compte GitHub Actions et aucun ZIP à extraire. La version 0.3.1 reste une version de développement, pas une version de production.

1. Téléchargez `niaktv-0.3.1.apk` avec le lien ci-dessus.
2. Transférez-le sur la TV et ouvrez-le avec un gestionnaire de fichiers, en autorisant l’installation depuis cette source si Android le demande. Autre possibilité : `adb install -r niaktv-0.3.1.apk`.
3. Ouvrez NiakTV, puis **Réglages → Bouton Accueil** pour configurer le comportement permis par votre TV.

**[Guide d’installation →](INSTALL.fr.md)** Commandes, premier démarrage, mises à jour, dépannage et retour à l’accueil d’origine.

Le réglage Accueil dépend de la TV. NiakTV prend en charge le choix natif et un relais de compatibilité facultatif sans lecture du contenu des fenêtres. Certains firmwares demandent une configuration ADB explicite et réversible pour désactiver l’accueil Google avant qu’Android puisse choisir directement NiakTV. L’app ne désactive aucun composant système elle-même. Consultez le [guide Accueil et restauration](INSTALL.fr.md#advanced-home).

<a id="customise"></a>

## 🎛 Cinéma quand vous voulez. Minimal quand vous préférez.

| Votre point de départ | Dans les réglages                                                                                                                                                            |
| --------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Cinéma**            | Style du fond → Cinéma, puis configurez votre propre clé TMDB. Choisissez le changement quotidien ou gardez un fond favori.                                                  |
| **Minimal**           | Activez le mode performance pour garder entrées et apps sur un fond uni. Le désactiver rétablit vos choix précédents de sélection et de fond.                                |
| **Entre les deux**    | Masquez Ma sélection indépendamment, choisissez un dégradé et votre palette. Changer le style du fond ou la visibilité du bloc désactive le mode performance.                |
| **Reprises**          | Avec Nuvio installé, autorisez l’accès aux programmes Android TV. Seuls les titres réellement publiés par Nuvio apparaissent ; leur ouverture suit le lien fourni par l’app. |

**Application de Ma sélection** permet de choisir Automatique ou une autre app installée et lançable, même avec Nuvio présent. Sans app compatible, aucune carte de remplacement. Le lanceur n’invente pas d’historique de lecture pour les autres services.

TMDB utilise votre clé API ou votre jeton de lecture personnel, conservé dans le stockage privé de la TV et exclu des sauvegardes. Cet identifiant n’est jamais intégré à l’APK ni transmis à l’interface JavaScript. Les fonds unis et dégradés ne téléchargent aucune image de cinéma.

<a id="documentation"></a>

## 📚 Trouver son chemin

| Vous souhaitez…                                          | Consultez                                       |
| -------------------------------------------------------- | ----------------------------------------------- |
| Installer, mettre à jour ou rétablir l’accueil d’origine | [Installation](INSTALL.fr.md)                   |
| Compiler l’app ou contribuer                             | [Contribution](CONTRIBUTING.md#francais)        |
| Comprendre les vérifications et les limites connues      | [Validation](VALIDATION.md#francais)            |
| Comprendre la navigation et les limites d’accessibilité  | [Accessibilité](ACCESSIBILITY.md#francais)      |
| Signaler une vulnérabilité en privé                      | [Sécurité](SECURITY.md#francais)                |
| Participer à la communauté                               | [Code de conduite](CODE_OF_CONDUCT.md#francais) |

Les questions vont dans [Discussions](https://github.com/niakw/NiakTV/discussions) ; les anomalies reproductibles et suggestions dans les [Issues](https://github.com/niakw/NiakTV/issues/new/choose). Le français et l’anglais sont les bienvenus.

Les dépendances des outils de compilation sont maintenues séparément de l’app installée. Voir le [périmètre sécurité et dépendances](SECURITY.md#francais).

## Crédits et licence

[GNU GPL v3](LICENSE). Les polices Manrope et Instrument Serif conservent leurs [notices SIL Open Font License](app/src/main/assets/fonts/). Le fond de secours original a été créé pour NiakTV. Les visuels de films restent la propriété de leurs ayants droit.

Ce produit utilise l’API TMDB sans être approuvé ni certifié par TMDB. NiakTV est indépendant de TMDB, de Nuvio et des marques d’applications représentées dans son interface.
