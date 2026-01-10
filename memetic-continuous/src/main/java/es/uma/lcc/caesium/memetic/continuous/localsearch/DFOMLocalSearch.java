package es.uma.lcc.caesium.memetic.continuous.localsearch;

import java.util.ArrayList;
import java.util.List;


import es.uma.lcc.caesium.dfopt.base.DerivativeFreeMethod;
import es.uma.lcc.caesium.dfopt.base.DerivativeFreeMethodFactory;
import es.uma.lcc.caesium.dfopt.base.DerivativeFreeObjectiveFunction;
import es.uma.lcc.caesium.dfopt.base.EvaluatedSolution;
import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.fitness.ContinuousObjectiveFunction;
import es.uma.lcc.caesium.ea.fitness.ObjectiveFunction;
import es.uma.lcc.caesium.ea.operator.variation.mutation.MutationOperator;

/**
 * Local search with a derivative-free optimization method
 * @author ccottap
 * @version 1.0
 */
public class DFOMLocalSearch extends MutationOperator {
	/**
	 * the internal solver
	 */
	private DerivativeFreeMethod solver;
	/**
	 * the objective function for the derivative-free method
	 */
	private DerivativeFreeObjectiveFunction dfof;
	/**
	 * the name of the internal solver
	 */
	private String method;
	/**
	 * the name of the internal solver configuration
	 */
	private String filename;
	
	/**
	 * Creates the operator. A filename with the configuration of the DF solver must be provided
	 * @param pars String representation of the mutation probability, filename of the LS configuration
	 */
	public DFOMLocalSearch(List<String> pars) {
		super(pars);
		solver = (new DerivativeFreeMethodFactory().create(pars.get(1)));
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
		Individual ind = parents.get(0).clone();
		ind.touch();
		Genotype g = ind.getGenome();
		int n = g.length();
		List<Double> point = new ArrayList<Double>(n);
		for (int i=0; i<n; i++) {
			point.add((double)g.getGene(i));
		}
		solver.setSeed(1);
		EvaluatedSolution sol = solver.run(point);
		for (int i=0; i<n; i++) {
			g.setGene(i, sol.point().get(i));
		}	
		ind.setFitness(sol.value());
		return ind;
	}

	@Override
	public String toString() {
		return "DFOM-LS(p=" + prob + ", " + method + ", " + filename + ")";
	}

}
