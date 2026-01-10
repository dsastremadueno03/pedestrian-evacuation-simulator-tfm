package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.trace;

import com.github.cliftonlabs.json_simple.JsonKey;

/**
 * Keys for the json representation of different objects.
 *
 * @author ppgllrd
 */
enum JsonKeys implements JsonKey {
  CELLDIMENSION("cellDimension"),
  COORDINATES("coordinates"),
  CROWD("crowd"),
  DOMAIN("domain"),
  ID("id"),
  LOCATION("location"),
  SNAPSHOTS("snapshots"),
  TIMESTAMP("timestamp"),
  X("X"),
  Y("Y");

  private final String key;

  JsonKeys(String key) {
    this.key = key;
  }

  @Override
  public String getKey() {
    return this.key;
  }

  @Override
  public Object getValue() {
    return "";
  }
}