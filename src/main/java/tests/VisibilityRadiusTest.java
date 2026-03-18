package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.MooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;
import es.uma.lcc.caesium.statistics.Random;
import pedestrian.PedestrianWithVision;
import pedestrian.PedestrianWithVisionParameters;

class VisibilityRadiusTest {

	private SpecificCellularAutomaton automaton;
	private PedestrianWithVisionParameters defaultParams;
	private final int ROWS = 10;
	private final int COLS = 10;

	@BeforeEach
	void setUp() {
		Random.random.setSeed(4);
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
		automaton.calculateDistanceMap();
		
		defaultParams = new PedestrianWithVisionParameters.Builder().visionRadius(2).build();
	}
	
	@Test
	void testPedestrianBlocksVisionVerticalHorizontal() {
		PedestrianWithVision observer = new PedestrianWithVision(5, 5, defaultParams, automaton);
		automaton.addPedestrian(observer);
		
		assertTrue(automaton.isCellOccupied(5,5), "Cell (5,5) should be occupied!");
		
		List<Location> visibleCells = observer.computeVisibleCells();
		
		// Diagonal should be visible
		
		assertTrue(visibleCells.contains(new Location(6,6)));
		// sqrt(1^2 + 2^2) = sqrt(5) > 2 (OUT OF RANGE)
		assertFalse(visibleCells.contains(new Location(6,7)), "(r+1, c+2) should not be visible!");
		// sqrt(2^2 + 2^2) = sqrt(8) > 2 (OUT OF RANGE)
		assertFalse(visibleCells.contains(new Location(7,7)), "(r+2, c+2) should not be visible!");
		
		// Horizontal should be visible
		
		assertTrue(visibleCells.contains(new Location(5,6)));
		assertTrue(visibleCells.contains(new Location(5,7)));
				
	}

}
