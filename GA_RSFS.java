import jmetal.metaheuristics.singleObjective.geneticAlgorithm.*;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;

import java.util.HashMap;

/**
 * This class runs a single-objective genetic algorithm (GA). The GA can be 
 * a steady-state GA (class ssGA), a generational GA (class gGA), a synchronous
 * cGA (class scGA) or an asynchronous cGA (class acGA). 
 */
/*
 * usage: java GA_RSFS <data_file> <data_file_format> <first_line_contain_attributes_names> 
 */

public class GA_RSFS {

  public static void main(String [] args) throws JMException, ClassNotFoundException {
    Problem   problem   ;         // The problem to solve
    Algorithm algorithm ;         // The algorithm to use
    Operator  crossover ;         // Crossover operator
    Operator  mutation  ;         // Mutation operator
    Operator  selection ;         // Selection operator
    String    data_file ; 		  // a data file
    String data_file_format;      // csv (csv format) or space (space delimited)
    String first_line_contain_attributes_names; // 0 or 1 
    											//0: the first line does not contain the names of the attributes 
    											//1: the first line contains the names of the attributes  		
    //int bits ; // Length of bit string in the OneMax problem
    HashMap  parameters ; // Operator parameters
    int bits;
    
    data_file = args[0];
    data_file_format = args[1];
    first_line_contain_attributes_names = args[2];
    
    problem = new ReductsSearch("Binary",data_file,data_file_format,first_line_contain_attributes_names);
    bits = problem.getNumberOfBits();
    
    //problem = new OneMax("Binary", bits);
  
    algorithm = new gGA(problem) ; // Generational GA
    //algorithm = new ssGA(problem); // Steady-state GA
    //algorithm = new scGA(problem) ; // Synchronous cGA
    //algorithm = new acGA(problem) ;   // Asynchronous cGA
    
    /* Algorithm parameters*/
    algorithm.setInputParameter("populationSize",100);
    algorithm.setInputParameter("maxEvaluations", 1000);
    /*
    // Mutation and Crossover for Real codification 
    parameters = new HashMap() ;
    parameters.put("probability", 0.9) ;
    parameters.put("distributionIndex", 20.0) ;
    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);                   

    parameters = new HashMap() ;
    parameters.put("probability", 1.0/problem.getNumberOfVariables()) ;
    parameters.put("distributionIndex", 20.0) ;
    mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);                    
    */
    
    // Mutation and Crossover for Binary codification 
    parameters = new HashMap() ;
    parameters.put("probability", 0.9) ;
    crossover = CrossoverFactory.getCrossoverOperator("SinglePointCrossover", parameters);                   
    
    parameters = new HashMap() ;
    //parameters.put("probability", 1.0/bits) ;
    parameters.put("probability", 0d) ;
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
    SolutionSet population = algorithm.execute();
    long estimatedTime = System.currentTimeMillis() - initTime;
    System.out.println("Total execution time: " + estimatedTime);

    /* Log messages */
    System.out.println("Objectives values have been writen to file FUN");
    population.printObjectivesToFile("FUN");
    System.out.println("Variables values have been writen to file VAR");
    population.printVariablesToFile("VAR");
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

