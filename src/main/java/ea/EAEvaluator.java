package ea;

import java.util.ArrayList;
import java.util.List;

import com.github.cliftonlabs.json_simple.JsonObject;

import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ContinuousObjectiveFunction;
import es.uma.lcc.caesium.ea.fitness.OptimizationSense;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.Double2AccessDecoder;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ExitEvacuationProblem;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.CellularAutomatonParameters;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.SpecificCellularAutomaton;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.floorField.DijkstraStaticFloorFieldWithMooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.neighbourhood.MooreNeighbourhood;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.automata.scenario.Scenario;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Access;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Domain;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Environment;
import pedestrian.PopulationConfig;
import run.ExperimentTester;
import signs.EphimeralVisualSign;
import signs.EvacuationPlanSign;

public class EAEvaluator extends ContinuousObjectiveFunction{
	
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
	
	// Objective function
	public EAEvaluator(ExitEvacuationProblem eep, int nExits, int nTempSigns, int nPermSigns, int nPolice, Domain domain, PopulationConfig populationConfig, JsonObject weightJson){
		// Number of genes
		super(nExits + (nTempSigns * 2) + (nPermSigns * 2) + (nPolice * 2), 0, 1);
		
		this.eep = eep;
		decoder = new Double2AccessDecoder(eep);
		mapWidth = (int) eep.getWidth();
		mapHeight = (int) eep.getHeight();
		this.domain = domain;
		this.populationConfig = populationConfig;
		this.weightJson = weightJson;
		
		this.nExits = nExits;
		this.nTempSigns = nTempSigns;
		this.nPermSigns = nPermSigns;
		this.nPolice = nPolice;
	}
	
	@Override
	public OptimizationSense getOptimizationSense() {
		return OptimizationSense.MINIMIZATION;
	}
	
	@Override
	protected double _evaluate(Individual i) {
		Genotype g = i.getGenome(); // Genes from EA
		int gene = 0; // Index of gene
		
		// Gene Translation
		// Exits 
		List<Access> trialExits = new ArrayList<>();
		int exitID = 0;
		for(int j = 0; j < nExits; j++) {
			double geneInfo = (double) g.getGene(gene);
			gene++;
			// Calculates cell over perimeter of map, avoiding precision errors
			double location = Math.round(geneInfo * eep.getPerimeterLength() * 10) / 10.0;
			// Converts location into real exit (access)
			// Requires addAll since it might generate more than one exit at a time (corner case)
			trialExits.addAll(decoder.decodeAccess(location, j, exitID));
			exitID = trialExits.size();
		}
		
		// Ephimeral Visual Signs
		List<EphimeralVisualSign> trialTempVisSigns = new ArrayList<>();
		for(int j = 0; j < nTempSigns; j++) {
			double geneRow = (double) g.getGene(gene);
			gene++;
			double geneCol = (double) g.getGene(gene);
			gene++;
			
			// Translate gene into actual coordinates
			// Change from scale 0-1 to real size map
			int row = (int) (geneRow * (mapHeight -1));
			int col = (int) (geneCol * (mapWidth -1));
			
			trialTempVisSigns.add(new EphimeralVisualSign(row, col));
		}
		
		// Permanent Visual Signs
		List<EvacuationPlanSign> trialPermVisSigns = new ArrayList<>();
		for(int j = 0; j < nPermSigns; j++) {
			double geneRow = (double) g.getGene(gene);
			gene++;
			double geneCol = (double) g.getGene(gene);
			gene++;
					
			// Translate gene into actual coordinates
			// Change from scale 0-1 to real size map
			int row = (int) (geneRow * (mapHeight -1));
			int col = (int) (geneCol * (mapWidth -1));
					
			trialPermVisSigns.add(new EvacuationPlanSign(row, col));
		}
		
		// Police
		List<int[]> trialPolice = new ArrayList<>();
		for(int j = 0; j < nPolice; j++) {
			double geneRow = (double) g.getGene(gene);
			gene++;
			double geneCol = (double) g.getGene(gene);
			gene++;
							
			// Translate gene into actual coordinates
			// Change from scale 0-1 to real size map
			int row = (int) (geneRow * (mapHeight -1));
			int col = (int) (geneCol * (mapWidth -1));
							
			trialPolice.add(new int[]{row, col});
		}
		
		
		// SCENARIO
		// Exit placement
		var domainAcc = domain.getAccesses();
		domainAcc.clear();
		domainAcc.addAll(trialExits);

	    Scenario scenario = new Scenario.FromDomainBuilder(domain)
	        .cellDimension(domain.getWidth() / 110)
	        .floorField(DijkstraStaticFloorFieldWithMooreNeighbourhood::of)
	        .build();

	    // Default Scenarios from classes
	    // var scenario = random.bernoulli(0.75) ? RandomScenario.randomScenario() : Supermarket.supermarket();

	    var cellularAutomatonParameters =
	        new CellularAutomatonParameters.Builder()
	            .scenario(scenario) // use this scenario
	            .timeLimit(10 * 60) // 10 minutes is time limit for simulation
	            .neighbourhood(MooreNeighbourhood::of) // use Moore's Neighbourhood for automaton
	            .pedestrianReferenceVelocity(1.3) // fastest pedestrians walk at 1.3 m/s
	            .GUITimeFactor(8) // perform GUI animation x8 times faster than real time
	            .build();

	    var automaton = new SpecificCellularAutomaton(cellularAutomatonParameters);
	    
	    // Generate signs on exits
	    ExperimentTester.GenerateSignOnExits(scenario, automaton);
	    
	    // Other signs
	    for(EphimeralVisualSign s : trialTempVisSigns) {
	    	automaton.addSign(s);
	    }
	    for(EvacuationPlanSign s : trialPermVisSigns) {
	    	automaton.addSign(s);
	    }

		return 0;
	}
	
}
