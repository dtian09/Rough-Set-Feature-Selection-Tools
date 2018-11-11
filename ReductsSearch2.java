//ReductsSearch2 class defines single objective reduct search problems
//objective is a combination of subset sizes ratio, relative dependency and information gain
//weka is called on the command line to compute information gain
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;

public class ReductsSearch2 extends Problem{
	String dataset;
	ReductsSearch rs;
    RSys rsys;
    float gamma_setOfAllFeatures;
  
    public ReductsSearch2(String disc_train_set, String os){
    	String linux_class_path = "/home/david/Downloads/weka-3-6-12/weka.jar";
    	//String windows_class_path = "C:\\Program Files\\Weka-3-6-12\\weka.jar";
    	String windows_class_path = "C:\\Users\\tian03\\programs files\\Weka-3-8\\weka.jar";
    	numberOfVariables_  = 1;
    	numberOfObjectives_ = 1;
		dataset = disc_train_set;
		rs = new ReductsSearch("",os,"Binary",dataset);
		if (os.equals("windows"))
			rs.windows_class_path = windows_class_path;
		else
			rs.linux_class_path = linux_class_path;
		this.rsys = rs.rsys;
	    rs.disc_training_set = dataset;		 	    
	    length_       = new int[numberOfVariables_];
	    length_      [0] = rs.numberOfBits;    
	    solutionType_ = new BinarySolutionType(this);	    
	    this.gamma_setOfAllFeatures = rs.gamma_setOfAllFeatures;
	    rs.get_features_scores("info_gain");//Get information gain of all features from a weka output file and put them in the scores array of rs
	}
	
	public void evaluate(Solution solution){
		 	Binary variable ;
		    float dependency = 0;
		    float subset_size_ratio = 0;
		    //double total_info_gain = 0;
		    double fitness;
		    float average_info_gain = 0;
		    variable = ((Binary)solution.getDecisionVariables()[0]) ;
		    //relative dependency of subset
		    dependency = rs.relative_dependency(rsys,variable);
		    //subset size ratio
		    if (rs.hash_subset_size_ratio.containsKey(variable))
		    	subset_size_ratio = rs.hash_subset_size_ratio.get(variable);
		    else
		    {
		    	subset_size_ratio = rs.subsetSizeRatio(variable);/* ratio = (|A|-|B|)/|A| where A is the set of all the features; B is a feature subset*/
		    	rs.hash_subset_size_ratio.put(variable, subset_size_ratio);
		    }
		    //total mutual information of subset    
		    //total_info_gain = rs.totalscore(variable);
		    average_info_gain = (float) rs.totalscore(variable)/rs.get_subset_size(variable);
		    //fitness = dependency + total_info_gain + subset_size_ratio;
		    fitness = dependency + average_info_gain + subset_size_ratio;
		    solution.setObjective(0,fitness);
	}
}
