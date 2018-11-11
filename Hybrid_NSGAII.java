import jmetal.metaheuristics.nsgaII.*;
import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
//import jmetal.problems.Kursawe;
import jmetal.problems.ProblemFactory;
//import jmetal.qualityIndicator.QualityIndicator;
//import jmetal.util.Configuration;
import jmetal.util.JMException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/** 
 * Class to configure and execute the NSGA-II algorithm.  
 *     
 * Besides the classic NSGA-II, a steady-state version (ssNSGAII) is also
 * included (See: J.J. Durillo, A.J. Nebro, F. Luna and E. Alba 
 *                  "On the Effect of the Steady-State Selection Scheme in 
 *                  Multi-Objective Genetic Algorithms"
 *                  5th International Conference, EMO 2009, pp: 183-197. 
 *                  April 2009)
 */ 
/*
 * input: discrete training data, no_of_generations_to_check_convergence, operating_system ("windows" or "linux")
 * 			or
 * 		  discrete training data, discrete test data, no_of_generations_to_check_convergence, operating_system ("windows" or "linux")
 *  		or
 *  	  discrete training data, continuous training data, continuous test data, no_of_generations_to_check_convergence, operating_system ("windows" or "linux")
 *  	  
 * usage:java Hybrid_NSGAII <training_data> <test_data> <no_of_generations_to_check_convergence> <operating system "windows" or "linux">
 *
 *Input data sets are in weka arff format.
 */
public class Hybrid_NSGAII {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object 
  public static void main(String [] args) throws 
                                  JMException, 
                                  SecurityException, 
                                  IOException, 
                                  ClassNotFoundException {
    //Problem   problem   ; // The problem to solve
    Algorithm algorithm ; // The algorithm to use
    Operator  crossover ; // Crossover operator
    Operator  mutation  ; // Mutation operator
    Operator  selection ; // Selection operator
    String disc_training_data="none"; 		  //discrete training data
    String disc_test_data="none";			  //discrete test data
    String cont_training_data="none"; 		  //continuous training data
    String cont_test_data="none";			  //continuous test data
    int no_of_generations_to_check_convergence = 3;  
    //QualityIndicator indicators ; // Object to get quality indicators
    HashMap<String,Double>  parameters ; // Operator parameters
    ReductsSearch rs = null;
    //indicators = null ;
    int maxEvaluations;
    int generations;
    int populationSize;
    double prob_crossover;
    double prob_mutation;
    OutputStreamWriter out;
    String os=null;//operating system
    // Logger object and file to store log messages
   // logger_      = Configuration.logger_ ;
   // fileHandler_ = new FileHandler("NSGAII_main.log"); 
   // logger_.addHandler(fileHandler_) ;
    String classifier;
    String linux_class_path = "/home/david/Downloads/weka-3-6-12/weka.jar";
    String windows_class_path = "C:\\Program Files\\Weka-3-6-12\\weka.jar";
    String cmd="none";
    Utility util;
    double accuracy=0d;
    
    classifier="naive bayes";
    //classifier="bayesian network";
    //classifier="logistic regression";
    //classifier="C4.5";
    //classifier="random forest";
    //classifier="decision table";
    //classifier = "KNN";
    //===NSGAII parameters setting===
    //generations = 5;//a generation is an execution of the evolutionary process consisting of selection, crossover and mutation.
    //generations = 1; 
    //generations = 2;
    //generations = 8;
    //generations = 10;
    //generations = 20;
    generations = 30;
    //generations = 50;
    //populationSize = 500;
    populationSize = 100;
    //populationSize = 30;   
    //populationSize = 10;
    //populationSize = 20;
    //populationSize = 30;
    //prob_crossover = 0.3d;
    prob_crossover = 0.6d; //crossover probability is percentage of pairs of subsets in the population that are crossed over
    prob_mutation = 0.033d;//mutation probability is percentage of bits of each subset undergoing bit flip
    //prob_mutation = 0.001d;
    //prob_mutation = 0.3d;
    //prob_mutation = 0.4d;
    //prob_mutation = 0.5d; //large mutation probability gives small subsets
    //prob_mutation = 0.6d;
    if(args.length == 3)//input is discrete training set in weka format 
    {
    	disc_training_data = args[0];
    	no_of_generations_to_check_convergence = Integer.parseInt(args[1]);//e.g. no_of_generations_to_check_convergence = 3
        os = args[2];
        rs = new ReductsSearch(classifier,os,"Binary",disc_training_data);
        //rs.training_option="percentage split";
        rs.training_option="cross validation";
        if(os.equals("windows"))
			  cmd = ClassificationAccuracy.create_cmd(classifier,disc_training_data,windows_class_path,rs.training_option,"results_file_temp");
        else
			  cmd = ClassificationAccuracy.create_cmd(classifier,disc_training_data,linux_class_path,rs.training_option,"results_file_temp");       
    }
    else if(args.length == 4)//input are discrete training set and discrete test set in weka format 
    {
    	disc_training_data = args[0];
    	disc_test_data = args[1];
        no_of_generations_to_check_convergence = Integer.parseInt(args[2]);//e.g. no_of_generations_to_check_convergence = 3
        os = args[3];
        rs = new ReductsSearch(classifier,os,"Binary",disc_training_data,disc_test_data);
        if(os.equals("windows"))
			  cmd = ClassificationAccuracy.create_cmd2(classifier,disc_training_data,disc_test_data,windows_class_path,"results_file_temp");
        else
    	  	  cmd = ClassificationAccuracy.create_cmd2(classifier,disc_training_data,disc_test_data,linux_class_path,"results_file_temp");       
    }
    else if(args.length == 5)//input are discrete training set, continuous training set and continuous test set
    {
     disc_training_data = args[0];
     cont_training_data = args[1];
     cont_test_data = args[2];
     no_of_generations_to_check_convergence = Integer.parseInt(args[3]);
     os = args[4];
     rs = new ReductsSearch(classifier,os,"Binary",disc_training_data,cont_training_data,cont_test_data);
     if(os.equals("windows"))
		  cmd = ClassificationAccuracy.create_cmd2(classifier,cont_training_data,cont_test_data,windows_class_path,"results_file_temp");
     else
	  	  cmd = ClassificationAccuracy.create_cmd2(classifier,cont_training_data,cont_test_data,linux_class_path,"results_file_temp");       
    }
    else
    {
    	System.out.println("wrong number of input arguments.");
    	System.exit(-1);
    }
    //train weka classifier on un-reduced training set and test the classifier on test set
    util = new Utility();  
    /*try
    {*/
    	util.run_system_command(cmd,os);
    /*}
    catch(IOException e)
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
    	accuracy = rs.get_accuracy("results_file_temp");
    }
    catch(IOException e)
    {
    	System.out.println("IOException occur at get_accuracy");
    	System.exit(-1);
    }    
    out = new OutputStreamWriter(new FileOutputStream("hybrid_NSGAII_local_search.log"));
    out.write("A "+classifier+" is trained on the unreduced training set.\nThe accuracy of the classifier on the test set: "+accuracy+" %\n");
    out.write("gamma of set of all the features: "+rs.gamma_setOfAllFeatures+"\n");
    out.write("=== Hybrid NSGAII local search algorithm===\n"); 
    System.out.println("\ngenerations: "+generations+"\npopulation size: "+populationSize+"\ncrossover probability: "+prob_crossover+"\nmutation probability: "+prob_mutation+"\n");
    out.write("\ngenerations: "+generations+"\npopulation size: "+populationSize+"\ncrossover probability: "+prob_crossover+"\nmutation probability: "+prob_mutation+"\n\n");
    algorithm = new modified_NSGAII_LocalSearch(disc_training_data,no_of_generations_to_check_convergence,rs,rs.rsys,out);
    //number of generations = maxEvaluation / populationSize
    //To set N generations, set maxEvaluation to N*populationSize i.e. total no. of offspring to generate
    maxEvaluations = generations*populationSize;    
    // Algorithm parameters
    algorithm.setInputParameter("populationSize",populationSize);   
    algorithm.setInputParameter("maxEvaluations",maxEvaluations);       
    parameters = new HashMap<String,Double>() ;
    parameters.put("probability", prob_crossover) ;
    parameters.put("distributionIndex", 20.0) ;
                      
    // Crossover for Binary codification 
    crossover = CrossoverFactory.getCrossoverOperator("SinglePointCrossover", parameters);
    //crossover = CrossoverFactory.getCrossoverOperator("PMXCrossover", parameters);
    //crossover = CrossoverFactory.getCrossoverOperator("TwoPointsCrossover", parameters);  
    
    parameters = new HashMap<String,Double>() ;
    //parameters.put("probability", 1.0/problem.getNumberOfVariables()) ;
    parameters.put("probability", prob_mutation) ;
    parameters.put("distributionIndex", 20.0) ;
                      
    //Mutation for Binary codification
    mutation = MutationFactory.getMutationOperator("BitFlipMutation", parameters);                    
    //###Remove features randomly from a subset using mutation
    //BitFlipTrueBitMutation flips True bits to False bits to eliminate features from a subset
    //mutation = MutationFactory.getMutationOperator("BitFlipTrueBitMutation", parameters);                    

    // Selection Operator 
    parameters = null ;
    selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters) ;                           

    // Add the operators to the algorithm
    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("mutation",mutation);
    algorithm.addOperator("selection",selection);

    // Add the indicator object to the algorithm
    //algorithm.setInputParameter("indicators", indicators) ;
    
    // Execute the Algorithm
    //long initTime = System.currentTimeMillis();
    //SolutionSet population = algorithm.execute();
    algorithm.execute();
    //long estimatedTime = System.currentTimeMillis() - initTime;
    // Result messages 
    //logger_.info("Total execution time: "+estimatedTime + "ms");
    /*
    logger_.info("Variables values have been writen to file VAR");
    population.printVariablesToFile("VAR");    
    logger_.info("Objectives values have been writen to file FUN");
    population.printObjectivesToFile("FUN");
   */
    //Remove reduced training and test data files
    cmd = "del reduced_training_data_*.arff";
    util.run_system_command(cmd,os);
    cmd = "del reduced_test_data_*.arff";
    util.run_system_command(cmd,os);
  }
} 