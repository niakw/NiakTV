package tv.niak.launcher;

import static org.junit.Assert.*;

import java.util.*;
import org.junit.Test;

public class DefaultAppsTest {
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
