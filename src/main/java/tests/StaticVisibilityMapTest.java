package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import automaton.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Rectangle;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.MooreNeighbourhood;

class StaticVisibilityMapTest {
	
	private SpecificCellularAutomaton automaton;
	private Scenario scenario;
	private final int ROWS = 10;
	private final int COLS = 10;
	
	
	@BeforeEach
	void setUp() {
		var scenarioBuilder = new Scenario.Builder()
				.rows(ROWS)
				.columns(COLS)
				.cellDimension(1);
		
		scenario = scenarioBuilder.build();
	}
	
	private void initAutomaton(Scenario sc) {
		var params = new CellularAutomatonParameters.Builder()
				.scenario(sc)
				.timeLimit(100)
				.neighbourhood(MooreNeighbourhood::of)
				.build();
		automaton = new SpecificCellularAutomaton(params);
	}

	/**
	 * Tests the generation of the static matrix by checking
	 * that all cells are considered by checking true when 
	 * no obstacles are placed.
	 */
	@Test
	void GenerationMapTest() {
		initAutomaton(scenario);
		automaton.calculateVisibilityMap();
		
		for(int r0 = 0; r0 < ROWS; r0++){
			for(int c0 = 0; c0 < COLS; c0++) {
				Location origin = new Location(r0,c0);
				assertTrue(automaton.isVisible(origin, origin), "Error: Cell " + origin + " is not visible for itself!");
				
				for(int r1 = 0; r1 < ROWS; r1++) {
					for(int c1 = 0; c1 < COLS; c1++) {
						Location dest = new Location(r1,c1);
						assertTrue(automaton.isVisible(origin, dest), "Error: Cell " + origin + "should be able to see " + dest + " but it cannot.");
					}
				}
			}
		}
		
	}
	
	/**
	 * Tests vision ray through obstacles
	 */
	@Test
	void ObstacleTest() {
		scenario.setBlock(new Rectangle(2,5,8,1));
		
		initAutomaton(scenario);
		automaton.calculateVisibilityMap();
		
		// X | *X*
		Location left = new Location(5,4);
		Location right = new Location(5,6);
		
		assertFalse(automaton.isVisible(left, right), "Obstacle should hide vision!");
		
		// X *|*
		Location start = new Location(5,4);
		Location end = new Location(5,5);
		
		assertTrue(automaton.isVisible(start, end), "Obstacle should be visible!");
		
		// X  |
		//*X* |
		Location up = new Location(5,4);
		Location down = new Location(6,4);
		
		assertTrue(automaton.isVisible(up, down), "Cell should be visible!");
		
		//	*X*
		// X |
		Location diagStart = new Location(2,4);
		Location diagEnd = new Location(1,5);
		
		assertTrue(automaton.isVisible(diagStart, diagEnd), "Cell should be visible in diagonal!");
		
	}

}
