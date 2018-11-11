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
The genetic algorithm and the multi-objectve genetic local search calls the Weka machine learning tool (weka.jar version 3.6) to train classifiers and evaluate their performance on test sets. 
