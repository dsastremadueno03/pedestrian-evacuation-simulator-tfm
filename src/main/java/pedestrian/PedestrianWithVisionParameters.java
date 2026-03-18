package pedestrian;

public record PedestrianWithVisionParameters(
		double fieldAttractionBias, 
	    double crowdRepulsion, 
	    double velocityPercent,
	    double visionRadius,
	    double attackRadius,
	    double inertiaWeight,
	    double civilianWeight,
	    double policeWeight,
	    double attackerWeight,
	    double greedyProb) {
	
	
	
	public static final class Builder {
	    private double fieldAttractionBias = 100.0;
	    private double crowdRepulsion = 1.10;
	    private double velocityPercent = 1.0;
	   
	    private double visionRadius = 7.0;
	    private double attackRadius = 1.5;
	    private double inertiaWeight = 1;
	    private double civilianWeight = 0.5;
	    private double policeWeight = 0.0;
	    private double attackerWeight = -10.0;
	    private double greedyProb = 0.7;

	    public Builder() {}

	    public Builder fieldAttractionBias(double fieldAttractionBias) {
	      this.fieldAttractionBias = fieldAttractionBias;
	      return this;
	    }

	    public Builder crowdRepulsion(double crowdRepulsion) {
	      this.crowdRepulsion = crowdRepulsion;
	      return this;
	    }

	    public Builder velocityPercent(double velocityPercent) {
	        assert velocityPercent > 0 && velocityPercent <= 1.0 : "PedestrianParameters.velocityPercent: velocity percent " +
	            "must be in (0, 1.0)";
	        this.velocityPercent = velocityPercent;
	        return this;
	    }

	    public Builder visionRadius(double visionRadius) {
	      this.visionRadius = visionRadius;
	      return this;
	    }

	    public Builder attackRadius(double attackRadius) {
	      this.attackRadius = attackRadius;
	      return this;
	    }

	    public Builder inertiaWeight(double inertiaWeight) {
	      this.inertiaWeight = inertiaWeight;
	      return this;
	    }

	    public Builder civilianWeight(double civilianWeight) {
	      this.civilianWeight = civilianWeight;
	      return this;
	    }

	    public Builder policeWeight(double policeWeight) {
	      this.policeWeight = policeWeight;
	      return this;
	    }

	    public Builder attackerWeight(double attackerWeight) {
	      this.attackerWeight = attackerWeight;
	      return this;
	    }
	    
	    public Builder greedyProb(double greedyProb) {
		      this.greedyProb = greedyProb;
		      return this;
		}

	    public PedestrianWithVisionParameters build() {
	      return new PedestrianWithVisionParameters(
	          fieldAttractionBias, 
	          crowdRepulsion, 
	          velocityPercent,
	          visionRadius, 
	          attackRadius, 
	          inertiaWeight, 
	          civilianWeight, 
	          policeWeight, 
	          attackerWeight, 
	          greedyProb
	      );
	    }
	  }
	}
