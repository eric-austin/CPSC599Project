
import java.util.ArrayList;
import java.util.Arrays;

import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class RuleBuilder {

	public static void main(String[] args) {
		J48 tree = new J48();
		
		String filepath = null;
		int target;
		
		//check whether correct number of command line args given
		if (args.length != 2) {
			System.err.println("Error! two command line arguments needed:\nFilepath to dataset and index of target attribute");
			return;
		} else {
			filepath = args[0];
			target = Integer.parseInt(args[1]);
			System.out.println("Dataset " + filepath);
			System.out.println("Target " + target);
		}
		
		//try opening file and importing dataset
		try {
			DataSource source = new DataSource(filepath);
			Instances data = source.getDataSet();
			//set attribute to target class based on user given input
			data.setClassIndex(target);
			//set up options for tree
			String[] options = weka.core.Utils.splitOptions("");
			tree.setOptions(options);
			//use J48 tree and given options to build decision tree
			tree.buildClassifier(data);
			
			System.out.println("Tree has " + tree.measureNumRules() + " rules.");
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
			ArrayList<Rule> ruleSet = buildRuleSet(lines, classAttString);
			
			System.out.println(treeString);
			System.out.println("We found " + ruleSet.size() + " rules.");
			for (Rule r : ruleSet) {
				System.out.println(r.toString());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static String[] getNodes(String[] lines) {
		String[] nodes = Arrays.copyOfRange(lines, 3, lines.length - 4);
		return nodes;
	}
	
	public static ArrayList<Rule> buildRuleSet(String[] lines, String classFeature) {
		//set up new arraylist representing ruleset
		ArrayList<Rule> ruleset = new ArrayList<Rule>();
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
					//get the feature and value and append to the end of antecedent
					tempAnt.add(fSplit[fSplit.length - 3] + " = " + fSplit[fSplit.length - 1]);
					//create new rule and add to ruleset
					ruleset.add(new Rule(tempAnt, classFeature + " = " + tSplit[1]));
					//remove last antecedent value since this cannot be used twice
					tempAnt.remove(currentDepth);
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
					//get the feature and value and append to the end of antecedent
					tempAnt.add(fSplit[fSplit.length - 3] + " = " + fSplit[fSplit.length - 1]);
					//create new rule and add to ruleset
					ruleset.add(new Rule(tempAnt, classFeature + " = " + tSplit[1]));
					//remove last antecedent value since this cannot be used twice
					tempAnt.remove(currentDepth);
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

}
