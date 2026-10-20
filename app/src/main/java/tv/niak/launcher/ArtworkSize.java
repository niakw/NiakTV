package tv.niak.launcher;

/** Position determines the rendition, never the identity of an artwork cache entry. */
final class ArtworkSize {
  static final ArtworkSize HERO = new ArtworkSize("hero-v1", 1200, 308, .65, .40);
  static final ArtworkSize CARD = new ArtworkSize("card-v1", 288, 332, .50, .50);
  final String key;
  final int width, height;
  private final double positionX, positionY;

  private ArtworkSize(String name, int width, int height, double x, double y) {
    this.key = name + "-" + width + "x" + height;
    this.width = width;
    this.height = height;
    positionX = x;
    positionY = y;
  }

  static ArtworkSize at(int index) {
    return index == 0 ? HERO : CARD;
  }

  // source x/y/width/height, followed by encoded width/height; never upscale a source.
  int[] crop(int sourceWidth, int sourceHeight) {
    if (sourceWidth <= 0 || sourceHeight <= 0) throw new IllegalArgumentException("Empty image");
    double ratio = (double) width / height;
    int w = Math.min(sourceWidth, Math.max(1, (int) Math.round(sourceHeight * ratio)));
    int h = Math.min(sourceHeight, Math.max(1, (int) Math.round(sourceWidth / ratio)));
    double scale = Math.min(1, Math.min((double) width / w, (double) height / h));
    return new int[] {
      (int) Math.round((sourceWidth - w) * positionX),
      (int) Math.round((sourceHeight - h) * positionY),
      w,
      h,
      Math.max(1, (int) Math.round(w * scale)),
      Math.max(1, (int) Math.round(h * scale))
    };
  }
}
