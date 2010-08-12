package minilight.scene;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static java.lang.Math.min;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

/**
 *
 * @author Tom Eklöf
 */
public class Vector
{
  // Some constants

  private static final Pattern V_PATTERN = Pattern.compile(
      "\\(\\s*(\\S+?)\\s+(\\S+?)\\s+(\\S+?)\\s*\\)");
  public static final Vector ZERO = new Vector();
  public static final Vector ONE = new Vector(1f, 1f, 1f);
  public static final Vector MAX = new Vector(Float.MAX_VALUE, Float.MAX_VALUE,
      Float.MAX_VALUE);
  public static final Vector MIN = new Vector(Float.MIN_VALUE, Float.MIN_VALUE,
      Float.MIN_VALUE);
  // Coordinates
  public final float x, y, z;

  public boolean equals(Vector v)
  {
    return (v.x == x && v.y == y && v.z == z);
  }

  private static Vector vectorFromScanner(Scanner s) throws
      NumberFormatException
  {
    String temp = s.findWithinHorizon(V_PATTERN, 0);
    if (temp == null)
      return null;
    float tx, ty, tz;
    assert s.match().groupCount() == 3 : "s.match().groupCount() was" + s.match().
        groupCount() + ", should have been 3";
    MatchResult m = s.match();
    /*
     * Instead of coming up with a good regex for decimal numbers, I took the
     * easy way out and just use Float.valueOf() to check if they're valid or not.
     * Invalid numbers should just cause an exception to be thrown.
     */
    try
    {
      tx = Float.valueOf(m.group(1));
      ty = Float.valueOf(m.group(2));
      tz = Float.valueOf(m.group(3));
    } catch (NumberFormatException e)
    {
      System.out.println("Weird input data: " + e.getMessage());
      throw e;
    }
    return new Vector(tx, ty, tz);
  }

  /**
   * Reads data from an InputStream and returns all vectors contained in it.
   * @param is
   * @return
   * @throws NumberFormatException
   */
  public static Collection<Vector> readVectors(File f) throws
      NumberFormatException, FileNotFoundException
  {
    Scanner s = new Scanner(f);

    ArrayList<Vector> vectors = new ArrayList<Vector>();
    Vector temp;

    while ((temp = vectorFromScanner(s)) != null)
    {
      vectors.add(temp);
      //System.out.println("readVectors() read "+temp);
    }
    return vectors;
  }

  /**
   * Convenience method to read first n vectors from an InputStream.
   * @param is
   * @param n How many vectors to read
   * @return
   * @throws NumberFormatException
   */
  public static Collection<Vector> readVectors(File f, int n) throws
      NumberFormatException, FileNotFoundException
  {
    Scanner s = new Scanner(f);
    ArrayList<Vector> vectors = new ArrayList<Vector>();

    for (int i = 0; i < n; ++i)
      vectors.add(vectorFromScanner(s));

    return vectors;
  }

  /**
   * Default to all-zero vectors
   */
  public Vector()
  {
    x = y = z = 0f;
  }

  public Vector(Vector v)
  {
    x = v.x;
    y = v.y;
    z = v.z;
  }

  public Vector(float x, float y, float z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public float dot(Vector v)
  {
    return (x * v.x)
        + (y * v.y)
        + (z * v.z);
  }

  public Vector neg()
  {
    return new Vector(-x, -y, -z);
  }

  public Vector unitize()
  {
    float length = (float) sqrt((x * x)
        + (y * y)
        + (z * z));
    if (length == 0)
      return ZERO;

    float inv = 1.0f / length;
    return new Vector(x * inv,
        y * inv,
        z * inv);
  }

  public Vector cross(Vector v)
  {
    return new Vector((y * v.z) - (z * v.y),
        (z * v.x) - (x * v.z),
        (x * v.y) - (y * v.x));
  }

  /*
   * I'd give my left... arm for operator overloading.
   */
  public Vector add(Vector v)
  {
    return new Vector(x + v.x,
        y + v.y,
        z + v.z);
  }

  public Vector sub(Vector v)
  {
    return new Vector(x - v.x,
        y - v.y,
        z - v.z);
  }

  public Vector mul(Vector v)
  {
    return new Vector(x * v.x,
        y * v.y,
        z * v.z);
  }

  public Vector mul(float d)
  {
    return new Vector(x * d,
        y * d,
        z * d);

  }

  public Vector div(float d)
  {
    float inv = 1f / d;
    return new Vector(x * inv,
        y * inv,
        z * inv);
  }

  public boolean isZero()
  {
    return x == 0f && y == 0f && z == 0f;
  }

  /**
   * Returns a vector with given minimum and maximum coordinates. <br/><code>
   * min.x <= x <= max.x<br/>
   * min.y <= y <= max.y<br/>
   * min.z <= z <= max.z<br/></code>
   * @param min The smallest values the coordinates can have
   * @param max The largest values the coordinates can have
   * @return Clamped vector
   */
  public Vector clamp(Vector min, Vector max)
  {
    float xn = min(max(x, min.x), max.x);
    float yn = min(max(y, min.y), max.y);
    float zn = min(max(z, min.z), max.z);
    return new Vector(xn, yn, zn);
  }

  public float get(int n)
  {
    switch (n)
    {
    case 0:
      return x;
    case 1:
      return y;
    case 2:
      return z;
    default:
      throw new IllegalArgumentException("0 <= n <= 2, was " + n);
    }
  }

  @Override
  public String toString()
  {
    return String.format("(%.3f %.3f %.3f)", x, y, z);
  }
}
