package tv.niak.launcher;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.database.ContentObserver;
import android.graphics.*;
import android.media.tv.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

/** Native device bridge + isolated local HTML/CSS frontend. No remote page or script can run. */
public final class MainActivity extends Activity {
  private static final String ORIGIN = "https://niaktv.local/";
  private static final List<String> USUAL =
      Arrays.asList(
          "com.nuvio.tv",
          "com.sportzx.live",
          "com.google.android.youtube.tv",
          "com.google.android.youtube.tvmusic",
          "tv.twitch.android.app",
          "com.alphainventor.filemanager",
          "org.droidtv.nettvbrowser",
          "fr.tf1.mytf1",
          "fr.francetv.pluzz",
          "fr.m6.m6replay",
          "com.android.vending",
          "com.eset.etvs.gp",
          "org.videolan.vlc");
  private final Handler main = new Handler(Looper.getMainLooper());
  private final ExecutorService catalogueIO = Executors.newSingleThreadExecutor(),
      artworkIO = Executors.newSingleThreadExecutor();
  private final Map<String, Intent> launches = new HashMap<>();
  private final List<ResumeStore.Item> resumes = new ArrayList<>();
  private final List<String> inputIds = new ArrayList<>();
  private JSONArray appData = new JSONArray(), inputData = new JSONArray();
  private SharedPreferences prefs;
  private WallpaperStore wallpapers;
  private ArtworkCache artworkCache;
  private FrameLayout root;
  private WebView web;
  private boolean ready, refreshing, resumed, scanning, observing, rendererGone;
  private long backgroundVersion;
  private volatile long artGeneration;
  private long lastScan;
  private boolean scanAgain;
  private Runnable selectedArtwork;
  private final Runnable midnight =
      () -> {
        updateWallpaper(false);
        scheduleMidnight();
      };
  private final Runnable reloadPrograms = () -> scan();
  private final ContentObserver programsObserver =
      new ContentObserver(main) {
        @Override
        public void onChange(boolean self) {
          main.removeCallbacks(reloadPrograms);
          main.postDelayed(reloadPrograms, 700);
        }
      };

  @SuppressLint("SetJavaScriptEnabled")
  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    prefs = getSharedPreferences("niaktv", 0);
    wallpapers = new WallpaperStore(this);
    artworkCache = new ArtworkCache(getCacheDir(), 64, 24L * 1024 * 1024);
    File scene = new File(getCacheDir(), "scene.webp");
    File original = new File(getFilesDir(), "daily-cinema.webp");
    if (cinema() && scene.length() > 0 && scene.lastModified() >= original.lastModified())
      backgroundVersion = scene.lastModified();
    getWindow()
        .getDecorView()
        .setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    root = new FrameLayout(this);
    root.setBackgroundColor(Color.rgb(7, 9, 14));
    WebView.setWebContentsDebuggingEnabled(
        (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
    web = new WebView(this);
    web.setBackgroundColor(Color.rgb(7, 9, 14));
    web.setOverScrollMode(View.OVER_SCROLL_NEVER);
    web.setFocusableInTouchMode(true);
    web.setRendererPriorityPolicy(
        WebView.RENDERER_PRIORITY_BOUND, prefs.getBoolean("performance", false));
    WebSettings s = web.getSettings();
    s.setJavaScriptEnabled(true);
    s.setAllowFileAccess(false);
    s.setAllowContentAccess(false);
    s.setDomStorageEnabled(false);
    s.setTextZoom(100);
    s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
    s.setBlockNetworkLoads(true);
    s.setMediaPlaybackRequiresUserGesture(true);
    s.setSupportZoom(false);
    CookieManager.getInstance().setAcceptCookie(false);
    web.addJavascriptInterface(new DeviceBridge(), "Device");
    web.setWebViewClient(
        new WebViewClient() {
          @Override
          public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return true;
          }

          @Override
          public WebResourceResponse shouldInterceptRequest(
              WebView view, WebResourceRequest request) {
            return asset(request.getUrl());
          }

          @Override
          public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
            ready = false;
            rendererGone = true;
            root.removeView(view);
            view.destroy();
            web = null;
            if (resumed) main.post(MainActivity.this::recreate);
            return true;
          }

          @Override
          public void onPageFinished(WebView view, String url) {
            if (ORIGIN.concat("index.html").equals(url)) {
              ready = true;
              web.requestFocus();
              postModel();
            }
          }
        });
    root.addView(web, new FrameLayout.LayoutParams(-1, -1));
    setContentView(root);
    web.loadUrl(ORIGIN + "index.html");
    artworkIO.execute(
        () -> {
          if (!cinema() || backgroundVersion != 0) return;
          Bitmap initial = wallpapers.cached();
          if (initial == null) initial = wallpapers.fallback();
          publishBackground(initial);
        });
    scan();
  }

  @Override
  protected void onResume() {
    super.onResume();
    HomeRedirectService.homeResumed();
    if (rendererGone) {
      recreate();
      return;
    }
    resumed = true;
    if (web != null) {
      web.onResume();
      web.resumeTimers();
    }
    syncProgramObserver();
    if (SystemClock.elapsedRealtime() - lastScan > 10000) scan();
    js("window.NiakUI&&NiakUI.refreshClock()");
    updateWallpaper(false);
    scheduleMidnight();
    postModel();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    HomeRedirectService.homeIntentDelivered();
    if (Intent.ACTION_MAIN.equals(intent.getAction()) && intent.hasCategory(Intent.CATEGORY_HOME))
      js("window.NiakUI&&NiakUI.key('back')");
  }

  @Override
  protected void onPause() {
    HomeRedirectService.homeHidden();
    resumed = false;
    main.removeCallbacks(midnight);
    main.removeCallbacks(reloadPrograms);
    if (observing) {
      getContentResolver().unregisterContentObserver(programsObserver);
      observing = false;
    }
    if (web != null) {
      web.onPause();
      web.pauseTimers();
    }
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    if (observing) getContentResolver().unregisterContentObserver(programsObserver);
    main.removeCallbacksAndMessages(null);
    catalogueIO.shutdownNow();
    artworkIO.shutdownNow();
    if (web != null) {
      root.removeView(web);
      web.removeJavascriptInterface("Device");
      web.destroy();
    }
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    js("window.NiakUI&&NiakUI.key('back')");
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
      if (event.getAction() == KeyEvent.ACTION_DOWN) js("window.NiakUI&&NiakUI.key('menu')");
      return true;
    }
    return super.dispatchKeyEvent(event);
  }

  private void js(String script) {
    if (ready && !isDestroyed()) web.evaluateJavascript(script, null);
  }

  private WebResourceResponse asset(Uri uri) {
    if (!"https".equals(uri.getScheme()) || !"niaktv.local".equals(uri.getHost()))
      return response("text/plain", new byte[0], 403);
    String path = uri.getPath();
    if (path == null || path.contains("..")) return response("text/plain", new byte[0], 403);
    try {
      if (path.equals("/background.webp"))
        return cachedImage(
            "image/webp",
            new android.util.AtomicFile(new File(getCacheDir(), "scene.webp")).openRead());
      if (path.startsWith("/art/")) {
        String filename = path.substring(5);
        if (!filename.matches("[a-f0-9]{64}\\.webp"))
          return response("text/plain", new byte[0], 403);
        return cachedImage("image/webp", new FileInputStream(new File(getCacheDir(), filename)));
      }
      String filename = path.substring(1);
      String mime =
          filename.endsWith(".css")
              ? "text/css"
              : filename.endsWith(".js")
                  ? "application/javascript"
                  : filename.endsWith(".woff2")
                      ? "font/woff2"
                      : filename.endsWith(".ttf") ? "font/ttf" : "text/html";
      return response(mime, getAssets().open(filename));
    } catch (IOException e) {
      return response("text/plain", new byte[0], 404);
    }
  }

  private WebResourceResponse response(String mime, byte[] bytes, int status) {
    WebResourceResponse r = response(mime, new ByteArrayInputStream(bytes));
    r.setStatusCodeAndReasonPhrase(status, status == 200 ? "OK" : "Unavailable");
    return r;
  }

  private WebResourceResponse cachedImage(String mime, InputStream stream) {
    WebResourceResponse result = response(mime, stream);
    Map<String, String> headers = new HashMap<>(result.getResponseHeaders());
    // Artwork is addressed by source hash; the background URL includes its file version.
    headers.put("Cache-Control", "private, max-age=31536000, immutable");
    result.setResponseHeaders(headers);
    return result;
  }

  private WebResourceResponse response(String mime, InputStream stream) {
    WebResourceResponse r = new WebResourceResponse(mime, "UTF-8", stream);
    Map<String, String> headers = new HashMap<>();
    headers.put(
        "Content-Security-Policy",
        "default-src 'none'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self'"
            + " data:; font-src 'self'; connect-src 'none'; base-uri 'none'; form-action 'none';"
            + " frame-ancestors 'none'");
    headers.put("Cache-Control", "no-cache");
    r.setResponseHeaders(headers);
    return r;
  }

  private List<String> list(String key, List<String> defaults) {
    String v = prefs.getString(key, null);
    return v == null
        ? new ArrayList<>(defaults)
        : new ArrayList<>(v.isEmpty() ? Collections.emptyList() : Arrays.asList(v.split(",")));
  }

  private String language() {
    String value = prefs.getString("language", "auto");
    return "auto".equals(value)
        ? ("fr".equals(getResources().getConfiguration().getLocales().get(0).getLanguage())
            ? "fr"
            : "en")
        : value;
  }

  private String tr(String french, String english) {
    return "fr".equals(language()) ? french : english;
  }

  private boolean nativeHome() {
    ResolveInfo home =
        getPackageManager()
            .resolveActivity(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
                PackageManager.MATCH_DEFAULT_ONLY);
    return home != null
        && home.activityInfo != null
        && getPackageName().equals(home.activityInfo.packageName);
  }

  private boolean stockHomeEnabled() {
    try {
      return getPackageManager()
          .getApplicationInfo(
              "com.google.android.tvlauncher", PackageManager.MATCH_DISABLED_COMPONENTS)
          .enabled;
    } catch (PackageManager.NameNotFoundException absent) {
      return false;
    }
  }

  private String selectionApp() {
    return DefaultApps.streaming(prefs.getString("selectionApp", ""), launches.keySet());
  }

  private boolean nuvioSelection() {
    return showSelection() && "com.nuvio.tv".equals(selectionApp());
  }

  private void syncProgramObserver() {
    boolean wanted =
        resumed
            && nuvioSelection()
            && checkSelfPermission("android.permission.READ_TV_LISTINGS")
                == PackageManager.PERMISSION_GRANTED;
    if (wanted == observing) return;
    if (observing) getContentResolver().unregisterContentObserver(programsObserver);
    if (wanted) {
      getContentResolver()
          .registerContentObserver(
              Uri.parse("content://android.media.tv/preview_program"), true, programsObserver);
      getContentResolver()
          .registerContentObserver(
              Uri.parse("content://android.media.tv/watch_next_program"), true, programsObserver);
    }
    observing = wanted;
  }

  private int resumeLimit() {
    return ResumeLimit.valid(prefs.getInt("resumeLimit", 3));
  }

  private boolean showSelection() {
    return !prefs.getBoolean("performance", false) && prefs.getBoolean("showSelection", true);
  }

  private String background() {
    return prefs.getBoolean("performance", false)
        ? "solid"
        : prefs.getString("background", "cinema");
  }

  private boolean cinema() {
    return "cinema".equals(background());
  }

  private void applyAppearance() {
    artGeneration++;
    if (web != null)
      web.setRendererPriorityPolicy(
          WebView.RENDERER_PRIORITY_BOUND, prefs.getBoolean("performance", false));
    main.removeCallbacks(reloadPrograms);
    if (selectedArtwork != null) main.removeCallbacks(selectedArtwork);
    if (!nuvioSelection()) resumes.clear();
    syncProgramObserver();
    postModel();
    scan();
    if (cinema()) {
      artworkIO.execute(
          () -> {
            Bitmap image = wallpapers.cached();
            if (image == null) image = wallpapers.fallback();
            publishBackground(image);
          });
      updateWallpaper(false);
    }
    scheduleMidnight();
  }

  private void postModel() {
    if (!ready) return;
    try {
      JSONObject model =
          new JSONObject()
              .put("language", language())
              .put("nativeHome", nativeHome())
              .put("stockHomeEnabled", stockHomeEnabled())
              .put("homeRedirectEnabled", prefs.getBoolean("redirect", true))
              .put("homeRedirectConnected", HomeRedirectService.isConnected())
              .put("showSelection", showSelection())
              .put("resumeLimit", resumeLimit())
              .put("performance", prefs.getBoolean("performance", false))
              .put("background", background())
              .put("palette", prefs.getString("palette", "mint"))
              .put("defaultStreamingApp", selectionApp())
              .put(
                  "selectionAppPreference",
                  launches.containsKey(prefs.getString("selectionApp", ""))
                      ? prefs.getString("selectionApp", "")
                      : "")
              .put("nuvioInstalled", launches.containsKey("com.nuvio.tv"))
              .put("apps", appData)
              .put("inputs", inputData)
              .put(
                  "favorites",
                  new JSONArray(list("favorites", DefaultApps.selection(launches.keySet()))))
              .put("order", new JSONArray(list("order", USUAL)))
              .put("wallpaperTitle", wallpapers.title())
              .put("wallpaperReleaseDate", wallpapers.releaseDate())
              .put("wallpaperStatus", wallpapers.status())
              .put("backgroundVersion", backgroundVersion)
              .put("tmdbConfigured", wallpapers.configured())
              .put("daily", prefs.getBoolean("daily", true))
              .put(
                  "resumePermission",
                  checkSelfPermission("android.permission.READ_TV_LISTINGS")
                      == PackageManager.PERMISSION_GRANTED);
      JSONArray programs = new JSONArray();
      for (ResumeStore.Item item : resumes) {
        if (item.image.startsWith(ORIGIN + "art/")) {
          String name = item.image.substring((ORIGIN + "art/").length());
          if (!new File(getCacheDir(), name).exists()) item.image = "";
        }
        programs.put(item.json());
      }
      model.put("resumes", programs);
      js("window.NiakUI&&NiakUI.update(" + model.toString() + ")");
    } catch (JSONException ignored) {
    }
  }

  private void scan() {
    if (scanning) {
      scanAgain = true;
      return;
    }
    scanning = true;
    catalogueIO.execute(
        () -> {
          JSONArray apps = new JSONArray(), sources = new JSONArray();
          Map<String, Intent> found = new LinkedHashMap<>();
          List<String> ids = new ArrayList<>();
          List<ResumeStore.Item> programs = new ArrayList<>();
          try {
            PackageManager pm = getPackageManager();
            List<ResolveInfo> results =
                pm.queryIntentActivities(
                    new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER),
                    0);
            results.sort(
                Comparator.comparing(
                    r -> r.loadLabel(pm).toString(), String.CASE_INSENSITIVE_ORDER));
            for (ResolveInfo r : results) {
              String id = r.activityInfo.packageName;
              if (id.equals(getPackageName())
                  || id.equals("com.google.android.tvlauncher")
                  || found.containsKey(id)) continue;
              Intent launch =
                  new Intent(Intent.ACTION_MAIN)
                      .addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                      .setComponent(new ComponentName(id, r.activityInfo.name))
                      .addFlags(
                          Intent.FLAG_ACTIVITY_NEW_TASK
                              | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
              found.put(id, launch);
              apps.put(new JSONObject().put("id", id).put("name", r.loadLabel(pm).toString()));
            }
            if (showSelection()
                && "com.nuvio.tv"
                    .equals(
                        DefaultApps.streaming(prefs.getString("selectionApp", ""), found.keySet())))
              programs.addAll(ResumeLimit.apply(ResumeStore.read(this), resumeLimit()));
            for (int index = 0; index < programs.size(); index++) {
              ResumeStore.Item item = programs.get(index);
              item.size = ArtworkSize.at(index);
              File cached = artworkCache.get(item.art, item.size.key);
              if (cached != null) item.image = ORIGIN + "art/" + cached.getName();
            }
            TvInputManager manager = (TvInputManager) getSystemService(TV_INPUT_SERVICE);
            List<TvInputInfo> hdmi = new ArrayList<>();
            if (manager != null)
              for (TvInputInfo info : manager.getTvInputList())
                if (info.getType() == TvInputInfo.TYPE_HDMI
                    && !info.isHidden(this)
                    && info.getParentId() == null) hdmi.add(info);
            hdmi.sort(Comparator.comparing(i -> i.loadLabel(this).toString()));
            for (TvInputInfo info : hdmi) {
              CharSequence custom = info.loadCustomLabel(this);
              ids.add(info.getId());
              sources.put(
                  new JSONObject()
                      .put("id", info.getId())
                      .put("name", (custom != null ? custom : info.loadLabel(this)).toString())
                      .put(
                          "connected",
                          manager.getInputState(info.getId())
                              == TvInputManager.INPUT_STATE_CONNECTED));
            }
          } catch (RuntimeException | JSONException ignored) {
          }
          main.post(
              () -> {
                scanning = false;
                if (isDestroyed()) return;
                appData = apps;
                inputData = sources;
                launches.clear();
                launches.putAll(found);
                inputIds.clear();
                inputIds.addAll(ids);
                artGeneration++;
                resumes.clear();
                if (nuvioSelection()) resumes.addAll(programs);
                syncProgramObserver();
                lastScan = SystemClock.elapsedRealtime();
                postModel();
                if (nuvioSelection())
                  loadProgramArt(programs.subList(0, Math.min(resumeLimit(), programs.size())));
                if (scanAgain) {
                  scanAgain = false;
                  scan();
                }
              });
        });
  }

  private void loadProgramArt(List<ResumeStore.Item> items) {
    if (!nuvioSelection()) return;
    final long generation = artGeneration;
    for (ResumeStore.Item item : items) {
      String address = item.art;
      if (address == null || !address.startsWith("https://") || address.startsWith(ORIGIN))
        continue;
      artworkIO.execute(
          () -> {
            if (!showSelection() || generation != artGeneration) return;
            try {
              String url = ArtworkCache.source(address);
              File file = artworkCache.get(url, item.size.key);
              if (file == null) {
                File legacy = artworkCache.legacy(url);
                File source = artworkCache.get(url);
                if (source == null) source = legacy;
                Bitmap image = null;
                if (source != null) {
                  try (InputStream in = new FileInputStream(source)) {
                    image = WallpaperStore.decode(in);
                  }
                }
                // A newly promoted card may need a larger source; never enlarge its thumbnail.
                if (image != null
                    && item.size == ArtworkSize.HERO
                    && image.getWidth() < 1024
                    && url.startsWith("https://image.tmdb.org/")) {
                  image.recycle();
                  image = null;
                }
                if (image == null) {
                  String download =
                      item.size == ArtworkSize.CARD
                          ? url.replace("image.tmdb.org/t/p/w1280/", "image.tmdb.org/t/p/w780/")
                          : url;
                  byte[] data = WallpaperStore.fetch(download, 5 * 1024 * 1024, null);
                  image = WallpaperStore.decode(new ByteArrayInputStream(data));
                  if (image == null) return;
                  // Keep an uncropped source in the same bounded cache for future recency changes.
                  try {
                    ByteArrayOutputStream original = new ByteArrayOutputStream();
                    if (image.compress(ImageCodec.format(), ImageCodec.QUALITY, original))
                      artworkCache.put(url, original.toByteArray());
                  } catch (IOException ignored) {
                    // Still publish the visible rendition if retaining the source fails.
                  }
                }
                Bitmap small = ImageCodec.resize(image, item.size);
                try {
                  ByteArrayOutputStream out = new ByteArrayOutputStream();
                  if (!small.compress(ImageCodec.format(), ImageCodec.QUALITY, out)) return;
                  file = artworkCache.put(url, item.size.key, out.toByteArray());
                } finally {
                  if (small != image) small.recycle();
                  image.recycle();
                }
              }
              String name = file.getName();
              main.post(
                  () -> {
                    if (!isDestroyed() && resumes.contains(item)) {
                      item.image = ORIGIN + "art/" + name;
                      js(
                          "window.NiakUI&&NiakUI.art("
                              + JSONObject.quote(item.id)
                              + ","
                              + JSONObject.quote(item.image)
                              + ")");
                    }
                  });
            } catch (Exception ignored) {
            }
          });
    }
  }

  private void launch(Intent intent) {
    try {
      startActivity(intent);
    } catch (ActivityNotFoundException | SecurityException e) {
      Toast.makeText(
              this,
              tr(
                  "Cette action n’est pas disponible sur la TV.",
                  "This action is unavailable on this TV."),
              Toast.LENGTH_LONG)
          .show();
    }
  }

  private Intent inputIntent(String id) {
    return new Intent(Intent.ACTION_VIEW)
        .setDataAndType(
            TvContract.buildChannelUriForPassthroughInput(id), "vnd.android.cursor.item/channel");
  }

  private final class DeviceBridge {
    @JavascriptInterface
    public void action(String action, String id) {
      main.post(() -> handle(action, id));
    }
  }

  private void handle(String action, String id) {
    switch (action) {
      case "language":
        if (Arrays.asList("auto", "en", "fr").contains(id)) {
          prefs.edit().putString("language", id).apply();
          postModel();
        }
        break;
      case "selection":
        prefs
            .edit()
            .putBoolean("showSelection", !showSelection())
            .putBoolean("performance", false)
            .apply();
        applyAppearance();
        break;
      case "selection-app":
        if (id.isEmpty() || launches.containsKey(id)) {
          prefs.edit().putString("selectionApp", id).apply();
          artGeneration++;
          resumes.clear();
          syncProgramObserver();
          postModel();
          scan();
        }
        break;
      case "resume-limit":
        artGeneration++;
        prefs.edit().putInt("resumeLimit", ResumeLimit.next(resumeLimit())).apply();
        scan();
        postModel();
        break;
      case "performance":
        prefs.edit().putBoolean("performance", !prefs.getBoolean("performance", false)).apply();
        applyAppearance();
        break;
      case "background":
        if (Arrays.asList("cinema", "solid", "gradient").contains(id)) {
          prefs.edit().putString("background", id).putBoolean("performance", false).apply();
          applyAppearance();
        }
        break;
      case "palette":
        if (Arrays.asList("mint", "violet", "blue", "graphite").contains(id)) {
          prefs.edit().putString("palette", id).apply();
          postModel();
        }
        break;
      case "app":
        if (launches.containsKey(id)) launch(launches.get(id));
        break;
      case "input":
        if (inputIds.contains(id)) launch(inputIntent(id));
        break;
      case "tv":
        launch(
            new Intent(Intent.ACTION_MAIN)
                .setComponent(
                    new ComponentName("org.droidtv.playtv", "org.droidtv.playtv.PlayTvActivity")));
        break;
      case "art":
        if (!nuvioSelection()) break;
        if (selectedArtwork != null) main.removeCallbacks(selectedArtwork);
        selectedArtwork =
            () -> {
              for (ResumeStore.Item item : resumes)
                if (item.id.equals(id) && !item.art.startsWith(ORIGIN)) {
                  loadProgramArt(Collections.singletonList(item));
                  break;
                }
            };
        main.postDelayed(selectedArtwork, 240);
        break;
      case "resume":
        for (ResumeStore.Item item : resumes)
          if (item.id.equals(id)) {
            try {
              Intent intent = Intent.parseUri(item.launch, Intent.URI_INTENT_SCHEME);
              if (intent.getComponent() != null
                  && "com.nuvio.tv".equals(intent.getComponent().getPackageName()))
                launch(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception ignored) {
            }
            break;
          }
        break;
      case "favorites":
        try {
          JSONArray values = new JSONArray(id);
          List<String> valid = new ArrayList<>();
          for (int i = 0; i < values.length(); i++) {
            String value = values.getString(i);
            if (launches.containsKey(value) && !valid.contains(value)) valid.add(value);
          }
          prefs.edit().putString("favorites", android.text.TextUtils.join(",", valid)).apply();
          postModel();
        } catch (JSONException ignored) {
        }
        break;
      case "move":
      case "move-favorite":
        try {
          JSONObject move = new JSONObject(id);
          boolean favoriteMove = "move-favorite".equals(action);
          List<String> order;
          if (favoriteMove) {
            order =
                new ArrayList<>(
                    new LinkedHashSet<>(
                        list("favorites", DefaultApps.selection(launches.keySet()))));
            order.removeIf(app -> !launches.containsKey(app));
          } else {
            order = Order.reconcile(list("order", USUAL), new ArrayList<>(launches.keySet()));
          }
          prefs
              .edit()
              .putString(
                  favoriteMove ? "favorites" : "order",
                  android.text.TextUtils.join(
                      ",", Order.move(order, move.getString("id"), move.getInt("position"))))
              .apply();
          postModel();
        } catch (JSONException ignored) {
        }
        break;
      case "permission":
        if (checkSelfPermission("android.permission.READ_TV_LISTINGS")
            != PackageManager.PERMISSION_GRANTED)
          requestPermissions(new String[] {"android.permission.READ_TV_LISTINGS"}, 42);
        else scan();
        break;
      case "refresh":
        scan();
        break;
      case "tmdb":
        configureTmdb();
        break;
      case "next":
        prefs
            .edit()
            .putInt("wall_offset", prefs.getInt("wall_offset", 0) + 1)
            .putBoolean("daily", true)
            .apply();
        updateWallpaper(true);
        break;
      case "daily":
        prefs.edit().putBoolean("daily", !prefs.getBoolean("daily", true)).apply();
        updateWallpaper(false);
        scheduleMidnight();
        postModel();
        break;
      case "home":
        if (nativeHome()) {
          Toast.makeText(
                  this,
                  tr(
                      "NiakTV est l’accueil Android par défaut",
                      "NiakTV is the default Android Home"),
                  Toast.LENGTH_SHORT)
              .show();
          break;
        }
        prefs.edit().putBoolean("redirect", true).apply();
        launch(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        break;
      case "stock":
        prefs.edit().putBoolean("redirect", false).apply();
        if (stockHomeEnabled()) {
          launch(
              new Intent()
                  .setComponent(
                      new ComponentName(
                          "com.google.android.tvlauncher",
                          "com.google.android.tvlauncher.MainActivity")));
        } else {
          new AlertDialog.Builder(this)
              .setTitle(tr("Réactiver l’accueil Google", "Restore Google Home"))
              .setMessage(
                  tr(
                      "Dans l’écran suivant, choisissez Activer, puis appuyez sur Accueil. La"
                          + " redirection NiakTV est arrêtée. Vos données Google sont conservées.",
                      "On the next screen, choose Enable, then press Home. NiakTV redirection is"
                          + " off. Your Google data is retained."))
              .setNegativeButton(tr("Annuler", "Cancel"), null)
              .setPositiveButton(
                  tr("Ouvrir les réglages", "Open settings"),
                  (dialog, button) ->
                      launch(
                          new Intent(
                              Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                              Uri.parse("package:com.google.android.tvlauncher"))))
              .show();
        }
        postModel();
        break;
      case "settings":
        launch(new Intent(Settings.ACTION_SETTINGS));
        break;
    }
  }

  private void configureTmdb() {
    EditText key = new EditText(this);
    key.setSingleLine();
    key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    key.setHint(
        wallpapers.configured()
            ? tr("Remplacer la clé actuelle", "Replace current key")
            : tr("Clé API ou jeton de lecture TMDB", "TMDB API key or read access token"));
    FrameLayout box = new FrameLayout(this);
    box.setPadding(36, 10, 36, 10);
    box.addView(key);
    new AlertDialog.Builder(this)
        .setTitle(tr("TMDB · Films à l’affiche", "TMDB · Now playing"))
        .setMessage(
            tr(
                "Clé conservée uniquement sur cette TV. Aucun envoi au dépôt ou à NiakTV.",
                "Key stored only on this TV. Never sent to the repository or NiakTV."))
        .setView(box)
        .setNegativeButton(tr("Annuler", "Cancel"), null)
        .setNeutralButton(
            tr("Oublier la clé", "Forget key"),
            (d, w) -> {
              getSharedPreferences("niaktv-secrets", 0).edit().remove("tmdb").apply();
              postModel();
            })
        .setPositiveButton(
            tr("Enregistrer", "Save"),
            (d, w) -> {
              String value = key.getText().toString().trim();
              if (value.isEmpty()) return;
              getSharedPreferences("niaktv-secrets", 0).edit().putString("tmdb", value).apply();
              prefs
                  .edit()
                  .putLong("wall_attempt", 0)
                  .putString("wall_day", "")
                  .putBoolean("daily", true)
                  .apply();
              updateWallpaper(true);
              postModel();
            })
        .show();
  }

  @Override
  public void onRequestPermissionsResult(int request, String[] permissions, int[] grants) {
    super.onRequestPermissionsResult(request, permissions, grants);
    if (request == 42) scan();
  }

  private void updateWallpaper(boolean force) {
    if (!cinema()
        || refreshing
        || !wallpapers.configured()
        || (!force
            && !prefs.getBoolean("daily", true)
            && prefs.getBoolean("wall_original_title", false))) return;
    refreshing = true;
    artworkIO.execute(
        () -> {
          Bitmap updated = null;
          boolean error = false;
          try {
            updated = wallpapers.refresh(force);
          } catch (Exception e) {
            error = true;
          }
          if (updated != null) publishBackground(updated);
          boolean failed = error;
          main.post(
              () -> {
                refreshing = false;
                if (isDestroyed()) return;
                postModel();
                if (force)
                  js(
                      "window.NiakUI&&NiakUI.toast("
                          + JSONObject.quote(
                              failed
                                  ? tr(
                                      "TMDB indisponible ou clé refusée. Le fond précédent est"
                                          + " conservé.",
                                      "TMDB unavailable or key rejected. Previous wallpaper kept.")
                                  : tr("Fond cinéma actualisé", "Cinema wallpaper updated"))
                          + ")");
              });
        });
  }

  private void publishBackground(Bitmap image) {
    if (image == null) return;
    if (!cinema()) {
      image.recycle();
      return;
    }
    android.util.AtomicFile file =
        new android.util.AtomicFile(new File(getCacheDir(), "scene.webp"));
    FileOutputStream out = null;
    try {
      out = file.startWrite();
      image.compress(ImageCodec.format(), ImageCodec.QUALITY, out);
      file.finishWrite(out);
      main.post(
          () -> {
            if (!isDestroyed()) {
              backgroundVersion = file.getBaseFile().lastModified();
              postModel();
            }
          });
    } catch (IOException e) {
      file.failWrite(out);
    } finally {
      image.recycle();
    }
  }

  private void scheduleMidnight() {
    main.removeCallbacks(midnight);
    if (resumed && cinema() && prefs.getBoolean("daily", true))
      main.postDelayed(
          midnight,
          Math.max(
              1000,
              Duration.between(
                      ZonedDateTime.now(),
                      LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()))
                  .toMillis()));
  }
}
