package es.uma.lcc.caesium.memetic.continuous.recombination;

import java.util.ArrayList;
import java.util.List;

import es.uma.lcc.caesium.dfopt.base.DerivativeFreeMethodFactory;
import es.uma.lcc.caesium.dfopt.base.DerivativeFreeObjectiveFunction;
import es.uma.lcc.caesium.dfopt.base.EvaluatedSolution;
import es.uma.lcc.caesium.dfopt.neldermead.NelderMead;
import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ContinuousObjectiveFunction;
import es.uma.lcc.caesium.ea.fitness.ObjectiveFunction;
import es.uma.lcc.caesium.ea.operator.variation.recombination.RecombinationOperator;
import es.uma.lcc.caesium.ea.util.EAUtil;
import es.uma.lcc.caesium.memetic.continuous.localsearch.DFOMObjectiveFunctionShell;

/**
 * Recombination with the Nelder-Mead method
 * @author ccottap
 * @version 1.0
 *
 */
public class NelderMeadRecombination extends RecombinationOperator {
	/**
	 * the internal solver
	 */
	private NelderMead solver;
	/**
	 * the internal objective function for the derivative free method
	 */
	private DerivativeFreeObjectiveFunction dfof;
	/**
	 * the name of the internal solver configuration
	 */
	private String filename;
	/**
	 * arity of the operator
	 */
	private int arity;
	
	
	/**
	 * Creates the operator. Besides the application probability, the arity and the filename of a valid 
	 * Nelder-Mead configuration file must be provided.
	 * @param pars String representation of the mutation probability, arity, and filename of the configuration
	 */
	public NelderMeadRecombination(List<String> pars) {
		super(pars);
		arity = Integer.parseInt(pars.get(1));
		solver = (NelderMead) (new DerivativeFreeMethodFactory().create(pars.get(2)));
	}
	
	@Override
	public int getArity() {
		return arity;
	}
	
	@Override
	public void setObjectiveFunction(ObjectiveFunction obj) {
		super.setObjectiveFunction(obj);
		dfof = new DFOMObjectiveFunctionShell((ContinuousObjectiveFunction)obj);
		solver.setObjectiveFunction(dfof);
		solver.setVerbosityLevel(0);
	}

	@Override
	protected Individual _apply(List<Individual> parents) {
		int n = dfof.getNumVariables();
		List<List<Double>> simplex = new ArrayList<List<Double>>(n);
		for (Individual i: parents) {
			Genotype g = i.getGenome();
			List<Double> point = new ArrayList<Double>(n);
			for (int j=0; j<n; j++) {
				point.add((double)g.getGene(j));
			}
			simplex.add(point);
		}
		// completes the simplex if points are missing (we need n+1 points)
		for (int k=parents.size(); k<n; k++) {
			List<Double> point = new ArrayList<Double>(n);
			for (int j=0; j<n; j++) {
				double r = dfof.getMaxValue(j)-dfof.getMinValue(j);
				point.add(dfof.getMinValue(j) + EAUtil.random01()*r);
			}
			simplex.add(point);
		}
		solver.setSeed(1); // not needed at this point, since the method should be deterministic given a full initial simplex
		EvaluatedSolution sol = solver.run(simplex);
		Genotype g = new Genotype(n);
		for (int i=0; i<n; i++) {
			g.setGene(i, sol.point().get(i));
		}
		Individual ind = new Individual();
		ind.setGenome(g);
		ind.setFitness(sol.value());
		return ind;
	}

	@Override
	public String toString() {
		return "NelderMeadRecombination(p=" + prob + ", " + arity + ", " + filename + ")";
	}

}
