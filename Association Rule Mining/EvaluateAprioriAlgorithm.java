import java.io.BufferedReader;
import java.io.InputStreamReader;


public class EvaluateAprioriAlgorithm {

	public static void init(){
		DataPreprocessing.init();
	}
	
	public static void performDataPreprocessing(String path, String sep, boolean hasHeader) throws Exception{
		DataPreprocessing.readData(path, sep, hasHeader);
	    DataPreprocessing.setDataDimensions();
		DataPreprocessing.buildTransactions();
		//DataPreprocessing.displayData();
	}
	
	public static void performCandidateGeneration(String type, float minSup){
		CandidateGeneration.init(minSup);
		CandidateGeneration.generateCandidates(type);
		CandidateGeneration.displayCandidateGenDtls();
	}
	
	public static void performRuleGeneration(float minConf, String measure){
		RuleGeneration.init(minConf, measure);
		RuleGeneration.performRuleGeneration();
		RuleGeneration.getTopAssociationRules();
		RuleGeneration.displayRules();
	}
	
	public static void main(String[] args) throws Exception {
		init();

		String filename = "flare.data2";
		String sep = " ";          // flare.data2 dataset has delimiter as " ". Default is "," (comma separated)
		boolean hasHeader = true;  // flare.data2 dataset has header. Hence this value should be set to true
		String type = "1";
		float minThreshold = 0f;
		String measure = "";
		
		String path = DataPreprocessing.curDir+"\\"+filename;
		performDataPreprocessing(path, sep, hasHeader);
		
		System.out.println("Enter 1 for Fk-1 * F1, 2 for Fk-1 * Fk-1: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String choice = br.readLine(); 
		
		if(choice.equals("1"))
		   type = "1";
		else if(choice.equals("2"))
		   type = "2";	
		else{
		   System.out.println("Invalid choice.System aborting...");
		   return;
		}
		
		float minSup = 0.2f;    // change values of Minimum Support to 0.4f and 0.6f.
		                        // Thus 3 values of Minimum Support are 0.1f, 0.4f and 0.6f
		
		System.out.println("Enter 1 for Confidence, 2 for Lift:");
		br = new BufferedReader(new InputStreamReader(System.in));
		choice = br.readLine(); 
		
		if(choice.equals("1"))
			measure = "C";
		else if(choice.equals("2"))
			measure = "L";
		else{
			System.out.println("Invalid choice.System aborting...");
			return;
		}
			
		if(measure.equals("C"))
		   minThreshold = 0.4f;    // change values of Minimum Confidence to 0.4f and 0.5f
                                   // Thus 3 values of Minimum Confidence are 0.4f, 0.5f and 0.6f
		//else
		   //minThreshold = 0.2f;    
        						   
		String attribute_file = "attributes_solar.txt";  // dataset attribute file; contains same delimiter as that in dataset
		                                           // contains 1 line of attribute values
												   // for eg. for "car" dataset, the contents of this file is...
												   // buying,maint,doors,persons,lug_boot,safety,class
												   // if this file is not provided, then the output will contain...
		                                           // indices of attributes(after discretization) 
		path = DataPreprocessing.curDir+"\\"+attribute_file;
		sep = ",";
		DataPreprocessing.readAttributeData(path, sep);
		
		performCandidateGeneration(type, minSup);
		performRuleGeneration(minThreshold, measure);
		
		
    }

}
