package tv.niak.launcher;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ArtworkCacheTest {
  @Rule public TemporaryFolder temporary = new TemporaryFolder();

  @Test
  public void cachedArtworkSurvivesNewCacheInstanceAndTmdbSizeChanges() throws Exception {
    ArtworkCache cache = new ArtworkCache(temporary.getRoot(), 16, 1024);
    byte[] bytes = {1, 2, 3};
    File file = cache.put("https://image.tmdb.org/t/p/original/test.jpg", bytes);
    ArtworkCache reopened = new ArtworkCache(temporary.getRoot(), 16, 1024);
    assertEquals(file, reopened.get("https://image.tmdb.org/t/p/w500/test.jpg"));
    assertArrayEquals(bytes, Files.readAllBytes(file.toPath()));
  }

  @Test
  public void movingBetweenHeroAndCardReusesTheCorrectVariant() throws Exception {
    ArtworkCache cache = new ArtworkCache(temporary.getRoot(), 16, 1024);
    String work = "https://image.tmdb.org/t/p/w1280/work.jpg";
    File hero = cache.put(work, ArtworkSize.HERO.key, new byte[] {1, 2});
    assertNull(cache.get(work, ArtworkSize.CARD.key));
    File card = cache.put(work, ArtworkSize.CARD.key, new byte[] {3});
    assertNotEquals(hero, card);
    assertEquals(hero, cache.get(work, ArtworkSize.at(0).key));
    assertEquals(card, cache.get(work, ArtworkSize.at(1).key));
    assertNull(cache.get("https://image.tmdb.org/t/p/w1280/new-work.jpg", ArtworkSize.HERO.key));
  }

  @Test
  public void emptyOrMissingArtworkIsNotPublished() throws Exception {
    ArtworkCache cache = new ArtworkCache(temporary.getRoot(), 16, 1024);
    assertNull(cache.get(null));
    assertNull(cache.get("http://example.com/image.jpg"));
    assertNull(cache.get("https://example.com/missing.jpg"));
    try {
      cache.put("https://example.com/empty.jpg", new byte[0]);
      fail("Empty entry must be rejected");
    } catch (IOException expected) {
      assertNull(cache.get("https://example.com/empty.jpg"));
    }
  }

  @Test
  public void entryLimitEvictsOldestAndPreservesNewImage() throws Exception {
    ArtworkCache cache = new ArtworkCache(temporary.getRoot(), 2, 1024);
    File old = cache.put("https://example.com/old.jpg", new byte[4]);
    old.setLastModified(1);
    cache.put("https://example.com/other.jpg", new byte[4]);
    File newest = cache.put("https://example.com/new.jpg", new byte[4]);
    assertFalse(old.exists());
    assertTrue(newest.exists());
    assertEquals(2, temporary.getRoot().list().length);
  }

  @Test
  public void byteLimitAndReplacementKeepCompleteFilesOnly() throws Exception {
    ArtworkCache cache = new ArtworkCache(temporary.getRoot(), 16, 6);
    File old = cache.put("https://example.com/old.jpg", new byte[4]);
    old.setLastModified(1);
    File newest = cache.put("https://example.com/new.jpg", new byte[] {5, 6, 7, 8});
    assertFalse(old.exists());
    cache.put("https://example.com/new.jpg", new byte[] {9, 10});
    assertArrayEquals(new byte[] {9, 10}, Files.readAllBytes(newest.toPath()));
    assertEquals(1, temporary.getRoot().list().length);
  }
}
