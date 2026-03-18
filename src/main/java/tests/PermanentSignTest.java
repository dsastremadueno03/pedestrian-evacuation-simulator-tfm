package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Rectangle;
import es.uma.lcc.caesium.statistics.Random;
import pedestrian.Age;
import pedestrian.Civilian;
import pedestrian.PedestrianWithVisionParameters;
import signs.EvacuationPlanSign;

class PermanentSignTest {
	
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
		EvacuationPlanSign sign = new EvacuationPlanSign(20, 20);
		automaton.addSign(sign);
		automaton.calculateVisibilityMap();
		automaton.calculateDistanceMap();
		
		PedestrianWithVisionParameters pParams = new PedestrianWithVisionParameters.Builder()
				.velocityPercent(1.0)
				.fieldAttractionBias(10.0)
				.build();
		
		Civilian obj = new Civilian(5, 5, pParams, automaton, Age.ADULT);
		
		automaton.addPedestrian(obj);
		
		System.out.println("Iniciando simulación test...");
		automaton.runGUI();
	}

}
