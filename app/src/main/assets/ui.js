"use strict";
(() => {
  let model = {
    apps: [],
    inputs: [],
    resumes: [],
    favorites: [],
    order: [],
    daily: true,
  };
  const t = (value) =>
    model.language === "en" ? window.NiakTranslations[value] || value : value;
  const nuvioInstalled = () => model.apps.some((a) => a.id === "com.nuvio.tv");
  const nuvioSelected = () => model.defaultStreamingApp === "com.nuvio.tv";
  let sceneVersion = 0;
  const markup = new Map();
  function setMarkup(id, html) {
    if (markup.get(id) === html) return;
    $(id).innerHTML = html;
    markup.set(id, html);
  }
  function selectionMarkup() {
    return model.showSelection === false
      ? ""
      : model.resumes.length
        ? feature(model.resumes[0]) +
          (model.resumes.length > 1
            ? `<div class="resume-queue">${model.resumes.slice(1).map(queue).join("")}</div>`
            : "")
        : emptyFeature();
  }
  let menu = null,
    menuApp = null,
    menuScope = "apps",
    previousKey = "feature",
    toastTimer;
  const $ = (id) => document.getElementById(id);
  const esc = (value) =>
    String(value ?? "").replace(
      /[&<>"']/g,
      (c) =>
        ({
          "&": "&amp;",
          "<": "&lt;",
          ">": "&gt;",
          '"': "&quot;",
          "'": "&#39;",
        })[c],
    );
  const device = (action, id = "") => {
    if (window.Device) Device.action(action, id);
    else toast(t("Aperçu du design — connexion Android requise"));
  };
  const play =
    '<svg viewBox="0 0 18 18" aria-hidden="true"><path d="M5 3l10 6-10 6z"/></svg>';
  const nuvio =
    '<svg viewBox="0 0 32 32" aria-hidden="true"><path d="M8 3c-2-1-4 0-4 3v20c0 3 2 4 4 3l19-10c3-1 3-5 0-6L8 3zm2 6l13 7-13 7V9z"/></svg>';
  const hdmi =
    '<svg viewBox="0 0 28 28" aria-hidden="true"><path d="M4 8h20v6l-5 6H9l-5-6zM9 12h10m-8 4h6"/></svg>';
  const source =
    '<svg viewBox="0 0 28 28" aria-hidden="true"><path d="M11 5h13v18H11M3 14h14m-5-5l5 5-5 5"/></svg>';
  const symbol = (path) => `<svg class="ui-symbol" viewBox="0 0 32 32" aria-hidden="true">${path}</svg>`;
  const globe = symbol('<circle cx="16" cy="16" r="12"/><path d="M4 16h24M16 4c-7 6-7 18 0 24 7-6 7-18 0-24z"/>');
  const television = symbol('<rect x="4" y="6" width="24" height="17" rx="3"/><path d="M11 28h10M16 23v5"/>');
  const marks = {
    "com.goodtoolapps.zeus": globe,
    "com.vewd.core.browserui": globe,
    "org.droidtv.playtv": television,
    "org.droidtv.nettvrecommender": symbol('<path d="m16 4 3.5 7.1 7.8 1.1-5.7 5.5 1.4 7.8-7-3.7-7 3.7 1.4-7.8-5.7-5.5 7.8-1.1z"/>'),
    "org.droidtv.eum": symbol('<circle cx="16" cy="16" r="12"/><path d="M12 12a4 4 0 0 1 8 0c0 3-4 3-4 6M16 23h.01"/>'),
    "com.phorus.playfi.tv": symbol('<path d="M4 11h5l6-5v20l-6-5H4zM20 11c4 3 4 7 0 10M24 7c7 5 7 13 0 18"/>'),
    "com.google.android.play.games": symbol('<path d="M10 9h12c4 0 6 5 6 11 0 6-4 5-8 0h-8c-4 5-8 6-8 0 0-6 2-11 6-11zM8 14h8M12 10v8M23 13h.01M20 17h.01"/>'),
    "org.droidtv.demome": symbol('<rect x="4" y="6" width="24" height="20" rx="3"/><path d="m13 11 8 5-8 5z"/>'),
    "org.droidtv.channels": symbol('<rect x="4" y="5" width="24" height="22" rx="3"/><path d="M4 12h24M12 12v15M17 17h6M17 22h6M8 17h.01M8 22h.01"/>'),
    "org.droidtv.nettv.market": symbol('<rect x="4" y="4" width="9" height="9" rx="2"/><rect x="19" y="4" width="9" height="9" rx="2"/><rect x="4" y="19" width="9" height="9" rx="2"/><path d="M19 23h9M23.5 19v9"/>'),
    "org.droidtv.contentexplorer": symbol('<rect x="4" y="6" width="24" height="20" rx="3"/><circle cx="11" cy="12" r="2"/><path d="m5 24 9-8 5 4 4-3 5 5"/>'),
    "org.droidtv.amazonalexa": symbol('<path d="M27 16a11 11 0 1 0-11 11c4 0 7-2 9-4V12"/><path d="M25 23v5l-6-2"/>'),
    "com.apple.atve.androidtv.appletv": '<span class="wordmark">tv</span>',
    "com.disney.disneyplus": '<span class="wordmark">Disney+</span>',
    "com.wbd.stream": '<span class="wordmark">max</span>',
    "com.hbo.hbonow": '<span class="wordmark">HBO</span>',
    "com.canal.android.canal": '<span class="wordmark">CANAL+</span>',
    "com.nuvio.tv": nuvio,
    "com.google.android.youtube.tv":
      '<svg viewBox="0 0 32 32"><path d="M27 7c-4-1-18-1-22 0-3 1-3 17 0 18 4 1 18 1 22 0 3-1 3-17 0-18zm-14 5l9 4-9 4v-8z"/></svg>',
    "com.google.android.youtube.tvmusic":
      '<svg viewBox="0 0 32 32"><path fill-rule="evenodd" d="M16 2a14 14 0 1 0 0 28 14 14 0 0 0 0-28zm0 3a11 11 0 1 1 0 22 11 11 0 0 1 0-22zm-3 5v12l9-6z"/></svg>',
    "org.videolan.vlc":
      '<svg viewBox="0 0 32 32"><path d="M14 3h4l9 25H5L14 3zm-2 9h8l-2-6h-4l-2 6zm-4 11h16l-2-6H10l-2 6z"/></svg>',
    "tv.twitch.android.app": '<span class="wordmark">twitch</span>',
    "com.sportzx.live":
      '<svg viewBox="0 0 32 32"><path d="M10 4h18l-3 5H13l-3 5h15l-8 14H2l3-5h9l3-5H2z"/></svg>',
    "fr.tf1.mytf1": '<span class="wordmark">TF1+</span>',
    "fr.francetv.pluzz":
      '<span class="wordmark">france<span style="color:#d6ecce">●</span>tv</span>',
    "fr.m6.m6replay": '<span class="wordmark">M6+</span>',
    "com.netflix.ninja":
      '<span class="wordmark" style="font-size:31px;font-weight:800">N</span>',
    "com.amazon.amazonvideo.livingroom": '<span class="wordmark">prime</span>',
    "com.android.vending":
      '<svg viewBox="0 0 32 32"><path fill="none" stroke="currentColor" stroke-width="2" d="M6 10h20v18H6zM11 12V8a5 5 0 0 1 10 0v4"/></svg>',
    "com.eset.etvs.gp":
      '<svg viewBox="0 0 32 32"><path fill="none" stroke="currentColor" stroke-width="2" d="M16 3L27 7v9c0 6-7 11-11 13C12 27 5 22 5 16V7zM21 11h-9v10h9m-9-5h7"/></svg>',
    "com.alphainventor.filemanager":
      '<svg viewBox="0 0 32 32"><path fill-rule="evenodd" d="M3 6h10l3 3h13v17H3V6zm3 6v11h20V12H6z"/></svg>',
  };
  const names = {
    "com.google.android.youtube.tv": "YouTube",
    "com.google.android.youtube.tvmusic": "Music",
    "tv.twitch.android.app": "Twitch",
    "org.videolan.vlc": "VLC",
    "com.android.vending": "Play Store",
    "com.eset.etvs.gp": "ESET",
    "com.alphainventor.filemanager": t("Fichiers"),
    "com.sportzx.live": "SportzX",
    "com.nuvio.tv": "Nuvio",
  };
  function mark(app) {
    return marks[app.id] || `<svg class="app-monogram" viewBox="0 0 32 32" aria-hidden="true"><rect x="3" y="3" width="26" height="26" rx="8"/><text x="16" y="16" text-anchor="middle" dominant-baseline="central">${esc(Array.from(appName(app))[0] || "A")}</text></svg>`;
  }
  const localizedNames = {
    "com.alphainventor.filemanager": "Fichiers",
    "com.goodtoolapps.zeus": "Navigateur",
    "com.vewd.core.browserui": "Navigateur",
    "org.droidtv.nettvrecommender": "Découvrir",
    "org.droidtv.eum": "Aide",
    "org.droidtv.playtv": "TV en direct",
    "org.droidtv.demome": "Démo",
    "org.droidtv.channels": "Guide TV",
    "org.droidtv.nettv.market": "Collection TV",
    "org.droidtv.contentexplorer": "Multimédia",
  };
  function appName(app) {
    return localizedNames[app.id] ? t(localizedNames[app.id]) : names[app.id] || app.name;
  }
  function buttonAttrs(action, id, key) {
    return `data-action="${action}" data-id="${esc(id)}" data-key="${esc(key)}" tabindex="-1"`;
  }
  function localImage(url) {
    return /^https:\/\/niaktv\.local\/art\/[a-f0-9]{64}\.webp$/.test(url || "")
      ? url
      : "";
  }
  function percent(item) {
    return item.duration > 0
      ? Math.max(0, Math.min(100, (100 * item.position) / item.duration))
      : 0;
  }
  function remaining(item) {
    if (item.duration > 120000 && item.position > 0)
      return `${Math.max(1, Math.ceil((item.duration - item.position) / 60000))} ${t("min restantes")}`;
    return percent(item) > 0
      ? `${Math.round(percent(item))} ${t("% regardé")}`
      : "";
  }
  function feature(item) {
    const image = localImage(item.image),
      episode = (item.episode || "").replace(/^\s*·\s*/, "");
    return `<button class="feature artwork" ${buttonAttrs("resume", item.id, "feature")} aria-label="${item.position > 0 ? t("Reprendre") : t("Continuer")} ${esc(item.title)}">${image ? `<img class="feature-image" src="${image}" alt="">` : ""}<span class="feature-shade"></span><div class="feature-content"><span class="eyebrow">${nuvio} ${item.position > 0 ? t("Reprendre") : t("À suivre")} ${t("sur")} Nuvio</span><h2 class="feature-title">${esc(item.title)}</h2><p class="feature-subtitle">${esc(episode)}</p><div class="feature-bottom"><span class="play-cta">${play} ${item.position > 0 ? t("Reprendre") : t("Continuer")}</span><span class="remaining">${remaining(item)}</span></div></div>${percent(item) > 0 ? `<span class="feature-progress"><i style="width:${percent(item)}%"></i></span>` : ""}</button>`;
  }
  function emptyFeature() {
    const app = model.apps.find((a) => a.id === model.defaultStreamingApp);
    if (!app) return "";
    const permission = app.id === "com.nuvio.tv" && !model.resumePermission;
    return `<button class="feature empty-feature" ${buttonAttrs(permission ? "permission" : "app", permission ? "" : app.id, "feature")}><span class="empty-art">${mark(app)}</span><div class="feature-content"><span class="eyebrow">${esc(appName(app))}</span><h2 class="feature-title">${permission ? t("Vos reprises, au premier plan.") : t("Votre prochaine séance.")}</h2><p class="feature-subtitle">${permission ? t("Autorisez l’accès aux programmes TV pour retrouver vos vidéos en cours ici.") : t("Un accès direct à votre application de streaming.")}</p><div class="feature-bottom"><span class="play-cta">${play}${permission ? t("Afficher mes reprises") : t("Ouvrir") + " " + esc(appName(app))}</span></div></div></button>`;
  }
  function queue(item, index) {
    const image = localImage(item.image);
    return `<button class="resume-card" ${buttonAttrs("resume", item.id, "resume:" + item.id)} aria-label="${item.position > 0 ? t("Reprendre") : t("Continuer")} ${esc(item.title)}">${image ? `<img src="${image}" alt="">` : ""}<span class="queue-shade"></span><div class="queue-content"><span class="eyebrow">Nuvio</span><h3 class="queue-title">${esc(item.title)}</h3><p class="queue-meta">${esc((item.episode || "").replace(/^\s*·\s*/, "")) || remaining(item)}</p>${percent(item) > 0 ? `<div class="queue-progress"><i style="width:${percent(item)}%"></i></div>` : ""}</div></button>`;
  }
  function releaseDateLabel() {
    const value = model.wallpaperReleaseDate || "";
    if (/^\d{4}$/.test(value)) return value;
    if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return "";
    const date = new Date(value + "T12:00:00");
    return Number.isFinite(date.getTime())
      ? date.toLocaleDateString(model.language === "en" ? "en-GB" : "fr-FR", {
          day: "numeric", month: "long", year: "numeric",
        })
      : "";
  }
  function render() {
    document.documentElement.lang = model.language || "fr";
    document
      .querySelectorAll("[data-i18n]")
      .forEach((n) => (n.textContent = t(n.dataset.i18n)));
    document
      .querySelectorAll("[data-i18n-label]")
      .forEach((n) => n.setAttribute("aria-label", t(n.dataset.i18nLabel)));
    document.body.classList.toggle(
      "no-selection",
      model.showSelection === false,
    );
    document.body.classList.toggle("performance", !!model.performance);
    document.body.dataset.palette = model.palette || "mint";
    const background = model.background || "cinema";
    document.body.dataset.background = background;
    document.querySelector(".selection").hidden = model.showSelection === false;
    clock();
    if (background !== "cinema") {
      $("wallpaper").style.backgroundImage = "none";
      sceneVersion = 0;
    } else if (
      model.backgroundVersion &&
      model.backgroundVersion !== sceneVersion
    ) {
      sceneVersion = model.backgroundVersion;
      const version = sceneVersion;
      const image = new Image();
      image.onload = () => {
        if (sceneVersion === version)
          $("wallpaper").style.backgroundImage = `url("${image.src}")`;
      };
      image.onerror = () => {
        if (sceneVersion === version) sceneVersion = 0;
      };
      image.src = `https://niaktv.local/background.webp?v=${version}`;
    }
    const active = document.activeElement?.dataset.key || previousKey;
    setMarkup("continue", selectionMarkup());
    $("selection-info").textContent = model.resumes.length
      ? `${model.resumes.length} ${t("reprises")} · Nuvio  →`
      : t("Votre espace personnel");
    document
      .querySelector(".selection")
      .classList.toggle("no-feature", !$("continue").children.length);
    setMarkup("favorites",
      model.favorites
        .map((id) => model.apps.find((a) => a.id === id))
        .filter(Boolean)
        .map(
          (app) =>
            `<button class="favorite" ${buttonAttrs("app", app.id, "fav:" + app.id)}>${mark(app)}${esc(appName(app))}</button>`,
        )
        .join("") +
      `<button class="favorite add" ${buttonAttrs("favorites", "", "edit-favorites")}><span>+</span> ${t("Ma sélection")}</button>`);
    setMarkup("inputs",
      model.inputs
        .map(
          (input) =>
            `<button class="input ${input.connected ? "connected" : ""}" ${buttonAttrs("input", input.id, "input:" + input.id)}>${hdmi}<span>${esc(input.name.replace(" / MHL", ""))}</span><i class="status"></i></button>`,
        )
        .join("") +
      `<button class="input" ${buttonAttrs("tv", "", "tv")}>${source}<span>${t("TV en direct")}</span></button>`);
    const ordered = [
      ...model.order
        .map((id) => model.apps.find((a) => a.id === id))
        .filter(Boolean),
      ...model.apps.filter((app) => !model.order.includes(app.id)),
    ];
    setMarkup("apps", ordered
      .map(
        (app) =>
          `<button class="app" ${buttonAttrs("app", app.id, "app:" + app.id)} aria-label="${esc(app.name)}"><span class="appmark">${mark(app)}</span><span class="app-name">${esc(appName(app))}</span></button>`,
      )
      .join(""));
    const releaseDate = releaseDateLabel();
    $("wallpaper-credit").textContent =
      background !== "cinema"
        ? background === "solid"
          ? t("Fond uni")
          : t("Fond dégradé")
        : model.wallpaperTitle
          ? `${t("Fond")} · ${model.wallpaperTitle}${releaseDate ? ` (${releaseDate})` : ""} · TMDB`
          : model.tmdbConfigured
            ? t("Films à l’affiche · TMDB")
            : t("Fond original · TMDB à configurer");
    const heading = document.querySelector(".feature-title");
    if (heading && heading.scrollHeight > 42)
      heading.classList.add("long-title");
    if (menu) renderMenu(active);
    else focusKey(active);
  }
  function ensureVisible(button) {
    const horizontal = button.closest(
      ".resume-queue,.favorites,.inputs,.apps-dock",
    );
    if (horizontal) {
      const b = button.getBoundingClientRect(),
        p = horizontal.getBoundingClientRect();
      if (b.left < p.left + 4) horizontal.scrollLeft -= p.left + 4 - b.left;
      else if (b.right > p.right - 4)
        horizontal.scrollLeft += b.right - p.right + 4;
    }
    const drawer = button.closest(".drawer");
    if (drawer) {
      const b = button.getBoundingClientRect(),
        p = drawer.getBoundingClientRect();
      if (b.top < p.top + 8) drawer.scrollTop -= p.top + 8 - b.top;
      else if (b.bottom > p.bottom - 8)
        drawer.scrollTop += b.bottom - p.bottom + 8;
    }
  }
  function focusKey(key) {
    let candidate = [...document.querySelectorAll("[data-key]")].find(
      (b) =>
        b.dataset.key === key &&
        !b.closest("[hidden]") &&
        b.getClientRects().length,
    );
    if (!candidate)
      candidate = menu
        ? $("overlay").querySelector("button")
        : document.querySelector('[data-key="feature"]') ||
          document.querySelector("#inputs button") ||
          document.querySelector("#apps button") ||
          document.querySelector(".settings-button");
    if (candidate) {
      document
        .querySelectorAll(".is-focused")
        .forEach((n) => n.classList.remove("is-focused"));
      candidate.classList.add("is-focused");
      if (candidate.dataset.action === "resume" && window.Device)
        Device.action("art", candidate.dataset.id);
      candidate.focus({ preventScroll: true });
      ensureVisible(candidate);
      previousKey = candidate.dataset.key || "feature";
    }
  }
  function rows() {
    if (menu) return [[...$("overlay").querySelectorAll("button")]];
    return [
      [document.querySelector(".settings-button")],
      [...$("continue").querySelectorAll("button")],
      [...$("favorites").querySelectorAll("button")],
      [...$("inputs").querySelectorAll("button")],
      [...$("apps").querySelectorAll("button")],
    ].map((row) =>
      row.filter(
        (b) => b && b.getClientRects().length && !b.closest("[hidden]"),
      ),
    );
  }
  function move(direction) {
    const grid = rows();
    let row = grid.findIndex((items) => items.includes(document.activeElement));
    if (row < 0) {
      focusKey("feature");
      return;
    }
    let index = grid[row].indexOf(document.activeElement),
      target;
    if (menu) {
      const delta = direction === "up" || direction === "left" ? -1 : 1;
      target =
        grid[0][Math.max(0, Math.min(grid[0].length - 1, index + delta))];
    } else if (direction === "left" || direction === "right")
      target =
        grid[row][
          Math.max(
            0,
            Math.min(
              grid[row].length - 1,
              index + (direction === "left" ? -1 : 1),
            ),
          )
        ];
    else {
      let next = row + (direction === "up" ? -1 : 1);
      while (next >= 0 && next < grid.length && !grid[next].length)
        next += direction === "up" ? -1 : 1;
      if (grid[next]?.length) {
        const center =
          document.activeElement.getBoundingClientRect().left +
          document.activeElement.offsetWidth / 2;
        target = grid[next].reduce((a, b) =>
          Math.abs(
            a.getBoundingClientRect().left + a.offsetWidth / 2 - center,
          ) <
          Math.abs(b.getBoundingClientRect().left + b.offsetWidth / 2 - center)
            ? a
            : b,
        );
      }
    }
    if (target) {
      document
        .querySelectorAll(".is-focused")
        .forEach((n) => n.classList.remove("is-focused"));
      target.classList.add("is-focused");
      if (target.dataset.action === "resume" && window.Device)
        Device.action("art", target.dataset.id);
      target.focus({ preventScroll: true });
      ensureVisible(target);
      previousKey = target.dataset.key;
    }
  }
  function showMenu(type = "settings", app = null) {
    previousKey = document.activeElement?.dataset.key || previousKey;
    menu = type;
    menuApp = app;
    $("overlay").hidden = false;
    renderMenu();
  }
  function closeMenu() {
    menu = null;
    $("overlay").hidden = true;
    focusKey(previousKey);
  }
  const menuItem = (title, description, action, id = "", key = action) =>
    `<button class="menu-item" ${buttonAttrs(action, id, "menu:" + key)}><span>${esc(t(title))}${description ? `<small>${esc(t(description))}</small>` : ""}</span><span>›</span></button>`;
  function renderMenu(active) {
    let html = "",
      title = t("Réglages");
    if (menu === "settings") {
      html =
        menuItem(
          t("Ma sélection"),
          t("Ajouter ou retirer vos raccourcis"),
          "favorites",
        ) +
        menuItem(
          t("Application de Ma sélection"),
          model.apps.find(a => a.id === model.defaultStreamingApp)
            ? appName(model.apps.find(a => a.id === model.defaultStreamingApp))
            : t("Automatique"),
          "selection-app-menu",
        ) +
        (nuvioSelected() && model.showSelection !== false
          ? menuItem(
              t("Reprises Nuvio"),
              model.resumePermission
                ? t("Actualiser les vidéos en cours")
                : t("Autoriser les programmes Android TV"),
              "permission",
            )
          : "") +
        (nuvioSelected() && model.showSelection !== false
          ? menuItem(
              t("Nombre de reprises"),
              (model.resumeLimit || 3) === 32
                ? t("Toutes · appuyer pour changer")
                : `${model.resumeLimit || 3} · ${t("appuyer pour changer")}`,
              "resume-limit",
            )
          : "") +
        menuItem(
          t("Mode performance"),
          model.performance
            ? t("Activé · accueil minimal, sans images")
            : t("Réduire le travail de l’accueil"),
          "performance",
        ) +
        menuItem(
          t("Bloc Ma sélection"),
          model.showSelection !== false ? t("Visible") : t("Masqué"),
          "selection",
        ) +
        menuItem(
          t("Langue / Language"),
          model.language === "fr" ? "Français" : "English",
          "language-menu",
        ) +
        `<div class="menu-group">${t("AMBIANCE")}</div>` +
        menuItem(
          t("Style du fond"),
          t(
            { cinema: t("Cinéma"), solid: t("Uni"), gradient: t("Dégradé") }[
              model.background || "cinema"
            ],
          ),
          "background-menu",
        ) +
        menuItem(
          t("Palette"),
          t(
            {
              mint: t("Menthe"),
              violet: t("Violet"),
              blue: t("Bleu"),
              graphite: t("Graphite"),
            }[model.palette || "mint"],
          ),
          "palette-menu",
        ) +
        ((model.background || "cinema") === "cinema"
          ? menuItem(
              t("Films à l’affiche · TMDB"),
              model.tmdbConfigured
                ? t("Clé personnelle configurée")
                : t("Configurer votre clé personnelle"),
              "tmdb",
            ) +
            menuItem(
              t("Fond suivant"),
              t("Choisir un autre film du catalogue actuel"),
              "next",
            ) +
            menuItem(
              t("Changement quotidien"),
              model.daily
                ? t("Activé · une image par jour")
                : t("Désactivé · le fond reste fixe"),
              "daily",
            )
          : "") +
        `<div class="menu-group">${t("VOTRE TÉLÉVISEUR")}</div>` +
        menuItem(
          t("Bouton Accueil"),
          model.nativeHome ? t("NiakTV · accueil Android par défaut") : !model.homeRedirectEnabled
            ? t("Redirection désactivée · configurer")
            : model.homeRedirectConnected
              ? t("Relais connecté · configurer")
              : t("Relais non connecté · ouvrir l’accessibilité"),
          "home",
        ) +
        menuItem(
          t("Accueil d’origine"),
          model.stockHomeEnabled === false ? t("Réactiver Google dans les réglages Android") : t("Retour réversible vers l’accueil Google"),
          "stock",
        ) +
        menuItem(t("Paramètres de la TV"), "", "settings") +
        menuItem(t("À propos et crédits"), "", "about");
    } else if (menu === "selection-app") {
      title = t("Application de Ma sélection");
      const preference = model.selectionAppPreference || "";
      html = `<p class="menu-note">${esc(t("Choisissez une application installée. Les reprises sont disponibles avec Nuvio ; les autres apps s’ouvrent via un raccourci."))}</p>` +
        menuItem(t("Automatique"), !preference ? "✓" : "", "selection-app", "", "selection-app:auto") +
        model.apps.map(app => menuItem(appName(app), preference === app.id ? "✓" : "", "selection-app", app.id, "selection-app:" + app.id)).join("");
    } else if (menu === "language") {
      title = t("Langue / Language");
      html =
        menuItem("Français", "", "language", "fr") +
        menuItem("English", "", "language", "en") +
        menuItem(t("Langue du téléviseur"), "", "language", "auto");
    } else if (menu === "background") {
      title = t("Style du fond");
      html =
        menuItem(
          t("Cinéma"),
          t("Films à l’affiche avec TMDB"),
          "background",
          "cinema",
        ) +
        menuItem(
          t("Uni"),
          t("Une couleur, sans image"),
          "background",
          "solid",
        ) +
        menuItem(
          t("Dégradé"),
          t("Une ambiance colorée, sans image"),
          "background",
          "gradient",
        );
    } else if (menu === "palette") {
      title = t("Palette");
      html = ["mint", "violet", "blue", "graphite"]
        .map((id, i) =>
          menuItem(
            [t("Menthe"), t("Violet"), t("Bleu"), t("Graphite")][i],
            "",
            "palette",
            id,
          ),
        )
        .join("");
    } else if (menu === "favorites") {
      title = t("Ma sélection");
      html =
        `<p class="menu-note">${t("Choisissez vos raccourcis favoris.")}</p>` +
        model.apps
          .map(
            (app) =>
              `<button class="menu-item" ${buttonAttrs("favorite-toggle", app.id, "menu:favorite:" + app.id)}><span>${esc(app.name)}</span><span class="check">${model.favorites.includes(app.id) ? "✓" : "+"}</span></button>`,
          )
          .join("");
    } else if (menu === "app" && menuApp) {
      title = appName(menuApp);
      html =
        menuItem(
          model.favorites.includes(menuApp.id)
            ? t("Retirer de Ma sélection")
            : t("Ajouter à Ma sélection"),
          "",
          "favorite-toggle",
          menuApp.id,
          "favorite:" + menuApp.id,
        ) +
        menuItem(
          menuScope === "favorites" ? t("Changer la position dans Ma sélection") : t("Changer la position dans Apps TV"),
          "",
          menuScope === "favorites" ? "move-favorite-menu" : "move-menu",
          menuApp.id,
        );
    } else if ((menu === "move" || menu === "move-favorite") && menuApp) {
      title = t("Placer") + " " + appName(menuApp);
      const ordered = menu === "move-favorite" ? model.favorites.map(id => model.apps.find(a => a.id === id)).filter(Boolean) : [
        ...model.order
          .map((id) => model.apps.find((a) => a.id === id))
          .filter(Boolean),
        ...model.apps.filter((a) => !model.order.includes(a.id)),
      ];
      html = ordered
        .map((app, index) =>
          menuItem(
            `${index + 1} · ${appName(app)}`,
            "",
            menu === "move-favorite" ? "move-favorite" : "move",
            JSON.stringify({ id: menuApp.id, position: index }),
            "move:" + index,
          ),
        )
        .join("");
    } else if (menu === "about") {
      title = "NiakTV";
      html = `<p class="menu-note">${[
        t("Un accueil personnel, sans publicité ni télémétrie."),
        t(
          "Les reprises Nuvio sont lues depuis Android TV, avec votre autorisation. Aucun accès aux fichiers privés de Nuvio. Sans Nuvio, un raccourci vers une app de streaming installée peut être proposé.",
        ),
        t(
          "Les fonds TMDB utilisent votre clé personnelle, conservée uniquement sur la TV et exclue des sauvegardes. Le mode performance coupe les reprises et les téléchargements d’images.",
        ),
        t(
          "Ce produit utilise l’API TMDB sans être approuvé ni certifié par TMDB.",
        ),
        t(
          "Manrope : Mikhail Sharanda, Mirko Velimirovic. Instrument Serif : Instrument. Polices sous SIL Open Font License.",
        ),
        t(
          "Le fond original de secours est une création pour NiakTV. Les visuels de films restent la propriété de leurs ayants droit.",
        ),
      ]
        .map((text) => esc(t(text)))
        .join("<br><br>")}</p>`;
    }
    $("drawer-title").textContent = t(title);
    $("drawer-content").innerHTML = html;
    const buttons = [...$("drawer-content").querySelectorAll("button")];
    if (active && buttons.some((b) => b.dataset.key === active))
      focusKey(active);
    else if (buttons[0]) focusKey(buttons[0].dataset.key);
    else $("overlay").querySelector(".close").focus();
  }
  function activate(button) {
    if (!button) return;
    const action = button.dataset.action,
      id = button.dataset.id || "";
    if (action === "menu") {
      showMenu();
      return;
    }
    if (action === "close") {
      closeMenu();
      return;
    }
    if (["language-menu", "background-menu", "palette-menu", "selection-app-menu"].includes(action)) {
      showMenu(action.replace("-menu", ""));
      return;
    }
    if (action === "favorites") {
      showMenu("favorites");
      return;
    }
    if (action === "about") {
      showMenu("about");
      return;
    }
    if (action === "selection-app") {
      device(action, id);
      showMenu("settings");
      return;
    }
    if (action === "favorite-toggle") {
      const values = model.favorites.includes(id)
        ? model.favorites.filter((v) => v !== id)
        : [...model.favorites, id];
      device("favorites", JSON.stringify(values));
      if (menu === "app") closeMenu();
      return;
    }
    if (action === "move-menu" || action === "move-favorite-menu") {
      showMenu(
        action === "move-favorite-menu" ? "move-favorite" : "move",
        model.apps.find((a) => a.id === id),
      );
      return;
    }
    if (action === "move" || action === "move-favorite") {
      device(action, id);
      toast(t("Ordre enregistré"));
      return;
    }
    device(action, id);
    if (
      [
        "resume",
        "app",
        "input",
        "tv",
        "stock",
        "settings",
        "home",
        "tmdb",
      ].includes(action) &&
      menu
    )
      closeMenu();
  }
  function toast(value) {
    clearTimeout(toastTimer);
    $("toast").textContent = value;
    $("toast").hidden = false;
    toastTimer = setTimeout(() => ($("toast").hidden = true), 4500);
  }
  function key(key) {
    if (["left", "right", "up", "down"].includes(key)) move(key);
    else if (key === "select") activate(document.activeElement);
    else if (key === "menu") showMenu();
    else if (key === "back") {
      if (menu) closeMenu();
      else focusKey("feature");
    } else if (key === "long") {
      if (menu) return;
      menuScope = document.activeElement?.closest("#favorites") ? "favorites" : "apps";
      const id = document.activeElement?.dataset.id,
        app = model.apps.find((a) => a.id === id);
      if (app) showMenu("app", app);
    }
  }
  document.addEventListener("click", (event) => {
    const button = event.target.closest("button");
    if (button) activate(button);
  });
  let enterTimer = null,
    longPressed = false;
  function mappedKey(event) {
    return (
      {
        ArrowLeft: "left",
        ArrowRight: "right",
        ArrowUp: "up",
        ArrowDown: "down",
        Enter: "select",
        " ": "select",
        Escape: "back",
      }[event.key] ||
      {
        19: "up",
        20: "down",
        21: "left",
        22: "right",
        23: "select",
        13: "select",
      }[event.keyCode]
    );
  }
  document.addEventListener("keydown", (event) => {
    const mapped = mappedKey(event);
    if (!mapped) return;
    event.preventDefault();
    if (mapped === "select") {
      if (!enterTimer && !event.repeat) {
        longPressed = false;
        enterTimer = setTimeout(() => {
          longPressed = true;
          key("long");
        }, 550);
      }
    } else key(mapped);
  });
  document.addEventListener("keyup", (event) => {
    if (mappedKey(event) !== "select") return;
    event.preventDefault();
    clearTimeout(enterTimer);
    enterTimer = null;
    if (!longPressed) key("select");
    longPressed = false;
  });
  function art(id, url) {
    if (!localImage(url)) return;
    const item = model.resumes.find((i) => i.id === id);
    if (item) item.image = url;
    document.querySelectorAll('[data-action="resume"]').forEach((button) => {
      if (button.dataset.id !== id) return;
      let image = button.querySelector("img");
      if (image && image.src === url) return;
      if (!image) {
        image = document.createElement("img");
        image.alt = "";
        image.decoding = "async";
        if (button.classList.contains("feature"))
          image.className = "feature-image";
        button.insertBefore(image, button.firstChild);
      }
      image.src = url;
    });
    // The DOM now matches the model; keep the same decoded images on the next refresh.
    markup.set("continue", selectionMarkup());
  }
  function clock() {
    const now = new Date();
    $("clock").textContent = now.toLocaleTimeString(
      model.language === "en" ? "en-GB" : "fr-FR",
      {
        hour: "2-digit",
        minute: "2-digit",
      },
    );
    $("date").textContent = now.toLocaleDateString(
      model.language === "en" ? "en-GB" : "fr-FR",
      {
        weekday: "short",
        day: "numeric",
        month: "long",
      },
    );
  }
  clock();
  setInterval(clock, 60000);
  document.querySelector(".settings-button").dataset.key = "settings";
  window.NiakUI = {
    update(value) {
      model = value;
      render();
    },
    key,
    toast,
    art,
    refreshClock: clock,
  };
  render();
})();
