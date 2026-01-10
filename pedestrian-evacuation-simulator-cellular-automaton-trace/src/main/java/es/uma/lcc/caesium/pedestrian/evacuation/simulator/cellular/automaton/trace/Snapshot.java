package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.trace;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;

/**
 * A class to represent a snapshot of a crowd of pedestrians in a trace.
 *
 * @author Pepe Gallardo
 */
public record Snapshot(double timestamp, Pedestrian[] crowd) {
  public static Snapshot fromJson(JsonObject json) {
    var timestamp = json.getDouble(JsonKeys.TIMESTAMP);
    JsonArray objects = json.getCollection(JsonKeys.CROWD);
    var crowd = new Pedestrian[objects.size()];
    var i = 0;
    for (var object : objects) {
      crowd[i++] = Pedestrian.fromJson((JsonObject) object);
    }
    return new Snapshot(timestamp, crowd);
  }

  public JsonObject toJson() {
    var json = new JsonObject();
    json.put(JsonKeys.TIMESTAMP.getKey(), timestamp);
    var crowdJson = new JsonArray();
    for (var pedestrian : crowd) {
      crowdJson.add(pedestrian.toJson());
    }
    json.put(JsonKeys.CROWD.getKey(), crowdJson);
    return json;
  }
}
