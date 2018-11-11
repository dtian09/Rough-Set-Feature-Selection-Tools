import java.io.*;
import java.util.BitSet;

//input: a data file
//		 data file format (valid options: "csv" or "space")
//		 first_line_contain_attributes_names (0 or 1
//  									      0: the first line of the data file does not contain the names of the attributes 
//                                     		  1: the first line of the data file contains the names of the attributes) 				  
//output: a reduct file (excluding the class attribute)
//format of reduct file: 0,1,2
//usage: java QuickReduct <data file> <data file format (csv or space)> <first_line_contain_attributes_names (0 or 1)> <reduct file>

public class QuickReduct {
	public final static void main(String[]args) throws Exception
	{
	    String data_file;
	    String data_file_format;
	    String first_line_contain_attributes_names;
	    String reduct_file=null;  
	    int class_index;	 
	    String [][] data_matrix=null;
	    String reduct_indices=null;
	    String [] reduct_indices_array;
	    Utility util;
	    RSys sys;
	    FileWriter out=null;
	    
	    data_file = args[0];
	    data_file_format = args[1];
	    first_line_contain_attributes_names = args[2];
	    reduct_file = args[3];//name of the file containing the reduct e.g. reduct.cleveland 
	    		
	    util = new Utility();
	    
	    data_matrix = util.load_data_into_array(data_file,data_file_format,first_line_contain_attributes_names);
	    class_index = data_matrix[0].length-1;//class attribute is the last attribute

	    sys = new RSys(data_matrix, data_matrix.length, data_matrix[0].length, class_index);
	       
	    reduct_indices=sys.quickReduct();
	    reduct_indices_array=reduct_indices.split(",");
	    try
        {
         out = new FileWriter(reduct_file);
        }
        catch(IOException e)
        {
         e.printStackTrace();
         System.exit(-1);
        }
	    out.write(reduct_indices_array[0]);
	    for(int i=1; i < reduct_indices_array.length-1; i++)//the class attribute index is excluded
	    {
          out.write(","+reduct_indices_array[i]);
	    }
        out.close();
	 }	
}
