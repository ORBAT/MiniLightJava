package minilight.rendering;

import java.util.concurrent.Callable;
import minilight.datastructures.Image;
import minilight.scene.Camera;
import minilight.scene.Scene;

/**
 *
 * @author Tom Eklöf
 */
public class SeparateImageRenderer implements Callable<Image>
{

  private static int taskCount = 0;
  private final int _id = ++taskCount;
  private final int _count;
  private final Scene _scene;
  private final Image _image;
  private final Camera _camera;
  private final RayTracer _raytracer;

  public SeparateImageRenderer(int n, Scene s, Camera c, int imgWidth,
                               int imgHeight)
  {
    _scene = s;
    _image = new Image(imgWidth, imgHeight);
    _camera = c;
    _count = n;
    _raytracer = new RayTracer(_scene);
    System.out.format("Thread %d created for %d iterations\n", _id, _count);
  }

  @Override
  public Image call()
  {
    for (int i = 0; i < _count; ++i)
    {
      if (i % 5 == 0)
        System.out.format("Thread %d at %d/%d\n", _id, i, _count);

      _camera.getFrame(_scene, _image, _raytracer);
      Thread.yield();
    }
    System.out.format("Thread %d finished\n", _id);
    return _image;
  }
}
