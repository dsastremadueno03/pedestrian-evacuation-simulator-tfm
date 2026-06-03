package ea;

import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ContinuousObjectiveFunction;
import es.uma.lcc.caesium.ea.fitness.OptimizationSense;

public class EAEvaluator extends ContinuousObjectiveFunction{
	
	// Optimization attributes known
	private int nExits;
	private int nTempSigns;
	private int nPermSigns;
	private int nPolice;
	
	// Objective function
	public EAEvaluator(int nExits, int nTempSigns, int nPermSigns, int nPolice){
		// Number of genes
		super(nExits + (nTempSigns * 2) + (nPermSigns * 2) + (nPolice * 2), 0, 1);
		
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
		// TODO Auto-generated method stub
		return 0;
	}
	
}
