import jmetal.metaheuristics.singleObjective.geneticAlgorithm.*;
import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;
import jmetal.util.Ranking;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

//to compile on command line: 
//							 1. Go to P:\downloads\java\featureselection\src> 
//							 2.	javac -cp .;..\bin GA_Reducts.java
//								 or (to compile all classes related to this class)
//							 	javac -cp .;..\bin ReductsSearch2.java ReductsSearch.java GA_Reducts.java gGA_Reducts.java RSys.java Utility.java
//to run program on command line:
//1. Go to P:\downloads\java\featureselection\src
//2. Run the commands like the following commands:
//java -cp .;..\bin GA_Reducts "P:\Nuclear Power Plant Safety Monitoring (Leeds Beckett)\project\mergeddata5class_sig_level=0.95.arff" windows > logfile
//java -cp .;..\bin GA_Reducts "P:\Nuclear Power Plant Safety Monitoring (Leeds Beckett)\project\mergeddata.arff" windows > logfile
//java -cp .;..\bin GA_Reducts "P:\Nuclear Power Plant Safety Monitoring (Leeds Beckett)\project\trainset.arff" windows > logfile
//java -cp .;..\bin GA_Reducts "P:\Nuclear Power Plant Safety Monitoring (Leeds Beckett)\project\trainset_sig_level=0.95.arff" windows > logfile
//java -cp .;..\bin GA_Reducts "P:\Nuclear Power Plant Safety Monitoring (Leeds Beckett)\data-driven approach for predicting failure scenarios in nuclear systems\oil_transient_train_T10_sig_level=0.95.arff" windows > logfile
/*
 * usage: java GA_Reducts <discrete data file> > <windows or linux>
 * input: discrete data file in weka format
 * output: results file1 
 * 		   results file2 (to be input to Matlab for neural networks training)
 * format of results file1:
 * {0,1,2,4,6}	size:	5	fitness:  1.9	relative dependency:	1	average info gain:	1	subset size ratio:	0.25
 * ...
 * format of results file2:
 * 1,2,3,5,7
 * ...    
 */

public class GA_Reducts {

  public static void main(String [] args) throws JMException, 
  												 SecurityException, 
  												 ClassNotFoundException, 
  												 IOException {
    ReductsSearch2   rs2   ;         // The problem to solve
    Algorithm algorithm ;         // The algorithm to use
    Operator  crossover ;         // Crossover operator
    Operator  mutation  ;         // Mutation operator
    Operator  selection ;         // Selection operator
    String    data_file ; 		  // a data file
    HashMap<String,Double> parameters ; // Operator parameters
    String os;
    int generations;
    int populationSize;
    double prob_crossover;
    double prob_mutation;
    OutputStreamWriter out;
    Utility util;
    int maxEvaluations = 0;//total no. of individuals of all the generations
    
    //populationSize = 100;
    populationSize = 30;
    generations = 30;
    //generations = 30;
    prob_crossover = 0.6d; //crossover probability is percentage of pairs of subsets in the population that are crossed over
    prob_mutation = 0.033d;//mutation probability is percentage of bits of each subset undergoing bit flip
   
    data_file = args[0];//input data file is a discrete data set in weka arff format
    os = args[1];//"windows" or "linux"
    rs2 = new ReductsSearch2(data_file,os); 
    out = new OutputStreamWriter(new FileOutputStream("GA_Reducts.log"));
    out.write("GA_Reducts algorithm is run on the data set: "+data_file+"\n");
    out.write("relative dependency of the set of all the features: "+rs2.gamma_setOfAllFeatures+"\n"); 
    System.out.println("\ngenerations: "+generations+"\npopulation size: "+populationSize+"\ncrossover probability: "+prob_crossover+"\nmutation probability: "+prob_mutation+"\n");
    out.write("\ngenerations: "+generations+"\npopulation size: "+populationSize+"\ncrossover probability: "+prob_crossover+"\nmutation probability: "+prob_mutation+"\n\n");     
    algorithm = new gGA_Reducts(rs2,os,data_file,out); //Generational GA
    //algorithm = new ssGA(problem); // Steady-state GA
    //algorithm = new scGA(problem) ; // Synchronous cGA
    //algorithm = new acGA(problem) ;   // Asynchronous cGA
    maxEvaluations = generations*populationSize;
    /* Algorithm parameters*/
    algorithm.setInputParameter("populationSize",populationSize);
    algorithm.setInputParameter("maxEvaluations",maxEvaluations);
    
    // Mutation and Crossover for Binary codification 
    parameters = new HashMap<String,Double>() ;
    parameters.put("probability", prob_crossover) ;
    crossover = CrossoverFactory.getCrossoverOperator("SinglePointCrossover", parameters);                   
    
    parameters = new HashMap<String,Double>() ;
    parameters.put("probability", prob_mutation) ;
    mutation = MutationFactory.getMutationOperator("BitFlipMutation", parameters);                    
    
    /* Selection Operator */
    parameters = null ;
    selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters) ;                            
    
    /* Add the operators to the algorithm*/
    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("mutation",mutation);
    algorithm.addOperator("selection",selection);
 
    /* Execute the Algorithm */
    long initTime = System.currentTimeMillis();
    SolutionSet subsets = algorithm.execute();
    long estimatedTime = System.currentTimeMillis() - initTime;
    System.out.println("Total execution time: " + estimatedTime+" milli seconds");
    util = new Utility();
    util.printRankedSolutionsToFile(rs2, subsets, "GA_Reducts_resultsfile1.");
    util.printSolutionsIndicesToFile(rs2, subsets, "GA_Reducts_resultsfile2.");
    System.out.println("Log information is saved as GA_Reducts.log.\nReducts are saved in GA_Reducts_resultsfile1 and GA_Reducts_resultsfile2.");
    /* Log messages */
    //System.out.println("Objectives values have been writen to file FUN");
    //population.printObjectivesToFile("FUN");
    //System.out.println("Variables values have been writen to file VAR");
    //population.printVariablesToFile("VAR");
    //print the population of reducts, their fitness, degree of dependency and sizes
    //output format:
    //subset number    subset		fitness		gamma 	subset size  reduct
    //			1:	    0,1    		1.95  		1		   2		  yes
    //					
    //			2:	    0,1,6		1.92		1		   3	      yes
    // 					     
    //			3:		0,1,5,7		1.9			1		   4		  no
    //					etc..
  } 
} 

