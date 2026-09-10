package es.uma.lcc.caesium.pedestrian.evacuation.simulator.cellular.automaton.tests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ DynamicVisionTest.class, StaticVisibilityMapTest.class, VisibilityRadiusTest.class })
public class VisionTests {

}
