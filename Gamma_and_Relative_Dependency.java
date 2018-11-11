import java.util.BitSet;

//input: a data file
//		 data file format (valid options: "csv" or "space")
//		 first_line_contain_attributes_names (0 or 1
//  									      0: the first line of the data file does not contain the names of the attributes 
//                                     		  1: the first line of the data file contains the names of the attributes) 				  
//output: gamma of a feature subset
//			  relative dependency of the feature subset

public class Gamma_and_Relative_Dependency {
	public final static void main(String[]args) throws Exception
	{
	    String data_file;
	    String data_file_format;
	    String first_line_contain_attributes_names;
	    int class_index;	 
	    String [][] data_matrix=null;
	    Utility util;
	    RSys sys; 
	    
	    data_file = args[0];
	    data_file_format = args[1];
	    first_line_contain_attributes_names = args[2];
	  
	    util = new Utility();
	    
	    data_matrix = util.load_data_into_array(data_file,data_file_format,first_line_contain_attributes_names);
	    class_index = data_matrix[0].length-1;//class attribute is the last attribute

	    sys = new RSys(data_matrix, data_matrix.length, data_matrix[0].length, class_index);
	  
	    BitSet subset = new BitSet(data_matrix[0].length);
	    
	    //create a BitSet representing the set of all features excluding the class feature
	    for(int i=0; i<data_matrix[0].length-1;i++)
	        subset.set(i);
	    	    
	    System.out.println("gamma of "+subset.toString()+": "+sys.calculateGamma2(subset));
	    System.out.println("relative dependency of "+subset.toString()+": "+sys.relativeDependency(subset));
	    
	 }	
}
