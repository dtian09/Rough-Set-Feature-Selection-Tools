/* 
   RSys.java - defines the RSys class 

   Copyright 2014 The University of Manchester tiand@cs.man.ac.uk

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
  
   The source files are available at: https://github.com/linked2safety/code
*/
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.BitSet;
import java.util.HashMap;

public final class RSys
{
  ArrayList<BitSet>[] partitions; //partitions for each individual attribute
  int attributes;
  int objects;
  BitSet remaining;
  BitSet empty;
  ArrayList<BitSet> R_ind;// U/IND(B) i.e. partition of U using a subset B
  int class_index0;
  int current_missing;
  int best_missing;
  boolean [] discObjects;
  boolean bailed;
  String [][] data_matrix;
  //ArrayList<BitSet>current;
  HashMap <BitSet,Float> subsets_partitions_sizes;
  HashMap <BitSet,ArrayList<BitSet>> subsets_partitions;
  ArrayList<BitSet> class_partition;// U/{class attribute}
  public RSys(String [][] data, int rows, int cols, int class_index) 
  {
    data_matrix = data;
	//discObjects = new boolean[rows];
    objects = rows;
    attributes = cols;//including the class attribute
    class_index0 = class_index;
    /*partitions = new ArrayList[attributes];
    constructPartitions(data);*/
    subsets_partitions_sizes = new HashMap<BitSet,Float>();
    subsets_partitions = new HashMap<BitSet,ArrayList<BitSet>>();
    class_partition = null;
  }

  public void constructPartitions(String [][] data)
  {
	//Compute the partition of each attribute i.e. U/IND(a), U/IND(b), ..., U/IND(k)
	//then, store the partitions into an array partitions.
	//input: data set
	//output: the partitions using each attribute
	//        (each partition U/IND(a) is stored in the global array 'partitions' 
	//        as partitions[a])
		  
    BitSet eq;
    for(int a=0;a<attributes;a++) {
      partitions[a] = new ArrayList<BitSet>();
      remaining = new BitSet(objects);
      for (int i=0;i<objects;i++) remaining.set(i);

      for( int o1 = 0; o1<objects; o1++ ) {
        if( remaining.get(o1) )
        {
          eq =  new BitSet(objects);
          eq.set(o1);
          for(int o2 = o1+1;o2<objects;o2++)
            if(remaining.get(o2))
              if((data[o1][a]).equals(data[o2][a]))
   	           {
                eq.set(o2);
		        remaining.clear(o2);
	           }
               remaining.clear(o1);
	           partitions[a].add(eq);
        }
      }
    }
  }

  public ArrayList<BitSet> partition(ArrayList a1) 
  { 
	//Compute the partition U/IND(B) of feature subset B where
	//B is created by adding a new feature a1 to B1 the previous feature subset 
	//i.e. B = B1 U {a1}
	// U/IND(B) = U/IND(B1) intersect U/IND(a1)
	//          = { elem | elem = elem1 intersect elem2, elem1 is in U/IND(B1), elem2 is in U/IND(a1)} 
	// e.g. U/IND(B1) = {{o1,o2,o3,o4,o5}} 
	//      U/IND(a1)={{o1,o2}{o3,o4,o5}}
	//      U/IND(B) = {{o1,o2},{o3,o4,o5}}
	//	   
	//      U/IND(B1) = {{o1,o2},{o3,o4},{o5}}
	//		U/IND(a1)={{o1,o2}{o3,o4,o5}} 
	//		U/IND(B) = {{o1,o2},{o3,o4},{o5}}
	// 
	//R_ind is U/IND(B1)
	//
	//input: U/IND({a1}),
	//       R_ind
	//output: U/IND(B)
	  
    Iterator IND_a = a1.iterator();
    BitSet temp = null;
    ArrayList<BitSet> newPartition = new ArrayList<BitSet>();
    Iterator IND_reduct;

    while (IND_a.hasNext()) {
      BitSet t = (BitSet)IND_a.next();
      IND_reduct = R_ind.iterator();

      while (IND_reduct.hasNext()) {
	    temp = (BitSet)t.clone();
	    temp.and((BitSet)IND_reduct.next());
	    if (!temp.isEmpty()) {
	     newPartition.add(temp);
	    }
      }
    }
    return newPartition;
  }

  public ArrayList<BitSet> partition2(BitSet subset)
  {//calculate partition of a feature subset i.e. U/IND(B).
   //This method is faster than partition(ArrayList a1)
   //ref: Han Jianchao, Feature Selection Based on Relative Attribute Dependency: An Experimental Study, RSFDGrC 2005
	  
   ArrayList<BitSet>partition = new ArrayList<BitSet>(); 
   HashSet<Integer> added_rows = new HashSet<Integer>();//the rows that have been added equivalence classes
   boolean match=true;
  
   for(int i=0;i<data_matrix.length;i++)
   { 
     
     if(added_rows.size() == data_matrix.length)
       break;
     else
     {       
      if(added_rows.contains(new Integer(i))==false)
      {
       BitSet eq = new BitSet(objects);//an equivalence class
       eq.set(i);
       added_rows.add(new Integer(i));
       
       for(int i2=0; i2<data_matrix.length; i2++)
       {
        if(i2!=i)
        {
         if(added_rows.contains(new Integer(i2))==false)
         {
           for(int j=0;j<data_matrix[0].length;j++)
           {
        	 if(subset.get(j))
        	 {	 
              if(data_matrix[i][j].equals(data_matrix[i2][j]))
              {
              }
              else
              {     
               match=false;
               break;
              }
        	 }
           }//for
           if(match==true)
           {
            eq.set(i2);
            added_rows.add(new Integer(i2)); 
           }      
           else
            match=true;
         }//if
        }//if
       }//for
       if(eq.size()>0)
         partition.add(eq);
      }//if 
     }//if          
    }//for
    
    return partition;
  }
  
  public ArrayList<BitSet> partition3 (ArrayList partitionOfSubset, ArrayList partitionOfSubset2) 
  { 
	//Compute the partition U/IND(B1 U B2) of feature subset B and B2
	// U/IND(B1 U B2) = U/IND(B1) intersect U/IND(B2)
	//          = { elem | elem = elem1 intersect elem2, for each pair (elem1, elem2) and elem1 in U/IND(B1) and elem2 in U/IND(B2)} 
	// e.g. U/IND(B1) = {{o1,o2,o3,o4,o5}} 
	//      U/IND(a1)={{o1,o2}{o3,o4,o5}}
	//      U/IND(B) = {{o1,o2},{o3,o4,o5}}
	//	   
	//      U/IND(B1) = {{o1,o2},{o3,o4},{o5}}
	//		U/IND(a1)={{o1,o2}{o3,o4,o5}} 
	//		U/IND(B) = {{o1,o2},{o3,o4},{o5}}
	// 
	//R_ind is U/IND(B1)
	//
	//input: U/IND(B1), partition of subset B1
	//       U/IND(B2), partition of subset B2
	//output: U/IND(B1 U B2)
	 	    
    Iterator iter_subset2 = partitionOfSubset2.iterator();
    BitSet temp = null;
    ArrayList<BitSet> newPartition = new ArrayList<BitSet>();
    Iterator iter_subset=null;
    BitSet e = null;
    BitSet t = null;
    while(iter_subset2.hasNext())
    {
      t = (BitSet)iter_subset2.next();
      iter_subset = partitionOfSubset.iterator();
      while(iter_subset.hasNext())
      {
	    temp = (BitSet)t.clone();
	    e = (BitSet)iter_subset.next();
	    temp.and(e);
	    if(!temp.isEmpty())
	    {
	     newPartition.add(temp);
	    }
      }
    }
    return newPartition;
  }

  public float relativeDependency(BitSet subset)
  {//calculate relative dependency of a subset: |U/IND(B)| / |U/IND(B union Class)|   
   //if a data set has noise, relative dependency of the set of all features is < 1.0
   //If gamma is < 1.0, the data set needs to be pre-processed (e.g. discretization etc) 
   //to improve its quality until the relative dependency == 1.0

	  BitSet subset2;
	  BitSet class_attr;
	  ArrayList<BitSet> p=null;
	  ArrayList<BitSet> p2=null;
	  float s;
	  float s2;
	  //compute U/IND(subset)
	  if(!subsets_partitions_sizes.containsKey(subset))
	  {
		  p = partition2(subset);
		  s = (float)p.size();
		  subsets_partitions_sizes.put(subset,s);
		  subsets_partitions.put(subset,p);
	  }
	  else
	  {
		  s = subsets_partitions_sizes.get(subset);
	  }
	  //compute U/IND(subset U {class_attribute}) where subset2 = subset U {class_attribute}
	  subset2 = (BitSet)subset.clone();
	  subset2.set(class_index0);	  
	  if(!subsets_partitions_sizes.containsKey(subset2))
	  {
		  /*
		  s2 = (float)partition2(subset2).size();
		  subsets_partitions_sizes.put(subset2, s2);
		  */		  
		  class_attr = new BitSet(attributes);
		  class_attr.set(class_index0);
		  if(class_partition == null)
		  {
			 class_partition = partition2(class_attr);// U/IND(class_attr)
		  }
		  //System.out.println("attributes: "+attributes);
		  //System.out.println(class_attr);
		  //System.out.println(class_partition);
		  p = subsets_partitions.get(subset);
		  p2 = partition3(p,class_partition); // U/IND(subset U {class_attr})
		  s2 = (float)p2.size();
		  subsets_partitions_sizes.put(subset2, s2);
	  }
	  else
	  {
		  s2 = subsets_partitions_sizes.get(subset2);
	  }
	  //System.out.println("relative dependency: "+s/s2);
	  return s/s2;
  }
  
  public float calculateGamma2(BitSet subset)
  {	  //calculate degree of dependency (gamma). 
	  //This method is faster than calculateGamma(int a)
	  //
	  //if a data set has noise, gamma of the set of all features is < 1.0
	  //If gamma is < 1.0, the data set needs to be pre-processed (e.g. discretization etc) 
	  //to improve its quality until the gamma == 1.0
	    ArrayList<BitSet> partition;
	    BitSet subset2; 	    
	    ArrayList<BitSet> decision_classes;
        Iterator<BitSet> e ;//an iterator of equivalence classes
	    BitSet temp = null;//an equivalence class
	    float positive_region_size = 0;
	    float temp_positive_region_size = 0;
	    Iterator<BitSet> f;//an iterator of decision classes
	    BitSet decision_class;

	    subset2 = new BitSet(attributes);
	    subset2.set(class_index0);
	    decision_classes = partition2(subset2);//partition using decision variable
	    
	    partition = partition2(subset);
	    e = partition.iterator();
	    while(e.hasNext())
	    {
	      temp = (BitSet)e.next();
	      f = decision_classes.iterator();
	      temp_positive_region_size=0;
	      while(f.hasNext())
	      {
	       decision_class = (BitSet)f.next();
	       temp_positive_region_size += subset(temp,decision_class);
	      }	    
	      positive_region_size+=temp_positive_region_size;
	    }
	    return positive_region_size/objects;
   }
  
 public BitSet positiveRegion(BitSet subset)
 { //return a BitSet representing the positive region of a subset
	 BitSet pos_region;
	 ArrayList<BitSet> partition;
	 BitSet subset2; 	    
	 ArrayList<BitSet> decision_classes;
     Iterator<BitSet> e ;
     
     BitSet eq = null;//an equivalence class
     Iterator<BitSet> f;
     BitSet decision_class;
     BitSet objectSubset;
     
     pos_region = new BitSet(objects);
     
     subset2 = new BitSet(attributes);
     subset2.set(class_index0);
     decision_classes = partition2(subset2);//partition using decision variable

     partition = partition2(subset);//partition using subset
     e = partition.iterator();
     while(e.hasNext())
     {
    	 eq = (BitSet)e.next();
    	 f = decision_classes.iterator(); 	
    	 while(f.hasNext())
    	 {
    		 decision_class = (BitSet)f.next();
    		 objectSubset = subset2(eq,decision_class);
    		 pos_region = union(pos_region,objectSubset);
    	 }
     }
	 return pos_region;
 }
 
 public BitSet subset2(BitSet eq, BitSet decision_class)
 {  //if the equivalence class is a subset of the decision class 
	 //then return the equivalence class
	 //otherwise, return an empty set

	 boolean issubset = true;
	 
	   for(int i=0;i<objects;i++) 
	   {
		   if(eq.get(i))
		   {
			   if(!decision_class.get(i))
			   {
				   issubset = false;     
				   break;
			   }		   
		   }
	   }
	   if(issubset)
	      return eq;
	   else
	   {
		   return (new BitSet(objects));
	   }
 }
     
 public BitSet union(BitSet set_of_objects1, BitSet set_of_objects2)
 {
	if (set_of_objects2.isEmpty())
		return set_of_objects1;	
	else
	{
		for(int i=0; i<objects; i++)
			if(set_of_objects2.get(i))
				set_of_objects1.set(i,true);
	    return set_of_objects1;
	}
 }
 
 public BitSet getPositiveRegion(BitSet subset, FileWriter out) throws IOException
 {//get the positive region of a data set and write it to a csv file
   //return the bit set of objects (noise instances) outside the positive region
	 BitSet pos_region;
	 BitSet outside_pos_region;
	 
	 int pos_region_size=0;
	 
	 outside_pos_region = new BitSet(objects);
	 	 
	 pos_region = positiveRegion(subset);
	 for(int i=0; i<objects; i++)
		 if(pos_region.get(i))
		 {  
			 pos_region_size++;
			 out.write(data_matrix[i][0]);
			 for(int j=1; j<data_matrix[0].length;j++)
				 out.write(","+data_matrix[i][j]);
			 out.write("\n");
		 }
		 else
			 outside_pos_region.set(i);
	  out.close();
	  System.out.println("positive region size: "+pos_region_size);
	  return outside_pos_region;
 }
 
 
  //if b1 subset= b2 then return no. of object in b1 else return 0
  public float subset(BitSet b1,BitSet b2) 
  {
    float ret=0;
    //boolean[] discTemp = discObjects;

    for(int i=0;i<objects;i++) 
    {
      if(b1.get(i))
      {
	   if(!b2.get(i))
        {
	     ret=0;
	     break;
	    }
	    else
        {
	     ret++;
        }
      }
    }
    return ret;
  }

  public int size(BitSet b)
  {
    int subset_size=0;
    for(int i=0;i<objects;i++)
    {
      if(b.get(i))
      {
       subset_size++;
      }
    }
    return subset_size;
  }

 public float calculateGamma(int a) 
 { //compute the gamma (degree of dependency) of the new subset which is created by adding a new attribute 'a' to the previous subset
	ArrayList<BitSet> current;
	current = partition(partitions[a]);
    Iterator e = current.iterator();
    BitSet temp = null;
    float gamma = 0;
    float tempGamma = 0;
    Iterator f;
    
    while(e.hasNext())
    {
      temp = (BitSet)e.next();
      f = partitions[class_index0].iterator(); //partition using decision variable
      tempGamma=0;
      while(f.hasNext())
      {
	   tempGamma += subset(temp,(BitSet)f.next());
      }
      if(tempGamma == 0)
      {
	   current_missing += size(temp);
	   if(current_missing >= best_missing)
       { 
 	     bailed=true;
         break;
       }	
      }
      else gamma+=tempGamma;      
    }
    return gamma/objects;
  }
 
 public String quickReduct()
 {
    BitSet reduct = new BitSet(attributes);
    empty = new BitSet(objects);
    int best=-1;
    float gamma = 0;
    float bestGamma=-1;
    best_missing=objects;
    BitSet temp=null;
    float max_so_far=-1;

    //initialise the partition U/IND(B) to {{o1,o2,o3,...oN}}
    //where B is a subset; {o1,o2,o3,...oN} is the set of all instances in a data set
    //R_ind is U/IND(B)
    R_ind = new ArrayList<BitSet>();
    BitSet t = new BitSet(objects);
    for(int i=0;i<objects;i++) 
      t.set(i);
    R_ind.add(t);
     
    while(true) 
    {
      for(int a=0;a<attributes;a++) 
      {
	   if(!reduct.get(a)&&a!=class_index0)
 	   {  
	    current_missing=0;
	    gamma=calculateGamma(a);//gamma of the new subset by adding a new feature to the current subset
        //System.out.println(" gamma of selecting "+a+" = "+gamma);
	    if(gamma>bestGamma)
	    {
	     bestGamma=gamma;
	     best=a;
	     best_missing=current_missing;
	     if(gamma==1)
          break;
	    }
	   }
      }
      if(max_so_far>=bestGamma)//previous gamma better than new gamma   
         break;
      else
      {
	    max_so_far = bestGamma;
	    reduct.set(best);	    
	    System.out.println("gamma of "+reduct.toString()+" = "+bestGamma);
        R_ind = partition(partitions[best]);
        //debug
         
        /*
	    if(calculateGamma2(reduct)==bestGamma)
        	System.out.println("calculateGamma2 = calculateGamma");
        else
        	System.out.println("calculateGamma2 != calculateGamma");
	   
	    Iterator iter = current.iterator();
	    BitSet eq;
	    Boolean contain_all_elements = true;
	    while(iter.hasNext())
	    {
	    	eq = (BitSet)iter.next();
	    	if(!R_ind.contains(eq))
	    	{
	    	  System.out.println("R_ind != partition");
	    	  contain_all_elements = false;
	    	  break;
	    	}
	    }
	    if(contain_all_elements == true && current.size()==R_ind.size())
         System.out.println("R_ind == partition");
	    else
      	 System.out.println("R_ind != partition");
		*/    
	    //debug end
       
	    if(bestGamma==1)
        {
          break;
        }	  
      }
    }
    System.out.println("size of "+reduct.toString()+": "+size(reduct)+" gamma = "+bestGamma);  
    reduct.set(class_index0);
    return reduct.toString();   
  }
 
 public String quickReduct_fast()
 { //implement Quick Reduct algorithm. This method is faster than method quickReduct
    //BitSet reduct = new BitSet(attributes);
   
    float gamma = 0;
    float bestGamma=-1;
    BitSet reduct;
    BitSet newSubset;    
    BitSet allFeatures;
    float gammaOfAllFeatures;
    
    //reduct = new BitSet(attributes);
    allFeatures = new BitSet(attributes);
    newSubset = new BitSet(attributes);
    reduct = new BitSet(attributes);
    
    for(int i=0; i<attributes; i++)
    {
    	allFeatures.set(i);
    	reduct.set(i);
    	newSubset.set(i,false);
    }
    
    gammaOfAllFeatures = calculateGamma2(allFeatures);
    System.out.println("gamma of all features: "+gammaOfAllFeatures);

    while(reduct.size() < attributes) 
    {
      for(int a=0;a<attributes;a++) 
      {
	   if(!newSubset.get(a)&&a!=class_index0)
 	   {  
		    newSubset.set(a);
		    gamma=calculateGamma2(newSubset);
	        if(gamma == gammaOfAllFeatures)
	        {
	          reduct = newSubset;
	          break;
	        }
		    if(gamma > bestGamma)
		    {
		      bestGamma=gamma;
		      reduct = (BitSet) newSubset.clone();	      
		    }
		    newSubset.clear(a);
	    }
      }
      System.out.println("gamma of "+reduct.toString()+" = "+bestGamma);
    }
    System.out.println("size of "+reduct.toString()+": "+size(reduct)+" gamma = "+bestGamma);
    reduct.set(class_index0);
    return reduct.toString();   
  }
 
 /*
 public BitSet intersects(BitSet b1, BitSet b2) 
 {
   BitSet ret=null;
   boolean flag=false;

   for(int i=0;i<objects;i++)
   {
     if(b1.get(i)&&b2.get(i))
     {
	   ret = b1;
	   break;
     }
   }
   return ret;
 }
*/
}
