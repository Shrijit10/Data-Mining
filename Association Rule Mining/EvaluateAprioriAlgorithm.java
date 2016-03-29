
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
	}
	
	public static void performRuleGeneration(float minConf){
		RuleGeneration.init(minConf);
		RuleGeneration.performRuleGeneration();
		RuleGeneration.displayRules();
	}
	
	public static void main(String[] args) throws Exception {
		init();

		String filename = "car.data";
		String sep = ",";
		boolean hasHeader = false;
		String path = DataPreprocessing.curDir+"\\"+filename;
		
		performDataPreprocessing(path, sep, hasHeader);
		
		String type = "1";
		float minSup = 0.1f;
		float minConf = 0.6f;
		performCandidateGeneration(type, minSup);
		performRuleGeneration(minConf);
		
    }

}
