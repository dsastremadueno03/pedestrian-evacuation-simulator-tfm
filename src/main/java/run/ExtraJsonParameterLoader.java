package run;

import java.math.BigDecimal;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;

import es.uma.lcc.caesium.statistics.Random;

public class ExtraJsonParameterLoader {
	
	public static final int PEDESTRIAN_TYPES = 3;

	/**
	 * Reads the JSON object's minimum and maximum.
	 * @param rangesJson Object to be read
	 * @return Random value from the range set
	 */
	public static double readValues(JsonObject rangesJson) {
		if(rangesJson == null) {
			return 0.0;
		}
		
		double min = ((BigDecimal)rangesJson.get("min")).doubleValue();
		double max = ((BigDecimal)rangesJson.get("max")).doubleValue();
	
		if(min == max) {
			return max;
		}
		
		return Random.random.nextDouble(min, max);
	}
	
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
}
