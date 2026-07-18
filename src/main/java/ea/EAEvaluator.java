package ea;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;

import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ContinuousObjectiveFunction;
import es.uma.lcc.caesium.ea.fitness.OptimizationSense;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.Double2AccessDecoder;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ExitEvacuationProblem;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.Statistics;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.floorField.DijkstraStaticFloorFieldWithMooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.floorField.DijkstraStaticFloorFieldWithVonNewmanNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.floorField.ManhattanStaticFloorField;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.MooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.VonNeumannNeighbourhood;


import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.floorField.FloorField;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.Neighbourhood;


import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.pedestrian.Pedestrian;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Location;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.geometry._2d.Rectangle;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.configuration.SimulationConfiguration;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Access;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Domain;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Environment;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Shape;
import pedestrian.Attacker;
import pedestrian.Civilian;
import pedestrian.MultiPedestrianFactory;
import pedestrian.PedestrianWithVisionParameters;
import pedestrian.Police;
import pedestrian.PopulationConfig;
import pedestrian.PopulationGenerator;
import run.ExperimentTester;
import run.ExtraJsonParameterLoader;
import signs.EphimeralVisualSign;
import signs.EvacuationPlanSign;

public class EAEvaluator extends ContinuousObjectiveFunction{
	
	private double maxSimulationTime;
	
	// Scenario to avoid sign generation in blocked cells
	private Scenario baseScenario;
	
	// Basic data and parameters
	private ExitEvacuationProblem eep;
	private Double2AccessDecoder decoder;
	private Domain domain;
	private PopulationConfig populationConfig;
	private JsonObject weightJson;
	
	// Optimization attributes known
	private int nExits;
	private int nTempSigns;
	private int nPermSigns;
	private int nPolice;
	
	private int mapWidth;
	private int mapHeight;
	
	// Contains data about cell dimension, neighborhood and floor field
	private SimulationConfiguration simulation; 
	private double cellDimension;
	private final Function<Scenario, FloorField> floorField;
	private final Function<Scenario, Neighbourhood> neighborhood;
	
	// Objective function
	public EAEvaluator(ExitEvacuationProblem eep, int nExits, int nTempSigns, int nPermSigns, int nPolice, Domain domain, PopulationConfig populationConfig, JsonObject weightJson, SimulationConfiguration simulation){
		// Number of genes
		super(nExits + (nTempSigns * 2) + (nPermSigns * 2) + (nPolice * 2), 0, 1);
		
		maxSimulationTime = simulation.getDouble("timeLimit");
		
		this.eep = eep;
		decoder = new Double2AccessDecoder(eep);
		
		// Need to convert it to cells
		cellDimension = simulation.getDouble("cellularAutomatonParameters/cellDimension");
		mapWidth = (int) Math.ceil(eep.getWidth() / cellDimension);
		mapHeight = (int) Math.ceil(eep.getHeight() / cellDimension);
		
		this.domain = domain;
		this.populationConfig = populationConfig;
		this.weightJson = weightJson;
		
		// No easy access to this data, need to calculate it here again
		floorField =
				switch (simulation.getString("cellularAutomatonParameters/floorField")) {
					case "DijkstraStaticMoore" -> DijkstraStaticFloorFieldWithMooreNeighbourhood::of;
					case "DijkstraStaticVonNeumann" -> DijkstraStaticFloorFieldWithVonNewmanNeighbourhood::of;
					case "ManhattanStatic" -> ManhattanStaticFloorField::of;
					default -> throw new IllegalArgumentException("Invalid floor field in configuration");
				};
		neighborhood =
				switch (simulation.getString("cellularAutomatonParameters/neighborhood")) {
					case "Moore" -> MooreNeighbourhood::of;
					case "VonNeumann" -> VonNeumannNeighbourhood::of;
					default -> throw new IllegalArgumentException("Invalid neighbourhood in configuration");
				};
		
		this.nExits = nExits;
		this.nTempSigns = nTempSigns;
		this.nPermSigns = nPermSigns;
		this.nPolice = nPolice;
		
		// Scenario to avoid sign generation in blocked cells
		this.baseScenario = new Scenario.FromDomainBuilder(domain)
				.cellDimension(cellDimension)
				.floorField(floorField)
				.build();
		
	}
	
	@Override
	public OptimizationSense getOptimizationSense() {
		return OptimizationSense.MINIMIZATION;
	}
	
	// Stores lists of coordinates out of decoding and the penalty for repairing
	public record DecodedDesign(
		    List<Access> trialExits,
		    List<EphimeralVisualSign> trialTempVisSigns,
		    List<EvacuationPlanSign> trialPermVisSigns,
		    List<int[]> trialPolice,
		    double repairPenalty
		) {
		
	}
	
	// Stores simulation metrics
	public record SimulationMetrics(
			double meanEvacuationTime,
		    double medianEvacuationTime,
		    double meanSteps,
		    double medianSteps,
		    int civEvacuated,
		    int civDead,
		    int civTrapped,
		    int attAlive,
		    int polAlive,
		    double avgDistToExit,
		    double avgDistToAtt,
		    double avgDistToPol
		) {
		
	}
	
	// Stores simulation data and metrics not to repeat calculations and simulation
	public record SimulationResult(
			SpecificCellularAutomaton automaton,
			List<Pedestrian> crowd,
			SimulationMetrics metrics
			) {}
	
	public double getCellDimension() {
		return this.cellDimension;
	}
	
	/**
	 * Checks whether an exit can be completely placed in a valid location
	 * 
	 * @param location Location of the exit in the 1D representation of the boarder
	 * @return True if exit can be placed
	 */
	private boolean isExitValid(double location) {
		// Calculates rectangles generated to resemble exits
		List<Shape.Rectangle> rectangles = decoder.locationToRectangles(location);
		
		// Requires getting bounds of rectangles to get coordinates
		for(Shape.Rectangle r : rectangles) {
			var bounds = r.getAWTShape().getBounds2D();
			
			// Convert to cells
			int minRow = (int) (bounds.getMinY() / cellDimension);
			int minCol = (int) (bounds.getMinX() / cellDimension);
			// -0.001 to avoid rounding problems
			int maxRow = (int) ((bounds.getMaxY() - 0.001) / cellDimension);
			int maxCol = (int) ((bounds.getMaxX() - 0.001) / cellDimension);
			
			// Enforce values inside map limit
			minRow = Math.max(0, Math.min(mapHeight - 1, minRow));
			minCol = Math.max(0, Math.min(mapWidth - 1, minCol));
			maxRow = Math.max(0, Math.min(mapHeight- 1, maxRow));
			maxCol = Math.max(0, Math.min(mapWidth - 1, maxCol));
			
			// Validates exit by looking the status of the cell facing INWARDS 
			// BOTTOM
			if(minRow == 0) {
				for(int col = minCol; col <= maxCol; col++) {
					if(baseScenario.isBlocked(1, col)) {
						return false;
					}
				}
			}
			// RIGHT
			if(maxCol == mapWidth - 1) {
				for(int row = minRow; row <= maxRow; row++) {
					if(baseScenario.isBlocked(row, mapWidth - 2)) {
						return false;
					}
				}
			}
			// TOP
			if(maxRow == mapHeight - 1) {
				for(int col = minCol; col <= maxCol; col++) {
					if(baseScenario.isBlocked(mapHeight - 2, col)) {
						return false;
					}
				}
			}
			// LEFT
			if(minCol == 0) {
				for(int row = minRow; row <= maxRow; row++) {
					if(baseScenario.isBlocked(row, 1)) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Repairs the exit's 1D location
	 * 
	 * @param location Exit's original location
	 * @return New valid location
	 */
	private double repairExit(double location) {
		if(isExitValid(location)) {
			return location;
		}
		
		int maxRadius = (int) Math.ceil(eep.getPerimeterLength() / cellDimension);
		
		for(int r = 1; r < maxRadius; r++) {
			// Translates radius to meters
			double offset = r * cellDimension;
			// Checks that to the right is valid
			double right = (location + offset) % eep.getPerimeterLength();
			if(isExitValid(right)) {
				return right;
			}
			// Checks that to the left is valid (add the perimeter to avoid negative numbers)
			double left = (location - offset + eep.getPerimeterLength()) % eep.getPerimeterLength();
			if(isExitValid(left)) {
				return left;
			}
			
		}
		// If repair is not possible
		return location;
		
	}
	
	/**
	 * Repairs the coordinates of a sign in the map if placed in an occupied cell
	 * 
	 * @param initRow Proposed row
	 * @param initCol Proposed column
	 * @return Repaired coordinates
	 */
	private int[] repair(int initRow, int initCol) {
		// If possible placement, do nothing
		if(!baseScenario.isBlocked(initRow, initCol)) {
			return new int[] {initRow, initCol};
		}
		// Otherwise, find valid coordinates 
		int maxRadius = Math.max(mapHeight, mapWidth);
		
		for(int r = 1; r < maxRadius; r++) {
			for(int row = initRow - r; row <= initRow + r; row++) {
				for(int col = initCol - r; col <= initCol + r; col++) {
					if(row >= 0 && row < mapHeight && col >= 0 && col < mapWidth) {
						if(!baseScenario.isBlocked(row, col)) {
							// Return new coordinates
							return new int[] {row, col};
						}
					}
				}
			}
		}
		// Return original coordinates is map is full
		return new int[] {initRow, initCol};
	}
	
	/**
	 * Decodes an individual from its genome and applies lamarckian evolution if necessary
	 * @param i Individual to be decoded
	 * @return record of lists containing exits, signs and police coordinates
	 */
	public DecodedDesign decode(Individual i) {
		Genotype g = i.getGenome(); // Genes from EA
		int gene = 0; // Index of gene
		
		// Gene Translation
		// Exits 
		List<Access> trialExits = new ArrayList<>();
		int exitID = 0;
		for(int j = 0; j < nExits; j++) {
			int currentGene = gene;
			double geneInfo = (double) g.getGene(gene);
			gene++;
			// Calculates cell over perimeter of map, avoiding precision errors
			double location = Math.round(geneInfo * eep.getPerimeterLength() * 10) / 10.0;
			// Check if door is valid and repair it otherwise
			double repairedLocation = repairExit(location);
			// If repaired, update genome
			if(location != repairedLocation) {
				g.setGene(currentGene, repairedLocation / eep.getPerimeterLength());
			}
			
			// Converts location into real exit (access)
			trialExits.addAll(decoder.decodeAccess(repairedLocation, j, exitID));
			exitID = trialExits.size();
		}
		
		// Ephimeral Visual Signs
		List<EphimeralVisualSign> trialTempVisSigns = new ArrayList<>();
		for(int j = 0; j < nTempSigns; j++) {
			int currentGeneRow = gene;
			double geneRow = (double) g.getGene(gene);
			gene++;
			int currentGeneCol = gene;
			double geneCol = (double) g.getGene(gene);
			gene++;
			
			// Translate gene into actual coordinates
			// Change from scale 0-1 to real size map
			int row = (int) (geneRow * (mapHeight -1));
			int col = (int) (geneCol * (mapWidth -1));
			
			int[] repairedCoordinates = repair(row,col);
			
			// If repaired, update genome
			if(row != repairedCoordinates[0] || col != repairedCoordinates[1]) {
				g.setGene(currentGeneRow, (double) repairedCoordinates[0] / (mapHeight - 1));
				g.setGene(currentGeneCol, (double) repairedCoordinates[1] / (mapWidth - 1));
			}
			
			trialTempVisSigns.add(new EphimeralVisualSign(repairedCoordinates[0], repairedCoordinates[1]));
		}
		
		// Permanent Visual Signs
		List<EvacuationPlanSign> trialPermVisSigns = new ArrayList<>();
		for(int j = 0; j < nPermSigns; j++) {
			int currentGeneRow = gene;
			double geneRow = (double) g.getGene(gene);
			gene++;
			int currentGeneCol = gene;
			double geneCol = (double) g.getGene(gene);
			gene++;
					
			// Translate gene into actual coordinates
			// Change from scale 0-1 to real size map
			int row = (int) (geneRow * (mapHeight -1));
			int col = (int) (geneCol * (mapWidth -1));
			
			int[] repairedCoordinates = repair(row,col);
			
			// If repaired, update genome
			if(row != repairedCoordinates[0] || col != repairedCoordinates[1]) {
				g.setGene(currentGeneRow, (double) repairedCoordinates[0] / (mapHeight - 1));
				g.setGene(currentGeneCol, (double) repairedCoordinates[1] / (mapWidth - 1));
			}
			
			trialPermVisSigns.add(new EvacuationPlanSign(repairedCoordinates[0], repairedCoordinates[1]));
		}
		
		// Police
		List<int[]> trialPolice = new ArrayList<>();
		for(int j = 0; j < nPolice; j++) {
			int currentGeneRow = gene;
			double geneRow = (double) g.getGene(gene);
			gene++;
			int currentGeneCol = gene;
			double geneCol = (double) g.getGene(gene);
			gene++;
							
			// Translate gene into actual coordinates
			// Change from scale 0-1 to real size map
			int row = (int) (geneRow * (mapHeight - 1));
			int col = (int) (geneCol * (mapWidth - 1));
			
			int[] repairedCoordinates = repair(row,col);
			
			// If repaired, update genome
			if(row != repairedCoordinates[0] || col != repairedCoordinates[1]) {
				g.setGene(currentGeneRow, (double) repairedCoordinates[0] / (mapHeight - 1));
				g.setGene(currentGeneCol, (double) repairedCoordinates[1] / (mapWidth - 1));
			}
			
			trialPolice.add(repairedCoordinates);
							
		}
		
		// Passed 0 because penalty is not being used for lamarckian evolution
		return new DecodedDesign(trialExits, trialTempVisSigns, trialPermVisSigns, trialPolice, 0);
	}
	
	/**
	 * Generates automaton with its scenario, placing exits and signs. 
	 * @param dd record of lists that stores coordinates 
	 * @return automaton set 
	 */
	public SpecificCellularAutomaton setAutomaton(DecodedDesign dd) {
		// Exit placement
		var domainAcc = domain.getAccesses();
		domainAcc.clear();
		domainAcc.addAll(dd.trialExits);

		Scenario scenario = new Scenario.FromDomainBuilder(domain)
				.cellDimension(cellDimension)
			    .floorField(floorField)
			    .build();

		// Default Scenarios from classes
		// var scenario = random.bernoulli(0.75) ? RandomScenario.randomScenario() : Supermarket.supermarket();

		var cellularAutomatonParameters =
				new CellularAutomatonParameters.Builder()
			            .scenario(scenario) // use this scenario
			            .timeLimit(maxSimulationTime) // 2 minutes is time limit for simulation
			            .neighbourhood(neighborhood) // use Moore's Neighbourhood for automaton
			            .pedestrianReferenceVelocity(1.3) // fastest pedestrians walk at 1.3 m/s
			            .GUITimeFactor(8) // perform GUI animation x8 times faster than real time
			            .build();

		var automaton = new SpecificCellularAutomaton(cellularAutomatonParameters);
			    
		// Generate signs on exits
		GenerateSignOnExits(scenario, automaton);
			    
		// Other signs
		for(EphimeralVisualSign s : dd.trialTempVisSigns) {
			automaton.addSign(s);
		}
		for(EvacuationPlanSign s : dd.trialPermVisSigns) {
			automaton.addSign(s);
		}
		
		return automaton;
			    
	}
	
	/**
	 * Sets crowd in the automaton following decoded individual
	 * 
	 * @param dd Decoded individual information
	 * @param automaton Pre-loaded scenario with environment data
	 * @return List of pedestrians in the automaton
	 */
	public List<Pedestrian> setCrowd(DecodedDesign dd, SpecificCellularAutomaton automaton){
		List<PedestrianWithVisionParameters> civParams = new ArrayList<>();
	    for (int j = 0; j < populationConfig.numCivilians(); j++) {
	        civParams.add(buildParams(weightJson, 0));
	    }

	    List<PedestrianWithVisionParameters> attParams = new ArrayList<>();
	    for (int j = 0; j < populationConfig.numAttackers(); j++) {
	        attParams.add(buildParams(weightJson, 1));
	    }

	    List<PedestrianWithVisionParameters> polParams = new ArrayList<>();
	    for (int j = 0; j < populationConfig.numPolice(); j++) {
	        polParams.add(buildParams(weightJson, 2));
	    }
	    
	    // Create factory and set generator
	    MultiPedestrianFactory factory = automaton.getPedestrianFactory();
	    PopulationGenerator generator = new PopulationGenerator(factory, automaton);
		
	    // Set in automaton
	    List<Pedestrian> crowd = generator.generatePopulation(populationConfig, civParams, attParams, polParams);
	    int policeCount = 0;
	    for(Pedestrian p : crowd) {
	    	// Placing police following EA
	        if(p instanceof Police && policeCount < dd.trialPolice.size()) {
	        	int[] coorPolice = dd.trialPolice.get(policeCount);
	        	policeCount++;
	        	((Police)p).setRow(coorPolice[0]);
	        	((Police)p).setCol(coorPolice[1]);
	        }
	        automaton.addPedestrian(p);
	    }
	    
	    return crowd;
	}
	
	/**
	 * Calculates maps before run and then simulates
	 * @param automaton Automaton to be used
	 */
	public void simulate(SpecificCellularAutomaton automaton) {
		automaton.calculateVisibilityMap();
	    automaton.calculateDistanceMap();

	    //System.out.println("Simulating...");
	    
	    // ONLY FOR DEBUG PURPOSES
	    //automaton.runGUI();
	    //System.out.print("Iteration Completed!");
	    automaton.run();
	}
	
	/**
	 * Returns the metrics from the automaton
	 * 
	 * @param automaton Used automaton
	 * @param crowd Data of pedestrians
	 * @return Collection of metrics
	 */
	public SimulationMetrics getMetrics(SpecificCellularAutomaton automaton, List<Pedestrian> crowd) {
		// Fix mean and median if evacuees less than 2
	    double meanEvacuationTime = 0;
        double medianEvacuationTime = 0;
        double meanSteps = 0;
        double medianSteps = 0;
	    int civEvacuated = automaton.evacuationTimes().length;
	    Statistics statistics = null;
	    try {
	        if (civEvacuated >= 2) {
	            statistics = automaton.computeStatistics();
	            System.out.println(statistics);
	            meanEvacuationTime = statistics.meanEvacuationTime();
	            medianEvacuationTime = statistics.medianEvacuationTime();
	            meanSteps = statistics.meanSteps();
	            medianSteps = statistics.medianSteps();
	        } else if (civEvacuated == 1) {
	            // If only 1, mean and median are the same
	            meanEvacuationTime = automaton.evacuationTimes()[0];
	            medianEvacuationTime = meanEvacuationTime;
	        }
	    } catch (Exception e) {
	        System.out.println("Aviso: Caesium no pudo procesar las estadísticas nativas.");
	    }
	    			    
	    // Our extended metrics
	    int civDead = 0;
	    
	    // Lists to store Pedestrians for calculating distances for optimization
	    List<Civilian> civList = new ArrayList<Civilian>();
	    List<Attacker> attList = new ArrayList<Attacker>();
	    List<Police> polList = new ArrayList<Police>();
	    
	    // Count types of pedestrians
	    for(Pedestrian p : crowd) {
	    	if(p instanceof Civilian) {
	    		if(!((Civilian) p).isAlive()) {
	    			civDead++;
	    		}
	    		else { // Pedestrians alive
	    			if(!automaton.getScenario().isExit(p.getLocation())){ // Pedestrians trapped
	    				civList.add((Civilian) p);
	    			}
	    		}
	    	}
	    	else if (p instanceof Attacker) {
	    		if(((Attacker) p).isAlive()) {
	    			attList.add((Attacker) p);
	    		}
	    	}
	    	else if (p instanceof Police) {
	    		if(((Police) p).isAlive()) {
	    			polList.add((Police) p);
	    		}
	    	}
	    }
	    
	    int attAlive = attList.size();
	    int polAlive = polList.size();
	    int civTrapped = civList.size();
	    
	    // Distance calculation for trapped civilians (used in EA)
	    double distToExit = 0;
	    double distToAtt = 0;
	    double distToPol = 0;
	    double avgDistToExit = 0;
	    double avgDistToAtt = 0;
	    double avgDistToPol = 0;
	    
	    if(!civList.isEmpty()) {
	    	for(Civilian c : civList) {
	    		
	    		// Distance to closest exit
	    		double minDistToExit = Double.MAX_VALUE;
	    		for(Rectangle exit : automaton.getScenario().exits()) {
	    			int centerRow = exit.bottom() + (exit.height() / 2);
	                int centerCol = exit.left() + (exit.width() / 2);
	    			double dist = automaton.getDistance(c.getRow(), c.getColumn(), centerRow, centerCol);
	    			if(dist < minDistToExit) {
	    				minDistToExit = dist;
	    			}
	    		}
	    		distToExit += minDistToExit;
	    		
	    		// Distance to attacker
	    		if(!attList.isEmpty()) {
	    		double minDistToAtt = Double.MAX_VALUE;
	    		for(Attacker att : attList) {
	    			double dist = automaton.getDistance(c.getLocation(), att.getLocation());
	    			if(dist < minDistToAtt) {
	    				minDistToAtt = dist;
	    			}
	    		}
	    		distToAtt += minDistToAtt;
	    		
	    		}
	    		
	    		// Distance to attacker
	    		if(!polList.isEmpty()) {
	    		double minDistToPol = Double.MAX_VALUE;
	    		for(Police pol : polList) {
	    			double dist = automaton.getDistance(c.getLocation(), pol.getLocation());
	    			if(dist < minDistToPol) {
	    				minDistToPol = dist;
	    			}
	    		}
	    		distToPol += minDistToPol;
	    		
	    		}
	    	}
	    }
	    
	    // Average distances to use in optimization
	    if(!civList.isEmpty()) {
		    avgDistToExit = distToExit / civTrapped;
		    avgDistToAtt = distToAtt / civTrapped;
		    avgDistToPol = distToPol / civTrapped;
	    }
	    
	     return new SimulationMetrics(meanEvacuationTime, medianEvacuationTime, meanSteps, medianSteps, civEvacuated, civDead, civTrapped, attAlive, polAlive, avgDistToExit, avgDistToAtt, avgDistToPol);
	}
	
	/**
	 * Calculates fitness based on metrics of an individual following a cascaded fitness calculation
	 * @param metrics Metrics used for fitness calculation
	 * @return Fitness calculated following metrics
	 */
	public double getFitness(SimulationMetrics metrics) {
	    // Limit values
	    double maxDiameter = eep.getDiameter();
	    
	    double fitness = 0;
	    
	    // Time
	    double time = 0;
	    if(maxSimulationTime > 0) {
	    time = metrics.meanEvacuationTime / maxSimulationTime;
	    }
	    
	    fitness = time;
	    
	    // Distance to closest exit
	    double dist = 0;
	    if(maxDiameter > 0) {
	    dist = metrics.avgDistToExit * cellDimension / maxDiameter; // In meters
	    }
	    
	    // Divided by 10 to ensure it never reaches 1 of the following level
	    fitness = dist + time / 10.0;
	    
	    // Civilians trapped
	    fitness = metrics.civTrapped + fitness / 10.0;
	    
	    // Civilians killed
	    fitness = metrics.civDead + fitness / 10.0;

		return fitness;
	}
	
	/**
	 * This is the main function of the EA. It manages via other functions all the individual's data
	 * and returns the fitness value.
	 */
	@Override
	protected double _evaluate(Individual i) {
		// 1. Decode individual
		DecodedDesign dd = decode(i);
		
		// 2. Generates automaton based on scenario and coordinates
		SpecificCellularAutomaton automaton = setAutomaton(dd);
	    
	    // 3. Generates population of the simulation
	    List<Pedestrian> crowd = setCrowd(dd, automaton);
	    
	    // 4. Calculates precalculated maps and runs simulation
	    simulate(automaton);
	    
	    // 5. Get metrics from individual
	    SimulationMetrics metrics = getMetrics(automaton, crowd);
	    
	    // 6. Calculate fitness
	    double repairPenaltyWeight = 0.001; // Repair penalty importance less than trapped civilians
	    return getFitness(metrics) + dd.repairPenalty * repairPenaltyWeight;
	}
	
	/**
	   * Creates a permanent sign on the center of the exit rectangle, 
	   * so that pedestrians are attracted.
	   * 
	   * @param scenario scenario built
	   * @param automaton automaton used
	   */
	  private static void GenerateSignOnExits(Scenario scenario, SpecificCellularAutomaton automaton) {
		//System.out.println("Generating permanent signs on exits...");
	    for(Rectangle exit : scenario.exits()) {
	    	int centerRow = exit.bottom() + (exit.height() / 2);
	    	int centerCol = exit.left() + (exit.width() / 2);
	    	EvacuationPlanSign exitSign = new EvacuationPlanSign(centerRow, centerCol);
	    	automaton.addSign(exitSign);
	    	//System.out.println("Exit Sign generated at (" + centerRow + ", " + centerCol + ").");
	    }
	  }

	  /**
	   * Generates the parameter list for Pedestrians
	   * @param paramJson file with data
	   * @param PedestrianType adjust parameters to the selected type (0 -> Civ, 1 -> Att, 2 -> Pol)
	   * @return builder of parameters
	   */
	  private static PedestrianWithVisionParameters buildParams(JsonObject paramJson, int PedestrianType) {
	        double[][] matrix = ExtraJsonParameterLoader.loadSocialWeightsMatrix((JsonArray) paramJson.get("socialMatrix"));
	        double civilWeight = matrix[PedestrianType][0];
	        double attackerWeight = matrix[PedestrianType][1];
	        double policeWeight = matrix[PedestrianType][2];

	        double vision = ExtraJsonParameterLoader.readValues((JsonObject) paramJson.get("visionRadius"));
	        double attack = ExtraJsonParameterLoader.readValues((JsonObject) paramJson.get("attackRadius"));
	        double greedy = ExtraJsonParameterLoader.readValues((JsonObject) paramJson.get("greedyProb"));
	        double inertia = ExtraJsonParameterLoader.readValues((JsonObject) paramJson.get("inertiaWeight"));

	        return new PedestrianWithVisionParameters.Builder()
	            .civilianWeight(civilWeight)
	            .attackerWeight(attackerWeight)
	            .policeWeight(policeWeight)
	            .visionRadius(vision)
	            .attackRadius(attack)
	            .greedyProb(greedy)
	            .inertiaWeight(inertia)
	            .build();
	    }
	  
	  /**
	   * Returns data from specific individual's simulation
	   * 
	   * @param i Individual to be analyzed
	   * @return Simulation data
	   */
	  public SimulationResult getSimulation(Individual i) {
		  DecodedDesign dd = decode(i);
		  SpecificCellularAutomaton automaton = setAutomaton(dd);
		  List<Pedestrian> crowd = setCrowd(dd, automaton);
		  simulate(automaton);
		  SimulationMetrics metrics = getMetrics(automaton, crowd);
		  return new SimulationResult(automaton, crowd, metrics);
	  }
}
