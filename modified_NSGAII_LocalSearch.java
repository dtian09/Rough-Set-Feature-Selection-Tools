import jmetal.core.*;
import jmetal.encodings.variable.Binary;
//import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Configuration;
import jmetal.util.Distance;
import jmetal.util.JMException;
//import jmetal.util.PseudoRandom;
import jmetal.util.Ranking;
import jmetal.util.comparators.CrowdingComparator;
import jmetal.operators.selection.RankingAndCrowdingSelection;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

public class modified_NSGAII_LocalSearch extends Algorithm {
	  /**
	   * Constructor
	   * @param problem Problem to solve
	   */
		boolean ga_converge;
		int no_of_populations_with_equal_average_fitness;
		HashMap<Integer,HashSet<BitSet>> approx_reducts; //key: size of a subset, value = set of subsets with the size
		HashMap<Integer,HashSet<BitSet>> reducts = null;
	    HashSet<Solution> reductsSet=null;
		Utility util;
		ReductsSearch rs2;
		RSys rsys2;
		int no_of_reducts;//number of reducts to find from approximate reducts. no_of_reducts <= number of approximate reducts	
		int count;//count no. of generations where consecutive populations have same average fitness
		TreeSet<Integer> subsets_sizes;
		int total_no_of_subsets;
		SolutionSet approxReductsPopulationOfFinalGeneration;
		OutputStreamWriter logfile;
		
	  public modified_NSGAII_LocalSearch(String data_file, int generations_to_check_convergence, ReductsSearch rs, RSys rsys, OutputStreamWriter log)
	  {
	    super (rs);
	    no_of_populations_with_equal_average_fitness = generations_to_check_convergence;
	    util = new Utility();
	    rs2 = rs;
	    rsys2 = rsys;
	    count=0;
	    subsets_sizes = new TreeSet<Integer>();
	    approx_reducts = new HashMap<Integer,HashSet<BitSet>>();
	    total_no_of_subsets=0;
	    approxReductsPopulationOfFinalGeneration=null;
	    logfile = log;
	  }

	  public SolutionSet execute() throws JMException, ClassNotFoundException
	  {
	    int populationSize;
	    int maxEvaluations;
	    int evaluation;//the ith offspring of all the offspring found. i starts from 0
	    int generation;//the jth generation, j starts from 0
		int i; //ith offspring of the current population. i starts from 0
	    float gamma = 0f;
	    //Iterator<Integer> iter;
	    //BitSet core;//core of all the reducts found by the GA
	    //FileWriter out;
	    SolutionSet population;//population created by a generation
	    SolutionSet offspringPopulation;
	    SolutionSet approxReductsPopulation;//all approximate reducts found by all the generations
	    SolutionSet otherSubsets; //subsets which are not reducts and approximate reducts i.e. subsets with degree of dependency < 1
	    Operator mutationOperator;
	    Operator crossoverOperator;
	    Operator selectionOperator;
	    int numberOfapproxReductsFoundAtPreviousGeneration = 0;
	    int numberOfapproxReductsFoundAtThisGeneration;
		float average_fitness_of_current_population;
		float average_fitness_of_last_generation = 0;
		Ranking ranking;
		long initTime;
		long estimatedTime;
		long initTime2;
		long estimatedTime2;
		Solution newSolution;
	    Solution[] parents;
	    Solution[] offSpring;
	    Binary variable;
	    Solution setOfAllFeatures = null;
	    double [] probs;//probabilities of fitness proportionate selection of individuals	    
	    populationSize = ((Integer) getInputParameter("populationSize")).intValue();
	    maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
	    population = new SolutionSet(populationSize);
	    evaluation = 0; // 0th offspring 
	    //requiredEvaluations = 0;
	    mutationOperator = operators_.get("mutation");
	    crossoverOperator = operators_.get("crossover");
	    selectionOperator = operators_.get("selection");    
	    variable=null;
	    HashMap<String,Object> parameters = new HashMap<String,Object>();
	    parameters.put("problem", rs2);
	    parameters.put("populationSize", populationSize);
	    RankingAndCrowdingSelection r = new RankingAndCrowdingSelection(parameters);//class to rank the population to compute probabilities of fitness proportionate selection of individuals
	    //SolutionSet rankedPopulation;//the ranked population
	    generation = 1;//initial population (0th population) is 1st generation
	    //create initial population
	    System.out.println("Creating initial population");
	    try
	    {
	    	logfile.write("Creating initial population\n");
	    }
	    catch (IOException e) {
	    	System.out.println("IOException 1 in writing logfile");
	    	e.printStackTrace();
	    }
	    approxReductsPopulation = new SolutionSet(maxEvaluations);//max no. of approximate reducts = maxEvaluations because 2 unique approximate reducts could be found from 2 parents by crossover and mutation.
	    otherSubsets = new SolutionSet(maxEvaluations);
	    initTime = System.currentTimeMillis();
	    //create initial population of randomly generated feature subsets
	    i=0; 
	    while(i < populationSize)
	    {
	     newSolution = new Solution(problem_);
	     variable = ((Binary)newSolution.getDecisionVariables()[0]);
         if (rs2.get_subset_size(variable) > 0)//at least 1 feature is selected
         {
	      problem_.evaluate(newSolution);
	      System.out.println(variable.bits_.toString());
	      try {
	    	  logfile.write(variable.bits_.toString()+"\n");
	      }
	      catch (IOException e) {
		    	System.out.println("IOException 2 in writing logfile");
		    	e.printStackTrace();
		  }
	      gamma = (Float) rs2.gammas.get(variable.bits_);  
	      if(gamma == rs2.gamma_setOfAllFeatures)
	      {	
	    	 if(get_subset_size(variable) < rs2.numberOfBits)
	    	 {	    	  
	    	  approxReductsPopulation.add(newSolution);
	    	  //System.out.println(variable.bits_.toString()+" is an approximate reduct.");
	    	 }
	      }
	      else
	      {
	    	  //System.out.println(variable.bits_.toString()+" is other subset");
	    	  otherSubsets.add(newSolution);
	      }
	      population.add(newSolution);
	      i++;
	      evaluation++;
         }
        }
	    population = (SolutionSet)r.execute(population);//Pareto rank the initial population
	    System.out.println("Initial population has been created.");
        System.out.println(approxReductsPopulation.size()+" approximate reducts are found by "+generation+"th generation");
	    try {
	    	logfile.write("Initial population has been created.\n");
	    	logfile.write(approxReductsPopulation.size()+" approximate reducts are found by "+generation+"th generation\n");
	    }
	    catch (IOException e) {
	    	System.out.println("IOException 3 in writing logfile");
	    	e.printStackTrace();
	    }
        ga_converge = false;
	    while (evaluation < maxEvaluations)
	    {
	      generation++;
	      System.out.println(generation+"th generation:");
	      try {
		    	logfile.write(generation+"th generation: \n");
	      }
	      catch (IOException e) {
	    	  System.out.println("IOException 4 in writing logfile");
	    	  e.printStackTrace();
		  }
	      //System.out.println("size of current population: "+population.size());
	      offspringPopulation = new SolutionSet(populationSize);
	      parents = new Solution[2];
	      //generate an offspring population of size populationSize
	      i=0; //ith offspring. i starts from 0
	      /* fitness proportionate selection	    
	         compute propabilities of selection for the individuals in the population */
	      probs = fitnessPropSelectionProbabilities(population);
	      /*end of fitness proportionate selection*/
	      while (i < populationSize && evaluation < maxEvaluations) 
	      {
	    	  /* Binary tournament2 selection */
	          //parents[0] = (Solution) selectionOperator.execute(population);
	          //parents[1] = (Solution) selectionOperator.execute(population);
	          /* fitness proportionate selection */
	          parents[0] = population.get(rouletteSelect(probs));
	    	  parents[1] = population.get(rouletteSelect(probs));
	          /* end of fitness proportionate selection */
	          offSpring = (Solution[]) crossoverOperator.execute(parents);
	          mutationOperator.execute(offSpring[0]);
	          mutationOperator.execute(offSpring[1]);
	          variable = (Binary)offSpring[0].getDecisionVariables()[0];
	          if (rs2.get_subset_size(variable) > 0)//at least 1 feature is selected
	          {
	        	  problem_.evaluate(offSpring[0]);
	        	  offspringPopulation.add(offSpring[0]);
	        	  i++;
	        	  evaluation++;
	        	  gamma = (Float) rs2.gammas.get(variable.bits_);
	        	  if(gamma == rs2.gamma_setOfAllFeatures)
	        	  {
	        		if(get_subset_size(variable) < rs2.numberOfBits)
	        		{
	        		  approxReductsPopulation.add(offSpring[0]);
	        		}
	        	  }
	        	  else
	        		 otherSubsets.add(offSpring[0]);
	          }
	          if (i < populationSize && evaluation < maxEvaluations)
	          {
	        	  variable = (Binary)offSpring[1].getDecisionVariables()[0];
	        	  if (rs2.get_subset_size(variable) > 0)//at least 1 feature is selected
	        	  {
	        		  problem_.evaluate(offSpring[1]);
	        		  offspringPopulation.add(offSpring[1]);
	        		  i++;
	        		  evaluation++;    	           
	        		  gamma = (Float) rs2.gammas.get(variable.bits_);
	        		  if(gamma == rs2.gamma_setOfAllFeatures)
	        		  {
	        			 if(get_subset_size(variable) < rs2.numberOfBits)
	        			 {
	        			     approxReductsPopulation.add(offSpring[1]);
	        			 }
	        		  }
	        		  else
	        			  otherSubsets.add(offSpring[1]);
	        	  }
	          }	      
	      }//end for, a offspring population of population size has been generated
	      //create a new population by elitism and pareto ranking 
	      population = createPopulationByParetoRanking(populationSize,population,offspringPopulation);        
	      //total_no_of_subsets = util.get_total_no_of_subsets(approx_reducts);
	      numberOfapproxReductsFoundAtThisGeneration = approxReductsPopulation.size() - numberOfapproxReductsFoundAtPreviousGeneration;
	      System.out.println(numberOfapproxReductsFoundAtThisGeneration+" approximate reducts are found by "+generation+"th generation");
	      try {
		    	logfile.write(numberOfapproxReductsFoundAtThisGeneration+" approximate reducts are found by "+generation+"th generation\n");
	      }
	      catch (IOException e) {
	    	  System.out.println("IOException 5 in writing logfile");
	    	  e.printStackTrace();
		  }
	      numberOfapproxReductsFoundAtPreviousGeneration = numberOfapproxReductsFoundAtThisGeneration;
	      //#### population fitness converges if there is no improvement of fitness for k consecutive generations ####
	      //Check whether populationg fitness coverges.
	      //e.g. k=5
          //
          //count = 0 (the number of consecutive generations with same average fitness)
	      //1. if(count == no_of_generations_to_check_for_ga_convergence)
	      //2. then
          //         output the approximate reducts.
	      //3. else {
          //4.          if(the average fitness of this population == the average fitness of the previous population)
          //		 	     generate a new population
          //			     count++
          //5.          else
          //			     generate a new population
          //		         count=0
          //		         go to 1
          //             }
	      /*
          if (average_fitness_of_last_generation != 0)
          {
        	  average_fitness_of_current_population = rs2.total_fitness/(populationSize*2);
        	  if (Math.abs(average_fitness_of_current_population - average_fitness_of_last_generation) < 0.005)
        	  {
        		count++;
        	    if(count == no_of_populations_with_equal_average_fitness)
        	    {
        	    	ga_converge = true;
        	    	System.out.println("converged at "+generation+"th generation.");	         
        	    	try {
        		    	logfile.write("converged at "+generation+"th generation.\n");
        	    	}
        	    	catch (IOException e) {
        	    	  System.out.println("IOException 6 in writing logfile");
        	    	  e.printStackTrace();
        	    	}
        	    	break;//stop GA search and output approximate reducts
        	     }
        	    else
        	    {
        	    	rs2.total_fitness=0;
        	    }
        	  }
        	  else
        	  {
        		  count = 0;
        		  average_fitness_of_last_generation = average_fitness_of_current_population;
      	    	  rs2.total_fitness=0;
        	  }
          }
          else
          {//this population is generated at 1st generation
        	  average_fitness_of_last_generation = rs2.total_fitness/populationSize;
        	  rs2.total_fitness=0;
          }*/ 
	    }//end while (modified NSGAII finishes)
	    estimatedTime = System.currentTimeMillis() - initTime;
	    if (approxReductsPopulation.size()==0)//no approximate reducts found by GA, the set of all the feature is the only approximate reduct
	    {
	    	setOfAllFeatures = createSolutionOfSetOfAllFeatures();
	    	rs2.evaluate(setOfAllFeatures);
	    	approxReductsPopulation.add(setOfAllFeatures);
	    }
	    /*
	    if(!ga_converge)
	    {
    		System.out.println("GA did not converge");
	    	try {
		    	logfile.write("GA did not converge\n");
	    	}
	    	catch (IOException e) {
	    	  System.out.println("IOException 7 in writing logfile");
	    	  e.printStackTrace();
	    	}
	    }
	    else
	    {
	    	System.out.println("converged at "+generation+"th generation.");	         
	    	try
	    	{
		    	logfile.write("converged at "+generation+"th generation.\n");	         	    	
	    	}
	    	catch (IOException e) {
	      	  System.out.println("IOException 7 in writing logfile");
	      	  e.printStackTrace();
	      	}
	    }*/
    	System.out.println("Modified NSGAII takes "+estimatedTime+"milli seconds ("+estimatedTime/1000+" seconds).");
    	System.out.println("GA found "+approxReductsPopulation.size()+" approximate reducts");
    	try {
	    	logfile.write("Modified NSGAII takes "+estimatedTime+"milli seconds ("+estimatedTime/1000+" seconds).\n");
	    	logfile.write("GA found "+approxReductsPopulation.size()+" approximate reducts\n");
    	}
    	catch (IOException e) {
    	  System.out.println("IOException 8 in writing logfile");
    	  e.printStackTrace();
    	}
    	//print other subsets
    	print_subsets(otherSubsets,"other subset");
    	try
    	{	
    		logfile.write("===other subsets===\n\n");
    		print_subsets_to_logfile(otherSubsets,"other subset",logfile);
    		logfile.write("===other subsets end===\n\n");
    	}
    	catch (IOException e)
    	{
    		System.out.println("IOException 9 in writing logfile");
    		e.printStackTrace();
    	}
    	//end print other subsets
    	//print approximate reducts
    	print_subsets(approxReductsPopulation,"approximate reduct");
    	try
    	{
    		print_subsets_to_logfile(approxReductsPopulation,"approximate reduct",logfile);
    	}
    	catch (IOException e)
    	{
    		System.out.println("IOException 9 in writing logfile");
    		e.printStackTrace();
    	}
    	//end print approximate reducts
   		System.out.println("\n=== Search for random reducts using local search ===");               
    	approx_reducts = get_approx_reducts(approxReductsPopulation);
        initTime2 = System.currentTimeMillis();
    	no_of_reducts = approxReductsPopulation.size();//find a reduct from each approximate reduct
        reducts = util.LocalSearchRandomReducts(rs2, rsys2, subsets_sizes, approx_reducts, no_of_reducts);
        estimatedTime2 = System.currentTimeMillis() - initTime2;
        System.out.println("Random local search takes "+estimatedTime2+" milli seconds ("+estimatedTime2/1000+") seconds.\n Total execution time: "+(estimatedTime+estimatedTime2)/1000+" seconds");
        try 
        {
        	logfile.write("Random local search takes "+estimatedTime2+" milli seconds ("+estimatedTime2/1000+") seconds.\n Total execution time: "+(estimatedTime+estimatedTime2)/1000+" seconds\n");
        }
        catch (IOException e) {
        	System.out.println("IOException 10 in writing logfile");
      	  	e.printStackTrace();
      	}
        //Create a SolutionSet of reduct Solution objects
        System.out.println("create solution set of reducts");
        SolutionSet reductsPopulation = createSolutionSetOfReducts(no_of_reducts,reducts);
        System.out.println("random local search found "+reductsPopulation.size()+" reducts");
	    //setOutputParameter("evaluations", requiredEvaluations);
        System.out.println("\n=== Ranking of feature subsets ===\n");
        try
        {
            logfile.write("random local search found "+reductsPopulation.size()+" reducts\n");
        	logfile.write("\n=== Ranking of feature subsets ===\n");
        }
        catch (IOException e) {
        	System.out.println("IOException 11 in writing logfile");
      	  	e.printStackTrace();
      	}
        SolutionSet subsetsPopulation = (approxReductsPopulation.union(reductsPopulation)).union(otherSubsets);
	    System.out.println("subsetsPopulation size: "+subsetsPopulation.size());
	    try
        {
	    	logfile.write("otherSubsets size: "+otherSubsets.size()+"\n");
	    	logfile.write("approxReductsPopulation size: "+approxReductsPopulation.size()+"\n");
	    	logfile.write("reductsPopulation size: "+reductsPopulation.size()+"\n");
        	logfile.write("subsetsPopulation size: "+subsetsPopulation.size()+"\n");
        }
        catch (IOException e) {
        	System.out.println("IOException 12 in writing logfile");
      	  	e.printStackTrace();
      	}
	    ranking = new Ranking(subsetsPopulation);
	    HashSet<BitSet> reductsSet = createHashSetOfBitSets(reductsPopulation,reductsPopulation.size());
	    printRankedSolutionsToFile(ranking,reductsSet,maxEvaluations+reductsPopulation.size(),"featuresubsets_NSGAII"); 
	    System.out.println("Feature subsets are saved to file featuresubsets_NSGAII");
	    System.out.println("Information about running of hybrid NSGAII local search is saved to file hybrid_NSGAII_local_search.log");
	    try
        {
        	logfile.write("Feature subsets are saved to file featuresubsets_NSGAII");
        	logfile.close();
        }
        catch (IOException e) {
        	System.out.println("IOException 13 in writing logfile");
      	  	e.printStackTrace();
      	}	    
	    return subsetsPopulation;
 }

double [] fitnessPropSelectionProbabilities(SolutionSet rankedPopulation)
{
	//the top ranked individual has highest fitness. 
	//individual at indices 0, 1, 2, ... have the highest fitness, 2nd highest, 3rd highest fitnesses and so on	
	double [] probs = new double [rankedPopulation.size()];
	double fitness;
	double [] fitnesses = new double [rankedPopulation.size()];
	double total_fitness=0;
	for(int i=0; i<probs.length;i++)
	{
		fitness = ((probs.length-1)-i)/(probs.length-1);//fitness is inversely proportional to index
		fitnesses[i]=fitness;
		total_fitness += fitness;
	}
	for(int i=0; i<probs.length;i++)
	{
	  probs[i] = fitnesses[i]/total_fitness;
	}
	return probs;
} 
	  
//Returns the selected index based on the weights(probabilities)
int rouletteSelect(double[] weight) {
	// calculate the total weight
	double weight_sum = 0;
	for(int i=0; i<weight.length; i++) {
		weight_sum += weight[i];
	}
	// get a random value
	double value = randUniformPositive() * weight_sum;	
	// locate the random value based on the weights
	for(int i=0; i<weight.length; i++) {		
		value -= weight[i];		
		if(value <= 0) return i;
	}
	// only when rounding errors occur
	return weight.length - 1;
}

// Returns a uniformly distributed double value between 0.0 and 1.0
double randUniformPositive() {
	  	// easiest implementation
	  	return new Random().nextDouble();
}
	  
void print_subsets_to_logfile(SolutionSet subsetsPopulation, String subsettype, OutputStreamWriter logfile) throws IOException
{
	Solution subset;
	Binary variable;
	Iterator<Solution> iter_subsets = subsetsPopulation.iterator();
	while(iter_subsets.hasNext())
	{
		subset = iter_subsets.next();
		variable = (Binary)subset.getDecisionVariables()[0];
		logfile.write(subsettype+": "+variable.bits_.toString()+"\n");
	}
}

void print_subsets(SolutionSet subsetsPopulation, String subsettype)
{
	Solution subset;
	Binary variable;
	Iterator<Solution> iter_subsets = subsetsPopulation.iterator();
    while(iter_subsets.hasNext())
    {
    	subset = iter_subsets.next();
    	variable = (Binary)subset.getDecisionVariables()[0];
    	System.out.println(subsettype+": "+variable.bits_.toString());
    }
}

Solution createSolutionOfSetOfAllFeatures()
{//create a Solution object to represent the set of all the features of the training set
	Binary binary;
	Solution solution;
	Variable [] variables;
	
	binary = new Binary(rs2.numberOfBits);      		
	for(int i=0; i < rs2.numberOfBits; i++)
		binary.setIth(i,true);
	variables = new Variable[1];
	variables[0] = binary;
	solution = new Solution(problem_,variables);
	return solution;
}

SolutionSet createSolutionSetOfReducts(int maxSize, HashMap<Integer,HashSet<BitSet>>reducts)
{
	Collection<HashSet<BitSet>> reductsSet2 = reducts.values();
	Iterator<HashSet<BitSet>> iter_reducts = reductsSet2.iterator();
	HashSet<BitSet> reducts2;
	BitSet reduct;
	Iterator<BitSet>iter2;
	Binary binary;
	Solution solution;
	Variable [] variables;
	SolutionSet reductsPopulation = new SolutionSet(maxSize);//max size of population
	float ratio;
	double accuracy;
	while(iter_reducts.hasNext())
	{
		reducts2 = iter_reducts.next();
      	iter2 = reducts2.iterator();
      	while(iter2.hasNext())
      	{
      		reduct = iter2.next();  
      		binary = new Binary(rs2.numberOfBits);      		
      		for(int i=0; i < rs2.numberOfBits; i++)
      		{
      			if(reduct.get(i))
      				binary.setIth(i,true);
          		else
          			binary.setIth(i,false);
      		}
      		variables = new Variable[1];
      		variables[0] = binary;
      		solution = new Solution(problem_,variables);
      		if (!rs2.hash_accuracy.containsKey(binary))
      	    {
      	    	accuracy = rs2.compute_accuracy(binary);
      	    	rs2.hash_accuracy.put(binary,accuracy);
      	    }     
      	    else
      	    	accuracy = rs2.hash_accuracy.get(binary);
      		solution.setObjective(0,accuracy);      		
      		solution.setObjective(1,(double)rs2.relative_dependency(rs2.rsys,binary));
      		if (rs2.hash_subset_size_ratio.containsKey(binary))
      	    	ratio = rs2.hash_subset_size_ratio.get(binary);
      	    else
      	    {
      	    	ratio = rs2.subsetSizeRatio(binary);/* ratio = (|A|-|B|)/|A| where A is the set of all the features; B is a feature subset*/
      	    	rs2.hash_subset_size_ratio.put(binary, ratio);
      	    }
      		solution.setObjective(2,ratio);
      		//solution.setObjective(0,rs2.compute_accuracy(binary));      		
      		//solution.setObjective(2,(double)rs2.getReductSizeRatio(binary));
      		//not used objective: average info gain of subset 
      		//					  solution.setObjective(1,rs2.average_importance_of_features(binary, "info_gain"));
      		reductsPopulation.add(solution);
      	}
	}
	return reductsPopulation; 
 } 
      
HashSet<BitSet> createHashSetOfBitSets(SolutionSet reducts, int size)
{//return a HashSet of approximate reducts BitSet objects
	HashSet<BitSet> hashsetOfReducts;
	Iterator<Solution> iter;
	Binary binary;
	hashsetOfReducts = new HashSet<BitSet>(size);
	iter = reducts.iterator();
    while(iter.hasNext())
    {    
      binary = (Binary)((Solution)iter.next()).getDecisionVariables()[0];
  	  //System.out.println(binary.bits_.toString());
      hashsetOfReducts.add(binary.bits_);	
    }
    
  	return hashsetOfReducts;
}

void printRankedSolutionsToFile(Ranking rank, HashSet<BitSet> reducts, int populationSize, String outfile)
{
 /* Print approximate reducts and reducts to a file
    Output format:
 	reduct: {0,2,4,5,6} objectives: 1.0, 0.99, 97.5%
 	approximate reduct: {0,9,11,12,15,16,20} objectives: 1.0, 0.9, 88%
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
 String results=null;
 Binary binary;
 try{
     fos = new FileOutputStream(outfile);
     osw = new OutputStreamWriter(fos);
     bw  = new BufferedWriter(osw);
     bw.write("=== indices of subsets (indices start from 0) ===\n");
     n = rank.getNumberOfSubfronts();
     for(int i=0; i<n; i++)
     {
    	 solutions = rank.getSubfront(i);
    	 iter = solutions.iterator();
    	 while(iter.hasNext()) 
    	 {
    		 solution = iter.next();
    		 binary = (Binary)solution.getDecisionVariables()[0];
    		 if(!solutionsPrinted.contains(binary.bits_))
    		 {
    			 solutionsPrinted.add(binary.bits_);
    			 results = feature_subset(solution)+"\tsize:\t"+get_subset_size(binary)+"\tobjectives:\t"+solution.toString();
    			 if(reducts.contains(binary.bits_))
    				 bw.write("reduct:\t"+results+"\n");
    			 else
    				 bw.write("non-reduct subset:\t"+results+"\n");
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

String feature_subset(Solution solution)
{
	Binary variable;
	variable = ((Binary)solution.getDecisionVariables()[0]);      
	return variable.bits_.toString();
}

BitSet compute_core(HashMap<Integer,HashSet<BitSet>> reducts)
{
	Iterator<Integer> iter;
	Iterator<BitSet> iter2;
	HashSet<BitSet> reductsSet;
	BitSet reduct;
	BitSet core;
	
	core = new BitSet(rs2.numberOfBits);
	for(int i=0; i<rs2.numberOfBits; i++)
	{
		core.set(i);
	}
	iter = (reducts.keySet()).iterator();
    while(iter.hasNext())
    {
    	reductsSet = reducts.get(iter.next());
    	iter2 = reductsSet.iterator();
    	while(iter2.hasNext())
    	{
    		reduct = iter2.next();
    		core.and(reduct);
    	}
    }
    return core;
}

SolutionSet createPopulationByParetoRanking(int populationSize,SolutionSet population, SolutionSet offspringPopulation)
{ //return the next population containing Pareto ranked individuals
  //Individuals at indices 0, 1, 2, ... are the fittest, 2nd fittest, 3rd fittest, ... individuals
  SolutionSet union;
  Distance distance = new Distance();
  union = ((SolutionSet) population).union(offspringPopulation);//combine offspring population and parent population into a new population
  Ranking ranking = new Ranking(union);//Pareto rank the new population
  int remain = populationSize;
  int index = 0;
  SolutionSet front = null;
  population.clear();//create an empty population of the next generation

  //Obtain the next front
  front = ranking.getSubfront(index);
  //add the front to the population of the next generation
  while ((remain > 0) && (remain >= front.size()))
  {
    //Add the individuals of this front to the next population
    for(int k = 0; k < front.size(); k++) 
    {
      population.add(front.get(k));
    }
    //Decrement remain
    remain = remain - front.size();
    //Obtain the next front
    index++;
    if(remain > 0) 
    {
      front = ranking.getSubfront(index);
    }
  }
  // Remain is less than front.size(), insert only the best solutions in the next front
  if(remain > 0)
  { // front contains individuals to insert
	  distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
	  front.sort(new CrowdingComparator());
	  for (int k = 0; k < remain; k++) {
           population.add(front.get(k));
	  }
	  remain = 0;
  } 
  return population;
}

 HashMap<Integer,HashSet<BitSet>> get_approx_reducts(SolutionSet approxReductsPopulation)
 {    //return approx_reducts: key=subset size, value = a set of approximate reducts of the size
      HashMap<Integer,HashSet<BitSet>> approx_reducts;
      Iterator<Solution> iter;
      Solution approxReductSolution;
      Binary variable;
      int subset_size;

      approx_reducts = new HashMap<Integer,HashSet<BitSet>>();
      iter = approxReductsPopulation.iterator();
      while(iter.hasNext())
      {
    	  approxReductSolution = (Solution)iter.next();
    	  variable = ((Binary)approxReductSolution.getDecisionVariables()[0]);
    	  subset_size = get_subset_size(variable);
    	  subsets_sizes.add(subset_size);
    	  if(approx_reducts.containsKey(subset_size))
    	  {
    		  HashSet<BitSet> approx_reductsSet;
    		  approx_reductsSet = approx_reducts.get(subset_size);
    		  approx_reductsSet.add(variable.bits_);
    		  approx_reducts.put(new Integer(subset_size), approx_reductsSet);
    		  //System.out.println("approx_reducts contains key: subset_size: "+subset_size);
    		  //System.out.println("updated value: "+approx_reductsSet);
    	  }
    	  else
    	  {
    		  HashSet<BitSet> approx_reductsSet2 = new HashSet<BitSet>();
    		  approx_reductsSet2.add(variable.bits_);
    		  approx_reducts.put(new Integer(subset_size), approx_reductsSet2);
    		  //System.out.println("approx_reducts does not contain key: subset_size: "+subset_size);
    		  //System.out.println("value: "+approx_reductsSet2);
    	  }
      }
      System.out.println("subsets_sizes: "+subsets_sizes);
      return approx_reducts;
 }
 public static int get_subset_size(Binary variable)
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
}
