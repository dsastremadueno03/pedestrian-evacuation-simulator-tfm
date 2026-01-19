package pedestrian;

import java.util.ArrayList;
import java.util.List;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianFactory;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;
import es.uma.lcc.caesium.statistics.Random;

public class PopulationGenerator {
	private final PedestrianFactory factory;
	private final CellularAutomaton automaton;
	private Random random;
	
	public PopulationGenerator(PedestrianFactory factory, CellularAutomaton automaton) {
		this.factory = factory;
		this.automaton = automaton;
		random = new Random();
	}
	
	// Para crear la población deseada según parámetros
	public List<Pedestrian> generatePopulation(PopulationConfig config, PedestrianParameters defaultParams){
		List<Pedestrian> pedestrians = new ArrayList<Pedestrian>();
		
		// Civiles
		for(int i = 0; i < config.numCivilians(); i++) {
			Location loc = getRandomEmptyLocation();
			Age age = determineAge(config);
			pedestrians.add(((MultiPedestrianFactory)factory).getCivilian(loc.row(), loc.column(), defaultParams, age));
		}
		
		// Atacantes
		// TODO: Ajustar parámetros de campo de visión y agresividad
		for(int i = 0; i < config.numAttackers(); i++) {
			Location loc = getRandomEmptyLocation();
			pedestrians.add(((MultiPedestrianFactory)factory).getAttacker(loc.row(), loc.column(), defaultParams, 5, 80));
		}
		
		// Policía
		// TODO: Ajustar parámetros de campo de visión
		for(int i = 0; i < config.numPolice(); i++) {
			Location loc = getRandomEmptyLocation();
			pedestrians.add(((MultiPedestrianFactory)factory).getPolice(loc.row(), loc.column(), defaultParams, 3));
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