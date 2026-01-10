package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.trace;

import com.github.cliftonlabs.json_simple.JsonObject;

/**
 * A class to represent the location of a pedestrian in a trace.
 *
 * @author Pepe Gallardo
 */
public record Location(int domain, Coordinates coordinates) {
  public static Location fromJson(JsonObject json) {
    var domain = json.getInteger(JsonKeys.DOMAIN);
    JsonObject jsonCoordinates = json.getMap(JsonKeys.COORDINATES);
    var coordinates = Coordinates.fromJson(jsonCoordinates);
    return new Location(domain, coordinates);
  }

  public JsonObject toJson() {
    var json = new JsonObject();
    json.put(JsonKeys.DOMAIN.getKey(), domain);
    json.put(JsonKeys.COORDINATES.getKey(), coordinates.toJson());
    return json;
  }
}
