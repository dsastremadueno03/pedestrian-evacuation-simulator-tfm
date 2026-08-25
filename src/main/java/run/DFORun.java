package run;

import static es.uma.lcc.caesium.statistics.Random.random;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Objects;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import ea.DFOEvaluator;
import ea.EAEvaluator;
import es.uma.lcc.caesium.dfopt.base.DerivativeFreeConfiguration;
import es.uma.lcc.caesium.dfopt.base.DerivativeFreeMethod;
import es.uma.lcc.caesium.dfopt.base.IteratedDerivativeFreeMethod;
import es.uma.lcc.caesium.dfopt.hookejeeves.HookeJeeves;
import es.uma.lcc.caesium.dfopt.hookejeeves.HookeJeevesConfiguration;
import es.uma.lcc.caesium.dfopt.neldermead.NelderMead;
import es.uma.lcc.caesium.dfopt.neldermead.NelderMeadConfiguration;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.util.JsonUtil;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ExitEvacuationProblem;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.configuration.SimulationConfiguration;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Domain;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Environment;
import es.uma.lcc.caesium.statistics.Descriptive;
import es.uma.lcc.caesium.statistics.Random;
import pedestrian.PopulationConfig;

public class DFORun {

	public static void main(String[] args) {
		
		if(args.length < 3) {
			System.out.println("STRUCTURE: <experiment_configuration> <dfo_configuration> <run_id>");
			System.exit(1);
		}
		
		String pathExperiment = args[0];
		String pathDFO = args[1];
		int idRun = Integer.parseInt(args[2]);

		File exp = new File(pathExperiment);
		if(!exp.exists()) {
			System.err.println("ERROR: Cannot find file!");
			System.exit(1);
		}
		
		String algoName = new File(pathDFO).getName().replace(".json", "").toLowerCase();
		String prefix = exp.getName().replace(".json", "");
		String resultsCSV = "data/results/results_" + algoName + "_" + prefix + "_run_" + idRun + ".csv";
			
		try(PrintWriter w = new PrintWriter(new FileWriter(resultsCSV))){
			
			w.println("Experiment id,Run id,Fitness,Civilians Evacuated,Civilians Killed,Civilians Trapped,Attackers Alive,Police Alive,Mean Time,Median Time,Mean Steps,Median Steps,Avg.Dist.Exit,Avg.Dist.Attaker,Avg.Dist.Police");
			
			JsonObject jsonMain;
			try(FileReader r = new FileReader(exp)) {
				jsonMain = (JsonObject) Jsoner.deserialize(r);
			}
			
			JsonArray listOfExperiments = (JsonArray) jsonMain.get("experiments");
			String mapPath = jsonMain.get("map").toString();
			int testSims = JsonUtil.getInt(jsonMain, "testSimulations");
					
			for(var obj : listOfExperiments) {
				JsonObject experiment = (JsonObject) obj;
				int idExperiment = JsonUtil.getInt(experiment, "id");
				
				JsonObject populationJson = (JsonObject) experiment.get("population");
				PopulationConfig populationConfig = ExtraJsonParameterLoader.loadPopulationConfig(populationJson);
				JsonObject weightJson = (JsonObject) experiment.get("weights");
				JsonObject environmentData = (JsonObject) experiment.get("environment");
				int[] environmentNumbers = ExtraJsonParameterLoader.loadEnvironment(environmentData);
				
				SimulationConfiguration simulation = SimulationConfiguration.fromJson(experiment);
				
				random.setSeed(JsonUtil.getInt(experiment, "seed"));
				    
				JsonObject joDFO;
			    try (FileReader reader = new FileReader(pathDFO)) {
			    	joDFO = (JsonObject) Jsoner.deserialize(reader);
			    }
			    
			    DerivativeFreeConfiguration conf;
			    DerivativeFreeMethod solver;
			    if (algoName.contains("neldermead")) {
			    	conf = new NelderMeadConfiguration(joDFO);
			    	solver = new NelderMead((NelderMeadConfiguration) conf);
			    } else {
			    	conf = new HookeJeevesConfiguration(joDFO);
			    	solver = new HookeJeeves((HookeJeevesConfiguration) conf);
			    }
				
				long thisSeed = 50000L + idRun;
			    	
				Environment environment = Environment.fromFile(mapPath);
				Domain domain = environment.getDomain(1);
				ExitEvacuationProblem eep = new ExitEvacuationProblem(environment, environmentNumbers[0], simulation);	
				
			    EAEvaluator evaluator = new EAEvaluator(eep, environmentNumbers[0], environmentNumbers[2], environmentNumbers[1], populationConfig.numPolice(), domain, populationConfig, weightJson, simulation, thisSeed, false);
			    
			    IteratedDerivativeFreeMethod dfoRunner = new IteratedDerivativeFreeMethod(conf, solver);
			    dfoRunner.setVerbosityLevel(0);
			    dfoRunner.setObjectiveFunction(new DFOEvaluator(evaluator));
			    		
			    dfoRunner.run();
			    
			    try {
			    	JsonArray statsJson = (JsonArray) dfoRunner.getStatistics().toJSON();
			    	String statsJsonFile = "data/statistics/" + algoName + "-stats-" + prefix + "_exp_" + idExperiment + "_run_" + idRun + ".json";

			    	try (FileWriter wJson = new FileWriter(statsJsonFile)){
			    		wJson.write(statsJson.toJson());
			    		wJson.flush();
			    	}
			    } catch(Exception e) {
			    	System.err.println("ERROR: Could not export JSON statistics!");
			    }
			    
			    var bestPoint = dfoRunner.getStatistics().getBest(0).point();
			    Individual bestInRun = new Individual();
			    for (int i = 0; i < bestPoint.size(); i++) {
			        bestInRun.getGenome().setGene(i, bestPoint.get(i));
			    }
			    
			    double[] fitnesses = new double[testSims];
			    
			    for(int s = 0; s < testSims; s++) {
			    	long simSeed = Objects.hash(thisSeed, bestInRun.hashCode(), s + 100000);
			    	Random.random.setSeed(simSeed);
			    	EAEvaluator.SimulationResult result = evaluator.getSimulation(bestInRun);
			    	fitnesses[s] = evaluator.getFitness(result.metrics());
			    }
			    
			    double avgFitness = Descriptive.mean(fitnesses);
			    
			    try {
			    	JsonArray testSimsJson = new JsonArray();
			    	for(double f : fitnesses) {
			    		testSimsJson.add(f);
			    	}
			    	
			    	JsonObject testOutJson = new JsonObject();
			    	testOutJson.put("idExperiment", idExperiment);
			    	testOutJson.put("idRun", idRun);
			    	testOutJson.put("avgFitness", avgFitness);
			    	testOutJson.put("testSimulations", testSimsJson);
			    	
			    	String testJsonFile = "data/simulations/test-stats-" + algoName + "_" + prefix + "_exp_" + idExperiment + "_run_" + idRun + ".json";
			    	try (FileWriter wJson = new FileWriter(testJsonFile)){
			    		wJson.write(testOutJson.toJson());
			    		wJson.flush();
			    	}
			    } catch(Exception e) {
			    	System.err.println("ERROR: Could not export test simulations to JSON!");
			    }
			    
			    int repIndex = 0;
			    double minDiff = Double.MAX_VALUE;
			    for(int s = 0; s < testSims; s++) {
			    	double diff = Math.abs(fitnesses[s] - avgFitness);
			    	if(diff < minDiff) {
			    		minDiff = diff;
			    		repIndex = s;
			    	}
			    }
			    
			    long repSeed = Objects.hash(thisSeed, bestInRun.hashCode(), repIndex + 100000);
			    Random.random.setSeed(repSeed);
			    EAEvaluator.SimulationResult repInfo = evaluator.getSimulation(bestInRun);

				EARun.saveNewMetricsJSON(algoName + "_" + prefix, idExperiment, idRun, avgFitness, repInfo);
				EARun.saveRunCSV(w, idExperiment, idRun, avgFitness, repInfo);
				EARun.saveData(w, idExperiment, avgFitness, repInfo, bestInRun, evaluator, algoName + "_" + prefix + "_run_" + idRun);
			}
			
			w.flush();
					
		} catch(Exception e) {
			e.printStackTrace();
		}
				
		System.out.println("--- DFO RUN " + idRun + " FINISHED ---");
	}
}