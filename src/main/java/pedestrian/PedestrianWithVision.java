package pedestrian;

import java.util.ArrayList;
import java.util.List;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.PedestrianParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;

public class PedestrianWithVision extends Pedestrian {
	
	
	//TODO: Incluir parámetro variable
	public static final int DEFAULT_VISION = 7;
	
	protected int visionRadius;
	
	public PedestrianWithVision(int row, int column, PedestrianParameters parameters, CellularAutomaton automaton) {
		super(row, column, parameters, automaton);
		this.visionRadius = DEFAULT_VISION;
	}
	
	public PedestrianWithVision(int row, int column, PedestrianParameters parameters, CellularAutomaton automaton, int visionRadius) {
		super(row, column, parameters, automaton);
		this.visionRadius = visionRadius;
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
		  
		  // Vision range following Manhattan distance
		  for(int r = row - visionRadius; r <= row + visionRadius; r++) {
			  for(int c = column - visionRadius; c <= column + visionRadius; c++) {
				  if(r >= 0 && r < rows && c >= 0 && c < cols) {
					  int distance = Math.abs(row-r) + Math.abs(column-c);
					  if(distance <= visionRadius) {
						  // Check obstacles
						  if(hasLineOfSight(row, column, r, c)) {
							  visibleCells.add(new Location(r, c));
						  }
					  }
				  }
			  }
		  }
		  return visibleCells;

	  }
	  
	  /**
	   * Checks if in line of sight (dynamic checking)
	   * 
	   * @param r0 initial row
	   * @param c0 initial column
	   * @param r1 final row
	   * @param c1 final column
	   * @return true if in line of sight
	   */
	  private boolean hasLineOfSight(int r0, int c0, int r1, int c1) {
		  if(r0 == r1 && c0 == c1)
			  return true; // Es la misma casilla, por lo tanto siempre es visible
		  
		  // Empleo del Algoritmo de Bresenham
		  // Deltas (distancia total a recorrer)
		  int dr = Math.abs(r1 - r0);
		  int dc = Math.abs(c1 - c0);
		  // Signo de la dirección
		  int sr = -1;
		  int sc = -1;
		  if(r0 > r1)
			  sr = 1;
		  if(c0 > c1)
			  sc = 1;
		  // Error para detectar desvío en la diagonal
		  int error = dr - dc;
		  
		  int currentRow = r0;
		  int currentCol = c0;
		  
		  while(true) {
			  if(currentRow == r1 && currentCol == c1)
				  return true;
			  if((currentRow != r0 || currentCol != c0) && automaton.getScenario().isBlocked(currentRow, currentCol))
				  return false;
			  
			  // Actualización del error para corregir la trayectoria
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

}

	
