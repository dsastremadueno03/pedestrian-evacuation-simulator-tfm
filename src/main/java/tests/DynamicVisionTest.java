package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.MooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;
import pedestrian.PedestrianWithVision;

class DynamicVisionTest {
	
	private SpecificCellularAutomaton automaton;
	private PedestrianParameters defaultParams;
	private final int ROWS = 10;
	private final int COLS = 10;

	@BeforeEach
	void setUp() {
		var scenarioBuilder = new Scenario.Builder()
				.rows(ROWS)
				.columns(COLS)
				.cellDimension(1);
		
		var scenario = scenarioBuilder.build();
		
		var params = new CellularAutomatonParameters.Builder()
				.scenario(scenario)
				.timeLimit(100)
				.neighbourhood(MooreNeighbourhood::of)
				.build();
		
		automaton = new SpecificCellularAutomaton(params);
		
		automaton.calculateVisibilityMap();
		
		defaultParams = new PedestrianParameters.Builder().build();
	}
	
	@Test
	void testPedestrianBlocksVisionVerticalHorizontal() {
		PedestrianWithVision observer = new PedestrianWithVision(5, 1, defaultParams, automaton, 5);
		automaton.addPedestrian(observer);
		
		PedestrianWithVision blocker = new PedestrianWithVision(5, 3, defaultParams, automaton, 5);
		boolean placed = automaton.addPedestrian(blocker);
		
		assertTrue(placed, "Blocker should be placed in (5,3)!");
		assertTrue(automaton.isCellOccupied(5,3), "Cell (5,3) should be occupied!");
		
		List<Location> visibleCells = observer.computeVisibleCells();
		
		// Gap in between is visible
		
		assertTrue(visibleCells.contains(new Location(5,2)));
		
		// Blocker is visible
		
		assertTrue(visibleCells.contains(new Location(5,3)));
		
		// Behind blocker is not visible
		
		assertFalse(visibleCells.contains(new Location(5,4)));
		assertFalse(visibleCells.contains(new Location(5,5)));
		
		// Diagonal is visible
		
		assertTrue(visibleCells.contains(new Location(4,3)));
				
	}
	
	@Test
	void testPedestrianBlocksVisionDiagonal() {
		PedestrianWithVision observer = new PedestrianWithVision(0, 0, defaultParams, automaton, 5);
		automaton.addPedestrian(observer);
		
		PedestrianWithVision blocker = new PedestrianWithVision(2, 2, defaultParams, automaton, 5);
		boolean placed = automaton.addPedestrian(blocker);
		
		assertTrue(placed, "Blocker should be placed in (2,2)!");
		assertTrue(automaton.isCellOccupied(2,2), "Cell (2,2) should be occupied!");
		
		List<Location> visibleCells = observer.computeVisibleCells();
		
		// Gap in between is visible
		
		assertTrue(visibleCells.contains(new Location(1,1)), "Gap in between should be visible");
		
		// Blocker is visible
		
		assertTrue(visibleCells.contains(new Location(2,2)), "Blocker should be visible");
		
		// Behind blocker is not visible
		
		assertFalse(visibleCells.contains(new Location(3,3)), "Behind blocker should not be visible");
		assertFalse(visibleCells.contains(new Location(4,4)), "Behind blocker should not be visible");
		
		// Next to diagonal is visible
		
		assertTrue(visibleCells.contains(new Location(4,3)), "Next to diagonal should be visible");
				
				
	}

}
