package es.uma.lcc.caesium.ea.problem.rastrigin;


import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ContinuousObjectiveFunction;
import es.uma.lcc.caesium.ea.fitness.OptimizationSense;

/**
 * Rastrigin function
 * @author ccottap
 * @version 1.0
 *
 */
public class Rastrigin extends ContinuousObjectiveFunction {
	/**
	 * function constant
	 */
	private static final double A = 10.0;
	
	/**
	 * Basic constructor
	 * @param i the number of variables and their range
	 * @param v range of variables ([-v, v])
	 */
	public Rastrigin(int i, double v) {
		super(i, -v, v);
	}
	
	


	@Override
	public OptimizationSense getOptimizationSense() {
		return OptimizationSense.MINIMIZATION;
	}



	@Override
	protected double _evaluate(Individual i) {
		Genotype g = i.getGenome();
		double c = A * numvars;
		for (int j=0; j<numvars; j++) {
			double v = (double)g.getGene(j);
			c += v * v - A * Math.cos(2.0*Math.PI*v);
		}
		return c;
	}



}
