package es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment;


import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;

/**
 * Abstract class for representing geometrical shapes.
 *
 * @author ccottap, ppgllrd
 */
public abstract class Shape {

  /**
   * Constructs a shape from a json object.
   *
   * @param json the json object.
   * @return a shape constructed from the provided json object.
   */
  public static Shape fromJson(JsonObject json) {
    String typeStr = json.getStringOrDefault(JsonKeys.TYPE).toUpperCase();
    ShapeType type = typeStr.isEmpty() ? ShapeType.RECTANGLE : ShapeType.valueOf(typeStr);
    return switch (type) {
      case CIRCLE -> Circle.fromJson(json);
      case POLYGON -> Polygon.fromJson(json);
      case RECTANGLE -> Rectangle.fromJson(json);
    };
  }

  /**
   * Returns the type of the shape.
   *
   * @return the type of the shape.
   */
  public abstract ShapeType getType();

  /**
   * Returns geometrical {@code java.awt.Shape} corresponding to the obstacle.
   *
   * @return geometrical {@code java.awt.Shape} corresponding to the obstacle.
   */
  public abstract java.awt.Shape getAWTShape();

  /**
   * Checks whether a point is contained withing this shape.
   *
   * @param x x-coordinate of point.
   * @param y y-coordinate of point.
   * @return {@code true} if point is contained within this shape.
   */
  public boolean contains(double x, double y) {
    return getAWTShape().contains(x, y);
  }

  /**
   * Checks whether a rectangle intersects with this shape.
   *
   * @param left   X coordinate of bottom left corner of rectangle.
   * @param bottom Y coordinate of bottom left corner of rectangle.
   * @param width  width of rectangle.
   * @param height height of rectangle.
   * @return {@code true} if rectangle intersects with this shape.
   */
  public boolean intersects(double left, double bottom, double width, double height) {
    return getAWTShape().intersects(left, bottom, width, height);
  }

  /**
   * Returns json representation of shape.
   *
   * @return json representation of shape.
   */
  public abstract JsonObject toJson();

  public static class Circle extends Shape {
    private final double x;
    private final double y;
    private final double radius;

    /**
     * internal circle object
     */
    private final Ellipse2D.Double circle;

    /**
     * Basic constructor for circle shape.
     *
     * @param x      x-coordinate of the circle center.
     * @param y      y-coordinate of the circle center.
     * @param radius radius of the circle.
     */
    public Circle(double x, double y, double radius) {
      this.x = x;
      this.y = y;
      this.radius = radius;
      double diameter = 2 * radius;
      circle = new Ellipse2D.Double(x - radius, y - radius, diameter, diameter);
    }

    /**
     * Constructs a circle from a json object.
     *
     * @param json the json object.
     * @return a circle constructed from the provided json object.
     */
    public static Circle fromJson(JsonObject json) {
      JsonObject jsonCenter = json.getMap(JsonKeys.CENTER);
      Point2D point = Point2D.fromJson(jsonCenter);
      return new Circle(point.getX(), point.getY(), json.getDouble(JsonKeys.RADIUS));
    }

    @Override
    public ShapeType getType() {
      return ShapeType.CIRCLE;
    }

    @Override
    public java.awt.Shape getAWTShape() {
      return circle;
    }

    @Override
    public JsonObject toJson() {
      JsonObject json = new JsonObject();
      json.put(JsonKeys.TYPE, getType().toString());
      json.put(JsonKeys.CENTER, Point2D.toJson(x, y));
      json.put(JsonKeys.RADIUS, radius);
      return json;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Circle circle = (Circle) o;
      return Double.compare(circle.x, x) == 0 && Double.compare(circle.y, y) == 0 && Double.compare(circle.radius, radius) == 0;
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y, radius);
    }
  }

  public static class Polygon extends Shape {
    private final List<Point2D> points;

    /**
     * Internal polygon object
     */
    private final java.awt.geom.Path2D.Double path;

    /**
     * Basic constructor for polygon shape.
     *
     * @param points a list of points defining the polygon.
     */
    public Polygon(List<Point2D> points) {
      this.points = points;
      path = Path2D.fromPoints(points);
    }

    /**
     * Constructs a polygon from a json object.
     *
     * @param json the json object.
     * @return a polygon constructed from the provided json object.
     */
    public static Polygon fromJson(JsonObject json) {
      JsonArray jsonArray = json.getCollection(JsonKeys.POINTS);
      return new Polygon(Path2D.fromJsonArray(jsonArray));
    }

    @Override
    public ShapeType getType() {
      return ShapeType.POLYGON;
    }

    @Override
    public java.awt.Shape getAWTShape() {
      return path;
    }

    @Override
    public JsonObject toJson() {
      JsonObject json = new JsonObject();
      json.put(JsonKeys.TYPE, getType().toString());
      json.put(JsonKeys.POINTS, Path2D.toJsonArray(points));
      return json;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Polygon polygon)) {
        return false;
      }
      return Objects.equals(points, polygon.points);
    }

    @Override
    public int hashCode() {
      return Objects.hash(points);
    }
  }

  public static class Rectangle extends Shape {
    private final double left;
    private final double bottom;
    private final double width;
    private final double height;

    /**
     * Internal rectangle object
     */
    private final Rectangle2D.Double rectangle;

    /**
     * Basic constructor for rectangle shaped.
     *
     * @param left   x-coordinate of the bottom-left corner of rectangle.
     * @param bottom y-coordinate of the bottom-left corner of the rectangle.
     * @param width  width of the rectangle.
     * @param height height of the rectangle.
     */
    public Rectangle(double left, double bottom, double width, double height) {
      this.left = left;
      this.bottom = bottom;
      this.width = width;
      this.height = height;
      rectangle = new Rectangle2D.Double(left, bottom, width, height);
    }

    /**
     * Constructs a rectangle from a json object.
     *
     * @param json the json object.
     * @return a rectangle constructed from the provided json object.
     */
    public static Rectangle fromJson(JsonObject json) {
      JsonObject jsonBottomLeft = json.getMap(JsonKeys.BOTTOMLEFT);
      Point2D point = Point2D.fromJson(jsonBottomLeft);
      double left = point.getX();
      double bottom = point.getY();
      double width = json.getDouble(JsonKeys.WIDTH);
      double height = json.getDouble(JsonKeys.HEIGHT);
      return new Rectangle(left, bottom, width, height);
    }

    @Override
    public ShapeType getType() {
      return ShapeType.RECTANGLE;
    }

    @Override
    public java.awt.Shape getAWTShape() {
      return rectangle;
    }

    public JsonObject toJson() {
      JsonObject json = new JsonObject();
      json.put(JsonKeys.TYPE, getType().toString());
      json.put(JsonKeys.BOTTOMLEFT, Point2D.toJson(left, bottom));
      json.put(JsonKeys.WIDTH, width);
      json.put(JsonKeys.HEIGHT, height);
      return json;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Rectangle rectangle)) {
        return false;
      }
      return Double.compare(rectangle.left, left) == 0 && Double.compare(rectangle.bottom, bottom) == 0 && Double.compare(rectangle.width, width) == 0 && Double.compare(rectangle.height, height) == 0;
    }

    @Override
    public int hashCode() {
      return Objects.hash(left, bottom, width, height);
    }
  }
}
