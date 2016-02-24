import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Closest {
	static List<WineObj> list_wine_obj;
	static Set<String> setLabels;
	
	public static void init(){
		list_wine_obj = new ArrayList<WineObj>();
		setLabels = new HashSet<String>();
	}
	
    public static void readWineData(String path, boolean hasHeader) throws Exception{
      BufferedReader br = new BufferedReader(new FileReader(path));
  	  String s = "";
  	  String[] temp = null;
  	  int line_no = 0;
  	  
  	  if(hasHeader)
  	    s = br.readLine();
  		  
  	  while((s=br.readLine())!=null){
  		 temp = s.split(",");
  		 line_no++;
  		 setLabels.add(temp[0]);
  		 
  		 list_wine_obj.add(new WineObj(line_no,
  				                       Integer.parseInt(temp[0]),
  				                       Float.parseFloat(temp[1]),
  				                       Float.parseFloat(temp[2]),
  				                       Float.parseFloat(temp[3]),
  				                       Float.parseFloat(temp[4]),
  				                       Float.parseFloat(temp[5]),
  				                       Float.parseFloat(temp[6]),
				                       Float.parseFloat(temp[7]),
				                       Float.parseFloat(temp[8]),
				                       Float.parseFloat(temp[9]),
				                       Float.parseFloat(temp[10]),
				                       Float.parseFloat(temp[11]),
				                       Float.parseFloat(temp[12]),
				                       Float.parseFloat(temp[13])));
  		  
  	  }
  	  
  	  br.close();
  	}
    
    public static float getEuclideanDist(WineObj w1, WineObj w2){
    	double sum = 0;
    	
    	sum = Math.pow(w1.alcalinity - w2.alcalinity,2) +
    		  Math.pow(w1.alcohol - w2.alcohol,2) +
    		  Math.pow(w1.ash - w2.ash,2) +
    		  Math.pow(w1.color - w2.color,2) +
    		  Math.pow(w1.diluted_wines - w2.diluted_wines,2) +
    		  Math.pow(w1.flavanoid - w2.flavanoid,2) +
    		  Math.pow(w1.hue - w2.hue,2) +
    		  Math.pow(w1.magnesium - w2.magnesium,2) +
    		  Math.pow(w1.malic_acid - w2.malic_acid,2) +
    		  Math.pow(w1.non_flavanoid - w2.non_flavanoid,2) +
    		  Math.pow(w1.proantho - w2.proantho,2) +
    		  Math.pow(w1.proline - w2.proline,2) + 
    		  Math.pow(w1.total_phenol - w2.total_phenol,2);
    	
    	sum = Math.sqrt(sum);
    	
    	return (float)sum;
    		  
    }
    
    public static void displayClosestExample(int index, int closest, FileWriter fw) throws Exception{
    	WineObj w = list_wine_obj.get(closest);
    	
    	if(fw!=null){
    	  fw.write("Closest Example for Record: "+(index+1)+" with class label: "+list_wine_obj.get(index).label+" is"+"\n");
    	  fw.write("Class Label: "+w.label+", Alcohol: "+w.alcohol+", Malic Acid: "+w.malic_acid+", Ash: "+w.ash+", Alcalinity: "+w.alcalinity+
		         ", Magnesium: "+w.magnesium+", Total Phenols: "+w.total_phenol+", Flavanoid: "+w.flavanoid+
		         ", Non-Flavanoid: "+w.non_flavanoid+", Proanthocyanins: "+w.proantho+
		         ", Color Intensity: "+w.color+", Hue: "+w.hue+", Diluted Wines: "+w.diluted_wines+
		         ", Proline: "+w.proline+"\n\n");
    	}
    	//System.out.println("Closest Example for Record: "+(index+1)+" with class label: "+list_wine_obj.get(index).label+" is");
    	/*System.out.println("Class Label: "+w.label+", Alcohol: "+w.alcohol+", Malic Acid: "+w.malic_acid+", Ash: "+w.ash+", Alcalinity: "+w.alcalinity+
    			           ", Magnesium: "+w.magnesium+", Total Phenols: "+w.total_phenol+", Flavanoid: "+w.flavanoid+
    			           ", Non-Flavanoid: "+w.non_flavanoid+", Proanthocyanins: "+w.proantho+
    			           ", Color Intensity: "+w.color+", Hue: "+w.hue+", Diluted Wines: "+w.diluted_wines+
    			           ", Proline: "+w.proline+"\n");
    	System.out.println();*/
    	
    }
    
    public static float getClosestLabelPerc(FileWriter fw, boolean is_norm, int label) throws Exception{
    	float min = Float.MAX_VALUE;
    	float dist = 0f;
    	int closest = -1;
    	int closest_label = -1;
    	int count = 0;
    	int size = 0;
    	
    	float closest_proline=0f;
    	float closest_mag=0f;
    	FileWriter fw_closest = null;
    	
    	if(fw!=null){
    	  if(!is_norm)
    	    fw_closest = new FileWriter("closest_example_without_normalization.txt");
    	  else
    	    fw_closest = new FileWriter("closest_example_with_normalization.txt");
    	}
    	
    	for(int i=0;i<list_wine_obj.size();i++){
    		 min = Float.MAX_VALUE;
    	     closest = -1;	 
    	     closest_label = -1;
    	     closest_proline = 0f;
    	     closest_mag = 0f;
    	     
    	     if(label == list_wine_obj.get(i).label || label == -1){
    	    	 size++;
	    		 for(int j=0;j<list_wine_obj.size();j++){
	    			  if(i!=j){
	    				 dist = getEuclideanDist(list_wine_obj.get(i), list_wine_obj.get(j)); 
	    				 
	    				 if(dist < min){
	    					 min = dist;
	    				     closest = j;
	    				     closest_label = list_wine_obj.get(j).label;
	    				     closest_proline = list_wine_obj.get(j).proline;
	    				     closest_mag = list_wine_obj.get(j).magnesium;
	    				 }
	    			  }
	    		 } 
	    		 
	    		 if(fw_closest!=null)
	    		   displayClosestExample(i, closest, fw_closest);
	    		 
	    		 list_wine_obj.get(i).closest_record_no = closest;
	    		 if(closest_label == list_wine_obj.get(i).label)
	    			 count++;
	    		 
	    	     double proline_mag = Math.pow(list_wine_obj.get(i).proline - closest_proline,2) + 
	    			                      Math.pow(list_wine_obj.get(i).magnesium - closest_mag, 2);
	    			 
	    	   	 proline_mag = Math.sqrt(proline_mag);
	    	   	 
	    	   	 if(fw!=null)
	    		   fw.write(min+","+proline_mag+"\n");
    	     }
    		 
    	}
    	
    	if(fw_closest!=null)
    	  fw_closest.close();
    	
    	if(fw!=null){
    	  if(!is_norm)
    	    System.out.println("Please check \"closest_example_without_normalization.txt\" to find closest example for a given example");
    	  else
    	    System.out.println("Please check \"closest_example_with_normalization.txt\" to find closest example for a given example");	
    	}
    	
    	return (float)count/size;
    }
	
	/*public static void main(String[] args) throws Exception{
      init();
		
	  String filename = "data_wine_without_norm.csv";    
  	  String curDir = System.getProperty("user.dir");
  	  String path = curDir+"\\"+filename;
  	  boolean is_norm = false;
  	  
  	  readWineData(path, false);
  	  
  	  FileWriter fw = new FileWriter(curDir+"\\without_norm_dist.csv");
  	  float result = getClosestLabelPerc(fw, is_norm, -1);
  	  fw.close();
  	  
  	  System.out.println("For entire dataset, Before Normalization...");
  	  System.out.println("Closest Neighbor %: "+result*100);
  	  System.out.println();
  	  
  	  for(String s : setLabels){
  		int label = Integer.parseInt(s);  
  		result = getClosestLabelPerc(null, is_norm, label);
  		
  		System.out.println("For class: "+label+", Before Normalization...");
    	System.out.println("Closest Neighbor %: "+result*100);
    	System.out.println();
  	  }
  	  
  	  list_wine_obj.clear();
  	  filename = "data_wine_with_norm.csv";
  	  path = curDir+"\\"+filename;
  	  readWineData(path, false);
  	  
  	  is_norm = true;
  	  
  	  fw = new FileWriter(curDir+"\\with_norm_dist.csv");
  	  result = getClosestLabelPerc(fw, is_norm, -1);
  	  fw.close();
	  
	  System.out.println("For entire dataset, After Normalization...");
	  System.out.println("Closest Neighbor %: "+result*100);
	  System.out.println();
  	  
  	  for(String s : setLabels){
  		int label = Integer.parseInt(s);  
  		result = getClosestLabelPerc(null, is_norm, label);
  		
  		System.out.println("For class: "+label+", After Normalization...");
    	System.out.println("Closest Neighbor %: "+result*100);
    	System.out.println();
  	  }
  	  
  	  
    }*/
	
}

class WineObj{
	int record_no;
	int label;
	float alcohol;
	float malic_acid;
	float ash;
	float alcalinity;
	float magnesium;
	float total_phenol;
	float flavanoid;
	float non_flavanoid;
	float proantho;
	float color;
	float hue;
	float diluted_wines;
	float proline;
	int closest_record_no;
	
	WineObj(int record_no,
			int label,
			float alcohol,
			float malic_acid,
			float ash,
			float alcalinity,
			float magnesium,
			float total_phenol,
			float flavanoid,
			float non_flavanoid,
			float proantho,
			float color,
			float hue,
			float diluted_wines,
			float proline){
		this.record_no = record_no;
		this.label=           label;
		this.alcohol=         alcohol;
		this.malic_acid=      malic_acid;	
		this.ash=             ash		;
		this.alcalinity=      alcalinity;
		this.magnesium=       magnesium	;
		this.total_phenol=    total_phenol;
		this.flavanoid=       flavanoid	;
		this.non_flavanoid=   non_flavanoid;
		this.proantho=        proantho  ;
		this.color=           color		;
		this.hue=             hue		;
		this.diluted_wines=   diluted_wines;
		this.proline=         proline       ;   

	}
}
