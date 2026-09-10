package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.ea;

import java.util.List;

import es.uma.lcc.caesium.dfopt.base.DerivativeFreeObjectiveFunction;
import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;

public class DFOEvaluator extends DerivativeFreeObjectiveFunction{
	
	private final EAEvaluator evaluator;
	private final int totalGenes;
	
	public DFOEvaluator(EAEvaluator evaluator) {
		this.evaluator = evaluator;
	    this.totalGenes = evaluator.getTotalGenes();
	}

	@Override
    protected double _evaluate(List<Double> sol) {
        assert sol.size() == totalGenes;
        
        Individual ind = new Individual();
        Genotype genome = new Genotype(totalGenes);
        for (int i = 0; i < totalGenes; i++) {
            genome.setGene(i, sol.get(i));
        }
        ind.setGenome(genome);
        return evaluator._evaluate(ind);
    }

	@Override
	public int getNumVariables() {
	    return totalGenes;
	}

	@Override
	public double getMinValue(int i) {
	    return 0.0;
	}

	@Override
	public double getMaxValue(int i) {
	    return 1.0;
	}

}
