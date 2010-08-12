package minilight.scene;

import minilight.datastructures.Image;
import minilight.rendering.RayTracer;
import static java.lang.Math.tan;
import java.util.Random;
import minilight.utils.MagicNumbers;

/**
 *
 * @author Tom Eklöf
 */
public class Camera
{

  protected final Vector _viewPosition;
  protected final float _viewAngle;
  protected final Vector _viewDirection;
  protected final Vector _right;
  protected final Vector _up;
  protected final Random rand = MagicNumbers.random;

  public Camera(Vector viewPosition, Vector viewDirection, float viewAngle)
  {

    viewDirection = viewDirection.unitize();
    if (viewDirection.isZero())
      _viewDirection = new Vector(0f, 0f, 1f);
    else
      _viewDirection = viewDirection;

    if (viewAngle < 10)
      viewAngle = 10;
    else if (viewAngle > 160)
      viewAngle = 160;

    _viewAngle = viewAngle * (float) (java.lang.Math.PI / 180);

    Vector up = new Vector(0f, 1f, 0f);
    Vector right = up.cross(_viewDirection).unitize();

    if (!right.isZero())
    {
      _right = right;
      _up = viewDirection.cross(right).unitize();
    }
    else
    {
      _up = new Vector(0.0f,
          0.0f,
          viewDirection.get(1) < 0.0f ? 1.0f : -1.0f);
      _right = _up.cross(viewDirection).unitize();
    }

    _viewPosition = viewPosition;
  }

  public void getFrame(Scene scn, Image img, RayTracer rt)
  {
    //RayTracer rt = new RayTracer(scn);  // TODO: use a shared RayTracer?
    int w = img.getWidth();
    int h = img.getHeight();

    for (int y = 0; y < h; ++y)
      for (int x = 0; x < w; ++x)
      {
        float halfAngle = (float) tan(_viewAngle * 0.5f);
        // image plane displacement vector coefficients
        float xf = ((x + rand.nextFloat()) * 2f / w) - 1f;
        float yf = ((y + rand.nextFloat()) * 2f / h) - 1f;
        // image plane offset vector
        Vector offset = _right.mul(xf).add(
            _up.mul(yf).mul((float) h / (float) w));
        // sample ray direction, stratified by pixels
        Vector sampleDir = _viewDirection.add(offset.mul(halfAngle)).unitize();
        Vector radiance = rt.getRadiance(_viewPosition, sampleDir, null);

        img.addToPixel(x, y, radiance);
      }
  }

  public Vector getCameraPosition()
  {
    return _viewPosition;
  }

  public float getViewAngle()
  {
    return _viewAngle;
  }

  public Vector getViewDirection()
  {
    return _viewDirection;
  }

  public Vector getRight()
  {
    return _right;
  }

  public Vector getUp()
  {
    return _up;
  }
}
