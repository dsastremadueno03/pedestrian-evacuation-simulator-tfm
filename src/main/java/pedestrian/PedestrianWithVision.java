package pedestrian;

import java.util.ArrayList;
import java.util.List;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;

public class PedestrianWithVision extends Pedestrian {
	
	
	//TODO: Incluir parámetro variable en METROS
	public static final double DEFAULT_VISION = 7.0;
	
	protected int visionRadius;
	
	public PedestrianWithVision(int row, int column, PedestrianParameters parameters, CellularAutomaton automaton) {
		super(row, column, parameters, automaton);
		this.visionRadius = calculateVisionRadiusInCells(DEFAULT_VISION);
	}
	
	public PedestrianWithVision(int row, int column, PedestrianParameters parameters, CellularAutomaton automaton, double visionRadius) {
		super(row, column, parameters, automaton);
		this.visionRadius = calculateVisionRadiusInCells(visionRadius);
	}
	
	/**
	 *
	 * Converts meters to cells using dimension defined in scenario.
	 * 
	 * @param meters to be converted to cells
	 * @return number of cells equivalent
	 */
	private int calculateVisionRadiusInCells(double meters) {
		double cellDim = automaton.getScenario().getCellDimension();
		return (int) Math.ceil(meters / cellDim);
	}
	
	public int getVisionRadius() {
		return visionRadius;
	}
	
	/**
	   * Calculates cells in range of vision.
	   * 
	   * @return Cells visible
	   */
	  public List<Location> computeVisibleCells(){
		  List<Location> visibleCells = new ArrayList<Location>();
		  int rows = automaton.getRows();
		  int cols = automaton.getColumns();
		  
		  // Vision range following Euclidean distance
		  for(int r = row - visionRadius; r <= row + visionRadius; r++) {
			  for(int c = column - visionRadius; c <= column + visionRadius; c++) {
				  if(r >= 0 && r < rows && c >= 0 && c < cols) {
					  double distance = Math.pow(row - r, 2) + Math.pow(column - c, 2);
					  if(distance <= visionRadius * visionRadius) { // Distance smaller than square radius
						  // Check obstacles
						  if(hasSight(row, column, r, c)) {
							  visibleCells.add(new Location(r, c));
						  }
					  }
				  }
			  }
		  }
		  return visibleCells;

	  }
	  
	  /**
	   * Checks if it is visible from the static map
	   * 
	   * @param r0 initial row
	   * @param c0 initial column
	   * @param r1 final row
	   * @param c1 final column
	   * @return true if (r1,c1) is visible from (r0,c0) in the static map
	   */
	  public boolean hasStaticSight(int r0, int c0, int r1, int c1) {
		  if(this.automaton instanceof SpecificCellularAutomaton) {
			  SpecificCellularAutomaton myAutomaton = (SpecificCellularAutomaton) this.automaton;
			  
			  if(!myAutomaton.isVisible(new Location(r0, c0), new Location(r1, c1))) {
				  return false;
			  }
		  }
		  
		  return true;
	  }
	  
	  /**
	   * Checks if in line of sight (dynamic checking for changes in scenario)
	   * 
	   * @param r0 initial row
	   * @param c0 initial column
	   * @param r1 final row
	   * @param c1 final column
	   * @return true if in line of sight
	   */
	  public boolean hasDynamicSight(int r0, int c0, int r1, int c1) {
		  if(r0 == r1 && c0 == c1)
			  return true; // Same cell, always visible
		  
		  // Bresenham Algorithm
		  // Distance to cover
		  int dr = Math.abs(r1 - r0);
		  int dc = Math.abs(c1 - c0);
		  // Direction
		  int sr = 1;
		  int sc = 1;
		  if(r0 > r1)
			  sr = -1;
		  if(c0 > c1)
			  sc = -1;
		  // Error trajectory calculation
		  int error = dr - dc;
		  
		  int currentRow = r0;
		  int currentCol = c0;
		  
		  while(true) {
			  if(currentRow == r1 && currentCol == c1)
				  return true;
			  if((currentRow != r0 || currentCol != c0) && (automaton.isCellOccupied(currentRow, currentCol)))
				  return false;
			  
			  // Fix trajectory
			  int e2 = 2 * error;
			  if(e2 > -dc) {
				  error -= dc;
				  currentRow += sr;
			  }
			  
			  if(e2 < dr) {
				  error += dr;
				  currentCol += sc;
			  }
		  }
		  
	  }
	  
	  /**
	   * Checks both static and dynamic visibility
	   * 
	   * @param r0 initial row
	   * @param c0 initial column
	   * @param r1 final row
	   * @param c1 final column
	   * @return true if visible
	   */
	  public boolean hasSight(int r0, int c0, int r1, int c1) {
		  return hasStaticSight(r0, c0, r1, c1) 
				  && hasDynamicSight(r0, c0, r1, c1);
	  }

}

	
