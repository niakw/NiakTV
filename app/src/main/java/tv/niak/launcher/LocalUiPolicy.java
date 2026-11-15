package tv.niak.launcher;

/** Only APK-bundled frontend files and generated images are served to the local page. */
final class LocalUiPolicy {
  static final String PAGE = "https://niaktv.local/index.html";

  private LocalUiPolicy() {}

  static boolean isOrigin(String scheme, String encodedAuthority) {
    return "https".equals(scheme) && "niaktv.local".equals(encodedAuthority);
  }

  static String mimeFor(String path) {
    if (path == null) return null;
    switch (path) {
      case "/index.html": return "text/html";
      case "/ui.css": return "text/css";
      case "/ui.js":
      case "/i18n.js": return "application/javascript";
      case "/fonts/manrope.woff2":
      case "/fonts/instrument-serif.woff2": return "font/woff2";
      case "/background.webp": return "image/webp";
      default:
        return path.matches("/art/[a-f0-9]{64}\\.webp") ? "image/webp" : null;
    }
  }
}
