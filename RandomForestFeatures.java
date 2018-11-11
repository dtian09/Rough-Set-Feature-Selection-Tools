import java.io.*;
import java.util.*;
import java.util.regex.*;

//get features in a random forest outpout by weka
//output: roots of the trees in the random forest and features of the random forest

public class RandomForestFeatures {
	
	static HashSet<String> features_names;//features names in the random forest of the original data set 

	public final static void main(String[] args) 
	{
		
	 String output_file;
	 Utility util;
	 Random randomGenerator;
	 String weka_output_file;//random forest file output by weka
	 BufferedReader in;
	 String features=null;//all features in the random forest
	 String [] features2=null;
	 String roots=null;
	 String [] roots2=null;//roots of trees in the random forest
	 HashSet<String> roots3=null;
	 String roots_and_features;//roots of trees and all the features in the random forest
	 										//format:
	 										//'roots=f1,f2,f2,f3,f4,f4,f4_features=f1,f2,f3,f4,f6,f7,f9...'
	 Iterator<String> iter;
	 FileWriter out=null;
	 Integer index;
	 String weka_data_file;
	 ArrayList<String> trees=null;//decision trees of a random forest
	 String out_file=null;
	 String [] roots_features;
	 Pattern p;
	 Pattern p2;
	 Matcher m;
	 Matcher m2;
	 String weka_data_format_feature=null;
	 TreeSet<Integer> roots_indices;
	 HashMap<String,Integer> features_indices;
	 TreeSet<Integer> features_indices2;
	 HashMap<String,Integer> roots_freq;
	 int freq;
     String feature_name=null;

	 weka_data_file =args[0];
	 weka_output_file = args[1];
	 out_file = args[2];//roots of trees and all features of random forest
	 roots3 = new HashSet<String>();
	 features_names = new HashSet<String>();
	 
	 try
	 {
		 out = new FileWriter(out_file);
	 }
	 catch(IOException e)
     {
   	  System.out.println("IOException thrown by FileWriter.");
   	  System.exit(-1);
     }
	 try
	 {
	   trees = get_trees(weka_output_file);
	 }
	 catch(IOException e)
     {
   	  System.out.println("IOException thrown by get_trees.");
   	  System.exit(-1);
     }
	 //System.out.println(trees);
	 
	 //print the roots of the trees
	 roots_freq = new HashMap<String,Integer>();
	 roots_and_features = get_features(trees);
	 //System.out.println(roots_and_features);
	 
	 roots_features = roots_and_features.split("##");
	 p=Pattern.compile("^roots=\\[(.+)\\]$");
	 p2=Pattern.compile("features=\\[(.+)\\]");
	 m = p.matcher(roots_features[0]);
	 m2 = p2.matcher(roots_features[1]);
 	 if (m.matches())
	 {
 		 roots = m.group(1);
 		 roots2 = roots.split(",");
 		 //count frequencies of roots

 		 for(int a=0; a<roots2.length; a++)
 		 {	   
 			 if(roots_freq.containsKey(roots2[a]))
 			 {
 				 freq = (Integer) roots_freq.get(roots2[a]);
 				 freq++;
 				 roots_freq.put(roots2[a],freq);
 			 }
 			 else
 				 roots_freq.put(roots2[a],1);
 		 }
	 }
	 if(m2.matches())
	 {
	  features = m2.group(1);
	  features2 = features.split(","); 
	 }
	 else
	 {
	   System.out.println(roots_features[1]+" does not match p2");
	 }
     //print the indices of roots
     //feature format in weka data file: 'Fetus(Transcript/Million)=\'[0-1)\''
     //feature format in random forest output file: Fetus(Transcript/Million)='[130-241)'
     p = Pattern.compile("^(.+)'(.+)'$");//pattern of feature format in random forest file
     features_indices = DataMatrix.find_attrs_col_nos(weka_data_file);
     roots_indices = new TreeSet<Integer>();
     for(int i=0;i<roots2.length;i++)
     {
    	 m = p.matcher(roots2[i].trim());
    	 if(m.matches())
    	 {
    		 weka_data_format_feature = "'"+m.group(1)+"\\'"+m.group(2)+"\\''";	  
    		 index = features_indices.get(weka_data_format_feature);
    		 //System.out.println(weka_data_format_feature+": "+index);
    		 roots_indices.add(index);
    		 roots3.add(weka_data_format_feature);
    	 }
    	 else
    		 System.out.println(roots2[i]+" does not match pattern");
     }
   
     try
	 {
	       out.write("roots:\n");
	       //System.out.println(roots);
	       out.write(roots3+"\n");
	       out.write("indices of roots:\n");
		   out.write(roots_indices.toString()+"\n");  
  	  	   out.write("number of different tree roots in random forest: "+roots_indices.size()+"\n");
	 }
	 catch(IOException e)
     {
   	  System.out.println("IOException thrown by write roots.");
   	  System.exit(-1);
     }
     
     System.out.println("number of roots: "+roots_indices.size());

	//print frequencies of roots
	
	 //print the features
	 //System.out.println("features: "+features);
	
	 //print the features indices
	features_indices2 = new TreeSet<Integer>();
	
	for(int i=0;i<features2.length;i++)
	{
		m = p.matcher(features2[i].trim());
		if (m.matches())
		{
			weka_data_format_feature = "'"+m.group(1)+"\\'"+m.group(2)+"\\''";	  
			index = features_indices.get(weka_data_format_feature);
			//System.out.println(weka_data_format_feature+": "+index);
			features_indices2.add(index);
			//out.write(index.toString()+",");
		}
		else
		{
			System.out.println(features2[i]+" does not match pattern");
		}
	}
	try
	 {
		 out.write("all features of random forest:\n");       
	     out.write(features+"\n");
		 out.write("indices of all features in random forest:\n"); 
		 out.write(features_indices2.toString()+"\n");
		 out.write("number of boolean features in random forest:\n"+Integer.toString(features_indices2.size())+"\n");
		 out.write("number of features in random forest:\n"+features_names.size()+"\n");
		 out.write("features names:\n");
	     //print the features names of the random forest of the original data set 
		 iter = features_names.iterator();
		 while(iter.hasNext())
		 {
			 feature_name = iter.next();
			 out.write(feature_name+"\n");
		 }
		 out.close();
	 }
	catch(IOException e)
	{
		System.out.println("IOException thrown by out.");
		System.exit(-1);
	}
	System.out.println("number of features in random forest: "+features_indices2.size());
  }
	
	static ArrayList<String> get_trees (String random_forest_file) throws IOException
	{
		/*format of a tree
	
RandomTree
==========

ClosenessCentrality_KP='[0.342601-0.343299)' < 0.5
|   BC_KP='[0.000172-0.000176)' < 0.5
|   |   BC_KP='[0.000013-0.000026)' < 0.5
|   |   |   D='[4.36-4.369)' < 0.5
...
Size of the tree : 2269
*/
		BufferedReader in = null;
		ArrayList<String> trees = new ArrayList<String>();
		String line;
		Pattern p;
		Matcher m;
		String tree;
		
		p=Pattern.compile("^Size of the tree.+$");
		tree = "";
		try
		{
			in = new BufferedReader(new FileReader(random_forest_file));
		}
		catch(IOException e)
	     {
	   	  System.out.println("IOException thrown by reading random forest file.");
	   	  System.exit(-1);
	     }
		line = in.readLine();
		while(line!=null)
		{  
			if(line.equals("RandomTree"))
			{  
				tree = "";
				line = in.readLine();	
				m = p.matcher(line);
				while(!m.matches())
				{ 
					tree += line+"\n"; 
					line = in.readLine();
					m = p.matcher(line);
				}
				tree ="tree start\n"+tree+"end";
				trees.add(tree);
			}
			line = in.readLine();
		}
		in.close();
		return trees;
	}
	
	static String get_features(ArrayList<String> trees)
	{
		String tree;
		HashSet<String> features;		
		String line=null;
		Pattern p;
		Pattern p2;
		Pattern p3;
		Matcher m;
		Matcher m2;
		Matcher m3;
		String root=null;
		ArrayList<String>roots;
		String feature;
		String roots_and_features;
		String [] lines;	
		boolean root_found;
		roots_and_features="";//roots of trees and all the features in the random forest
												     //format:
													 //'roots=f1,f2,f2,f3,f4,f4,f4_features=f1,f2,f3,f4,f6,f7,f9...'
		features = new HashSet<String>();
		/*
		ClosenessCentrality_KP='[0.342601-0.343299)' < 0.5
		|   BC_KP='[0.000172-0.000176)' < 0.5
		|   |   BC_KP='[0.000013-0.000026)' < 0.5
		*/
		roots = new ArrayList<String>();
		//p=Pattern.compile("^(.+)[<=>]+[^<=>]+$");//root
		p=Pattern.compile("^(.+)(<|(>=))[^<=>]+$");//root
		//p2=Pattern.compile("^[\\s\\|]+([^<>]+)\\s*[<=>]+[^<=>]+$");//internal nodes		
		p2=Pattern.compile("^[\\s\\|]+(.+)(<|(>=))[^<=>]+$");//internal nodes
		p3=Pattern.compile("^([^=]+)=[^=]+$");//pattern of the feature name of a boolean feature 
										      //e.g. a boolean feature: ClosenessCentrality_KP='[0.325279-0.325688)'
										      //	   feature name: ClosenessCentrality_KP
		root_found=false;
		for(int i=0; i<trees.size();i++)
		{
			tree = trees.get(i);
			lines = tree.split("\n");
		    for(int j=0; j<lines.length;j++)
		    {
		    	line = lines[j];
		    	m = p.matcher(line);
		    	m2 = p2.matcher(line);
		    	if (m.matches()&&!root_found)
		    	{
		    		root = m.group(1);
		    		root = root.trim();
		    		roots.add(root);
		    		root_found=true;
		    		features.add(root);
		    	    m3 = p3.matcher(root);
		    		if (m3.matches())
		    			features_names.add(m3.group(1));
		    	}
		    	else if(m2.matches())
		    	{
		    	  feature = m2.group(1);
		    	  feature = feature.trim();
		    	  features.add(feature);
		    	  m3 = p3.matcher(feature);
		    	  if (m3.matches())
		    		  features_names.add(m3.group(1));
		    	}
		    }
		}
		
		roots_and_features = "roots="+roots.toString()+"##features="+features.toString();
		return roots_and_features;
	}	
}
