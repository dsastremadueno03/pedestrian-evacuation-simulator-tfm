package run;

import static es.uma.lcc.caesium.statistics.Random.random;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import ea.EAEvaluator;
import ea.EAEvaluator.DecodedDesign;
import es.uma.lcc.caesium.ea.base.EvolutionaryAlgorithm;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.config.EAConfiguration;
import es.uma.lcc.caesium.ea.util.JsonUtil;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ExitEvacuationProblem;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Rectangle;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.configuration.SimulationConfiguration;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Access;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Domain;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Environment;
import pedestrian.Attacker;
import pedestrian.Civilian;
import pedestrian.Police;
import pedestrian.PopulationConfig;
import signs.EphimeralVisualSign;
import signs.EvacuationPlanSign;

public class ExperimentTester {

	public static void main(String[] args) {
		
		// Call structure
		if(args.length < 3) {
			System.out.println("STRUCTURE: <experiment_configuration> <ea_configuration> <run_id>");
			System.exit(1);
		}
		
		String pathExperiment = args[0];
		String pathEA = args[1];
		int idRun = Integer.parseInt(args[2]);

		System.out.println("STARTING...");
		
		File exp = new File(pathExperiment);
		if(!exp.exists()) {
			System.err.println("ERROR: Cannot find file!");
		}
		
		String prefix = exp.getName().replace(".json", "");
		String resultsCSV = "data/results/results_" + prefix + "_run_" + idRun + ".csv";
		System.out.println("Running " + prefix);
			
			
			try(PrintWriter w = new PrintWriter(new FileWriter(resultsCSV))){
			
			// Header
			w.println("Experiment id,Run id,Fitness,Civilians Evacuated,Civilians Killed,Civilians Trapped,Attackers Alive,Police Alive,Mean Time,Median Time,Mean Steps,Median Steps,Avg.Dist.Exit,Avg.Dist.Attaker,Avg.Dist.Police");
			
			JsonObject jsonMain;
			try(FileReader r = new FileReader(exp)) {
				jsonMain = (JsonObject) Jsoner.deserialize(r);
			}
			
			JsonArray listOfExperiments = (JsonArray) jsonMain.get("experiments");
			String mapPath = jsonMain.get("map").toString();
					
			// Read JSON
			for(var obj : listOfExperiments) {
				JsonObject experiment = (JsonObject) obj;
				int idExperiment = JsonUtil.getInt(experiment, "id");
				
				// Extract values
				JsonObject populationJson = (JsonObject) experiment.get("population");
				PopulationConfig populationConfig = ExtraJsonParameterLoader.loadPopulationConfig(populationJson);
				JsonObject weightJson = (JsonObject) experiment.get("weights");
				JsonObject environmentData = (JsonObject) experiment.get("environment");
				int[] environmentNumbers = ExtraJsonParameterLoader.loadEnvironment(environmentData); // 0 -> nExits, 1 -> permSigns, 2 -> tempSigns
				
				SimulationConfiguration simulation = SimulationConfiguration.fromJson(experiment);
				
				// Initialization
				random.setSeed(JsonUtil.getInt(experiment, "seed"));
				    
			    FileReader reader = new FileReader(pathEA);
				EAConfiguration conf = new EAConfiguration((JsonObject) Jsoner.deserialize(reader));
				
				// PARALLELISM
				long seed = conf.getSeed();
				long thisSeed = seed + idRun;
			    	
			    // Scenario from json
				Environment environment = Environment.fromFile(mapPath);
				Domain domain = environment.getDomain(1);
				ExitEvacuationProblem eep = new ExitEvacuationProblem(environment, environmentNumbers[0], simulation);	
			    EAEvaluator evaluator = new EAEvaluator(eep, environmentNumbers[0], environmentNumbers[2], environmentNumbers[1], populationConfig.numPolice(), domain, populationConfig, weightJson, simulation);
			    EvolutionaryAlgorithm ea = new EvolutionaryAlgorithm(conf);
			    ea.setObjectiveFunction(evaluator);
			    		
			    ea.run(thisSeed);
			    		
			    Individual bestInRun = ea.getStatistics().getBest(0); // There is only 1 run in a thread
				EAEvaluator.SimulationResult infoInRun = evaluator.getSimulation(bestInRun);
				double fitness = evaluator.getFitness(infoInRun.metrics());

			  
				saveRunCSV(w, idExperiment, idRun, fitness, infoInRun);
			    		
			    saveData(w, idExperiment, fitness, infoInRun, bestInRun, evaluator, prefix + "_run_" + idRun);
			    
			   
			}
			
			w.flush();
					
		} catch(Exception e) {
			System.err.println("ERROR: configuration file " + exp.getName() + " had a problem while running!");
			e.printStackTrace();
		}
				
		System.out.println("---		FINISHED	---");
	}
	
	/**
	 * Saves data of the run
	 * @param w file to write in
	 * @param idExperiment current experiment
	 * @param idRun current run
	 * @param fitness fitness of the best individual of current run
	 * @param info simulation data
	 */
	public static void saveRunCSV(PrintWriter w, int idExperiment, int idRun, double fitness, EAEvaluator.SimulationResult info) {
		EAEvaluator.SimulationMetrics m = info.metrics();
		
		w.println(idExperiment + "," +
				idRun + "," +
				fitness + "," +
				m.civEvacuated() + "," +
				m.civDead() + "," +
				m.civTrapped() + "," +
				m.attAlive() + "," +
				m.polAlive() + "," +
				m.meanEvacuationTime() + "," +
				m.medianEvacuationTime() + "," +
				m.meanSteps() + "," +
				m.medianSteps() + "," +
				m.avgDistToExit() + "," +
				m.avgDistToAtt() + "," +
				m.avgDistToPol());
		
		w.flush();
	}

	public static void saveData(PrintWriter w, int idExperiment, double fitness, EAEvaluator.SimulationResult info, Individual ind, EAEvaluator evaluator, String prefix) {
		SpecificCellularAutomaton automaton = info.automaton();
		EAEvaluator.SimulationMetrics m = info.metrics();
		List<Pedestrian> crowd = info.crowd();
		
		/*
		// Write statistics to csv file
		w.println(idExperiment + "," +
				"BEST," +
				fitness + "," +
				m.civEvacuated() + "," +
				m.civDead() + "," +
				m.civTrapped() + "," +
				m.attAlive() + "," +
				m.polAlive() + "," +
				m.meanEvacuationTime() + "," +
				m.medianEvacuationTime() + "," +
				m.meanSteps() + "," +
				m.medianSteps() + "," +
				m.avgDistToExit() + "," +
				m.avgDistToAtt() + "," +
				m.avgDistToPol());
		
		w.flush();
		*/

		// Write trace to json file
		var trace = automaton.getTrace();
		String fileName = "data/traces/trace_" + prefix + "_experiment_" + idExperiment + ".json";
		
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

		String rolesFileName = "data/traces/roles_" + prefix + "_experiment_" + idExperiment + ".json";

		try (FileWriter rolesWriter = new FileWriter(rolesFileName)) {
		    rolesWriter.write(Jsoner.prettyPrint(rolesJson.toJson()));
		    rolesWriter.flush();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		// COORDINATES
		
		if(ind != null && evaluator != null) {
			DecodedDesign bestDesign = evaluator.decode(ind);
			
			JsonObject coorJson = new JsonObject();
			coorJson.put("experiment_id", idExperiment);
			coorJson.put("fitness", fitness);
			coorJson.put("total_penalty", bestDesign.repairPenalty());
			
			// EXITS
			JsonArray exitsArray = new JsonArray();
			for(Access exit : bestDesign.trialExits()) {
				var bounds = exit.getShape().getAWTShape().getBounds2D();
				JsonObject coor = new JsonObject();
				coor.put("X", bounds.getCenterX());
				coor.put("Y", bounds.getCenterY());
				exitsArray.add(coor);
			}
			coorJson.put("exits", exitsArray);
			
			// TEMPORAL SIGNALS
			JsonArray tempSignalArray = new JsonArray();
			for(EphimeralVisualSign s : bestDesign.trialTempVisSigns()) {
				JsonObject coor = new JsonObject();
				// Convert coordinates to meters (getting middle point of cell)
				double x = (s.getLocation().column() * evaluator.getCellDimension()) + (evaluator.getCellDimension() / 2.0);
				double y = (s.getLocation().row() * evaluator.getCellDimension()) + (evaluator.getCellDimension() / 2.0);
				coor.put("X", x);
				coor.put("Y", y);
				tempSignalArray.add(coor);
			}
			coorJson.put("temporal_signals", tempSignalArray);
			
			// PERMANENT SIGNALS
			JsonArray permSignalArray = new JsonArray();
			for(EvacuationPlanSign s : bestDesign.trialPermVisSigns()) {
				JsonObject coor = new JsonObject();
				// Convert coordinates to meters (getting middle point of cell)
				double x = (s.getLocation().column() * evaluator.getCellDimension()) + (evaluator.getCellDimension() / 2.0);
				double y = (s.getLocation().row() * evaluator.getCellDimension()) + (evaluator.getCellDimension() / 2.0);
				coor.put("X", x);
				coor.put("Y", y);
				permSignalArray.add(coor);
			}
			coorJson.put("permanent_signals", permSignalArray);
			
			// POLICE
			JsonArray policeArray = new JsonArray();
			for(int[] p : bestDesign.trialPolice()) {
				JsonObject coor = new JsonObject();
				// Convert coordinates to meters (getting middle point of cell)
				double x = (p[1] * evaluator.getCellDimension()) + (evaluator.getCellDimension() / 2.0);
				double y = (p[0] * evaluator.getCellDimension()) + (evaluator.getCellDimension() / 2.0);
				coor.put("X", x);
				coor.put("Y", y);
				policeArray.add(coor);
			}
			coorJson.put("police", policeArray);
		
		
			// Coordinates stored
			String coorFileName = "data/traces/coordinates_" + prefix + "_experiment_" + idExperiment + ".json";
			try (FileWriter coorWriter = new FileWriter(coorFileName)) {
				coorWriter.write(Jsoner.prettyPrint(coorJson.toJson()));
				coorWriter.flush();
			} catch (IOException e) {
			    e.printStackTrace();
			}
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
		//System.out.println("Generating permanent signs on exits...");
	    for(Rectangle exit : scenario.exits()) {
	    	int centerRow = exit.bottom() + (exit.height() / 2);
	    	int centerCol = exit.left() + (exit.width() / 2);
	    	EvacuationPlanSign exitSign = new EvacuationPlanSign(centerRow, centerCol);
	    	automaton.addSign(exitSign);
	    	System.out.println("Exit Sign generated at (" + centerRow + ", " + centerCol + ").");
	    }
	  }
	  
}
