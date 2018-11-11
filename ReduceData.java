import java.io.*;
//Reduce a csv data file using the indices of a reduct passed on command line
//input: a data file
//		 data file format (valid options: csv or space)
//		 first_line_contain_attributes_names (0 or 1
//  									      0: the first line of the data file does not contain the names of the attributes 
//                                     		  1: the first line of the data file contains the names of the attributes) 				  
//       indices of a reduct (excluding the class attribute) e.g. 0,1,2,3
//output: a reduced data file
//
//usage: java ReduceData <data file> <data file format (csv or space)> <first_line_contain_attributes_names (0 or 1)> <reduct indices e.g. 0,2,4,5,6> <reduced data file>

public class ReduceData {

	public static void main(String[] args) {
		String data_file=null;
	    String data_file_format=null;
	    String first_line_contain_attributes_names=null;
	    String reduced_data_file=null;
	    String [][] data_matrix=null;
	    String [] reduct_indices_array;
	    Utility util;
	    String reduct_indices;
	    String reduct_indices_file;
	    
	    data_file = args[0];
	    data_file_format = args[1];
	    first_line_contain_attributes_names = args[2];
	    //reduct_indices = args[3]; //indices of a reduct is on command line. indices of a reduct without class index e.g. 0,2,4,5 
	    reduct_indices_file = args[3];//indices of a reduct is read from a file
	    reduced_data_file = args[4];
	   
 	    reduct_indices = Utility.read_reduct_indices(reduct_indices_file);
	    util = new Utility();
	    data_matrix = util.load_data_into_array(data_file,data_file_format,first_line_contain_attributes_names);
	    reduct_indices_array = reduct_indices.split(",");
	    
	    if(first_line_contain_attributes_names.equals("0"))
	       util.reduce_csv_or_space_delimited_data(data_file,data_matrix,reduct_indices_array,data_file_format,reduced_data_file,"0");
	    else if(first_line_contain_attributes_names.equals("1"))
	    {
	       util.reduce_csv_or_space_delimited_data(data_file,data_matrix,reduct_indices_array,data_file_format,reduced_data_file,"1");
	    }
	    else
	    {
	    	System.out.println("invalid first_line_contain_attributes_names option specifed.\n valid options: 0 or 1");
			System.exit(-1);
	    }
	    System.out.println("reduced data is saved to "+reduced_data_file);
	 }	
}


