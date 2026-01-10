package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.trace;

import com.github.cliftonlabs.json_simple.JsonObject;

/**
 * A class to represent a pedestrian in a trace.
 *
 * @author Pepe Gallardo
 */
public record Pedestrian(int id, Location location) {
  public static Pedestrian fromJson(JsonObject json) {
    var id = json.getInteger(JsonKeys.ID);
    JsonObject jsonLocation = json.getMap(JsonKeys.LOCATION);
    var location = Location.fromJson(jsonLocation);
    return new Pedestrian(id, location);
  }

  public JsonObject toJson() {
    var json = new JsonObject();
    json.put(JsonKeys.ID.getKey(), id);
    json.put(JsonKeys.LOCATION.getKey(), location.toJson());
    return json;
  }
}
