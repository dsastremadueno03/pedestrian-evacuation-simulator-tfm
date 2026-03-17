package pedestrian;

import java.util.ArrayList;
import java.util.List;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;
import static es.uma.lcc.caesium.statistics.Random.random;

public class PopulationGenerator {
	private final MultiPedestrianFactory factory;
	private final SpecificCellularAutomaton automaton;
	
	public PopulationGenerator(MultiPedestrianFactory factory, SpecificCellularAutomaton automaton) {
		this.factory = factory;
		this.automaton = automaton;
	}
	
	/**
	 * Generates population following parameters passed
	 * @param config parameters for general population distribution
	 * @param defaultParams parameters for individual pedestrian
	 * @return list of population
	 */
		public List<Pedestrian> generatePopulation(PopulationConfig config, PedestrianWithVisionParameters defaultParams){
			List<Pedestrian> pedestrians = new ArrayList<Pedestrian>();
			
			// Civilians
			for(int i = 0; i < config.numCivilians(); i++) {
				Location loc = getRandomEmptyLocation();
				Age age = determineAge(config);
				
				double finalSpeed = defaultParams.velocityPercent();
				
				// Adjust speed to age
				if(age == Age.CHILD)
					finalSpeed = Math.min(1.0, 1.5 * finalSpeed);
				else if (age == Age.ELDERLY)
					finalSpeed = finalSpeed * 0.5;
				
				// Rebuilding parameters
				PedestrianWithVisionParameters finalParams = new PedestrianWithVisionParameters.Builder()
						.fieldAttractionBias(defaultParams.fieldAttractionBias())
						.crowdRepulsion(defaultParams.crowdRepulsion())
						.velocityPercent(finalSpeed)
						.visionRadius(defaultParams.visionRadius())
					    .attackRadius(defaultParams.attackRadius())
					    .inertiaWeight(defaultParams.inertiaWeight())
					    .civilianWeight(defaultParams.civilianWeight())
					    .policeWeight(defaultParams.policeWeight())
					    .attackerWeight(defaultParams.attackerWeight())
						.build();
				
				pedestrians.add(factory.getCivilian(loc.row(), loc.column(), finalParams, age));
			}
			
			// Attackers
			// TODO: Ajustar parámetros de campo de visión y agresividad
			for(int i = 0; i < config.numAttackers(); i++) {
				Location loc = getRandomEmptyLocation();
				pedestrians.add(factory.getAttacker(loc.row(), loc.column(), defaultParams, 5, 80));
			}
			
			// Police
			// TODO: Ajustar parámetros de campo de visión
			for(int i = 0; i < config.numPolice(); i++) {
				Location loc = getRandomEmptyLocation();
				pedestrians.add(factory.getPolice(loc.row(), loc.column(), defaultParams, 3));
			}
			
			return pedestrians;
		}
		
		private Age determineAge(PopulationConfig config) {
			double rand = random.nextDouble();
			if(rand < config.probChild())
				return Age.CHILD;
			if(rand < config.probChild() + config.probAdult())
				return Age.ADULT;
			else return Age.ELDERLY;
		}
		
		private Location getRandomEmptyLocation() {
			int row;
			int col;
			do {
				row = random.nextInt(automaton.getRows());
				col = random.nextInt(automaton.getColumns());
			} while (!automaton.isCellReachable(row, col));
			return new Location(row, col);
		}
	}