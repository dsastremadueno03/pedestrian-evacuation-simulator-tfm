package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian;

import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;

/**
 * A class for creating different pedestrians for an automaton.
 *
 * @author Pepe Gallardo
 */
public class PedestrianFactory {
  private final CellularAutomaton automaton;

  public PedestrianFactory(CellularAutomaton automaton) {
    this.automaton = automaton;
  }
  
  // Generación de diferentes tipos de Pedestrian
  
  public Civilian getCivilian(int row, int column, PedestrianParameters parameters, Age ageGroup) {
	  validateCoordinates(row, column);
	  return new Civilian(row, column, parameters, automaton, ageGroup);
  }
  
  public Attacker getAttacker(int row, int column, PedestrianParameters parameters, int attackRange, int aggressiveness) {
	  validateCoordinates(row, column);
	  return new Attacker(row, column, parameters, automaton, attackRange, aggressiveness);
  }
  
  public Police getPolice(int row, int column, PedestrianParameters parameters, int attackRange) {
	  validateCoordinates(row, column);
	  return new Police(row, column, parameters, automaton, attackRange);
  }

  // --------------
  
  public Pedestrian getInstance(int row, int column, PedestrianParameters parameters) {
    return new Pedestrian(row, column, parameters, automaton);
  }

  public Pedestrian getInstance(Location location, PedestrianParameters parameters) {
    return getInstance(location.row(), location.column(), parameters);
  }
  
  private void validateCoordinates(int row, int column) {
	  assert row >= 0 && row < automaton.getRows() : "getInstance: invalid row";
	    assert column >= 0 && row < automaton.getColumns() : "getInstance: invalid column";
  }
  
}
