import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Preprocessor {
    //instance variables
    private ArrayList<String> missingStrings;
    
    public static void main(String[] args) {
    	Preprocessor p = new Preprocessor();
    	p.createRandomPartition("clean_NCDB_1999_to_2017.csv");
    }

    public Preprocessor() {
        //create list of strings that indicate field missing
        this.missingStrings = new ArrayList<String>();
        this.missingStrings.add("UU");
        this.missingStrings.add("XX");
        this.missingStrings.add("X");
        this.missingStrings.add("U");
        this.missingStrings.add("QQ");
        this.missingStrings.add("Q");
        this.missingStrings.add("UUUU");
        this.missingStrings.add("XXXX");
    }

    public void removeIncompleteExamples(String filepath) {
        //variable to hold lines being read/written
        String line = null;
        String output= null;
        try {
            //set up file reader and writer
            FileReader fileReader = new FileReader(filepath);
            BufferedReader buffReader = new BufferedReader(fileReader);
            FileWriter fileWriter = new FileWriter("clean_" + filepath);
            BufferedWriter buffWriter = new BufferedWriter(fileWriter);
            //copy feature labels
            line = buffReader.readLine();

            String[] values = line.split(",");
            //String[] truncatedValues = Arrays.copyOf(values, values.length - 1); leave c case in to change
            String[] truncatedValues = updateLabels(values);

            output = String.join(",", truncatedValues);

            buffWriter.write(output);
            buffWriter.newLine();
            //iterate through lines in input file
            while ((line = buffReader.readLine()) != null) {
                //split line on comma
                values = line.split(",");
                //check whether any of the fields match a string indicating missing value
                boolean valid = true;
                for (int j = 0; j < values.length; j++) {
                    if (this.missingStrings.contains(values[j])) {
                        valid = false;
                        break;
                    }
                }
                //if example valid ie. no missing fields, write to output
                if (valid) {
                    //Convert fields (Incorporating Relevant Knowledge)
                    values = convertFields(values);

                    //truncatedValues = Arrays.copyOf(values, values.length - 1);
                    truncatedValues = Arrays.copyOf(values, values.length);

                    output = String.join(",", truncatedValues);
                    buffWriter.write(output);
                    buffWriter.newLine();
                }
            }
            //close streams
            buffReader.close();
            buffWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createRandomPartition(String filepath) {
        //variable to hold lines being read/written
        String line = null;
        Random rng = new Random();
        try {
            //create file reader and writers
            FileReader fileReader = new FileReader(filepath);
            BufferedReader buffReader = new BufferedReader(fileReader);
            BufferedWriter[] buffWriters = new BufferedWriter[10];
            for (int i = 0; i < 10; i++) {
                buffWriters[i] = new BufferedWriter(new FileWriter("partition" + i + ".csv"));
            }
            //copy labels to each partition
            line = buffReader.readLine();
            for (BufferedWriter b : buffWriters) {
                b.write(line);
                b.newLine();
            }
            //iterate through all the lines of input file
            while ((line = buffReader.readLine()) != null) {
                int i = rng.nextInt(10);
                buffWriters[i].write(line);
                buffWriters[i].newLine();
            }
            //close streams
            buffReader.close();
            for (BufferedWriter b : buffWriters) {
                b.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] updateLabels(String[] values) {
        values[values.length-1]="V_AGE_CAT";//change c case
        values[values.length-5-1]="P_AGE_CAT";//change p age
        values[values.length-17-1]="VEHS_CAT";//change c vehs
        values[values.length-19-1]="TIMEOFDAY_CAT";//change c hour

        return values;
    }

    private String[] convertFields(String[] values) {
        values = convertDriverAge(values);//Driver Age
        values = convertTime(values);//Time of Collision
        values = convertVehicleNumber(values);//Number of Vehicles Involved
        values = convertVehicleAge(values);//Age of Vehicle
        return values;
    }

    private String[] convertVehicleAge(String[] values) {
        if(values[values.length-9].equals("NNNN")) {
            values[values.length-1]="NNNN";
            return values;
        }
        int age = Integer.parseInt(values[0]) - Integer.parseInt(values[values.length-9]);
        if(age<=1) {
            values[values.length-1]="1";
        } else if(age<=4) {
            values[values.length-1]="2";
        } else if(age<=10) {
            values[values.length-1]="3";
        } else if(age<=20) {
            values[values.length-1]="4";
        } else if(age<=40) {
            values[values.length-1]="5";
        } else if(age>=41) {
            values[values.length-1]="6";
        }
        return values;
    }

    private String[] convertVehicleNumber(String[] values) {

        int num = Integer.parseInt(values[values.length-17-1]);
        if(num==1) {
            values[values.length-17-1]="1";
        } else if(num==2) {
            values[values.length-17-1]="2";
        } else if(num<=4) {
            values[values.length-17-1]="3";
        } else if(num<=8) {
            values[values.length-17-1]="4";
        } else if(num<=16) {
            values[values.length-17-1]="5";
        } else if(num<=32) {
            values[values.length-17-1]="6";
        } else if(num<=64) {
            values[values.length-17-1]="7";
        } else if(num>=65) {
            values[values.length-17-1]="8";
        }
        return values;
    }

    private String[] convertTime(String[] values) {
        String month = values[1];
        int hour = Integer.parseInt(values[3]);

        int a = 0;
        int b = 0;
        if(month=="1") {// +1 if daylight savings
            a=8;
            b=16;
        } else if(month=="2") {
            a=7;
            b=17;
        } else if(month=="3") {
            a=6+1;
            b=18+1;
        } else if(month=="4") {
            a=5+1;
            b=19+1;
        } else if(month=="5") {
            a=4+1;
            b=19+1;
        } else if(month=="6") {
            a=3+1;
            b=20+1;
        } else if(month=="7") {
            a=4+1;
            b=20+1;
        } else if(month=="8") {
            a=4+1;
            b=19+1;
        } else if(month=="9") {
            a=5+1;
            b=18+1;
        } else if(month=="10") {
            a=6+1;
            b=17+1;
        } else if(month=="11") {
            a=7;
            b=16;
        } else {
            a=8;
            b=16;
        }

        if(hour==a || hour== a+1 ) {
            values[3]="sunRise";
        } else if(hour > a+1 && hour < b ) {
            values[3]="midday";
        } else if (hour==b || hour == b+1) {
            values[3]="sunSet";
        } else {
            values[3]="night";
        }
        return values;
    }

    private String[] convertDriverAge(String[] values) {
        if(values[values.length-5-1].equals("NN")) {
            return values;
        }
        int age = Integer.parseInt(values[values.length-5-1]);
        if(age<=13) {
            values[values.length-5-1]="1";
        } else if(age<=15) {
            values[values.length-5-1]="2";
        } else if(age<=17) {
            values[values.length-5-1]="3";
        } else if(age<=20) {
            values[values.length-5-1]="4";
        } else if(age<=24) {
            values[values.length-5-1]="5";
        } else if(age<=34) {
            values[values.length-5-1]="6";
        } else if(age<=44) {
            values[values.length-5-1]="7";
        } else if(age<=54) {
            values[values.length-5-1]="8";
        } else if(age<=64) {
            values[values.length-5-1]="9";
        } else if(age<=69) {
            values[values.length-5-1]="10";
        } else if(age<=74) {
            values[values.length-5-1]="11";
        } else if(age<=79) {
            values[values.length-5-1]="12";
        } else if(age>=80) {
            values[values.length-5-1]="13";
        }
        return values;
    }
}
