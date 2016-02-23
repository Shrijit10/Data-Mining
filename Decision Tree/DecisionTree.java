import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class DecisionTree {
   static String curDir;
   static HashMap<Integer, HashMap<Integer, String>> hashData;
   static Set<String> setLabels;
   static int noFeatures;
   static int depth_limit = 10;
   static Node root;
   static String criteria;
   
   public static void init(String measure){
	   curDir = System.getProperty("user.dir");
	   hashData = new HashMap<Integer, HashMap<Integer, String>>();
	   setLabels = new HashSet<String>();
	   root = new Node();	   
	   criteria = measure;
   }
   
   public static LinkedHashSet<Integer> getVisitedFeatures(Set<Integer> setVisited, int parentFeature){
	   Iterator<Integer> it = setVisited.iterator();
	   LinkedHashSet<Integer> result = new LinkedHashSet<Integer>();
	   
	   while(it.hasNext()){
		   int feature = it.next();
		   
		   if(feature != parentFeature)
		      result.add(feature);
		   else{
			   result.add(feature);
			   break;
		   }
	   }
	   
	   return result;
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
   
   /*public static String predictClass(Node node, String[] temp){
	   String predict_class = node.label; 
	   
	   if(!predict_class.equals("-1"))
	    	return predict_class;
	   
	   int feature_index = node.featureIndex;
	   float test_feature_val = Float.parseFloat(temp[feature_index]);
	   float train_feature_val = node.featureValue;
	    
	   if(test_feature_val <= train_feature_val)
	      return predictClass(node.left, temp);
	   else
	      return predictClass(node.right, temp);
	    
	}*/
   
   public static List<String> getClassLabel(int parentFeature, float st, float end, Set<Integer> setValidRecords){
	   HashMap<String, Integer> hashLabel = new HashMap<String, Integer>(); // list label count for feature values less than split_pos
	   int records = hashData.size();
	   int count = 0;
	   
	   for(String s : setLabels)
		   hashLabel.put(s, 0);
	   
	   for(int i=1;i<=records;i++){
		   float val = Float.parseFloat(hashData.get(i).get(parentFeature));
		   if(val >= st && val <= end && setValidRecords.contains(i)){
			   hashLabel.put(hashData.get(i).get(noFeatures-1)+"", hashLabel.get(hashData.get(i).get(noFeatures-1))+1);
		       count++;
		   }
	   }

	   int max_val = Integer.MIN_VALUE;
	   String max_key = "";
	   List<String> result = new ArrayList<String>();
	   
	   for(Entry<String, Integer> e : hashLabel.entrySet()){
		   if(e.getValue() > max_val){
			   max_val = e.getValue();
			   max_key = e.getKey();
		   }
	   }
	   
	   result.add(max_key);
	   result.add(count+"");
	   result.add(max_val+"");
	   return result;
   }
   
   public static List<Set<Integer>> getValidRecords(List<String> list){
	   Set<Integer> setLeft = new HashSet<Integer>();
	   Set<Integer> setRight = new HashSet<Integer>();
	   List<Set<Integer>> result = new ArrayList<Set<Integer>>();
	   
	   int i=0;
	   for(i=0;i<list.size();i++){
		  if(list.get(i).equals(""))
			 break;   
		  else
			 setLeft.add(Integer.parseInt(list.get(i)));
	   }
	   
	   for(i=i+1;i<list.size();i++){
		   setRight.add(Integer.parseInt(list.get(i)));
	   }
	   
	   result.add(setLeft);
	   result.add(setRight);
	   
	   return result;
	   
   }
   
   /*public static void evalTestFile(String file_name) throws Exception{
	   BufferedReader br = new BufferedReader(new FileReader(file_name));
	   String s="";
	   String[] temp = null;
	   String predict_class = "";
	   int correct = 0;
	   int total = 0;
	   
	   while((s=br.readLine())!=null){
		   total++;
		   temp = s.split(",");
		   predict_class = predictClass(root, temp);
		   
		   String actual_class = temp[temp.length-1];
		   if(predict_class.equals(actual_class))
			   correct++;
		}
	   
	   System.out.println("Accuracy: "+ (float)correct/total);
	   System.out.println("Total: "+total+", Correct: "+correct);
	   
	   br.close();
	   
   }*/
   
   public static void readDataset(String fileName, boolean hasHeader) throws IOException{
	   BufferedReader br = new BufferedReader(new FileReader(fileName));
	   String s="";
	   String[] temp = null;
	   int records = 0;
	   
	   if(hasHeader)
	      s = br.readLine(); // skipping header
	   
	   while((s=br.readLine())!=null){
		   records++;
		   HashMap<Integer, String> hash = new HashMap<Integer, String>();
 		   
		   //System.out.println("s: "+s);
		   temp = s.split(",");
		   for(int i=0;i<temp.length;i++){
			   hash.put(i, temp[i]);
			}
		   
		   hashData.put(records, hash);
		   if(!setLabels.contains(temp[temp.length-1])){
			   setLabels.add(temp[temp.length-1]);
		   }
		   
		   noFeatures = temp.length;
		   
		}
	   
	   br.close();
	}
   
   public static float computeGini(HashMap<String, Integer> hash1, HashMap<String, Integer> hash2){
	   int totalNode1 = 0;
	   int totalNode2 = 0;
	   float giniNode1 = 0f;
	   float giniNode2 = 0f;
	   
	   for(String s : setLabels){
		  totalNode1+=hash1.get(s);
		  totalNode2+=hash2.get(s);
	   }
	   
	   for(String s : setLabels){
		   giniNode1 += Math.pow((double)hash1.get(s)/totalNode1, 2);
		   giniNode2 += Math.pow((double)hash2.get(s)/totalNode2, 2);
       }
	   
	   giniNode1 = 1 - giniNode1;
	   giniNode2 = 1 - giniNode2;
	   
	   float gini = (float)totalNode1/(totalNode1 + totalNode2)*giniNode1 + (float)totalNode2/(totalNode1 + totalNode2)*giniNode2;
       return gini;
   }
   
   public static float getBestMeasure(List<FeatureLabel> listFeatLabel, float split_val, String criteria){
	   HashMap<String, Integer> hash1 = new HashMap<String, Integer>(); // list label count for feature values less than split_pos
	   HashMap<String, Integer> hash2 = new HashMap<String, Integer>(); // list label count for feature values greater than split_pos
	   float result = 0f;
	   
	   for(String s : setLabels){
		   hash1.put(s, 0);
		   hash2.put(s, 0);
	   }
	   
	   //System.out.println("Split Val: "+split_val);
	   
	   for(FeatureLabel fl : listFeatLabel){
		  if(fl.feature_val <= split_val)
			 hash1.put(fl.label, hash1.get(fl.label)+1);
		  else
			 hash2.put(fl.label, hash2.get(fl.label)+1);
	   }
	   
	   if(criteria.equals("gini"))
	      result = computeGini(hash1, hash2);
	   else if(criteria.equals("info_gain"))
		  result = computeInfoGain(hash1, hash2);
	   
	   return result;
   }
   
   public static float computeInfoGain(HashMap<String, Integer> hash1, HashMap<String, Integer> hash2){
	   float info_gain = 0f;
	   float entropy1 = 0f;
	   float entropy2 = 0f;
	   float parent_entropy = 0f;
	   int totalNode1 = 0;
	   int totalNode2 = 0;
	   float prob1 = 0f;
	   float prob2 = 0f;
	   float prob = 0f;
	   
       HashMap<String, Integer> hashParent = new HashMap<String, Integer>(); 
	   
	   for(Entry<String, Integer> e : hash1.entrySet()){
		   if(hashParent.containsKey(e.getKey()))
			   hashParent.put(e.getKey(), hashParent.get(e.getKey())+e.getValue());
		   else
			   hashParent.put(e.getKey(), e.getValue());
	   }
	   
	   for(Entry<String, Integer> e : hash2.entrySet()){
		   if(hashParent.containsKey(e.getKey()))
			   hashParent.put(e.getKey(), hashParent.get(e.getKey())+e.getValue());
		   else
			   hashParent.put(e.getKey(), e.getValue());
	   }
	   
	   for(String s : setLabels){
		   totalNode1+=hash1.get(s);
		   totalNode2+=hash2.get(s);
	   }
	   
	   for(String s : setLabels){
		   prob1 = (float)hash1.get(s)/totalNode1;
		   prob2 = (float)hash2.get(s)/totalNode2;
		   prob = (float)hashParent.get(s)/(totalNode1 + totalNode2);
		   
		   prob1 = prob1 == 0 ? 1:prob1;
		   prob2 = prob2 == 0 ? 1:prob2;
		   prob  = prob  == 0 ? 1:prob;
		   
		   entropy1 += prob1 * Math.log(prob1)/Math.log(2);
		   entropy2 += prob2 * Math.log(prob2)/Math.log(2);
		   parent_entropy += prob * Math.log(prob)/Math.log(2);
       }
	   
	   float total_child_entropy = entropy1*totalNode1/(totalNode1 + totalNode2) + 
			                       entropy2*totalNode2/(totalNode1 + totalNode2);
	   
	   info_gain = -1*parent_entropy - total_child_entropy;
	   return info_gain;
	}
   
   public static List<String> getGini(List<FeatureLabel> listFeatLabel){
	   List<String> result = new ArrayList<String>();
	   float measure = 0f;
	   HashMap<String, Integer> hashLabel = new HashMap<String, Integer>(); // list label count for feature values less than split_pos
	   
	   String prevLabel = listFeatLabel.get(0).label;
	   float prevFeatureVal = listFeatLabel.get(0).feature_val;
	   float node_gini = 1f;
	   float node_gain = 0f;
	   float best_split_value = Float.MIN_VALUE;
	   float min = Float.MAX_VALUE;
	   float max = Float.MIN_VALUE;
	   float split_val = Float.MIN_VALUE;
	   int split_val_index = -1;
	   
	   for(String s : setLabels)
		  hashLabel.put(s, 0);
	   
	   hashLabel.put(listFeatLabel.get(0).label, hashLabel.get(listFeatLabel.get(0).label)+1);
	   
	   for(int i=1;i<listFeatLabel.size();i++){
		   hashLabel.put(listFeatLabel.get(i).label, hashLabel.get(listFeatLabel.get(i).label)+1);
		   
		   if(!listFeatLabel.get(i).label.equals(prevLabel)){ //optimization to reduce no of comparisons to calculate gini
			  split_val = (listFeatLabel.get(i).feature_val + prevFeatureVal)/2;
			  measure = getBestMeasure(listFeatLabel, split_val, criteria);   // gets gini for a node in the feature (<= 97 >)
			 
			  if(criteria.equals("gini")){
			    node_gini = measure;
			    
			    if(node_gini < min){
				   min = node_gini;	
				   split_val_index = i-1;
				   best_split_value = split_val;
				}
			  }
			  else if(criteria.equals("info_gain")){
				node_gain = measure;
				
				if(node_gain > max){
				   max = node_gain;	
				   split_val_index = i-1;
				   best_split_value = split_val;
			    }
			  }
		   }
		   
		   prevLabel = listFeatLabel.get(i).label;
		   prevFeatureVal = listFeatLabel.get(i).feature_val;
	   }
	   
	   if(criteria.equals("gini"))
	      result.add(min+"");
	   else if(criteria.equals("info_gain"))
		  result.add(max+"");
	   
	   result.add(best_split_value+"");
	   result.add(split_val_index+"");
	   
	   if(split_val_index == -1){
		   result.set(0, "0");
		   
		   int max_val = Integer.MIN_VALUE;
		   String max_key = "";
		   for(Entry<String, Integer> e : hashLabel.entrySet()){
			   if(e.getValue() > max_val){
				   max_val = e.getValue();
				   max_key = e.getKey();
			   }
		   }
		   
		   result.add(max_key);
	   }
	   else
		   result.add("-1");
	   
	   return result;
	   
   }
   
   public static NodeDetails getBestSplit(HashMap<Integer, HashMap<Integer, String>> hashData, int parentFeature, 
		                                  Set<Integer> setValidRecords, float st, float end, Set<Integer> setVisited, int depth){
	   //List<String> result = new ArrayList<String>();
	   List<String> temp_result = new ArrayList<String>();
	   List<String> list_left_record_no = new ArrayList<String>();
	   List<String> list_right_record_no = new ArrayList<String>();
	   int records = hashData.size();
	   int split_val_index = -1;
	   int feature_index = 0;   // class label for parent node remaining
	   float min = Integer.MAX_VALUE;
	   float gini = 1f;
	   float split_value = Float.MIN_VALUE;
	   String class_label = "-1";
	   float feature_left_st = Float.MIN_VALUE;
	   float feature_left_end = Float.MAX_VALUE;
	   float feature_right_st = Float.MIN_VALUE;
	   float feature_right_end = Float.MAX_VALUE;
	   boolean flag = false;
	   
	   //System.out.println("In bestsplit");
	   
	   for(int i=0;i<noFeatures-1;i++){
	      if(!setVisited.contains(i)){
	    	flag = true;  
	    	List<FeatureLabel> listFeatLabel = new ArrayList<FeatureLabel>();
	    	
	    	for(int j=1;j<=records;j++){
	    	   if(st==Float.MIN_VALUE && end == Float.MAX_VALUE){
	    		  String label = hashData.get(j).get(noFeatures-1)+""; 
	    		  listFeatLabel.add(new FeatureLabel(j, Float.parseFloat(hashData.get(j).get(i)), label));
	    	   }
	    	   else{
	    		   float parentFeatVal = Float.parseFloat(hashData.get(j).get(parentFeature));
	    		   
	    		   if(parentFeatVal >= st && parentFeatVal <= end && setValidRecords.contains(j)){
	    			  String label = hashData.get(j).get(noFeatures-1)+""; 
	 	    		  listFeatLabel.add(new FeatureLabel(j, Float.parseFloat(hashData.get(j).get(i)), label));
	    		   }
	    	   }
	        }
	    	
	    	sortFeatureLabel(listFeatLabel);
	    	
	    	temp_result = getGini(listFeatLabel);   // gets gini for the given feature
	    	gini = Float.parseFloat(temp_result.get(0));
	    	
	    	if(gini < min){
	    	   min = gini;
	    	   feature_index = i;
	    	   split_value = Float.parseFloat(temp_result.get(1));
	    	   split_val_index = Integer.parseInt(temp_result.get(2));
	    	   
	    	   feature_left_st = listFeatLabel.get(0).feature_val;
	    	   
	    	   if(split_val_index != -1){
	    		   feature_left_end = listFeatLabel.get(split_val_index).feature_val;
	    		   
	    		   if(split_val_index < listFeatLabel.size()-1){
	    			   feature_right_st = listFeatLabel.get(split_val_index+1).feature_val;
	    			   feature_right_end = listFeatLabel.get(listFeatLabel.size()-1).feature_val;
	    		   }
	    		   
	    		   list_left_record_no.clear();
	    		   list_right_record_no.clear();
	    		   
	    		   for(FeatureLabel fl : listFeatLabel){
	    			   if(fl.feature_val <= split_value)
	    			      list_left_record_no.add(fl.record_no+"");
	    			   else
	    				  list_right_record_no.add(fl.record_no+"");
	    			}
	    		}
	    		else{
	    		   feature_left_end = listFeatLabel.get(listFeatLabel.size()-1).feature_val;
	    		   feature_right_st = listFeatLabel.get(listFeatLabel.size()-1).feature_val;
	    		   feature_right_end = listFeatLabel.get(listFeatLabel.size()-1).feature_val;
	    		}
	    	   
	    	   split_val_index = Integer.parseInt(temp_result.get(2));
	    	   class_label = temp_result.get(3);  // parent class label
	    	   
	    	   
	    	}
	    	
	      }
	   
		}
	   
	   //System.out.println("Visited Index: "+feature_index+", Split Val: "+split_value+", Class: "+class_label);
	   List<String> list_class_dtls = new ArrayList<String>();
	   //int class_count = 0;
	   
	   if(flag){
	     if(depth > depth_limit){
	    	 list_class_dtls = getClassLabel(parentFeature, st, end, setValidRecords);
	    	 class_label = list_class_dtls.get(0);
	    	 //class_count = Integer.parseInt(list_class_dtls.get(1));
	      }
	   }
	   else{
		  list_class_dtls =  getClassLabel(parentFeature, st, end, setValidRecords);
		  class_label = list_class_dtls.get(0);
	      //class_count = Integer.parseInt(list_class_dtls.get(1));
	   }
	   
	   int temp_parent_feature = 0;
	   if(parentFeature == -1)
		  temp_parent_feature = feature_index;
	   else
		   temp_parent_feature = parentFeature;
	   
	   list_class_dtls = getClassLabel(temp_parent_feature, st, end, setValidRecords);
	   
	   String max_class_label = list_class_dtls.get(0);
	   int max_class_count = Integer.parseInt(list_class_dtls.get(1));
	   int label_count = Integer.parseInt(list_class_dtls.get(2));
	   
	   List<String> list_record_no = new ArrayList<String>();
	   list_record_no.addAll(list_left_record_no);
	   list_record_no.add("");
	   list_record_no.addAll(list_right_record_no);
	   
	   NodeDetails node_details = new NodeDetails(feature_index, split_value, class_label, feature_left_st,
                                                  feature_left_end, feature_right_st, feature_right_end, 
                                                  list_record_no, max_class_label, max_class_count, label_count);
		   
	   return node_details;
   }
   
   public static Node decisionTree(HashMap<Integer, HashMap<Integer, String>> hashData, Set<Integer> setVisited,
		                           int parentFeature,Set<Integer> setValidRecords, float st, float end, Node node, int depth) throws IOException{
	   
	   Node new_node = new Node();
	   NodeDetails nd = getBestSplit(hashData, parentFeature, setValidRecords, st, end, setVisited, depth);
  	   List<Set<Integer>> list_sets = getValidRecords(nd.list_record_no);
	   Set<Integer> setLeftValidRecords =  list_sets.get(0);
	   Set<Integer> setRightValidRecords = list_sets.get(1);
		  
	   if(setLabels.contains(nd.class_label) || depth > depth_limit){
	       new_node.label = nd.class_label;  // assign label based on st and end
		   new_node.max_class_label = nd.max_class_label;
		   new_node.max_class_count = nd.max_class_count;
		   new_node.label_count = nd.label_count;
	       return new_node;
		}
	   else{
			int feature_index = nd.feature_index; 
		    node.featureIndex = feature_index; // child feature index
		    node.featureValue = nd.split_value;  // child feature value
		    node.label = nd.class_label; 
		    node.st = nd.feature_left_st;
		    node.end = nd.feature_right_end;
		    node.max_class_label = nd.max_class_label;
		    node.max_class_count = nd.max_class_count;
		    node.label_count = nd.label_count;
		    
		    setVisited.add(feature_index);
		    depth++;
		    
		    //System.out.println("****************************************");
		    //System.out.println("Before Left: Parent: "+parentFeature);
		    //displayTree(node, null);
		    
		    node.left = decisionTree(hashData, setVisited, feature_index, setLeftValidRecords, nd.feature_left_st, 
		    		                 nd.feature_left_end, new_node, depth);
		    
		    setVisited = getVisitedFeatures(setVisited, node.featureIndex);
		    
		    //System.out.println("****************************************");
		    //System.out.println("After Left: Parent: "+parentFeature);
		    //displayTree(node, null);
		    
		    new_node = new Node();
		    node.right = decisionTree(hashData, setVisited, feature_index, setRightValidRecords, nd.feature_right_st/*Float.parseFloat(list.get(5))*/, 
		    		                 nd.feature_right_end/*Float.parseFloat(list.get(6))*/, new_node, depth);
		     
		    //System.out.println("****************************************");
		    //System.out.println("After Right: Parent: "+parentFeature);
		    //displayTree(node, null);
		    
		    return node;
		  }
   }
   
   /*public static void displaySet(Set<Integer> set){
	   for(int i : set)
		  System.out.print(i+", ");
	   System.out.println();
   }*/
   
   /*public static void displayAllRecords(){
	   Set<Integer> set = hashData.keySet();
	   Iterator<Integer> it = set.iterator();
	   
	   while(it.hasNext()){
		 Integer key = it.next();  
		 System.out.println("Outer key: "+key);
		 
	     HashMap<Integer, String> hash = hashData.get(key);
	     
	     Set<Integer> set1 = hash.keySet();
	     Iterator<Integer> it1 = set1.iterator();
	     
	     
	     while(it1.hasNext()){
	    	 Integer key1 = it1.next();
	    	 System.out.println("Inner key: "+key1+", Value: "+hash.get(key1));
	     }
	   }
   }*/
   
   /*public static void displaySetVisited(Set<Integer> setVisited){
	   Iterator<Integer> it = setVisited.iterator();
	   
	   while(it.hasNext()){
		   System.out.println("Feature: "+it.next());
	   }
   }*/
   
   public static void displayTree(Node node, FileWriter fw) throws IOException{
	   if(node == null)
		   return;
	   else{
		   if(fw!=null)
		     fw.write("Class: "+node.label+", Index: "+node.featureIndex+", Split Value: "+node.featureValue+"\n");
		   
		   System.out.println("Class: "+node.label+", Index: "+node.featureIndex+", Split Value: "+node.featureValue+
				              ", Max Class Label: "+node.max_class_label+", Max Count: "+node.max_class_count+
				              ", Label Count: "+node.label_count);
		   displayTree(node.left, fw);
		   displayTree(node.right, fw);
	   }
   }
	
   /*public static void main(String[] args) throws Exception{
	   BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	   System.out.println("Please enter 1 for Gini, 2 for Info Gain measure: ");
	   
	   String criteria = br.readLine();
	   if(!criteria.equals("1") && !criteria.equals("2")){
		   System.out.println("Invalid option...Aborting...");
		   return;
	   }
	   
	   if(criteria.equals("1"))
	      criteria = "gini";
	   else
		  criteria = "info_gain";
	   
	   init(criteria);
	   String fileName = curDir+"\\iris.csv";
	   readDataset(fileName, false);
	   
	   Set<Integer> setVisited = new LinkedHashSet<Integer>();
	   Set<Integer> setValidRecords = new HashSet<Integer>();
	   
	   for(int i=1;i<=hashData.size();i++){
		   setValidRecords.add(i);
	   }
	   
	   System.out.println("Decision Tree");
	   root = decisionTree(hashData, setVisited, -1, setValidRecords, Float.MIN_VALUE, Float.MAX_VALUE, root, 0);
	   
	   FileWriter fw = new FileWriter("C:\\Users\\Shrijit\\Desktop\\output.txt");
	   displayTree(root, fw);
	   fw.close();
	   
	   //String test_file = "test6.csv";
	   //String path = curDir+"\\"+test_file;
	   //evalTestFile(path);
   }*/
}

class Node{
	int featureIndex;
	float featureValue;
	String label;
	Node left;
	Node right;
	float st;
    float end;
    List<String> list_record_no;
    String max_class_label;
    int max_class_count;
    int label_count;
    
	Node(){
		featureIndex = -1;
		featureValue = Float.MIN_VALUE;
	}
	
	Node(int index, float value, Node left, Node right, String label){
		this.featureIndex = index;
		this.featureValue = value;
		this.left = left;
		this.right = right;
		this.label = label;
	}
	
	Node(int index, float value){
		this.featureIndex = index;
		this.featureValue = value;
	}
	
	Node(Node n){
		this(n.featureIndex, n.featureValue, n.left, n.right, n.label);
	}
}

class FeatureLabel{
	int record_no;
	float feature_val;
	String label;
	
	FeatureLabel(int record_no, float feature_val, String label){
		this.record_no = record_no;
		this.feature_val = feature_val;
		this.label = label;
	}
}

class NodeDetails{
	int feature_index;
	float split_value;
    String class_label;
    float feature_left_st;
    float feature_left_end;
    float feature_right_st;
    float feature_right_end;
    List<String> list_record_no;
    String max_class_label;
    int max_class_count;
    int label_count;
    
    NodeDetails(int index, float split_value, String label, float left_st, float left_end,
    		    float right_st, float right_end, List<String> list_record_no, String max_class_label,
    		    int max_class_count, int label_count){
    	feature_index = index;
    	this.split_value = split_value;
    	class_label = label;
    	feature_left_st = left_st;
    	feature_left_end = left_end;
    	feature_right_st = right_st;
    	feature_right_end = right_end;
    	this.list_record_no = list_record_no;
    	this.max_class_label = max_class_label;
    	this.max_class_count = max_class_count;
    	this.label_count = label_count;
    }
}
