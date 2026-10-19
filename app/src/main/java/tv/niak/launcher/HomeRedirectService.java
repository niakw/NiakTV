package tv.niak.launcher;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;

/** Event driven compatibility for Philips firmware, no polling or window content access. */
public final class HomeRedirectService extends AccessibilityService {
  private long lastLaunch;

  @Override
  public void onAccessibilityEvent(AccessibilityEvent event) {
    if (!"com.google.android.tvlauncher"
        .contentEquals(event.getPackageName() == null ? "" : event.getPackageName())) return;
    if (!"com.google.android.tvlauncher.MainActivity"
        .contentEquals(event.getClassName() == null ? "" : event.getClassName())) return;
    if (!getSharedPreferences("niaktv", MODE_PRIVATE).getBoolean("redirect", true)) return;
    long now = SystemClock.elapsedRealtime();
    if (now - lastLaunch < 1200) return;
    lastLaunch = now;
    startActivity(
        new Intent(this, MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
  }

  @Override
  public void onInterrupt() {}
}
