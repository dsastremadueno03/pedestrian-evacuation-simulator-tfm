package run;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;

import es.uma.lcc.caesium.ea.util.JsonUtil;
import es.uma.lcc.caesium.statistics.Random;
import pedestrian.PopulationConfig;

public class ExtraJsonParameterLoader {
	
	public static final int PEDESTRIAN_TYPES = 3;
	public static final int ENVIRONMENT_COMPONENTS = 3; // Temporal signs, permanent signs, exits

	/**
	 * Reads the JSON object's minimum and maximum.
	 * @param rangesJson Object to be read
	 * @return Random value from the range set
	 */
	public static double readValues(JsonObject rangesJson) {
		if(rangesJson == null) {
			return 0.0;
		}
		
		// JSON is in BigDecimal
		double min = JsonUtil.getDouble(rangesJson, "min");
		double max = JsonUtil.getDouble(rangesJson, "max");
	
		if(min == max) {
			return max;
		}
		
		return Random.random.nextDouble(min, max);
	}
	
	public static int[] loadEnvironment(JsonObject environment) {
		if(environment == null) {
			System.err.println("Error en el JsonArray!");
			return null;
		}
		
		int[] env = new int[ENVIRONMENT_COMPONENTS];
		
		env[0] = JsonUtil.getInt(environment, "nExits"); // Check whether adding an extra exit is worth it or not
		env[1] = JsonUtil.getInt(environment, "nPermSigns");
		env[2] = JsonUtil.getInt(environment, "nTempSigns");
		
		return env;
	}
	
	/**
	 * Organizes read data in a social weight matrix.
	 * @param matrix data from JSON
	 * @return matrix with social weights
	 */
	public static double[][] loadSocialWeightsMatrix(JsonArray matrix){
		if(matrix == null || matrix.size() < PEDESTRIAN_TYPES) {
			System.err.println("Error en el JsonArray!");
			return null;
		}
		
		double[][] M = new double[PEDESTRIAN_TYPES][PEDESTRIAN_TYPES];
		
		for(int i = 0; i < PEDESTRIAN_TYPES; i++) {
			JsonArray row = (JsonArray) matrix.get(i);
			if(row.size() < PEDESTRIAN_TYPES) {
				System.err.println("Error en el JsonArray!");
				return null;
			}
			for(int j = 0; j < PEDESTRIAN_TYPES; j++) {
				M[i][j] = readValues((JsonObject) row.get(j));
			}
		}
		return M;
	}
	
	/**
	 * Converts the read data to the population configuration data structure.
	 * @param population Data regarding population configuration as JsonObject
	 * @return Population configuration loaded
	 */
	public static PopulationConfig loadPopulationConfig(JsonObject population) {
		if(population == null) {
			System.err.println("Error en el JsonArray!");
			return null;
		}
		
		int civ = JsonUtil.getInt(population, "numCivilians");
		int att = JsonUtil.getInt(population, "numAttackers");
		int pol = JsonUtil.getInt(population, "numPolice");
		
		double pChild = JsonUtil.getDouble(population, "pChild");
		double pAdult = JsonUtil.getDouble(population, "pAdult");
		double pElder = JsonUtil.getDouble(population, "pElder");
		
		if(pChild + pAdult + pElder != 1.0) {
			System.err.println("Error en el JsonArray!");
		}
		
		return new PopulationConfig(civ, att, pol, pChild, pAdult, pElder);
	}
}
