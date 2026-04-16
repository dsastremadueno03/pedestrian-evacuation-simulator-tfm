package pedestrian;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianFactory;

public class MultiPedestrianFactory extends PedestrianFactory {
	
	private CellularAutomaton automaton;
	
	public MultiPedestrianFactory(CellularAutomaton automaton) {
		super(automaton);
		this.automaton = automaton;
	}

	// Generación de diferentes tipos de Pedestrian

	public Civilian getCivilian(int row, int column, PedestrianWithVisionParameters parameters, Age ageGroup) {
		  validateCoordinates(row, column);
		  return new Civilian(row, column, parameters, automaton, ageGroup);
	  }

	public Attacker getAttacker(int row, int column, PedestrianWithVisionParameters parameters) {
		validateCoordinates(row, column);
		return new Attacker(row, column, parameters, automaton);
	}

	public Police getPolice(int row, int column, PedestrianWithVisionParameters parameters) {
		validateCoordinates(row, column);
		return new Police(row, column, parameters, automaton);
	}

	// --------------

	private void validateCoordinates(int row, int column) {
		assert row >= 0 && row < automaton.getRows() : "getInstance: invalid row";
		assert column >= 0 && row < automaton.getColumns() : "getInstance: invalid column";
	}
}
