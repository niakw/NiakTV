# NiakTV 0.3.4 — évolutions depuis la version 0.3.1

Cette version rassemble les améliorations développées en **0.3.2-dev**, **0.3.3-dev** et **0.3.4**. Elle apporte une gestion des applications plus complète, davantage de réglages système et une navigation plus agréable à la télécommande.

## 0.3.2-dev — Applications et personnalisation

- **Menu contextuel des applications** enrichi (appui long sur OK ou clic droit) : ouvrir, ajouter ou retirer de **Ma sélection**, changer la position dans **Apps TV**, consulter les informations Android, demander une désinstallation, masquer une application, accéder aux options d'arrêt forcé ou de cache.
- **Opérations Android sécurisées** : les actions non autorisées sont grisées ou renvoient aux réglages système avec confirmation. Le bouton « Vider le cache » ne supprime jamais les données d'une application.
- **Gestion de la visibilité** : masquer une application dans **Apps TV** ne la désinstalle pas. Un bouton **+** en fin de liste permet de retrouver les applications masquées et d'ajouter celles qui n'apparaissent pas automatiquement.
- **Icônes et libellés mieux différenciés** : moins de noms génériques pour des applications distinctes ; les icônes sous forme de lettre restent présentes lorsqu'aucun pictogramme dédié n'existe.
- **Accueil d'origine** : choix entre un retour **temporaire** (sans modifier la configuration durable de NiakTV) et un retour **permanent** (désactivation de la redirection et accès au choix de l'accueil Android).

## 0.3.3-dev — Réglages « Applications et système »

Nouveau panneau accessible dans **Réglages → Applications et système** :

- **Applications installées** : accès à leur gestion dans Android.
- **Autorisations** : accès au gestionnaire d'autorisations Android et aux paramètres d'une application.
- **Applications au démarrage** : inventaire des applications déclarant pouvoir recevoir l'événement de démarrage. Cette liste peut être **incomplète** et ne prouve pas qu'une application se lance réellement ou reste active.
- **Stockage** : capacité totale et espace disponible de la partition de données Android, avec indicateur visuel.
- **Mémoire vive (RAM)** : mémoire totale et disponible, avec indicateur visuel.
- **Entretien** : nettoyage du **cache de NiakTV uniquement**, après confirmation, sans effacement des favoris, préférences ou données d'autres applications.
- **Limites explicites** : « Vider tous les caches » et « Forcer l'arrêt de toutes les applications » restent **grisés**, car Android ne permet pas ces opérations à un lanceur ordinaire.
- **Raccourcis système** : accès aux réglages Wi-Fi, affichage, accessibilité, informations sur la TV et options développeur lorsque le fabricant les propose.

## 0.3.4 — Navigation et finition de l'interface

- **Retour hiérarchique** : depuis un sous-menu, la touche Retour revient au **niveau précédent** au lieu de fermer tous les réglages. Exemple : *Stockage → Applications et système → Réglages → accueil*.
- **Focus préservé** : lors du retour au menu parent, l'option sélectionnée précédemment retrouve le focus, à la télécommande comme à la souris.
- **Cohérence visuelle** : harmonisation de certains pictogrammes d'applications avec la direction artistique monochrome du lanceur.

## Compatibilité, tests et sécurité

- **Mise à jour sans réinstallation** : APK signé avec la même clé d'origine que les versions précédentes ; la mise à jour conserve les données de NiakTV.
- **Vérifications effectuées** : compilation Android, tests unitaires, Android Lint et tests de navigation de l'interface dans la WebView sur une Philips Android TV 12.
- **Dépendances et CI** : vérification des versions de dépendances et épinglage des actions GitHub à des commits immuables. Les avertissements CodeQL relatifs à l'architecture WebView et au réseau restent documentés et suivis, sans prétendre qu'ils sont tous corrigés.
- **Accès anticipé** : les écrans et autorisations disponibles peuvent varier selon la version d'Android TV et le fabricant.

**Fichiers inclus :** `niaktv-0.3.4.apk` et `SHA256SUMS.txt` pour vérifier l'intégrité du téléchargement.

---

## English — cumulative changes from 0.3.1 to 0.3.4

**0.3.2-dev — App management:** Expanded long-press/right-click menu; favourites, ordering, app details, Android-managed uninstall, force-stop and cache options; hide/show visibility in TV apps; permanent **+** picker for hidden or undisplayed apps; clearer app labels and icons; temporary or permanent return to Android's original Home.

**0.3.3-dev — Apps & system:** Installed apps and permissions shortcuts, a limited inventory of declared startup receivers (not a list of running processes), live storage and RAM figures, confirmation-based NiakTV-only cache cleanup, and access to relevant Android TV settings. Unsupported global cache cleaning and mass force-stop are visibly disabled.

**0.3.4 — Navigation and polish:** Back returns to the parent menu, preserving the previous selection for remote and mouse use; application icons are visually aligned with the launcher's monochrome design.

**Installation and quality:** Update-compatible signing key; successful Android build, unit tests, lint and real-TV WebView navigation checks. Includes an APK and SHA-256 checksum. Early-access release.
