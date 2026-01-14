package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;

public class Civilian extends Pedestrian {
	private final Age ageGroup;
	
	public Civilian(int row, int column, PedestrianParameters parameters, CellularAutomaton automaton, Age ageGroup){
		super(row, column, parameters, automaton);
		this.ageGroup = ageGroup;
	}
	
	public Age getAge() {
		return ageGroup;
	}
	
	@Override
	public String toString() {
		return super.toString() + "[Civilian: " + ageGroup + "]";
	}
	
}
