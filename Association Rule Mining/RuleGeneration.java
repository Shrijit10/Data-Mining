import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RuleGeneration {
	static List<List<RuleObj>> listFreqRules;
	static float minConf;
	
	public static void init(float conf){
		listFreqRules = new ArrayList<List<RuleObj>>();
		minConf = conf;
	}
	
	public static float getRuleConfidence(String ante, String conse, String item){
		HashMap<String, Integer> hash = CandidateGeneration.hashFreqItemset;
		int itemset_support = 0;
		int ante_support = 0;
		
		//System.out.println("Ante: "+ante);
		//System.out.println("Conse: "+conse);
		
		itemset_support = hash.get(item);
		ante_support = hash.get(ante);
		
		float confidence = (float)itemset_support/ante_support;
		
		return confidence;
		
	}
	
	public static void populateInitRules(List<ItemsetObj> list_itemset){
		List<RuleObj> list_rule_obj = new ArrayList<RuleObj>();
		
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
				
				  float confidence = getRuleConfidence(ante, conse, item);
				
				  if(confidence>=minConf){
				    RuleObj rule_obj = new RuleObj(ante, conse, confidence, item);
				    list_rule_obj.add(rule_obj);
				  }
				  
				  pos--;
				}
			}
		}
		
		listFreqRules.add(list_rule_obj);
			
	}
	
	public static boolean generateFreqRules(String ante, String conse, String item){
		List<RuleObj> list_rule_obj = new ArrayList<RuleObj>();
		String[] temp_ante = ante.split(",");
		int pos = temp_ante.length-1;
		
		if(temp_ante.length>1){
		   while(pos>=0){
			 String new_conse = temp_ante[pos] + "," + conse;
			 String new_ante = "";
				
			 for(int i=0;i<temp_ante.length;i++){
				if(i!=pos){
				  new_ante+=temp_ante[i]+","; 	
				 }
			 } 
				
			 new_ante = new_ante.substring(0, new_ante.length()-1);
				
			 float confidence = getRuleConfidence(new_ante, new_conse, item);
			 if(confidence>=minConf){
			   RuleObj rule_obj = new RuleObj(new_ante, new_conse, confidence, item);
			   list_rule_obj.add(rule_obj);
			 }
				
			 pos--;
		   }
		}
		
		if(!list_rule_obj.isEmpty()){
		  listFreqRules.add(list_rule_obj);
		  return true;
		}
		
		return false;
		
	}
	
	public static void performRuleGeneration(){
		List<List<ItemsetObj>> list_freq_itemsets = CandidateGeneration.listFreqItemsets;
		int last_index = -1;
		
		if(!list_freq_itemsets.get(list_freq_itemsets.size()-1).isEmpty()){
		   last_index = list_freq_itemsets.size()-1;
		   populateInitRules(list_freq_itemsets.get(last_index));
		}
		
		last_index = listFreqRules.size()-1;
		one: while(!listFreqRules.get(last_index).isEmpty()){
				List<RuleObj> list_rule_obj = listFreqRules.get(last_index);
				
				
				for(RuleObj rule_obj : list_rule_obj){
				   String ante = rule_obj.antecedent;
				   String conse = rule_obj.consequent;
				   String item = rule_obj.item;
				   
				   boolean status = generateFreqRules(ante, conse, item);	
				   if(!status)
					   break one;
				}
				
				last_index = listFreqRules.size()-1;
		     }
	}
	
	public static void displayRules(){
		for(List<RuleObj> list : listFreqRules){
			for(RuleObj rule_obj : list){
				System.out.println("Ante: "+rule_obj.antecedent+"  Conse: "+rule_obj.consequent+" Confidence: "+rule_obj.confidence);
			}
		}
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