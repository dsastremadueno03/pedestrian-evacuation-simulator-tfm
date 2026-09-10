package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.signs;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.pedestrian.Civilian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.pedestrian.PedestrianWithVision;

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
	 * Calculates distance from sign to pedestrian
	 * @param p pedestrian to calculate distance to
	 * @return distance
	 */
	protected double calculateDistance(PedestrianWithVision p) {
		return p.getDistance(this.location.row(), this.location.column(), p.getRow(), p.getColumn());
	}
	
	/**
	 * Applies effect of specific signal on Pedestrian p
	 * 
	 * @param p pedestrian to apply on
	 */
	public void applyEffect(PedestrianWithVision p) {
		if(p instanceof Civilian) {
			if(isDetectedBy(p)) {
				executeBehavior((Civilian)p);
			}
		}
	}
	
	/**
	 * Execute the behavior to be applied on civilian
	 * 
	 * @param c civilian to execute it on
	 */
	protected abstract void executeBehavior(Civilian c);
	
}
