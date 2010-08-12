package minilight.rendering;

import minilight.scene.Scene;
import minilight.scene.SurfacePoint;
import minilight.scene.Triangle;
import minilight.scene.Vector;

/**
 *
 * @author orbat
 */
public class RayTracer
{

  private final Scene _scene;

  public RayTracer(Scene s)
  {
    _scene = s;

  }

  public Vector getRadiance(Vector rayOrigin, Vector rayDirection,
                            Triangle lastHit)
  {
    Vector hitPosition;
    Triangle pHitObject;
    Vector radiance;

    /*
     * XXX: modify getIntersection so that we won't need the Object[].
     * A new class, maybe?
     */
    Object[] temp = _scene.getIntersection(rayOrigin, rayDirection, lastHit);
    pHitObject = (Triangle) temp[0];
    hitPosition = (Vector) temp[1];


    if (pHitObject != null)
    {
      SurfacePoint sp = new SurfacePoint(pHitObject, hitPosition);

      radiance = (lastHit != null ? Vector.ZERO : sp.getEmission(rayOrigin,
          rayDirection.neg(), false));

      radiance = radiance.add(sampleEmitters(rayDirection, sp));


      Vector nextDirection, color;
      temp = sp.getNextDirection(rayDirection.neg());
      nextDirection = (Vector) temp[0];
      color = (Vector) temp[1];

      if (!nextDirection.isZero())  // check if surface bounces ray, recurse
        radiance = radiance.add(color.mul(getRadiance(sp.getPosition(),
            nextDirection,
            sp.getItem())));

    }
    else// no hit: scene default emission
      radiance = _scene.getDefaultEmission(rayDirection.neg());


    return radiance;
  }

  private Vector sampleEmitters(Vector rayDirection, SurfacePoint sp)
  {
    Vector radiance;
    Vector emitterPos;
    Triangle emitter;

    Object[] temp = _scene.getEmitter(); // XXX: fix all methods that return an Object[]
    emitterPos = (Vector) temp[0];
    emitter = (Triangle) temp[1];

    if (emitter != null)
    {

      // direction to emit point
      Vector emitDir = (emitterPos.sub(sp.getPosition())).unitize();

      // send shadow ray
      Triangle hitObject;
      Vector hitPos;
      temp = _scene.getIntersection(sp.getPosition(), emitDir, sp.getItem());

      hitObject = (Triangle) temp[0];
      hitPos = (Vector) temp[1];



      // if unshadowed, get inward emission value
      Vector emissionIn;
      SurfacePoint spTemp = new SurfacePoint(emitter, emitterPos);

      if ((hitObject == null) | (emitter == hitObject))
        emissionIn = spTemp.getEmission(sp.getPosition(), emitDir.neg(), true);
      else
        emissionIn = Vector.ZERO;


      // get amount reflected by surface
      radiance = sp.getReflection(emitDir, emissionIn.mul(_scene.
          getEmittersAmount()), rayDirection.neg());
    }
    else // no emitter found
      radiance = Vector.ZERO;
    return radiance;
  }
}
