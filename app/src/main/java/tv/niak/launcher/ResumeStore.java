package tv.niak.launcher;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import java.util.*;
import org.json.*;

/** Read only the Nuvio programs Android exposes; never read Nuvio's private files. */
final class ResumeStore {
  static final class Item {
    String id, title, episode, art, launch;
    String image = "";
    long position, duration, engagement;

    JSONObject json() throws JSONException {
      return new JSONObject()
          .put("id", id)
          .put("title", title)
          .put("episode", episode)
          .put("image", image)
          .put("position", position)
          .put("duration", duration);
    }
  }

  static List<Item> read(Context context) {
    Map<String, Item> found = new LinkedHashMap<>();
    String[] columns = {
      "_id",
      "title",
      "episode_title",
      "season_display_number",
      "episode_display_number",
      "intent_uri",
      "poster_art_uri",
      "last_playback_position_millis",
      "duration_millis",
      "last_engagement_time_utc_millis",
      "weight"
    };
    for (String table : new String[] {"preview_program", "watch_next_program"}) {
      // Explicit projection avoids the broken Philips Android 12 split_id column.
      // The provider disallows SQL selection from ordinary launchers; use PARAM_PACKAGE.
      Uri uri =
          Uri.parse("content://android.media.tv/" + table)
              .buildUpon()
              .appendQueryParameter("package", "com.nuvio.tv")
              .build();
      try (Cursor c = context.getContentResolver().query(uri, columns, null, null, null)) {
        if (c == null) continue;
        while (c.moveToNext()) {
          Item item = new Item();
          item.launch = value(c, "intent_uri");
          item.title = value(c, "title");
          if (item.launch.isEmpty() || item.title.isEmpty() || found.containsKey(item.launch))
            continue;
          item.id = table + ":" + value(c, "_id");
          item.art = value(c, "poster_art_uri");
          String season = value(c, "season_display_number"),
              episode = value(c, "episode_display_number"),
              name = value(c, "episode_title");
          item.episode =
              (!season.isEmpty() ? "S" + season : "")
                  + (!episode.isEmpty() ? " · E" + episode : "")
                  + (!name.isEmpty() ? " · " + name : "");
          item.position = number(c, "last_playback_position_millis");
          item.duration = number(c, "duration_millis");
          item.engagement = number(c, "last_engagement_time_utc_millis");
          if (item.engagement == 0) item.engagement = number(c, "weight");
          found.put(item.launch, item);
        }
      } catch (RuntimeException ignored) {
        /* Permission unavailable or OEM provider mismatch. */
      }
    }
    List<Item> result = new ArrayList<>(found.values());
    result.sort((a, b) -> Long.compare(b.engagement, a.engagement));
    return result.size() > 32 ? new ArrayList<>(result.subList(0, 32)) : result;
  }

  private static String value(Cursor c, String column) {
    int i = c.getColumnIndex(column);
    return i < 0 || c.isNull(i) ? "" : c.getString(i);
  }

  private static long number(Cursor c, String column) {
    int i = c.getColumnIndex(column);
    return i < 0 || c.isNull(i) ? 0 : c.getLong(i);
  }
}
