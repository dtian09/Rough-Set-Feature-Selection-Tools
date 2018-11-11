import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;

import java.io.*;
import java.util.*;
import java.util.regex.*;
//to compile this class:
// javac -cp .;..\bin ReductsSearch.java
//
//ReductsSearch class to define multi-objective reducts search problems: 
//objectives: subset sizes ratio
//			  relative dependency
//			  information gain
//			  classification accuracy
//Weka is called on command line to train and test classifiers on feature subsets.
public class ReductsSearch extends Problem {
  private static final long serialVersionUID = 236769006946647231L;
  String [][] data_matrix;
  String disc_training_set = "none";//discrete training set
  String cont_training_set = "none";//continuous training set
  String disc_test_set = "none";//discrete test set
  String cont_test_set = "none";//continuous test set
  boolean disc_training_set_only;
  String training_option="none"; //"percentage split" or "cross validation"
  boolean disc_training_set_and_disc_test_set;
  boolean cont_training_set_and_disc_training_set_and_disc_test_set;
  String classifier="none";
  //HashMap<BitSet,Float> gammas;
  HashMap<Binary,Float> gammas;
  LinkedList<String>all_variables;
  boolean gamma_setOfAllFeatures_computed = false;
  float gamma_setOfAllFeatures=0f;
  Integer numberOfBits;
  boolean displayed = false;
  RSys rsys;
  int class_index=0;
  float total_fitness=0;//sum of fitness of all subsets (parents and offsprings) of a population
  //float average_fitness_of_last_generation=0;//average fitness of population of last generation
  double rank [] = null;//ranks of features by information gain metric or chi squared correlation metric output by weka
  double info_gain [] = null;//information gain of features
  double chisquared [] = null; //chisquared of features
  Utility util;
  String linux_class_path = "/home/david/Downloads/weka-3-6-12/weka.jar";
  String windows_class_path = "C:\\Program Files\\Weka-3-6-12\\weka.jar";
  HashMap<Binary,Double> hash_accuracy;//classification accuracies of subsets
  HashMap<Binary,Float> hash_subset_size_ratio;//subset size ratio
  String os; //operating system
  
  public ReductsSearch(String classifier, String os, String solutionType, String disc_training_data) 
  {
	//Use 1 bit to represent selection of a feature. For a data set with N features,
	//the number of bits is N  
    numberOfVariables_  = 1;
    /***single objective ***/
    //numberOfObjectives_ = 1;
    /***multi-objectives ***/
    numberOfObjectives_ = 3;
    numberOfConstraints_= 0;
    problemName_        = "ReductsSearch";
    util = new Utility();
    //gammas = new HashMap<BitSet,Float>();
    gammas = new HashMap<Binary,Float>();
    hash_accuracy = new HashMap<Binary,Double>();
    hash_subset_size_ratio = new HashMap<Binary,Float>();
    this.os = os;
    this.classifier = classifier;
    disc_training_set_only = true;
    disc_training_set_and_disc_test_set = false;
    cont_training_set_and_disc_training_set_and_disc_test_set = false;    
    try{
    	data_matrix = util.load_weka_data_into_array(disc_training_data);
    }
    catch(IOException e)
    {
    	System.out.println("IOException thrown by load_weka_data_into_array");
    	System.exit(-1);
    }  
    disc_training_set = disc_training_data;
    class_index = data_matrix[0].length-1;//class attribute is the last attribute	 	   
    rsys = new RSys(data_matrix, data_matrix.length, data_matrix[0].length, class_index);
    numberOfBits = getNumberOfBits();    		   	    
    length_       = new int[numberOfVariables_];
    length_      [0] = numberOfBits ;    
    if (solutionType.compareTo("Binary") == 0)
       solutionType_ = new BinarySolutionType(this);
    else {
    	  System.out.println("ReductsSearch: solution type " + solutionType + " invalid") ;
    	  System.exit(-1);
    }
    computeDegreeOfDependencyOfAllFeatures();
  }
  
  public ReductsSearch(String classifier, String os, String solutionType, String disc_training_data, String disc_test_data) 
  {
	//Use 1 bit to represent selection of a feature. For a data set with N features,
	//the number of bits is N  
    numberOfVariables_  = 1;
    /***single objective ***/
    //numberOfObjectives_ = 1;
    /***multi-objectives ***/
    numberOfObjectives_ = 3;
    numberOfConstraints_= 0;
    problemName_        = "ReductsSearch";
    util = new Utility();
    gammas = new HashMap<Binary,Float>();
    hash_accuracy = new HashMap<Binary,Double>();
    hash_subset_size_ratio = new HashMap<Binary,Float>();
    this.os = os;
    this.classifier = classifier;
    disc_training_set_only = false;
    disc_training_set_and_disc_test_set = true;
    cont_training_set_and_disc_training_set_and_disc_test_set = false;    
    try{
    	data_matrix = util.load_weka_data_into_array(disc_training_data);
    }
    catch(IOException e)
    {
    	System.out.println("IOException thrown by load_weka_data_into_array");
    	System.exit(-1);
    }  
    disc_training_set = disc_training_data;
    disc_test_set = disc_test_data;
    class_index = data_matrix[0].length-1;//class attribute is the last attribute	 	   
    rsys = new RSys(data_matrix, data_matrix.length, data_matrix[0].length, class_index);
    numberOfBits = getNumberOfBits();    		
    solutionType_ = new BinarySolutionType(this) ;    	    
    length_       = new int[numberOfVariables_];
    length_      [0] = numberOfBits ;    
    if (solutionType.compareTo("Binary") == 0)
       solutionType_ = new BinarySolutionType(this);
    else {
    	  System.out.println("ReductsSearch: solution type " + solutionType + " invalid") ;
    	  System.exit(-1);
    }
    computeDegreeOfDependencyOfAllFeatures();
  }
    
  public ReductsSearch(String classifier, String os, String solutionType, String disc_training_data, String cont_training_data, String cont_test_data) 
  {
	//Use 1 bit to represent selection of a feature. For a data set with N features,
	//the number of bits is N  
    numberOfVariables_  = 1;
    /***single objective ***/
    //numberOfObjectives_ = 1;
    /***multi-objectives ***/
    numberOfObjectives_ = 3;
    numberOfConstraints_= 0;
    problemName_        = "ReductsSearch";
    util = new Utility();
    gammas = new HashMap<Binary,Float>();
    hash_accuracy = new HashMap<Binary,Double>();
    hash_subset_size_ratio = new HashMap<Binary,Float>();
    this.os = os;
    this.classifier = classifier;
    disc_training_set_only = false;
    disc_training_set_and_disc_test_set = false;
    cont_training_set_and_disc_training_set_and_disc_test_set = true;    
    try{
    	data_matrix = util.load_weka_data_into_array(disc_training_data);
    }
    catch(IOException e)
    {
    	System.out.println("IOException thrown by load_weka_data_into_array");
    	System.exit(-1);
    }
    disc_training_set = disc_training_data;
    cont_training_set = cont_training_data;
    cont_test_set = cont_test_data;
    class_index = data_matrix[0].length-1;//class attribute is the last attribute	 	   
    rsys = new RSys(data_matrix, data_matrix.length, data_matrix[0].length, class_index);
    numberOfBits = getNumberOfBits();    		
    solutionType_ = new BinarySolutionType(this) ;    	    
    length_       = new int[numberOfVariables_];
    length_      [0] = numberOfBits ;    
    if (solutionType.compareTo("Binary") == 0)
       solutionType_ = new BinarySolutionType(this);
    else {
    	  System.out.println("ReductsSearch: solution type " + solutionType + " invalid") ;
    	  System.exit(-1);
    }
    computeDegreeOfDependencyOfAllFeatures();
  }
    
  public int getNumberOfBits()
  {
   //a bit string does not represent the class attribute, 
   //so number of bits = number of columns - 1
	return data_matrix[0].length-1;
  }
  
  public void computeDegreeOfDependencyOfAllFeatures()
  {
	//###compute degree of dependency of set of all the features ###
	//A subset is an approximate reduct if the gamma of the subset == gamma of the set of all the features
	//For noisy data sets, gamma of all the features is < 1.0. 
	//For good-quality data sets, gamma of all the features is 1.0.
	//To check whether a subset is an approximate reduct, its gamma is compared with gamma of the set of all the features   
	  Binary variable_AllFeatures; 
	  if (gamma_setOfAllFeatures_computed == false)
	    {
	     variable_AllFeatures = new Binary(numberOfBits);
	     for(int i = 0; i < numberOfBits; i++)
	     {
	    	 variable_AllFeatures.setIth(i,true);
	     }
	     gamma_setOfAllFeatures = relative_dependency(rsys,variable_AllFeatures);
	     gamma_setOfAllFeatures_computed = true;
	    }
	    
	    if(displayed == false)
	    {
	      System.out.println("degree of relative dependency of set of all features: "+gamma_setOfAllFeatures);
	      displayed = true;
	    }  
	    
  }
  
  public void evaluate(Solution solution) {
	//This method evaluates the fitness of a candidate solution
	//fitness function = degree of dependency of B + (|A|-|B|)/|A|
	//where A is the set of all the features; B is a feature subset;
	//(|A|-|B|)/|A| is subset size ratio  
    Binary variable ;
    //Binary variable_AllFeatures;
    float gamma = 0;
    float ratio = 0;
    double accuracy =0;
    //double obj1=0d;
    //double obj2=0d;
    //double obj3=0d;
   
    variable = ((Binary)solution.getDecisionVariables()[0]) ;
     
    //multi-objectives
    /*Objective function 1 = relative dependency of the subset – (|B|/|A|)^(dimensionality of dataset – size of the subset) */
    /*Objective function 2 = the average information gain of the features of a feature subset.*/
    /*Objective function 3 = the classification accuracy of the naive bayes classifier trained on reduced training set using a feature subset.*/
		
    //objective 1: relative dependency
    gamma = relative_dependency(rsys,variable);
    if (hash_subset_size_ratio.containsKey(variable))
    	ratio = hash_subset_size_ratio.get(variable);
    else
    {
    	ratio = subsetSizeRatio(variable);/* ratio = (|A|-|B|)/|A| where A is the set of all the features; B is a feature subset*/
    	hash_subset_size_ratio.put(variable, ratio);
    }
    //obj1 = gamma + ratio;
    //obj1 = gamma;
    
    //objective 2: ratio = (|A|-|B|)/|A|
    //obj2 = ratio;
    /*ratio2 = getReductSizeRatio2(variable); //ratio2 = |B|/|A|																   
     subset_size = get_subset_size(variable);
     obj2 = gamma - Math.pow(ratio2,((double)numberOfBits - subset_size));//obj2 = gamma - (|B|/|A|)^(total no. of features of data set - size of subset);
     solution.setObjective(1,obj2);*/       
    //objective 2: average information gain
    //info_gain = features_rank_ratio(os, data_file2,data_file_format2,variable,"info_gain");//1 - total_rank_subset/total_rank_all_features
    //obj2 = average_importance_of_features(variable,"info_gain"); //average information gain of features of a subset
    //info_gain = average_importance_of_features(variable,"info_gain"); //average information gain of features of a subset
    //double chisquared = features_rank_ratio(os, data_file2,data_file_format2,variable,"chisquared");
    //favours small subset over large subsets when the total info gain of the 2 subsets are the same
    //obj2 = Math.pow(info_gain,subset_size);
    //obj2 = chisquared;
    //obj2 = Math.pow(chisquared,subset_size);
    //solution.setObjective(1,obj2);   
    //objective 3: classification accuracy of Naive Bayes  
    if (!hash_accuracy.containsKey(variable))
    {
    	accuracy = compute_accuracy(variable);
    	hash_accuracy.put(variable,accuracy);
    }     
    else
    	accuracy = hash_accuracy.get(variable);
    solution.setObjective(0,accuracy);//objective 1: classifier accuracy
    solution.setObjective(1,gamma);//objective 2: gamma of subset
    solution.setObjective(2,ratio);//objective 3: subset size ratio
    total_fitness += accuracy + gamma + ratio;
  }
  
  double compute_accuracy(Binary subset)
  { //compute objective 3: classification accuracy of feature subset
	  String reduct;
	  double accuracy=-888;
	  String cmd="none";
	  String ext = Integer.toString(new Random().nextInt(9000000));
	  String reduced_train_set="reduced_training_data_"+ext+".arff";
	  String reduced_test_set = "reduced_test_data_"+ext+".arff";
	  
	  reduct = reduct_indices(subset);
	  if(os.equals("windows"))
	  {
		  if(disc_training_set_only)
		  {			 
			  util.reduce_weka_arff_file(reduct,windows_class_path,os,Integer.toString(class_index+1),disc_training_set,reduced_train_set);
			  cmd = ClassificationAccuracy.create_cmd(classifier,reduced_train_set,windows_class_path,training_option,"results_file_temp");
		  }
		  else if (disc_training_set_and_disc_test_set)//Input are a discrete training set and a discrete test set    
		  {   
			  util.reduce_weka_arff_file(reduct,windows_class_path,os,Integer.toString(class_index+1),disc_training_set,reduced_train_set);
			  util.reduce_weka_arff_file(reduct,windows_class_path,os,Integer.toString(class_index+1),disc_test_set,reduced_test_set);
			  cmd = ClassificationAccuracy.create_cmd2(classifier,reduced_train_set,reduced_test_set,windows_class_path,"results_file_temp");
		  }
		  else if(cont_training_set_and_disc_training_set_and_disc_test_set)//Input are a discrete training set, a continuous training set and a continuous test set    
		  {
			  util.reduce_weka_arff_file(reduct,windows_class_path,os,Integer.toString(class_index+1),cont_training_set,reduced_train_set);
			  util.reduce_weka_arff_file(reduct,windows_class_path,os,Integer.toString(class_index+1),cont_test_set,reduced_test_set);
			  cmd = ClassificationAccuracy.create_cmd2(classifier,reduced_train_set,reduced_test_set,windows_class_path,"results_file_temp");
		  }
		  else
		  {
			  System.out.println("disc_training_set_only: "+disc_training_set_only+", disc_training_set_and_disc_test_set: "+disc_training_set_and_disc_test_set+", cont_training_set_and_disc_training_set_and_disc_test_set: "+cont_training_set_and_disc_training_set_and_disc_test_set);
			  System.exit(-1);
		  }
	  }
	  else if(os.equals("linux"))
	  {
		  if(disc_training_set_only)
		  {
			  training_option="percentage split";
			  util.reduce_weka_arff_file(reduct,linux_class_path,os,Integer.toString(class_index+1),disc_training_set,reduced_train_set);
			  cmd = ClassificationAccuracy.create_cmd(classifier,reduced_train_set,linux_class_path, training_option,"results_file_temp");
		  }
		  else if (disc_training_set_and_disc_test_set)    
		  {
			  util.reduce_weka_arff_file(reduct,linux_class_path,os,Integer.toString(class_index),disc_training_set,reduced_train_set);
			  util.reduce_weka_arff_file(reduct,linux_class_path,os,Integer.toString(class_index+1),disc_test_set,reduced_test_set);
			  cmd = ClassificationAccuracy.create_cmd2(classifier,reduced_train_set,reduced_test_set,linux_class_path,"results_file_temp");
		  }
		  else if(cont_training_set_and_disc_training_set_and_disc_test_set)
		  {
			  util.reduce_weka_arff_file(reduct,linux_class_path,os,Integer.toString(class_index),cont_training_set,reduced_train_set);
			  util.reduce_weka_arff_file(reduct,linux_class_path,os,Integer.toString(class_index+1),cont_test_set,reduced_test_set);	
			  cmd = ClassificationAccuracy.create_cmd2(classifier,reduced_train_set,reduced_test_set,linux_class_path,"results_file_temp");
		  }
		  else
		  {
			  System.out.println("disc_training_set_only: "+disc_training_set_only+", disc_training_set_and_disc_test_set: "+disc_training_set_and_disc_test_set+", cont_training_set_and_disc_training_set_and_disc_test_set: "+cont_training_set_and_disc_training_set_and_disc_test_set);
			  System.exit(-1);
		  }
	  }
	  else
	  {
		  System.out.println("invalid os: "+os);
		  System.exit(-1);
	  }
	  System.out.println("classifier training and performance evaluation\n"+cmd);
	  if(new File(reduced_train_set).length()==0 || new File(reduced_train_set).length()==0L)
	  {
		  System.out.println(reduced_train_set+" has 0 bytes or does not exist.");
		  System.exit(-1);
	  }
	  //train weka classifier and test the classifier on test set
	  //try
	  //{
		  util.run_system_command(cmd,os);
	  //}
	  /* catch(IOException e)
	  {
		  System.out.println("IOException in running "+cmd);
		  System.exit(-1);
	  }
	  catch(InterruptedException e)
	  {
		  System.out.println("InterruptedException in running "+cmd);
		  System.exit(-1);
	  }*/
	  if(new File("results_file_temp").length()==0 || new File("results_file_temp").length()==0L)
	  {
		  System.out.println("results_file_temp has 0 bytes or does not exist.");
		  System.exit(-1);
	  }
	  //get accuracy of classifier from weka output file
	  try{
		  accuracy=get_accuracy("results_file_temp");
	  }
	  catch(IOException e)
	  {
		  System.out.println("IOException occur at get_accuracy");
		  System.exit(-1);
	  }
	  return accuracy;     
  }
  
  double get_accuracy(String weka_results_file)throws IOException
  {
	  Pattern p1, p2;
	  BufferedReader in;
	  String line;
	  Matcher m1, m2;
	  String accuracy=null;
	  boolean accuracy_obtained=false;
	  
	  if(disc_training_set_only)
		  if(training_option.equals("percentage split"))
			  p1=Pattern.compile("^=== Error on test split ===$");
		  else
			  p1=Pattern.compile("^=== Stratified cross-validation ===$");
	  else
		  p1=Pattern.compile("^=== Error on test data ===$");
	  p2=Pattern.compile("^Correctly Classified Instances\\s+[0-9]+\\s*([0-9.]+)\\s+%$");//matches "Correctly Classified Instances       14           60.8696% "  
	  in=new BufferedReader(new FileReader(weka_results_file));
	  line = in.readLine();
	  while(line!=null)
	  {
		  m1=p1.matcher(line);
		  if(m1.matches())
		  {
			  line = in.readLine();
			  while(line!=null)
			  {
				  m2=p2.matcher(line);
				  if(m2.matches())
				  {
					  accuracy = m2.group(1);
					  accuracy_obtained = true;
					  break;
				  }
				  else
					  line = in.readLine();
			  }
			  if(accuracy_obtained)
				  break;
			  else
			  {
				  System.out.println("Test accuracy is not found in "+weka_results_file);
				  System.exit(-1);
			  }
		  }
		  else
			  line = in.readLine();
	  }
	  in.close();
	  return Double.parseDouble(accuracy);
  }
  
  public double average_importance_of_features(Binary variable,String metric)
  {
	 //compute the average information gain of a feaure subset =
	 //				        sum of information gain of features of a subset / size of the subset
	  
	 if (metric.equals("info_gain"))
	 {
		if(info_gain == null)//information gain of features have not been computed yet
		{	
			 info_gain = new double[data_matrix[0].length-1];
		     get_features_scores("info_gain");
		}
	}
	 else if(metric.equals("chisquared"))
	 {
		if(chisquared == null)
		{
			chisquared = new double[data_matrix[0].length-1];
			get_features_scores("chisquared");
		}
	 }
	 else
	 {
		 System.out.println("invalid metric: "+metric+" valid metrics: info_gain or chisquared");
		 System.exit(-1);
	 }
	  return average(variable);
  }
  
  void get_features_scores (String metric)
  { 
	//run information gain features ranker or chi squared ranker of weka 
	//and read the rank into 'rank' array (ith element of array = the rank of ith feature)
	  String weka_output_file;
	  
	  weka_output_file = run_weka_feature_ranking_command(metric);
	  if(metric.equals("info_gain"))
	  {   
		  //System.out.println("numberOfBits: "+numberOfBits);
		  info_gain = new double[numberOfBits];
		  try
		  {
			  info_gain = get_features_scores_from_weka_output_file(weka_output_file,info_gain);
		  }
		  catch(IOException e)
		  {
			  System.out.println("IOException in calling get_features_score_from_weka_output_file on "+weka_output_file);
			  System.exit(-1);
		  }
	  }
	  else if(metric.equals("chisquared"))
	  {
		  chisquared = new double[numberOfBits];
		  try
		  {
			  chisquared = get_features_scores_from_weka_output_file(weka_output_file,chisquared);
		  }
		  catch(IOException e)
		  {
			  System.out.println("IOException in calling get_features_rank_from_weka_output_file on "+weka_output_file);
			  System.exit(-1);
		  }
	  }
	  else
	  {
		  System.out.println("invalid metric in get_features_scores: "+metric+" valid metrics: info_gain or chisquared");
		  System.exit(-1);
	  }
	  remove_temporary_files(weka_output_file);
  }
  
  String reduct_indices(Binary subset)
  {
	  String reduct=null;
	  int i2=0;//next index after 1st true bit 
	  for(int i=0; i < numberOfBits; i++)
		  if(subset.bits_.get(i))
		  {
			  reduct=Integer.toString(i+1);//Add 1 to index as indices start from 1 in weka		  
	  		  i2=i+1;//next index after 1st true bit index
			  break;
		  }
	  for(int i=i2; i < numberOfBits; i++)
		  if(subset.bits_.get(i))
			  reduct+=","+Integer.toString(i+1);
	  return reduct;
  }
  
  double average(Binary subset)
  {
	  //average of the scores of the features in the subset
	  double total_scores=0;
	  for(int i=0; i < numberOfBits; i++)
		  if(subset.bits_.get(i))
			  total_scores += info_gain[i];
	  return (total_scores/get_subset_size(subset));
  }
  
  double totalscore(Binary subset)
  {//sum of scores of features in the subset
	  double total_scores=0;
	  for(int i=0; i < numberOfBits; i++)
		  if(subset.bits_.get(i))
			  total_scores += info_gain[i];
	  return total_scores;
  }
  
  double features_rank_ratio(String os, String data_file,String data_file_format,Binary variable,String metric)
  {
	 //compute the information gain rank ratio =
	 //				        1 - (sum of ranks of features of subset / sum of ranks of all features of data set)
	  
	 if (metric.equals("info_gain"))
	 {
		  if(rank==null)
		       get_rank(os, data_file,data_file_format,"info_gain");
	 }
	 else if(metric.equals("chisquared"))
	 {
		 if(rank==null)
		       get_rank(os, data_file,data_file_format,"chisquared");
	 }
	 else
	 {
		 System.out.println("invalid metric in features_rank_ratio: "+metric+" valid metrics: info_gain or chisquared");
		 System.exit(-1);
	 }
	 
	  return rank_ratio(variable);
  }
  
  double rank_ratio(Binary variable)
  {
	  double total_rank_all_features=0;
	  double total_rank_subset=0;
	  
	  for(int i=0; i < numberOfBits; i++)
		         total_rank_all_features += rank[i];
	  
	  for(int i=0; i < numberOfBits; i++)
		  	if(variable.bits_.get(i))
		  		  total_rank_subset += rank[i];
	  
	  return (1 - total_rank_subset/total_rank_all_features);
  }
  
  void get_rank (String os, String data_file, String data_file_format, String metric)
  { 
	//run information gain features ranker or chi squared ranker of weka 
	//and read the rank into 'rank' array (ith element of array = the rank of ith feature)
	  String weka_output_file;
	  
	  weka_output_file = run_weka_feature_ranking_command(metric);
	  try
  	  {
	   rank = get_features_rank_from_weka_output_file(weka_output_file,rank);
  	  }
  	  catch(IOException e)
  	  {
  		System.out.println("IOException in calling get_features_rank_from_weka_output_file on "+weka_output_file);
		System.exit(-1);
  	  }
  	  remove_temporary_files(weka_output_file);
  }
  
  void remove_temporary_files(String file)
  {
	  String cmd=null;
	  if(os.equals("windows"))
		  cmd = "del \""+file+"\"";
	  else if (os.equals("linux"))
		 cmd = "rm \""+file+"\"";
	  else
	  {  
		  System.out.println("invalid os: "+os);
		  System.exit(-1);
	   }	  
	  /*try
	  {*/
		  util.run_system_command(cmd,os);
	  /*}
	  catch(IOException e)
	  {
		  System.out.println("IOException in removing "+file);
		  System.exit(-1);
	  }
	  catch(InterruptedException e)
	  {
		  System.out.println("InterruptedException removing "+file);
		  System.exit(-1);
	  }*/
  }

  double [] get_features_scores_from_weka_output_file(String weka_output_file, double [] scores) throws IOException
  {
	   BufferedReader in=null;
	   String line=null;
	   Pattern p;
	   Matcher m;
	   String feature_score=null;
	   int feature_index;
	   String feature_index_str=null;
	   
	   //format: 0.0204702    17 diag_1
	   p=Pattern.compile("^\\s+([\\d,\\.]+)\\s+([\\d]+)\\s+[\\w\\W]+$");	   
	   in = new BufferedReader(new FileReader(weka_output_file));
	   line = in.readLine();
	   while(line!=null)
	   { 
		  m = p.matcher(line);
		  if(m.matches())
		  {
			  //System.out.println("matched: "+line);
			  feature_score = m.group(1);
			  feature_index_str = m.group(2);
			  //System.out.println(feature_index_str);
			  feature_index = Integer.parseInt(feature_index_str) - 1;
			  scores[feature_index] = Double.parseDouble(feature_score);
		  }
		  line = in.readLine();
	   }
	   in.close();
	   return scores;
  }
  /*
  String create_weka_arff_file_and_weka_output_filename()
  {//create a weka .arff file name and a weka output file name
	//If input file format is csv, create a weka .arff file from the csv file
	//return: the weka .arff file name and the weka output file name
	  Random randomGenerator;
	  String weka_arff_file=null;
	  String weka_output_file=null;
	  
	  randomGenerator = new Random();
	  if(data_format.equals("csv"))
	  {
	   weka_arff_file = randomGenerator.nextInt(1000000)+".arff";
	   weka_output_file =Integer.toString(randomGenerator.nextInt(9000000))+".output";
	   try{
		  util.create_weka_arff_file(data_matrix,disc_training_set,weka_arff_file);//create a weka file from a csv file
	   }
	   catch(IOException e)
	   {
		  System.out.println("IOException in create_weka_arff_file.");
		  System.exit(-1);
	   }
	  }
	  else if(data_format.equals("weka"))
	  {
		  weka_arff_file = disc_training_set;
  	      weka_output_file =Integer.toString(randomGenerator.nextInt(9000000))+".output"; 	      
	  }
	  else
	  {
		  System.out.println("invalid data file format\n valid data file format: csv or weka");
	  }
	  return weka_arff_file+'#'+weka_output_file;
  }
  */
  String run_weka_feature_ranking_command(String metric)
  {
	  String cmd=null;
	  String weka_output_file=null;
	  String class_path=null;
	  Random randomGenerator;
	  //String output_dir=".\\";//current directory
	  String output_dir="C:\\Users\\tian03\\Downloads\\temp\\";
	  if (os.equals("windows"))
	      class_path = windows_class_path;
	  else if (os.equals("linux"))
		  class_path = linux_class_path;
	  else
	  {
		  System.out.println("invalid os: "+os);
		  System.exit(-1);
	  }
	  randomGenerator = new Random();
	  weka_output_file = output_dir+Integer.toString(randomGenerator.nextInt(9000000))+".output";
	  if(metric.equals("info_gain"))
	  {//command: java -cp weka.jar weka.attributeSelection.InfoGainAttributeEval -c 9 -i "./data/diabetes.arff" > "./rank"
	    cmd ="java -cp \""+class_path+"\" weka.attributeSelection.InfoGainAttributeEval -i \""+disc_training_set+"\" > \""+weka_output_file+"\"";
	  }
	  else if(metric.equals("chisquared"))//chisquared ranker
	  {
	    cmd ="java -cp \""+class_path+"\" weka.attributeSelection.ChiSquaredAttributeEval -i \""+disc_training_set+"\" > \""+weka_output_file+"\"";
	  }
	  else
	  {
		  System.out.println("invalid metric: "+metric+"\n valid metric: info_gain or chisquared");
		  System.exit(-1);
	  }	 
	  util.run_system_command(cmd,os);
	  System.out.println(cmd);
	  return weka_output_file;
  }
  
   double [] get_features_rank_from_weka_output_file(String weka_output_file, double [] rank) throws IOException
   {
	   BufferedReader in=null;
	   String line=null;
	   String [] features_rank;
	   Pattern p;
	   Matcher m;
	   String features_rank_Str=null;
	   int f_index;
	   
	   p=Pattern.compile("^\\s*Selected attributes:\\s*([\\d,]+)\\s*:\\s*[\\d]+\\s*$");  
	   in = new BufferedReader(new FileReader(weka_output_file));
	   line = in.readLine();
	   while(line!=null)
	   {
		  line = in.readLine();
		  m = p.matcher(line);
		  if(m.matches())
		  {
			  features_rank_Str = m.group(1);
			  break;
		  }
	   }
	   
	   features_rank = features_rank_Str.split(",");
	   rank = new double [features_rank.length];
	   
	   for(int i=0; i < features_rank.length; i++)
	   {
		   f_index = Integer.parseInt(features_rank[i]) - 1;
		   rank[f_index] = (double) (i+1);
	   }
	   in.close();
	   return rank;
   }
   
  public float relative_dependency (RSys rsys, Binary variable)
  {
	//relative dependency of a feature subset
	  //BitSet subset=null;
	  float gamma=0f;
	  /*
	  subset = new BitSet(numberOfBits+1);//add the bit representing the class index 
	  for (int i = 0; i < numberOfBits; i++)
	  {
	   if(variable.bits_.get(i))
		   subset.set(i);
	  }
	  if(gammas.containsKey(subset))
		  gamma = (Float) gammas.get(subset);
	  else
	  {
		  gamma = rsys.relativeDependency(subset);
		  gammas.put(subset, gamma);		
	  }
	  */
	  if(gammas.containsKey(variable))
		  gamma = (Float) gammas.get(variable);
	  else
	  {    
		  BitSet subset;
		  subset = new BitSet(numberOfBits+1);//add the bit representing the class index 
		  for (int i = 0; i < numberOfBits; i++)
		  {
		   if(variable.bits_.get(i))
			   subset.set(i);
		  }
		  gamma = rsys.relativeDependency(subset);
		  gammas.put(variable, gamma);		
	  }
	  return gamma;
  }
 
  public float get_subset_size(Binary variable)
  {//get number of true bits of a subset
	  float subset_size=0;
	  for (int i = 0; i < numberOfBits; i++)
	  {
		  if(variable.bits_.get(i))
			  subset_size++;
	  }
	  return subset_size;
  }
  
  public float subsetSizeRatio(Binary variable)
  { //compute (|A|-|B|)/|A|, the subset size ratio
	//where A is the set of all the features; B is a feature subset
	 
	  float subset_size=0;
	  float dimensionality=0;
	  int n;
	 	  
	  dimensionality = data_matrix[0].length-1;//the class attribute is not counted
      /*
	  n = data_matrix[0].length-1;//the class attribute is not counted
	  for (int i = 0; i<n; i++)
	  {
	    if(variable.bits_.get(i))
	        subset_size++;
	  }*/
	  subset_size = get_subset_size(variable);
	  //System.out.println("subset size:" + subset_size);
	  return (dimensionality-subset_size)/dimensionality;
  }
  
  public float subsetSizeRatio2(Binary variable)
  { //compute |B|/|A|, the subset size ratio
	//where A is the set of all the features; B is a feature subset
	  float subset_size=0;
	  float dimensionality=0;
	  int n;
	 	  
	  dimensionality = data_matrix[0].length-1;//the class attribute is not counted
      n = data_matrix[0].length-1;//the class attribute is not counted
      
	  for (int i = 0; i<n; i++)
	  {
	    if(variable.bits_.get(i))
	        subset_size++;
	  }
	  //System.out.println("subset size:" + subset_size);
	  return subset_size/dimensionality;
  }
}

  /*
  public float degree_of_dependency(RSys rsys, Binary variable)
  { //input: a solution candidate representing a feature subset 
	//       a csv data file with first line containing the names of the attributes
	//output: degree of dependency of the feature subset
	//effect: rsys is updated in terms of the partitions of the data set
	//  
	  float gamma = 0;
	  float bestGamma = -1;
	  int n=0;
	  String subset="";
	  float max_so_far=-1;
	  
	  n = variable.getNumberOfBits();
	      
	  for (int i = 0; i<n; i++)
	  {
	   if(variable.bits_.get(i))
	   {    
		    if(subset.equals(""))
		    	subset = ""+i;
		    else
		        subset = subset+","+i;
		    
		    if(gammas.containsKey(subset))
		    {
		      gamma = (Float) gammas.get(subset);
		      rsys.current_missing= (Integer)current_missings.get(subset);
		    }
		    else
		    {
		     rsys.current_missing=0;
	         gamma = rsys.calculateGamma(i);
	         gammas.put(subset,gamma);
	         current_missings.put(subset,rsys.current_missing);
	         //if gamma of this subset == gamma of set of all features
	         //then the subset is an approximate reduct, so
	         //     keep a record of the approximate reduct
	         //if gamma of the set of all features is < 1
	         //then gamma of a feature subset is also < 1 
	         //     and can be > than gamma of set of all features
	         //		    due error in calculation
	         //e.g. gamma of the set of all features == 0.876520
	         //		gamma of a feature subset == 0.876521
	         if(gamma >= gamma_setOfAllFeatures)
	            candidate_reducts.add(subset);
		    }
		    
	        if(gamma>bestGamma)
		    {
		     bestGamma=gamma;
	         rsys.best_missing = rsys.current_missing;
	         //####
	         if(gamma >= gamma_setOfAllFeatures)
	             break;
	         //####
		    }
	        //####
	        if(max_so_far != 0.0 && bestGamma != 0.0 && max_so_far>=bestGamma)//previous gamma better than new gamma   
	        {    
	        	bestGamma = max_so_far;
	        	break;
	        }
	        else
	        {
	          max_so_far = bestGamma;
	          if(bestGamma >= gamma_setOfAllFeatures)
	          {
	             break;
	          }        	 
	        }
	        //####
	        //System.out.println(subset+": gamma: "+gamma);
	        rsys.R_ind = rsys.partition(rsys.partitions[i]);
	   }
	  }
	   
	  return bestGamma;	   
  }

  public float degree_of_dependency_AllFeatures(RSys rsys, Binary variable)
  { //Compute degree of dependency of the set of all features of a data set
	//effect: rsys is updated in terms of the partitions of the data set
	  
	  float gamma = 0;
	  float bestGamma = -1;//best gamma among new subset and previous subsets
	  int n=0;
	  float max_so_far=-1;//best gamma among previous subsets
	  
	  n = variable.getNumberOfBits();
	      
	  for (int i = 0; i<n; i++)
	  {  
		    rsys.current_missing=0;	   
	        gamma = rsys.calculateGamma(i);	     	   
	        System.out.println(i+": "+gamma);
	        if(gamma>bestGamma)
		    {
		     bestGamma=gamma;
	         rsys.best_missing = rsys.current_missing;
	         if(gamma==1)
	             break;
		    }
	        System.out.println("bestGamma: "+bestGamma+" max_so_far: "+max_so_far);
	        
	        if(max_so_far != 0.0 && bestGamma != 0.0 && max_so_far>=bestGamma)//if best previous gamma >= new gamma   
	        {   
	        	bestGamma = max_so_far;
	        	break;
	        }
	         else
	        {
	          max_so_far = bestGamma;
	          if(bestGamma==1)
	          {
	             break;
	          }        	 
	        }
	        rsys.R_ind = rsys.partition(rsys.partitions[i]);
	   }   
	   return bestGamma;	   
  }
  */
  /*
  public float degree_of_dependency_AllFeatures2(RSys rsys, Binary variable)
  { //Compute degree of dependency of the set of all features of a data set using QuickReduct
	//effect: rsys is updated in terms of the partitions of the data set
	  
	  float gamma = 0;
	  float bestGamma = -1;
	  int n=0;
	  float max_so_far=-1;
	  HashSet<String> subset = new HashSet<String>();
	  int best=0;
	  
	  n = variable.getNumberOfBits();
	  /*
	  while(true) 
	    {
	      for(int a=0;a<attributes;a++) 
	      {
		   if(!reduct.get(a)&&a!=class_index0)
	 	   {
		    temp=(BitSet)reduct.clone();
		    temp.set(a);
		   
		    current_missing=0;
		    gamma=calculateGamma(a);//gamma of the new subset by adding a new feature to the current subset
	        //System.out.println(" gamma of selecting "+a+" = "+gamma);
		    if(gamma>bestGamma)
		    {
		     bestGamma=gamma;
		     best=a;
		     best_missing=current_missing;
		     if(gamma==1)
	          break;
		    }
		   }
	      }
	      if(max_so_far>=bestGamma)//previous gamma better than new gamma   
	         break;
	      else
	      {
		    max_so_far = bestGamma;
		    reduct.set(best);
		    System.out.println("gamma of "+reduct.toString()+" = "+bestGamma);
	        if(bestGamma==1)
	        {
	          break;
	        }
		    R_ind = partition(partitions[best]);
	      }
	    }
	  */
  /*
	  while(true)
	  {	  
	    for (int i = 0; i<n; i++)
	    {
	    	System.out.println(subset);
	    	if(subset.contains(Integer.toString(i))==false)
	    	{
	    	  rsys.current_missing=0;	   
	          gamma = rsys.calculateGamma(i);	     	   
	          System.out.println(i+": "+gamma);
	          if(gamma>bestGamma)
		      {
		       bestGamma=gamma;
		       best = i;
	           rsys.best_missing = rsys.current_missing;
	           if(gamma==1)
	             break;
		      }
	    	}
	    }
	    if(max_so_far >0 && bestGamma >0 && max_so_far>=bestGamma)//previous gamma better than new gamma   
	    {
	    	bestGamma = max_so_far;
	    	break;
	    }
	    else
	    {
	        max_so_far = bestGamma;
	        subset.add(Integer.toString(best));
	        if(bestGamma==1)
	             break;
	        rsys.R_ind = rsys.partition(rsys.partitions[best]);
	    }
	  }   
	  return bestGamma;	   
  }
  */
