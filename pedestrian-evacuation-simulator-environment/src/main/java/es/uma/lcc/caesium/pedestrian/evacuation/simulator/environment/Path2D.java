package es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Auxiliary class for building a 2D path from an iterable of points.
 *
 * @author ppgllrd
 */
class Path2D {
  /**
   * Returns a closed path constructed from an iterable of points.
   *
   * @param points iterable of points.
   * @return a closed path constructed from an iterable of points.
   */
  public static java.awt.geom.Path2D.Double fromPoints(Iterable<Point2D> points) {
    assert points.iterator().hasNext() : "Path2D.fromPoints: path should include at least one point.";

    java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
    Iterator<Point2D> it = points.iterator();
    Point2D point = it.next();
    path.moveTo(point.getX(), point.getY());
    while (it.hasNext()) {
      point = it.next();
      path.lineTo(point.getX(), point.getY());
    }
    path.closePath();
    return path;
  }

  /**
   * Constructs a json array with provided points.
   * @param points iterable of points.
   * @return a json array with provided points.
   */
  public static JsonArray toJsonArray(Iterable<Point2D> points) {
    assert points.iterator().hasNext() : "Path2d.toJsonArray: path should include at least one point.";

    JsonArray jsonArray = new JsonArray();
    for (Point2D p : points) {
      jsonArray.add(p.toJson());
    }
    return jsonArray;
  }

  /**
   * Constructs a list of points from a json array.
   * @param jsonArray a json array with points.
   * @return a list of points from a json array.
   */
  public static List<Point2D> fromJsonArray(JsonArray jsonArray) {
    assert jsonArray.size() > 0 : "Path2d.fromJsonArray: jsonArray should include at least one point.";

    List<Point2D> points = new ArrayList<>();
    for (Object object : jsonArray) {
      JsonObject json = (JsonObject) object;
      points.add(Point2D.fromJson(json));
    }
    return points;
  }
}
