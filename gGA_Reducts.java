import jmetal.core.*;
import jmetal.encodings.variable.Binary;
import jmetal.util.JMException;
import jmetal.util.comparators.ObjectiveComparator;

import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Comparator;

/** 
 * Class implementing a generational genetic algorithm to find reducts and approximate reducts
 */
//to compile this class:
//javac -cp .;..\bin gGA_Reducts.java

public class gGA_Reducts extends Algorithm {
	
	ReductsSearch rs;
	ReductsSearch2 rs2;
	OutputStreamWriter logfile;
	Comparator comparator;
	
  public gGA_Reducts(ReductsSearch2 rs2, String os, String dataset, OutputStreamWriter logfile){
    super(rs2);
    this.rs = rs2.rs;
    this.rs2 = rs2;
    this.logfile = logfile;  
  }
  
  public SolutionSet execute() throws JMException, ClassNotFoundException {
    int populationSize ;
    int maxEvaluations ;
    int evaluations    ;
    SolutionSet reductsAndApproxReducts;//all reducts and approximate reducts found during all the generations
    SolutionSet otherSubsets; //subsets which are not reducts and approximate reducts i.e. subsets with degree of dependency < 1    
    SolutionSet population          ;
    SolutionSet offspringPopulation ;

    Operator    mutationOperator  ;
    Operator    crossoverOperator ;
    Operator    selectionOperator ;
    Solution newIndividual;
    int i;
    boolean offspring0_is_empty_subset_or_set_of_all_features;
    boolean offspring1_is_empty_subset_or_set_of_all_features;
    int generation = 1;//initial population (0th population) is 1st generation
    int numberOfapproxReductsFoundAtPreviousGeneration = 0;
    int numberOfapproxReductsFoundAtThisGeneration;
    
    comparator = new ObjectiveComparator(0) ; // Single objective comparator   
    // Read the params
    populationSize = ((Integer)this.getInputParameter("populationSize")).intValue();
    maxEvaluations = ((Integer)this.getInputParameter("maxEvaluations")).intValue();                
   
    // Initialize the variables
    population          = new SolutionSet(populationSize) ;   
    offspringPopulation = new SolutionSet(populationSize) ;
    reductsAndApproxReducts = new SolutionSet(maxEvaluations);//max no. of approximate reducts = maxEvaluations because 2 unique approximate reducts could be found from 2 parents by crossover and mutation.
    otherSubsets = new SolutionSet(maxEvaluations);    
    evaluations  = 0;                
    i = 0;
    // Read the operators
    mutationOperator  = this.operators_.get("mutation");
    crossoverOperator = this.operators_.get("crossover");
    selectionOperator = this.operators_.get("selection");  

    // Create the initial population
    System.out.println("Initial population:\n");
    while(i < populationSize) {
      newIndividual = new Solution(problem_);
      offspring0_is_empty_subset_or_set_of_all_features = collectReductsApproximateReductsAndOtherSubsets(newIndividual, rs, reductsAndApproxReducts, otherSubsets);      
      if(!offspring0_is_empty_subset_or_set_of_all_features)
      {
    	population.add(newIndividual);
      	i++;
      	evaluations++;
      }
    }//for       
    System.out.println("Initial population has been created.");
    //System.out.println("evaluations: "+evaluations);
    System.out.println(reductsAndApproxReducts.size()+" reducts and approximate reducts are found by "+generation+"th generation");
    try {
    	logfile.write("Initial population has been created.\n");
    	logfile.write(reductsAndApproxReducts.size()+" reducts and approximate reducts are found by "+generation+"th generation\n");
    }
    catch (IOException e) {
    	System.out.println("IOException in writing logfile");
    	e.printStackTrace();
    }
    // Sort population
    population.sort(comparator) ;
    while (evaluations < maxEvaluations) {
      generation++;
	  System.out.println(generation+"th generation:");
      
	  //if ((evaluations % 10) == 0) {
      //  System.out.println(evaluations + ": " + population.get(0).getObjective(0)) ;
      //} //

      // Copy the best two individuals to the offspring population
      offspringPopulation.add(new Solution(population.get(0))) ;	
      offspringPopulation.add(new Solution(population.get(1))) ;	
      evaluations+=2;  
      //Reproductive cycle
      //i=0;
      while(offspringPopulation.size() < populationSize && evaluations < maxEvaluations)
      {
     	
        // Selection
        Solution [] parents = new Solution[2];

        parents[0] = (Solution)selectionOperator.execute(population);
        parents[1] = (Solution)selectionOperator.execute(population);
 
        // Crossover
        Solution [] offspring = (Solution []) crossoverOperator.execute(parents);                
          
        // Mutation
        mutationOperator.execute(offspring[0]);
        mutationOperator.execute(offspring[1]);
        
        offspring0_is_empty_subset_or_set_of_all_features = collectReductsApproximateReductsAndOtherSubsets(offspring[0], rs, reductsAndApproxReducts, otherSubsets);              
        offspring1_is_empty_subset_or_set_of_all_features = collectReductsApproximateReductsAndOtherSubsets(offspring[1], rs, reductsAndApproxReducts, otherSubsets);      
        if(!offspring0_is_empty_subset_or_set_of_all_features && offspringPopulation.size() < populationSize && evaluations < maxEvaluations)
        {
        	offspringPopulation.add(offspring[0]);
        	//System.out.println("offspring is added 1");
        	//i++;
        	evaluations++;
        }
        if(!offspring1_is_empty_subset_or_set_of_all_features && offspringPopulation.size() < populationSize && evaluations < maxEvaluations)
        {
        	offspringPopulation.add(offspring[1]) ;
        	//System.out.println("offspring is added 2");
        	//i++;
        	evaluations++;
        }
      }// while
      //System.out.println("offspringPopulation size: "+offspringPopulation.size());
      //System.out.println("evaluations: "+evaluations);
      i=0;
      //The offspring population becomes the new current population
      population.clear();
      while(i < populationSize){
        population.add(offspringPopulation.get(i));
    	//System.out.println("offspring is added to population");
        i++;
      }
      offspringPopulation.clear();
      population.sort(comparator) ;
      //At end of this generation, log results to file 
      numberOfapproxReductsFoundAtThisGeneration = reductsAndApproxReducts.size() - numberOfapproxReductsFoundAtPreviousGeneration;
      System.out.println(numberOfapproxReductsFoundAtThisGeneration+" reducts and approximate reducts are found by "+generation+"th generation");
      try {
	    	logfile.write(numberOfapproxReductsFoundAtThisGeneration+" reducts and approximate reducts are found by "+generation+"th generation\n");
      }
      catch (IOException e) {
    	  System.out.println("IOException in writing logfile");
    	  e.printStackTrace();
	  }
      numberOfapproxReductsFoundAtPreviousGeneration = numberOfapproxReductsFoundAtThisGeneration;
    } // while
    System.out.println("total no. of reducts and approximate reducts: "+reductsAndApproxReducts.size());
    try{
    	logfile.write("total no. of reducts and approximate reducts: "+reductsAndApproxReducts.size());
    	logfile.close();
    }
    catch (IOException e)
    {
    	e.printStackTrace();
    }
     // Return a population with the best individual
    /*
    SolutionSet resultPopulation = new SolutionSet(1) ;
    resultPopulation.add(population.get(0)) ;
    
    System.out.println("Evaluations: " + evaluations ) ;
    */
    //return resultPopulation ;
    //###return the final population with all the solutions###
    //System.out.println("Evaluations: " + evaluations ) ;
    //return population;
    
    return reductsAndApproxReducts.union(otherSubsets);
  } // execute

boolean collectReductsApproximateReductsAndOtherSubsets(Solution individual, ReductsSearch rs, SolutionSet ReductsAndApproxReducts, SolutionSet otherSubsets) 
throws JMException {
    Binary variable;
    float gamma;
    boolean individual_is_empty_subset_or_set_of_all_features;
	variable = ((Binary)individual.getDecisionVariables()[0]);
	individual_is_empty_subset_or_set_of_all_features = false;
    if (rs.get_subset_size(variable) > 0 && rs.get_subset_size(variable) < rs.numberOfBits)//individual is a non-empty feature subset.
    {
	      rs2.evaluate(individual);
	      gamma = (Float) rs.gammas.get(variable);
	      System.out.println("relative dependency of "+variable.toString()+": "+gamma);
	      if(gamma == rs.gamma_setOfAllFeatures)
	      {		    	  
	    	  if(ReductsAndApproxReducts.size() < ReductsAndApproxReducts.getMaxSize())
	    	  {
	    		  ReductsAndApproxReducts.add(individual);
	    	      System.out.println(variable.bits_.toString()+" is a reduct or an approximate reduct and collected.");
	    	      writeToLogFile(logfile,variable,"reduct or approximate reduct and collected");
	    	  }
	      }
	      else
	      {
	    	  if(otherSubsets.size() < otherSubsets.getMaxSize())
	    	  {
	    		  otherSubsets.add(individual);
	    		  System.out.println(variable.bits_.toString()+" is other subset and collected");
	    		  writeToLogFile(logfile,variable,"other subset and collected");
	    	  }
	      }	             
     }
    else
    	individual_is_empty_subset_or_set_of_all_features = true;
    return individual_is_empty_subset_or_set_of_all_features;
   }

void writeToLogFile(OutputStreamWriter logfile, Binary subset, String subset_description)
   {
	  try{
		  logfile.write(subset.bits_.toString()+": "+subset_description+"\n");
	  }
	  catch (IOException e) {
	    	System.out.println("IOException in writing logfile");
	    	e.printStackTrace();
	  }
   }
} // gGA_Reducts