package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.pedestrian;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianFactory;

public class MultiPedestrianFactory extends PedestrianFactory {
	
	private CellularAutomaton automaton;
	
	public MultiPedestrianFactory(CellularAutomaton automaton) {
		super(automaton);
		this.automaton = automaton;
	}

	// Generation of different pedestrians

	/**
	 * Generates a civilian.
	 * @param row Row to be placed in
	 * @param column Column to be placed in
	 * @param parameters Parameters specified for pedestrians
	 * @param ageGroup Age of the civilian
	 * @return Fully configured civilian
	 */
	public Civilian getCivilian(int row, int column, PedestrianWithVisionParameters parameters, Age ageGroup) {
		  validateCoordinates(row, column);
		  return new Civilian(row, column, parameters, automaton, ageGroup);
	  }

	/**
	 * Generates an attacker.
	 * @param row Row to be placed in
	 * @param column Column to be placed in
	 * @param parameters Parameters specified for pedestrians
	 * @return Fully configured attacker
	 */
	public Attacker getAttacker(int row, int column, PedestrianWithVisionParameters parameters) {
		validateCoordinates(row, column);
		return new Attacker(row, column, parameters, automaton);
	}

	/**
	 * Generates a police entity.
	 * @param row Row to be placed in
	 * @param column Column to be placed in
	 * @param parameters Parameters specified for pedestrians
	 * @return Fully configured police entity
	 */
	public Police getPolice(int row, int column, PedestrianWithVisionParameters parameters) {
		validateCoordinates(row, column);
		return new Police(row, column, parameters, automaton);
	}

	// --------------

	/**
	 * Testing for validity of coordinates.
	 * @param row Row selected
	 * @param column Column selected
	 */
	private void validateCoordinates(int row, int column) {
		assert row >= 0 && row < automaton.getRows() : "getInstance: invalid row";
		assert column >= 0 && row < automaton.getColumns() : "getInstance: invalid column";
	}
}
