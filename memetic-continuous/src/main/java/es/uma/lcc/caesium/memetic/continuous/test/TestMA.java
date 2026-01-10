package es.uma.lcc.caesium.memetic.continuous.test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Locale;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import es.uma.lcc.caesium.ea.base.EvolutionaryAlgorithm;
import es.uma.lcc.caesium.ea.config.EAConfiguration;
import es.uma.lcc.caesium.ea.fitness.ObjectiveFunction;
import es.uma.lcc.caesium.ea.problem.continuous.Sphere;
import es.uma.lcc.caesium.ea.problem.rastrigin.Rastrigin;
import es.uma.lcc.caesium.ea.statistics.VarianceDiversity;
import es.uma.lcc.caesium.memetic.continuous.localsearch.MemeticContinuousVariationFactory;

/**
 * Class for testing the memetic algorithm on continuous spaces
 * @author ccottap
 * @version 1.0
 */
public class TestMA {

	/**
	 * Main method
	 * @param args command-line arguments
	 * @throws FileNotFoundException if configuration file cannot be read 
	 * @throws JsonException if the configuration file is not correctly formatted
	 */
	public static void main(String[] args) throws FileNotFoundException, JsonException {
		if (args.length < 2) {
			System.out.println("<problem> <configuration>");
			System.exit(1);
		}
		String problem = args[0]; 
		FileReader reader = new FileReader(args[1]);
		EAConfiguration conf = new EAConfiguration((JsonObject) Jsoner.deserialize(reader));

		conf.setVariationFactory(new MemeticContinuousVariationFactory());
		int numruns = conf.getNumRuns();
		System.out.println(conf);
		EvolutionaryAlgorithm myEA = new EvolutionaryAlgorithm(conf);
		myEA.setObjectiveFunction(create(problem));
		myEA.getStatistics().setDiversityMeasure(new VarianceDiversity());
		myEA.setVerbosityLevel(0);
		for (int i=0; i<numruns; i++) {
			myEA.run();
			System.out.println ("Run " + i + ": " + 
								String.format(Locale.US, "%.2f", myEA.getStatistics().getTime(i)) + "s\t" +
								myEA.getStatistics().getBest(i).getFitness());
		}
		PrintWriter file = new PrintWriter("stats.json");
		file.print(myEA.getStatistics().toJSON().toJson());
		file.close();
	}
	
	
	/**
	 * Creates an objective function with some default parameters
	 * @param problem name of the objective function
	 * @return the objective function indicated
	 */
	private static ObjectiveFunction create(String problem) {
		switch (problem.toUpperCase()) {
		case "RASTRIGIN": 
			return new Rastrigin(25, 5.12);

		case "SPHERE":
			return new Sphere(100, 5.12);
			
		default:
			return null;
		}
	}
	

}
