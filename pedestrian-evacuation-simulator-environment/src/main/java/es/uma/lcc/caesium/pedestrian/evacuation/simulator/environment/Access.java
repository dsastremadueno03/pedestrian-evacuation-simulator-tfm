package es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment;

import com.github.cliftonlabs.json_simple.JsonObject;

import java.util.Objects;


/**
 * Class for representing accesses, namely the physical instantiation of a gateway within a certain domain.
 *
 * @param id          the id of the access.
 * @param name        the name of the access (can be empty).
 * @param description the description of the access (can be empty).
 * @param shape       a shape defining the boundary of area whose crossing leads to.
 *                    passing to another domain.
 * @author ccottap, ppgllrd
 */
public record Access(
    int id,
    String name,
    String description,
    Shape shape
) {
  /**
   * Constructs an access from the provided json.
   *
   * @param json a json object containing the information of the access.
   * @return an access from the provided json.
   */
  public static Access fromJson(JsonObject json) {
    int id = json.getInteger(JsonKeys.ID);
    String name = json.getStringOrDefault(JsonKeys.NAME);
    String description = json.getStringOrDefault(JsonKeys.DESCRIPTION);
    Shape shape = Shape.fromJson(json.getMap(JsonKeys.SHAPE));
    return new Access(id, name, description, shape);
  }

  /**
   * Returns shape corresponding to this access.
   *
   * @return shape corresponding to this access.
   */
  public Shape getShape() {
    return shape;
  }

  /**
   * @return a json object containing the information of the access.
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put(JsonKeys.ID, id);
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
    Access access = (Access) o;
    return id == access.id && Objects.equals(name, access.name) && Objects.equals(description, access.description) && Objects.equals(shape, access.shape);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, shape);
  }
  
  
  @Override
  public String toString() {
	  return toJson().toString();
  }
}
