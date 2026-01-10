package es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment;

import com.github.cliftonlabs.json_simple.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class for handling domains, namely enclosed areas that the
 * pedestrians can navigate.
 *
 * @author ccottap, ppgllrd
 */
public class Domain {
  /**
   * id of the domain
   */
  private final int id;
  /**
   * height of the domain
   */
  private final double height;
  /**
   * width of the domain
   */
  private final double width;
  /**
   * list of obstacles in the domain
   */
  private final List<Obstacle> obstacles;
  /**
   * list of accesses in the domain
   */
  private final List<Access> accesses;
  /**
   * name of the domain
   */
  private String name;
  /**
   * description of the domain
   */
  private String description;

  /**
   * Creates an empty domain with the given id and dimensions.
   * Initially there are no obstacles or accesses.
   *
   * @param id     The id of the domain.
   * @param width  The width of the domain.
   * @param height The height of the domain.
   */
  public Domain(int id, double width, double height) {
    this.id = id;
    this.width = width;
    this.height = height;
    name = "domain" + id;
    description = "";
    obstacles = new ArrayList<>();
    accesses = new ArrayList<>();
  }

  /** Constructs a domain from a json object.
   * @param json The json object to parse.
   * @return a domain from the provided json.
   */
  public static Domain fromJson(JsonObject json) {
    int id = json.getInteger(JsonKeys.ID);
    double width = json.getDouble(JsonKeys.WIDTH);
    double height = json.getDouble(JsonKeys.HEIGHT);
    Domain domain = new Domain(id, width, height);

    domain.setName(json.getStringOrDefault(JsonKeys.NAME));
    domain.setDescription(json.getStringOrDefault(JsonKeys.DESCRIPTION));
    if (json.containsKey(JsonKeys.OBSTACLES.getKey())) {
      List<JsonObject> jsonObstacles = json.getCollection(JsonKeys.OBSTACLES);
      for (JsonObject jsonObstacle : jsonObstacles) {
        domain.addObstacle(Obstacle.fromJson(jsonObstacle));
      }
    }
    if (json.containsKey(JsonKeys.ACCESSES.getKey())) {
      List<JsonObject> jsonAccesses = json.getCollection(JsonKeys.ACCESSES);
      for (JsonObject jsonAccess : jsonAccesses) {
        domain.addAccess(Access.fromJson(jsonAccess));
      }
    }
    return domain;
  }

  /**
   * Returns the id of the domain.
   *
   * @return the id of the domain.
   */
  public int id() {
    return id;
  }

  /**
   * Returns the name of the domain.
   *
   * @return the name of the domain.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the domain.
   *
   * @param name of the domain.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the description of the domain.
   *
   * @return the description of the domain.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description of the domain.
   *
   * @param description the description of the domain.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the height of the domain.
   *
   * @return the height of the domain.
   */
  public double getHeight() {
    return height;
  }

  /**
   * Returns the width of the domain.
   *
   * @return the width of the domain.
   */
  public double getWidth() {
    return width;
  }

  /**
   * Adds an obstacle to the domain.
   *
   * @param obstacle an obstacle.
   */
  public void addObstacle(Obstacle obstacle) {
    obstacles.add(obstacle);
  }

  /**
   * Adds an access to the domain.
   *
   * @param a an access.
   */
  public void addAccess(Access a) {
    accesses.add(a);
  }

  /**
   * Returns the list of obstacles in the domain.
   *
   * @return the obstacles.
   */
  public List<Obstacle> getObstacles() {
    return obstacles;
  }

  /**
   * Returns the list of accesses in the domain.
   *
   * @return the accesses.
   */
  public List<Access> getAccesses() {
    return accesses;
  }

  /**
   * Constructs a json representation of the domain.
   * @return a json representation of the domain.
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
    json.put(JsonKeys.WIDTH, width);
    json.put(JsonKeys.HEIGHT, height);
    if (!obstacles.isEmpty()) {
      List<JsonObject> jsonObstacles = new ArrayList<>();
      for (Obstacle obstacle : obstacles) {
        jsonObstacles.add(obstacle.toJson());
      }
      json.put(JsonKeys.OBSTACLES, jsonObstacles);

    }
    if (!accesses.isEmpty()) {
      List<JsonObject> jsonAccesses = new ArrayList<>();
      for (Access a : accesses) {
        jsonAccesses.add(a.toJson());
      }
      json.put(JsonKeys.ACCESSES, jsonAccesses);
    }
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
    Domain domain = (Domain) o;
    return id == domain.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
