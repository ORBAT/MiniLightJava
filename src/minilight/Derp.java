package minilight;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import minilight.scene.Vector;

class TaskWithResult implements Runnable
{

  private int id;
  private int min, max;

  public TaskWithResult(int id, int min, int max)
  {
    this.id = id;
    this.min = min;
    this.max = max;
  }

  public void run()
  {
    for (int i = min; i < max; ++i)
      System.out.format("(%d): %d (%d->%d)\n", id, i, min, max);
  }
}

/**
 *
 * @author orbat
 */
public class Derp
{

  public boolean derp(Double[] d)
  {
    d[0] = 15d;
    return false;
  }

  public void doink(Vector a, Vector b)
  {
    a = new Vector(1, 2, 3);
    b = new Vector(3, 2, 1);
  }

  public boolean returnarry(Vector a, Vector[] two)
  {
    two[0] = new Vector(1, 2, 3);
    two[1] = new Vector(3, 2, 1);
    return false;
  }

  public static void main(String[] args)
  {

    Derp d = new Derp();

    for (int i = 6; i-- > 0;)
      System.out.println(i);

    Double[] q = new Double[1];
    d.derp(q);
    System.out.println(q[0]);
    Vector n = null, k = null;
    d.doink(k, n);
    System.out.println(n);  // Of course this doesn't work...

    Vector[] varry = new Vector[2];
    d.returnarry(k, varry);
    System.out.println(varry[1]);

    Vector a, b;
    a = new Vector(1, 2, 3);
    b = a;
    a = Vector.ZERO;
    int[] derp = new int[3];
    System.out.println(derp[2]);

    int maxp = 16;
    System.out.println(1 << maxp);

    ExecutorService es = Executors.newCachedThreadPool();

    int countToThis = 50;
    int threadNum = 6;
    int countDivider = countToThis / threadNum;
    int remainder = countToThis % threadNum;

    int min = 0;
    System.out.format(
        "count to %d with %d threads. Divider is %d, with remainder %d\n",
        countToThis, threadNum, countDivider, remainder);

    for (int i = 0; i < threadNum; ++i)
    {
      int countAmt = (min + countDivider);
      if (i == threadNum - 1)// must add remainder + 1 to last thread
        countAmt += remainder;

      es.execute(new TaskWithResult(i, min, countAmt));


      System.out.format("Thread %d will count from %d to %d\n", i, min, countAmt
                                                                        - 1);
      min += countDivider;
    }
    es.shutdown();
    // must add remainder + 1 to last thread

    int[] hurr =
    {
      1, 2, 3
    };
    int[] durr = hurr;
    System.out.println(hurr + " " + durr);
    durr[0] = 9001;
    System.out.println(hurr[0]);

    Vector va = new Vector(2, 3, 4);
    Vector vb = new Vector(5, 6, 7);

    float ax = va.x, ay = va.y, az = va.z;
    float bx = vb.x, by = vb.y, bz = vb.z;

    float axbX = (ay * bz) - (az * by);
    float axbY = (az * bx) - (ax * bz);
    float axbZ = (ax * by) - (ay * bx);

    Vector axb = va.cross(vb);

    assert axbX == axb.x : axbX + " != "+axb.x;
    assert axbY == axb.y;
    assert axbZ == axb.z;

    float det = (ax * bx) + (ay * by) + (az*bz);
    assert det == va.dot(vb);
    System.out.println(det + " -- "+ va.dot(vb));

    /* CROSS
    return new Vector(
    (y * v.z) - (z * v.y),
    (z * v.x) - (x * v.z),
    (x * v.y) - (y * v.x));

    DOT
    return (x * v.x)
    + (y * v.y)
    + (z * v.z);

     */


  }
}
