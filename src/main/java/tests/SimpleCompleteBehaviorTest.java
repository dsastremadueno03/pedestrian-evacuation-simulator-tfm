package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.MooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Rectangle;
import es.uma.lcc.caesium.statistics.Random;
import pedestrian.Age;
import pedestrian.Attacker;
import pedestrian.Civilian;
import pedestrian.PedestrianWithVisionParameters;
import pedestrian.Police;

public class SimpleCompleteBehaviorTest {

	double cellSize;
	Scenario scenario;
	CellularAutomatonParameters params;
	SpecificCellularAutomaton automaton;
	
	@BeforeEach
	void start() {
		Random.random.setSeed(4);
		
		scenario = new Scenario.Builder()
				.rows(50)
				.columns(50)
				.cellDimension(1.0)
				.build();
		
		scenario.setExit(new Rectangle(0, 50, 10, 0));

	    var cellularAutomatonParameters =
	        new CellularAutomatonParameters.Builder()
	            .scenario(scenario) // use this scenario
	            .timeLimit(10 * 60) // 10 minutes is time limit for simulation
	            .neighbourhood(MooreNeighbourhood::of) // use Moore's Neighbourhood for automaton
	            .pedestrianReferenceVelocity(1.3) // fastest pedestrians walk at 1.3 m/s
	            .GUITimeFactor(8) // perform GUI animation x8 times faster than real time
	            .build();

	    automaton = new SpecificCellularAutomaton(cellularAutomatonParameters);
	}

	@Test
	void test() {
		automaton.calculateVisibilityMap();
		automaton.calculateDistanceMap();
		
		PedestrianWithVisionParameters pParams = new PedestrianWithVisionParameters.Builder()
				.attackerWeight(-100)
				.policeWeight(0)
				.civilianWeight(0)
				.crowdRepulsion(0)
				.fieldAttractionBias(0)
				.greedyProb(1)
				.inertiaWeight(0)
				.visionRadius(500)
				.build();
		PedestrianWithVisionParameters pParamsAtt = new PedestrianWithVisionParameters.Builder()
				.attackRadius(0)
				.attackerWeight(0)
				.civilianWeight(100)
				.crowdRepulsion(0)
				.fieldAttractionBias(0)
				.greedyProb(1)
				.inertiaWeight(0)
				.policeWeight(0)
				.visionRadius(500)
				.build();
		PedestrianWithVisionParameters pParamsPol = new PedestrianWithVisionParameters.Builder()
				.attackRadius(0)
				.attackerWeight(100)
				.civilianWeight(0)
				.crowdRepulsion(0)
				.fieldAttractionBias(0)
				.greedyProb(1)
				.inertiaWeight(0)
				.policeWeight(0)
				.visionRadius(500)
				.build();
		
		Civilian obj = new Civilian(3, 3, pParams, automaton, Age.ADULT);
		Attacker atk = new Attacker(3, 47, pParamsAtt, automaton);
		Police pol = new Police(47, 47, pParamsPol, automaton);
		
		automaton.addPedestrian(obj);
		automaton.addPedestrian(atk);
		automaton.addPedestrian(pol);
		
		System.out.println("Iniciando simulación test...");
		automaton.runGUI();
	}
	
}
