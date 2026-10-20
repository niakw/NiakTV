package tv.niak.launcher;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/** Event driven compatibility for Philips firmware, no polling or window content access. */
public final class HomeRedirectService extends AccessibilityService {
  private static final HomeRedirectGate gate = new HomeRedirectGate();
  private static volatile boolean connected;

  static boolean isConnected() {
    return connected;
  }

  static void homeResumed() {
    gate.visibility(true);
  }

  static void homeHidden() {
    gate.visibility(false);
  }

  static void homeIntentDelivered() {
    gate.reset();
  }

  @Override
  protected void onServiceConnected() {
    super.onServiceConnected();
    gate.reset();
    connected = true;
  }

  @Override
  public void onAccessibilityEvent(AccessibilityEvent event) {
    if (event == null || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
      return;
    if (!gate.request(
        getSharedPreferences("niaktv", MODE_PRIVATE).getBoolean("redirect", true),
        event.getPackageName(),
        event.getClassName(),
        SystemClock.elapsedRealtime())) return;
    try {
      startActivity(
          new Intent(this, MainActivity.class)
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
    } catch (RuntimeException failure) {
      gate.reset();
      Log.w("NiakTV", "Could not open Home", failure);
    }
  }

  @Override
  public void onInterrupt() {}

  @Override
  public void onDestroy() {
    connected = false;
    gate.reset();
    super.onDestroy();
  }
}
