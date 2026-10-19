package tv.niak.launcher;

import static org.junit.Assert.*;

import java.util.*;
import org.junit.Test;

public class OrderTest {
  @Test
  public void dailyRotationAvoidsYesterdayAfterCatalogueChanges() {
    assertEquals(2, Order.dailyIndexAvoiding(1, Arrays.asList(10L, 20L, 30L), 20L));
    assertEquals(0, Order.dailyIndexAvoiding(1, Collections.singletonList(20L), 20L));
  }

  @Test
  public void preservesUserOrderRemovesUninstalledAndAppendsNewApps() {
    assertEquals(
        Arrays.asList("nuvio", "youtube", "vlc"),
        Order.reconcile(
            Arrays.asList("gone", "nuvio", "nuvio"), Arrays.asList("youtube", "nuvio", "vlc")));
  }

  @Test
  public void movingAnAppNeverDuplicatesOrDropsOthers() {
    assertEquals(Arrays.asList("c", "a", "b"), Order.move(Arrays.asList("a", "b", "c"), "c", 0));
    assertEquals(Arrays.asList("b", "c", "a"), Order.move(Arrays.asList("a", "b", "c"), "a", 99));
  }

  @Test
  public void dailyChoiceChangesAndWraps() {
    assertEquals(0, Order.dailyIndex(8, 8));
    assertEquals(1, Order.dailyIndex(9, 8));
    assertEquals(7, Order.dailyIndex(-1, 8));
  }
}
