import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class CandidateGeneration {
	static List<String> listFeatures;
	static LinkedList<List<ItemsetObj>> listFreqItemsets;
	static HashMap<String, Integer> hashFreqItemset;
	static float minSup;
	static int gen_itemsets;
	static int freq_itemsets;
	static int closed_freq_itemsets;
	static int maximal_freq_itemsets;
   
   public static void init(float sup){
	   listFeatures = new ArrayList<String>();
	   listFreqItemsets = new LinkedList<List<ItemsetObj>>();
	   hashFreqItemset = new HashMap<String, Integer>();
	   minSup = sup;
   }
   
   public static void populateListFeatures(){
	  for(int i=0;i<DataPreprocessing.cols;i++){
		 listFeatures.add(i+"");
	  }
	}
   
   public static void populateK1Itemset(){
	  List<ItemsetObj> listItemsets = new ArrayList<ItemsetObj>();	   
	   
	  for(int i=0;i<listFeatures.size();i++){
	    gen_itemsets++;
	    ItemsetObj itemset_obj = new ItemsetObj(listFeatures.get(i));
	    
	    boolean is_freq = isFrequent(itemset_obj);
	   
	    if(is_freq){
		  freq_itemsets++;
		  listItemsets.add(itemset_obj);
		  hashFreqItemset.put(itemset_obj.name, itemset_obj.support_count);
	    }
	  }
	  
	  listFreqItemsets.add(listItemsets);
   }
   
   public static boolean isFrequent(ItemsetObj itemset_obj){
	   String item = itemset_obj.name;
	   String[] temp = item.split(",");
	   int count = 0;
	   boolean flag = true;
	   
	   for(int i=0;i<DataPreprocessing.data.length;i++){
		  for(int j=0;j<temp.length;j++){
			 if(DataPreprocessing.data[i][Integer.parseInt(temp[j])]==1)
			   flag = true;
			 else{
				 flag = false;
			     break;
			 }
		  }
		  
		  if(flag)
		    count++;
		    
		}
	   
	   itemset_obj.support_count = count;
	   
	   if((float)count/DataPreprocessing.data.length >= minSup)
		   return true;
	   return false;
   }
   
   public static void generateFkMinus1F1(){
	  populateK1Itemset(); 
	  
	  while(listFreqItemsets.get(listFreqItemsets.size()-1).size()!=0){
		  List<ItemsetObj> listItemsets = new ArrayList<ItemsetObj>();	   
		  List<ItemsetObj> list_FkMinus1 = listFreqItemsets.get(listFreqItemsets.size()-1);
		  
		  for(int i=0;i<list_FkMinus1.size();i++){
			  String item = list_FkMinus1.get(i).name;
			  int item_support = list_FkMinus1.get(i).support_count;
			  int last_feat = Integer.parseInt(item.substring(item.lastIndexOf(',')+1));
			  
			  boolean closed_flag = true;
			  boolean maximal_flag = true;
			  for(int j=last_feat+1;j<listFeatures.size();j++){
				  gen_itemsets++;
				  
				  String new_item = item + "," + listFeatures.get(j);
				  ItemsetObj itemsetObj = new ItemsetObj(new_item);
				  
				  boolean is_freq = isFrequent(itemsetObj);
				  int new_item_support = itemsetObj.support_count;
				  
				  if(item_support==new_item_support)
					  closed_flag = false;
				  
				  if(is_freq){
					freq_itemsets++;
					listItemsets.add(itemsetObj);
					hashFreqItemset.put(itemsetObj.name, itemsetObj.support_count);
					maximal_flag = false;
				  }
			  }
			  
			  if(closed_flag)
				closed_freq_itemsets++;
			  
			  if(maximal_flag)
				maximal_freq_itemsets++;
		   }
		   
	     listFreqItemsets.add(listItemsets);
	  }
	  
	  listFreqItemsets.remove(listFreqItemsets.size()-1);
	   
   }
   
   public static void generateFkMinus1_2(){
	   populateK1Itemset();
	   
	   while(listFreqItemsets.get(listFreqItemsets.size()-1).size()!=0){
		   List<ItemsetObj> listItemsets = new ArrayList<ItemsetObj>();	   
		   List<ItemsetObj> list_FkMinus1 = listFreqItemsets.get(listFreqItemsets.size()-1);
		   
		   for(int i=0;i<list_FkMinus1.size();i++){
			  String item = list_FkMinus1.get(i).name;
			  int item_support = list_FkMinus1.get(i).support_count;
			  int pos1 = item.lastIndexOf(',');
			  
			  if(pos1==-1){
				  boolean closed_flag = true;
				  boolean maximal_flag = true;
				  for(int j=i+1;j<list_FkMinus1.size();j++){
					 gen_itemsets++; 
					 String new_item = item+","+list_FkMinus1.get(j).name;
					 ItemsetObj itemsetObj = new ItemsetObj(new_item);
					 
					 boolean is_freq = isFrequent(itemsetObj);
					 int new_item_support = itemsetObj.support_count;
					  
					  if(item_support==new_item_support)
						  closed_flag = false;
					  
					 if(is_freq){
					   freq_itemsets++;
					   listItemsets.add(itemsetObj);
					   hashFreqItemset.put(itemsetObj.name, itemsetObj.support_count);
					   maximal_flag = false;
					 }
				  }
				  
				  if(closed_flag)
					 closed_freq_itemsets++;
				  
				  if(maximal_flag)
					 maximal_freq_itemsets++;
				  
				}
			  else{
				 boolean closed_flag = true;
				 boolean maximal_flag = true;
				 String k_minus_2_items = item.substring(0, pos1); 
				 
				 for(int j=i+1;j<list_FkMinus1.size();j++){
					 
					 int pos2 = list_FkMinus1.get(j).name.lastIndexOf(',');
					 
					 if(k_minus_2_items.equals(list_FkMinus1.get(j).name.substring(0, pos2))){
						 gen_itemsets++;
						 
						 String new_item = item+","+list_FkMinus1.get(j).name.substring(pos2+1);
						 ItemsetObj itemsetObj = new ItemsetObj(new_item);
						 
						 boolean is_freq = isFrequent(itemsetObj);
						 int new_item_support = itemsetObj.support_count;
						  
						  if(item_support==new_item_support)
							  closed_flag = false;
						  
						 if(is_freq){
						   freq_itemsets++;
						   listItemsets.add(itemsetObj);
						   hashFreqItemset.put(itemsetObj.name, itemsetObj.support_count);
						   maximal_flag = false;
						 }
					 }
				 }
				 
				 if(closed_flag)
				   closed_freq_itemsets++;
				 
				 if(maximal_flag)
					 maximal_freq_itemsets++;
			  }
		   }
		   
		   listFreqItemsets.add(listItemsets);
	   }
	   
	   listFreqItemsets.remove(listFreqItemsets.size()-1);
   }
   
   
   public static void displayFreqItemsets(){
	   for(int i=0;i<listFreqItemsets.size();i++){
		   List<ItemsetObj> list_itemset = listFreqItemsets.get(i);
		   
		   for(int j=0;j<list_itemset.size();j++)
		     System.out.print(list_itemset.get(j).name+" | ");
		   System.out.println();
	   }
   }
   
   public static void generateCandidates(String type){
	   populateListFeatures();
	   
	   if(type.equals("1"))
	      generateFkMinus1F1();
	   else
		  generateFkMinus1_2();
	   
	   displayFreqItemsets();
	   System.out.println("Generated Itemsets: "+gen_itemsets);
	   System.out.println("Frequent Itemsets: "+freq_itemsets);
	   System.out.println("Closed Frequent: "+closed_freq_itemsets);
	   System.out.println("Maximal Frequent: "+maximal_freq_itemsets);
	   
   }
   
   
}

class ItemsetObj{
	String name;
	int support_count;
	
	ItemsetObj(String name, int support){
		this.name = name;
		this.support_count = support;
	}
	
	ItemsetObj(String name){
		this.name = name;
	}
}
