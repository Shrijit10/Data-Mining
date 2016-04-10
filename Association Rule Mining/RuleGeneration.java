import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class RuleGeneration {
	static List<List<RuleObj>> listFreqRules;
	static List<RuleObj> listTopRules;
	static float minConf;
	static String measure;
	static int freqRules;
	static int genRules;
	
	public static void init(float conf, String measure_type){
		listFreqRules = new ArrayList<List<RuleObj>>();
		listTopRules = new ArrayList<RuleObj>();
		minConf = conf;
		measure = measure_type;
	}
	
	public static float getRuleInterestingness(String ante, String conse, String item){
		HashMap<String, Integer> hash = CandidateGeneration.hashFreqItemset;
		int itemset_support = 0;
		int ante_support = 0;
		int conse_support = 0;
		float interestingness = 0f;
		
		itemset_support = hash.get(item);
		ante_support = hash.get(ante);
		conse_support = hash.get(conse);
		
		if(measure.equals("C"))
			interestingness = (float)itemset_support/ante_support;  // confidence
		else if(measure.equals("L"))
			interestingness = (float)itemset_support*DataPreprocessing.rows/ante_support/conse_support;  //lift
		
		return interestingness;
		
	}
	
	public static void populateInitRules(List<ItemsetObj> list_itemset){
		List<RuleObj> list_rule_obj = new ArrayList<RuleObj>();
		float interestingness = 0f;
		
		for(ItemsetObj itemset : list_itemset){
			String item = itemset.name;
			String[] temp = item.split(",");
			int pos = temp.length-1;
			
			if(temp.length>1){
				while(pos>=0){
				  String conse = temp[pos];
				  String ante = "";
				  
				  for(int i=0;i<temp.length;i++){
					  if(i!=pos){
						ante+=temp[i]+",";  
					  }
				  }
				  
				  ante = ante.substring(0, ante.length()-1);
				
				  interestingness = getRuleInterestingness(ante, conse, item);
				  	  
				  //generatedRules++;
				  
				  if(measure.equals("C")){
					 genRules++;
				     if(interestingness>=minConf){
					   freqRules++;  
				       RuleObj rule_obj = new RuleObj(ante, conse, interestingness, item);
				       list_rule_obj.add(rule_obj);
				       listTopRules.add(rule_obj);
				     }
				   }
				  else if(measure.equals("L")){  // brute force applied for lift as it is not anti-monotone
				      RuleObj rule_obj = new RuleObj(ante, conse, interestingness, item);
					  list_rule_obj.add(rule_obj);
					  listTopRules.add(rule_obj);
				   }
				  
				  pos--;
				}
			}
		}
		
		listFreqRules.add(list_rule_obj);
			
	}
	
	public static void generateFreqRules(String ante, String conse, String item, List<RuleObj> list_temp_obj){
		//List<RuleObj> list_rule_obj = new ArrayList<RuleObj>();
		String[] temp_ante = ante.split(",");
		int pos = temp_ante.length-1;
		
		if(temp_ante.length>1){
		   while(pos>=0){
			 String new_conse = temp_ante[pos] + "," + conse;
			 
			 if(!CandidateGeneration.hashFreqItemset.containsKey(new_conse)){
				pos--;
				continue;
			 }
				 
			 String new_ante = "";
				
			 for(int i=0;i<temp_ante.length;i++){
				if(i!=pos){
				  new_ante+=temp_ante[i]+","; 	
				 }
			 } 
				
			 new_ante = new_ante.substring(0, new_ante.length()-1);
			 
			 //System.out.println("New Ante: "+new_ante);
			 //System.out.println("New Conse: "+new_conse);
			 float interestingness = getRuleInterestingness(new_ante, new_conse, item);
			 
			 if(measure.equals("C")){
				genRules++; 
				if(interestingness>=minConf){
				   freqRules++;	 
				   RuleObj rule_obj = new RuleObj(new_ante, new_conse, interestingness, item);
				   list_temp_obj.add(rule_obj);
				   listTopRules.add(rule_obj);
				}
			 }
			 else if(measure.equals("L")){  // brute force applied for lift as it is not anti-monotone
				 RuleObj rule_obj = new RuleObj(new_ante, new_conse, interestingness, item);
				 list_temp_obj.add(rule_obj);
				 listTopRules.add(rule_obj);
			 }
				
			 pos--;
		   }
		}
		
	}
	
	public static void performRuleGeneration(){
		List<List<ItemsetObj>> list_freq_itemsets = CandidateGeneration.listFreqItemsets;
		int last_index = -1;
		
		for(int i=list_freq_itemsets.size()-1;i>=1;i--){
			if(!list_freq_itemsets.get(i).isEmpty()){
			   populateInitRules(list_freq_itemsets.get(i));
			}
		    
			last_index = listFreqRules.size()-1;
			
			while(!listFreqRules.get(last_index).isEmpty()){
				List<RuleObj> list_rule_obj = listFreqRules.get(last_index);
				List<RuleObj> list_temp_rule_obj = new ArrayList<RuleObj>();
				
				for(RuleObj rule_obj : list_rule_obj){
				   String ante = rule_obj.antecedent;
				   String conse = rule_obj.consequent;
				   String item = rule_obj.item;
				   
				   generateFreqRules(ante, conse, item, list_temp_rule_obj);
				}
				
				listFreqRules.add(list_temp_rule_obj);
				last_index = listFreqRules.size()-1;
			}
		}
	}
	
	public static long getTotalRules(){
		int total_rules = 0;
		
		for(int i=1;i<CandidateGeneration.listFreqItemsets.size();i++){
			List<ItemsetObj> list_itemset_obj = CandidateGeneration.listFreqItemsets.get(i);
			for(ItemsetObj itemset_obj : list_itemset_obj){
				String[] temp = itemset_obj.name.split(",");
				   total_rules+= (long)Math.pow(2, temp.length) - 2;
			}
		}
		
		return total_rules;
		//return (long)Math.pow(3, items) - (long)Math.pow(2, items+1) + 1;
	}
	
	public static void getTopAssociationRules(){
		Collections.sort(listTopRules, new Comparator<RuleObj>(){
			
			@Override
			public int compare(RuleObj r1, RuleObj r2){
				if(r1.confidence == r2.confidence)
					return 0;
				
				if(r2.confidence > r1.confidence)
					return 1;
			    else 
					return -1;
			}
		});
	}
	
	public static void displayTop10Rules(){
		String measure_type = "";
		String ante = "";
		String conse = "";
		
		if(measure.equals("C"))
			measure_type = "Confidence";
	    else
		    measure_type = "Lift";		
	    	
		System.out.println("\nTop 10 Rules (Measure: "+measure_type+")");
		for(int i=0;i<Math.min(listTopRules.size(), 10);i++)
		   if(DataPreprocessing.hashIndexToAttributes.size()>0){	
			  String[] temp_ante = listTopRules.get(i).antecedent.split(",");
			  String[] temp_conse = listTopRules.get(i).consequent.split(",");
			  
			  ante = mapIndexToAttrNames(temp_ante);
			  conse = mapIndexToAttrNames(temp_conse);
			  
			  System.out.println(ante+" -> "+conse+" ; "+measure_type+": "+listTopRules.get(i).confidence);
		    }
		   else
			 System.out.println(listTopRules.get(i).antecedent+" -> "+listTopRules.get(i).consequent+" ; "+measure_type+":"+listTopRules.get(i).confidence);  
	}
	
	public static void displayFreqRules(){
		for(List<RuleObj> list : listFreqRules){
			for(RuleObj rule_obj : list){
				System.out.println("Ante: "+rule_obj.antecedent+"  Conse: "+rule_obj.consequent+"  Interestingness: "+rule_obj.confidence);
			}
		}
	}
	
	public static String mapIndexToAttrNames(String[] str){
		String result = "";
		
		for(int j=0;j<str.length;j++)
			result+=DataPreprocessing.hashIndexToAttributes.get(Integer.parseInt(str[j]))+",";
		  
		result = result.substring(0, result.length()-1);
		return result;
	}
	
	public static void displayRules(){
		/*for(List<RuleObj> list : listFreqRules){
			for(RuleObj rule_obj : list){
				System.out.println("Ante: "+rule_obj.antecedent+"  Conse: "+rule_obj.consequent+"  Confidence: "+rule_obj.confidence);
			}
		}*/
		
		long total_rules = getTotalRules();
		System.out.println("\nTotal Possible Rules(Brute Force): "+total_rules);
		
		if(measure.equals("C")){
		   System.out.println("Generated Rules: "+genRules);	
		   System.out.println("Frequent Rules: "+freqRules);
		}
		
		displayTop10Rules();
	}
	
}

class RuleObj{
	String antecedent;
	String consequent;
	String item;
	float confidence;
	
	RuleObj(String ante, String conse, float conf, String item){
		this.antecedent = ante;
		this.consequent = conse;
		this.confidence = conf;
		this.item = item;
	}
	
}