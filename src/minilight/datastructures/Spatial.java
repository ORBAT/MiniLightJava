package minilight.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import minilight.scene.Triangle;
import minilight.scene.Vector;
import static java.lang.Math.max;

/**
 * A minimal spatial index for ray tracing.<br/><br/>
 *
 * Suitable for a scale of 1 metre == 1 numerical unit, and has a resolution of
 * 1 millimetre. (Implementation uses fixed tolerances)
 *
 * Constant.<br/><br/>
 *
 * A degenerate State pattern: typed by isBranch_m field to be either a branch
 * or leaf cell.<br/><br/>
 *
 * Octree: axis-aligned, cubical. Subcells are numbered thusly:
 * <pre>
 *            110---111
 *            /|    /|
 *         010---011 |
 *    y z   | 100-|-101
 *    |/    |/    | /
 *    .-x  000---001      </pre><br/><br/>
 *
 * Each cell stores its bound: fatter data, but simpler code.<br/><br/>
 *
 * Calculations for building and tracing are absolute rather than incremental --
 * so quite numerically solid. Uses tolerances in: bounding triangles (in
 * Triangle::getBound), and checking intersection is inside cell (both effective
 * for axis-aligned items). Also, depth is constrained to an absolute subcell
 * size (easy way to handle overlapping items).
 * @author Tom Eklöf
 */
public class Spatial
{

  public static final int N_TREE = 8;
  private boolean _isBranch;
  private float[] _bounds = new float[6];
  private Triangle[] _triangles = null;
  private Spatial[] _spatial = null;
  private final int MAX_LEVELS = 44;
  private final int MAX_ITEMS = 8;

  @SuppressWarnings("empty-statement")
  public Spatial(Vector eyePosition, List<Triangle> items)
  {
    // set overall bound
    // accommodate eye position
    // (makes tracing algorithm simpler)

    for (int i = 6; i-- > 0; _bounds[i] = eyePosition.get(i % 3));
    float[] itemBounds;
    float maxSize;

    for (Triangle item : items)
    {
      itemBounds = item.getBound();
      for (int j = 0; j < 6; ++j)
        if ((_bounds[j] > itemBounds[j]) ^ (j > 2))
          _bounds[j] = itemBounds[j];
    }

    maxSize = 0f;

    // Make the bounds cubical
    for (int i = 0; i < 3; ++i)
      maxSize = max(maxSize, _bounds[3 + i] - _bounds[i]);

    for (int i = 0; i < 3; ++i)
      _bounds[3 + i] = max(_bounds[3 + i], _bounds[i] + maxSize);

    // Construct the cell tree
    construct(items, 0);

  }

  private Spatial(float[] bounds)
  {
    _bounds = Arrays.copyOf(bounds, bounds.length);
  }

  public Spatial construct(List<Triangle> items, int level)
  {
    /*
     * if there are too many items and the tree is not too deep, make
     * this node into a branch.
     */

    _isBranch = (items.size() > MAX_ITEMS) && (level < (MAX_LEVELS - 1));

    if (_isBranch)
    { // Make sub-cells, recurse construction
      _spatial = new Spatial[N_TREE];
      float[] itemBound, subBound;
      boolean isOverlap;
      int nextLevel;

      List<Triangle> subItems;

      for (int s = N_TREE, q = 0; s-- > 0;)
      {
        subBound = new float[6];
        subItems = new ArrayList<Triangle>(items.size());

        for (int i = items.size(); i-- > 0;)
        {
          itemBound = items.get(i).getBound();
          isOverlap = true;


          for (int j = 0, d = 0, m = 0; j < 6; ++j, d = j / 3, m = j % 3)
          {
            subBound[j] = (((s >> m) & 1) ^ d) == 1
                          ? (_bounds[m] + _bounds[m + 3]) * 0.5f
                          : _bounds[j];

            // Must overlap in all dimensions
            isOverlap &= (itemBound[(d ^ 1) * 3 + m] >= subBound[j]) ^ (d != 0);
            if (!isOverlap)
              break;
          }
          if (isOverlap)
            subItems.add(items.get(i));
        }
        q += subItems.size() == items.size() ? 1 : 0;
        nextLevel = (q > 1)
                    || ((subBound[3] - subBound[0])
                        < (Triangle.TOLERANCE * 4.0f))
                    ? MAX_LEVELS : level + 1;
        // recursion.
        _spatial[s] = !(subItems.size() == 0) ? (new Spatial(subBound)).
            construct(subItems, nextLevel) : null;
      }
    }
    else // I'm a leaf! Just store the items
      _triangles = items.toArray(new Triangle[0]);
    return this;
  }

  /**
   * Calculates which object a ray hits and the position of the hit.
   * @return An array with two elements, the first is the Triangle that got hit and
   * the second the position Vector of the hit.
   */
  public Object[] getIntersection(Vector rayOrigin, Vector rayDirection,
                                  Triangle lastHit, Vector pStart)
  {
    Vector hitPosition = null;
    Triangle pHitObject = null;
    if (_isBranch)
    { // it's a branch: step through subcells and recurse
      if (pStart == null)
        pStart = rayOrigin;

      // Find which subcell has the ray origin
      int subCell = 0;

      for (int i = 3; i-- > 0;) // XXX: remove ugly
        // compare dimension with center
        if (pStart.get(i) >= ((_bounds[i] + _bounds[i + 3]) * 0.5f))
          subCell |= 1 << i;
      Vector cellPosition = pStart;
      boolean high;
      float face, temp;
      float[] step = new float[3];
      int axis;

      while (true)
      {  // Step through intersected subcells

        if (_spatial[subCell] != null)
        {
          Object[] ret = _spatial[subCell].getIntersection(rayOrigin,
              rayDirection, lastHit,
              cellPosition);
          pHitObject = (Triangle) ret[0]; // XXX: remove the ugly
          hitPosition = (Vector) ret[1]; // XXX: remove the ugly

          if (pHitObject != null) // exit if something got hit
            break;
        }

        // find next subcell ray moves to
        // (by finding which face of the corner ahead is crossed first)
        axis = 2;
        for (int i = 3; i-- > 0; axis = step[i] < step[axis] ? i : axis)
        {
          high = ((subCell >> i) & 1) != 0;
          face = (rayDirection.get(i) < 0f) ^ high
                 ? _bounds[i + ((high ? 1 : 0) * 3)]
                 : (_bounds[i] + _bounds[i + 3]) * 0.5f;
          temp = rayDirection.get(i);
          step[i] = temp == 0 ? Float.MAX_VALUE
                    : (face - rayOrigin.get(i)) / temp;

        }

        if ((((subCell >> axis) & 1) != 0)
            ^ (rayDirection.get(axis) < 0.0f))
          break;

        cellPosition = rayOrigin.add(rayDirection.mul(step[axis]));
        subCell = subCell ^ (1 << axis);
        Arrays.fill(step, 0f); // Clears the step array.
      } // while
    } // if(_isBranch)
    else
    { // it's a leaf.
      float nearestDistance = Float.MAX_VALUE;
      float distance;
      Vector hit;
      float t;

      for (Triangle item : _triangles)
        if (item != lastHit)
        { // avoid false intersection with surface we just came from
          distance = item.getIntersection(rayOrigin, rayDirection);
          if (distance != -1f && distance < nearestDistance)
          {
            hit = rayOrigin.add(rayDirection.mul(distance));
            t = Triangle.TOLERANCE;
            float h0 = hit.get(0);
            float h1 = hit.get(1);
            float h2 = hit.get(2);
            if ((_bounds[0] - h0 <= t)
                && (h0 - _bounds[3] <= t)
                && (_bounds[1] - h1 <= t)
                && (h1 - _bounds[4] <= t)
                && (_bounds[2] - h2 <= t)
                && (h2 - _bounds[5] <= t))
            {
              pHitObject = item;
              nearestDistance = distance;
              hitPosition = hit;
            }
          }
        }
    }
    return new Object[]
        {
          pHitObject, hitPosition
        };

  }

  public boolean isBranch()
  {
    return _isBranch;
  }
}
