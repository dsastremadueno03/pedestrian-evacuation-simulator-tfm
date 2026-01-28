package signs;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;
import pedestrian.PedestrianWithVision;

public abstract class Sign {

	private Location location;
	
	public Sign(Location location) {
		this.location = location;
	}
	
	public Sign(int row, int col) {
		this.location = new Location(row, col);
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	/**
	 * Defines how a sign can be detected
	 * @return true if detected
	 */
	public abstract boolean isDetectedBy(PedestrianWithVision p);
	
	/**
	 * Calculates squared distance from sign to pedestrian
	 * @param p pedestrian to calculate distance to
	 * @return squared distance
	 */
	protected double calculateSquaredDistance(PedestrianWithVision p) {
		return Math.pow(location.row() - p.getRow(), 2) + Math.pow(location.column() - p.getColumn(), 2);
		
	}
	
}
