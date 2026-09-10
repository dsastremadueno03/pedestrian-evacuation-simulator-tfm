package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.signs;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.pedestrian.Civilian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.pedestrian.PedestrianWithVision;

public class EphimeralVisualSign extends Sign {

	public EphimeralVisualSign(Location location) {
		super(location);
	}
	
	public EphimeralVisualSign(int row, int col) {
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
	
	@Override
	protected void executeBehavior(Civilian c) {
		c.setTemporalExitKnown(true);
		
	}

}
