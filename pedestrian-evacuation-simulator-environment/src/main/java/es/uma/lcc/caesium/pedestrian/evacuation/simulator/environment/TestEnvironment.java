package es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

/**
 * Class to test the classes modeling the environment.
 *
 * @author ccottap, ppgllrd.
 */
public class TestEnvironment {

  /**
   * Main method
   *
   * @param args command-line arguments (name of JSON file).
   * @throws FileNotFoundException if JSON file does  not exist.
   * @throws JsonException         if there is a problem reading the JSON file.
   */
  public static void main(String[] args) throws IOException, JsonException {
    String defaultFilename = Objects.requireNonNull(TestEnvironment.class.getResource("/environment-example.json")).getFile();
    String filename = (args.length == 0) ? defaultFilename : args[0];

    Environment environment = Environment.fromFile(filename);
    JsonObject json = environment.toJson();
    System.out.println(json);
    System.out.println(environment.jsonSerialized());
    System.out.println(environment.jsonPrettyPrinted());
  }
}
