import java.util.ArrayList;

public class Rule {
	//instance variables
	ArrayList<String> antecedent = null;
	String consequent = null;

	public Rule(ArrayList<String> ant, String con) {
		antecedent = new ArrayList<String>(ant);
		consequent = new String(con);
	}
	
	@Override
	public String toString() {
		return (antecedent.toString() + " -> " + consequent);
	}
}
