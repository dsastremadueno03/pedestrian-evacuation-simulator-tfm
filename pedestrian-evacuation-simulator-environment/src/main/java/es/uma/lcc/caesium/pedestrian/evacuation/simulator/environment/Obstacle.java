package es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment;

import com.github.cliftonlabs.json_simple.JsonObject;

import java.util.Objects;


/**
 * Class for representing obstacles.
 *
 * @param name        the name of the obstacle (can be empty).
 * @param description the description of the obstacle (can be empty).
 * @param shape       a shape defining the boundary of area of the obstacle.
 * @author ccottap, ppgllrd
 */
public record Obstacle(
    String name,
    String description,
    Shape shape
) {
  /**
   * Constructs an obstacle from the provided json.
   *
   * @param json a json object containing the information of the obstacle.
   * @return an obstacle from the provided json.
   */
  public static Obstacle fromJson(JsonObject json) {
    String name = json.getStringOrDefault(JsonKeys.NAME);
    String description = json.getStringOrDefault(JsonKeys.DESCRIPTION);
    Shape shape = Shape.fromJson(json.getMap(JsonKeys.SHAPE));
    return new Obstacle(name, description, shape);
  }

  /**
   * Returns shape corresponding to this obstacle.
   *
   * @return shape corresponding to this obstacle.
   */
  public Shape getShape() {
    return shape;
  }

  /**
   * @return a json object containing the information of the obstacle.
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    if (!name.isEmpty()) {
      json.put(JsonKeys.NAME, name);
    }
    if (!description.isEmpty()) {
      json.put(JsonKeys.DESCRIPTION, description);
    }
    json.put(JsonKeys.SHAPE, shape.toJson());
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
    Obstacle obstacle = (Obstacle) o;
    return Objects.equals(name, obstacle.name) && Objects.equals(description, obstacle.description) && Objects.equals(shape, obstacle.shape);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, shape);
  }
}
