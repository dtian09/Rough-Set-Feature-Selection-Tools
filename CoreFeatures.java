import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//Learn a decision from a continuous data set or a discrete data set and select the core features from the decision tree 
//by doing Breadth First Search traversal of the tree 
//input: a csv data file
//		 number of core features
//output: a file containing the core features

public class CoreFeatures 
{
	public final static void main(String[] args) 
	{
		
	 String output_file;
	 Utility util;
	 String [][] data_matrix;
	 //String class_path = "/home/david/Downloads/weka-3-6-12/weka.jar";
	 String class_path = "C:\\Program Files\\Weka-3-6-12\\weka.jar";
	 String cmd;
	 Random randomGenerator;
	 String weka_arff_file;
	 String weka_output_file;
	 String data_file;
	 int number_of_core_features;
	 HashMap<String,Integer> features_indices;
	 ArrayList<String> core_features;
	 Iterator<String> iter;
	 String feature;
	 FileWriter out=null;
	 Integer index;
	 
	 data_file = args[0];//csv data file
	 //weka_arff_file = args[0];
	 number_of_core_features = Integer.parseInt(args[1]);// number of core features
	 output_file = args[2];
	 
	 core_features = null;
	 util = new Utility();
	 
	 data_matrix = util.load_data_into_array(data_file, "csv", "1");
	 try
	 {
	  out = new FileWriter(output_file);
	 }
	 catch(IOException e)
     {
   	  System.out.println("IOException in output_file");
     }
	  randomGenerator = new Random();
	  weka_arff_file = randomGenerator.nextInt(1000000)+".arff";
	  weka_output_file = Integer.toString(randomGenerator.nextInt(9000000))+".tree";
	  /*
	   try{
		  //***create a continuous weka arff file (the input csv data file is continuous) to learn a decision tree from the continuous data and select core features from the decision tree
		  util.create_numeric_weka_arff_file(data_matrix,data_file,weka_arff_file);
		  //***create a discrete weka arff file (the input csv data file is discrete) to learn a decision tree from the discrete data and select core features from the decision tree
		  //util.create_weka_arff_file(data_matrix,data_file,weka_arff_file);
	   }
	   catch(IOException e)
	  {
		  System.out.println("IOException in create_weka_arff_file.");
		  System.exit(-1);
	  }
	  */
	 cmd = "java -cp \""+class_path+"\" weka.core.converters.CSVLoader \""+data_file+"\" > "+weka_arff_file;
	 try
 	 {
	    util.run_system_command(cmd,"linux");
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
	 cmd = "java -Xmx1024m -cp "+class_path+" weka.classifiers.trees.J48 -t "+weka_arff_file+" > "+weka_output_file;
     try
 	 {
	    util.run_system_command(cmd,"linux");
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
	   //get a list of core features using breadth first search
      try{
       core_features = BFS(weka_output_file,number_of_core_features);
      }
      catch(IOException e)
      {
    	  System.out.println("IOException thrown in BFS method.");
    	  System.exit(-1);
      }
    
       System.out.println(core_features);
      
      features_indices = DataCube.find_attrs_col_nos(data_file);
      iter = core_features.iterator();
      feature = (String)iter.next();
      try
      {
    	index = features_indices.get(feature);
        out.write(index.toString());
      }
      catch(IOException e)
      {
    	  System.out.println("IOException thrown by out.");
    	  System.exit(-1);
      }
        while(iter.hasNext())
      {
    	  feature = (String)iter.next();
    	  index = features_indices.get(feature);
    	  try
    	  {
    	    out.write(","+index);
    	  }
    	  catch(IOException e)
          {
        	  System.out.println("IOException thrown by out2.");
        	  System.exit(-1);
          }
      }
      try{
        out.close();
      }
      catch(IOException e)
	  {
	    System.out.println("IOException thrown by out.");
	    System.exit(-1);
	  }
       cmd = "rm "+weka_arff_file;
       try
   	  {
  	    util.run_system_command(cmd,"linux");
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
	
	static ArrayList<String> BFS(String weka_output_file,int number_of_core_features) throws IOException
	{
		ArrayList<String> core_features;
		ArrayList<ArrayList<String>> BFS_features;//store features traversed in Breadth First Order
																		   //root node at index 0, nodes at level 1 at index 1, nodes at level 2 at index 2 etc.
																		   //e.g. [[0][1,2][3,4,5],[6,7]]
		BufferedReader in=null;
		String line=null;
		Pattern p;
		Pattern p2;
		Pattern p3;
		Pattern p4;
		Matcher m;
		Matcher m2;
		Matcher m3;
		Matcher m4;
		String pipesStr;
		String root=null;
		String [] pipes=null;
		int level;
		ArrayList<String> rootL;
		ArrayList<String> featuresL;
		String feature;
		Iterator<String> iter;
		Iterator<ArrayList<String>> iter2;
		int i;
		boolean root_obtained;
		
		BFS_features = new ArrayList<ArrayList<String>>();
		root_obtained = false;
		
		p=Pattern.compile("^([\\w]+)\\s*[<=>]+.+$");
		p2=Pattern.compile("^([\\s\\|]+)([\\w\\p{Punct}]+)\\s*[<=>]+.+$");
		p3 = Pattern.compile("^\\-+$");
		p4 = Pattern.compile("^\\s*Number\\s+of\\s+Leaves.+$");
		in = new BufferedReader(new FileReader(weka_output_file));
		line = in.readLine();
		while(line!=null)//go to the line "-------------"
		{
		  m3 = p3.matcher(line);
		  if(m3.matches())
			  break;
		  line = in.readLine();
		}
		
		while(line!=null)
		{
		  m = p.matcher(line);
		  m2 = p2.matcher(line);
		  m4 = p4.matcher(line);
		  if(m4.matches())
			  break;
		  if(m.matches() && root_obtained == false)
		  {
			  root = m.group(1);
			  rootL = new ArrayList<String>();
			  rootL.add(root);
			  BFS_features.add(0, rootL);
			  root_obtained = true;
		  }
		  else if(m2.matches())
		  {
				 pipesStr = m2.group(1);
				 feature = m2.group(2);
				 pipes = pipesStr.split("\\s+");
				 level = pipes.length;					    
			     if(level < BFS_features.size())
			     {
				    featuresL = BFS_features.get(level);
				    featuresL.add(feature);
					BFS_features.set(level,featuresL);
				  }
			     else
			     {//if no nodes at this level have been added to BFS_features
				   featuresL = new ArrayList<String>();
				   featuresL.add(feature);
				   BFS_features.add(featuresL);
			     }			  
		  }
		  line = in.readLine();
		}
		core_features = new ArrayList<String>();
		i=0;//count the number of features added to the core
		System.out.println("number of levels in decision tree (root node (top level) is not counted): "+(BFS_features.size()-1));
		iter2 = BFS_features.iterator();
		while(iter2.hasNext())
		{
			featuresL = iter2.next();
			iter = featuresL.iterator();
			while(iter.hasNext() && i < number_of_core_features)
			{
				feature = iter.next();
				if(!core_features.contains(feature))
				{
					core_features.add(feature);
				    i++;
				}
			}
		}
		if (i < number_of_core_features)//if i < number of core features, then i == total no. of features in decision tree
			System.out.println("no. of core features: "+number_of_core_features+", total no. of features in decision tree: "+i);
		in.close();
		return core_features;
	}	
}
