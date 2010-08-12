package minilight.scene;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import minilight.datastructures.Spatial;
import minilight.utils.MagicNumbers;

/**
 *
 * @author Tom Eklöf
 */
public class Scene
{

  private final List<Triangle> _triangles; //FIXME: change all lists into arrays
  private final List<Triangle> _emitters;
  private final Spatial _octree;
  private final Vector _skyEmission;
  private final Vector _groundReflection;
  // 2^20 = 16^5 = 1048576
  final int MAX_TRIANGLES = 0x100000;
  final int MAX_EMITTERS = (1 << 16);
  private final Random _rand = MagicNumbers.random;

  public Scene(List<Vector> vectors,
               Vector cameraPosition,
               Vector skyEmission_t,
               Vector groundReflection_t)
  {

    _skyEmission = skyEmission_t.clamp(Vector.ZERO, skyEmission_t);
    _groundReflection = _skyEmission.mul(groundReflection_t.clamp(Vector.ZERO,
        Vector.ONE));

    _triangles = Triangle.makeTriangles(vectors);

    _emitters = new LinkedList<Triangle>();

    for (Triangle t : _triangles)
      if (!t.getEmissivity().isZero() && t.getArea() > 0f)
      {
        _emitters.add(t);
        if (_emitters.size() >= MAX_EMITTERS)
          break;
      }
    System.out.println("Scene() emitters: " + _emitters.size());
    System.out.println("Scene() triangles " + _triangles.size());

    _octree = new Spatial(cameraPosition, _triangles);
  }

  /**
   * Calculates which object a ray hits and the position of the hit.
   * @return An array with two elements: {Triangle, Vector}
   */
  public Object[] getIntersection(Vector rayOrigin, Vector rayDirection,
                                  Triangle lastHit)
  {
    // XXX: modify getIntersection so it returns something sensible?
    return _octree.getIntersection(rayOrigin, rayDirection, lastHit, null);
  }

  /**
   * Gets an emitter triangle and a sample point on the emitter.
   * @return {Vector, Triangle}
   */
  public Object[] getEmitter()
  {

    if (_emitters.size() != 0)
    {
      Triangle t = _emitters.get(_rand.nextInt(_emitters.size()));
      Vector v = t.getSamplePoint();
      return new Object[]
          {
            v, t
          };
    }
    else
      return new Object[]
          {
            Vector.ZERO, null
          };
  }

  public int getEmittersAmount()
  {
    return _emitters.size();
  }

  public Vector getDefaultEmission(Vector backDirection)
  {
    if (backDirection.get(1) < 0f)
      return _skyEmission;
    else
      return _groundReflection;
  }
}
