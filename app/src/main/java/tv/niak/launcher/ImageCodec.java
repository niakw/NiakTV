package tv.niak.launcher;

import android.graphics.Bitmap;
import android.os.Build;

/** Consistent quality for all raster images served by the launcher. */
final class ImageCodec {
  static final int QUALITY = 80;

  static Bitmap resize(Bitmap image, ArtworkSize size) {
    int[] crop = size.crop(image.getWidth(), image.getHeight());
    Bitmap cropped = Bitmap.createBitmap(image, crop[0], crop[1], crop[2], crop[3]);
    Bitmap small = Bitmap.createScaledBitmap(cropped, crop[4], crop[5], true);
    if (cropped != image && cropped != small) cropped.recycle();
    return small;
  }

  @SuppressWarnings("deprecation")
  static Bitmap.CompressFormat format() {
    return Build.VERSION.SDK_INT >= 30
        ? Bitmap.CompressFormat.WEBP_LOSSY
        : Bitmap.CompressFormat.WEBP;
  }
}
