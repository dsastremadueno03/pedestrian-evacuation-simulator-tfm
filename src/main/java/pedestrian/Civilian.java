package pedestrian;

import static es.uma.lcc.caesium.statistics.Random.random;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.gui.Canvas;
import signs.Sign;

public class Civilian extends PedestrianWithVision {

	private final Age ageGroup;
	
	private boolean permanentExitKnown; // Knows exit map always
	
	private boolean temporalExitKnown; // Knows exit map right now

		public Civilian(int row, int column, PedestrianWithVisionParameters parameters, CellularAutomaton automaton, Age ageGroup){
			super(row, column, parameters, automaton);
			this.ageGroup = ageGroup;
			permanentExitKnown = false;
			temporalExitKnown = false;
		}
		
		public Age getAge() {
			return ageGroup;
		}
		
		public void setPermanentExitKnown(boolean b) {
			permanentExitKnown = b;
		}
		
		public void setTemporalExitKnown(boolean b) {
			temporalExitKnown = b;
		}
		
		/**
		 * Detects signals in the surroundings
		 */
		protected void updateDetection() {
			temporalExitKnown = false; // Always update
			
			if(automaton instanceof SpecificCellularAutomaton) {
				SpecificCellularAutomaton myNewAutomaton = (SpecificCellularAutomaton) automaton;
				
				for(Sign sign : myNewAutomaton.getSigns()) {
					sign.applyEffect(this);
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
			// Checks civilian is alive
			if(!this.isAlive) {
				return Optional.empty();
			}
			
			updateDetection();
			
			if (random.bernoulli(parameters.velocityPercent())) {
				List<CivilianMovement> movements = computeCustomDesirabilities();
				if (movements.isEmpty()) {
					// cannot make a movement
					return Optional.empty();
				}
				
				// GREEDY
				if(random.nextDouble() < super.customParameters.greedyProb()) {
					// Storing max desirability
				    double maxDesirability = -Double.MAX_VALUE;
				    for (CivilianMovement m : movements) {
				        if (m.desirability() > maxDesirability) {
				            maxDesirability = m.desirability();
				        }
				    }
				    
				    // Storing best moves
				    List<CivilianMovement> bestMovements = new ArrayList<>();
				    for (CivilianMovement m : movements) {
				        // Compared to almost zero (avoid problems with perfect 0)
				        if (Math.abs(m.desirability() - maxDesirability) < 1e-10) {
				            bestMovements.add(m);
				        }
				    }
				    
				    // Random selection among best moves
				    int randomIndex = (int)(random.nextDouble() * bestMovements.size());
				    CivilianMovement chosenBest = bestMovements.get(randomIndex);
				    
				    return Optional.of(chosenBest.location());
				} 
				// RANDOM
				else {
				// choose one movement according to discrete distribution of desirabilities
				var chosen = random.discrete(movements, CivilianMovement::desirability);
				return Optional.of(chosen.location());
				}
				
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
			SpecificCellularAutomaton myAutomaton = (SpecificCellularAutomaton) this.automaton;
			var scenario = automaton.getScenario();
			
			// Allows staying in the same cell
			var neighbours = automaton.neighbours(row, column);
			List<Location> candidates = new ArrayList<>(neighbours);
			candidates.add(new Location(this.row, this.column));

			var movements = new ArrayList<CivilianMovement>(candidates.size());
			double minDesirability = Double.MAX_VALUE;

			// Changes according to whether civilian has seen sign
			double currentAttraction;
			if(temporalExitKnown || permanentExitKnown) {
				currentAttraction = super.customParameters.fieldAttractionBias();
			} else {
				currentAttraction = 0;
			}
			
			// Calculate inertia vector for movement desirability
			double inertiaWeight = super.customParameters.inertiaWeight();
			int inertiaRow = 0;
			int inertiaCol = 0;
			
			// Get path history of civilian
			List<Location> path = this.getPath();
			if(path != null && path.size() >= 2) {
				// Get previous location of civilian
				Location prevLoc = path.get(path.size()-2);
				inertiaRow = this.row - prevLoc.row();
				inertiaCol = this.column - prevLoc.column();
			}
			

			
			// SOCIAL FIELD - PHASE 1 - FINDING NEIGHBORS
			List<PedestrianWithVision> visiblePeople = new ArrayList<PedestrianWithVision>();
			for(Location loc : computeVisibleCells()) { // Limits to field of vision
				Pedestrian person = myAutomaton.getPedestrianAt(loc.row(), loc.column());
				// Add to list of visible pedestrians if it's PedestrianWithVision 
				// and it's not itself
				if(person instanceof PedestrianWithVision && person != this) {
					visiblePeople.add((PedestrianWithVision) person);
				}
			}
			
			
			// Checks if same cell or reachable cells
			for (var neighbour : candidates) {
				if ((neighbour.row() == this.row && neighbour.column() == this.column) 
						|| myAutomaton.isCellReachable(neighbour)) {
					// Count reachable cells around new location
					var numberOfReachableCellsAround = 0;
					for (var around : myAutomaton.neighbours(neighbour)) {
						if (myAutomaton.isCellReachable(around)) {
							numberOfReachableCellsAround++;
						}
					}

					var attraction = currentAttraction
							* scenario.getStaticFloorField().getField(neighbour);
					var repulsion = parameters.crowdRepulsion() / (1 + numberOfReachableCellsAround);
					
					
					// SOCIAL FIELD - PHASE 2 - CALCULATE CS (SOCIAL FIELD)
					double socialField = 0;
					
					// Differentiation between Pedestrian types
					for(PedestrianWithVision p : visiblePeople) {
						double weight = 0;
						if(!p.isAlive) {
							continue;
						}
						// Civilian
						if(p instanceof Civilian) {
							weight = super.customParameters.civilianWeight();
						}
						// Police
						else if(p instanceof Police) {
							weight = super.customParameters.policeWeight();
						}
						// Attacker
						else if(p instanceof Attacker) {
							weight = super.customParameters.attackerWeight();
						}
						
						double dist = getDistance(neighbour.row(), neighbour.column(), p.getRow(), p.getColumn());
						dist = Math.max(0.00001, dist); // Prevent from 0 division
						
						socialField += weight * (1 / dist);
						
					}
					
					// Calculate Intertia desirability
					double inertiaDesirability = 0;
					if(inertiaRow != 0 || inertiaCol != 0) {
						int moveRow = neighbour.row() - this.row;
						int moveCol = neighbour.column() - this.column;
						// Dot product between inertia and proposed neighbor
						// + -> Same direction
						// - -> Different direction
						double joining = (inertiaRow * moveRow) + (inertiaCol * moveCol);
						inertiaDesirability = inertiaWeight * joining; 
					}
					
					// Recalibration needed to not get to very high values
					double recalibrate = (attraction + socialField + inertiaDesirability - repulsion) * 0.1; 
					var desirability = Math.exp(recalibrate);
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
			if(permanentExitKnown || temporalExitKnown) {
				outlineColor = Color.WHITE;
			}
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
			if(!this.isAlive) { // Dead
				super.paint(canvas, new Color(150, 0, 0), Color.BLACK);
			}
		}
		
		@Override
		public String toString() {
			return super.toString() + "[Civilian: " + ageGroup + "]";
		}
		
}
