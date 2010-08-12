package minilight.datastructures;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import minilight.scene.Vector;
import minilight.utils.MagicNumbers;
import static java.lang.Math.log10;
import static java.lang.Math.pow;
import static java.lang.Math.floor;

/**
 *
 * @author Tom Eklöf
 */
public class Image
{

  private final int _width;
  private final int _height;
  private final String PPM_ID = "P6";
  private final String MINILIGHT_URI = "http://www.hxa7241.org/minilight/";
  // ITU-R BT.709 standard RGB luminance weighting
  public static final Vector RGB_LUMINANCE = new Vector(0.2126f, 0.7152f,
      0.0722f);
  public static final float GAMMA_ENCODE = 0.45f; // ITU-R BT.709 standard gamma
  private final int NUM_PIXELS;
  private Vector[] _pixels;

  public Image(int width, int height)
  {
    _width = width;
    _height = height;
    NUM_PIXELS = _width * _height;
    _pixels = new Vector[NUM_PIXELS];

    Arrays.fill(_pixels, Vector.ZERO); // initialize pixels to all zero
  }

  public void addToPixel(int x, int y, Vector radiance)
  {
    final int index = x + ((_height - 1 - y) * _width);
    _pixels[index] = _pixels[index].add(radiance);
  }

  /**
   * Sums the pixel radiances of several images.
   * @param imgs A list of images to sum.
   * @return Composite of all images given in the list.
   */
  public static Image addImages(List<Image> imgs)
  {
    Image result = new Image(imgs.get(0)._width, imgs.get(0)._height);

    for (Image img : imgs)
      for (int i = 0; i < result.NUM_PIXELS; ++i)
        result._pixels[i] = result._pixels[i].add(img._pixels[i]);
    return result;
  }

  public byte[][] getImageBytes(int iteration)
  {
    byte[][] data = new byte[NUM_PIXELS][3]; // RGB

    float divider = 1.0f / ((iteration > 0 ? iteration : 0) + 1);

    float tonemapScaling = calculateToneMapping(_pixels, divider);
    float mapped;
    

    for (int i = 0; i < NUM_PIXELS; ++i)
      for (int c = 0; c < 3; ++c)
      {
        // tone mapping
        mapped = _pixels[i].get(c) * divider * tonemapScaling;
        
        // gamma encoding
        mapped = (float) pow((mapped > 0.0f ? mapped : 0.0f), GAMMA_ENCODE);

        // quantizing
        mapped = (float) floor((mapped * 255f) + 0.5f);
        data[i][c] = (byte) (mapped <= 255.0f ? mapped : 255.0f);
      } // i

    return data;
  }

  private void savePPM(String fileName, int iteration) throws IOException
  {

    byte[][] data = getImageBytes(iteration);
    DataOutputStream os = null;
    try
    {
      os = new DataOutputStream(
          new BufferedOutputStream(
          new FileOutputStream(fileName)));

      // Header
      os.writeBytes(PPM_ID + "\n# " + MINILIGHT_URI + "\n\n");
      // width, height, maxval
      os.writeBytes(getWidth() + " " + getHeight() + "\n255\n");

      for (int i = 0; i < NUM_PIXELS; ++i)
        for (int c = 0; c < 3; ++c)
          os.writeByte(data[i][c]);

    }
    catch (Exception ex)
    {
      System.out.println("Error writing PPM file: " + ex.getMessage());
      System.exit(1);
    }
    finally
    {
      os.close();
    }
  }

  public void saveImage(String fileName, int iteration, boolean asPNG) throws
      IOException
  {
    if (asPNG)
      throw new UnsupportedOperationException("Saving as PNG not supported yet");

    savePPM(fileName, iteration);
  }

  private float calculateToneMapping(Vector[] pixels, float divider)
  {
    float logMeanLuminance;
    float sumOfLogs = 0f;
    float y = 0;

    for (int i = 0; i < NUM_PIXELS; ++i)
    {
      y = _pixels[i].dot(RGB_LUMINANCE) * divider;
      sumOfLogs += log10((y > 1e-4f) ? y : 1e-4f);
    }

    logMeanLuminance = (float) pow(10f, sumOfLogs / (float) pixels.length);
    float a = 1.219f + (float) pow(MagicNumbers.DISPLAY_LUMINANCE_MAX * 0.25f,
        0.4f);
    float b = 1.219f + (float) pow(logMeanLuminance, 0.4f);

    return (float) pow(a / b, 2.5f) / MagicNumbers.DISPLAY_LUMINANCE_MAX;

  }

  public int getWidth()
  {
    return _width;
  }

  public int getHeight()
  {
    return _height;
  }
}
