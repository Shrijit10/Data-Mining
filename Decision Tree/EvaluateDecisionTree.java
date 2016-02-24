import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


public class EvaluateDecisionTree {
  static String curDir;
  static int total;
  static int correct;
  
 
  public static void init(String criteria){
	  curDir = System.getProperty("user.dir");
	  CrossValidation.init(10);
	  DecisionTree.init(criteria);
  }
  
  public static void buildDecisionTree() throws IOException{
	  Set<Integer> setVisited = new LinkedHashSet<Integer>();
	  Set<Integer> setValidRecords = new HashSet<Integer>();
	   
	  for(int i=1;i<=DecisionTree.hashData.size();i++)
		   setValidRecords.add(i);
	   
	  DecisionTree.root = DecisionTree.decisionTree(DecisionTree.hashData, setVisited, 0, setValidRecords,
			                                        Float.MIN_VALUE, Float.MAX_VALUE, DecisionTree.root, 0);
  }
  
  public static String predictClass(Node node, String[] temp){
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
	    
	}
  
  public static void evalTestFile(String file_name) throws Exception{
	   BufferedReader br = new BufferedReader(new FileReader(file_name));
	   String s="";
	   String[] temp = null;
	   String predict_class = "";
	   int correct = 0;
	   int total = 0;
	   
	   while((s=br.readLine())!=null){
		   total++;
		   temp = s.split(",");
		   predict_class = predictClass(DecisionTree.root, temp);
		   
		   String actual_class = temp[temp.length-1];
		   if(predict_class.equals(actual_class))
			   correct++;
		}
	   
	   System.out.println("Accuracy: "+ (float)correct/total);
	   System.out.println("Total: "+total+", Correct: "+correct);
	   
	   br.close();
	   
  }
	
  public static void main(String[] args) throws Exception{
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
	 int k = CrossValidation.k;
	 
	 String filename = "winequality-white.csv";  // change filenames to 10 different datasets
	 int pos = filename.lastIndexOf(".");
	 String ext = filename.substring(pos);
	 String path = curDir+"\\"+filename;
	 
	 CrossValidation.readDataset(path, false); 

	 int records = CrossValidation.hash.size();
	 for(int i=1;i<=k;i++){
		CrossValidation.generatePartitions(i, records, ext, false);
		DecisionTree.hashData.clear();
		DecisionTree.setLabels.clear();
		
		path = curDir+"\\"+"train"+i+ext;
		DecisionTree.readDataset(path, false);
		
		System.out.println("Building tree");
		buildDecisionTree();
		
		path = curDir+"\\"+"test"+i+ext;
		System.out.println("Evaluating file: "+path);
		
	    evalTestFile(path);
	 }
	 
   }
}
