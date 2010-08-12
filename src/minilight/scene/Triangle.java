package minilight.scene;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import static java.lang.Math.sqrt;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.max;
import java.util.Random;
import minilight.utils.MagicNumbers;

/**
 *
 * @author Tom Eklöf
 */
public class Triangle
{

  public static final float TOLERANCE = 1f / 1024f;
  private final Vector[] _verts;
  private final float[] _bound;
  private final Vector _edge1, _edge2;
  private final Vector _reflectivity;
  private final Vector _emissivity;
  private final Vector _normal;
  private final Vector _tangent;
  private final float _area;
  private final Random _rand = MagicNumbers.random;

  /**
   * Makes a new triangle from an array of 5 vectors.
   * @param vv The array of vectors. The first 3 are the vertexes and the last 2 are
   * the reflectivity and emissivity
   */
  public Triangle(Vector[] vv)
  {

    assert vv.length == 5 : "vv length was " + vv.length + ", needed 5";
    _verts = Arrays.copyOf(vv, 3);

    _edge1 = _verts[1].sub(_verts[0]);
    _edge2 = _verts[2].sub(_verts[0]);


    Vector pa2 = _edge1.cross(_verts[2].sub(_verts[1]));
    _area = (float) sqrt(pa2.dot(pa2)) * 0.5f;

    _bound = calculateBound();

    /*
     * Since Vector#clamp() returns a new vector anyhow there is no need to create
     * one with new here
     */
    _reflectivity = vv[3].clamp(Vector.ZERO, Vector.ONE);
    _emissivity = vv[4].clamp(Vector.ZERO, Vector.MAX);

    _tangent = _edge1.unitize();
    _normal = _tangent.cross(_verts[2].sub(_verts[1])).unitize();
  }

  /**
   *
   * @param vvc
   */
  public static List<Triangle> makeTriangles(List<Vector> vvc)
  {
    assert vvc.size() % 5 == 0 : "vvc.size() % 5 != 0 (" + vvc.size() + ")";

    LinkedList<Vector> lv = new LinkedList<Vector>(vvc);
    LinkedList<Triangle> result = new LinkedList<Triangle>();

    Vector[] temp = new Vector[5];
    for (int i = 0, j = 0; i < vvc.size(); ++i, j = i % 5)
    {
      temp[j] = lv.pollFirst();
      if (j == 4)
        result.add(new Triangle(temp));
    }

    return result;
  }

  @SuppressWarnings("empty-statement")
  private float[] calculateBound()
  {
    float[] btemp = new float[6];
    // Initialize the bound array
    float v;
    for (int i = 6; i-- > 0; btemp[i] = _verts[2].get(i % 3));

    for (int i = 0; i < 3; ++i)
      for (int j = 0, d = 0, m = 0; j < 6; ++j, d = j / 3, m = j % 3)
      {
        v = _verts[i].get(m) + ((d != 0 ? 1f : -1f) * (abs(_verts[i].get(m))
                                                       + 1f) * TOLERANCE);
        if (d == 0)
          btemp[j] = min(v, btemp[j]);
        else
          btemp[j] = max(v, btemp[j]);
      }

    return btemp;
  }

  ///////////
  /// Operations on triangles
  ///////////
  /**
   * Calculates whether a ray intersects the triangle.
   * <br/>Adapted from:
   * <cite>'Fast, Minimum Storage Ray-Triangle Intersection'
   * Moller, Trumbore;
   * Journal Of Graphics Tools, v2n1p21, 1997.
   * http://www.acm.org/jgt/papers/MollerTrumbore97/</cite>
   * @param rayOrigin
   * @param rayDirection
   *
   * @return Either the hit distance as a float or <em>-1</em> if there was no hit.
   */
  public float getIntersection(Vector rayOrigin, Vector rayDirection)
  {

    float rdx = rayDirection.x, rdy = rayDirection.y, rdz = rayDirection.z;
    float e1x = _edge1.x, e1y = _edge1.y, e1z = _edge1.z;
    float e2x = _edge2.x, e2y = _edge2.y, e2z = _edge2.z;

    // begin calculating determinant - also used to calculate U parameter
    // Vector pvec = rayDirection.cross(_edge2);
    float px = (rdy * e2z) - (rdz * e2y);
    float py = (rdz * e2x) - (rdx * e2z);
    float pz = (rdx * e2y) - (rdy * e2x);

    // if determinant is near zero, ray lies in plane of triangle
    float det = (e1x * px) + (e1y * py) + (e1z * pz);
    // float det = _edge1.dot(pvec);

    float epsilon = 0.000001f;
    if ((det > -epsilon) && (det < epsilon))
      return -1;

    float inv_det = 1f / det;

    // distance from vertex 0 to ray origin
    // Vector tvec = rayOrigin.sub(_verts[0]);
    final Vector v0 = _verts[0];
    float tx = rayOrigin.x - v0.x,
        ty = rayOrigin.y - v0.y,
        tz = rayOrigin.z - v0.z;

    // u parameter calculation + bounds testing
    //float u = tvec.dot(pvec) * inv_det;
    float u = ((tx * px) + (ty * py) + (tz * pz)) * inv_det;
    if ((u < 0f) || (u > 1f))
      return -1;

    // calculate V parameter and test bounds
    //Vector qvec = tvec.cross(_edge1);
    float qx = (ty * e1z) - (tz * e1y);
    float qy = (tz * e1x) - (tx * e1z);
    float qz = (tx * e1y) - (ty * e1x);

    //float v = rayDirection.dot(qvec) * inv_det;
    float v = ((rdx * qx) + (rdy * qy) + (rdz * qz)) * inv_det;
    if ((v < 0f) || (u + v > 1f))
      return -1;

    // calculate t: ray intersects triangle
    //float hitDistance = _edge2.dot(qvec) * inv_det;
    float hitDistance = ((e2x * qx) + (e2y * qy) + (e2z * qz)) * inv_det;

    // only allow hits in forward ray direction
    return hitDistance >= 0f ? hitDistance : -1f;
  }

  public Vector getSamplePoint()
  {
    float sqr1 = (float) sqrt(_rand.nextFloat());
    float r2 = _rand.nextFloat();

    // make barycentric coords
    float a = 1f - sqr1;
    float b = (1f - r2) * sqr1;

    // make position from barycentrics
    // calculate interpolation by using two edges as axes scaled by the
    // barycentrics
    //return edge1 * a + edge2 * b + verts[0];
    return _edge1.mul(a).add(_edge2.mul(b)).add(_verts[0]);

  }

  ///////////
  /// Here be getters. Yarr.
  ///////////
  public Vector[] getVerts()
  {
    return _verts;
  }

  public float[] getBound()
  {
    return _bound;
  }

  public Vector getEdge1()
  {
    return _edge1;
  }

  public Vector getEdge2()
  {
    return _edge2;
  }

  public Vector getReflectivity()
  {
    return _reflectivity;
  }

  public Vector getEmissivity()
  {
    return _emissivity;
  }

  public Vector getNormal()
  {
    return _normal;
  }

  public Vector getTangent()
  {
    return _tangent;
  }

  public float getArea()
  {
    return _area;
  }

  @Override
  public String toString()
  {
    String bla = new String();
    for (Vector v : _verts)
      bla += v.toString();
    return bla;

  }
}
