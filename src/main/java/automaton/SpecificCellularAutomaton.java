package automaton;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianFactory;

public class SpecificCellularAutomaton extends CellularAutomaton {
	
	public SpecificCellularAutomaton(CellularAutomatonParameters parameters) {
		super(parameters);
	}
	
	public boolean addPedestrian(Pedestrian pedestrian) {
		int row = pedestrian.getRow();
		int column = pedestrian.getColumn();
		
		if(row < 0 || row >= getRows() || column < 0 || column >= getColumns()) {
			return false;
		}
		
		if(isCellReachable(row, column)) {
			occupied[row][column] = true;
			inScenarioPedestrians.add(pedestrian);
			return true;
		}
		
		return false;
	}
	
	public PedestrianFactory getPedestrianFactory() {
		return this.pedestrianFactory;
	}

}
