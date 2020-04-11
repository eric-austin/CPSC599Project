import java.util.ArrayList;

public class Rule {
	//instance variables
	ArrayList<String> antecedent = null;
	String consequent = null;

	public Rule(ArrayList<String> ant, String con) {
		antecedent = new ArrayList<String>(ant);
		consequent = new String(con);
	}

	public boolean equivalent(Rule rule2) {  //check if consequent is the same, and check if all rules in the smaller rule occur in the larger rule
	    //if(!this.consequent.equals(rule2.consequent)) return false;

	    if(this.antecedent.size() > rule2.antecedent.size()) { // if this rule is bigger than the given rule check if  every element in given rule occurs in this rule
	        for(int i = 0; i < rule2.antecedent.size(); i++) {
	            if(!this.antecedent.contains(rule2.antecedent.get(i))) return false;
            }
	    } else { //if given rule is bigger or they are the same check if every element in this rule occurs in given rule
	        for(int i = 0; i < this.antecedent.size(); i++) {
	            if(!rule2.antecedent.contains(this.antecedent.get(i))) return false;
	        }
	    }
	    return true;
	}

	@Override
	public String toString() {
		return (antecedent.toString() + " -> " + consequent);
	}
}
