import java.util.*;
import java.io.*;
//input: a data file in weka format
//		 number of reducts to find
//		 operating system "windows" or "linux"
//output: reducts
//usage: java Random_RSFS <data file> <no. of reducts to find> <operating system "windows" or "linux">
public class Random_RSFS {  	
   public static void main(String[] args) {
	   String data_matrix [][]=null;
	   String data_file=null;
	   ReductsSearch rs=null;
	   RSys rsys=null;
	   int class_index=0; 
	   int number_of_reducts=1;
	   Utility util=null;
	   BitSet allFeatures=null;
	   BitSet reduct =null;
	   HashSet<BitSet> reducts;
	   Iterator<BitSet> iter;
	   int n;
	   FileWriter out=null;
	   String os=null;
	   BitSet smallestSubset;
	   if(args.length == 3)
	   {
		   data_file = args[0];
		   number_of_reducts = Integer.parseInt(args[1]);//no. of reducts
		   os = args[2];//operating system "windows" or "linux"
	   }
	   else
	   {
		   System.out.println("wrong number of input arguments.\nUsage: java Random_RSFS <data_file> weka <no_of_reducts> <os>");
		   System.exit(-1);
	   }
	   try
	   {
		   out = new FileWriter(data_file+".random_rsfs_reducts");
	   }
	   catch(IOException e)
	   {
		   System.out.println("IOException in open "+data_file+".random_rsfs_reducts");
		   System.exit(-1);
	   }
	   rs = new ReductsSearch("classifier",os,"Binary",data_file);   	   
	   data_matrix =  rs.data_matrix;
	   class_index = data_matrix[0].length-1;
	   rsys = new RSys(data_matrix, data_matrix.length, data_matrix[0].length, class_index);
	   allFeatures = new BitSet(rs.numberOfBits+1);
	   for(int k = 0; k < rs.numberOfBits; k++)
		   allFeatures.set(k);
	   rs.gamma_setOfAllFeatures = (rs.rsys).relativeDependency(allFeatures);
	   n=0;//no. of reducts which have been found so far
	   //no_of_times_to_try = 1;//no. of times to try to find a new reduct
	   //no_of_times = 0;//no. of times which have been tried to find a new reduct
	   reducts = new HashSet<BitSet>();	   
	   smallestSubset = allFeatures;
	   util = new Utility();
	   while(n < number_of_reducts)
	   {   		   
		   allFeatures = new BitSet(rs.numberOfBits+1);
		   for(int k = 0; k < rs.numberOfBits; k++)
			   allFeatures.set(k);
		   //===option 1: Do forward selection then do backward elimination
		   //reduct = util.RandomForwardSearch(rs,rsys,allFeatures);   		   
		   //reduct = util.RandomBackwardElimination(rs,rsys,reduct); 
		   //===option 2: Do backward elimination only
		   reduct = util.RandomBackwardElimination(rs,rsys,allFeatures); 
		   //System.out.println(reduct);
		   reducts.add(reduct);
		   if(util.get_subset_size(reduct, rs.numberOfBits) < util.get_subset_size(smallestSubset, rs.numberOfBits))
			   smallestSubset = reduct;
		   //n++;
		   n = reducts.size();
	   }		 	   
	   System.out.println("no. of reducts found: "+reducts.size());
	   iter = reducts.iterator();
	   try
	   {
		   out.write("smallest reduct: "+smallestSubset+"\n");
		   while(iter.hasNext())
		   {
			   reduct = iter.next();
			   System.out.println(reduct+" size: "+util.get_subset_size(reduct,rs.numberOfBits));
			   out.write(reduct+"\n");
		   }
		   out.close();
	   }
	   catch(IOException e)
	   {
		   System.out.println("IOException in write "+data_file+".random_rsfs_reducts");
		   System.exit(-1);
	   }
	   System.out.println("reducts are saved to "+data_file+".random_rsfs_reducts");
   }
}

