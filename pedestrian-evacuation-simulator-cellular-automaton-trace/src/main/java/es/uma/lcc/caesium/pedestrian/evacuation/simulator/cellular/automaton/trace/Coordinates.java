package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.trace;

import com.github.cliftonlabs.json_simple.JsonObject;


/**
 * A class to represent coordinates of a pedestrian in a trace.
 *
 * @author Pepe Gallardo
 */
public record Coordinates(double x, double y) {
  public static Coordinates fromJson(JsonObject json) {
    var x = json.getDouble(JsonKeys.X);
    var y = json.getDouble(JsonKeys.Y);
    return new Coordinates(x, y);
  }

  public JsonObject toJson() {
    var json = new JsonObject();
    json.put(JsonKeys.X.getKey(), x);
    json.put(JsonKeys.Y.getKey(), y);
    return json;
  }
}
