package minilight.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import minilight.scene.Vector;

/**
 *
 * @author orbat
 */
public class ModelReader
{
  /*XXX: remove the stupid from this class. Will probably require
   * revamping Vector.readVectors() and learning how to do IO properly...
   */

  /*
   * #MiniLight
   *
   * 100
   * ^ iterations
   * 280 280
   * ^ image width & height
   *                                v   camera angle
   * (0 0.650 -1.273) (0 -0.425 1) 45
   * ^ camera position  ^ camera direction
   *           v ground reflection
   * (0 0 0) (0 0 0)
   * ^ sky emissivity
   */
  private final Pattern P_INTEGERS = Pattern.compile("\\d+");
  // The camera angle comes after a vector definition
  private final Pattern P_CAM_ANGLE = Pattern.compile("\\(.+\\)\\s*(\\d+)");
  private int _numIterations;
  private final int _cameraAngle;
  private final int[] _pictureDimensions = new int[2];
  private final Vector _cameraPosition;
  private final Vector _cameraDirection;
  private final Vector _skyEmissivity;
  private final Vector _groundReflectivity;
  private final List<Vector> _modelVectors;

  public ModelReader(String path) throws IllegalStateException
  {
    File f = new File(path);
    Scanner s;
    try
    {
      s = new Scanner(f);
    }
    catch (FileNotFoundException ex)
    {
      throw new IllegalStateException(ex);
    }

    try
    {
      try
      {
        _numIterations = Integer.valueOf(s.findWithinHorizon(P_INTEGERS, 0));
        _pictureDimensions[0] = Integer.valueOf(s.findWithinHorizon(P_INTEGERS,
            0));
        _pictureDimensions[1] = Integer.valueOf(s.findWithinHorizon(P_INTEGERS,
            0));
        s.findWithinHorizon(P_CAM_ANGLE, 0); // camera angle
        _cameraAngle = Integer.valueOf(s.match().group(1));
      }
      catch (Exception e)
      {
        throw new IllegalStateException(e);
      }

      LinkedList<Vector> vectors = null;
      try
      {
        vectors = new LinkedList<Vector>(Vector.readVectors(f));
      }
      catch (Exception ex)
      {
        throw new IllegalStateException(ex);
      }

      _cameraPosition = vectors.pollFirst();
      _cameraDirection = vectors.pollFirst();
      _skyEmissivity = vectors.pollFirst();
      _groundReflectivity = vectors.pollFirst();

      System.out.format(
          "%n%n----Model data----%nIterations: %d%nImage size: "
          + "%dx%d%nCamera angle: %d%n",
          _numIterations, _pictureDimensions[0], _pictureDimensions[1],
          _cameraAngle);

      System.out.format(
          "Camera pos: %s%nCamera dir: %s%nSky emiss.: %s%nGround refl.: %s%n",
          _cameraPosition, _cameraDirection, _skyEmissivity, _groundReflectivity);
      System.out.format("Number of vectors: %d%n", vectors.size());

      assert vectors.size() % 5 == 0 : "Number of vectors in file not "
                                       + "divisible by 5: " + vectors.size();

      _modelVectors = vectors;
    }
    finally
    {
      s.close();
    }
  }

  public int getNumIterations()
  {
    return _numIterations;
  }

  public int getCameraAngle()
  {
    return _cameraAngle;
  }

  public int[] getPictureDimensions()
  {
    return _pictureDimensions;
  }

  public int getPictureWidth()
  {
    return _pictureDimensions[0];
  }

  public int getPictureHeight()
  {
    return _pictureDimensions[1];
  }

  public Vector getCameraPosition()
  {
    return _cameraPosition;
  }

  public Vector getCameraDirection()
  {
    return _cameraDirection;
  }

  public Vector getSkyEmissivity()
  {
    return _skyEmissivity;
  }

  public Vector getGroundReflectivity()
  {
    return _groundReflectivity;
  }

  public List<Vector> getModelVectors()
  {
    return _modelVectors;
  }

  public void setNumIterations(int i)
  {
    _numIterations = i;
  }
}
