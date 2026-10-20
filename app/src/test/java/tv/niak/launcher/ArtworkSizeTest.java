package tv.niak.launcher;

import static org.junit.Assert.*;

import org.junit.Test;

public class ArtworkSizeTest {
  @Test
  public void recencyChangeSelectsNewRenditionWithoutChangingWorkIdentity() {
    assertSame(ArtworkSize.HERO, ArtworkSize.at(0));
    assertSame(ArtworkSize.CARD, ArtworkSize.at(1));
    assertSame(ArtworkSize.CARD, ArtworkSize.at(31));
  }

  @Test
  public void smallCardCropsInvisibleSidesBeforeEncoding() {
    int[] p = ArtworkSize.CARD.crop(1280, 720);
    assertEquals(288, p[4]);
    assertEquals(332, p[5]);
    assertTrue(p[2] < 1280);
    assertTrue(p[0] > 0);
    assertEquals(720, p[3]);
  }

  @Test
  public void heroCropsInvisibleVerticalPixelsAndNeverUpscales() {
    int[] p = ArtworkSize.HERO.crop(1024, 576);
    assertEquals(1024, p[2]);
    assertEquals(1024, p[4]);
    assertTrue(p[3] < 576);
    assertTrue(p[1] > 0);
    assertTrue(p[5] <= 308);
  }

  @Test
  public void tinyAndPortraitImagesStayWithinTheirBounds() {
    for (ArtworkSize size : new ArtworkSize[] {ArtworkSize.HERO, ArtworkSize.CARD}) {
      for (int[] dimensions : new int[][] {{50, 30}, {300, 900}, {1, 1}}) {
        int[] p = size.crop(dimensions[0], dimensions[1]);
        assertTrue(p[0] >= 0 && p[1] >= 0);
        assertTrue(p[0] + p[2] <= dimensions[0] && p[1] + p[3] <= dimensions[1]);
        assertTrue(p[4] <= p[2] && p[5] <= p[3]);
        assertTrue(p[4] <= size.width && p[5] <= size.height);
      }
    }
  }
}
