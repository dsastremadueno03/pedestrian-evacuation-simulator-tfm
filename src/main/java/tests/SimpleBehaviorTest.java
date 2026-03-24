package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Rectangle;
import es.uma.lcc.caesium.statistics.Random;
import pedestrian.Age;
import pedestrian.Attacker;
import pedestrian.Civilian;
import pedestrian.PedestrianWithVisionParameters;

public class SimpleBehaviorTest {

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
		
		params = new CellularAutomatonParameters.Builder()
				.scenario(scenario)
				.timeLimit(1000)
				.build();
		
		automaton = new SpecificCellularAutomaton(params);
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
				.visionRadius(100)
				.build();
		PedestrianWithVisionParameters pParamsAtt = new PedestrianWithVisionParameters.Builder()
				.attackerWeight(0)
				.civilianWeight(100)
				.crowdRepulsion(0)
				.fieldAttractionBias(0)
				.greedyProb(1)
				.inertiaWeight(0)
				.policeWeight(0)
				.visionRadius(100)
				.build();
		
		Civilian obj = new Civilian(5, 2, pParams, automaton, Age.ADULT);
		Attacker atk = new Attacker(5, 1, pParamsAtt, automaton);
		
		automaton.addPedestrian(obj);
		automaton.addPedestrian(atk);
		
		System.out.println("Iniciando simulación test...");
		automaton.runGUI();
	}
	
}
