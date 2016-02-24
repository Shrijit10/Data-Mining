import java.io.FileWriter;


public class EvalClosestWithNorm {
  public static void main(String[] args) throws Exception{
	  Closest.init();
		
	  String filename = "data_wine_with_norm.csv";    
  	  String curDir = System.getProperty("user.dir");
  	  String path = curDir+"\\"+filename;
  	  boolean is_norm = true;
  	  
  	  Closest.readWineData(path, false);
  	  
  	  FileWriter fw = new FileWriter(curDir+"\\with_norm_dist.csv");
  	  float result = Closest.getClosestLabelPerc(fw, is_norm, -1);
	  fw.close();
	  
	  System.out.println();
	  System.out.println("For entire dataset, After Normalization...");
	  System.out.println("Closest Neighbor % with same class label: "+result*100);
	  System.out.println();
	  
	  for(String s : Closest.setLabels){
		int label = Integer.parseInt(s);  
		result = Closest.getClosestLabelPerc(null, is_norm, label);
		
	    System.out.println("For class: "+label+", After Normalization...");
	    System.out.println("Closest Neighbor % with same class label: "+result*100);
	    System.out.println();
	  }
  }
}