import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;


public class EvaluateBeamSearch {
	static String curDir;
	
	public static void init(String criteria, int m, int k){
		curDir = System.getProperty("user.dir");
		CombinedBeamSearch.init(criteria, m, k);
	}
	
	public static String predictClass(BeamNode node, String[] temp){
	    String predict_class = node.sd.class_label; 
	   
	    if(!predict_class.equals("-1"))
	    	return predict_class;
	   
	    int feature_index = node.sd.feature_index;
	    float test_feature_val = Float.parseFloat(temp[feature_index]);
	    float train_feature_val = node.sd.split_value;
	    
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
		   predict_class = predictClass(CombinedBeamSearch.root, temp);
		   
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
  	  System.out.println("Please enter number of trees: ");
  	  
  	  String criteria = "gini";
  	  int k = 10;
  	  int m = Integer.parseInt(br.readLine());
  	  init(criteria, m, k);
  	  
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
		
		System.out.println("Building trees");
		CombinedBeamSearch.getBestTree();
		
		CombinedBeamSearch.root = CombinedBeamSearch.listNodeDetails.get(0).beam_node;
		
		path = curDir+"\\"+"test"+i+ext;
		System.out.println("Evaluating file: "+path);
		
	    evalTestFile(path);
	  }
  	  
    }
}
