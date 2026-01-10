package es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * This class allows handling the basic information of the physical environment
 * to be simulated.
 *
 * @author ccottap, ppgllrd
 */
public class Environment {
  /**
   * Domains in the environment.
   */
  protected Map<Integer, Domain> domains;

  /**
   * Gateways in the environment.
   */
  protected Map<Integer, Gateway> gateways;

  /**
   * Basic constructor. Creates an environment with no domains or gateways.
   */
  public Environment() {
    domains = new HashMap<>();
    gateways = new HashMap<>();
  }

  /** Constructs an environment with the given domains and gateways.
   * @param json The json object to be parsed.
   * @return an environment with the given domains and gateways.
   */
  public static Environment fromJson(JsonObject json) {
    Environment environment = new Environment();
    JsonArray domains = json.getCollection(JsonKeys.DOMAINS);
    for (Object object : domains) {
      Domain domain = Domain.fromJson((JsonObject) object);
      environment.addDomain(domain);
    }
    JsonArray gateways = json.getCollection(JsonKeys.GATEWAYS);
    for (Object object : gateways) {
      Gateway gateway = Gateway.fromJson((JsonObject) object);
      if (!environment.addGateway(gateway)) {
        throw new UnsupportedOperationException(String.format("Environment.fromJson: gateway %d could not be added",
            gateway.id()));
      }
    }
    return environment;
  }

  /**
   * @param file the file with json contents to be parsed.
   * @return an environment from the provided file.
   * @throws IOException
   * @throws JsonException
   */
  public static Environment fromFile(File file) throws IOException, JsonException {
    FileReader reader = new FileReader(file);
    JsonObject json = (JsonObject) Jsoner.deserialize(reader);
    reader.close();
    return Environment.fromJson(json);
  }

  /**
   * @param filename the name of the file with json contents to be parsed.
   * @return an environment from the provided file.
   * @throws IOException
   * @throws JsonException
   */
  public static Environment fromFile(String filename) throws IOException, JsonException {
    return fromFile(new File(filename));
  }

  /**
   * Adds a domain to the environment. Note that it is not possible to
   * add a domain whose id is 0, since this is assumed to represent
   * the safety domain.
   *
   * @param domain the domain to be added.
   */
  public void addDomain(Domain domain) {
    int id = domain.id();
    if (id == 0) {
      throw new UnsupportedOperationException(String.format("Environment.addDomain: domain %d could not be added",  id));
    }
    domains.put(id, domain);
  }

  /**
   * Adds a gateway to the environment. The domains must already exist in the environment, otherwise nothing is done.
   *
   * @param gateway the gateway to be added.
   * @return true iff the gateway could be added.
   */
  public boolean addGateway(Gateway gateway) {
    if ((gateway.domain1() != gateway.domain2()) && ((gateway.domain1() == 0)
        || domains.containsKey(gateway.domain1())) && ((gateway.domain2() == 0)
        || domains.containsKey(gateway.domain2()))) {
      gateways.put(gateway.id(), gateway);
      return true;
    }
    return false;
  }

  /**
   * Gets a specific domain.
   *
   * @param id the id of the domain sought.
   * @return the domain whose id is indicated (null if it does not exist).
   */
  public Domain getDomain(int id) {
    return domains.get(id);
  }

  /**
   * Returns a set with the ids of all domains in the environment.
   *
   * @return a set with the ids of all domains in the environment.
   */
  public Set<Integer> getDomainsIDs() {
    return domains.keySet();
  }

  /**
   * Gets a specific gateway.
   *
   * @param id the id of the gateway sought.
   * @return the gateway whose id is indicated (null if it does not exist)..
   */
  public Gateway getGateway(int id) {
    return gateways.get(id);
  }

  /**
   * Returns a set with the ids of all gateways in the environment.
   *
   * @return a set with the ids of all gateways in the environment.
   */
  public Set<Integer> getGatewayIDs() {
    return domains.keySet();
  }

  /**
   * Constructs a json object with the environment information.
   * @return a json object with the environment information.
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    JsonArray domains = new JsonArray();
    for (Domain domain : this.domains.values()) {
      domains.add(domain.toJson());
    }
    json.put(JsonKeys.DOMAINS, domains);
    JsonArray gateways = new JsonArray();
    for (Gateway gateway : this.gateways.values()) {
      gateways.add(gateway.toJson());
    }
    json.put(JsonKeys.GATEWAYS, gateways);
    return json;
  }

  /**
   * Returns a string with the json serialization of the environment.
   * @return a string with the json serialization of the environment.
   */
  String jsonSerialized() {
    return Jsoner.serialize(toJson());
  }


  /**
   * Returns a string with the json serialization of the environment, pretty printed.
   * @param indentation the indentation to be used.
   * @return a string with the json serialization of the environment, pretty printed.
   */
   private String jsonPrettyPrinted(String indentation) {
    String printable = jsonSerialized();
    StringWriter writer = new StringWriter();

    try {
      Jsoner.prettyPrint(new StringReader(printable), writer, indentation, "\n");
    } catch (IOException | JsonException ioException) {
    }

    return writer.toString();
  }

  /**
   * Returns a string with the json serialization of the environment, pretty printed.
   * @return a string with the json serialization of the environment, pretty printed.
   */
  public String jsonPrettyPrinted() {
    return jsonPrettyPrinted("  ");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Environment that = (Environment) o;
    return Objects.equals(domains, that.domains) && Objects.equals(gateways, that.gateways);
  }

  @Override
  public int hashCode() {
    return Objects.hash(domains, gateways);
  }
}