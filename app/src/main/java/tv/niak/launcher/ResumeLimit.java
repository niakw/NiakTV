package tv.niak.launcher;

import java.util.*;

/** One limit controls both displayed cards and automatic artwork downloads. */
final class ResumeLimit {
  private static final int[] VALUES = {3, 5, 8, 32};

  static int valid(int value) {
    for (int allowed : VALUES) if (value == allowed) return value;
    return 3;
  }

  static int next(int value) {
    for (int i = 0; i < VALUES.length; i++)
      if (VALUES[i] == valid(value)) return VALUES[(i + 1) % VALUES.length];
    return 3;
  }

  static <T> List<T> apply(List<T> items, int limit) {
    return new ArrayList<>(items.subList(0, Math.min(items.size(), valid(limit))));
  }
}
