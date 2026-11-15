package tv.niak.launcher;

import static org.junit.Assert.*;
import org.junit.Test;

public final class LocalUiPolicyTest {
  @Test public void exactLocalOrigin() {
    assertTrue(LocalUiPolicy.isOrigin("https", "niaktv.local"));
    assertFalse(LocalUiPolicy.isOrigin("http", "niaktv.local"));
    assertFalse(LocalUiPolicy.isOrigin("https", "example.invalid"));
    assertFalse(LocalUiPolicy.isOrigin("https", "niaktv.local:443"));
    assertFalse(LocalUiPolicy.isOrigin(null, null));
  }

  @Test public void onlyKnownAssetsAndGeneratedImages() {
    assertEquals("text/html", LocalUiPolicy.mimeFor("/index.html"));
    assertEquals("text/css", LocalUiPolicy.mimeFor("/ui.css"));
    assertEquals("application/javascript", LocalUiPolicy.mimeFor("/ui.js"));
    assertEquals("application/javascript", LocalUiPolicy.mimeFor("/i18n.js"));
    assertEquals("font/woff2", LocalUiPolicy.mimeFor("/fonts/manrope.woff2"));
    assertEquals("image/webp", LocalUiPolicy.mimeFor("/background.webp"));
    assertEquals("image/webp", LocalUiPolicy.mimeFor("/art/" + "a".repeat(64) + ".webp"));
    assertNull(LocalUiPolicy.mimeFor("/other.html"));
    assertNull(LocalUiPolicy.mimeFor("/fonts/unknown.woff2"));
    assertNull(LocalUiPolicy.mimeFor("/art/invalid.webp"));
    assertNull(LocalUiPolicy.mimeFor(null));
  }
}
