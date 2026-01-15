package pedestrian;

public record PopulationConfig(int numCivilians, int numAttackers, int numPolice, double probChild, double probAdult,
		double probElderly) {

	public PopulationConfig {
		if (numCivilians < 0 || numAttackers < 0 || numPolice < 0) {
			throw new IllegalArgumentException("El número de agentes no puede ser negativo.");
		}

		double sum = probChild + probAdult + probElderly;
		if (Math.abs(sum - 1) > 0.001) {
			System.err.println("Aviso: Las probabilidades de edades no suman 1,0.");
		}
	}
}


