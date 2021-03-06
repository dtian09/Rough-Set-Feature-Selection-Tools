# Rough-Set-Feature-Selection-Tools
Rough set feature selection (RSFS) algorithms implemented in java. RSFS can be used to remove the irrelevant and redundant features from a training set before training machine learning classifiers such as neural networks and Bayesian network to improve the classification performance of the classifiers. RSFS works on discrete features only. To apply RSFS on continuous features, the continuous features must be discretized before running RSFS. Two popular discretization algorithms are the Entropy-based discretization method and the Chimerge discretization. The entroy-based discretization is available in the weka machine learning tool (https://www.cs.waikato.ac.nz/ml/weka/). A compiled java implementation of Chimerge is available under this repository. The following RSFS algorithms are implemented: 

  Genetic algorithm (GA_Reducts.java),
  
  QuickReduct (QuickReduct.java),
  
  Random forward search (Random_RSFS.java),
  
  Random backward search (Random_RSFS.java),
  
  Random forward-backward search (Random_RSFS.java),
  
  Multi-object genetic local search (Hybrid_NSGAII.java),
  
  Computation of degree of dependency of a feature n a dataset (Gamma_and_Relative_Dependency.java),
  
  Computation of relative dependency of a feature in a dataset (Gamma_and_Relative_Dependency.java).
  
The genetic algorithm and the multi-objectve genetic local search calls the jmetal optimization library (http://jmetal.sourceforge.net/index.html) and the Weka machine learning tool (weka.jar version 3.6) (https://www.cs.waikato.ac.nz/ml/weka/) to train classifiers and evaluate their performance on test sets.

References:

D. Tian “A Multi-objective Genetic Local Search Algorithm for Optimal Feature Subset Selection”,
2016 International Conference on Computational Science and Computational Intelligence (CSCI), Las Vegas, USA, 2016

D. Tian, X. Zeng and J. Keane, “Core-generating Approximate Minimum Entropy Discretization for Rough Set Feature Selection in Pattern Classification” International Journal of Approximate Reasoning, vol. 52, issue 6, pp.863-880, 2011

D. Tian, J. Keane and X. Zeng, "Evaluating The Effect of Rough Set Feature Selection On The Performance of Decision Trees", Proc. IEEE International Conference on Granular Computing, Georgia State University, Atlanta, USA, 2006

D. Tian, "Effective Rough Set Feature Selection via Core-generating Approximate Minimum Entropy Discretization", PhD Thesis, The University of Manchester, 2009

