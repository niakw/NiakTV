# Accessibility / Accessibilité

[English](#english) · [Français](#francais) · [NiakTV](README.md)

<a id="english"></a>

## English

### A TV interface you can navigate

NiakTV prioritises operation from a standard directional TV remote. The current interface provides:

- Arrow navigation through content, favourites, inputs, apps and settings, with a visible focus outline.
- OK to open, a long press on OK to organise an app, Menu or the on-screen settings button to open settings, and Back to close a panel or return to the main content.
- Scrolling that follows focused items, including horizontal rows. A hidden selection section is removed from directional navigation.
- English and French text, labelled primary controls and text alongside app/input marks. Background art is decorative.
- No autoplay or permanently animated background. Solid backgrounds and Performance mode offer a less visually busy alternative while preserving focus.

### What is not yet established

Remote navigation has been exercised on an Android TV 12 reference device. This does **not** establish TalkBack compatibility, complete switch-access support, a screen-reader reading order or conformance to an accessibility standard. Contrast has not been audited across every palette and cinema image. System font scaling, magnification and low-vision use across screen sizes also need dedicated evaluation.

The interface fixes text zoom and uses a custom directional focus model; those choices can limit assistive-technology behaviour. If text or focus is hard to distinguish, try a solid background or another palette and report the affected task. This is a workaround, not a claim that the barrier is resolved.

### The Home accessibility service has a different purpose

**NiakTV — Home button** is an optional compatibility service for TVs that enforce Google home. It observes Google home window-change events and can open NiakTV; it is configured without access to window content. It is not a screen reader or an accessibility certification. It can be disabled independently in Android settings. See [Installation](INSTALL.md).

### Report a barrier

Use the [accessibility issue template](https://github.com/niakw/NiakTV/issues/new?template=accessibility.md). Describe the task, steps, expected behaviour, TV/Android version and input method. Mention assistive technology only if relevant; no disability disclosure is required. Images and recordings are optional and should avoid private content.

Maintainers review the impact on the task and available workarounds. A task that cannot be completed deserves attention before a cosmetic inconsistency. Contributors should include remote-navigation checks and any assistive-technology checks they actually performed; see [Contributing](CONTRIBUTING.md#english).

<a id="francais"></a>

## Français

### Une interface navigable à la télécommande

NiakTV privilégie l’utilisation avec une télécommande TV directionnelle standard. L’interface actuelle propose :

- Une navigation par flèches entre contenus, favoris, entrées, apps et réglages, avec un contour de focus visible.
- OK pour ouvrir, un appui long sur OK pour organiser une app, Menu ou le bouton de réglages à l’écran pour ouvrir le panneau, Retour pour le fermer ou revenir au contenu principal.
- Un défilement qui suit le focus, y compris dans les lignes horizontales. Un bloc de sélection masqué est retiré du parcours directionnel.
- Des textes en français et en anglais, des libellés sur les commandes principales et du texte avec les pictogrammes d’apps et d’entrées. Le fond est décoratif.
- Aucun lancement automatique de vidéo ni fond animé permanent. Les fonds unis et le mode performance réduisent la charge visuelle en conservant le focus.

### Ce qui reste à établir

La navigation à la télécommande a été essayée sur un appareil de référence Android TV 12. Cela ne valide **pas** la compatibilité TalkBack, la prise en charge complète du contrôle par contacteurs, l’ordre de lecture d’un lecteur d’écran ou la conformité à une norme d’accessibilité. Le contraste n’a pas été audité sur toutes les palettes et images de cinéma. Le grossissement du texte, la loupe et les usages en basse vision sur différentes tailles d’écran demandent également une évaluation dédiée.

L’interface fixe le zoom du texte et utilise son propre focus directionnel ; ces choix peuvent limiter le comportement des technologies d’assistance. Si le texte ou le focus se distingue mal, essayez un fond uni ou une autre palette et signalez la tâche concernée. Il s’agit d’un contournement, pas d’une résolution garantie.

### Le service d’accessibilité Accueil a un autre rôle

**NiakTV — bouton Accueil** est un service facultatif de compatibilité avec les TV qui imposent Google. Il observe les changements de fenêtre de l’accueil Google et peut ouvrir NiakTV ; sa configuration ne lui permet pas de lire le contenu des fenêtres. Ce n’est ni un lecteur d’écran ni une certification d’accessibilité. Il peut être désactivé séparément dans Android. Voir [Installation](INSTALL.fr.md).

### Signaler un obstacle

Utilisez le [modèle d’issue d’accessibilité](https://github.com/niakw/NiakTV/issues/new?template=accessibility.md). Décrivez la tâche, les étapes, le comportement attendu, la version TV/Android et le moyen de saisie. Mentionnez une technologie d’assistance seulement si cela aide ; aucune information sur un handicap n’est demandée. Les images et vidéos sont facultatives et doivent éviter les contenus privés.

La maintenance examine l’impact sur la tâche et les contournements possibles. Une tâche impossible à terminer mérite de l’attention avant une incohérence visuelle. Les contributions doivent préciser les contrôles télécommande et les essais de technologies d’assistance réellement effectués ; voir [Contribution](CONTRIBUTING.md#francais).
