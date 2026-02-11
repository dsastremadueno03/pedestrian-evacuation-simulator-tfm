package tests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ EphimeralSignTest.class, PermanentSignTest.class })
public class SignTests {

}
