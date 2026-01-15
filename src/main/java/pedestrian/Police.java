package pedestrian;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianParameters;
import java.awt.Color;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.gui.Canvas;

public class Police extends Pedestrian {
	private final int attackRange;
	
	public Police(int row, int column, PedestrianParameters parameters, CellularAutomaton automaton, int attackRange){
		super(row, column, parameters, automaton);
		this.attackRange = attackRange;
	}

	public int getAttackRange() {
		return attackRange;
	}

	public boolean isMelee() {
		return attackRange <= 1;
	}
	
	@Override
	public void paint(Canvas canvas, Color fillColor, Color outlineColor) {
		super.paint(canvas, Color.BLUE, outlineColor);
	}
	
	@Override
	public String toString() {
		return super.toString() + " [Police: Range=" + attackRange + " cells]";
	}
	
}