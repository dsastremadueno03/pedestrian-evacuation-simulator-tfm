package pedestrian;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianFactory;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianParameters;

public class MultiPedestrianFactory extends PedestrianFactory {
	
	private CellularAutomaton automaton;
	
	public MultiPedestrianFactory(CellularAutomaton automaton) {
		super(automaton);
		this.automaton = automaton;
	}

	// Generación de diferentes tipos de Pedestrian

	public Civilian getCivilian(int row, int column, PedestrianParameters parameters, Age ageGroup) {
		  validateCoordinates(row, column);
		  return new Civilian(row, column, parameters, automaton, ageGroup);
	  }

	public Attacker getAttacker(int row, int column, PedestrianParameters parameters, int attackRange,
			int aggressiveness) {
		validateCoordinates(row, column);
		return new Attacker(row, column, parameters, automaton, attackRange, aggressiveness);
	}

	public Police getPolice(int row, int column, PedestrianParameters parameters, int attackRange) {
		validateCoordinates(row, column);
		return new Police(row, column, parameters, automaton, attackRange);
	}

	// --------------

	private void validateCoordinates(int row, int column) {
		assert row >= 0 && row < automaton.getRows() : "getInstance: invalid row";
		assert column >= 0 && row < automaton.getColumns() : "getInstance: invalid column";
	}
}
