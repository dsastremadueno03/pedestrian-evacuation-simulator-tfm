package pedestrian;

import java.awt.Color;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.gui.Canvas;

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
		public void paint(Canvas canvas, Color fillColor, Color outlineColor) {
			switch (ageGroup) {
			case CHILD: {
				super.paint(canvas, Color.LIGHT_GRAY, outlineColor);
				break;
			}
			case ADULT: {
				super.paint(canvas, Color.GRAY, outlineColor);
				break;
			}
			case ELDERLY: {
				super.paint(canvas, Color.DARK_GRAY, outlineColor);
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + ageGroup);
			}

		}
		
		@Override
		public String toString() {
			return super.toString() + "[Civilian: " + ageGroup + "]";
		}
		
}
