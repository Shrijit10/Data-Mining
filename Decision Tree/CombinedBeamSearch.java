import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;


public class CombinedBeamSearch {
	static List<NodeDetails> listNodeDetails;
	static int m;
	
	public static void init(String criteria, int noTrees){
		m = noTrees;
		listNodeDetails = new ArrayList<NodeDetails>();
		DecisionTree.init(criteria);
		
		for(int i=0;i<m;i++){
		   listNodeDetails.add(new NodeDetails());
		}
	}
	
	public static float getOverallGini(BeamNode node){
		if(node == null || node.sd == null)
			return 0;
		
		return node.sd.gini + getOverallGini(node.left) + getOverallGini(node.right);
	}
	
	public static void compareTrees(List<NodeDetails> list_node_details){
		for(NodeDetails node : list_node_details){
			BeamNode beam_node = node.beam_node;
			
			float total_gini = getOverallGini(beam_node);
			node.total_gini = total_gini;
		}
		
		Collections.sort(list_node_details, new Comparator<NodeDetails>(){
			@Override
			public int compare(NodeDetails n1, NodeDetails n2){
				if(n1.total_gini == n2.total_gini)
					return 0;
				else if(n1.total_gini < n2.total_gini)
					   return -1;
				else return 1;
			}
		});
	}
	
	public static void populateListResult(int index, List<String> split_result, List<FeatureLabel> listFeatLabel,
			                              List<String> result, int parentFeature, float st, float end, 
			                              Set<Integer> setValidRecords, int depth, List<SplitDetails> list_result){
	   float split_value = Float.MIN_VALUE;
	   String class_label = "-1";
	   float feature_left_st = Float.MIN_VALUE;
       float feature_left_end = Float.MAX_VALUE;
	   float feature_right_st = Float.MIN_VALUE;
	   float feature_right_end = Float.MAX_VALUE;
	   List<String> list_left_record_no = new ArrayList<String>();
	   List<String> list_right_record_no = new ArrayList<String>();
	   float gini = 1f;
	   
	   int feature_index = index;
 	   int split_val_index = Integer.parseInt(split_result.get(2));
 	   
 	   split_value = Float.parseFloat(split_result.get(1));
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
 	   
 	   split_val_index = Integer.parseInt(split_result.get(2));
 	   class_label = split_result.get(3);  // parent class label
 	   gini = Float.parseFloat(split_result.get(0));
	     
	   if(depth > DecisionTree.depth_limit){
	      class_label = DecisionTree.getClassLabel(parentFeature, st, end, setValidRecords);
	      result.set(2, class_label);
	   }
	   
	   SplitDetails sd = new SplitDetails(gini,
							              feature_index,
							              split_value,
							              class_label,
							              feature_left_st,
							              feature_left_end,
							              feature_right_st,
						                  feature_right_end,
						                  list_left_record_no,
						                  list_right_record_no);
	   
	   if(list_result.size() == m)
	     list_result.set(m-1, sd);
	   else
		 list_result.add(sd);
	}
	
	public static void sortListResult(List<SplitDetails> list_result){
		Collections.sort(list_result, new Comparator<SplitDetails>(){
			
			@Override
			public int compare(SplitDetails sd1, SplitDetails sd2){
				if(sd1.gini == sd2.gini)
					return 0;
				else if(sd1.gini < sd2.gini)
					   return -1;
				else return 1;
			} 
		});
	}
	
	public static TargetBeamNode searchTargetNode(BeamNode beam_node){
		   Queue<BeamNode> queue = new LinkedList<BeamNode>();
		   Set<String> setLabels = DecisionTree.setLabels;
		   
		   if(beam_node.sd == null){
			   return new TargetBeamNode(beam_node, -1);
		   }
		   
		   queue.add(beam_node);
		   
		   while(!queue.isEmpty()){
			  BeamNode node = queue.remove();
			   
			  if(!setLabels.contains(node.sd.class_label)){
				  if(node.left == null)
					 return new TargetBeamNode(node, 0);
				  
				  if(node.right == null)
				     return new TargetBeamNode(node, 1);
				  
				  queue.add(beam_node.left);
				  queue.add(beam_node.right);
			   }
			}
		   
		   return null;
		}
	
	public static boolean attachToTarget(BeamNode node, BeamNode target, BeamNode new_node, int direction){
		if(node == null)
			return false;
		
		if(node.hashCode() == target.hashCode()){
		   if(direction == 0)
			  node.left = new_node;
		   else if(direction == 1)	
		      node.right = new_node;
		
		   return true;
		 }
		else{
			boolean b1 = attachToTarget(node.left, target, new_node, direction);
			boolean b2 = attachToTarget(node.right, target, new_node, direction);
		
		    return b1 || b2;
		}
	}
	
	public static List<SplitDetails> getBestSplit(HashMap<Integer, HashMap<Integer, String>> hashData, int parentFeature, 
                                            Set<Integer> setValidRecords, float st, float end, Set<Integer> setVisited, 
                                            int depth){
	   
	   List<SplitDetails> list_result = new ArrayList<SplitDetails>();
	   List<String> result = new ArrayList<String>();
	   
	   List<Float> list_gini = new ArrayList<Float>();
	   List<String> temp_result = new ArrayList<String>();
	   List<String> list_left_record_no = new ArrayList<String>();
	   List<String> list_right_record_no = new ArrayList<String>();
	   int records = DecisionTree.hashData.size();
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
	   int noFeatures = DecisionTree.noFeatures;
	   int m = CombinedBeamSearch.m;
		   
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
	    	
	    	DecisionTree.sortFeatureLabel(listFeatLabel);
	    	
	    	temp_result = DecisionTree.getGini(listFeatLabel);   // gets gini for the given feature
	    	gini = Float.parseFloat(temp_result.get(0));
	    	
	    	if(list_result.size() < m){
	    		populateListResult(i, temp_result, listFeatLabel, result, parentFeature, st, end, setValidRecords, depth, list_result);
	    		sortListResult(list_result);
	    	}
	    	else{
	    	   if(gini < list_result.get(m-1).gini){
	    		   populateListResult(i, temp_result, listFeatLabel, result, parentFeature, st, end, setValidRecords, depth, list_result);
	    		   sortListResult(list_result);
	    	   }
	    	}
	    	
	      }
	   
		}
		   
	   //System.out.println("Visited Index: "+feature_index+", Split Val: "+split_value+", Class: "+class_label);
	   if(!flag){
		  class_label = DecisionTree.getClassLabel(parentFeature, st, end, setValidRecords);
		  
		  SplitDetails sd = new SplitDetails(1f, parentFeature, split_value,         class_label, 
				                             feature_left_st,   feature_left_end,    feature_right_st, 
				                             feature_right_end, list_left_record_no, list_right_record_no);
		  
		  list_result.add(sd);
	   }
		   
	   return list_result;
	}
	
	
	
	public static void getBestTree(){
		List<SplitDetails> list_result = new ArrayList<SplitDetails>();
		float st = Float.MIN_VALUE;
		float end = Float.MAX_VALUE;
		
		int depth_limit = DecisionTree.depth_limit;
		int no_features = DecisionTree.noFeatures;
		int depth = -1;
		int count = 0;
		int parentFeature = -1;
		
		while(true){
		    if(depth > depth_limit || count == m)
			   break;
		    
			depth++;
			count = 0;
			
			List<NodeDetails> temp_node_details = new ArrayList<NodeDetails>();
			
			for(NodeDetails node_details : listNodeDetails){
				BeamNode beam_node = node_details.beam_node;
				LinkedHashSet<Integer> setVisited = node_details.setVisited;
				
				TargetBeamNode target_node = searchTargetNode(beam_node);
				Set<Integer> setValidRecords = new HashSet<Integer>();
				
				if(target_node == null || setVisited.size() == no_features-1){
					count++;
					continue;
				}
					
			    BeamNode target = target_node.beam_node;
				int direction = target_node.direction;
				
				if(target.sd == null)
				   parentFeature = -1;
				else
				   parentFeature = target.sd.feature_index;
				
				if(direction == 0){
					st = target.sd.feature_left_st;
					end = target.sd.feature_left_end;
					setValidRecords = DecisionTree.getValidRecords(target.sd.list_record_no, 0).get(0);
				}
				else if(direction == 1){
					st = target.sd.feature_right_st;
					end = target.sd.feature_right_end;
					setValidRecords = DecisionTree.getValidRecords(target.sd.list_record_no, 0).get(1);
				}
				
				setVisited = DecisionTree.getVisitedFeatures(setVisited, parentFeature);
				list_result = getBestSplit(DecisionTree.hashData, parentFeature, setValidRecords, st, end, setVisited, depth);
				
				System.out.println("Set Size: "+setVisited.size());
				for(int i=0;i<list_result.size();i++){
					BeamNode new_node = new BeamNode();
					
					SplitDetails sd = list_result.get(i);
					new_node.sd = sd;
					
					if(target.sd == null){
						beam_node = new_node;
					}
					else{
						attachToTarget(beam_node, target, new_node, direction);
					}
					
					BeamNode copy_node = new BeamNode(beam_node);
					temp_node_details.add(new NodeDetails(copy_node, sd.feature_index, sd.class_label));
					
				}
				
				//System.out.println("Index: "+list_result.get(0).feature_index+", Value: "+list_result.get(0).gini+", Class: "+list_result.get(0).class_label);
				//System.out.println("Index: "+list_result.get(1).feature_index+", Value: "+list_result.get(1).gini+", Class: "+list_result.get(1).class_label);
				//System.out.println("Index: "+list_result.get(2).feature_index+", Value: "+list_result.get(2).gini+", Class: "+list_result.get(2).class_label);
			    //System.out.println("******************************************");
			}
			
			compareTrees(temp_node_details);
			
			for(int i=0;i<list_result.size();i++){
				LinkedHashSet<Integer> set = listNodeDetails.get(i).setVisited;
				
				if(!DecisionTree.setLabels.contains(temp_node_details.get(i).class_label)){
					set.add(temp_node_details.get(i).cur_feature);
				}
				
				listNodeDetails.set(i, temp_node_details.get(i));
				listNodeDetails.get(i).setVisited = set;
				
				//System.out.println("Index: "+i+", Size: "+set.size());
				
			}
			System.out.println("********************************");
		}
	}
	
	public static void displayBeamTree(BeamNode node){
		if(node == null)
			return;
		else{
			System.out.println("Class: "+node.sd.class_label+", Index: "+node.sd.feature_index+
					           ", Split Value: "+node.sd.split_value);
			displayBeamTree(node.left);
			displayBeamTree(node.right);
		}
	}
	
	public static void main(String[] args) throws Exception{
		String criteria = "gini";		   
	    int m = 3;
	    init(criteria, m);
	    
	    String fileName = DecisionTree.curDir+"\\iris.csv";
	    DecisionTree.readDataset(fileName, false);
	 
	    System.out.println("Decision Tree");
	    getBestTree();
	    
	    for(NodeDetails node : listNodeDetails){
	    	BeamNode root = node.beam_node;
	    	System.out.println("***************************");
	    	displayBeamTree(root);
	    	System.out.println("***************************");
	    }
	    
	    //DecisionTree.root = DecisionTree.decisionTree(DecisionTree.hashData, setVisited, -1, setValidRecords, Float.MIN_VALUE, Float.MAX_VALUE, DecisionTree.root, 0);
	   
	    FileWriter fw = new FileWriter("C:\\Users\\Shrijit\\Desktop\\output.txt");
	   
	    //System.out.println("******************** Final Tree *****************************");
	    //displayTree(root, fw);
	    fw.close();
		   
		   //String test_file = "test3.csv";
		   //String path = curDir+"\\"+test_file;
		   //evalTestFile(path);
     }
}

class NodeDetails{
	BeamNode beam_node;
	LinkedHashSet<Integer> setVisited;
	float total_gini;
	int cur_feature;
	String class_label;
	
	NodeDetails(){
		beam_node = new BeamNode();
		setVisited = new LinkedHashSet<Integer>();
	}
	
	NodeDetails(BeamNode node, int cur_feature, String label){
		beam_node = node;
		this.cur_feature = cur_feature;
		class_label = label;
	}
}

class BeamNode{
	BeamNode left;
	BeamNode right;
	SplitDetails sd;
	
	BeamNode(){}
	
	BeamNode(BeamNode left, BeamNode right, SplitDetails sd){
		this.left = left;
		this.right = right;
		this.sd = sd;
	}
	
	BeamNode(BeamNode beam_node){
		this(beam_node.left, beam_node.right, beam_node.sd);
	}
}

class SplitDetails{
	float gini;
	int feature_index;
	float split_value;
	String class_label;
	float feature_left_st;
	float feature_left_end;
	float feature_right_st;
	float feature_right_end;
	Set<Integer> setVisited;
	List<String> list_record_no;
	
	SplitDetails(float gini,
				 int feature_index,
				 float split_value,
				 String class_label,
				 float feature_left_st,
				 float feature_left_end,
				 float feature_right_st,
				 float feature_right_end,
				 List<String> list_left_record_no,
				 List<String> list_right_record_no){
		this.gini = gini;
		this.feature_index = feature_index;
		this.split_value = split_value;
		this.class_label = class_label;
		this.feature_left_st = feature_left_st;
		this.feature_left_end = feature_left_end;
		this.feature_right_st = feature_right_st;
		this.feature_right_end = feature_right_end;
		
		setVisited = new LinkedHashSet<Integer>();
		list_record_no = new ArrayList<String>();
		
		this.list_record_no.addAll(list_left_record_no);
		this.list_record_no.add("");
		this.list_record_no.addAll(list_right_record_no);
	}
	
}

class TargetBeamNode{
	BeamNode beam_node;
	int direction;
	
	TargetBeamNode(BeamNode node, int direction){
		this.beam_node = node;
		this.direction = direction;
	}
}
