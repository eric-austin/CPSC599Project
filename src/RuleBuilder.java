
import java.io.BufferedWriter;
import java.io.FileWriter;

import weka.classifiers.trees.J48;
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
			String[] options = weka.core.Utils.splitOptions("-M 100");
			tree.setOptions(options);
			tree.buildClassifier(data);
			String treeVisual = tree.toString();
			System.out.println(treeVisual);
			//System.out.println("Size of string: " + treeVisual.getBytes().length/(Math.pow(1024, 2)) + "MB");
			//FileWriter writer = new FileWriter("output.txt");
			//System.out.println("tree written to file");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
