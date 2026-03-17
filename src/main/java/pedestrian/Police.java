package pedestrian;

import java.awt.Color;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.gui.Canvas;

public class Police extends PedestrianWithVision {
	
	public Police(int row, int column, PedestrianWithVisionParameters parameters, CellularAutomaton automaton){
		super(row, column, parameters, automaton);
	}

	public boolean isMelee() {
		return super.attackRadius <= 1;
	}
	
	@Override
	public void paint(Canvas canvas, Color fillColor, Color outlineColor) {
		super.paint(canvas, Color.BLUE, outlineColor);
	}
	
	@Override
	public String toString() {
		return super.toString() + " [Police: Range=" + super.attackRadius + " cells]";
	}
	
}