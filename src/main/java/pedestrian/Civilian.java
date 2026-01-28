package pedestrian;

import static es.uma.lcc.caesium.statistics.Random.random;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import automaton.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.gui.Canvas;
import signs.Sign;

public class Civilian extends PedestrianWithVision {

	private final Age ageGroup;
	
	private boolean knowsExit; // Access to desirability static map 
	
	/**
	 * Minimum desirability of a cell so that it is never 0.
	 */
	private static final double DESIRABILITY_EPSILON = 0.00001;

		public Civilian(int row, int column, PedestrianParameters parameters, CellularAutomaton automaton, Age ageGroup){
			super(row, column, parameters, automaton);
			this.ageGroup = ageGroup;
			knowsExit = false;
		}
		
		public Age getAge() {
			return ageGroup;
		}
		
		/**
		 * Detects signals in the surroundings
		 */
		protected void updateDetection() {
			if(knowsExit) {
				return;
			}
			
			if(automaton instanceof SpecificCellularAutomaton) {
				SpecificCellularAutomaton newAutomaton = (SpecificCellularAutomaton) automaton;
				List<Sign> signs = newAutomaton.getSigns();
				
				for(Sign sign : signs) {
					if(sign.isDetectedBy(this)) {
						knowsExit = true;
						break;
					}
				}
			}
			
		}
		
		//Since TentativeMovement cannot be created, CivilianMovement is created
		private record CivilianMovement(Location location, double desirability) {}
				
		
		/**
		 * Choose randomly pedestrian's next move from those computed by
		 * {@code computeCustomDesirabilities}.
		 *
		 * @return {@code Optional.empty} if no move is available or {@code Optional(m)}
		 *         if move {@code m} was chosen.
		 */
		@Override
		public Optional<Location> chooseMovement() {
			
			updateDetection();
			
			if (random.bernoulli(parameters.velocityPercent())) {
				List<CivilianMovement> movements = computeCustomDesirabilities();
				if (movements.isEmpty()) {
					// cannot make a movement
					return Optional.empty();
				}

				// choose one movement according to discrete distribution of desirabilities
				var chosen = random.discrete(movements, CivilianMovement::desirability);
				return Optional.of(chosen.location());
			} else {
				// do not move at this step to respect pedestrian speed
				return Optional.empty();
			}
		}
		
		
		/**
		 * Computes transition desirabilities for reachable cells in the neighbourhood
		 * on this civilian. (the higher the desirability the higher the willingness to
		 * move to such location). We do not use the term probability because sum of all
		 * desirabilities do not have to be 1.
		 *
		 * @return List of tentative movements that this pedestrian can make, each one
		 *         with associate desirability.
		 */
		private List<CivilianMovement> computeCustomDesirabilities() {
			var scenario = automaton.getScenario();
			var neighbours = automaton.neighbours(row, column);

			var movements = new ArrayList<CivilianMovement>(neighbours.size());
			double minDesirability = Double.MAX_VALUE;
			
			// Changes according to whether civilian has seen sign
			double currentAttraction;
			if(knowsExit) {
				currentAttraction = parameters.fieldAttractionBias();
			} else {
				currentAttraction = 0;
			}
			
			for (var neighbour : neighbours) {
				if (automaton.isCellReachable(neighbour)) {
					// count reachable cells around new location
					var numberOfReachableCellsAround = 0;
					for (var around : automaton.neighbours(neighbour)) {
						if (automaton.isCellReachable(around)) {
							numberOfReachableCellsAround++;
						}
					}

					var attraction = currentAttraction
							* scenario.getStaticFloorField().getField(neighbour);
					var repulsion = parameters.crowdRepulsion() / (1 + numberOfReachableCellsAround);
					var desirability = Math.exp(attraction - repulsion);
					movements.add(new CivilianMovement(neighbour, desirability));
					if (desirability < minDesirability)
						minDesirability = desirability;
				}
			}
			var gradientMovements = new ArrayList<CivilianMovement>(neighbours.size());
			for (CivilianMovement m : movements)
				gradientMovements.add(
						new CivilianMovement(m.location(), DESIRABILITY_EPSILON + m.desirability() - minDesirability));

			return gradientMovements;
		}
		
		@Override
		public void paint(Canvas canvas, Color fillColor, Color outlineColor) {
			switch (ageGroup) {
			case CHILD: {
				super.paint(canvas, Color.YELLOW, outlineColor);
				break;
			}
			case ADULT: {
				super.paint(canvas, Color.GREEN, outlineColor);
				break;
			}
			case ELDERLY: {
				super.paint(canvas, Color.MAGENTA, outlineColor);
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + ageGroup);
			}

		}
		
		@Override
		public String toString() {
			return super.toString() + "[Civilian: " + ageGroup + "]";
		}
		
}
