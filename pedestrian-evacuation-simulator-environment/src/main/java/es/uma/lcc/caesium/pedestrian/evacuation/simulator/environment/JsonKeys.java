package es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment;

import com.github.cliftonlabs.json_simple.JsonKey;

/**
 * Keys for the json representation of different objects.
 * @author ppgllrd
 */
enum JsonKeys implements JsonKey {
  ACCESSES("accesses"),
  CENTER("center"),
  DESCRIPTION("description"),
  DOMAIN1("domain1"),
  DOMAIN2("domain2"),
  DOMAINS("domains"),
  HEIGHT("height"),
  GATEWAYS("gateways"),
  ID("id"),
  NAME("name"),
  OBSTACLES("obstacles"),
  POINTS("points"),
  RADIUS("radius"),
  SHAPE("shape"),
  BOTTOMLEFT("bottomLeft"),
  TYPE("type"),
  WIDTH("width"),
  X("x"),
  Y("y");

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
