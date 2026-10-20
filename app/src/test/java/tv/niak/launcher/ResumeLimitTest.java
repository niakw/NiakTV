package tv.niak.launcher;

import static org.junit.Assert.*;

import java.util.*;
import org.junit.Test;

public class ResumeLimitTest {
  @Test
  public void defaultsToThreeAndPreservesRecencyOrder() {
    assertEquals(3, ResumeLimit.valid(0));
    assertEquals(Arrays.asList(9, 8, 7), ResumeLimit.apply(Arrays.asList(9, 8, 7, 6, 5), 3));
  }

  @Test
  public void cyclesThroughChoicesThenReturnsToDefault() {
    assertEquals(5, ResumeLimit.next(3));
    assertEquals(8, ResumeLimit.next(5));
    assertEquals(32, ResumeLimit.next(8));
    assertEquals(3, ResumeLimit.next(32));
  }

  @Test
  public void allAndShortListsDoNotInventOrDropItems() {
    List<Integer> items = Arrays.asList(1, 2, 3, 4, 5, 6);
    assertEquals(items, ResumeLimit.apply(items, 32));
    assertEquals(Arrays.asList(1), ResumeLimit.apply(Arrays.asList(1), 3));
    assertTrue(ResumeLimit.apply(Collections.emptyList(), 3).isEmpty());
  }
}
