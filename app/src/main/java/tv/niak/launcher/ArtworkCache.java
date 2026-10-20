package tv.niak.launcher;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/** Bounded on-disk artwork cache. Only complete files are exposed to the UI. */
final class ArtworkCache {
  private final File directory;
  private final int maxEntries;
  private final long maxBytes;

  ArtworkCache(File directory, int maxEntries, long maxBytes) {
    this.directory = directory;
    this.maxEntries = maxEntries;
    this.maxBytes = maxBytes;
  }

  static String source(String address) {
    if (address == null || !address.startsWith("https://")) return "";
    return address.replaceAll(
        "image\\.tmdb\\.org/t/p/(?:original|w[0-9]+)/", "image.tmdb.org/t/p/w1280/");
  }

  private File file(String address, String variant) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256")
              .digest(
                  (source(address) + (variant.isEmpty() ? "" : "|" + variant))
                      .getBytes(StandardCharsets.UTF_8));
      StringBuilder name = new StringBuilder();
      for (byte b : digest) name.append(String.format(Locale.ROOT, "%02x", b));
      return new File(directory, name + ".webp");
    } catch (NoSuchAlgorithmException impossible) {
      throw new IllegalStateException(impossible);
    }
  }

  synchronized File get(String address) {
    return get(address, "");
  }

  synchronized File get(String address, String variant) {
    if (source(address).isEmpty()) return null;
    File file = file(address, variant);
    if (!file.isFile() || file.length() == 0) return null;
    file.setLastModified(System.currentTimeMillis());
    return file;
  }

  synchronized File legacy(String address) {
    if (source(address).isEmpty()) return null;
    File old = new File(directory, file(address, "").getName().replace(".webp", ".jpg"));
    return old.isFile() && old.length() > 0 ? old : null;
  }

  synchronized File put(String address, byte[] bytes) throws IOException {
    return put(address, "", bytes);
  }

  synchronized File put(String address, String variant, byte[] bytes) throws IOException {
    if (source(address).isEmpty() || bytes.length == 0 || bytes.length > maxBytes)
      throw new IOException("Invalid artwork cache entry");
    File destination = file(address, variant);
    File temporary = File.createTempFile("art-", ".tmp", directory);
    try {
      try (FileOutputStream out = new FileOutputStream(temporary)) {
        out.write(bytes);
      }
      if (!temporary.renameTo(destination)) throw new IOException("Cannot publish artwork");
      trim(destination);
      return destination;
    } finally {
      temporary.delete();
    }
  }

  private void trim(File newest) {
    File[] files = directory.listFiles((dir, name) -> name.matches("[a-f0-9]{64}\\.(?:jpg|webp)"));
    if (files == null) return;
    Arrays.sort(files, Comparator.comparingLong(File::lastModified));
    long total = 0;
    for (File entry : files) total += entry.length();
    int count = files.length;
    for (File entry : files) {
      if (count <= maxEntries && total <= maxBytes) break;
      long size = entry.length();
      if (!entry.equals(newest) && entry.delete()) {
        count--;
        total -= size;
      }
    }
  }
}
