package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Rectangle;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.pedestrian.Age;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.pedestrian.Civilian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.pedestrian.PedestrianWithVisionParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.signs.EvacuationPlanSign;
import es.uma.lcc.caesium.statistics.Random;

public class HerdBehaviorTest {

	double cellSize;
	Scenario scenario;
	CellularAutomatonParameters params;
	SpecificCellularAutomaton automaton;
	
	@BeforeEach
	void start() {
		Random.random.setSeed(4);
		scenario = new Scenario.Builder()
				.rows(40)
				.columns(40)
				.cellDimension(1.0)
				.build();
		
		scenario.setExit(new Rectangle(35, 35, 5, 5));
		
		params = new CellularAutomatonParameters.Builder()
				.scenario(scenario)
				.timeLimit(1000)
				.build();
		
		automaton = new SpecificCellularAutomaton(params);
	    for(Rectangle exit : scenario.exits()) {
	    	int centerRow = exit.bottom() + (exit.height() / 2);
	    	int centerCol = exit.left() + (exit.width() / 2);
	    	EvacuationPlanSign exitSign = new EvacuationPlanSign(centerRow, centerCol);
	    	automaton.addSign(exitSign);
	    	System.out.println("Exit Sign generated at (" + centerRow + ", " + centerCol + ").");
	    }
	}

	@Test
	void test() {
		EvacuationPlanSign sign = new EvacuationPlanSign(20, 20);
		automaton.addSign(sign);
		automaton.calculateVisibilityMap();
		automaton.calculateDistanceMap();
		
		PedestrianWithVisionParameters pParams = new PedestrianWithVisionParameters.Builder().build();
		
		Civilian obj = new Civilian(5, 8, pParams, automaton, Age.ADULT);
		Civilian obj2 = new Civilian(10, 3, pParams, automaton, Age.ADULT);
		Civilian obj3 = new Civilian(4, 5, pParams, automaton, Age.ADULT);
		Civilian obj4 = new Civilian(2, 10, pParams, automaton, Age.ADULT);
		Civilian obj5 = new Civilian(7, 5, pParams, automaton, Age.ADULT);
		
		automaton.addPedestrian(obj);
		automaton.addPedestrian(obj2);
		automaton.addPedestrian(obj3);
		automaton.addPedestrian(obj4);
		automaton.addPedestrian(obj5);
		
		System.out.println("Iniciando simulación test...");
		automaton.runGUI();
	}
	
}
