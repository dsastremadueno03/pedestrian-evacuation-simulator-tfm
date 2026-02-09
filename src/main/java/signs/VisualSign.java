package signs;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;
import pedestrian.PedestrianWithVision;

public class VisualSign extends Sign {

	public VisualSign(Location location) {
		super(location);
	}
	
	public VisualSign(int row, int col) {
		super(row, col);
	}
	
	@Override
	public boolean isDetectedBy(PedestrianWithVision p) {
		double visionRadius = p.getVisionRadius();
		if(calculateDistance(p) > visionRadius) { // Better for efficiency rather than using squared root
			return false;
		}
		// Only checks static for walls (pedestrians do not affect vision)
		return p.hasStaticSight(p.getRow(), p.getColumn(), super.getLocation().row(), super.getLocation().column());
	}

}
