import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;


public class OverfitPrevention {
   static String curDir;
   static Node droot;
   static PNode proot;
   static int total_records;
   static Set<String> setLabels;
   static HashMap<Integer, ROCObj> hashROC;
   
   public static void init(String criteria){
	   curDir = System.getProperty("user.dir");
	   droot = new Node();	  
	   CrossValidation.init(10);
	   DecisionTree.init(criteria);
	   
	   hashROC = new HashMap<Integer, ROCObj>();
	   total_records = DecisionTree.hashData.size();
   }
   
   public static void getROC(){
	   
   }
   
   public static void buildValidationSet(String path) throws Exception{
	   BufferedReader br = new BufferedReader(new FileReader(path));
	   HashMap<Integer, String> hash = new HashMap<Integer, String>();
	   String s = "";
	   int count = 0;
	   String filename = path.substring(path.lastIndexOf("\\")+1);
	   
	   FileWriter fw = new FileWriter("v_"+filename);
	   while((s=br.readLine())!=null){
		   count++;
		   hash.put(count, s);
	   }
	   
	   for(int i=1;i<=count/4;i++){
		   fw.write(hash.get(i)+"\n");
	   }
	   
	   fw.close();
	   br.close();
	   
   }
   
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
   
   public static void displayETree(PNode node, FileWriter fw) throws IOException{
	   if(node == null)
		   return;
	   else{
		   if(fw!=null)
		     fw.write("Class: "+node.label+", Index: "+node.feature_index+", Split Value: "+node.split_value+"\n");
		   
		   System.out.println("Class: "+node.label+", Index: "+node.feature_index+", Split Value: "+node.split_value+
				              ", Max Class Label: "+node.max_class_label+", Max Count: "+node.max_class_count+
				              ", Label Count: "+node.label_count);
		   displayETree(node.left, fw);
		   displayETree(node.right, fw);
	   }
   }
   
   public static void refinePTree(PNode pnode){
	   if(pnode == null)
		   return;
	   
	   if(!setLabels.contains(pnode.label) && pnode.left == null && pnode.right == null){
		   pnode.label = pnode.max_class_label;
		   return;
	   }
	   else{
		   refinePTree(pnode.left);
		   refinePTree(pnode.right);
	   }
   }
   
   public static String predictLabel(PNode pnode, String[] temp){
	   String predict_class = pnode.label; 
	   
	   if(!predict_class.equals("-1"))
	    	return predict_class;
	   
	   if(predict_class.equals("-1") && pnode.left == null && pnode.right == null){
		   return pnode.max_class_label;
	   }
	   
	   int feature_index = pnode.feature_index;
	   float test_feature_val = Float.parseFloat(temp[feature_index]);
	   float train_feature_val = pnode.split_value;
	    
	   if(test_feature_val <= train_feature_val)
	      return predictLabel(pnode.left, temp);
	   else
	      return predictLabel(pnode.right, temp);
	    
	}
  
  public static void evalTestFile(String file_name) throws Exception{
	   BufferedReader br = new BufferedReader(new FileReader(file_name));
	   String s="";
	   String[] temp = null;
	   String predict_class = "";
	   int correct = 0;
	   int total = 0;
	   boolean flag = false;
	   String pos_class = "";
	   String neg_class = "";
	   int tp = 0;
	   int tn = 0;
	   int fp = 0;
	   int fn = 0;
	   
	   for(String label : setLabels){
		   if(!flag){
			 pos_class = label;
			 flag = true; 
		   }  
		   else{
			 neg_class = label;
		   }
	   }
	   
	   while((s=br.readLine())!=null){
		   total++;
		   temp = s.split(",");
		   predict_class = predictLabel(proot, temp);
		   
		   String actual_class = temp[temp.length-1];
		   if(predict_class.equals(actual_class)){
			   correct++;
		       if(predict_class.equals(pos_class))
		    	   tp++;
		       else
		    	   tn++;
		   }
		   else{
			   if(predict_class.equals(pos_class))
		    	   fp++;
		       else
		    	   fn++;
		   }
		}
	   
	   System.out.println("Accuracy: "+ (float)correct/total);
	   System.out.println("Total: "+total+", Correct: "+correct);
	   
	   br.close();
	   
   }
   
   
   public static int evalValidationFile(String file_name, PNode pnode) throws Exception{
	   BufferedReader br = new BufferedReader(new FileReader(file_name));
	   String s="";
	   String[] temp = null;
	   String predict_class = "";
	   int correct = 0;
	   int total = 0;
	   
	   while((s=br.readLine())!=null){
		   total++;
		   temp = s.split(",");
		   predict_class = predictLabel(pnode, temp);
		   
		   String actual_class = temp[temp.length-1];
		   if(predict_class.equals(actual_class))
			   correct++;
		}
	   
	   //System.out.println("Accuracy: "+ (float)correct/total);
	   //System.out.println("Total: "+total+", Correct: "+correct);
	   
	   br.close();
	   
	   return total - correct;
   }
  
  
   public static void buildDecisionTree() throws IOException{
		  Set<Integer> setVisited = new LinkedHashSet<Integer>();
		  Set<Integer> setValidRecords = new HashSet<Integer>();
		   
		  for(int i=1;i<=DecisionTree.hashData.size();i++)
			   setValidRecords.add(i);
		   
		  droot = DecisionTree.decisionTree(DecisionTree.hashData, setVisited, 0, setValidRecords,
				                           Float.MIN_VALUE, Float.MAX_VALUE, droot, 0);
   }
   
   public static void buildETree(PNode pnode, Node node, Node parent, int direction){
	   if(pnode == null)
	     return;
	   
	   if(parent == null){
		   pnode.feature_index = node.featureIndex;
		   pnode.label = node.label;
		   pnode.label_count = node.label_count;
		   pnode.max_class_count = node.max_class_count;
		   pnode.max_class_label = node.max_class_label;
		   pnode.split_value = node.featureValue;
		   return;
	   }
	   else{
		   if(pnode.feature_index == parent.featureIndex && pnode.split_value == parent.featureValue){
			   PNode new_pnode = new PNode(node);
			   if(direction == 0)
				   pnode.left = new_pnode;
			   else
				   pnode.right = new_pnode;
			   return;
		   }
		   else{
			   buildETree(pnode.left, node, parent, direction);
			   buildETree(pnode.right, node, parent, direction);
		   }
	   }
   }
   
   public static MisclassifiedObj getEstimate(PNode pnode, MisclassifiedObj missObj){
	   if(setLabels.contains(pnode.label)){
		   missObj.leaf_count++;
		   missObj.record_count += pnode.max_class_count;
		   return missObj;
	   }
	   else{
		  if(pnode.left == null && pnode.right == null){
			  missObj.leaf_count++;
			  missObj.misclassified_count += pnode.max_class_count - pnode.label_count;
			  missObj.record_count += pnode.max_class_count;
			  return missObj;
		  }
		  else{
			  missObj.internal_count++;
			  getEstimate(pnode.left, missObj);
			  getEstimate(pnode.right, missObj);
			  return missObj;
		  }
	   }
   }

   public static void buildBestETree(Node node, String type) throws Exception{
	   Queue<Node> queue = new LinkedList<Node>();
	   PNode root = new PNode();
	   float error = -1f;
	   float prev_error = -1f;
	   
	   queue.add(node);
	   
	   buildETree(root, node, null, -1);
	   MisclassifiedObj missObj = getEstimate(root, new MisclassifiedObj());
	   
	   if(type.equals("P"))
	     error = (float) ((missObj.misclassified_count + missObj.leaf_count * 0.5) / missObj.record_count);
	   else
		 error = (float) (missObj.internal_count *  Math.log(missObj.internal_count)/Math.log(2) + 
		         missObj.leaf_count * Math.log(missObj.leaf_count)/Math.log(2) + 
		         missObj.misclassified_count * Math.log(missObj.record_count)/Math.log(2));
	   
	   prev_error = error;
       
       PNode prev_root = new PNode(root);
	   //System.out.println("Error: "+error);
	   
	   while(!queue.isEmpty()){
		   Node n = queue.remove();
		   
		   if(n.left != null){
		     queue.add(n.left);
		     buildETree(root, n.left, n, 0);
		   }
		   
		   if(n.right != null){
		     queue.add(n.right);
		     buildETree(root, n.right, n, 1);
		   }
		   
		   missObj = getEstimate(root, new MisclassifiedObj());
		   
		   if(type.equals("P"))
		     error = (float) ((missObj.misclassified_count + missObj.leaf_count * 0.5) / missObj.record_count);
		   else
			 error = (float) (missObj.internal_count *  Math.log(missObj.internal_count)/Math.log(2) + 
				              missObj.leaf_count * Math.log(missObj.leaf_count)/Math.log(2) + 
				              missObj.misclassified_count * Math.log(missObj.record_count)/Math.log(2));  
		   
		   System.out.println("Error: "+error);
		   if(prev_error < error)
		      break;
		   
		   prev_error = error;
		   prev_root = new PNode(root);
		   
	   }
	  
	  System.out.println("************** PTree **********");
	  proot = prev_root;
	  refinePTree(proot);
	  displayETree(proot, null);
	   
   }
   
   public static void buildBestVTree(Node node, String fileName) throws Exception{
	   Queue<Node> queue = new LinkedList<Node>();
	   PNode root = new PNode();
	   float error = -1f;
	   float prev_error = -1f;
	   int count = 0;
	   
	   queue.add(node);
	   buildETree(root, node, null, -1);
	   
	   //PNode temp_pnode = new PNode(root);
	   
	   error = evalValidationFile(fileName, root);
	   prev_error = error;
       
       PNode prev_root = new PNode(root);
       //displayETree(prev_root, null);
	   
	   while(!queue.isEmpty()){
		   Node n = queue.remove();
		   
		   if(n.left != null){
		     queue.add(n.left);
		     buildETree(root, n.left, n, 0);
		   }
		   
		   if(n.right != null){
		     queue.add(n.right);
		     buildETree(root, n.right, n, 1);
		   }
		   	   
		   error = evalValidationFile(fileName, root);
		   
		   //System.out.println("Error: "+error);
		   
		   if(prev_error > error)
			   count = 0;
		   
		   if(prev_error == error){
			   count++;
			   if(count == 3)  // no improvement in error, then stop growing  
				   break;
		   }
		   
		   if(prev_error < error){
			  break;
		   }
		   
		   prev_error = error;
		   prev_root = new PNode(root);
		   
	   }
	  
	  System.out.println("************** PTree **********");
	  proot = prev_root;
	  refinePTree(proot);
	  displayETree(proot, null);
	   
   }
   
   
   public static void main(String[] args) throws Exception{
	   String criteria = "gini";
	   String type = "V";
	   init(criteria);
	   String fileName = curDir+"\\train6.csv";
	   DecisionTree.readDataset(fileName, false);
	   
	   setLabels = DecisionTree.setLabels;
	   buildDecisionTree();
	   
	   //displayTree(droot, null);
	   //System.out.println("*************************");
	   
	   if(!type.equals("V"))
	      buildBestETree(droot, type);
	   else{
		  buildValidationSet(fileName);
		  buildBestVTree(droot, fileName);
	   }
	   
	   String test_file = "test6.csv";
	   String path = curDir+"\\"+test_file;
	   evalTestFile(path);
	}
	   
}

class PNode{
	int feature_index;
	float split_value;
	String label;
	PNode left;
	PNode right;
    String max_class_label;
    int max_class_count;
    int label_count;
    
    PNode(Node node){
    	this.feature_index = node.featureIndex;
    	this.label = node.label; 
    	this.max_class_count = node.max_class_count;
    	this.max_class_label = node.max_class_label;
    	this.label_count = node.label_count;
    	this.split_value = node.featureValue;
    }
    
    PNode(PNode pnode){
    	this(pnode.feature_index, pnode.label, pnode.label_count, pnode.left, pnode.right, pnode.max_class_count,
    	     pnode.max_class_label, pnode.split_value);
    }
    
    PNode(int feature_index, String label, int label_count, PNode left, PNode right, 
    	  int max_class_count, String max_class_label, float split_value){
    	
    	this.feature_index = feature_index;
    	this.split_value = split_value;
    	this.label = label;
    	this.left = left;
    	this.right = right;
    	this.max_class_count = max_class_count;
    	this.max_class_label = max_class_label;
    	this.label_count = label_count;
    	 	
    }
    
    PNode(){
    	feature_index = -1;
    	label = "";
    	max_class_label = "";
    	max_class_count = -1;
    	label_count = -1;
    }
    
    
}

class MisclassifiedObj{
	int leaf_count;
	int internal_count;
	int misclassified_count;
	int record_count;
	
	MisclassifiedObj(){
		leaf_count = 0;
		misclassified_count = 0;
		record_count = 0;
		internal_count = 0;
	}
}

class ROCObj{
	float threshold;
	boolean isCorrect;
	
	ROCObj(){}
	
	ROCObj(float threshold, boolean isCorrect){
		this.threshold = threshold;
		this.isCorrect = isCorrect;
	}
}

class predictLabelObj{
	String label;
	float prob;
	
	predictLabelObj(){}
	
	predictLabelObj(String label, float prob){
		this.label = label;
		this.prob = prob;
	}
}

