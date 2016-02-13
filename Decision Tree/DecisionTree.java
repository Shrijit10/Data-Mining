import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class DecisionTree {
   static String curDir;
   static HashMap<Integer, HashMap<Integer, Float>> hashData;
   static int noFeatures;
   static int depth_limit = 10;
   
   public static void init(){
	   curDir = System.getProperty("user.dir");
	   hashData = new HashMap<Integer, HashMap<Integer, Float>>();
   }
   
   public static void sortFeatureLabel(List<FeatureLabel> listFeatLabel){
	   Collections.sort(listFeatLabel, new Comparator<FeatureLabel>(){
   		
   		@Override
   		public int compare(FeatureLabel l1, FeatureLabel l2){
   			if(l1.feature_val == l2.feature_val)
   				return 0;
   			else if(l1.feature_val < l2.feature_val)
   				    return -1;
   			else return 1;
   		}
   	});
   }
   
   public static int getClassLabel(int parentFeature, float st, float end){
	   int records = hashData.size();
	   int label0 = 0;
	   int label1 = 0;
	   
	   for(int i=1;i<=records;i++){
		   float val = hashData.get(i).get(parentFeature);
		   if(val >= st && val <= end){
			   if(hashData.get(i).get(noFeatures-1) == 0)
				   label0++;
			   else
				   label1++;
			}
	   }
	   
	   if(label0 >= label1)
		   return 0;
	   else
		   return 1;
   }
   
   public static void readDataset(String fileName, boolean hasHeader) throws IOException{
	   BufferedReader br = new BufferedReader(new FileReader(fileName));
	   String s="";
	   String[] temp = null;
	   int records = 0;
	   
	   if(hasHeader)
	      s = br.readLine(); // skipping header
	   
	   while((s=br.readLine())!=null){
		   records++;
		   HashMap<Integer, Float> hash = new HashMap<Integer, Float>();
 		   
		   //System.out.println("s: "+s);
		   temp = s.split(",");
		   for(int i=0;i<temp.length;i++){
			   hash.put(i, Float.parseFloat(temp[i]));
			}
		   
		   hashData.put(records, hash);
		   
		}
	   
	   noFeatures = temp.length;
	   br.close();
	}
   
   public static List<Float> getNodeGini(List<FeatureLabel> listFeatLabel, float split_val){
	   List<Integer> list1 = new ArrayList<Integer>(); // list label count for feature values less than split_pos
	   List<Integer> list2 = new ArrayList<Integer>(); // list label count for feature values greater than split_pos
	   List<Float> result = new ArrayList<Float>();
	   
	   list1.add(0);  // count for NO labels
	   list1.add(0);  // count for YES labels
	   
	   list2.add(0);  // count for NO labels
	   list2.add(0);  // count for YES labels
	   
	   //System.out.println("Split Val: "+split_val);
	   
	   for(FeatureLabel fl : listFeatLabel){
		   if(fl.feature_val <= split_val){
			  if(fl.label == 0)
				  list1.set(0, list1.get(0)+1);
			  else 	  
				  list1.set(1, list1.get(1)+1);
		    }
		   else{
			  if(fl.label == 0)
				 list2.set(0, list2.get(0)+1);
			  else 	  
				 list2.set(1, list2.get(1)+1);
		   }
	   }
	   
	   int totalNode1 = list1.get(0)+list1.get(1); 
	   int totalNode2 = list2.get(0)+list2.get(1);
	   
	   float giniNode1 = (float) (1 - Math.pow((double)list1.get(0)/totalNode1, 2) - Math.pow((double)list1.get(1)/totalNode1, 2));  
	   float giniNode2 = (float) (1 - Math.pow((double)list2.get(0)/totalNode2, 2) - Math.pow((double)list2.get(1)/totalNode2, 2));
	   
	   float gini = (float)totalNode1/(totalNode1 + totalNode2)*giniNode1 + (float)totalNode2/(totalNode1 + totalNode2)*giniNode2;
	   
	   result.add(gini);
	   result.add((float)totalNode1);
	   result.add((float)totalNode1);
	   
	   
	   return result;
   }
   
   public static List<Float> getGini(List<FeatureLabel> listFeatLabel){
	   List<Float> result = new ArrayList<Float>();
	   List<Float> list_node_gini = new ArrayList<Float>();
	   
	   int prevLabel = listFeatLabel.get(0).label;
	   float prevFeatureVal = listFeatLabel.get(0).feature_val;
	   int label1_count = 0;
	   int label0_count = 0;
	   float node_gini = 0.5f;
	   float best_split_value = Float.MIN_VALUE;
	   float min = Float.MAX_VALUE;
	   float split_val = Float.MIN_VALUE;
	   int split_val_index = -1;
	   
	   if(listFeatLabel.get(0).label == 0)
		   label0_count++;
	   else
		   label1_count++;
	   
	   for(int i=1;i<listFeatLabel.size();i++){
		   
		   if(listFeatLabel.get(i).label == 0)
			   label0_count++;
		   else
			   label1_count++;
		   
		   if(listFeatLabel.get(i).label != prevLabel){ //optimization to reduce no of comparisons to calculate gini
			 split_val = (listFeatLabel.get(i).feature_val + prevFeatureVal)/2;
			 list_node_gini = getNodeGini(listFeatLabel, split_val);   // gets gini for a node in the feature (<= 97 >)
			 
			 node_gini = list_node_gini.get(0);
			 
			 if(node_gini <= min){
			   min = node_gini;	
			   split_val_index = i-1;
			   best_split_value = split_val;
			 }
			  
		  }
		   
		   prevLabel = listFeatLabel.get(i).label;
		   prevFeatureVal = listFeatLabel.get(i).feature_val;
	   }
	   
	   result.add(min);
	   result.add(best_split_value);
	   result.add((float)split_val_index);
	   
	   if(split_val_index == -1){
		   result.set(0, 0f);
		   if(label0_count > label1_count)
			   result.add(0f);
		   else
			   result.add(1f);
	   }
	   else
		   result.add(-1f);
	   
	   return result;
	   
   }
   
   public static List<Float> getBestSplit(HashMap<Integer, HashMap<Integer, Float>> hashData, int parentFeature, 
		                                  float st, float end, Set<Integer> setVisited){
	   List<Float> result = new ArrayList<Float>();
	   List<Float> temp_result = new ArrayList<Float>();
	   int records = hashData.size();
	   int split_val_index = -1;
	   int feature_index = 0;   // class label for parent node remaining
	   float min = Integer.MAX_VALUE;
	   float gini = 0.5f;
	   float split_value = Float.MIN_VALUE;
	   float class_label = -1f;
	   float feature_left_st = Float.MIN_VALUE;
	   float feature_left_end = Float.MAX_VALUE;
	   float feature_right_st = Float.MIN_VALUE;
	   float feature_right_end = Float.MAX_VALUE;
	   boolean flag = false;
	   
	   for(int i=0;i<noFeatures-1;i++){
	      if(!setVisited.contains(i)){
	    	flag = true;  
	    	List<FeatureLabel> listFeatLabel = new ArrayList<FeatureLabel>();
		    
	    	for(int j=1;j<=records;j++){
	    	   if(st==Float.MIN_VALUE && end == Float.MAX_VALUE){
	    		  int label = Math.round(hashData.get(j).get(noFeatures-1)); 
	    		  listFeatLabel.add(new FeatureLabel(hashData.get(j).get(i), label));
	    	   }
	    	   else{
	    		   float parentFeatVal = hashData.get(j).get(parentFeature);
	    		   
	    		   if(parentFeatVal >= st && parentFeatVal <= end){
	    			  int label = Math.round(hashData.get(j).get(noFeatures-1)); 
	 	    		  listFeatLabel.add(new FeatureLabel(hashData.get(j).get(i), label));
	    		   }
	    	   }
	         }
	    	
	    	sortFeatureLabel(listFeatLabel);
	    	
	    	temp_result = getGini(listFeatLabel);   // gets gini for the given feature
	    	gini = temp_result.get(0);
	    	
	    	if(gini <= min){
	    	   min = gini;
	    	   feature_index = i;
	    	   split_value = temp_result.get(1);
	    	   split_val_index = Math.round(temp_result.get(2));
	    	   
	    	   feature_left_st = listFeatLabel.get(0).feature_val;
	    	   
	    	   if(split_val_index != -1){
	    		   feature_left_end = listFeatLabel.get(split_val_index).feature_val;
	    		   
	    		   if(split_val_index < listFeatLabel.size()-1){
	    			   feature_right_st = listFeatLabel.get(split_val_index+1).feature_val;
	    			   feature_right_end = listFeatLabel.get(listFeatLabel.size()-1).feature_val;
	    		   }
	    		}
	    		else{
	    		   feature_left_end = listFeatLabel.get(listFeatLabel.size()-1).feature_val;
	    		   feature_right_st = listFeatLabel.get(listFeatLabel.size()-1).feature_val;
	    		   feature_right_end = listFeatLabel.get(listFeatLabel.size()-1).feature_val;
	    		}
	    	   
	    	   
	    	   split_val_index = Math.round(temp_result.get(2));
	    	   class_label = temp_result.get(3);  // parent class label
	    	}
	    	
	      }
	   
		}
	   
	   //System.out.println("Visited Index: "+feature_index+", Split Val: "+split_value+", Class: "+class_label);
	   if(flag){
	     setVisited.add(feature_index);
	   
	     result.add((float)feature_index);
	     result.add(split_value);
	     result.add(class_label);
	     result.add(feature_left_st);
	     result.add(feature_left_end);
	     result.add(feature_right_st);
	     result.add(feature_right_end);
	   }
	   else{
		  result.add((float)parentFeature);
		  result.add(split_value);
		  
		  class_label = getClassLabel(parentFeature, st, end);
		  result.add(class_label);
		  result.add(feature_left_st);
		  result.add(feature_left_end);
		  result.add(feature_right_st);
		  result.add(feature_right_end);
	   }
		   
	   return result;
   }
   
   public static Node decisionTree(HashMap<Integer, HashMap<Integer, Float>> hashData, Set<Integer> setVisited,
		                           int parentFeature, float st, float end, Node node, Node parent, int depth){
	   
	   if(depth > depth_limit){
		  node.label = parent.label;
		  return node;
	   }
	   else{
	      Node new_node = new Node();
		  List<Float> list = getBestSplit(hashData, parentFeature, st, end, setVisited);
		  
		  if(list.get(2) == 0 || list.get(2) == 1){
			  new_node.label = Math.round(list.get(2)); // assign label based on st and end
		      return new_node;
		  }
		  else{
		    node.featureIndex = Math.round(list.get(0)); // child feature index
		    node.featureValue = list.get(1); // child feature value
		    node.label = Math.round(list.get(2));
		    
		    parentFeature = Math.round(list.get(0));
		    depth++;
		    
		    node.left = decisionTree(hashData, setVisited, parentFeature, list.get(3), list.get(4), new_node, node, depth);
		    node.right = decisionTree(hashData, setVisited, parentFeature, list.get(5), list.get(6), new_node, node, depth);
		    setVisited.remove(node.featureIndex);
		     
		    return node;
		  }
	    }
   }
   
   public static void displayAllRecords(){
	   Set<Integer> set = hashData.keySet();
	   Iterator<Integer> it = set.iterator();
	   
	   while(it.hasNext()){
		 Integer key = it.next();  
		 System.out.println("Outer key: "+key);
		 
	     HashMap<Integer, Float> hash = hashData.get(key);
	     
	     Set<Integer> set1 = hash.keySet();
	     Iterator<Integer> it1 = set1.iterator();
	     
	     
	     while(it1.hasNext()){
	    	 Integer key1 = it1.next();
	    	 System.out.println("Inner key: "+key1+", Value: "+hash.get(key1));
	     }
	   }
   }
   
   public static void displayTree(Node node){
	   if(node == null)
		   return;
	   else{
		   System.out.println("Class: "+node.label+", Index: "+node.featureIndex+", Split Value: "+node.featureValue);
		   displayTree(node.left);
		   displayTree(node.right);
	   }
   }
	
   public static void main(String[] args) throws IOException{
	   init();
	   String fileName = curDir+"\\demo_data1.csv";
	   readDataset(fileName, false);
	   
	   Set<Integer> setVisited = new HashSet<Integer>();
	   Node node = decisionTree(hashData, setVisited, 0, Float.MIN_VALUE, Float.MAX_VALUE, new Node(), new Node(), 0);
	   
	   displayTree(node);
	   
   }
}

class Node{
	int featureIndex;
	float featureValue;
	int label;
	Node left;
	Node right;
	
	Node(){
		featureIndex = -1;
		featureValue = Float.MIN_VALUE;
	}
	
	Node(int index, float value){
		this.featureIndex = index;
		this.featureValue = value;
	}
}

class FeatureLabel{
	float feature_val;
	int label;
	
	FeatureLabel(float feature_val, int label){
		this.feature_val = feature_val;
		this.label = label;
	}
}
