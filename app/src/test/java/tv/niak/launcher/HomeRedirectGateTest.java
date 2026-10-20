package tv.niak.launcher;

import static org.junit.Assert.*;

import org.junit.Test;

public class HomeRedirectGateTest {
  private static final String GOOGLE = "com.google.android.tvlauncher";
  private static final String HOME = GOOGLE + ".MainActivity";

  @Test
  public void rapidReturnAfterHomeResumesIsNotDropped() {
    HomeRedirectGate gate = new HomeRedirectGate();
    assertTrue(gate.request(true, GOOGLE, HOME, 1000));
    gate.reset(); // NiakTV resumed; a second Home press needs no 1.2-second delay.
    assertTrue(gate.request(true, GOOGLE, HOME, 1000));
  }

  @Test
  public void repeatedEventsDoNotLaunchMultipleActivitiesWhileOpening() {
    HomeRedirectGate gate = new HomeRedirectGate();
    assertTrue(gate.request(true, GOOGLE, HOME, 1000));
    assertFalse(gate.request(true, GOOGLE, HOME, 1000));
  }

  @Test
  public void originalHomeAndOtherWindowsAreNotIntercepted() {
    HomeRedirectGate gate = new HomeRedirectGate();
    assertFalse(gate.request(false, GOOGLE, HOME, 1000));
    assertFalse(gate.request(true, GOOGLE, GOOGLE + ".SettingsActivity", 1000));
    assertFalse(gate.request(true, "tv.niak.launcher", HOME, 1000));
    assertFalse(gate.request(true, null, null, 1000));
    assertTrue(gate.request(true, GOOGLE, HOME, 1000));
  }

  @Test
  public void lateGoogleEventWhileNiakIsVisibleCannotPoisonTheNextHomePress() {
    HomeRedirectGate gate = new HomeRedirectGate();
    gate.visibility(true);
    assertFalse(gate.request(true, GOOGLE, HOME, 1000));
    gate.visibility(false);
    assertTrue(gate.request(true, GOOGLE, HOME, 1001));
  }

  @Test
  public void launchWithoutResumeEventuallyAllowsAnotherAttempt() {
    HomeRedirectGate gate = new HomeRedirectGate();
    assertTrue(gate.request(true, GOOGLE, HOME, 1000));
    assertFalse(gate.request(true, GOOGLE, HOME, 1100));
    assertTrue(gate.request(true, GOOGLE, HOME, 1750));
  }

  @Test
  public void failedLaunchOrServiceReconnectionAllowsRetry() {
    HomeRedirectGate gate = new HomeRedirectGate();
    assertTrue(gate.request(true, GOOGLE, HOME, 1000));
    gate.reset();
    assertTrue(gate.request(true, GOOGLE, HOME, 1000));
  }
}
