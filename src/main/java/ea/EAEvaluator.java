package ea;

import java.util.ArrayList;
import java.util.List;

import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ContinuousObjectiveFunction;
import es.uma.lcc.caesium.ea.fitness.OptimizationSense;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.Double2AccessDecoder;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.ExitEvacuationProblem;
import es.uma.lcc.caesium.pedestrian.evacuation.simulator.environment.Access;
import signs.EphimeralVisualSign;
import signs.EvacuationPlanSign;

public class EAEvaluator extends ContinuousObjectiveFunction{
	
	private ExitEvacuationProblem eep;
	private Double2AccessDecoder decoder;
	
	// Optimization attributes known
	private int nExits;
	private int nTempSigns;
	private int nPermSigns;
	private int nPolice;
	
	private int mapWidth;
	private int mapHeight;
	
	// Objective function
	public EAEvaluator(ExitEvacuationProblem eep, int nExits, int nTempSigns, int nPermSigns, int nPolice){
		// Number of genes
		super(nExits + (nTempSigns * 2) + (nPermSigns * 2) + (nPolice * 2), 0, 1);
		
		this.eep = eep;
		decoder = new Double2AccessDecoder(eep);
		mapWidth = (int) eep.getWidth();
		mapHeight = (int) eep.getHeight();
		
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
			// Calculates cell over perimeter of map
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
		

		return 0;
	}
	
}
