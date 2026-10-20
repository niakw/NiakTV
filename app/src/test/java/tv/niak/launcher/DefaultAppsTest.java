package tv.niak.launcher;

import static org.junit.Assert.*;

import java.util.*;
import org.junit.Test;

public class DefaultAppsTest {
  @Test
  public void installedAppChoiceOverridesAutomaticNuvio() {
    Set<String> installed =
        new HashSet<>(
            Arrays.asList(
                "com.nuvio.tv",
                "com.google.android.youtube.tv",
                "com.apple.atve.androidtv.appletv"));
    assertEquals(
        "com.google.android.youtube.tv",
        DefaultApps.streaming("com.google.android.youtube.tv", installed));
    assertEquals(
        "com.apple.atve.androidtv.appletv",
        DefaultApps.streaming("com.apple.atve.androidtv.appletv", installed));
    assertEquals("com.nuvio.tv", DefaultApps.streaming("", installed));
  }

  @Test
  public void removedAppFallsBackToAnInstalledAppOrNothing() {
    assertEquals(
        "com.google.android.youtube.tv",
        DefaultApps.streaming(
            "missing.app", Collections.singleton("com.google.android.youtube.tv")));
    assertEquals("", DefaultApps.streaming("missing.app", Collections.emptySet()));
  }

  @Test
  public void nuvioIsOptional() {
    Set<String> installed =
        new HashSet<>(Arrays.asList("com.netflix.ninja", "com.android.vending"));
    assertEquals("com.netflix.ninja", DefaultApps.streaming(installed));
    assertEquals(Collections.singletonList("com.netflix.ninja"), DefaultApps.selection(installed));
  }

  @Test
  public void noStreamingAppMeansNoDefault() {
    assertTrue(DefaultApps.selection(Collections.singleton("com.android.vending")).isEmpty());
    assertEquals("", DefaultApps.streaming(Collections.emptySet()));
  }

  @Test
  public void preferenceIsDeterministic() {
    assertEquals(
        "com.nuvio.tv",
        DefaultApps.streaming(new HashSet<>(Arrays.asList("com.netflix.ninja", "com.nuvio.tv"))));
  }
}
