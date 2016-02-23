import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


public class EvaluateOverfitPrevention {
  static String curDir;	
	
  public static void init(String criteria){
	  OverfitPrevention.init(criteria);
      curDir = System.getProperty("user.dir");
  }
  
  public static void buildDecisionTree() throws IOException{
	  Set<Integer> setVisited = new LinkedHashSet<Integer>();
	  Set<Integer> setValidRecords = new HashSet<Integer>();
	   
	  for(int i=1;i<=DecisionTree.hashData.size();i++)
		   setValidRecords.add(i);
	   
	  OverfitPrevention.droot = DecisionTree.decisionTree(DecisionTree.hashData, setVisited, 0, setValidRecords,
			                           Float.MIN_VALUE, Float.MAX_VALUE, OverfitPrevention.droot, 0);
   }
  
  public static void setPosNegClass(){
      boolean flag = false;
      OverfitPrevention.setLabels = DecisionTree.setLabels;
      
	   for(String label : OverfitPrevention.setLabels){
		   if(!flag){
			 OverfitPrevention.pos_class = label;
			 flag = true; 
		   }  
		   else{
			 OverfitPrevention.neg_class = label;
		   }
	   }
   }
  
   public static PredictLabelObj predictLabel(PNode pnode, String[] temp){
	   String predict_class = pnode.label; 
	   float prob = -1f;
	   
	   if(!predict_class.equals("-1")){
		   if(predict_class.equals(OverfitPrevention.pos_class))
			  prob = (float)pnode.label_count/pnode.max_class_count;	
		   else
			  prob =  (float)(pnode.max_class_count - pnode.label_count)/pnode.max_class_count;
		   
	       return new PredictLabelObj(predict_class, prob);
	   }
	   if(predict_class.equals("-1") && pnode.left == null && pnode.right == null){
		   if(predict_class.equals(OverfitPrevention.pos_class))
			  prob = (float)pnode.label_count/pnode.max_class_count;	
		   else
		      prob = (float)(pnode.max_class_count - pnode.label_count)/pnode.max_class_count;
		   
		   return new PredictLabelObj(pnode.max_class_label, prob);
	   }
	   
	   int feature_index = pnode.feature_index;
	   float test_feature_val = Float.parseFloat(temp[feature_index]);
	   float train_feature_val = pnode.split_value;
	    
	   if(test_feature_val <= train_feature_val)
	      return predictLabel(pnode.left, temp);
	   else
	      return predictLabel(pnode.right, temp);
	    
	}
  
  public static void evalTestFile(String file_name, boolean roc_YN) throws Exception{
	   BufferedReader br = new BufferedReader(new FileReader(file_name));
	   String s="";
	   String[] temp = null;
	   String predict_class = "";
	   float prob = -1f;
	   int correct = 0;
	   int total = 0;
	   String pos_class = "";
	   int tp = 0;
	   int tn = 0;
	   int fp = 0;
	   int fn = 0;
	   
	   while((s=br.readLine())!=null){
		   total++;
		   temp = s.split(",");
		   PredictLabelObj pl = predictLabel(OverfitPrevention.proot, temp);
		   predict_class = pl.label;
		   prob = pl.prob;
		   
		   String actual_class = temp[temp.length-1];
		   
		   if(roc_YN){
			  OverfitPrevention.hashROC.put(total, new ROCObj(prob, actual_class)); 
		   }
		   
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
	
  public static void main(String[] args) throws Exception{
	  String type = "";
	  String criteria = "gini";
	  int k = 10;
	  
	  BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	  System.out.println("Please enter 1 for Pessimistic, 2 for Validation Set and 3 for MDL: ");
	  
	  String s = br.readLine();
	  if(!s.equals("1") && !s.equals("2") && !s.equals("3")){
		  System.out.println("Invalid option entered...System aborting..");
		  return;
	  }
	  
	  if(s.equals("1"))
		  type = "P";
	  else if(s.equals("2"))
	      type = "V";
	  else
		  type = "M";
	  
	  init(criteria);
	  
	  String filename = "iris.csv";  // change filenames to 10 different datasets
	  int pos = filename.lastIndexOf(".");
      String ext = filename.substring(pos);
  	  String path = curDir+"\\"+filename;
		 
	  CrossValidation.readDataset(path, false); 

	  int records = CrossValidation.hash.size();
	  for(int i=1;i<=k;i++){
		CrossValidation.generatePartitions(i, records, ext);
	    
		path = curDir+"\\"+"train"+i+ext;
		DecisionTree.readDataset(path, false);
		
		setPosNegClass();
		buildDecisionTree();

		Node droot = OverfitPrevention.droot;
		if(!type.equals("V"))
			OverfitPrevention.buildBestETree(droot, type);
		else{
			OverfitPrevention.buildValidationSet(path);
			OverfitPrevention.buildBestVTree(droot, path);
		}
		
		path = curDir+"\\"+"test"+i+ext;
		System.out.println("Evaluating file: "+path);
		
	    evalTestFile(path, false);
	  }
	 
	  
	  //OverfitPrevention.getROC();
   }
}
