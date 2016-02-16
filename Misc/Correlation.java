import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Correlation {
  static List<CorrelatedPair> highest;
  static List<CorrelatedPair> lowest;
  static Set<String> setVisited;
  
  public static void init(){
	  highest = new ArrayList<CorrelatedPair>();
	  lowest  = new ArrayList<CorrelatedPair>();
	  setVisited = new HashSet<String>();
  }
	
  public static void getMostCorrelated(String[] record, int line_no){
	  for(int i=0;i<record.length;i++){
		 if(Float.parseFloat(record[i])!=1 && (!setVisited.contains(line_no+"-"+(i+1)) && !setVisited.contains((i+1)+"-"+line_no))){ 
			highest.add(new CorrelatedPair(line_no, i+1, Float.parseFloat(record[i])));
		    lowest.add(new CorrelatedPair(line_no, i+1, Float.parseFloat(record[i])));
		    setVisited.add(line_no+"-"+(i+1));
		 }
	  }
   
  }
	
  public static void readCorData(String filename) throws Exception{
      BufferedReader br = new BufferedReader(new FileReader(filename));
	  String s = "";
	  String[] temp = null;
	  int line = 0;
	  
	  while((s=br.readLine())!=null){
		  line++;
		  temp = s.split(",");
		  
		  getMostCorrelated(temp, line);
	  }
	  
      Collections.sort(highest, new Comparator<CorrelatedPair>(){
		  @Override
		  public int compare(CorrelatedPair c1, CorrelatedPair c2){
			 if(c1.val == c2.val)
				 return 0;
			 else if(c1.val > c2.val)
				 return -1;
			 else
				 return 1;
		  }
	  });
      
      Collections.sort(lowest, new Comparator<CorrelatedPair>(){
		  @Override
		  public int compare(CorrelatedPair c1, CorrelatedPair c2){
			 if(Math.abs(c1.val) == Math.abs(c2.val))
				 return 0;
			 else if(Math.abs(c1.val) < Math.abs(c2.val))
				 return -1;
			 else
				 return 1;
		  }
	  });
	  
	  int count = 0;
	  System.out.println("Most Correlated");
	  for(CorrelatedPair cp : highest){
		  count++;
		  System.out.println("Row: "+ cp.row+", Col: "+cp.col+", Value: "+cp.val);
		  if(count==4)
			  break;
	  }
	  
	  System.out.println();
	  System.out.println("Least Correlated");
	  count = 0;
	  for(CorrelatedPair cp : lowest){
		  count++;
		  System.out.println("Row: "+ cp.row+", Col: "+cp.col+", Value: "+cp.val);
	      
		  if(count==4)
			  break;
	  }
	  
	  br.close();
  }
	
  public static void main(String[] args) throws Exception{
	  init();
	  String filename = "correlation.csv";
	  String curDir = System.getProperty("user.dir");
	  String path = curDir+"\\"+filename;
	  
	  readCorData(path);
  }
}

class CorrelatedPair{
	int row;
	int col;
	float val;
	
	CorrelatedPair(int r, int c, float v){
		row = r;
		col= c;
		val = v;
	}
}