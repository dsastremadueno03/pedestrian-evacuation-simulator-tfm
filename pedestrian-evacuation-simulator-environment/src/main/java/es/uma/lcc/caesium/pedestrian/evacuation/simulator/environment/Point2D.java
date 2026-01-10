package es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment;

import com.github.cliftonlabs.json_simple.JsonObject;

import java.util.Objects;

/**
 * Auxiliary class for representing 2D points.
 *
 * @author ppgllrd
 */
public class Point2D {
  private final double x;
  private final double y;

  public Point2D(double x, double y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Constructs a json object with provided coordinates.
   * @param x x-coordinate of point.
   * @param y y-coordinate of point.
   * @return  a json object with provided coordinates.
   */
  static JsonObject toJson(Double x, Double y) {
    JsonObject json = new JsonObject();
    json.put(JsonKeys.X, x);
    json.put(JsonKeys.Y, y);
    return json;
  }

  /**
   * Constructs a json object with coordinates of this point.
   * @return a json object with coordinates of this point.
   */
  JsonObject toJson() {
    return toJson(x, y);
  }

  /**
   * Constructs a point from a json object with coordinates.
   * @param json a json object with coordinates of a point.
   * @return a point from a json object with coordinates.
   */
  static Point2D fromJson(JsonObject json) {
    return new Point2D(json.getDouble(JsonKeys.X), json.getDouble(JsonKeys.Y));
  }

  /**
   * Returns x-coordinate of point.
   * @return x-coordinate of point.
   */
  public double getX() {
    return x;
  }

  /**
   * Returns y-coordinate of point.
   * @return y-coordinate of point.
   */
  public double getY() {
    return y;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Point2D point2D = (Point2D) o;
    return Double.compare(point2D.x, x) == 0 && Double.compare(point2D.y, y) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }
}
