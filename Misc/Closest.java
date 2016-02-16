import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Closest {
	static List<WineObj> list_wine_obj;
	
	public static void init(){
		list_wine_obj = new ArrayList<WineObj>();
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
    
    public static float getClosestLabelPerc(){
    	float min = Float.MAX_VALUE;
    	float dist = 0f;
    	int closest = -1;
    	int closest_label = -1;
    	int count = 0;
    	
    	for(int i=0;i<list_wine_obj.size();i++){
    		 min = Float.MAX_VALUE;
    	     closest = -1;	 
    	     closest_label = -1;
    		 for(int j=0;j<list_wine_obj.size();j++){
    			  if(i!=j){
    				 dist = getEuclideanDist(list_wine_obj.get(i), list_wine_obj.get(j)); 
    				 
    				 if(dist < min){
    					 min = dist;
    				     closest = j;
    				     closest_label = list_wine_obj.get(j).label;
    				 }
    			  }
    		 } 
    		 
    		 list_wine_obj.get(i).closest_record_no = closest;
    		 if(closest_label == list_wine_obj.get(i).label)
    			 count++;
    	}
    	
    	return (float)count/list_wine_obj.size();
    }
	
	public static void main(String[] args) throws Exception{
      init();
		
	  String filename = "wine.csv";
  	  String curDir = System.getProperty("user.dir");
  	  String path = curDir+"\\"+filename;
  	  
  	  readWineData(path, true);
  	  float result = getClosestLabelPerc();
  	  System.out.println("Result: "+result);
    }
	
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
