import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.BitSet;

public class UnitTest {
	  	
   public static void main(String[] args) {
		String data_matrix [][];
		String data_file;
		String data_file_format;
		String first_line_contain_attributes_names;
		Binary variable;
		ReductsSearch rs;
		float gamma;	
		RSys rsys;
	    int class_index=0; 
	    String output_file;
	    FileWriter out=null;
	    HashSet<BitSet> approx_reducts = new HashSet<BitSet>();
	    String features [] = null;
	    String unreduced_data_weka_file;
	    String reduced_data_weka_file;
	    String subset_indices;
	    
	    unreduced_data_weka_file = args[0];
	    reduced_data_weka_file = args[1];
	    
	    subset_indices = Utility.get_indices_of_subset(unreduced_data_weka_file,reduced_data_weka_file);
	    System.out.println(subset_indices);
	    /*
		data_file = args[0];
	    data_file_format = args[1];
	    first_line_contain_attributes_names = args[2];
	    output_file = args[3];
	    
	    try{ 
	      out = new FileWriter(output_file);
	    }
	    catch(IOException e)
	    {
	    	System.out.println("IOException in main method");
	    	System.exit(-1);
	    }
	    
	    rs = new ReductsSearch("Binary",data_file,data_file_format,first_line_contain_attributes_names);
	   
	    
	    String csv_data_file = "/home/david/Dropbox/datasets/essential genes prediction/known essentiality genes/Training_All_GenesInfo_balanced1_chimerge_significance_level0.95_discretized.csv";
	    String weka_arff_file ="/home/david/Dropbox/datasets/essential genes prediction/known essentiality genes/training3.arff";
	    String weka_output_file ="/home/david/Dropbox/datasets/essential genes prediction/known essentiality genes/rank.info_gain3";
	    double rank [] = null;
	    
	    try
	    {
	      rs.create_weka_arff_file(csv_data_file, weka_arff_file);
	    }
		 catch(IOException e)
	    {
			  System.out.println("IOException in create_weka_arff_file.");
			  System.exit(-1);
		}
	    
	    String class_path = "/home/david/Downloads/weka-3-6-12/weka.jar";
	    String cmd ="java -Xmx2G -cp \""+class_path+"\" weka.attributeSelection.InfoGainAttributeEval -i \""+weka_arff_file+"\" > \""+weka_output_file+"\"";
	    try
	  	{
		   rs.run_system_command(cmd);
	  	}
	  	  catch(IOException e)
	  	{
	  		System.out.println("IOException in running features ranker on "+weka_arff_file);
			System.exit(-1);
	  	}
	  	  catch(InterruptedException e)
	  	{
	  		System.out.println("InterruptedException in running features ranker on "+weka_arff_file);
			System.exit(-1);
	  	}
	    try
	    {
	      rank = rs.get_features_rank_from_weka_output_file(weka_output_file, rank);
	    }
	  	catch(IOException e)
	  	{
	  	  System.out.println("IOException in calling get_features_rank_from_weka_output_file on "+weka_output_file);
		  System.exit(-1);
	  	}
	    for(int i=0; i<rank.length; i++)
	    		System.out.println(i+": "+rank[i]);
	    
	    //remove temporary files
	    
	  	  cmd = "rm \""+weka_arff_file+"\" \""+weka_output_file+"\"";
		  try
	  	  {
		   rs.run_system_command(cmd);
	  	  }
	  	  catch(IOException e)
	  	  {
	  		System.out.println("IOException in removing "+weka_arff_file+" and "+weka_output_file);
			System.exit(-1);
	  	  }
	  	  catch(InterruptedException e)
	  	  {
	  		System.out.println("InterruptedException removing "+weka_arff_file+" and "+weka_output_file);
			System.exit(-1);
	  	  }
		  System.out.println(weka_arff_file+" and "+weka_output_file+" are removed.");
		  */
	    
	   /*
		Utility util = new Utility();
	    
	    data_matrix = util.load_data_into_array(data_file,data_file_format,first_line_contain_attributes_names);
	 
		class_index = data_matrix[0].length-1;//class attribute is the last attribute
		
		
		//UnitTest ut = new UnitTest();
		try{
			features = util.attributes_names(data_file);
		}
		catch(IOException e)
	    {
	    	System.out.println("IOException in variables_names method");
	    	System.exit(-1);
	    }
		
		try
		{
			out.write(features[0]);
			for(int i=1; i<features.length; i++)
				out.write(","+features[i]);
			out.write("\n");
		}
		catch(IOException e)
	    {
	    	System.out.println("IOException in main method");
	    	System.exit(-1);
	    }	
		
		rsys = new RSys(data_matrix, data_matrix.length, data_matrix[0].length, class_index);
		BitSet allFeatures = new BitSet(rs.numberOfBits+1);
	     
	    for(int k = 0; k < rs.numberOfBits; k++)
	       	 allFeatures.set(k);
	    
	    try
	    {    
	    	rsys.getPositiveRegion(allFeatures, out);
	    	System.out.println("Positive region is saved to "+output_file);
	    }
	    catch (IOException e)
	    {
	    	System.out.println("IOException in getPostiveRegion.");
	    	System.exit(-1);
	    }
	    /* 
	    rs.gamma_setOfAllFeatures = rsys.relativeDependency(allFeatures);
	    */
	    /*
	    //Test convertToReducts
		//Create a BitSet with no bit to represent class attribute
		 
		BitSet approx_reduct = new BitSet(102);
																	  	
		approx_reduct.set(6);
		approx_reduct.set(8);
		approx_reduct.set(78);
		approx_reduct.set(10);
		approx_reduct.set(1);
		approx_reduct.set(11);
		approx_reduct.set(12);
		approx_reduct.set(9);
		
		BitSet approx_reduct2 = new BitSet(102);
		
		for(int k = 0; k < rs.numberOfBits-1; k++)
	    {
			approx_reduct2.set(k);
	    }
				
		approx_reducts.add(approx_reduct);
		approx_reducts.add(approx_reduct2);
		
		*/
  
	    //Test RanddomForwardSearch and RandomBackwardElimination
	    //Create a BitSet with a bit to represent class attribute
		/*BitSet approx_reduct = new BitSet(103);
		  														     
		  	
		approx_reduct.set(6);
		approx_reduct.set(8);
		approx_reduct.set(78);
		approx_reduct.set(10);
		approx_reduct.set(1);
		approx_reduct.set(11);
		approx_reduct.set(12);
		approx_reduct.set(9);
		*/
		/*
		HashSet<BitSet> reducts = new HashSet<BitSet>();
		BitSet reduct =null;
		reduct = util.RandomForwardSearch(rs, rsys,allFeatures);
		System.out.println(reduct);
	    reduct = util.RandomBackwardElimination(rs,rsys,allFeatures); 
	    System.out.println(reduct);
	    //reducts = util.convertToReducts(rs, rsys, approx_reducts, reducts, 5);
		//System.out.println(reducts);
	    */
	    
	}
}
