package minilight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import minilight.datastructures.Image;
import minilight.rendering.RayTracer;
import minilight.rendering.SeparateImageRenderer;
import minilight.scene.Camera;
import minilight.scene.Scene;
import minilight.utils.MagicNumbers;

class ThreadCreator
{

  private ExecutorService es;

  public void startWorkers(int iterations, Scene scn, Camera cam)
  {
    es = Executors.newFixedThreadPool(MagicNumbers.NUM_THREADS);
    ArrayList<Future<Image>> results = new ArrayList<Future<Image>>();

    int iterDivider = iterations / MagicNumbers.NUM_THREADS;
    int remainder = iterations % MagicNumbers.NUM_THREADS;

    for (int i = 0; i < MagicNumbers.NUM_THREADS; ++i)
    {
      if (i == MagicNumbers.NUM_THREADS - 1)
        // last thread has to take care of the remainder
        iterDivider += remainder;
      results.add(es.submit(new SeparateImageRenderer(iterDivider, scn,
          cam,
          MagicNumbers.getPictureWidth(),
          MagicNumbers.getPictureHeight())));
    }
    es.shutdown();
    ArrayList<Image> images =
                     new ArrayList<Image>(MagicNumbers.NUM_THREADS);

    for (Future<Image> f : results)
      try
      {
        images.add(f.get());
      }
      catch (InterruptedException ex)
      {
        System.out.println("Retrieving results from a thread failed (ie) ("
                           + ex.getMessage() + ")");
        ex.printStackTrace(System.out);
        System.exit(2);
      }
      catch (ExecutionException ex)
      {
        System.out.println("Retrieving results from a thread failed (ee) ("
                           + ex.getMessage() + ")");
        ex.printStackTrace(System.out);
        System.exit(2);
      }

    Image img = Image.addImages(images);
    try
    {
      // FIXME: save image DURING rendering too, not just when finished
      img.saveImage(MagicNumbers.IMAGE_FILENAME, iterations, false);
    }
    catch (IOException ex)
    {
      System.out.println("Error saving image file (" + ex.getMessage() + ")");
    }
  }
}

/**
 *
 * @author Tom Eklöf
 */
public class Main
{

  public static void main(String[] args)
  {

    long startTime = System.currentTimeMillis();
    long lastSaveTime = startTime;

    // Initializes some "constants" like camera position, image size and so on.
    MagicNumbers.initMagicNumbers(args);

    Image i = null;
    if (!MagicNumbers.PARALLEL)
      i = new Image(MagicNumbers.getPictureWidth(),
          MagicNumbers.getPictureHeight());
    Camera c = new Camera(MagicNumbers.getCameraPosition(),
        MagicNumbers.getCameraDirection(),
        MagicNumbers.getCameraAngle());
    Scene s = new Scene(MagicNumbers.getModelVectors(), c.getCameraPosition(),
        MagicNumbers.getSkyEmissivity(), MagicNumbers.getGroundReflectivity());

    int iterations = MagicNumbers.getNumIterations();

    if (MagicNumbers.PARALLEL)
    {

      System.out.println("Starting multi-threaded renderer with "
                         + MagicNumbers.NUM_THREADS + " threads...");
      ThreadCreator t = new ThreadCreator();
      t.startWorkers(iterations, s, c);

    }
    else
    {
      RayTracer rt = new RayTracer(s);
      System.out.println("Starting single-threaded renderer...");
      final String ESC = "\033[";
      for (int frameNo = 0; frameNo <= iterations; ++frameNo)
      {
        c.getFrame(s, i, rt);
        System.out.format("Iteration: %d of %d. Time elapsed: %d", frameNo,
            iterations, (System.currentTimeMillis() - startTime)
                        / 1000);
        System.out.print(ESC + "1G");  // XXX: fix this ugly hack. Might not work on all platforms.
        System.out.flush();
        if ((frameNo == 1
             || (System.currentTimeMillis() - lastSaveTime > MagicNumbers.SAVE_PERIOD
                                                             * 1000)))
        {
          try
          {
            i.saveImage(MagicNumbers.IMAGE_FILENAME, frameNo-1, false);
          }
          catch (IOException ex)
          {
            System.out.println("Error saving image file (" + ex.getMessage()
                               + ")");
          }
          lastSaveTime = System.currentTimeMillis();
        }
      }
      try
      {
        // save at the end of rendering too
        i.saveImage(MagicNumbers.IMAGE_FILENAME, iterations, false);
      }
      catch (IOException ex)
      {
        System.out.println("Error saving image file (" + ex.getMessage() + ")");
      }
    }
    System.out.println("Rendering took "
                       + (System.currentTimeMillis() - startTime) / 1000
                       + " seconds");

  }
}
