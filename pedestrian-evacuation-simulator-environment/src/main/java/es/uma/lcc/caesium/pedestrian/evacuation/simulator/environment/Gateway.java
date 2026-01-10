package es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment;


import com.github.cliftonlabs.json_simple.JsonObject;

import java.util.Objects;

/**
 * Class for representing gateways, namely connections that allow pedestrians
 * moving from a certain domain to another adjacent domain.
 *
 * @param id          the id of the gateway.
 * @param name        the name of the gateway (can be empty).
 * @param description the description of the gateway (can be empty).
 * @param domain1     a domain in one of the endpoints of the gateway.
 * @param domain2     a domain in the other endpoint of the gateway.
 * @author ccottap, ppgllrd
 */
public record Gateway(int id, String name, String description, int domain1, int domain2) {
  /**
   * Constructs a gateway from the provided json.
   * @param json a json object containing the information of the gateway.
   * @return a gateway from the provided json.
   */
  public static Gateway fromJson(JsonObject json) {
    int id = json.getInteger(JsonKeys.ID);
    String name = json.getStringOrDefault(JsonKeys.NAME);
    String description = json.getStringOrDefault(JsonKeys.DESCRIPTION);
    int domain1 = json.getInteger(JsonKeys.DOMAIN1);
    int domain2 = json.getInteger(JsonKeys.DOMAIN2);
    return new Gateway(id, name, description, domain1, domain2);
  }

  /** Constructs a gateway from the provided json.
   * @return a json object containing the information of the gateway.
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
    json.put(JsonKeys.DOMAIN1, domain1);
    json.put(JsonKeys.DOMAIN2, domain2);
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
    Gateway gateway = (Gateway) o;
    return id == gateway.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
