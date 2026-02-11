package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.gui.Canvas;
import pedestrian.MultiPedestrianFactory;
import signs.EphimeralVisualSign;
import signs.EvacuationPlanSign;
import signs.Sign;

public class SpecificCellularAutomaton extends CellularAutomaton {
	
	private final MultiPedestrianFactory myFactory;
	
	// Matrix variables for static map
	private boolean[][][][] visibilityMatrix;
	private boolean visibilityCalculated = false;
	
	// Matrix for distance map
	private double[][] distanceMatrix;
	private boolean distanceCalculated = false;
	
	private List<Sign> signs = new ArrayList<>(); 
	
	public SpecificCellularAutomaton(CellularAutomatonParameters parameters) {
		super(parameters);
		this.myFactory = new MultiPedestrianFactory(this);
	}
	
	/**
	 * Adds a specific pedestrian
	 * 
	 * @param pedestrian pedestrian to be added
	 * @return true if possible to add
	 */
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
	
	public MultiPedestrianFactory getPedestrianFactory() {
		return myFactory;
	}
	
	public boolean isVisible(Location loc1, Location loc2) {
		if (!visibilityCalculated) {
			return checkLineOfSightWithObstacles(loc1.row(), loc1.column(), loc2.row(), loc2.column());
		}
		
		return visibilityMatrix[loc1.row()][loc1.column()][loc2.row()][loc2.column()];
	}
	
	public void addSign(Sign sign) {
		this.signs.add(sign);
	}
	
	public List<Sign> getSigns(){
		return this.signs;
	}
	
	/**
	   * Checks if in line of sight (static obstacles)
	   * 
	   * @param r0 initial row
	   * @param c0 initial column
	   * @param r1 final row
	   * @param c1 final column
	   * @return true if in line of sight
	   */
	private boolean checkLineOfSightWithObstacles(int r0, int c0, int r1, int c1) {
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
			  if((currentRow != r0 || currentCol != c0) && (scenario.isBlocked(currentRow, currentCol)))
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
	 * TO BE CALLED IN MAIN
	 * 
	 * Calculates static visibility map
	 */
	public void calculateVisibilityMap() {
		int rows = getRows();
		int cols = getColumns();
		
		System.out.println("Calculating static visibility map...");
		
		long start = System.currentTimeMillis();
		
		visibilityMatrix = new boolean[rows][cols][rows][cols];
		
		for(int r0 = 0; r0 < rows; r0++){
			for(int c0 = 0; c0 < cols; c0++){
				for(int r1 = 0; r1 < rows; r1++){
					for(int c1 = 0; c1 < cols; c1++){
						if(r1 == r0 && c1 == c0){
							visibilityMatrix[r0][c0][r1][c1] = true;
						} 
						else{
							// It is simmetrical
							if(r1 > r0 || (r1 == r0 && c1 > c0)){
								boolean visible = checkLineOfSightWithObstacles(r0, c0, r1, c1);
								visibilityMatrix[r0][c0][r1][c1] = visible;
								visibilityMatrix[r1][c1][r0][c0] = visible;
							}
						}
					}
				}
			}
			System.out.println("Calculating visibility map, please wait... (" + (Math.round((r0/(rows * 1.0)) * 100)) + "%)");
		}
		visibilityCalculated = true;
		long end = System.currentTimeMillis();
		System.out.println("Visibility map calculated in " + (end-start) + "ms.");
		
	}
	
	/**
	 * TO BE USED IN MAIN
	 * 
	 * Calculate distances between two cells in advance
	 */
	public void calculateDistanceMap() {
		int rows = getRows();
		int cols = getColumns();
		
		System.out.println("Calculating static distance map...");
		
		long start = System.currentTimeMillis();
		
		distanceMatrix = new double[rows][cols];
		
		for(int dr = 0; dr < rows; dr++){
			for(int dc = 0; dc < cols; dc++){
				distanceMatrix[dr][dc] = Math.sqrt(dr * dr + dc * dc);
			}
		}
		
		distanceCalculated = true;
		long end = System.currentTimeMillis();
		System.out.println("Distance map calculated in " + (end-start) + "ms.");
		
	}
	
	/**
	 * Calculates distance distance by looking in the matrix
	 * 
	 * @param r0 initial row
	 * @param c0 initial column
	 * @param r1 final row
	 * @param c1 final column
	 * @return euclidean distance for offset
	 */
	public double getDistance(int r0, int c0, int r1, int c1) {
		if(!distanceCalculated) { // Should not run this snippet
			return Math.sqrt((r1 - r0) * (r1 - r0) + (c1 - c0) * (c1 - c0));
		}
		
		int dr = Math.abs(r0 - r1);
		int dc = Math.abs(c0 - c1);
		return distanceMatrix[dr][dc];
	}
	
	/**
	 * Calculates distance distance by looking in the matrix
	 * 
	 * @param l1 initial position
	 * @param l2 final position
	 * @return euclidean distance for offset
	 */
	public double getDistance(Location l1, Location l2) {
		return getDistance(l1.row(), l1.column(), l2.row(), l2.column());
	}
	
	@Override
	void paint(Canvas canvas) {
		super.paint(canvas);
		
		Graphics2D g = canvas.graphics2D();
		Color originalColor = g.getColor();
		
		for(Sign sign : signs) {
			Location loc = sign.getLocation();
			
			if(sign instanceof EphimeralVisualSign) {
				g.setColor(Color.CYAN);
			} else if(sign instanceof EvacuationPlanSign) {
				g.setColor(Color.YELLOW);
			}
			
			g.fillRect(loc.column(), loc.row(), 1, 1);
			g.setColor(Color.BLACK);
			g.drawRect(loc.column(), loc.row(), 1, 1);
		}
		
		g.setColor(originalColor);
		
	}
	
}

