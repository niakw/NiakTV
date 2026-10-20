package tv.niak.launcher;

/**
 * Coalesces duplicate Google Home events until NiakTV has actually resumed. Visibility changes
 * release the gate; failed launches have a bounded retry window.
 */
final class HomeRedirectGate {
  private boolean launching, visible;
  private long launchAt;

  boolean request(boolean enabled, CharSequence packageName, CharSequence className, long now) {
    if (!enabled
        || visible
        || (launching && now - launchAt < 750)
        || !"com.google.android.tvlauncher".contentEquals(packageName == null ? "" : packageName)
        || !"com.google.android.tvlauncher.MainActivity"
            .contentEquals(className == null ? "" : className)) return false;
    launching = true;
    launchAt = now;
    return true;
  }

  void visibility(boolean value) {
    visible = value;
    reset();
  }

  void reset() {
    launching = false;
  }
}
