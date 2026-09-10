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
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.signs.EphimeralVisualSign;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.signs.EvacuationPlanSign;
import es.uma.lcc.caesium.statistics.Random;

class EphimeralSignTest {
	
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
				.timeLimit(100)
				.build();
		
		automaton = new SpecificCellularAutomaton(params);
		
		System.out.println("Generating permanent signs on exits...");
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
		EphimeralVisualSign sign = new EphimeralVisualSign(20, 20);
		EphimeralVisualSign sign2 = new EphimeralVisualSign(10, 10);
		automaton.addSign(sign);
		automaton.addSign(sign2);
		automaton.calculateVisibilityMap();
		automaton.calculateDistanceMap();
		
		PedestrianWithVisionParameters pParams = new PedestrianWithVisionParameters.Builder().build();
		
		Civilian obj = new Civilian(5, 5, pParams, automaton, Age.ADULT);
		
		automaton.addPedestrian(obj);
		
		System.out.println("Iniciando simulación test...");
		automaton.runGUI();
	}

}
