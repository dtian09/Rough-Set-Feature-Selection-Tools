import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//train classifiers using the reducts and evaluate performance of the classifers
//input: a weka arff training data file
//		 a weka arff test data file (optional)
//		 a reducts file
//output: classification accuracies of classifiers trained using the reducts

public class ClassificationAccuracy {
		
    public final static void main(String[]args) throws Exception
	{
    	Utility util;
    	String reduct_indices;
    	String [] reduct_indices_array;
    	int [] reduct_indices_array_int;
    	String [] all_attributes = null;
    	Random randomGenerator;
    	String reducts_file=null;
    	String cmd;
    	String classifier = null;
    	String class_path;
    	String results_file_temp;
    	String option;
    	FileWriter results_file;
    	String os;
    	int n;
    	BufferedReader in=null;
    	String line=null;
    	String weka_data_file=null;
    	String weka_test_file=null;
    	String reduced_weka_data_file=null;
    	String reduced_weka_test_file=null;
    	boolean test_file_provided = false;
    	String reduct_indices2 ="";
    	String class_index;
    	int index;
    	String get_accuracy_program=null;
    	String results_file_name=null;
    	
    	randomGenerator = new Random();
    	if (args.length == 2)//a labelled data file is specified
    	{
    		weka_data_file = args[0];//a csv data file or a space delimited data file
    		reducts_file = args[1];//a reducts file output by random forward backward search
    		reduced_weka_data_file = randomGenerator.nextInt(9000000)+"_reduced_train_set.arff";
    	}
    	else if(args.length==3)//a training data file and a testing data file are specified 
    	{
    		weka_data_file = args[0];// a training data file in csv or a space delimited format
    		weka_test_file = args[1];// a test data file in same format as training file
    		reducts_file = args[2];//a reducts file output by random forward backward search
    		reduced_weka_data_file = randomGenerator.nextInt(9000000)+"_reduced_train_set.arff";
    		reduced_weka_test_file = randomGenerator.nextInt(99900000)+"_reduced_test_set.arff";
    		test_file_provided = true; 		
    	}
    	else
    	{
    		System.out.println("wrong number of input arguments.\n Input either 2 arguments (<train weka data file>, <reducts file>) or 3 arguments (<train weka data file>,<test weka data file> <reducts file>).");
    		System.exit(-1);
    	}   	
    	//classifier="C4.5";
    	classifier="naive bayes";
    	//classifier="bayesian network";
    	//classifier="random forest";
    	//classifier="logistic regression";
    	option="percentage split";
		//option = "cross validation";
    	//os="linux";
    	os="windows";
    	
    	if(classifier.equals("C4.5"))
    	   get_accuracy_program = "C:\\Users\\David\\Dropbox\\programs\\get_reducts_accuracy.py";
    	else
    	   get_accuracy_program = "C:\\Users\\David\\Dropbox\\programs\\get_reducts_accuracy2.py";

    	if(os.equals("windows"))
    		class_path = "C:\\Program Files\\Weka-3-6\\weka.jar";
    	else//linux
    		class_path = "/home/david/Downloads/weka-3-6-12/weka.jar";
    	
    	results_file_name = weka_data_file+".rsfs_"+classifier+"_accuracy";
    	util = new Utility();
        results_file_temp = "temp_results_file";
    	if(test_file_provided)
    	{ //train a classifier using training file and test it using a test file
        	cmd = create_cmd2(classifier,weka_data_file,weka_test_file,class_path,results_file_temp);
    	}
    	else
    		cmd = create_cmd(classifier,weka_data_file,class_path,option,results_file_temp);
		System.out.println(cmd);
    	//try
		//{
			util.run_system_command(cmd,os);
		//}
		/*catch(IOException e)
		{
			System.out.println("IOException in running "+cmd);
			System.exit(-1);
		}
		catch(InterruptedException e)
		{
			System.out.println("InterruptedException in running "+cmd);
			System.exit(-1);
		}*/ 	
    	results_file = new FileWriter(results_file_name);
    	results_file.write("###################################"+classifier+" Results###########################");
    	results_file.write("\n"+cmd+"\n");
    	add_results_to_file(results_file_temp,results_file);    		
		all_attributes = Utility.attributes_names_of_weka_arff_file(weka_data_file);
		
		class_index = Integer.toString(all_attributes.length);
		try
		{
			in = new BufferedReader(new FileReader(reducts_file));
		}
		catch(IOException e)
		{
			System.out.println("IOException open file: "+reducts_file);
			System.exit(-1);
		}
		n=1;
		try
		{
    	 line = in.readLine();
		}
		catch(IOException e)
		{
			System.out.println("IOException read file: "+reducts_file);
			System.exit(-1);
		}		
    	while(line!=null)
    	{ 
    		reduct_indices = get_subset(line);
    		reduct_indices2 = "";//reduct indices start from 1 
    		if (reduct_indices == null)
    			break;
    		reduct_indices_array = reduct_indices.split(",");
    		reduct_indices_array_int = new int [reduct_indices_array.length];
    		index = Integer.parseInt(reduct_indices_array[0].trim());
    		reduct_indices_array_int[0] = index;
    		reduct_indices2 = Integer.toString(index+1);
    		for(int i=1; i<reduct_indices_array_int.length;i++)
    		{   
    			index = Integer.parseInt(reduct_indices_array[i].trim());
    			reduct_indices_array_int[i]= index;
    			reduct_indices2 = reduct_indices2+","+Integer.toString(index+1);
    		}
    		results_file.write("\n######################RSFS+"+classifier+" Results############################\n\n");  		
    		//write the reduct to the results file	
    		try
    		{
    			results_file.write("subset "+n+": "+all_attributes[reduct_indices_array_int[0]]);
    			System.out.print("subset "+n+": "+all_attributes[reduct_indices_array_int[0]]);
    		}
    		catch(IOException e)
    		{
    			e.printStackTrace();
    			System.exit(-1);
    		}
    		for(int k=1;k < reduct_indices_array_int.length; k++)
    		{  
    			try
    			{
    				results_file.write(","+all_attributes[reduct_indices_array_int[k]]);
    				System.out.print(","+all_attributes[reduct_indices_array_int[k]]);
    			}
    			catch(IOException e)
    			{
    				e.printStackTrace();
    				System.exit(-1);
    			}
    		}
    		//write the indices of the reduct to results file
    		try
    		{
    			results_file.write("\nindices: "+reduct_indices_array[0]);
    		}
    		catch(IOException e)
    		{
    			e.printStackTrace();
    			System.exit(-1);
    		}
    		for(int k=1;k < reduct_indices_array.length; k++)
    		{  
    			try
    			{
    				results_file.write(","+reduct_indices_array[k]);
    			}
    			catch(IOException e)
    			{
    				e.printStackTrace();
    				System.exit(-1);
    			}
    		}
    		results_file.write("\nsubset size: "+reduct_indices_array_int.length+"\n");
    		//System.out.println("\nsubset size: "+reduct_indices_array_int.length);	
    		//System.out.println("reduce train data");
    		util.reduce_weka_arff_file(reduct_indices2,class_path,os,class_index,weka_data_file,reduced_weka_data_file);
    		//run weka c4.5 or logistic regression on reduced dataset
    		if(test_file_provided)
    		{
        		//System.out.println("reduce test data");
    			util.reduce_weka_arff_file(reduct_indices2,class_path,os,class_index,weka_test_file,reduced_weka_test_file);
        		//System.out.println("train weka classifier and evaluate its accuracy on test set");
    			cmd = create_cmd2(classifier,reduced_weka_data_file,reduced_weka_test_file,class_path,results_file_temp);	
    		}
    		else
    		{
        		//System.out.println("train weka classifier and evaluate its accuracy using "+option);
    			cmd = create_cmd(classifier,reduced_weka_data_file,class_path,option,results_file_temp);
    		}
    		//try
    		//{
    			util.run_system_command(cmd,os);
    		//}
    		/*
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
    		add_results_to_file(results_file_temp,results_file);
    		//delete the temp reduced data files and results file   		
    		//delete_temp_files(util,os,test_file_provided,reduced_weka_data_file,reduced_weka_test_file,results_file_temp);		
    		line = in.readLine();
    		n++;
    	}
    	in.close();
    	results_file.close();
		System.out.println("results are saved to "+results_file_name);		
    	//find the best classifier in the results file
    	//find the proportion of the classifiers with better accuracy than original classifier		
		cmd = "python \""+get_accuracy_program+"\" \""+results_file_name+"\"";
		System.out.println(cmd);
		//try
		//{
			util.run_system_command(cmd,os);
		//}
		/*	
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
		System.out.println("results are saved to "+results_file_name);
}
 
static void delete_temp_files(Utility util, String os, boolean test_file_provided,String reduced_weka_data_file, String reduced_weka_test_file, String results_file_temp)
{  
	String cmd = null;
	
	if(test_file_provided)
	{
		if(os.equals("linux"))
			cmd = "rm \""+reduced_weka_data_file+"\" \""+reduced_weka_test_file+"\" \""+results_file_temp+"\"";
		else if(os.equals("windows"))
			cmd = "del \""+reduced_weka_data_file+"\" \""+reduced_weka_test_file+"\" \""+results_file_temp+"\"";
		else
		{
			System.out.println("invalid os: "+os);
			System.exit(-1);
		}
		//try
		//{
			util.run_system_command(cmd,os);
		//}
		/*catch(IOException e)
		{
			System.out.println("IOException in running "+cmd);
			System.exit(-1);
		}
		catch(InterruptedException e)
		{
			System.out.println("InterruptedException in running "+cmd);
			System.exit(-1);
		}*/
	}
	else 
	{
		if(os.equals("linux"))
			cmd = "rm \""+reduced_weka_data_file+"\" \""+results_file_temp+"\"";
		else if(os.equals("windows"))
			cmd = "del \""+reduced_weka_data_file+"\" \""+results_file_temp+"\"";
		else
		{
			System.out.println("invalid os: "+os);
			System.exit(-1);
		}
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
	}	    
}

static void add_results_to_file(String rsfs_classifier_results, FileWriter all_results_file){
	String line;
	try
	{
		BufferedReader in = new BufferedReader(new FileReader(rsfs_classifier_results));
		line = in.readLine();
		while(line != null)
		{
			//System.out.println(line);
			all_results_file.write(line+"\n");
			line = in.readLine();
		}
		in.close();
	}
	catch (IOException e)
	{
		System.out.println("ioexception in add_results_file");
	}
 }
        
static String get_subset(String line)
{
	String pStr;
	Pattern p;
	Matcher m;
	String subset = null;

	pStr = "^.*\\{(.+)\\}.*$";
	//pStr = "^\\{(.+)\\}.*$";	
	p = Pattern.compile(pStr);
	m = p.matcher(line);

	if(m.matches())
	{
		subset = m.group(1);
		//System.out.println("m matched "+subset);
	}
	return subset;
}

static String create_cmd(String classifier, String labelled_data, String class_path, String option, String results_file)
{
	String cmd=null;
	String percentage;
	String K;
	percentage="66";//percentage for training classifiers
	//decision table classifier: weka.classifiers.rules.DecisionTable -X 1 -S "weka.attributeSelection.BestFirst -D 1 -N 5"
	K="10"; //K-Nearest Neighbour classifier
	if (classifier.equals("KNN"))
		if (option.equals("percentage split"))
			cmd = "java -Xmx1G -cp \""+class_path+"\"  weka.classifiers.lazy.IBk -K "+K+" -W 0 -A \"weka.core.neighboursearch.LinearNNSearch -A \\\"weka.core.EuclideanDistance -R first-last\\\" -t \""+labelled_data+"\" -split-percentage "+percentage+" > \""+results_file+"\"";
		else if(option.equals("cross validation"))
			cmd = "java -Xmx1G -cp \""+class_path+"\"  weka.classifiers.lazy.IBk -K "+K+" -W 0 -A \"weka.core.neighboursearch.LinearNNSearch -A \\\"weka.core.EuclideanDistance -R first-last\\\" -t \""+labelled_data+"\" -x 10 > \""+results_file+"\"";
		else
		{
			System.out.println("invalid option: "+option+"\nvalid options: percentage split or cross validation");
			System.exit(-1);
		}
	else if (classifier.equals("decision table"))
		if (option.equals("percentage split"))
			cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.rules.DecisionTable -X 1 -S \"weka.attributeSelection.BestFirst -D 1 -N 5\" -t \""+labelled_data+"\" -split-percentage "+percentage+" > \""+results_file+"\"";
		else if(option.equals("cross validation"))
			cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.rules.DecisionTable -X 1 -S \"weka.attributeSelection.BestFirst -D 1 -N 5\" -t \""+labelled_data+"\" -x 10 > \""+results_file+"\"";
		else
		{
			System.out.println("invalid option: "+option+"\nvalid options: percentage split or cross validation");
			System.exit(-1);
		}
	else if (classifier.equals("C4.5"))
		if (option.equals("percentage split"))
			cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.trees.J48 -i -t \""+labelled_data+"\" -split-percentage "+percentage+" > \""+results_file+"\"";
		else if(option.equals("cross validation"))
			cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.trees.J48 -i -t \""+labelled_data+"\" -x 10 > \""+results_file+"\"";
		else
		{
			System.out.println("invalid option: "+option+"\nvalid options: percentage split or cross validation");
			System.exit(-1);
		}
	else if(classifier.equals("logistic regression"))
		if (option.equals("percentage split"))
			//weka.classifiers.functions.Logistic -R 1.0E-8 -M -1
			cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.functions.Logistic -i -R 1.0E-8 -M -1 -t \""+labelled_data+"\" -split-percentage "+percentage+" > \""+results_file+"\"";
		else if(option.equals("cross validation"))
			cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.functions.Logistic -i -R 1.0E-8 -M -1 -t \""+labelled_data+"\" -x 10 > \""+results_file+"\"";
		else
		{
			System.out.println("invalid option: "+option+"\nvalid options: percentage split or cross validation");
			System.exit(-1);
		}
	else if(classifier.equals("naive bayes"))
		if (option.equals("percentage split"))
			  cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.bayes.NaiveBayes -i -t \""+labelled_data+"\" -split-percentage "+percentage+" > \""+results_file+"\"";
		else if(option.equals("cross validation"))
			  cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.bayes.NaiveBayes -i -t \""+labelled_data+"\" -x 10 > \""+results_file+"\"";		
		else
		{
			System.out.println("invalid option: "+option+"\nvalid options: percentage split or cross validation");
			System.exit(-1);
		}
	else if(classifier.equals("bayesian network"))
		//java -cp <class path> weka.classifiers.bayes.BayesNet -i -t <train data> -split-percentage 66 -D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5 
		if (option.equals("percentage split"))
			  cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.bayes.BayesNet -i -t \""+labelled_data+"\" -split-percentage "+percentage+" -D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5 > \""+results_file+"\"";
		else if(option.equals("cross validation"))
			  cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.bayes.BayesNet -i -t \""+labelled_data+"\" -x 10 -D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5 > \""+results_file+"\"";		
		else
		{
			System.out.println("invalid option: "+option+"\nvalid options: percentage split or cross validation");
			System.exit(-1);
		}
	else if(classifier.equals("svm poly3"))
		if (option.equals("percentage split"))
			cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.functions.SMO -C 200.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 0 -E 3.0\" -i -t \""+labelled_data+"\" -percentage-split "+percentage+" > \""+results_file+"\"";
		else if(option.equals("cross validation"))
			cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.functions.SMO -C 200.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 0 -E 3.0\" -i -t \""+labelled_data+"\" -x 10 > \""+results_file+"\"";		
		else
		{
			System.out.println("invalid option: "+option+"\nvalid options: percentage split or cross validation");
			System.exit(-1);
		}
	else if(classifier.equals("random forest"))
		if (option.equals("percentage split"))
			cmd =  "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.trees.RandomForest -I 200 -K 0 -S 1 -i -t \""+labelled_data+"\" -split-percentage "+percentage+" > \""+results_file+"\"";
		else if(option.equals("cross validation"))
			cmd =  "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.trees.RandomForest -I 200 -K 0 -S 1 -i -t \""+labelled_data+"\" -x 10 > \""+results_file+"\"";
		else
		{
			System.out.println("invalid option: "+option+"\nvalid options: percentage split or cross validation");
			System.exit(-1);
		}
	else
	{
		System.out.println("invalid classifier: "+classifier+"\nvalid classifiers: decision tree or logistic regression");
		System.exit(-1);
	}
	return cmd;
}

static String create_cmd2(String classifier, String train_data, String test_data, String class_path, String results_file)
{
	String cmd=null;
	String K; //K of K-nearest neighbour classifier
	//Decision table classifier: weka.classifiers.rules.DecisionTable -X 1 -S "weka.attributeSelection.BestFirst -D 1 -N 5"
	K="10";
	if(classifier.equals("KNN"))
			cmd = "java -Xmx1G -cp \""+class_path+"\"  weka.classifiers.lazy.IBk -K "+K+" -W 0 -A \"weka.core.neighboursearch.LinearNNSearch -A \\\"weka.core.EuclideanDistance -R first-last\\\" -t \""+train_data+"\" -T \""+test_data+"\" > \""+results_file+"\"";
	else if (classifier.equals("decision table"))
			cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.rules.DecisionTable -X 1 -S \"weka.attributeSelection.BestFirst -D 1 -N 5\" -i -t \""+train_data+"\" -T \""+test_data+"\" > \""+results_file+"\"";
	else if (classifier.equals("C4.5"))
			cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.trees.J48 -i -t \""+train_data+"\" -T \""+test_data+"\" > \""+results_file+"\"";
	else if(classifier.equals("logistic regression"))
			//weka.classifiers.functions.Logistic -R 1.0E-8 -M -1
			cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.functions.Logistic -i -R 1.0E-8 -M -1 -t \""+train_data+"\" -T \""+test_data+"\" > \""+results_file+"\"";
	else if(classifier.equals("naive bayes"))
		//java -Xmx1g -cp /home/david/Downloads/weka-3-6-12/weka.jar weka.classifiers.bayes.NaiveBayes -i -t "/home/david/Dropbox/datasets/essential genes prediction/train set/new_lethal_new_viable_balanced_discretized.arff"  -T "/home/david/Dropbox/datasets/essential genes prediction/test set/new_lethal_new_viable_genes_not_in_train_set_discretized_by_cuts.arff"
	{
	  cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.bayes.NaiveBayes -i -t \""+train_data+"\" -T \""+test_data+"\" > \""+results_file+"\"";
	}
	else if(classifier.equals("bayesian network"))
	//java -cp <class path> weka.classifiers.bayes.BayesNet -i -t <train data> -T <test data> -D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5 
	{
	  cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.bayes.BayesNet -i -t \""+train_data+"\" -T \""+test_data+"\" -D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5 > \""+results_file+"\"";
	}
	else if(classifier.equals("svm poly3"))
	{//java -Xmx1g -cp /home/david/Downloads/weka-3-6-12/weka.jar weka.classifiers.functions.SMO -C 200.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.PolyKernel -C 0 -E 3.0" -i -t <train set> -T <test set> > <results_file>
		cmd = "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.functions.SMO -C 200.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 0 -E 3.0\" -i -t \""+train_data+"\" -T \""+test_data+"\" > \""+results_file+"\"";
	}
	else if(classifier.equals("random forest"))
	{//java -Xmx1g -cp /home/david/Downloads/weka-3-6-12/weka.jar weka.classifiers.trees.RandomForest -I 200 -K 0 -S 1 -i -t <train set> -T <test set> > <results file>
		cmd =  "java -Xmx1G -cp \""+class_path+"\" weka.classifiers.trees.RandomForest -I 200 -K 0 -S 1 -i -t \""+train_data+"\" -T \""+test_data+"\" > \""+results_file+"\"";
	}
	else
	{
		System.out.println("invalid classifier: "+classifier);
		System.exit(-1);
	}
	System.out.println(cmd);
	return cmd;
}
}
