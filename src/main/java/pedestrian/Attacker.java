package pedestrian;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianParameters;
import java.awt.Color;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.gui.Canvas;

public class Attacker extends Pedestrian {
	private final int attackRange;
	// aggressiveness = 0% - 100% 
	private final int aggressiveness;
	
	public Attacker(int row, int column, PedestrianParameters parameters, CellularAutomaton automaton, int attackRange, int aggressiveness){
		super(row, column, parameters, automaton);
		this.attackRange = attackRange;
		this.aggressiveness = aggressiveness;
	}

	public int getAttackRange() {
		return attackRange;
	}

	public int getAggressiveness() {
		return aggressiveness;
	}
	
	@Override
	public void paint(Canvas canvas, Color fillColor, Color outlineColor) {
		super.paint(canvas, Color.RED, outlineColor);
	}
	
	@Override
	public String toString() {
		return super.toString() + " [Attacker: Range=" + attackRange + " cells, Aggressiveness=" + aggressiveness + "%]";
	}
	
}