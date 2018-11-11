import java.io.*;
import java.util.regex.*;
//Reduce a weka data file using the features of another weka data file
//
//input: file1, a weka data file to be reduced
//		 file2, a reduced weka data file to be used to reduce file1
//		 out file
//		 operating system (windows or linux)
//output: the reduced file1 using the features in file2

public class ReduceWekaData2 {

	public static void main(String[] args) {
		Utility util;
		String class_path;
    	String file1;//a weka data file to reduce
    	String file2;//a reduced weka data file to be used to reduce file1
    	String reduced_file1;
    	String reduct_indices = null;
    	String [] all_attributes;
    	String class_index;
    	String os;
    	String reduct_indices2="";
    	
    	file1 = args[0];
    	file2 = args[1];
    	reduced_file1 = args[2];
    	os = args[3];
    	
    	if (os.equals("windows"))
        	class_path = "C:\\Program Files\\Weka-3-6-12\\weka.jar";
    	else//linux os
    		class_path = "/home/david/Downloads/weka-3-6-12/weka.jar";   	
    	
    	all_attributes = Utility.attributes_names_of_weka_arff_file(file1);
		class_index = Integer.toString(all_attributes.length);

    	reduct_indices = Utility.get_indices_of_subset(file1,file2);
    	//System.out.println(reduct_indices);
		reduct_indices2 = Utility.convert_subset_indices_to_weka_attributes_indices(reduct_indices);
    	util = new Utility();
  		util.reduce_weka_arff_file(reduct_indices2,class_path,os,class_index,file1,reduced_file1);
		System.out.println("reduced data is saved to "+reduced_file1);
	}
}
