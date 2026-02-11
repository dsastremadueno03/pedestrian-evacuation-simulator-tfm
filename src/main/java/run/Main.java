package run;

import static es.uma.lcc.caesium.statistics.Random.random;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.github.cliftonlabs.json_simple.Jsoner;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.Statistics;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.MooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.examples.RandomScenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.examples.Supermarket;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Rectangle;
import pedestrian.PopulationConfig;
import pedestrian.PopulationGenerator;
import signs.EphimeralVisualSign;
import signs.EvacuationPlanSign;

class Main {
  public static void main(String[] args) {
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
    
    PopulationConfig populationConfig = new PopulationConfig(
    		20, // numCivilians
    		1, //numAttackers
    		1, // numPolice
    		0.1, // probChild
    		0.7, // probAdult
    		0.2 // probElderly
    		);
    
    var pedestrianParametersSupplier =
        new PedestrianParameters.Builder()
            .fieldAttractionBias(random.nextDouble(1.0, 10.0 ))
            .crowdRepulsion(random.nextDouble(0.1, 0.5))
            .velocityPercent(random.nextDouble(0.3, 1.0))
            .build();

    var generator = new PopulationGenerator(automaton.getPedestrianFactory(), automaton);
    
    List<Pedestrian> crowd = generator.generatePopulation(populationConfig, pedestrianParametersSupplier);
    
    int addedCount = 0;
    for(Pedestrian p : crowd) {
    	if(automaton.addPedestrian(p)) {
    		addedCount++;
    	} else {
    		System.out.println("Warning: could not place " + p);
    	}
    }
    System.out.println("Simulation initiated with " + addedCount + " people.");
    
    // ------------------
    
    // Calculate visibility map
    
    automaton.calculateVisibilityMap();
    
    // Calculate distance map
    
    automaton.calculateDistanceMap();
    
    // ------------------
    
    automaton.runGUI(); // automaton.run() to run without GUI
    Statistics statistics = automaton.computeStatistics();
    System.out.println(statistics);

    // write trace to json file
    var trace = automaton.getTrace();
    String fileName = "data/traces/trace.json";
    
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
}