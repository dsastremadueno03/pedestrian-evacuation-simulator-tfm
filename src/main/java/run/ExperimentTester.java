package run;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

public class ExperimentTester {

	public static void main(String[] args) {
		
		String resultsCSV = "results.csv";
		
		try(PrintWriter w = new PrintWriter(new FileWriter(resultsCSV))){
			// Header
			w.println("id_experiment,civilians_exit,civilians_kill,civilians_alive,police_alive,killers_alive,total_turns,time_max");
			
			// Read JSON
			FileReader r = new FileReader("experiments.json");
			JsonObject jsonMain = (JsonObject) Jsoner.deserialize(r);
			JsonArray listOfExperiments = (JsonArray) jsonMain.get("experiments");
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

}
