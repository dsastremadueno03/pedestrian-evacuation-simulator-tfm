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
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.floorField.DijkstraStaticFloorFieldWithMooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.MooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.examples.RandomScenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.examples.Supermarket;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Rectangle;
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
				random.setSeed();
				
				// Scenario from json
				Environment environment = Environment.fromFile(mapPath);
			    Domain domain = environment.getDomain(1);

			    Scenario scenario = new Scenario.FromDomainBuilder(domain)
			        .cellDimension(domain.getWidth() / 110)
			        .floorField(DijkstraStaticFloorFieldWithMooreNeighbourhood::of)
			        .build();

			    // Default Scenarios from classes
			    //var scenario = random.bernoulli(0.75) ? RandomScenario.randomScenario() : Supermarket.supermarket();

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
			    
			    // ONLY FOR DEBUG PURPOSES
			    automaton.runGUI();
			    
			    //automaton.run();
			    
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
			        System.out.println("Aviso: Caesium no pudo procesar las estadísticas nativas.");
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
		}
		catch(Exception e) {
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
