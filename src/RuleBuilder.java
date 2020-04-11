
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class RuleBuilder {

	public static void main(String[] args) {
		J48Mod tree = new J48Mod();

		String filepath = null;
		String givenRulesPath = null;
		int target;

		double minOccurences = 50000.0;
		double minAccuracy = 0.8;

		//check whether correct number of command line args given
		if (args.length != 3) {
			System.err.println("Error! three command line arguments needed:\nFilepath to dataset, index of target attribute, and filepath to known rules");
			return;
		} else {
			filepath = args[0];
			target = Integer.parseInt(args[1]);
			givenRulesPath = args[2];
			System.out.println("Dataset " + filepath);
			System.out.println("Target " + target);
		}

		//try opening file and importing dataset
		try {
		    ArrayList<Rule> givenRules = readRules(givenRulesPath); //inputted rules
		    ArrayList<Rule> conflictingRules = new ArrayList<>();
			DataSource source = new DataSource(filepath);
			Instances data = source.getDataSet();
			//set attribute to target class based on user given input
			data.setClassIndex(target);
			//set up options for tree
			String[] options = weka.core.Utils.splitOptions("-M 200 -R -N 10");
			tree.setOptions(options);
			//use J48 tree and given options to build decision tree
			tree.buildClassifier(data);
			//get string representation of tree
			String treeString =  tree.toString();
			//break into the lines and remove header/footer to get only lines representing nodes
			String[] lines = treeString.split("\n");
			lines = getNodes(lines);

			//build rules from lines
			Attribute classAttribute = data.classAttribute();
			String classAttString = classAttribute.toString();
			//need to grab name of class attribute/feature
			classAttString = (classAttString.split(" "))[1];
			ArrayList<Rule> ruleSet = buildRuleSet(lines, classAttString, minOccurences, minAccuracy);

			//filter rules
			for(int i = 0; i < givenRules.size(); i++) {
			    for (int j = 0; j < ruleSet.size(); j++) {
			    	//if rules are equivalent then we want to keep shorter rule
			    	if (ruleSet.get(j).equivalent(givenRules.get(i)) && ruleSet.get(j).consequent.equals(givenRules.get(i).consequent)) {
			    		if (givenRules.get(i).antecedent.size() < ruleSet.get(j).antecedent.size()) {
			    			ruleSet.remove(j);
			    			ruleSet.add(j, givenRules.get(i));
			    		} else {
			    		    //conflictingRules.add(givenRules.get(i));
			    			givenRules.remove(i);
			    		}
			    	} else if (ruleSet.get(j).equivalent(givenRules.get(i))) {
			    	    conflictingRules.add(givenRules.get(i));
			    	    givenRules.remove(i);
			    	}
			    }
			}

			System.out.println(tree.toSummaryString());
			System.out.println("We found " + ruleSet.size() + " rules:");
			for (Rule r : ruleSet) {
				System.out.println(r.toString());
			}
			System.out.println("And kept these " + givenRules.size() + " given rules:");
			for (Rule r : givenRules) {
				System.out.println(r.toString());
			}
			if(conflictingRules.size() > 0) {
			    System.out.println("These " + conflictingRules.size() + " given rules conflicted with learned rules: ");
			    for(Rule r : conflictingRules) {
			        System.out.println(r.toString());
			    }
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String[] getNodes(String[] lines) {
		String[] nodes = Arrays.copyOfRange(lines, 3, lines.length - 4);
		return nodes;
	}

	public static ArrayList<Rule> buildRuleSet(String[] lines, String classFeature, double minOccurences, double minAccuracy) {
		//set up new arraylist representing ruleset
		ArrayList<Rule> ruleset = new ArrayList<Rule>();
		//split up the node lines for parsing
		String[][] splitNodes = new String[lines.length][];
		for (int i = 0; i < lines.length; i++) {
			splitNodes[i] = lines[i].split(" ");
		}
		//set up local vars to hold temp rule antecedent as we build it
		int currentDepth = 0;
		ArrayList<String> tempAnt = new ArrayList<>();
		//iterate through lines of tree
		for (String line : lines) {
			//if line represents a leaf we need to build a rule
			if (isLeaf(line)) {
				//check the depth of the leaf
				int leafDepth = getDepth(line);
				//if leaf depth is equal to current depth, we can add the next
				//feature value and the leaf value
				if (leafDepth == currentDepth) {
					//break up the line to get the last antecedent value and class value
					String feature = line.split(":")[0];
					String target = line.split(":")[1];
					//break up the strings to parse for the values we want
					String[] fSplit = feature.split(" ");
					String[] tSplit = target.split(" ");
					//check whether rule occurs often enough
					boolean include = false;
					String stats = tSplit[2];
					stats = stats.substring(1, stats.length() - 1);
					//if accuracy not 100% will contain a '/'
					if (stats.contains("/")) {
						String[] temp = stats.split("/");
						double hits = Double.parseDouble(temp[0]);
						double misses = Double.parseDouble(temp[1]);
						//if total occurances and accuracy are high enough set include to true
						if ((hits + misses) >  minOccurences && (hits / (hits + misses)) > minAccuracy) {
							include = true;
						}
					} else {
						//if stats does not contain '/' it is 100% accurate but may have too few occurences
						double hits = Double.parseDouble(stats);
						if (hits > minOccurences) {
							include = true;
						}
					}
					//if values good, then include rule
					if (include) {
						//get the feature and value and append to the end of antecedent
						tempAnt.add(fSplit[fSplit.length - 3] + " = " + fSplit[fSplit.length - 1]);
						//create new rule and add to ruleset
						ruleset.add(new Rule(tempAnt, classFeature + " = " + tSplit[1]));
						//remove last antecedent value since this cannot be used twice
						tempAnt.remove(currentDepth);
					}
				} else {
					//if leaf depth is <= to current depth, need to go back up tree
					//ie deleting parts of the antecedent until we are right below leaf
					while(currentDepth > leafDepth) {
						currentDepth--;
						tempAnt.remove(currentDepth);
					}
					//break up the line to get the last antecedent value and class value
					String feature = line.split(":")[0];
					String target = line.split(":")[1];
					//break up the strings to parse for the values we want
					String[] fSplit = feature.split(" ");
					String[] tSplit = target.split(" ");
					//check whether rule occurs often enough
					boolean include = false;
					String stats = tSplit[2];
					stats = stats.substring(1, stats.length() - 1);
					//if accuracy not 100% will contain a '/'
					if (stats.contains("/")) {
						String[] temp = stats.split("/");
						double hits = Double.parseDouble(temp[0]);
						double misses = Double.parseDouble(temp[1]);
						//if total occurances and accuracy are high enough set include to true
						if ((hits + misses) >  minOccurences && (hits / (hits + misses)) > minAccuracy) {
							include = true;
						}
					} else {
						//if stats does not contain '/' it is 100% accurate but may have too few occurences
						double hits = Double.parseDouble(stats);
						if (hits > minOccurences) {
							include = true;
						}
					}
					//if values good, then include rule
					if (include) {
						//get the feature and value and append to the end of antecedent
						tempAnt.add(fSplit[fSplit.length - 3] + " = " + fSplit[fSplit.length - 1]);
						//create new rule and add to ruleset
						ruleset.add(new Rule(tempAnt, classFeature + " = " + tSplit[1]));
						//remove last antecedent value since this cannot be used twice
						tempAnt.remove(currentDepth);
					}
				}
			} else {
				//if next path is not to a leaf then we check whether depth is the current, if not have to wind back
				//before adding and continuing
				int leafDepth = getDepth(line);
				if (leafDepth == currentDepth) {
					String[] split = line.split(" ");
					tempAnt.add(split[split.length - 3] + " = " + split[split.length - 1]);
					currentDepth++;
				} else {
					while (currentDepth > leafDepth) {
						currentDepth--;
						tempAnt.remove(currentDepth);
					}
					String[] split = line.split(" ");
					tempAnt.add(split[split.length - 3] + " = " + split[split.length - 1]);
					currentDepth++;
				}
			}
		}

		return ruleset;
	}

	public static int getDepth(String line) {
		int depth = 0;
		String[] strings = line.split(" ");
		for (String s : strings) {
			if (s.contains("|")) {
				depth++;
			}
		}
		return depth;
	}

	public static boolean isLeaf(String line) {
		return (line.contains(":"));
	}

	public static ArrayList<Rule> readRules(String filepath) throws FileNotFoundException {
	    ArrayList<Rule> rules = new ArrayList<>();
	    File input = new File(filepath);
	    Scanner in = new Scanner(input);
	    String line;
	    ArrayList<String> ant;
	    while(in.hasNext()) {
	        line = in.nextLine();
	        String[] splitLine = line.split(",");
	        ant = new ArrayList<>();
	        for(int i = 0; i < splitLine.length - 1; i++) {
	            ant.add(splitLine[i].trim()); //get rid of trailing/leading whitespace if there is any
	        }
	        rules.add(new Rule(ant, splitLine[splitLine.length -1].trim())); //each line is a rule - consequent is last element in line, antecedent is everything else
	    }
	    in.close();
	    return rules;
	}

}
