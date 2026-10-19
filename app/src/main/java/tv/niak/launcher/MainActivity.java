package tv.niak.launcher;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.database.ContentObserver;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.media.tv.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
  private final Map<String, byte[]> icons = new ConcurrentHashMap<>();
  private final Map<String, Intent> launches = new HashMap<>();
  private final List<ResumeStore.Item> resumes = new ArrayList<>();
  private final List<String> inputIds = new ArrayList<>();
  private JSONArray appData = new JSONArray(), inputData = new JSONArray();
  private SharedPreferences prefs;
  private WallpaperStore wallpapers;
  private FrameLayout root;
  private WebView web;
  private boolean ready, refreshing, resumed, scanning, observing, rendererGone;
  private long backgroundVersion;
  private long lastScan;
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
    web.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_BOUND, true);
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
          if (!cinema()) return;
          Bitmap initial = wallpapers.cached();
          if (initial == null) initial = wallpapers.fallback();
          publishBackground(initial);
        });
    scan();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (rendererGone) {
      recreate();
      return;
    }
    resumed = true;
    if (web != null) {
      web.onResume();
      web.resumeTimers();
    }
    if (showSelection()
        && !observing
        && checkSelfPermission("android.permission.READ_TV_LISTINGS")
            == PackageManager.PERMISSION_GRANTED) {
      getContentResolver()
          .registerContentObserver(
              Uri.parse("content://android.media.tv/preview_program"), true, programsObserver);
      getContentResolver()
          .registerContentObserver(
              Uri.parse("content://android.media.tv/watch_next_program"), true, programsObserver);
      observing = true;
    }
    if (SystemClock.elapsedRealtime() - lastScan > 10000) scan();
    js("window.NiakUI&&NiakUI.refreshClock()");
    updateWallpaper(false);
    scheduleMidnight();
  }

  @Override
  protected void onPause() {
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
      if (path.startsWith("/icons/")) {
        byte[] data = icons.get(URLDecoder.decode(path.substring(7), "UTF-8"));
        return response("image/png", data == null ? new byte[0] : data, 200);
      }
      if (path.equals("/background.jpg"))
        return response(
            "image/jpeg",
            new android.util.AtomicFile(new File(getCacheDir(), "scene.jpg")).openRead());
      if (path.startsWith("/art/")) {
        String filename = path.substring(5);
        if (!filename.matches("[a-f0-9]{64}\\.jpg"))
          return response("text/plain", new byte[0], 403);
        return response("image/jpeg", new FileInputStream(new File(getCacheDir(), filename)));
      }
      String filename = path.substring(1);
      String mime =
          filename.endsWith(".css")
              ? "text/css"
              : filename.endsWith(".js")
                  ? "application/javascript"
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
    main.removeCallbacks(reloadPrograms);
    if (selectedArtwork != null) main.removeCallbacks(selectedArtwork);
    if (observing) {
      getContentResolver().unregisterContentObserver(programsObserver);
      observing = false;
    }
    if (!showSelection()) resumes.clear();
    else if (resumed
        && checkSelfPermission("android.permission.READ_TV_LISTINGS")
            == PackageManager.PERMISSION_GRANTED) {
      getContentResolver()
          .registerContentObserver(
              Uri.parse("content://android.media.tv/preview_program"), true, programsObserver);
      getContentResolver()
          .registerContentObserver(
              Uri.parse("content://android.media.tv/watch_next_program"), true, programsObserver);
      observing = true;
    }
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
              .put("showSelection", showSelection())
              .put("performance", prefs.getBoolean("performance", false))
              .put("background", background())
              .put("palette", prefs.getString("palette", "mint"))
              .put("defaultStreamingApp", DefaultApps.streaming(launches.keySet()))
              .put("nuvioInstalled", launches.containsKey("com.nuvio.tv"))
              .put("apps", appData)
              .put("inputs", inputData)
              .put(
                  "favorites",
                  new JSONArray(list("favorites", DefaultApps.selection(launches.keySet()))))
              .put("order", new JSONArray(list("order", USUAL)))
              .put("wallpaperTitle", wallpapers.title())
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
    if (scanning) return;
    scanning = true;
    catalogueIO.execute(
        () -> {
          JSONArray apps = new JSONArray(), sources = new JSONArray();
          Map<String, Intent> found = new LinkedHashMap<>();
          List<String> ids = new ArrayList<>();
          List<ResumeStore.Item> programs =
              showSelection() ? ResumeStore.read(this) : Collections.emptyList();
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
              if (!icons.containsKey(id)) {
                Bitmap b = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(b);
                Drawable icon = r.loadIcon(pm);
                icon.setBounds(4, 4, 92, 92);
                icon.draw(canvas);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.PNG, 100, out);
                icons.put(id, out.toByteArray());
                b.recycle();
              }
              apps.put(
                  new JSONObject()
                      .put("id", id)
                      .put("name", r.loadLabel(pm).toString())
                      .put("icon", ORIGIN + "icons/" + id));
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
                resumes.clear();
                if (showSelection()) resumes.addAll(programs);
                lastScan = SystemClock.elapsedRealtime();
                postModel();
                if (showSelection())
                  loadProgramArt(programs.subList(0, Math.min(3, programs.size())));
              });
        });
  }

  private void loadProgramArt(List<ResumeStore.Item> items) {
    if (!showSelection()) return;
    for (ResumeStore.Item item : items) {
      String address = item.art;
      if (address == null || !address.startsWith("https://") || address.startsWith(ORIGIN))
        continue;
      artworkIO.execute(
          () -> {
            if (!showSelection()) return;
            try {
              String url =
                  address.replaceAll(
                      "image\\.tmdb\\.org/t/p/(?:original|w[0-9]+)/", "image.tmdb.org/t/p/w1280/");
              String name = hash(url) + ".jpg";
              File file = new File(getCacheDir(), name);
              if (!file.exists()) {
                byte[] data = WallpaperStore.fetch(url, 5 * 1024 * 1024, null);
                Bitmap image = WallpaperStore.decode(new ByteArrayInputStream(data));
                if (image == null) return;
                Bitmap small =
                    Bitmap.createScaledBitmap(
                        image,
                        Math.min(1024, image.getWidth()),
                        Math.max(
                            1, (int) (image.getHeight() * Math.min(1024f / image.getWidth(), 1))),
                        true);
                try (FileOutputStream out = new FileOutputStream(file)) {
                  small.compress(Bitmap.CompressFormat.JPEG, 86, out);
                }
                if (small != image) small.recycle();
                image.recycle();
                trimArt();
              }
              file.setLastModified(System.currentTimeMillis());
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

  private static String hash(String value) throws Exception {
    byte[] digest =
        MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    StringBuilder s = new StringBuilder();
    for (byte b : digest) s.append(String.format(Locale.ROOT, "%02x", b));
    return s.toString();
  }

  private void trimArt() {
    File[] files = getCacheDir().listFiles((dir, name) -> name.matches("[a-f0-9]{64}\\.jpg"));
    if (files == null || files.length <= 16) return;
    Arrays.sort(files, Comparator.comparingLong(File::lastModified));
    for (int i = 0; i < files.length - 16; i++) files[i].delete();
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
        if (!showSelection()) break;
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
        try {
          JSONObject move = new JSONObject(id);
          List<String> order =
              Order.reconcile(list("order", USUAL), new ArrayList<>(launches.keySet()));
          prefs
              .edit()
              .putString(
                  "order",
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
        prefs.edit().putBoolean("redirect", true).apply();
        launch(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        break;
      case "stock":
        prefs.edit().putBoolean("redirect", false).apply();
        launch(
            new Intent()
                .setComponent(
                    new ComponentName(
                        "com.google.android.tvlauncher",
                        "com.google.android.tvlauncher.MainActivity")));
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
        new android.util.AtomicFile(new File(getCacheDir(), "scene.jpg"));
    FileOutputStream out = null;
    try {
      out = file.startWrite();
      image.compress(Bitmap.CompressFormat.JPEG, 90, out);
      file.finishWrite(out);
      main.post(
          () -> {
            if (!isDestroyed()) {
              backgroundVersion = System.currentTimeMillis();
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
