# Rough-Set-Feature-Selection-Tools
Rough set feature selection (RSFS) algorithms implemented in java. RSFS can be used to remove the irrelevant and redundant features from a training set before training machine learning classifiers such as neural networks and Bayesian network to improve the classification performance of the classifiers.
The following RSFS algorithms are implemented: 
  genetic algorithm (GA_Reducts.java),
  QuickReduct (QuickReduct.java),
  random forward search (Random_RSFS.java),
  random backward search (Random_RSFS.java),
  random forward-backward search (Random_RSFS.java),
  a multi-object genetic local search (Hybrid_NSGAII.java),
  computation of degree of dependency (Gamma_and_Relative_Dependency.java) and
  computation of relative dependency  (Gamma_and_Relative_Dependency.java).
The genetic algorithm and the multi-objectve genetic local search calls the jmetal optimization library and the Weka machine learning tool (weka.jar version 3.6) to train classifiers and evaluate their performance on test sets.

References:

D. Tian “A Multi-objective Genetic Local Search Algorithm for Optimal Feature Subset Selection”,
2016 International Conference on Computational Science and Computational Intelligence (CSCI), Las Vegas, USA, 2016

D. Tian, X. Zeng and J. Keane, “Core-generating Approximate Minimum Entropy Discretization for Rough Set Feature Selection in Pattern Classification” International Journal of Approximate Reasoning, vol. 52, issue 6, pp.863-880, 2011

D. Tian, J. Keane and X. Zeng, ``Evaluating The Effect of Rough Set Feature Selection On The Performance of Decision Trees'', Proc. IEEE International Conference on Granular Computing, Georgia State University, Atlanta, USA, 2006

