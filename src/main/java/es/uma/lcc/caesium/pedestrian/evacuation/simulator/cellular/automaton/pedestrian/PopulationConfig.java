package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.pedestrian;

public record PopulationConfig(int numCivilians, int numAttackers, int numPolice, double probChild, double probAdult,
		double probElderly) {

	public PopulationConfig {
		if (numCivilians < 0 || numAttackers < 0 || numPolice < 0) {
			throw new IllegalArgumentException("Agent number cannot be negative.");
		}

		double sum = probChild + probAdult + probElderly;
		if (Math.abs(sum - 1) > 0.001) {
			System.err.println("Age probabilities do not sum up to 1!");
		}
	}
}


