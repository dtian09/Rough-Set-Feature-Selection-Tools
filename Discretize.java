import java.io.*;
import java.util.regex.*;
import java.util.*;
//Discretize a continuous data set using the cuts of a discrete data set
//assumption: the discrete data set has identical attributes and attributes values as the continuous data set
//
//input: a continuous data file in weka format, csv format or space delimited format
//         a discrete weka data file
//output: a discrete data file in weka format using the cuts of the input discrete data file
//
//running command: java -cp . Discretize "/home/david/Dropbox/datasets/essential genes prediction/test set/93 features data/new_lethal_new_viable_genes_not_in_train_set.arff" weka "/home/david/Dropbox/datasets/essential genes prediction/train set/93 features data/new_lethal_new_viable_balanced_discretized_sig_level=0_99.arff" "/home/david/Dropbox/datasets/essential genes prediction/test set/93 features data/new_lethal_new_viable_genes_not_in_train_set_discretized_by_cuts_sig_level=0_99.arff" 

public class Discretize {

	public final static void main(String[] args)
	{
	 String continuous_data_file;
	 String discrete_data_weka_file;
	 String output_file;
	 Utility util;
	 String [][] data=null;
	 String data_file_format;
	 
	 continuous_data_file = args[0];
	 data_file_format = args[1];//format of input continuous data file. Formats are 'weka', 'csv' or 'space'
	 discrete_data_weka_file = args[2];
	 output_file = args[3];

	 util = new Utility();
	 
	 if(data_file_format.equals("weka"))
	 {
		 try{
		     data = util.load_weka_data_into_array(continuous_data_file);
		 }
	 	 catch(IOException e)
	 	{
	 		 System.out.println("IOException thrown by load_weka_data_into_array");
	 		 System.exit(-1);
	 	}
	 }
	 else if(data_file_format.equals("csv")) 
	 	 data = util.load_data_into_array(continuous_data_file, "csv", "1");
	 else if(data_file_format.equals("space")) 
	 	 data = util.load_data_into_array(continuous_data_file, "space", "1");
	 else
	 {
		  System.out.println("invalid file format specified\n valid format: weka, csv or space\n");
		  System.exit(-1);
	 }
	 try
	 {
		 discretizeUsingCutsOfWekaDiscreteFile(data,discrete_data_weka_file,output_file);
	 }
	 catch(IOException e)
	 {
		 System.out.println("IOException in discretizeUsingCutsOfWekaDiscreteFile");
		 System.exit(-1);
	 }
	 System.out.println("Discretization done.");
   }

    static void discretizeUsingCutsOfWekaDiscreteFile(String [][] data, String discrete_data_weka_file,String output_file) throws IOException
    {//Discretize a continuous data file using the cuts in a discrete data file in weka .arff format.
      //
      //Condition for discretization: The continuous data file must have the same attributes as the ones in the weka discrete data file
     //format of discrete weka file
     //
     //@attribute Exon_Count {'\'(-inf-1)\'','\'[1-14)\'','\'[14-26)\'','\'[26-27)\'','\'[27-50)\'','\'[50-54)\'','\'[54-inf)\''}

    	Pattern p;
    	Pattern p2;
    	BufferedReader in;
    	FileWriter out;
    	String line;
    	Matcher m;
    	Matcher m2;
    	String attr;
    	String intervals;
    	String [] intervalsArray;
    	HashMap<Integer,ArrayList<String>> cuts;//key=attribute index, value=a list of discrete intervals
    	ArrayList<String> intervalsList;
    	int no_of_attrs;
    	int indx;
    	HashSet<Integer> indices_to_leave_out;

    	indices_to_leave_out = new HashSet<Integer>();
    	cuts = new HashMap<Integer,ArrayList<String>>();

    	p=Pattern.compile("^\\@attribute\\s+([\\w\\p{Punct}\\s]+)\\s+\\{([\\w\\p{Punct}]+)\\}$");
    	in= new BufferedReader(new FileReader(discrete_data_weka_file));
    	out = new FileWriter(output_file);
    	out.write("@relation discretized_data\n\n");
    	line = in.readLine();
    	no_of_attrs=0;
    	indx=0;
    	while(!line.equals("@data"))
    	{
    		m=p.matcher(line);
    		if(m.matches())
    		{
    			no_of_attrs++;
    			attr = m.group(1);
    			intervals = m.group(2);
    			intervalsArray = intervals.split(",");
    			intervalsList = new ArrayList<String>();
    			for(int i=0; i<intervalsArray.length;i++)
    			{
    				intervalsList.add(intervalsArray[i]);
    			}
    			cuts.put(indx,intervalsList);
    			out.write(line+"\n");
    			indx++;
    		}
    		line=in.readLine();
    	}
    	in.close();
    	if (data[0].length != no_of_attrs)
    	{
    		System.out.println("no. of attributes of continuous data: "+data[0].length+", no. of attributes of discrete data: "+no_of_attrs);
    		System.out.println("number of attributes in continuous data file != number of discrete attributes in the discrete data file");
    		System.exit(-1);
    	}
    	out.write("\n@data\n");
    	data = discretizeUsingCuts(data,cuts);
    	writeToFile(data,out);
    }

    static String [][] discretizeUsingCuts(String [][] data, HashMap<Integer,ArrayList<String>> cuts)
    { //Discretize a value iof a feature by trying each interval in turn from the leftmost interval to the rightmost interval of the feature
    	ArrayList<String> intervalsList;
    	String interval;
    	Pattern p;
    	Pattern p2;
    	Pattern p3;
    	Pattern p4;
    	Matcher m;
    	Matcher m2;
    	Matcher m3;
    	Matcher m4;
        Double val=0d;
        Double cut;
        Double cut2;
        boolean discretized;

    	p = Pattern.compile("^.*\\(-inf-([-\\d\\.]+)\\).*$"); //pattern: '\'(-inf-1)\''
    	p2 = Pattern.compile("^.*\\[([-\\d\\.]+)-([-\\d\\.]+)\\).*$");	//pattern: '\'[1-14)\''
    	p3 = Pattern.compile("^.*\\[([-\\d\\.]+)-inf\\).*$"); //pattern: '\'[54-inf)\''
    	for(int j=0; j<data[0].length-1; j++)//leave out the class attribute in the last column
    		for(int i=0; i<data.length; i++)
    		{
    				discretized = false;
    				try
    				{
    					val = Double.parseDouble(data[i][j]);
    				}
    				catch(NumberFormatException e)
    				{
    					System.out.println(data[i][j]+" at "+i+", "+j+" is not a real number.");
    					System.exit(-1);
    				}
    				intervalsList = cuts.get(j);
    				for(int k=0; k<intervalsList.size(); k++)
    				{
    					interval = intervalsList.get(k);
    					m = p.matcher(interval);
    					m2 = p2.matcher(interval);
    					m3 = p3.matcher(interval);
    					if (m.matches())
    					{
    						cut = Double.parseDouble(m.group(1));
    						if (val < cut)
    						{
    							data[i][j] = interval;
    							discretized = true;
    							break;
    						}
    					}
    					else if(m2.matches())
    					{
    						cut = Double.parseDouble(m2.group(1));
    						cut2 = Double.parseDouble(m2.group(2));
    						if (val >= cut &&  val < cut2)
    						{
    							data[i][j] = interval;
    							discretized = true;
    							break;
    						}
    					}
    					else if(m3.matches())
    					{
    						cut = Double.parseDouble(m3.group(1));
    						if (val >= cut)
    						{
    							data[i][j] = interval;
    							discretized = true;
    							break;
    						}
    					}
    					else
    						System.out.println(interval+" does not match any patterns");
    				}
    				if (!discretized)
    					System.out.println("attribute: "+j+", value: "+val+" is undiscretized.");
    		}
    	return data;
    }

    static void writeToFile(String [][]data, FileWriter out) throws IOException
    {
    	for(int i=0; i<data.length; i++)
    	{
    		for(int j=0; j<data[0].length-1;j++)
    		{
    			out.write(data[i][j]+",");
    		}
    	    out.write(data[i][data[0].length-1]+"\n");
    	}
    	out.close();
    }

}
