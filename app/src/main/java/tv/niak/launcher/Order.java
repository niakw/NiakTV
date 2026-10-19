package tv.niak.launcher;

import java.util.*;

/** Pure ordering policy, independent of Android and package discovery order. */
final class Order {
  static List<String> reconcile(List<String> saved, List<String> available) {
    Set<String> remaining = new LinkedHashSet<>(available);
    List<String> result = new ArrayList<>();
    for (String id : saved) if (remaining.remove(id)) result.add(id);
    result.addAll(remaining);
    return result;
  }

  static List<String> move(List<String> ids, String id, int destination) {
    List<String> result = new ArrayList<>(ids);
    if (!result.remove(id)) return result;
    result.add(Math.max(0, Math.min(destination, result.size())), id);
    return result;
  }

  static int dailyIndex(long day, int count) {
    if (count < 1) throw new IllegalArgumentException("Empty wallpaper catalogue");
    return (int) Math.floorMod(day, (long) count);
  }

  static int dailyIndexAvoiding(long day, List<Long> ids, long previous) {
    int index = dailyIndex(day, ids.size());
    return ids.size() > 1 && ids.get(index) == previous ? (index + 1) % ids.size() : index;
  }
}
