README

Included files:

README.txt
NCDB.arff
no_given.txt
some_given.txt
NCDB_Data_Dictionary.docx
weka.jar
599system.jar
Preprocessor.java
Rule.java
RuleBuilder.java
J48Mod.java
ClassifierTreeMod.java


Compiling the system:

We have included an executable 599system.jar file which will run the system. We have also included the java source files which can be compiled and run. The weka.jar file is needed to compile and run the system so you will need to add the path to the weka.jar to your system class path to be able to compile and run properly.

First add the file path to weka.jar to you class path variable 

export CLASSPATH=$CLASSPATH:/path/to/.../weka.jar

Then you can compile all of the java class files like so

javac *.java


Running the system:

The weka library is already packaged into the runnable jar, so to run the 599system.jar file you just need the command 

java -jar 599system.jar [dataset file] [target index] [given rules file] [min accuracy] [min occurrences]

The main method is found in the RuleBuilder class, so this is the class that needs to be run. Again, must have added the file path to weka.jar to your class path to be able to run. 

java RuleBuilder [dataset file] [target index] [given rules file] [min accuracy] [min occurrences]

The system takes five command line arguments, all of which are required.

The first argument is the file path to the dataset being used. We have provided NCDB.arff which is the dataset that has been transformed by our outside knowledge.

The second argument is the index of the feature that you want to target. NCDB.arff has 23 different features so the index can be 0-22 inclusive. The beginning of the NCDB.arff file has all of the different features.

The third argument is the file path to a file contain any given rules that you would like to provide to the system. We have included no_given.txt, an empty file to use when you have no given rules, and some_given.txt, an example file with some given rules for the C_CONF feature (target index 6). If you would like to create your own given rule file, each line is a rule. The rule should be a comma separated list of features set equal to some value. The last of these is the target. For example, the rule [VEHS_CAT = 1, V_ID = 01, P_ISEV = N] -> C_CONF = 02 would be written as 

VEHS_CAT = 1, V_ID = 01, P_ISEV = N, C_CONF = 02

The fourth argument is the minimum accuracy parameter. This should be a real number in the range 0.0 to 1.0. (0.9 should be high enough for most features to keep the rulesets of a manageable size).

The fifth argument is the minimum number of occurrences parameter. This should be a positive integer. (1000 should be high enough for most features to keep the rulesets of a manageable size).

The included NCDB_Data_Dictionary.docx and our interim report can be referenced to understand what the different features and values mean.


Output:

The system will print output both to standard out on the console and to a .txt file. The output file name will be a combination of the target feature, minimum accuracy parameter, and minimum occurrences parameter and will be written to the same directory from which you are running the system.

The output is a list of rules found by the system followed by any of the rules kept from the set of given rules. If there were any contradictions between the learned and given rules, these will be printed last.

*****
NOTE
*****
The Weka code throws a warning message about an illegal reflective access operation. This relates to something that is happening in the weka code itself. This doesn't crash the program or stop it working, it is just a warning. You can disregard this warning.

The program takes several minutes to run after that warning is thrown, give it some time to run and the output will come.