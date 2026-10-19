package tv.niak.launcher;

import android.content.*;
import android.graphics.*;
import android.util.AtomicFile;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import org.json.*;

/** Daily, foreground-only TMDB cinema artwork. The credential is never sent to WebView. */
final class WallpaperStore {
  private final Context context;
  private final SharedPreferences prefs;
  private final AtomicFile cache;

  WallpaperStore(Context context) {
    this.context = context.getApplicationContext();
    prefs = context.getSharedPreferences("niaktv", Context.MODE_PRIVATE);
    cache = new AtomicFile(new File(context.getFilesDir(), "daily-cinema.jpg"));
  }

  boolean configured() {
    return !context.getSharedPreferences("niaktv-secrets", 0).getString("tmdb", "").isEmpty();
  }

  Bitmap cached() {
    if (!"tmdb".equals(prefs.getString("wall_source", ""))) return null;
    try (InputStream stream = cache.openRead()) {
      return decorate(decode(stream));
    } catch (IOException | RuntimeException e) {
      return null;
    }
  }

  Bitmap fallback() {
    BitmapFactory.Options o = new BitmapFactory.Options();
    o.inScaled = false;
    o.inMutable = true;
    o.inPreferredConfig = Bitmap.Config.RGB_565;
    return decorate(
        BitmapFactory.decodeResource(context.getResources(), R.drawable.cinema_fallback, o));
  }

  String title() {
    return "tmdb".equals(prefs.getString("wall_source", ""))
        ? prefs.getString("wall_title", "")
        : "";
  }

  String status() {
    return configured()
        ? "TMDB · Films à l’affiche · " + prefs.getString("wall_day", "")
        : "Fond original · Configurer TMDB";
  }

  Bitmap refresh(boolean force) throws IOException, JSONException {
    String credential =
        context.getSharedPreferences("niaktv-secrets", 0).getString("tmdb", "").trim();
    if (prefs.getBoolean("performance", false)
        || !"cinema".equals(prefs.getString("background", "cinema"))) return null;
    if (credential.isEmpty()
        || (!force
            && !prefs.getBoolean("daily", true)
            && prefs.getBoolean("wall_original_title", false))) return null;
    String today = LocalDate.now().toString();
    long now = System.currentTimeMillis();
    if (!force
        && today.equals(prefs.getString("wall_day", ""))
        && "tmdb".equals(prefs.getString("wall_source", ""))
        && prefs.getBoolean("wall_original_title", false)) return null;
    String attemptKey =
        prefs.getBoolean("wall_original_title", false) ? "wall_attempt" : "wall_original_attempt";
    if (!force && now - prefs.getLong(attemptKey, 0) < 3600000L) return null;
    prefs.edit().putLong(attemptKey, now).apply();
    String address =
        "https://api.themoviedb.org/3/movie/now_playing?language=fr-FR&region=FR&page=1";
    String bearer = credential;
    if (credential.matches("[a-fA-F0-9]{32}")) {
      address += "&api_key=" + credential;
      bearer = null;
    } else if (bearer.startsWith("Bearer ")) bearer = bearer.substring(7).trim();
    JSONObject response =
        new JSONObject(new String(fetch(address, 512 * 1024, bearer), StandardCharsets.UTF_8));
    JSONArray raw = response.getJSONArray("results");
    List<JSONObject> movies = new ArrayList<>();
    for (int i = 0; i < raw.length(); i++) {
      JSONObject movie = raw.getJSONObject(i);
      String path = movie.optString("backdrop_path");
      if (!movie.optBoolean("adult") && path.matches("/[a-zA-Z0-9._-]+\\.(jpg|png)"))
        movies.add(movie);
    }
    if (movies.isEmpty()) throw new IOException("Aucun fond cinéma disponible");
    // Upgrade legacy localized credits without replacing the matching cached artwork.
    if (!force && !prefs.getBoolean("wall_original_title", false)) {
      for (JSONObject candidate : movies) {
        if (prefs.getString("wall_url", "").endsWith(candidate.getString("backdrop_path"))) {
          prefs
              .edit()
              .putString(
                  "wall_title", candidate.optString("original_title", candidate.optString("title")))
              .putLong("wall_movie_id", candidate.optLong("id"))
              .putBoolean("wall_original_title", true)
              .apply();
          if (today.equals(prefs.getString("wall_day", ""))) return null;
          break;
        }
      }
    }
    movies.sort(Comparator.comparingLong(m -> m.optLong("id")));
    long day = LocalDate.now().toEpochDay() + prefs.getInt("wall_offset", 0);
    List<Long> ids = new ArrayList<>();
    for (JSONObject candidate : movies) ids.add(candidate.optLong("id"));
    JSONObject movie =
        movies.get(Order.dailyIndexAvoiding(day, ids, prefs.getLong("wall_movie_id", -1)));
    byte[] data =
        fetch(
            "https://image.tmdb.org/t/p/w1280" + movie.getString("backdrop_path"),
            8 * 1024 * 1024,
            null);
    Bitmap image = decode(new ByteArrayInputStream(data));
    if (image == null || image.getWidth() < 640 || image.getWidth() < image.getHeight())
      throw new IOException("Fond non valide");
    FileOutputStream out = null;
    try {
      out = cache.startWrite();
      out.write(data);
      cache.finishWrite(out);
    } catch (IOException e) {
      cache.failWrite(out);
      image.recycle();
      throw e;
    }
    prefs
        .edit()
        .putString("wall_source", "tmdb")
        .putLong("wall_movie_id", movie.optLong("id"))
        .putString("wall_title", movie.optString("original_title", movie.optString("title")))
        .putBoolean("wall_original_title", true)
        .putString("wall_day", today)
        .putString("wall_year", movie.optString("release_date"))
        .putString(
            "wall_url", "https://image.tmdb.org/t/p/w1280" + movie.getString("backdrop_path"))
        .apply();
    return decorate(image);
  }

  static Bitmap decode(InputStream in) throws IOException {
    byte[] data = readLimited(in, 8 * 1024 * 1024);
    BitmapFactory.Options b = new BitmapFactory.Options();
    b.inJustDecodeBounds = true;
    BitmapFactory.decodeByteArray(data, 0, data.length, b);
    if (b.outWidth <= 0 || b.outHeight <= 0 || b.outWidth > 12000 || b.outHeight > 12000)
      return null;
    BitmapFactory.Options o = new BitmapFactory.Options();
    o.inMutable = true;
    o.inPreferredConfig = Bitmap.Config.RGB_565;
    o.inSampleSize = 1;
    while (b.outWidth / o.inSampleSize > 1920 || b.outHeight / o.inSampleSize > 1080)
      o.inSampleSize *= 2;
    return BitmapFactory.decodeByteArray(data, 0, data.length, o);
  }

  static byte[] fetch(String address, int limit, String bearer) throws IOException {
    URL url = new URL(address);
    String host = url.getHost();
    if (!"https".equals(url.getProtocol())
        || !(host.equals("api.themoviedb.org")
            || host.equals("image.tmdb.org")
            || host.equals("images.metahub.space")))
      throw new IOException("Source d’image non autorisée");
    HttpURLConnection c = (HttpURLConnection) url.openConnection();
    c.setConnectTimeout(10000);
    c.setReadTimeout(12000);
    c.setInstanceFollowRedirects(false);
    c.setRequestProperty("User-Agent", "NiakTV/0.2");
    if (bearer != null && host.equals("api.themoviedb.org"))
      c.setRequestProperty("Authorization", "Bearer " + bearer);
    try {
      if (c.getResponseCode() != 200) throw new IOException("Source HTTP " + c.getResponseCode());
      try (InputStream in = c.getInputStream()) {
        return readLimited(in, limit);
      }
    } finally {
      c.disconnect();
    }
  }

  private static byte[] readLimited(InputStream in, int limit) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[16384];
    int n;
    while ((n = in.read(buffer)) != -1) {
      if (out.size() + n > limit) throw new IOException("Image trop volumineuse");
      out.write(buffer, 0, n);
    }
    return out.toByteArray();
  }

  private static Bitmap decorate(Bitmap bitmap) {
    if (bitmap == null) return null;
    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();
    paint.setShader(
        new LinearGradient(
            0,
            0,
            0,
            bitmap.getHeight(),
            new int[] {0x1807090E, 0x6A07090E, 0xF907090E},
            new float[] {0, .45f, 1},
            Shader.TileMode.CLAMP));
    canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);
    paint.setShader(
        new LinearGradient(
            0, 0, bitmap.getWidth(), 0, 0xC907090E, 0x1607090E, Shader.TileMode.CLAMP));
    canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);
    return bitmap;
  }
}
