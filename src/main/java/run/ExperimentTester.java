package run;

import static es.uma.lcc.caesium.statistics.Random.random;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import es.uma.lcc.caesium.ea.util.JsonUtil;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.Statistics;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.MooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.examples.RandomScenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.examples.Supermarket;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Rectangle;
import pedestrian.Attacker;
import pedestrian.Civilian;
import pedestrian.MultiPedestrianFactory;
import pedestrian.PedestrianWithVisionParameters;
import pedestrian.Police;
import pedestrian.PopulationConfig;
import pedestrian.PopulationGenerator;
import signs.EphimeralVisualSign;
import signs.EvacuationPlanSign;

public class ExperimentTester {

	public static void main(String[] args) {
		
		String resultsCSV = "results.csv";
		
		try(PrintWriter w = new PrintWriter(new FileWriter(resultsCSV))){
			// Header
			w.println("Experiment id,Civilians Evacuated,Civilians Killed,Civilians Trapped,Attackers Alive,Police Alive,Mean Time,Median Time,Mean Steps,Median Steps");
			
			// Read JSON
			FileReader r = new FileReader("data/experiments.json");
			JsonObject jsonMain = (JsonObject) Jsoner.deserialize(r);
			JsonArray listOfExperiments = (JsonArray) jsonMain.get("experiments");
			
			for(var obj : listOfExperiments) {
				JsonObject experiment = (JsonObject) obj;
				int idExperiment = JsonUtil.getInt(experiment, "id");
				
				// Extract values
				JsonObject populationJson = (JsonObject) experiment.get("population");
				PopulationConfig populationConfig = ExtraJsonParameterLoader.loadPopulationConfig(populationJson);
				JsonObject weightJson = (JsonObject) experiment.get("weights");
				
				// Initialization
				random.setSeed();

			    var scenario = random.bernoulli(0.75) ? RandomScenario.randomScenario() : Supermarket.supermarket();

			    var cellularAutomatonParameters =
			        new CellularAutomatonParameters.Builder()
			            .scenario(scenario) // use this scenario
			            .timeLimit(10 * 60) // 10 minutes is time limit for simulation
			            .neighbourhood(MooreNeighbourhood::of) // use Moore's Neighbourhood for automaton
			            .pedestrianReferenceVelocity(1.3) // fastest pedestrians walk at 1.3 m/s
			            .GUITimeFactor(8) // perform GUI animation x8 times faster than real time
			            .build();

			    var automaton = new SpecificCellularAutomaton(cellularAutomatonParameters);
			    
			    // Generate signs on exits
			    
			    GenerateSignOnExits(scenario, automaton);
			    
			    // Place signs
			    
			    EphimeralVisualSign sign = new EphimeralVisualSign(3,3);
			    automaton.addSign(sign);
			    
			    // Place pedestrians
			    
			    System.out.println("Placing entities...");
			    
			    List<PedestrianWithVisionParameters> civParams = new ArrayList<>();
			    for (int i = 0; i < populationConfig.numCivilians(); i++) {
			        civParams.add(buildParams(weightJson, 0));
			    }

			    List<PedestrianWithVisionParameters> attParams = new ArrayList<>();
			    for (int i = 0; i < populationConfig.numAttackers(); i++) {
			        attParams.add(buildParams(weightJson, 1));
			    }

			    List<PedestrianWithVisionParameters> polParams = new ArrayList<>();
			    for (int i = 0; i < populationConfig.numPolice(); i++) {
			        polParams.add(buildParams(weightJson, 2));
			    }
			    
			    // Create factory and set generator
			    MultiPedestrianFactory factory = automaton.getPedestrianFactory();
			    PopulationGenerator generator = new PopulationGenerator(factory, automaton);
				
			    // Set in automaton
			    List<Pedestrian> crowd = generator.generatePopulation(populationConfig, civParams, attParams, polParams);
			    int addedCount = 0;
			    for(Pedestrian p : crowd) {
			        if(automaton.addPedestrian(p)) addedCount++;
			    }
			    System.out.println("Simulation initiated with " + addedCount + " people.");
			    
			    automaton.calculateVisibilityMap();
			    automaton.calculateDistanceMap();

			    System.out.println("Simulating...");
			    automaton.run();
			    
			    // Collect metrics
			    Statistics statistics = automaton.computeStatistics();
			    System.out.println(statistics);
			    
			    // Our extended metrics
			    int civDead = 0;
			    int civEvacuated = statistics.numberOfEvacuees();
			    int civTrapped = populationConfig.numCivilians() - civEvacuated - civDead;
			    int attAlive = 0;
			    int polAlive = 0;
			    
			    for(Pedestrian p : crowd) {
			    	if(p instanceof Civilian) {
			    		if(!((Civilian) p).isAlive()) {
			    			civDead++;
			    		}
			    	}
			    	else if (p instanceof Attacker) {
			    		if(((Attacker) p).isAlive()) {
			    			attAlive++;
			    		}
			    	}
			    	else if (p instanceof Police) {
			    		if(((Police) p).isAlive()) {
			    			polAlive++;
			    		}
			    	}
			    }
			    
			    // Write statistics to csv file
			    w.println(idExperiment + "," +
			    		civEvacuated + "," +
			    		civDead + "," +
			    		civTrapped + "," +
			    		attAlive + "," +
			    		polAlive + "," +
			    		statistics.meanEvacuationTime() + "," +
			    		statistics.medianEvacuationTime() + "," +
			    		statistics.meanSteps() + "," +
			    		statistics.medianSteps());
			    
			    w.flush();

			    // Write trace to json file
			    var trace = automaton.getTrace();
			    String fileName = "data/traces/trace_experiment_" + idExperiment + ".json";
			    
			    File file = new File(fileName);
			    if (file.getParentFile() != null) {
			    	file.getParentFile().mkdirs();
			    }
			    
			    try (FileWriter fileWriter = new FileWriter(fileName)) {
			      fileWriter.write(Jsoner.prettyPrint(trace.toJson().toJson()));
			      fileWriter.flush();
			      System.out.printf("Trace written to file %s successfully.%n", fileName);
			    } catch (IOException e) {
			      e.printStackTrace();
			    }
			    
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	   * Creates a permanent sign on the center of the exit rectangle, 
	   * so that pedestrians are attracted.
	   * 
	   * @param scenario scenario built
	   * @param automaton automaton used
	   */
	  public static void GenerateSignOnExits(Scenario scenario, SpecificCellularAutomaton automaton) {
		System.out.println("Generating permanent signs on exits...");
	    for(Rectangle exit : scenario.exits()) {
	    	int centerRow = exit.bottom() + (exit.height() / 2);
	    	int centerCol = exit.left() + (exit.width() / 2);
	    	EvacuationPlanSign exitSign = new EvacuationPlanSign(centerRow, centerCol);
	    	automaton.addSign(exitSign);
	    	System.out.println("Exit Sign generated at (" + centerRow + ", " + centerCol + ").");
	    }
	  }

	  private static PedestrianWithVisionParameters buildParams(JsonObject paramJson, int PedestrianType) {
	        double[][] matrix = ExtraJsonParameterLoader.loadSocialWeightsMatrix((JsonArray) paramJson.get("socialMatrix"));
	        double civilWeight = matrix[PedestrianType][0];
	        double attackerWeight = matrix[PedestrianType][1];
	        double policeWeight = matrix[PedestrianType][2];

	        double vision = ExtraJsonParameterLoader.readValues((JsonObject) paramJson.get("visionRadius"));
	        double attack = ExtraJsonParameterLoader.readValues((JsonObject) paramJson.get("attackRadius"));
	        double greedy = ExtraJsonParameterLoader.readValues((JsonObject) paramJson.get("greedyProb"));
	        double inertia = ExtraJsonParameterLoader.readValues((JsonObject) paramJson.get("inertiaWeight"));

	        return new PedestrianWithVisionParameters.Builder()
	            .civilianWeight(civilWeight)
	            .attackerWeight(attackerWeight)
	            .policeWeight(policeWeight)
	            .visionRadius(vision)
	            .attackRadius(attack)
	            .greedyProb(greedy)
	            .inertiaWeight(inertia)
	            .build();
	    }
	  
}
