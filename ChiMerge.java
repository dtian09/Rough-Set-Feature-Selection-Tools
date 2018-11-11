/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    ChiMerge.java
 *    Copyright (C) 2004 Giuseppe Manco
 *
 */



//package weka.filters.supervised.attribute;

import weka.filters.*;
import java.io.*;
import java.util.*;
import weka.core.*;

/**
 * @author Giuseppe Manco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ChiMerge extends Filter
		implements
			OptionHandler,
			SupervisedFilter,
			WeightedInstancesHandler {
	/* (non-Javadoc)
	 * @see weka.core.OptionHandler#listOptions()
	 */
	  /** Stores which columns to Discretize */
	  protected Range m_DiscretizeCols = new Range();

	  /** Store the current cutpoints */
	  protected double [][] m_CutPoints = null;

		/** Significance level value */
	  protected double  m_SignificanceLevel;

	  /** Output binary attributes for discretized attributes. */
	  protected boolean m_MakeBinary = false;

	  /** Use better encoding of split point for MDL. */
	  protected boolean m_UseBetterEncoding = false;

	  /** Constructor - initialises the filter */
	  public ChiMerge() {

		setAttributeIndices("first-last");
	  }
	  /**
	   * Returns the tip text for this property
	   *
	   * @return tip text for this property suitable for
	   * displaying in the explorer/experimenter gui
	   */
	  public String attributeIndicesTipText() {
		return "Specify range of attributes to act on."
		  + " This is a comma separated list of attribute indices, with"
		  + " \"first\" and \"last\" valid values. Specify an inclusive"
		  + " range with \"-\". E.g: \"first-3,5,6-10,last\".";
	  }
	  /**
	   * Signifies that this batch of input to the filter is finished. If the
	   * filter requires all instances prior to filtering, output() may now
	   * be called to retrieve the filtered instances.
	   *
	   * @return true if there are instances pending output
	   * @exception IllegalStateException if no input structure has been defined
	   */
	  public boolean batchFinished() {

		if (getInputFormat() == null) {
		  throw new IllegalStateException("No input instance format defined");
		}
		if (m_CutPoints == null) {
		  calculateCutPoints();

		  setOutputFormat();

		  // If we implement saving cutfiles, save the cuts here

		  // Convert pending input instances
		  for(int i = 0; i < getInputFormat().numInstances(); i++) {
		convertInstance(getInputFormat().instance(i));
		  }
		}
		flushInput();

		m_NewBatch = true;
		return (numPendingOutput() != 0);
	  }
	  /** Generate the cutpoints for each attribute */
	  protected void calculateCutPoints() {

		Instances copy = null;

		m_CutPoints = new double [getInputFormat().numAttributes()] [];
		for(int i = getInputFormat().numAttributes() - 1; i >= 0; i--) {
		  if ((m_DiscretizeCols.isInRange(i)) &&
		  (getInputFormat().attribute(i).isNumeric())) {

		  // Use copy to preserve order
		  if (copy == null) {
		    copy = new Instances(getInputFormat());
		  }
		  calculateCutPointsByChiTest(i, copy);
		  }
		}
	  }
	  /**
	   * Set cutpoints for a single attribute using MDL.
	   *
	   * @param index the index of the attribute to set cutpoints for
	   */
	  protected void calculateCutPointsByChiTest(int index,
						 Instances data) {

		// Sort instances
		data.sort(data.attribute(index));

		// Find first instances that's missing
		int firstMissing = data.numInstances();
		for (int i = 0; i < data.numInstances(); i++) {
		  if (data.instance(i).isMissing(index)) {
			firstMissing = i;
			break;
		  }
		}
		m_CutPoints[index] = cutPointsForSubset(data, index, 0, firstMissing);
	  }
	  /**
	   * Convert a single instance over. The converted instance is added to
	   * the end of the output queue.
	   *
	   * @param instance the instance to convert
	   */
	  protected void convertInstance(Instance instance) {

		int index = 0;
		double [] vals = new double [outputFormatPeek().numAttributes()];
		// Copy and convert the values
		for(int i = 0; i < getInputFormat().numAttributes(); i++) {
		  if (m_DiscretizeCols.isInRange(i) &&
		  getInputFormat().attribute(i).isNumeric()) {
		int j;
		double currentVal = instance.value(i);
		if (m_CutPoints[i] == null) {
		  if (instance.isMissing(i)) {
		    vals[index] = Instance.missingValue();
		  } else {
		    vals[index] = 0;
		  }
		  index++;
		} else {
		  if (!m_MakeBinary) {
		    if (instance.isMissing(i)) {
		      vals[index] = Instance.missingValue();
		    } else {
		      for (j = 0; j < m_CutPoints[i].length; j++) {
			if (currentVal < m_CutPoints[i][j]) {
			  break;
			}
		      }
				  vals[index] = j;
		    }
		    index++;
		  } else {
		    for (j = 0; j < m_CutPoints[i].length; j++) {
		      if (instance.isMissing(i)) {
					vals[index] = Instance.missingValue();
		      } else if (currentVal < m_CutPoints[i][j]) {
					vals[index] = 0;
		      } else {
					vals[index] = 1;
		      }
		      index++;
		    }
		  }
		}
		  } else {
			vals[index] = instance.value(i);
		index++;
		  }
		}

		Instance inst = null;
		if (instance instanceof SparseInstance) {
		  inst = new SparseInstance(instance.weight(), vals);
		} else {
		  inst = new Instance(instance.weight(), vals);
		}
		copyStringValues(inst, false, instance.dataset(), getInputStringIndex(),
						 getOutputFormat(), getOutputStringIndex());
		inst.setDataset(getOutputFormat());
		push(inst);
	  }
	  private double chiCriticalValue(double p, double df){
	  	    double  fval;
	  	    double  maxf = 99999.0;     /* maximum possible F ratio */
	  	    double  minf = .000001;     /* minimum possible F ratio */

	  	    if (p <= 0.0 || p >= 1.0)
	  	      return (0.0);

	  	    fval = 1.0 / p; /* the smaller the p, the larger the F */

	  	    while (Math.abs (maxf - minf) > .000001) {
	  	      if (Statistics.chiSquaredProbability(fval, df) < p) /* F too large */
	  		maxf = fval;
	  	      else /* F too small */
	  		minf = fval;
	  	      fval = (maxf + minf) * 0.5;
	  	    }

	  	    return (fval);

	  	}

	  /** Selects cutpoints for sorted subset. */
	  private double[] cutPointsForSubset(Instances instances, int attIndex,
					      int first, int lastPlusOne) {

		double[][] bestCounts;
		double[] priorCounts, left, right, cutPoints;
		double bestChiProb;
		int bestIndex = -1,  numCutPoints = 0;
		int size;

		// Compute number of instances in set
		if ((lastPlusOne - first) < 2) {
		  return null;
		}
		// Create a statistics table
		int nValues = instances.attributeStats(attIndex).distinctCount -1;
		FastVector counts = new FastVector(nValues+1);
		FastVector chiVals = new FastVector(nValues);
		int nClasses = instances.numClasses();
//		double chiVal = Statistics.chiCriticalValue(1-m_SignificanceLevel,nClasses-1);
		double chiVal = chiCriticalValue(1-m_SignificanceLevel,nClasses-1);

		priorCounts = new double[nClasses+1];
		for (int j = 0; j < nClasses; j++){
			priorCounts[j] = 0.0;
		}
		// Compute classes counts
		double val = instances.instance(0).value(attIndex);
		priorCounts[nClasses] = 0;
		for (int i = first; i < lastPlusOne; i++){
		    if (val != instances.instance(i).value(attIndex)){
				counts.addElement(priorCounts.clone());
				val = instances.instance(i).value(attIndex);
			  	for (int j = 0; j < nClasses; j++){
					priorCounts[j] = 0.0;
	  			}
				priorCounts[nClasses] = i;
		    }
			priorCounts[(int)instances.instance(i).classValue()] +=
			    instances.instance(i).weight();
		}
		counts.addElement(priorCounts.clone());
		bestCounts = new double[2][nClasses];

		for (int i = 0; i < nValues; i++) {
		    for (int j = 0; j < nClasses; j++){
			    bestCounts[0][j] = ((double[])counts.elementAt(i))[j];
			    bestCounts[1][j] = ((double[])counts.elementAt(i+1))[j];
			}
			chiVals.addElement(new Double(ContingencyTables.chiVal(bestCounts,false)));
		}

		do {
			bestChiProb = Double.MAX_VALUE;
			// Find best chi value.
			for (int i = 0; i < nValues; i++) {
				if (Utils.sm(((Double)chiVals.elementAt(i)).doubleValue(), bestChiProb)) {
				    bestChiProb = ((Double)chiVals.elementAt(i)).doubleValue();
				    bestIndex = i;
				}
			}
		    // Check if split is to be accepted
		    if (bestChiProb <= chiVal) {
				// Merge cutpoints and return them

				for (int i = 0; i < instances.numClasses(); i++) {
					((double[])counts.elementAt(bestIndex))[i] +=
						((double[])counts.elementAt(bestIndex+1))[i];
				}
				counts.removeElementAt(bestIndex+1);

				// recompute the chi values at the borders
				if (bestIndex > 0){
					chiVals.removeElementAt(bestIndex-1);
					for (int j = 0; j < nClasses; j++){
			 		   bestCounts[0][j] = ((double[])counts.elementAt(bestIndex-1))[j];
					   bestCounts[1][j] = ((double[])counts.elementAt(bestIndex))[j];
				    }
					chiVals.insertElementAt(new Double(ContingencyTables.chiVal(bestCounts,false)),bestIndex-1);
				}
				chiVals.removeElementAt(bestIndex);
				nValues--;
				if (bestIndex < nValues){
					chiVals.removeElementAt(bestIndex);
				    for (int j = 0; j < nClasses; j++){
			 		   bestCounts[0][j] = ((double[])counts.elementAt(bestIndex))[j];
					   bestCounts[1][j] = ((double[])counts.elementAt(bestIndex+1))[j];
				    }
					chiVals.insertElementAt(new Double(ContingencyTables.chiVal(bestCounts,false)),bestIndex);
				}
		    }
		}
		while (bestChiProb <= chiVal);

		// Determine cut points
		size = counts.size();
		cutPoints = new double[size];
		for (int i = 0; i < size; i++){
			int idx = (int)((double[])counts.elementAt(i))[nClasses];
			cutPoints[i] = instances.instance(idx).value(attIndex);
		}
		return cutPoints;
	  }
	   /**
	   * Gets the current range selection
	   *
	   * @return a string containing a comma separated list of ranges
	   */
	  public String getAttributeIndices() {

		return m_DiscretizeCols.getRanges();
	  }
	  /**
	   * Gets the cut points for an attribute
	   *
	   * @param the index (from 0) of the attribute to get the cut points of
	   * @return an array containing the cutpoints (or null if the
	   * attribute requested isn't being Discretized
	   */
	  public double [] getCutPoints(int attributeIndex) {

		if (m_CutPoints == null) {
		  return null;
		}
		return m_CutPoints[attributeIndex];
	  }
	  /**
	   * Gets whether the supplied columns are to be removed or kept
	   *
	   * @return true if the supplied columns will be kept
	   */
	  public boolean getInvertSelection() {

		return m_DiscretizeCols.getInvert();
	  }
	  /**
	   * Gets whether binary attributes should be made for discretized ones.
	   *
	   * @return true if attributes will be binarized
	   */
	  public boolean getMakeBinary() {

		return m_MakeBinary;
	  }
	  /**
	   * Gets the current settings of the filter.
	   *
	   * @return an array of strings suitable for passing to setOptions
	   */
	  public String [] getOptions() {

		String [] options = new String [11];
		int current = 0;

		if (getMakeBinary()) {
		  options[current++] = "-D";
		}
		if (getInvertSelection()) {
		  options[current++] = "-V";
		}
		// Giuseppe Manco added this part not necessary at all
		//----------------------------------------------------
		  options[current++] = "-C";
		  options[current++] = "" + getClassValue();
		//----------------------------------------------------


		options[current++] = "-A";
		  options[current++] = "" + getSignificanceLevel();

		if (!getAttributeIndices().equals("")) {
		  options[current++] = "-R"; options[current++] = getAttributeIndices();
		}
		while (current < options.length) {
		  options[current++] = "";
		}
		return options;
	  }
	/**
	 * Insert the method's description here.
	 * Creation date: (18/04/2001 19.02.59)
	 * @return double
	 */
	public double getSignificanceLevel() {
		return m_SignificanceLevel;
	}
	  /**
	   * Returns a string describing this filter
	   *
	   * @return a description of the filter suitable for
	   * displaying in the explorer/experimenter gui
	   */
	  public String globalInfo() {

		return "An instance filter that discretizes a range of numeric"
		  + " attributes in the dataset into nominal attributes."
		  + " Discretization is by Kerber's Chi-Squared method.";
	  }
	  /**
	   * Input an instance for filtering. Ordinarily the instance is processed
	   * and made available for output immediately. Some filters require all
	   * instances be read before producing output.
	   *
	   * @param instance the input instance
	   * @return true if the filtered instance may now be
	   * collected with output().
	   * @exception IllegalStateException if no input format has been defined.
	   */
	  public boolean input(Instance instance) {

		if (getInputFormat() == null) {
		  throw new IllegalStateException("No input instance format defined");
		}
		if (m_NewBatch) {
		  resetQueue();
		  m_NewBatch = false;
		}

		if (m_CutPoints != null) {
		  convertInstance(instance);
		  return true;
		}

		bufferInput(instance);
		return false;
	  }
	  /**
	   * Returns the tip text for this property
	   *
	   * @return tip text for this property suitable for
	   * displaying in the explorer/experimenter gui
	   */
	  public String invertSelectionTipText() {

		return "Set attribute selection mode. If false, only selected"
		  + " (numeric) attributes in the range will be discretized; if"
		  + " true, only non-selected attributes will be discretized.";
	  }
	  /**
	   * Gets an enumeration describing the available options
	   *
	   * @return an enumeration of all the available options
	   */
	  public Enumeration listOptions() {

		Vector newVector = new Vector(5);

		// Giuseppe Manco added this part not necessary at all
		//----------------------------------------------------
		newVector.addElement(new Option(
				  "\tSpecify the Supervised variable"
		      + " to discretize againts.\n",
				  "C", 1, "-C <num>"));
		//----------------------------------------------------


		newVector.addElement(new Option(
				  "\tSpecify the significance level to use"
		      + " in the Chi-Squared test.\n"
	   	      + "\t(default 0.95)",
				  "A", 1, "-A <num>"));

		/* If we decide to implement loading and saving cutfiles like
		 * the C Discretizer (which is probably not necessary)
		newVector.addElement(new Option(
				  "\tSpecify that the cutpoints should be loaded from a file.",
				  "L", 1, "-L <file>"));
		newVector.addElement(new Option(
				  "\tSpecify that the chosen cutpoints should be saved to a file.",
				  "S", 1, "-S <file>"));
		*/

		newVector.addElement(new Option(
				  "\tSpecify list of columns to Discretize. First"
		      + " and last are valid indexes.\n"
		      + "\t(default none)",
				  "R", 1, "-R <col1,col2-col4,...>"));

		newVector.addElement(new Option(
				  "\tInvert matching sense of column indexes.",
				  "V", 0, "-V"));

		newVector.addElement(new Option(
				  "\tOutput binary attributes for discretized attributes.",
				  "D", 0, "-D"));

		return newVector.elements();
	  }
	  /**
	   * Main method for testing this class.
	   *
	   * @param argv should contain arguments to the filter: use -h for help
	   */
	  public static void main(String [] argv) {

		try {
		  if (Utils.getFlag('b', argv)) {
	 	Filter.batchFilterFile(new ChiMerge(), argv);
		  } else {
		Filter.filterFile(new ChiMerge(), argv);
		  }
		} catch (Exception ex) {
		  System.out.println(ex.getMessage());
		}
	  }
	  /**
	   * Returns the tip text for this property
	   *
	   * @return tip text for this property suitable for
	   * displaying in the explorer/experimenter gui
	   */
	  public String makeBinaryTipText() {

		return "Make resulting attributes binary.";
	  }
	  /**
	   * Sets which attributes are to be Discretized (only numeric
	   * attributes among the selection will be Discretized).
	   *
	   * @param rangeList a string representing the list of attributes. Since
	   * the string will typically come from a user, attributes are indexed from
	   * 1. <br>
	   * eg: first-3,5,6-last
	   * @exception IllegalArgumentException if an invalid range list is supplied
	   */
	  public void setAttributeIndices(String rangeList) {

		m_DiscretizeCols.setRanges(rangeList);
	  }
	  /**
	   * Sets which attributes are to be Discretized (only numeric
	   * attributes among the selection will be Discretized).
	   *
	   * @param attributes an array containing indexes of attributes to Discretize.
	   * Since the array will typically come from a program, attributes are indexed
	   * from 0.
	   * @exception IllegalArgumentException if an invalid set of ranges
	   * is supplied
	   */
	  public void setAttributeIndicesArray(int [] attributes) {

		setAttributeIndices(Range.indicesToRangeList(attributes));
	  }
	  /**
	   * Sets the format of the input instances.
	   *
	   * @param instanceInfo an Instances object containing the input instance
	   * structure (any instances contained in the object are ignored - only the
	   * structure is required).
	   * @return true if the outputFormat may be collected immediately
	   * @exception Exception if the input format can't be set successfully
	   */
	  public boolean setInputFormat(Instances instanceInfo) throws Exception {

		super.setInputFormat(instanceInfo);

		//-----------------------------------------------------------------
		// This part is added by Giuseppe Manco, but isn't really necessary
		instanceInfo.setClassIndex(m_classValue);
		//-----------------------------------------------------------------
		m_DiscretizeCols.setUpper(instanceInfo.numAttributes() - 1);
		m_CutPoints = null;
		  if (instanceInfo.classIndex() < 0) {
		throw new UnassignedClassException("Cannot use class-based discretization: "
											   + "no class assigned to the dataset");
		  }
		  if (!instanceInfo.classAttribute().isNominal()) {
		throw new UnsupportedClassTypeException("Supervised discretization not possible:"
													+ " class is not nominal!");
		  }

		// If we implement loading cutfiles, then load
		//them here and set the output format
		return false;
	  }
	  /**
	   * Sets whether selected columns should be removed or kept. If true the
	   * selected columns are kept and unselected columns are deleted. If false
	   * selected columns are deleted and unselected columns are kept.
	   *
	   * @param invert the new invert setting
	   */
	  public void setInvertSelection(boolean invert) {

		m_DiscretizeCols.setInvert(invert);
	  }
	  /**
	   * Sets whether binary attributes should be made for discretized ones.
	   *
	   * @param makeBinary if binary attributes are to be made
	   */
	  public void setMakeBinary(boolean makeBinary) {

		m_MakeBinary = makeBinary;
	  }
	  /**
	   * Parses the options for this object. Valid options are: <p>
	   *
	   * -B num <br>
	   * Specify the (maximum) number of equal-width bins to divide
	   * numeric attributes into. (default class-based discretization).<p>
	   *
	   * -O
	   * Optimizes the number of bins using a leave-one-out estimate of the
	   * entropy.
	   *
	   * -R col1,col2-col4,... <br>
	   * Specify list of columns to discretize. First
	   * and last are valid indexes. (default none) <p>
	   *
	   * -V <br>
	   * Invert matching sense.<p>
	   *
	   * -D <br>
	   * Make binary nominal attributes. <p>
	   *
	   * -E <br>
	   * Use better encoding of split point for MDL. <p>
	   *
	   * -K <br>
	   * Use Kononeko's MDL criterion. <p>
	   *
	   * @param options the list of options as an array of strings
	   * @exception Exception if an option is not supported
	   */
	  public void setOptions(String[] options) throws Exception {

		setMakeBinary(Utils.getFlag('D', options));
		setInvertSelection(Utils.getFlag('V', options));

		// Giuseppe Manco added this part not necessary at all
		//----------------------------------------------------

		String cls = Utils.getOption('C',options);
		if (cls.length() != 0){
			setClassValue(Integer.parseInt(cls));
		}
		else
			setClassValue(-1);
		//----------------------------------------------------


		String confInt = Utils.getOption('A', options);
		if (confInt.length() != 0) {
		  setSignificanceLevel(Double.parseDouble(confInt));
		} else {
		  setSignificanceLevel(0.95);
		}

		String convertList = Utils.getOption('R', options);
		if (convertList.length() != 0) {
		  setAttributeIndices(convertList);
		} else {
		  setAttributeIndices("first-last");
		}

		if (getInputFormat() != null) {
		  setInputFormat(getInputFormat());
		}
	  }
	  /**
	   * Set the output format. Takes the currently defined cutpoints and
	   * m_InputFormat and calls setOutputFormat(Instances) appropriately.
	   */
	  protected void setOutputFormat() {

		if (m_CutPoints == null) {
		  setOutputFormat(null);
		  return;
		}
		FastVector attributes = new FastVector(getInputFormat().numAttributes());
		int classIndex = getInputFormat().classIndex();
		for(int i = 0; i < getInputFormat().numAttributes(); i++) {
		  if ((m_DiscretizeCols.isInRange(i))
		  && (getInputFormat().attribute(i).isNumeric())) {
		if (!m_MakeBinary) {
		  FastVector attribValues = new FastVector(1);
		  if (m_CutPoints[i] == null) {
		    attribValues.addElement("'All'");
		  } else {
		    for(int j = 0; j <= m_CutPoints[i].length; j++) {
		      if (j == 0) {
			attribValues.addElement("'(-inf-"
				+ Utils.doubleToString(m_CutPoints[i][j], 6) + ")'");
		      } else if (j == m_CutPoints[i].length) {
			attribValues.addElement("'["
				+ Utils.doubleToString(m_CutPoints[i][j - 1], 6)
						+ "-inf)'");
		      } else {
			attribValues.addElement("'["
				+ Utils.doubleToString(m_CutPoints[i][j - 1], 6) + "-"
				+ Utils.doubleToString(m_CutPoints[i][j], 6) + ")'");
		      }
		    }
		  }
		  attributes.addElement(new Attribute(getInputFormat().
						      attribute(i).name(),
						      attribValues));
		} else {
		  if (m_CutPoints[i] == null) {
		    FastVector attribValues = new FastVector(1);
		    attribValues.addElement("'All'");
		    attributes.addElement(new Attribute(getInputFormat().
							attribute(i).name(),
							attribValues));
		  } else {
		    if (i < getInputFormat().classIndex()) {
		      classIndex += m_CutPoints[i].length - 1;
		    }
		    for(int j = 0; j < m_CutPoints[i].length; j++) {
		      FastVector attribValues = new FastVector(2);
		      attribValues.addElement("'(-inf-"
			      + Utils.doubleToString(m_CutPoints[i][j], 6) + "]'");
		      attribValues.addElement("'("
			      + Utils.doubleToString(m_CutPoints[i][j], 6) + "-inf)'");
		      attributes.addElement(new Attribute(getInputFormat().
							  attribute(i).name(),
							  attribValues));
		    }
		  }
		}
		  } else {
		attributes.addElement(getInputFormat().attribute(i).copy());
		  }
		}
		Instances outputFormat =
		  new Instances(getInputFormat().relationName(), attributes, 0);
		outputFormat.setClassIndex(classIndex);
		setOutputFormat(outputFormat);
	  }
	  /**
	   * Set the value of m_SignificanceLevel.
	   *
	   * @param confInterval Value to assign to m_ConfInterval.
	   */
	  public void setSignificanceLevel(double confInterval) {

		m_SignificanceLevel = confInterval;
	  }
	  /**
	   * Returns the tip text for this property
	   *
	   * @return tip text for this property suitable for
	   * displaying in the explorer/experimenter gui
	   */
	  public String significanceLevelTipText() {

		return "Use the given significance level for the Chi-Squared test.";
	  }

		public int m_classValue;

	/**
	 * GIUSEPPE: No necessary method
	 * Creation date: (16/03/2002 20.50.46)
	 * @return java.lang.String
	 */
	public String classValueTipText() {
		return "Specify the supervised variable to discretize against.";
	}

	/**
	 * GIUSEPPE - No necessary method.
	 * Creation date: (16/03/2002 20.52.04)
	 * @return int
	 */
	public int getClassValue() {
		return m_classValue;
	}

	/**
	 * GIUSEPPE: not necessary method
	 * Creation date: (16/03/2002 20.53.33)
	 * @param clas int
	 */
	public void setClassValue(int clas) {
		m_classValue = clas;
	}
}
