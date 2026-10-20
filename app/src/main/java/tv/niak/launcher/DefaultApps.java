package tv.niak.launcher;

import java.util.*;

/** Installed-only fallback. Never suggests installing an absent streaming app. */
final class DefaultApps {
  private static final List<String> STREAMING =
      Arrays.asList(
          "com.nuvio.tv",
          "com.netflix.ninja",
          "com.amazon.amazonvideo.livingroom",
          "com.disney.disneyplus",
          "com.apple.atve.androidtv.appletv",
          "com.wbd.stream",
          "com.hbo.hbonow",
          "com.canal.android.canal",
          "fr.tf1.mytf1",
          "fr.francetv.pluzz",
          "fr.m6.m6replay",
          "com.google.android.youtube.tv");

  static String streaming(String preferred, Set<String> installed) {
    return preferred != null && installed.contains(preferred) ? preferred : streaming(installed);
  }

  static String streaming(Set<String> installed) {
    for (String id : STREAMING) if (installed.contains(id)) return id;
    return "";
  }

  static List<String> selection(Set<String> installed) {
    String id = streaming(installed);
    return id.isEmpty() ? Collections.emptyList() : Collections.singletonList(id);
  }
}
