package minilight.utils;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import minilight.scene.Vector;

/**
 *
 * @author Tom Eklöf
 */
public class MagicNumbers
{

  public static long RANDOM_SEED = 42;
  public static boolean PARALLEL = false;
  public final static Random random = new Random(RANDOM_SEED);
  public static int SAVE_PERIOD = 360; // in seconds
  public static int NUM_THREADS = 4;
  public static String FILENAME;
  public static String IMAGE_FILENAME;
  public static float DISPLAY_LUMINANCE_MAX = 200.0f; // guess of average screen maximum brightness
  public static ModelReader mr = null;

  public static void initMagicNumbers(String[] args)
  {


    OptionParser parsa = new OptionParser();

    OptionSpec<Long> seedOpt = parsa.accepts("seed", "Set random seed").
        withRequiredArg().ofType(Long.class).defaultsTo(42l);

    OptionSpec<Integer> threadsOpt = parsa.accepts("threads",
        "Number of threads to use. 1 naturally means no multithreading, and "
        + "this is the default").
        withRequiredArg().ofType(Integer.class).defaultsTo(1);

    OptionSpec<Integer> periodOpt = parsa.accepts("period",
        "Set the save period (in seconds). Currently only works in single-"
        + "threaded mode.").
        withRequiredArg().ofType(Integer.class).defaultsTo(360);

    OptionSpec<String> imageOpt = parsa.accepts("image",
        "Alternate name for image file. (Defaults to model name + \".ppm\")").
        withRequiredArg().ofType(String.class);

    OptionSpec<Float> luminanceOpt = parsa.accepts("luminance",
        "Sets the display luminance to be used when saving images. The lower "
        + "the number, the brighter the image.").
        withRequiredArg().ofType(Float.class).defaultsTo(200f);

    OptionSpec<Integer> iterationOpt = parsa.accepts("override",
        "Overrides the number of iterations specified in the model file.").
        withRequiredArg().ofType(Integer.class);

    OptionSpec<Void> helpOpt = parsa.accepts("help", "Prints usage information");

    OptionSet opts = null;

    try
    {
      opts = parsa.parse(args);
    }
    catch (OptionException e)
    {
      System.out.println("Invalid options: " + e.getMessage());
      System.exit(2);
    }

    if (opts.has(helpOpt))
    {
      try
      {
        parsa.printHelpOn(System.out);
        System.out.println("\nAll options can be abbreviated, so "
                           + "--luminance can be written as -l");
      }
      catch (IOException ex)
      {
        System.out.println("Error printing help text: " + ex);
        System.exit(2);
      }
      System.exit(0);
    }

    if (opts.nonOptionArguments().size() == 0)
    {
      System.out.println(
          "No model file specified on command line. Use --help or -h for help");
      System.exit(2);
    }



    FILENAME = opts.nonOptionArguments().get(0);
    IMAGE_FILENAME = opts.has(imageOpt) ? (String) opts.valueOf(imageOpt)
                     : FILENAME + ".ppm";

    System.out.println("Model file: " + FILENAME);
    System.out.println("Image file: " + IMAGE_FILENAME);

    RANDOM_SEED = seedOpt.value(opts);
    System.out.println("Random seed set to " + RANDOM_SEED);
    reseedRandom();

    NUM_THREADS = threadsOpt.value(opts);
    PARALLEL = NUM_THREADS > 1 ? true : false;
    System.out.println("Multithreading: " + PARALLEL);

    SAVE_PERIOD = periodOpt.value(opts);
    System.out.println("Save period: " + SAVE_PERIOD);

    DISPLAY_LUMINANCE_MAX = luminanceOpt.value(opts);
    System.out.println("Display luminance: " + DISPLAY_LUMINANCE_MAX);




    try
    {
      mr = new ModelReader(MagicNumbers.FILENAME);
    }
    catch (IllegalStateException e)
    {
      System.out.println("Error reading model file (" + e + ")");
      System.exit(2);
    }

    if (opts.has(iterationOpt))
    {
      mr.setNumIterations(opts.valueOf(iterationOpt));
      System.out.println("Overrode number of iterations to: "
                         + getNumIterations());
    }

  }

  public static int getPictureWidth()
  {
    return mr.getPictureWidth();
  }

  public static int getPictureHeight()
  {
    return mr.getPictureHeight();
  }

  public static Vector getCameraPosition()
  {
    return mr.getCameraPosition();
  }

  public static Vector getCameraDirection()
  {
    return mr.getCameraDirection();
  }

  public static int getCameraAngle()
  {
    return mr.getCameraAngle();
  }

  public static Vector getSkyEmissivity()
  {
    return mr.getSkyEmissivity();
  }

  public static Vector getGroundReflectivity()
  {
    return mr.getGroundReflectivity();
  }

  public static int getNumIterations()
  {
    return mr.getNumIterations();
  }

  public static List<Vector> getModelVectors()
  {
    return mr.getModelVectors();
  }

  public static void reseedRandom()
  {
    random.setSeed(RANDOM_SEED);
  }
}
