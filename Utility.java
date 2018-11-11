import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.encodings.variable.Binary;
import jmetal.util.Configuration;
import jmetal.util.Ranking;

import java.util.BitSet;

//This class contains functions to do data file processing
//javac -cp .;..\bin Utility.java
//java -cp .;..\bin Utility

public class Utility {

 boolean a_reduct_found = false;
 
 void printRankedSolutionsToFile(ReductsSearch2 rs2, SolutionSet subsets, String resultsfile)
 {
  /* Print subsets to a file (features indices start from 0 and class attribute not included)
     Output format: 
     {0,2,4,5,6}	size:	5	fitness:	1.9	relative dependency:	1	mutual info:	1	subset size ratio:	0.25 
  	...
  */
  int n;
  SolutionSet solutions;
  HashSet <BitSet> solutionsPrinted = new HashSet<BitSet>();
  Iterator<Solution> iter;
  Solution solution;
  FileOutputStream fos;
  OutputStreamWriter osw; 
  BufferedWriter bw;
  Binary binary;
  Ranking ranking;
  String results;
  
  ranking = new Ranking(subsets);
  try{
      fos = new FileOutputStream(resultsfile);
      osw = new OutputStreamWriter(fos);
      bw  = new BufferedWriter(osw);
      bw.write("=== indices of subsets (indices start from 0) ===\n");
      n = ranking.getNumberOfSubfronts();
      for(int i=n-1; i > -1; i--)
      {
     	 solutions = ranking.getSubfront(i);
     	 iter = solutions.iterator();
     	 while(iter.hasNext()) 
     	 {
     		 solution = iter.next();
     		 binary = (Binary)solution.getDecisionVariables()[0];
     		 if(!solutionsPrinted.contains(binary.bits_))
     		 {
     			 solutionsPrinted.add(binary.bits_);
     			 //results = subset_indices(solution)+"\tsize:\t"+get_subset_size(binary)+"\tfitness:\t"+solution.toString()+"\trelative dependency:\t"+rs2.rs.gammas.get(binary)+"\ttotal info gain of subset:\t"+rs2.rs.totalscore(binary)+"\tsubset size ratio: "+rs2.rs.hash_subset_size_ratio.get(binary);
     			 results = subset_indices(solution)+"\tsize:\t"+get_subset_size(binary)+"\tfitness:\t"+solution.toString()+"\trelative dependency:\t"+rs2.rs.gammas.get(binary)+"\taverage info gain of subset:\t"+rs2.rs.totalscore(binary)/rs2.rs.get_subset_size(binary)+"\tsubset size ratio:\t"+rs2.rs.hash_subset_size_ratio.get(binary);     			 
     			 bw.write(results+"\n");
     		 }
     	 }
      }
      bw.close();
    }
    catch (IOException e) {
      Configuration.logger_.severe("Error accessing to the file");
      e.printStackTrace();
    }
 }
 
 void printSolutionsIndicesToFile(ReductsSearch2 rs2, SolutionSet subsets, String resultsfile)
 {
	 /* Print subsets to a file (features indices start from 1 and class attribute not included)
     Output format: 
     1,3,5,6,7
     2,4,6,8 
  	 ...
     */
  int n;
  SolutionSet solutions;
  HashSet <BitSet> solutionsPrinted = new HashSet<BitSet>();
  Iterator<Solution> iter;
  Solution solution;
  FileOutputStream fos;
  OutputStreamWriter osw; 
  BufferedWriter bw;
  Binary binary;
  Ranking ranking;
  String subset;
  
  ranking = new Ranking(subsets);
  try{
      fos = new FileOutputStream(resultsfile);
      osw = new OutputStreamWriter(fos);
      bw  = new BufferedWriter(osw);
      n = ranking.getNumberOfSubfronts();
      for(int i=0; i<n; i++)
      {
     	 solutions = ranking.getSubfront(i);
     	 iter = solutions.iterator();
     	 while(iter.hasNext()) 
     	 {
     		 solution = iter.next();
     		 binary = (Binary)solution.getDecisionVariables()[0];
     		 if(!solutionsPrinted.contains(binary.bits_))
     		 {
     			 solutionsPrinted.add(binary.bits_);
     			 subset = subset_indices2(solution);
     			 bw.write(subset+"\n");
     		 }
     	 }
      }
      bw.close();
    }
    catch (IOException e) {
      Configuration.logger_.severe("Error acceding to the file");
      e.printStackTrace();
    } 
 }
 
 int get_subset_size(Binary variable)
 {  //input: a Binary variable representing a subset
     //output: size of the subset
	  int subset_size=0;
	  for(int j = 0; j < variable.getNumberOfBits(); j++)
	  {
	    if(variable.bits_.get(j))
	        subset_size++;
	  }
	  return subset_size;
 }
 
 String subset_indices(Solution subset)
 {//return a String object representing a Solution object (indices start from 0)
 	Binary variable;
 	variable = ((Binary)subset.getDecisionVariables()[0]);      
 	return variable.bits_.toString();
 }
 
 String subset_indices2(Solution subset)
 {//return a String object representing a Solution object (indices start from 1)
 	Binary variable;
 	String subset_indices="";
 	int last_indx;
 	int i2;
 	variable = ((Binary)subset.getDecisionVariables()[0]);      
 	for(int i=0; i < variable.getNumberOfBits(); i++)
 		if(variable.bits_.get(i))
		{
 			i2=i+1;
 			subset_indices+=Integer.toString(i2)+",";			
		}
 	last_indx = subset_indices.length()-1;
 	return subset_indices.substring(0, last_indx);//remove the last comma
 }
 
 static String get_reduct_indices_from_roots_features_file(String roots_features_file)				
 {  
	 String reduct_indices=null;
	 Pattern p;
	 Pattern p2;
	 Matcher m;
	 Matcher m2;
	 BufferedReader in = null;
	 String line;

	 p=Pattern.compile("^indices of all features in random forest:$");
	 p2=Pattern.compile("^\\[(.+)\\]$");//reduct indices

	 try
	 {
		 in = new BufferedReader(new FileReader(roots_features_file));
	 }
	 catch(IOException e)
	 {
		 System.out.println("ioexception thrown by get_reduct_indices_from_roots_features_file");
		 System.exit(-1);
	 }
	 try
	 {
		 line = in.readLine();
		 m = p.matcher(line);
		 while(!m.matches())
		 {
			 line = in.readLine();
			 m = p.matcher(line);	
		 }
		 line = in.readLine();
		 m2 = p2.matcher(line);
		 if (m2.matches())
			 reduct_indices = m2.group(1);
	 }
	 catch(IOException e)
	 {
		 System.out.println("ioexception thrown by reading reduct indices file");
		 System.exit(-1);
	 }
	 try
	 {
		 in.close();
	 }
	 catch(IOException e)
	 {
		 System.out.println("ioexception thrown by closing reduct indices file");
		 System.exit(-1);
	 } 
	 return reduct_indices;
   }
 static String read_reduct_indices(String reduct_indices_file)
 {
	 BufferedReader in = null;
	 String reduct_indices = null;
	 
	 try{
		   in = new BufferedReader(new FileReader(reduct_indices_file));
		}
		catch(IOException e)
		{
			System.out.println("ioexception thrown by reading reduct indices file");
			System.exit(-1);
		}
		try{
				reduct_indices = in.readLine();							
		}
		catch(IOException e)
		{
			System.out.println("ioexception thrown by reading reduct indices file");
			System.exit(-1);
		}
		try{
		 in.close();
		}
		catch(IOException e)
		{
			System.out.println("ioexception thrown by closing reduct indices file");
			System.exit(-1);
		} 
	 return reduct_indices;
 }
static String convert_subset_indices_to_weka_attributes_indices(String reduct_indices)
{
	String [] reduct_indices2;
	String reduct_indices3="";
	int indx;

	reduct_indices2 = reduct_indices.split(",");
	for(int i=0; i<reduct_indices2.length;i++)
	{
		indx = Integer.parseInt(reduct_indices2[i].trim())+1;//indices start from 1 in weka. add 1 to indices starting from 0.
		if(i != reduct_indices2.length-1)
			reduct_indices3 += indx+",";
		else
			reduct_indices3 += Integer.toString(indx);
	}
	return reduct_indices3;
}

static String get_indices_of_subset(String original_data_weka_file, String reduced_data_weka_file)
{//input: an unreduced weka data file containing all the features of a dataset
  //		  a reduced weka data file containing some features of the dataset
  //output: the indices (in the unreduced weka data file) of the features (excluding the class index) in the reduced weka data file 
  String [] allattributes;
  String [] subsetattributes;
  String f=null;
  String subset_indices="";
  boolean feature_found=false;
  
 allattributes = attributes_names_of_weka_arff_file(original_data_weka_file);
 subsetattributes = attributes_names_of_weka_arff_file(reduced_data_weka_file);
 for(int i=0; i<subsetattributes.length-1;i++)
 {
	 f=subsetattributes[i];
	 feature_found=false;
	 for(int j=0; j<allattributes.length-1;j++)
	 {
		feature_found=true;
		 if (f.equals(allattributes[j]))
		 {
			 //System.out.println(j+": "+f);
			 if (i != subsetattributes.length-2)
				 subset_indices+=j+",";
			 else
				 subset_indices+=j;
			 break;
		 }
	 }
	 if(!feature_found)
		 System.out.println(f+" is not in the set of all features");
 }
  return subset_indices;
}

void reduce_weka_arff_file(String reduct_indices, String class_path, String os, String class_index, String weka_data_file, String reduced_weka_data_file)
{  //class_index starts from 1 (indices start from 1 in Weka)
	//java -cp "c:\program files\weka-3-6\weka.jar" weka.filters.unsupervised.attribute.Remove -R "1-4, 93" -V -i "C:\Users\David\Dropbox\datasets\essential genes prediction\train set\new_lethal_new_viable_balanced_discretized.arff" -o "C:\Users\David\Dropbox\datasets\essential genes prediction\train set\reduced.arff"
	String cmd;
	
	cmd = "java -Xmx1g -cp \""+class_path+"\" weka.filters.unsupervised.attribute.Remove -R \""+reduct_indices+","+class_index+"\" -V -i \""+weka_data_file+"\" -o \""+reduced_weka_data_file+"\"";
    System.out.println("Reduce data using feature subset"+cmd);
	//try
    //{
	  run_system_command(cmd,os);
    //}
    /*catch(IOException e)
	{
		System.out.println("IOException in running "+cmd);
		e.printStackTrace();
		System.exit(-1);
	}
	catch(InterruptedException e)
	{
		System.out.println("InterruptedException in running "+cmd);
		System.exit(-1);
	}*/		
}
 
static String [] attributes_names_of_weka_arff_file(String weka_data_file)
{ //get the attributes names including the class attribute

	BufferedReader in=null;	
	String line=null;
	Pattern p;
	Matcher m;
    String [] variables_names;
    String variables_names2="";
    int n;
    
    n=1;
	p=Pattern.compile("^\\@attribute\\s+([\\w\\p{Punct}]+)\\s+.+$");        
	try
	{        
	 in= new BufferedReader(new FileReader(weka_data_file));
	}
	catch(FileNotFoundException e)
	{
	 System.out.println("FileNotFoundException thrown by attributes_names_of_weka_arff_file at BufferedReader");
	}
 	try
	{       
	 line = in.readLine();
    }
	catch(IOException e)
	{
	 System.out.println("IOException thrown by attributes_names_of_weka_arff_file");	
	}
	while(!line.equals("@data"))
	{       
		m=p.matcher(line);
	 	if(m.matches())
		{
	 	   if(n==1)
	 		   variables_names2 = m.group(1);
	 	   else
	 		   variables_names2 = variables_names2+","+m.group(1);      
	 	   n++;
		}
	 	//else
	 	//	System.out.println(line+" does not match pattern.");
		try
		{
		 line=in.readLine();
		}
		catch(IOException e)
		{
		  System.out.println("IOException thrown by attributes_names_of_weka_arff_file at in.readLine()");	
		}	
	}
	//System.out.println(variables_names2);
	variables_names = variables_names2.split(",");
	try
	{
	 in.close();
	}
	catch(IOException e)
	{
	 System.out.println("IOException thrown by attributes_names_of_weka_arff_file at in.close()");	
	}	
	return variables_names;
}

 void run_system_command(String cmd,String os) //throws IOException, InterruptedException
 { 	 
    Process p=null;
    int val=0;
    //ProcessBuilder pb;
	//pb = new ProcessBuilder();
    try{	 
     if (os.equals("linux"))
     {
	      /*
	      pb.command("bash", "-c", cmd);
	      p = pb.start();
	      p.waitFor();
	      */
    	 p = Runtime.getRuntime().exec("bash -c " + cmd);
    	 val = p.waitFor();
    	 if(val!=0)
    		 System.out.println("System command execution failed. exit val: "+val);	               	
     }
     else if(os.equals("windows"))
     {
    	 p = Runtime.getRuntime().exec("cmd.exe /C " + cmd);
    	 val = p.waitFor();
    	 if(val!=0)
    		 System.out.println("System command execution failed. exit val: "+val);	               	
     }
    }
    catch(IOException e)
	{
		  System.out.println("IOException in running "+cmd);
		  System.exit(-1);
	}
	catch(InterruptedException e)
	{
		  System.out.println("InterruptedException in running "+cmd);
		  System.exit(-1);
	}
}
 
 String [][] load_weka_data_into_array(String data_matrix_arff_file_name) throws IOException, FileNotFoundException
 {
	 BufferedReader in=null; 
	 String line="";
	 int rows=0;
	 int cols;
	 Pattern p,p2;
	 Matcher m;
	 int i=0;
	 String [] row;
	 String [][] data_matrix = null;

	 p=Pattern.compile("^\\s*$");//empty line or a line of spaces
	 p2=Pattern.compile("^[a-zA-Z_0-9\\p{Punct}\\s]+$");//an instance
	 try
	 {
		 in= new BufferedReader(new FileReader(data_matrix_arff_file_name));
	 }
	 catch (IOException e)
	 {
		 System.out.println("IOException thrown by BufferedReader(new FileReader(data_matrix_arff_file_name))");
	 }
	 line = in.readLine();
	 //create a 2-d array
	 while(!line.equals("@data"))//go to line containing @data
	 {  
		 line=in.readLine();
	 }  
	 line=in.readLine();
	 m=p.matcher(line);
	 while(m.matches())//consume any lines of spaces after @data
	 {
		 line=in.readLine();
		 m=p.matcher(line);
	 }
	 cols = line.split(",").length;   
	 while(line!=null)
	 {
		 m = p2.matcher(line);
		 if(m.matches())
		 {
			 rows++;
		 }
		 line=in.readLine();
	 }
	 data_matrix = new String[rows][cols];
	 System.out.println("rows: "+rows+" cols:"+cols);
	 in.close();
	 //read data into array
	 in = new BufferedReader(new FileReader(data_matrix_arff_file_name));
	 line=in.readLine();  
	 while(!line.equals("@data"))//go to line containing @data
	 {  
		 line=in.readLine();
	 }  
	 line=in.readLine();
	 m=p.matcher(line);
	 while(m.matches())//consume any lines of spaces after @data
	 {
		 line=in.readLine();
		 m=p.matcher(line);
	 }
	 while(line!=null)
	 {
		 m = p2.matcher(line);
		 if(m.matches())
		 {
			 row = line.split(",");
			 data_matrix[i] = row;
			 i++;
		 }
		 line = in.readLine();
	 }
	 in.close();
	 return data_matrix;
 }
 
 String [][] load_data_into_array(String data_file_name, String data_file_format, String first_line_contain_attributes_names) 
 {//input: a data file in csv format or space delimited format
  //	   data file format (valid options: "csv" or "space") 
  //       first_line_contain_attributes_names (0 or 1
  //										    0: the first line does not contain the names of the attributes 
  //                                            1: the first line contains the names of the attributes) 		
  //output: a 2-d array containing the values in the data file
  
	  BufferedReader in=null; 
	  String line="";
	  int rows=0;
	  int cols=0;
	  Pattern p;
	  Matcher m;
	  int i=0;
	  String [] row;
	  String [][] data_matrix = null;
	  String delimiter=null;
	  
	  p=Pattern.compile("^[a-zA-Z_0-9\\p{Punct}\\s]+$");//an instance
	  
	  try
	  {
	   in= new BufferedReader(new FileReader(data_file_name));
	  }
	  catch (IOException e)
	  {
	   System.out.println("IOException thrown by BufferedReader(new FileReader(data_file_name)) in load_data_into_array method");
	   System.exit(-1);
	  }
	  if(first_line_contain_attributes_names.equals("1"))
	  {
		try{
		   line = in.readLine();//first line contains names of the attributes
		}
		catch(IOException e)
		{
	  	   System.err.println("IOException thrown by load_data_into_array method");
		   e.printStackTrace();
		   System.exit(-1);
		}
		try{
		   line = in.readLine();
		}
		catch(IOException e)
		{
	  	   System.err.println("IOException thrown by load_data_into_array method");
		   e.printStackTrace();
		   System.exit(-1);
		}
	  }
	  else if(first_line_contain_attributes_names.equals("0"))
	  {
		 try{
		     line = in.readLine();//first line is the first instance in the data set		      
		 }
		 catch(IOException e)
		 {
		    System.err.println("IOException thrown by load_data_into_array method");
		    e.printStackTrace();
		    System.exit(-1);
		 }
	  }
	  else
	  {
		  System.out.println("invalid first_line_contain_attributes_names option specifed.\n valid options: 0 or 1");
		  System.exit(-1);
	  } 
	  if(data_file_format.equals("csv"))
	  {
		  cols = line.split(",").length;
		  delimiter = ",";	    
	  }
	  else if(data_file_format.equals("space"))
	  {
		  cols = line.split("\\s+").length;
		  delimiter="\\s+";	  
	  }
	  else
	  {
		  System.out.println("invalid data file format specifed.\n valid data file format: csv or space");
		  System.exit(-1);
	  }
	  while(line!=null)
	  {
	   m = p.matcher(line);
	   if(m.matches())
	   {
	    rows++;
	   }	   
	   try{
	      line=in.readLine();
	   }
	   catch(IOException e)
	   {
	  	   System.err.println("IOException thrown by load_data_into_array method");
		   e.printStackTrace();
		   System.exit(-1);
	   }
	  }//while 
	  data_matrix = new String[rows][cols];
	  System.out.println("size of data set:\n rows: "+rows+" cols:"+cols);
	  try
	  {
	   in.close();
	  }
	  catch(IOException e)
	  {
	  	System.err.println("IOException thrown by load_data_into_array method");
		e.printStackTrace();
		System.exit(-1);
	  }
	  //read data into array
	  try
	  {
	   in= new BufferedReader(new FileReader(data_file_name));
	  }
	  catch (IOException e)
	  {
	   System.out.println("IOException thrown by BufferedReader(new FileReader(data_matrix_arff_file_name))");
	   System.exit(-1);
	  }
	  
	  if(first_line_contain_attributes_names.equals("1"))
	  {
	    try
	    {
	       in.readLine();//first line contains names of the attributes
	    }
	    catch(IOException e)
	    {
	  	   System.err.println("IOException thrown by load_data_into_array method");
		   e.printStackTrace();
		   System.exit(-1);
	    }
	    try{
	       line = in.readLine();
	    }
	    catch(IOException e)
	    {
	  	   System.err.println("IOException thrown by load_data_into_array method");
		   e.printStackTrace();
		   System.exit(-1);
	    }
	  }
	  else if(first_line_contain_attributes_names.equals("0"))
	  {
		 try{
		     line = in.readLine();//first line is the first instance in the data set		      
		 }
		 catch(IOException e)
		 {
		    System.err.println("IOException thrown by load_data_into_array method");
		    e.printStackTrace();
		    System.exit(-1);
		 }
	  }
	  else
	  {
		  System.out.println("invalid first_line_contain_attributes_names option specifed.\n valid options: 0 or 1");
		  System.exit(-1);
	  } 
	  while(line!=null)
	  {
	   m = p.matcher(line);
	   if(m.matches())
	   {
	    row = line.split(delimiter);
	    data_matrix[i] = row;
	    i++;
	   }
	   try
	   {
	    line = in.readLine();
	   }
	   catch(IOException e)
	   {
	    System.err.println("IOException thrown by load_csv_data_into_array method");
		e.printStackTrace();
		System.exit(-1);
	   }
	  }//while
	  try{
	   in.close();
	  }
	  catch(IOException e)
	  {
	  	   System.err.println("IOException thrown by load_csv_data_into_array method");
		   e.printStackTrace();
		   System.exit(-1);
	  }
	  return data_matrix;
 }
 
 public void reduce_csv_or_space_delimited_data(String data_file,String [][] data_matrix, String [] reduct_indices_array, String data_file_format, String reduced_data_file, String first_line_contain_attributes_names) 
 {
  //Reduce a csv or space delimited classification data set using a subset and write the class attribute to the reduced dataset.
  //Since reduct_indices_array does not contain the class attribute index,
  // so keep the class attribute (the last attribute) of the data set.
  int class_index=0;
  FileWriter out=null;
  String delimiter=null;
  String [] var_names=null;
  int [] reduct_indices_array_int=null;
  
  if(data_file_format.equals("csv"))
	  delimiter=",";
  else if(data_file_format.equals("space"))
	  delimiter=" ";
  else
  {
	  System.out.println("invalid data file format specified.\n valid data file format: csv or space");
	  System.exit(-1);
  }
  
  try
  {
   out = new FileWriter(reduced_data_file);   
  }
  catch(IOException e)
  {
   e.printStackTrace();
   System.exit(-1);
  }
  
  if(first_line_contain_attributes_names.equals("1"))
  {  
	  try
	  {
		  var_names = attributes_names(data_file);
		  out.write(var_names[Integer.parseInt(reduct_indices_array[0].trim())]);
		  for(int i=1; i<reduct_indices_array.length; i++)
			  out.write(delimiter+var_names[Integer.parseInt(reduct_indices_array[i].trim())]);
		  out.write(delimiter+var_names[var_names.length-1]+"\n");
	  }
	  catch(IOException e)
	  {
		  System.out.println("IOException in reduce_csv_or_space_delimited_data");
		  System.exit(-1);
	  }
  }	 
  else if(first_line_contain_attributes_names.equals("0"))
  {
  }
  else
  {
	  System.out.println("invalid first_line_contain_attributes_names option specifed.\n valid options: 0 or 1");
	  System.exit(-1);
  }
    
  reduct_indices_array_int = new int[reduct_indices_array.length];
  
  for(int j=0; j<reduct_indices_array.length; j++)
	  reduct_indices_array_int[j] = Integer.parseInt(reduct_indices_array[j].trim());
  
  //write the values of the selected features  
  class_index = data_matrix[0].length-1;
  
  for(int i=0;i<data_matrix.length;i++) 
  {  
   for(int k=0;k<reduct_indices_array_int.length;k++)
   {
	   try
	   {
		   out.write(data_matrix[i][reduct_indices_array_int[k]]+delimiter);
	   }
	   catch(IOException e)
	   {
		   e.printStackTrace();
		   System.exit(-1);
	   }
   } 
   try{
    out.write(data_matrix[i][class_index]+"\n");
   }
   catch(IOException e)
   {
     e.printStackTrace();
     System.exit(-1);
   }
  }//for
  try{
   out.close();
  }
  catch(IOException e)
  {
    e.printStackTrace();
    System.exit(-1);
  }
 }


 HashMap<Integer,HashSet<BitSet>> LocalSearchRandomReducts(ReductsSearch rs, RSys rsys, TreeSet<Integer> approx_reducts_sizes, HashMap<Integer,HashSet<BitSet>>approx_reducts,int no_of_reducts)
 {   //Run local search to find a random reduct from each approximate reduct by random forward search and backward elimination in ascending order of sizes of approximate reducts
  	 Iterator<Integer> iter;
     Iterator<BitSet> iter2;
     Iterator<BitSet> iter3;
     BitSet subset;
     int approx_reduct_size = 0;
     HashSet<Integer> features_considered;
     BitSet allFeatures;
     HashSet<BitSet> reducts;
     HashMap<Integer,HashSet<BitSet>> reducts2;//key: reduct size, value: set of reducts of a same size
     HashSet<BitSet> approx_reducts_of_same_size;
     BitSet reduct;
     BitSet approx_reduct;
     int reduct_size;
     int i=1;//count the number of reducts found so far
     boolean total_no_of_reducts_obtained = false;
     
     reducts = new HashSet<BitSet>();
     reducts2 = new HashMap<Integer,HashSet<BitSet>>();
     if(approx_reducts.isEmpty())
     {
     	System.out.println("No candidate reducts. Return null object.");
     	return null;
     } 
     allFeatures = new BitSet(rs.numberOfBits+1);//1 bit is added to represent class attribute
     for(int k = 0; k < rs.numberOfBits; k++)
       	 allFeatures.set(k);
     if (rs.gamma_setOfAllFeatures_computed == false)
     {
     	rs.gamma_setOfAllFeatures = rsys.relativeDependency(allFeatures);
     	rs.gamma_setOfAllFeatures_computed = true;
     }
     iter = approx_reducts_sizes.iterator(); // sort subset sizes in ascending order     
     while(iter.hasNext() && !total_no_of_reducts_obtained)
     {     
       approx_reduct_size = iter.next();
       approx_reducts_of_same_size = approx_reducts.get(approx_reduct_size);
       iter2 = approx_reducts_of_same_size.iterator();
       while(iter2.hasNext())
       {
    	   subset = iter2.next();
    	   features_considered = new HashSet<Integer>();
    	   features_considered.clear();
    	   approx_reduct = new BitSet(rs.numberOfBits+1);//add 1 bit to represent the class index
    	   for(int k=0;k < rs.numberOfBits; k++)
    	   {
    		   if(subset.get(k))
    		   {
    			   approx_reduct.set(k);   		
    		   }
    	   }
    	   System.out.println("approximate reduct "+i+": "+subset+" size: "+approx_reduct_size+" gamma: "+rs.gammas.get(subset));
	       //find a reduct randomly from this approximate reduct
		   //===option 1: Do forward selection then do backward elimination
    	   //reduct = RandomForwardSearch(rs,rsys,approx_reduct);   		   
		   //System.out.println("Random forward selection found reduct: "+reduct);
		   //reduct = RandomBackwardElimination(rs,rsys,reduct); 
		   //===option 2: Do backward elimination only
		   reduct = RandomBackwardElimination(rs,rsys,approx_reduct); 
		   System.out.println("Random backward elimination found reduct: "+reduct);
		   reducts.add(reduct);
		   if(reducts.size() == no_of_reducts)
    		   break;
    	   else
    		   i++;	   
       }
     }
     //display all reducts found
     System.out.println("total no. of reducts found by local search: "+reducts.size());
	 iter3 = reducts.iterator();
	 while(iter3.hasNext())
	 {
	   subset = iter3.next();
	   System.out.println("reduct: "+subset+" size: "+get_subset_size(subset,rs.numberOfBits));
	 }
     //store reducts to HashMap<Integer,HashSet<BitSet>>
     iter2 = reducts.iterator();
     while(iter2.hasNext())
     {
    	reduct = iter2.next();
    	reduct_size = get_subset_size(reduct,rs.numberOfBits);
    	if(!reducts2.containsKey(new Integer(reduct_size)))
    	{
    		HashSet<BitSet> reductsSet= new HashSet<BitSet>();
    		reductsSet.add(reduct);
    		reducts2.put(reduct_size,reductsSet);
    	}
    	else
    	{
    		HashSet<BitSet> reductsSet = reducts2.get(reduct_size);
    		reductsSet.add(reduct);
    		reducts2.put(new Integer(reduct_size),reductsSet);
    	}
     }
     //System.out.println("reducts2: "+reducts2.toString());
    //System.out.println("total number of approximate reducts input to LocalSearchRandomReducts function: "+n);
    return reducts2;
  }
/* 
HashSet<BitSet> convertToReducts(ReductsSearch rs, RSys rsys, HashSet<BitSet>approx_reducts, HashSet<BitSet>reducts,int no_of_reducts)
{//Transform each approximate reduct in a hashset to a reduct by removing any redundant attributes from the approximate reduct
 //input: ReductsSearch rs
 //       hashset of approximate reducts (BitSets with no bit to represent the class) to be converted to reducts
 //       hashset of reducts 	(an empty hashset) 
 //	 	  no. of reducts to convert to
	
    Iterator<BitSet> iter;
    BitSet subset;
    BitSet reduct=null;
    int i=1;
    int subset_size = 0;
    HashSet<String> features_considered;
    BitSet allFeatures;
    
    if(approx_reducts.isEmpty())
    {
    	System.out.println("no candidate reducts.");
    	System.exit(-1);
    }
    
    allFeatures = new BitSet(rs.numberOfBits+1);//1 bit is added to represent class attribute
    for(int k = 0; k < rs.numberOfBits; k++)
      	 allFeatures.set(k);
    if (rs.gamma_setOfAllFeatures_computed == false)
    {
    	rs.gamma_setOfAllFeatures = rsys.relativeDependency(allFeatures);
    	rs.gamma_setOfAllFeatures_computed = true;
    }
    
    iter = approx_reducts.iterator();
    System.out.println("total no. of approximate reducts: "+approx_reducts.size());
    
    features_considered = new HashSet<String>();
    
    while(iter.hasNext())
    {
     subset = iter.next();
     subset_size = 0;
     features_considered.clear();
     BitSet approx_reduct = new BitSet(rs.numberOfBits+1);//add 1 bit to represent the class index
     for(int k=0;k < rs.numberOfBits; k++)
     {
       if(subset.get(k))
       {
    	    approx_reduct.set(k);
    	    subset_size++;
       }
     }
     
     System.out.println("approximate reduct "+i+": "+subset+" size: "+subset_size+" gamma: "+rs.gammas.get(subset));
     reduct = RandomBackwardElimination(rs,rsys,approx_reduct); 
     reducts.add(reduct);
     if(reducts.size() >= no_of_reducts)//print the specified no. of reducts only if there are a lot more
     {
    	 System.out.println("no. of reducts found: "+reducts.size());
    	 break;
     }
     else
     {
    	 System.out.println("no. of reducts found: "+reducts.size());
    	 i++;
     }
    }
    return reducts;
 }
 */
 void print_subsets(HashSet<BitSet> subsets, int no_of_subsets_to_print,ReductsSearch rs,String results_file) throws IOException
 {//print the specified numbe of subsets in a random order to screen and to a file
	Iterator<BitSet> iter;
    BitSet subset;
    FileWriter out;
    String subset_info;
    
    int i=0;//no. of reducts which have been printed  
    
    out = new FileWriter(results_file);
    
    System.out.println("total no. of reducts: "+subsets.size());

    iter = subsets.iterator();

    while(iter.hasNext())
    {     
      subset = iter.next();
      i++;
      subset_info = "subset "+i+": "+subset.toString()+" size: "+get_subset_size(subset,rs.numberOfBits)+" gamma: "+rs.gamma_setOfAllFeatures;
      System.out.println();
      out.write(subset_info+'\n');
      if(i == no_of_subsets_to_print)
         break;     
    }
    out.close();
 }

 void print_subsets(TreeSet<Integer> subsets_sizes, HashMap<Integer,HashSet<BitSet>> subsets, ReductsSearch rs,String results_file) throws IOException
 {//print the specified number of subsets in ascending order of their sizes to screen and to a file
	//Set<Integer> ks;
    Iterator<Integer> iter;
    Iterator<BitSet> iter2;
	BitSet subset;
    FileWriter out;
    String subset_info;
    int i=0;  //no. of subsets which have been printed  
    int subset_size;
    HashSet<BitSet> subsets_of_same_size;
    int total_no_of_subsets = 0;
    boolean total_no_of_subsets_printed=false;
    
    out = new FileWriter(results_file);
    
    total_no_of_subsets = get_total_no_of_subsets(subsets);
        
    System.out.println("total no. of subsets: "+total_no_of_subsets);
    iter = subsets_sizes.iterator(); // sort subset sizes in ascending order
    
     while(iter.hasNext() && !total_no_of_subsets_printed)
    {     
      subset_size = iter.next();
      subsets_of_same_size = subsets.get(subset_size);
      iter2 = subsets_of_same_size.iterator();
      while(iter2.hasNext())
      {
    	  i++;
    	  subset = iter2.next();
          subset_info = "subset: "+i+": "+subset.toString()+" size: "+get_subset_size(subset,rs.numberOfBits)+" gamma: "+rs.gamma_setOfAllFeatures;
          System.out.println(subset_info);
          out.write(subset_info+'\n');
          if(i == total_no_of_subsets)
          {
        	  total_no_of_subsets_printed = true;
        	  break;           
         }
     }
    }
    out.close();
 }

 int get_total_no_of_subsets (HashMap<Integer,HashSet<BitSet>>subsets)
 {
	 //get the total no. of subsets in a hashtable: key=size of subset, value= a set of subsets of the size
	 Set<Integer> ks;
     Iterator<Integer> iter;
      int total_no_of_subsets = 0;
      Integer subset_size;
      HashSet<BitSet> subsets_of_same_size;
      
     ks = (Set<Integer>)subsets.keySet();
     iter = ks.iterator();

    while(iter.hasNext())
   {
 	 subset_size = iter.next();
 	 subsets_of_same_size = subsets.get(subset_size);
 	 total_no_of_subsets += subsets_of_same_size.size();
  }
   return total_no_of_subsets;
 }
 /*
 HashSet<BitSet> backwardElimination(ReductsSearch rs, RSys rsys, BitSet subset, HashSet<BitSet> reducts)
{ //Use backward elimination to transform an approximate reduct to a reduct 
  //where gamma of approximate reduct == gamma of set of all features 
  //input: rs (a ReductsSearch object)
  //	   rsys (a RSys object)
  //       a subset (a BitSet which contains the class index)
  //	   reducts (a hashtable of reducts containing the class attribute)
	 BitSet subset2;    
     int i=0;   
     String features_indices = "";
     String f_indices [];
     float gamma_of_subset;
     
     gamma_of_subset = rsys.relativeDependency(subset);
    
     if(gamma_of_subset < rs.gamma_setOfAllFeatures)
     {
    	 System.out.println(subset.toString()+" is not an approximate reduct.\n gamma of "+subset.toString()+": "+gamma_of_subset+" gamma of set of all features: "+rs.gamma_setOfAllFeatures);
    	 return reducts;
     }
     
     for(int k = 0; k < rs.numberOfBits; k++)
     {
    	 if(subset.get(k))
    		 features_indices+=k+",";   	 
     }
     f_indices = features_indices.split(",");
     //remove each feature from the subset and check gamma of each new subset until a reduct is found
     while(i < f_indices.length && a_reduct_found == false)
     {
       //create a new subset by removing ith feature
       subset2 = (BitSet) subset.clone();
       subset2.clear(Integer.parseInt(f_indices[i]));
      
       //System.out.println(subset2.toString());
       
       if(get_subset_size(subset2,rs.numberOfBits) < rs.numberOfBits)
       {	   
        if(rsys.relativeDependency(subset2) >= rs.gamma_setOfAllFeatures)//ith feature is redundant
           //ith feature is redundant, so remove it from the approximate reduct and check next features
           reducts = backwardElimination(rs,rsys,subset2,reducts); 	          
        else if(i == f_indices.length-1)//i is index of last feature in the subset and none of the features is redundant
        {
          a_reduct_found = true;
          reducts.add(subset); 
        }
        else//i is a relevant feature, so try remove the next feature from the subset
        {
        }
       }
       i++;
     } 
     return reducts;
 }
*/
BitSet RandomBackwardElimination(ReductsSearch rs, RSys rsys, BitSet subset)
{ //Use backward elimination to transform an approximate reduct to a reduct by randomly deleting a redundant feature repeatedly.
  //where gamma of approximate reduct == gamma of set of all features 
  //input: rs (a ReductsSearch object)
  //	   rsys (a RSys object)
  //       a subset which does not contains the class attribute (a BitSet)
	 int i;
     int f_indices [];
     float gamma_of_subset;
     Random r;
     BitSet not_considered_features;
     int subset_size=0;
     
     gamma_of_subset = rsys.relativeDependency(subset);
     
     if(gamma_of_subset < rs.gamma_setOfAllFeatures)
     {
    	 System.out.println(subset.toString()+" is not an approximate reduct.\n gamma of "+subset.toString()+": "+gamma_of_subset+" gamma of set of all features: "+rs.gamma_setOfAllFeatures);
    	 return null;
     }
     subset_size = get_subset_size(subset,rs.numberOfBits);
     if(subset_size == 1)
     {
    	 System.out.println("feature subset has 1 feature only. No more features can be removed by backward elimination.");
    	 return subset;
     }
     f_indices = new int[subset_size];
     i=0;  
     for(int k = 0; k < rs.numberOfBits; k++)
     {
    	 if(subset.get(k))
    	 {
    		 f_indices[i] = k;  	 
    		 i++;
    	 }
     }
     //System.out.println("total no. of features: "+rs.numberOfBits);
     //remove each feature from the subset and check gamma of each new subset until a reduct is found
     not_considered_features = (BitSet) subset.clone();
     int s = new Random().nextInt(923223499);
     r = new Random(s);
     //System.out.println(s);
     while(!not_considered_features.isEmpty())
   	 {     	 
    	 i = r.nextInt(f_indices.length);
    	 //System.out.println("i: "+i);
    	 if(not_considered_features.get(f_indices[i]))
    	 {
		  subset.clear(f_indices[i]);
		  if(rsys.relativeDependency(subset) == rs.gamma_setOfAllFeatures)
			  not_considered_features.clear(f_indices[i]);
		  else
		  {		
     		  subset.set(f_indices[i]);
     		  not_considered_features.clear(f_indices[i]);
		  }
    	 }
   	 }   	  
     return subset;
 }

BitSet RandomForwardSearch(ReductsSearch rs, RSys rsys, BitSet features_to_choose_from)
{ //Starting from an empty subset, use forward search to transform an approximate reduct to a reduct by randomly adding a relevant feature repeatedly until a reduct is obtained.
  //where gamma of approximate reduct == gamma of set of all features 
  //input: rs (a ReductsSearch object)
  //	   rsys (a RSys object)
  //	   a subset containing the features to choose from
	 BitSet new_subset;  
	 BitSet reduct;
     int j;
     int f_indices [];
     float gamma_of_subset;
     Random r;
     int subset_size;
     float gamma_of_previous_subset;
     float gamma_of_new_subset;
     
     subset_size = 0;
     new_subset = null;
     reduct = null;
     gamma_of_subset = rsys.relativeDependency(features_to_choose_from);    
     if(gamma_of_subset < rs.gamma_setOfAllFeatures)
     {
    	 System.out.println(features_to_choose_from.toString()+" is not an approximate reduct.\n gamma of "+features_to_choose_from.toString()+": "+gamma_of_subset+" < gamma of set of all features: "+rs.gamma_setOfAllFeatures);
    	 return null;
     }
     subset_size = get_subset_size(features_to_choose_from,rs.numberOfBits);
     /*
     for(int k = 0; k < rs.numberOfBits; k++)
     {
    	 if(features_to_choose_from.get(k))
    		 subset_size++;  	 
     }
     */
     f_indices = new int[subset_size];
     j=0;
     
     for(int k = 0; k < rs.numberOfBits; k++)
     {
    	 if(features_to_choose_from.get(k))
    	 {
    		 f_indices[j] = k;  	 
    		 j++;
    	 }
     }
            
     //System.out.println("total no. of features: "+rs.numberOfBits);
     //add each feature from the subset and check gamma of each new subset until a reduct is found
     r = new Random();
     j = r.nextInt(f_indices.length);	
     new_subset = new BitSet(rs.numberOfBits+1);
     new_subset.set(f_indices[j]);
     reduct = new_subset;
     gamma_of_previous_subset = rsys.relativeDependency(new_subset);
     while(gamma_of_previous_subset < rs.gamma_setOfAllFeatures)
     {	 
    	j = r.nextInt(f_indices.length);	 
    	if(new_subset.get(f_indices[j])==false)
    	{
    	 new_subset.set(f_indices[j]);
    	 gamma_of_new_subset = rsys.relativeDependency(new_subset);
    	 if(gamma_of_new_subset == rs.gamma_setOfAllFeatures)
    	 {   
    	   reduct = new_subset;
    	   break;
    	 }
    	 else if(gamma_of_new_subset > gamma_of_previous_subset)
    	 {	 //adding this feature to the previous subset increases gamma, this feature is significant
    		gamma_of_previous_subset = gamma_of_new_subset;
    		reduct = new_subset;
   		 }
    	 else//adding this feature to the previous subset does not increase gamma, this feature is redundant
     	    new_subset.clear(f_indices[j]);
    	 	reduct = new_subset;
   		}
     }    
     /*if (reduct == null)
    	 return features_to_choose_from;
     else
    	 return reduct;
    	 */
     return reduct;
 }

public int get_subset_size(BitSet subset, int numberOfFeatures)
 { 
	  int subset_size=0;
	  
	  for(int j = 0; j < numberOfFeatures; j++)
	  {
	    if(subset.get(j))
	        subset_size++;
	  }
	  
	  return subset_size;
 }
 
 public String [] attributes_names(String data_file_name)throws FileNotFoundException,IOException
 {
  //input: a csv data file
  //output names of all variables including the class attribute

  BufferedReader in;
  String all_variables;
  String all_features [] = null;
  
  in = new BufferedReader(new FileReader(data_file_name));
  all_variables=in.readLine();
  in.close();
  
  all_features = all_variables.split(",");

  return all_features;
 }
 
 void create_weka_arff_file(String [][] data_matrix, String csv_data_file, String weka_arff_file)  throws IOException
 { //create a discrete weka arff file from a csv discrete data file
	  
     String [] all_variables=null;
	  int all_features_indices [];
	  
	  if (data_matrix == null)
	  {
		  System.out.println("data_matrix is null in create_weka_arff_file.");
	  	  System.exit(-1);
     }
	  
	  try
	  { 
	    all_variables = attributes_names(csv_data_file);
	  } 
	  catch (IOException e)
	  { 
	     System.err.println("IOException thrown by variables_names method");
	     e.printStackTrace();
	     System.exit(1);
	  }
	//indices of all the features of the data set excluding the class attribute
	  all_features_indices = new int [all_variables.length-1];//exclude the class attribute
	  
	  for(int i=0; i < all_features_indices.length; i++)//exclude the class attribute
		  all_features_indices[i]=i;
	  
	  reduce_data(all_variables,data_matrix,all_features_indices,weka_arff_file);  
 }

  void create_numeric_weka_arff_file(String [][] data_matrix, String csv_data_file, String weka_arff_file)  throws IOException
  { //create a numeric weka arff file from a numeric csv data file
	  
     String [] all_variables=null;
	  int all_features_indices [];
	  
	  if (data_matrix == null)
	  {
		  System.out.println("data_matrix is null in create_weka_arff_file.");
	  	  System.exit(-1);
     }
	  
	  try
	  { 
	    all_variables = attributes_names(csv_data_file);
	  } 
	  catch (IOException e)
	  { 
	     System.err.println("IOException thrown by variables_names method");
	     e.printStackTrace();
	     System.exit(1);
	  }
	  //indices of all the features of the data set excluding the class attribute
	  all_features_indices = new int [all_variables.length-1];
	  
	  for(int i=0; i < all_features_indices.length; i++)
		  all_features_indices[i]=i;
	  
	  reduce_data2(all_variables,data_matrix,all_features_indices,weka_arff_file);  
  }
  
 public void reduce_data(String [] all_variables,String [][] data_matrix, int []reduct_indices_array, String reduced_data_arff_file) throws IOException
 {
    //all_variables is output by variables_names method
    //output: a discrete weka .arff file containing the features specified by the reduct_indices_array
	 
  Set<String> attrs_vals_set;
  Iterator<String> iter;
  int last_index=0;
  FileWriter out;
  int class_index = data_matrix[0].length-1;
  
  attrs_vals_set = new HashSet<String>();

  out = new FileWriter(reduced_data_arff_file);   
  out.write("@relation data\n");

  for(int k=0;k<reduct_indices_array.length;k++)
  {  
    for(int i=0;i<data_matrix.length;i++) 
    {
      attrs_vals_set.add(data_matrix[i][reduct_indices_array[k]]);
    }    
    out.write("@attribute "+all_variables[reduct_indices_array[k]] +" {");
    //System.out.println(k+" @attribute "+all_variables[reduct_indices_array[k]]);
    iter = attrs_vals_set.iterator();
    out.write(iter.next());
    while(iter.hasNext())
    {
     out.write(","+iter.next());
    }
    out.write("}\n"); 
    attrs_vals_set.clear();
  }
  //write the class attribute
  for(int i=0;i<data_matrix.length;i++) 
  {
    attrs_vals_set.add(data_matrix[i][class_index]);
  }    
  out.write("@attribute "+all_variables[class_index] +" {");
  //System.out.println("@attribute "+all_variables[class_index]);
  iter = attrs_vals_set.iterator();
  out.write(iter.next());
  while(iter.hasNext())
  {
   out.write(","+iter.next());
  }
  out.write("}\n");  
  out.write("@data\n");
  last_index = reduct_indices_array.length-1;
  for(int i=0;i<data_matrix.length;i++) 
  {  
	  for(int k=0;k<last_index;k++)
	  {
		  out.write(data_matrix[i][reduct_indices_array[k]]+",");
	  }  
	  out.write(data_matrix[i][reduct_indices_array[last_index]]+",");
	  out.write(data_matrix[i][class_index]+"\n");
  }
  out.close();
 }

 public void reduce_data2(String [] all_variables,String [][] data_matrix, int []reduct_indices_array, String reduced_data_arff_file) throws IOException
 {
    //all_variables is output by variables_names method
    //output: a numeric weka .arff file containing the features specified by the reduct_indices_array
	 
  Set<String> attrs_vals_set;
  Iterator<String> iter;
  int last_index=0;
  FileWriter out;
  int class_index = data_matrix[0].length-1;
  
  attrs_vals_set = new HashSet<String>();

  out = new FileWriter(reduced_data_arff_file);   
  out.write("@relation data\n");

  for(int k=0;k<reduct_indices_array.length;k++)
  {  
    out.write("@attribute "+all_variables[reduct_indices_array[k]] +" numeric\n");   
  }
  //write the class attribute
  for(int i=0;i<data_matrix.length;i++) 
  {
    attrs_vals_set.add(data_matrix[i][class_index]);
  }    
  out.write("@attribute "+all_variables[class_index] +" {");
  iter = attrs_vals_set.iterator();
  out.write(iter.next());
  while(iter.hasNext())
  {
   out.write(","+iter.next());
  } 
  out.write("}\n");  
  out.write("@data\n");
  last_index = reduct_indices_array.length-1;
  for(int i=0;i<data_matrix.length;i++) 
  {  
	  for(int k=0;k<last_index;k++)
	  {
		  out.write(data_matrix[i][reduct_indices_array[k]]+",");
	  }  
	  out.write(data_matrix[i][reduct_indices_array[last_index]]+",");
	  out.write(data_matrix[i][class_index]+"\n");
  }
  out.close();
 }
 
 
   
 
}
