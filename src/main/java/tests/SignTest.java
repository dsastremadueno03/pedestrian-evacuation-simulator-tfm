package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Rectangle;
import pedestrian.Age;
import pedestrian.Civilian;
import signs.VisualSign;

class SignTest {
	
	double cellSize;
	Scenario scenario;
	CellularAutomatonParameters params;
	SpecificCellularAutomaton automaton;
	
	@BeforeEach
	void start() {
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
	}

	@Test
	void test() {
		VisualSign sign = new VisualSign(20, 20);
		automaton.addSign(sign);
		automaton.calculateVisibilityMap();
		
		PedestrianParameters pParams = new PedestrianParameters.Builder()
				.velocityPercent(1.0)
				.fieldAttractionBias(10.0)
				.build();
		
		Civilian obj = new Civilian(5, 5, pParams, automaton, Age.ADULT);
		
		automaton.addPedestrian(obj);
		
		System.out.println("Iniciando simulación test...");
		automaton.runGUI();
	}

}
