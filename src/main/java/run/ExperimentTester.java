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

import ea.EAEvaluator;
import ea.EAEvaluator.SimulationResult;
import es.uma.lcc.caesium.ea.base.EvolutionaryAlgorithm;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.config.EAConfiguration;
import es.uma.lcc.caesium.ea.util.JsonUtil;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ExitEvacuationProblem;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.Statistics;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.floorField.DijkstraStaticFloorFieldWithMooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.MooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.examples.RandomScenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.examples.Supermarket;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Rectangle;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.configuration.SimulationConfiguration;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Domain;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Environment;
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
			w.println("Experiment id,Civilians Evacuated,Civilians Killed,Civilians Trapped,Attackers Alive,Police Alive,Mean Time,Median Time,Mean Steps,Median Steps,Avg.Dist.Exit,Avg.Dist.Attaker,Avg.Dist.Police");
			
			// Read JSON
			FileReader r = new FileReader("data/experiments.json");
			JsonObject jsonMain = (JsonObject) Jsoner.deserialize(r);
			JsonArray listOfExperiments = (JsonArray) jsonMain.get("experiments");
			String mapPath = jsonMain.get("map").toString();
			
			// Experiment
			for(var obj : listOfExperiments) {
				JsonObject experiment = (JsonObject) obj;
				int idExperiment = JsonUtil.getInt(experiment, "id");
				
				// Extract values
				JsonObject populationJson = (JsonObject) experiment.get("population");
				PopulationConfig populationConfig = ExtraJsonParameterLoader.loadPopulationConfig(populationJson);
				JsonObject weightJson = (JsonObject) experiment.get("weights");
				JsonObject environmentData = (JsonObject) experiment.get("environment");
				int[] environmentNumbers = ExtraJsonParameterLoader.loadEnvironment(environmentData); // 0 -> nExits, 1 -> permSigns, 2 -> tempSigns
				
				// Initialization
				random.setSeed(JsonUtil.getInt(experiment, "seed"));
				
				// Scenario from json
				Environment environment = Environment.fromFile(mapPath);
			    Domain domain = environment.getDomain(1);
			    SimulationConfiguration simulation = SimulationConfiguration.fromJson(experiment);
			    ExitEvacuationProblem eep = new ExitEvacuationProblem(environment, environmentNumbers[0], simulation);
			    
			    EAEvaluator evaluator = new EAEvaluator(eep, environmentNumbers[0], environmentNumbers[2], environmentNumbers[1], populationConfig.numPolice(), domain, populationConfig, weightJson);
			    
			    FileReader reader = new FileReader("data/numeric.json");
				EAConfiguration conf = new EAConfiguration((JsonObject) Jsoner.deserialize(reader));
			    EvolutionaryAlgorithm ea = new EvolutionaryAlgorithm(conf);
			    ea.setObjectiveFunction(evaluator);
			    
			    // num_runs loop
			    for(int i = 0; i < conf.getNumRuns(); i++) {
			    	long thisSeed = conf.getSeed() + i;
			    	ea.run(thisSeed);
			    }
			    
			    
			    Individual best = ea.getStatistics().getBest();
			    
			    SimulationResult result = evaluator.getSimulation(best);
				
			    saveData(w, idExperiment, evaluator.getAutomatonToSave(), evaluator.getCrowdToSave(), evaluator.getScenarioToSave());
			   
			    
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public static void saveData(PrintWriter w, int idExperiment, SpecificCellularAutomaton automaton, List<Pedestrian> crowd, Scenario scenario) {
		// Fix mean and median if evacuees less than 2
		double meanEvacuationTime = 0;
		double medianEvacuationTime = 0;
		double meanSteps = 0;
		double medianSteps = 0;
		int civEvacuated = automaton.evacuationTimes().length;
		Statistics statistics = null;
		try {
		    if (civEvacuated >= 2) {
		        statistics = automaton.computeStatistics();
		        System.out.println(statistics);
		        meanEvacuationTime = statistics.meanEvacuationTime();
		        medianEvacuationTime = statistics.medianEvacuationTime();
		        meanSteps = statistics.meanSteps();
		        medianSteps = statistics.medianSteps();
		    } else if (civEvacuated == 1) {
		        // If only 1, mean and median are the same
		        meanEvacuationTime = automaton.evacuationTimes()[0];
		        medianEvacuationTime = meanEvacuationTime;
		    }
		} catch (Exception e) {
		    System.out.println("Caesium cannot process native statistics.");
		}
					    
		// Our extended metrics
		int civDead = 0;
		
		// Lists to store Pedestrians for calculating distances for optimization
		List<Civilian> civList = new ArrayList<Civilian>();
		List<Attacker> attList = new ArrayList<Attacker>();
		List<Police> polList = new ArrayList<Police>();
		
		// Count types of pedestrians
		for(Pedestrian p : crowd) {
			if(p instanceof Civilian) {
				if(!((Civilian) p).isAlive()) {
					civDead++;
				}
				else { // Pedestrians alive
					if(!automaton.getScenario().isExit(p.getLocation())){ // Pedestrians trapped
						civList.add((Civilian) p);
					}
				}
			}
			else if (p instanceof Attacker) {
				if(((Attacker) p).isAlive()) {
					attList.add((Attacker) p);
				}
			}
			else if (p instanceof Police) {
				if(((Police) p).isAlive()) {
					polList.add((Police) p);
				}
			}
		}
		
		int attAlive = attList.size();
		int polAlive = polList.size();
		int civTrapped = civList.size();
		
		// Distance calculation for trapped civilians (used in EA)
		double distToExit = 0;
		double distToAtt = 0;
		double distToPol = 0;
		double avgDistToExit = 0;
		double avgDistToAtt = 0;
		double avgDistToPol = 0;
		
		if(!civList.isEmpty()) {
			for(Civilian c : civList) {
				
				// Distance to closest exit
				double minDistToExit = Double.MAX_VALUE;
				for(Rectangle exit : scenario.exits()) {
					int centerRow = exit.bottom() + (exit.height() / 2);
		            int centerCol = exit.left() + (exit.width() / 2);
					double dist = automaton.getDistance(c.getRow(), c.getColumn(), centerRow, centerCol);
					if(dist < minDistToExit) {
						minDistToExit = dist;
					}
				}
				distToExit += minDistToExit;
				
				// Distance to attacker
				if(!attList.isEmpty()) {
				double minDistToAtt = Double.MAX_VALUE;
				for(Attacker att : attList) {
					double dist = automaton.getDistance(c.getLocation(), att.getLocation());
					if(dist < minDistToAtt) {
						minDistToAtt = dist;
					}
				}
				distToAtt += minDistToAtt;
				
				}
				
				// Distance to attacker
				if(!polList.isEmpty()) {
				double minDistToPol = Double.MAX_VALUE;
				for(Police pol : polList) {
					double dist = automaton.getDistance(c.getLocation(), pol.getLocation());
					if(dist < minDistToPol) {
						minDistToPol = dist;
					}
				}
				distToPol += minDistToPol;
				
				}
			}
		}
		
		// Average distances to use in optimization
		if(!civList.isEmpty()) {
		    avgDistToExit = distToExit / civTrapped;
		    avgDistToAtt = distToAtt / civTrapped;
		    avgDistToPol = distToPol / civTrapped;
		}
		
		
		// Write statistics to csv file
		w.println(idExperiment + "," +
				civEvacuated + "," +
				civDead + "," +
				civTrapped + "," +
				attAlive + "," +
				polAlive + "," +
				meanEvacuationTime + "," +
				medianEvacuationTime + "," +
				meanSteps + "," +
				medianSteps + "," +
				avgDistToExit + "," +
				avgDistToAtt + "," +
				avgDistToPol);
		
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
		
		// We identify the roles of each Pedestrian
		JsonObject rolesJson = new JsonObject();

		for (Pedestrian p : crowd) {
		    String rol = "Unknown";
		    if (p instanceof Civilian) {
		        rol = "Civilian";
		    } else if (p instanceof Attacker) {
		        rol = "Attacker";
		    } else if (p instanceof Police) {
		        rol = "Police";
		    }
		    
		    rolesJson.put(String.valueOf(p.getIdentifier()), rol); 
		}

		String rolesFileName = "data/traces/roles_experiment_" + idExperiment + ".json";

		try (FileWriter rolesWriter = new FileWriter(rolesFileName)) {
		    rolesWriter.write(Jsoner.prettyPrint(rolesJson.toJson()));
		    rolesWriter.flush();
		    System.out.printf("Roles written to file %s successfully.%n", rolesFileName);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	// Distancia puertas, atacantes y policias para optimizar civiles
	// Para atacantes optimizar distancia de civiles y numero de civiles derribados????
	// Para policias optimizar distancia civiles y numero de atacantes derribados???
	
	// TODO: QUITAR ESTAS FUNCIONES DE AQUÍ
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

	  /**
	   * Generates the parameter list for Pedestrians
	   * @param paramJson file with data
	   * @param PedestrianType adjust parameters to the selected type (0 -> Civ, 1 -> Att, 2 -> Pol)
	   * @return builder of parameters
	   */
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
