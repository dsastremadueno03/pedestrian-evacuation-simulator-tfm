package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.trace;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * A class to represent trace corresponding to pedestrian's movements in a simulation.
 *
 * @author Pepe Gallardo
 */
public record Trace(double cellDimension, Snapshot[] snapshots) {
  public static Trace fromJson(JsonObject json) {
    var cellDimension = json.getDouble(JsonKeys.CELLDIMENSION);
    JsonArray objects = json.getCollection(JsonKeys.SNAPSHOTS);
    var snapshots = new Snapshot[objects.size()];
    var i = 0;
    for (Object object : objects) {
      snapshots[i++] = Snapshot.fromJson((JsonObject) object);
    }
    return new Trace(cellDimension, snapshots);
  }

  public JsonObject toJson() {
    var json = new JsonObject();
    json.put(JsonKeys.CELLDIMENSION.getKey(), cellDimension);
    var snapshotsJson = new JsonArray();
    for (var snapshot : snapshots) {
      snapshotsJson.add(snapshot.toJson());
    }
    json.put(JsonKeys.SNAPSHOTS.getKey(), snapshotsJson);
    return json;
  }

  public static Trace fromFile(File file) throws IOException, JsonException {
    var reader = new FileReader(file);
    JsonObject json = (JsonObject) Jsoner.deserialize(reader);
    reader.close();
    return Trace.fromJson(json);
  }

  public static Trace fromFile(String filename) throws IOException, JsonException {
    return fromFile(new File(filename));
  }
}
