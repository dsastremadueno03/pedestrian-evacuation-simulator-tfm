package es.uma.lcc.caesium.memetic.continuous.localsearch;

import java.util.List;

import es.uma.lcc.caesium.dfopt.base.DerivativeFreeObjectiveFunction;
import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ContinuousObjectiveFunction;

/**
 * Objective function for a derivative-free optimization method to be used as a local search operator
 * @author ccottap
 * @version 1.0
 */
public class DFOMObjectiveFunctionShell extends DerivativeFreeObjectiveFunction {
	/**
	 * the objective function of the MA
	 */
	private ContinuousObjectiveFunction obj;
	/**
	 * an internal individual for evaluating	
	 */
	private Individual ind;
	/**
	 * number of variables
	 */
	private int n;
	
	
	/**
	 * Creates the objective function
	 */
	public DFOMObjectiveFunctionShell(ContinuousObjectiveFunction obj) {
		this.obj = obj;
		n = obj.getNumVars();
		ind = new Individual();
		ind.setGenome(new Genotype(n));
	}

	@Override
	public int getNumVariables() {
		return obj.getNumVars();
	}

	@Override
	public double getMinValue(int i) {
		return obj.getMinVal(i);
	}

	@Override
	public double getMaxValue(int i) {
		return obj.getMaxVal(i);
	}

	@Override
	protected double _evaluate(List<Double> solution) {
		Genotype g = ind.getGenome();
		for (int i=0; i<n; i++) {
			g.setGene(i, solution.get(i));
		}
		ind.setGenome(g);
		return obj.evaluate(ind);
	}

}
