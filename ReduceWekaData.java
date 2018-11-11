import java.io.*;
import java.util.regex.*;
//Reduce a weka data file using a reduct indices (excluding the class attribute) which:
//					are input from the command line 
//						or
//				    read from a file
//					    or 
//				    read from a roots_features file output by RandomForestFeatures.java
//input: a weka arff data file
//	 indices of a reduct (excluding the class attribute) e.g. 0,1,2,3
//	 (input options: 
//			 indices of a reduct is input from command line
//			 indices of a reduct is read from a file
//		         indices of a reduct is read from a roots_features file output by RandomForestFeatures.java
//	 		 indices start from 0
//			 indices start from 1
//			 operating system: linux or windows			 
//       )
//output: the reduced weka arff data file
//usage: Set the input options
//	 Compile ReduceWekaData
//       java ReduceWekaData <weka data file> <indices of a subset> <reduced weka data file>
public class ReduceWekaData {
	public static void main(String[] args) {
		Utility util;
		String class_path;
    	String weka_data_file;
    	String reduced_weka_data_file;
    	String reduct_indices = null;
    	String reduct_indices_file;
    	String [] reduct_indices_array;
    	int [] reduct_indices_array_int;
    	int index;
    	String reduct_indices2;
    	String [] all_attributes;
    	String class_index;
    	String os;
    	String roots_features_file;
    	boolean reduct_indices_input_from_command_line=false;
    	boolean reduct_indices_read_from_file=false;
    	boolean reduct_indices_read_from_roots_features_file=false;
    	boolean subset_indices_start_from_zero = false;
	boolean subset_indices_start_from_one = false;//feature indices output by weka start from 1
		/* input options */
    	//os="linux";
    	os="windows";
    	reduct_indices_input_from_command_line=true;
    	reduct_indices_read_from_file=false;
    	reduct_indices_read_from_roots_features_file=false;
    	subset_indices_start_from_zero = false;
    	//subset_indices_start_from_one = true;//feature indices output by weka start from 1
        /*end of input options */
	System.out.println("input options");
	if(reduct_indices_input_from_command_line)
		System.out.println("reduct indices input from command line");
	if(reduct_indices_read_from_file)
		System.out.println("reduct indices input from file");
        if(reduct_indices_read_from_roots_features_file)
		System.out.println("reduct indices read from roots feature file");
	if(subset_indices_start_from_zero)
		System.out.println("subset indices start from zero");
	if(subset_indices_start_from_one)
		System.out.println("subset indices start from one");
	System.out.println("operating system: "+os);
        if(subset_indices_start_from_zero == true && subset_indices_start_from_one == true)
        {
	  System.out.println("subset indices start from 0 or 1. both can not be true.");
          System.exit(-1);
        }
        else if(subset_indices_start_from_zero == false && subset_indices_start_from_one == false)
        {
	  System.out.println("subset indices start from 0 or 1. none of these are true.");
          System.exit(-1);
        }
        else if (subset_indices_start_from_zero == true)
           System.out.println("subset indices start from 0.");
        else
           System.out.println("subset indices start from 1.");
    	if (os.equals("windows"))
        	class_path = "C:\\Program Files\\Weka-3-6\\weka.jar";
    	else//linux
    		class_path = "/home/david/Downloads/weka-3-6-12/weka.jar";
    		
    	weka_data_file = args[0];
    	reduced_weka_data_file = args[2];
    	all_attributes = Utility.attributes_names_of_weka_arff_file(weka_data_file);
	class_index = Integer.toString(all_attributes.length);
	if (reduct_indices_input_from_command_line)
    	{
    	    reduct_indices = args[1];
	    if(subset_indices_start_from_zero)
	       reduct_indices2 = Utility.convert_subset_indices_to_weka_attributes_indices(reduct_indices);
    	    else //subset indices start from 1
               reduct_indices2 = reduct_indices;
            util = new Utility();
  	    util.reduce_weka_arff_file(reduct_indices2,class_path,os,class_index,weka_data_file,reduced_weka_data_file); 	   
    	}
    	else if(reduct_indices_read_from_file)
    	{
    	   reduct_indices_file = args[1];
    	   reduct_indices = Utility.read_reduct_indices(reduct_indices_file);
	   if(subset_indices_start_from_zero)   	
	   	reduct_indices2 = Utility.convert_subset_indices_to_weka_attributes_indices(reduct_indices);
	   else //subset indices start from 1
		reduct_indices2 = reduct_indices;      	
	   util = new Utility();
 	   util.reduce_weka_arff_file(reduct_indices2,class_path,os,class_index,weka_data_file,reduced_weka_data_file);	   
    	}
        else if(reduct_indices_read_from_roots_features_file)
        { //read the reduct indices from roots_features file output by RandomForestFeatures.java    		
        	roots_features_file = args[1];
        	reduct_indices = Utility.get_reduct_indices_from_roots_features_file(roots_features_file);
    		reduct_indices2 = Utility.convert_subset_indices_to_weka_attributes_indices(reduct_indices);
        	util = new Utility();
        	util.reduce_weka_arff_file(reduct_indices2,class_path,os,class_index,weka_data_file,reduced_weka_data_file);
        }   	   
        else
        {
        	System.out.println("Set a reduct indices option: read from command line, read from file, read from a roots features file.");
        	System.exit(-1);
        }
		System.out.println("reduced data is saved to "+reduced_weka_data_file);
	}
}
