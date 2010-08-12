package minilight.scene;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.PI;
import java.util.Random;
import minilight.utils.MagicNumbers;

/**
 *
 * @author Tom Eklöf
 */
public class SurfacePoint
{

  private final Triangle _item;
  private final Vector _position;
  private final Random _rand = MagicNumbers.random;

  public SurfacePoint(Triangle item, Vector pos)
  {
    _item = item;
    _position = pos;
  }

  public Vector getEmission(Vector toPosition, Vector outDirection,
                            boolean isSolidAngle)
  {
    Vector ray = toPosition.sub(_position);
    float distance2 = ray.dot(ray);
    float cosArea = outDirection.dot(_item.getNormal()) * _item.getArea();
    if (cosArea <= 0) // Emit from front face of surface only
      return Vector.ZERO;

    float solidAngle = 1;
    // clamp-out infinity
    if (isSolidAngle)
    {
      if (distance2 < 1e-6f)
        distance2 = 1e-6f;
      solidAngle = cosArea / distance2;
    }

    Vector ret = _item.getEmissivity().mul(solidAngle);
    return ret;
  }

  public Vector getReflection(Vector inDirection, Vector inRadiance,
                              Vector outDirection)
  {
    float inDot = inDirection.dot(_item.getNormal());
    float outDot = outDirection.dot(_item.getNormal());

    if ((inDot < 0f) ^ (outDot < 0f))
      return Vector.ZERO;


    return inRadiance.mul(_item.getReflectivity()).mul((float) (abs(inDot) / PI));
  }

  /**
   * Calculates the next direction of the ray.
   * @param inDirection
   * @return {Vector outDir, Vector color}
   */
  public Object[] getNextDirection(Vector inDirection)
  {
    float reflectivityMean = _item.getReflectivity().dot(Vector.ONE) / 3f;

    assert inDirection != null : "inDirection was null";
    Vector color, outDir;
    float d = _rand.nextFloat();

    if (d < reflectivityMean)
    {
      color = _item.getReflectivity().div(reflectivityMean);

      float a2pr1 = (float) PI * 2f * _rand.nextFloat();
      float sr2 = (float) sqrt(_rand.nextFloat());

      float x = (float) cos(a2pr1) * sr2;
      float y = (float) sin(a2pr1) * sr2;
      float z = (float) sqrt(1f - (sr2 * sr2));

      Vector normal = _item.getNormal();
      Vector tangent = _item.getTangent();

      if (normal.dot(inDirection) < 0f)
        normal = normal.neg();

      outDir = tangent.mul(x).add(normal.cross(tangent).mul(y)).add(
          normal.mul(z));
    }
    else
    {
      color = Vector.ZERO;
      outDir = Vector.ZERO;
    }

    return new Object[]
        {
          outDir, color
        };
  }

  public Triangle getItem()
  {
    return _item;
  }

  public Vector getPosition()
  {
    return _position;
  }
}
