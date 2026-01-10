package es.uma.lcc.caesium.memetic.continuous.localsearch;

import java.util.List;

import es.uma.lcc.caesium.ea.operator.variation.VariationFactory;
import es.uma.lcc.caesium.ea.operator.variation.VariationOperator;
import es.uma.lcc.caesium.memetic.continuous.recombination.NelderMeadRecombination;

/**
 * Factory for operators of tha MA on continuous spaces
 * @author ccottap
 * @version 1.1
 */
public class MemeticContinuousVariationFactory extends VariationFactory {

	@Override
	public VariationOperator create (String name, List<String> pars) {
		VariationOperator op = null;
		
		switch (name.toUpperCase()) {
		case "DFLS":
			op = new DFOMLocalSearch(pars);
			break;
			
		case "NELDERMEAD":
			op = new NelderMeadRecombination(pars);
			break;
		
		default:
			op = super.create(name, pars);
		}
		
		return op;
	}

}
