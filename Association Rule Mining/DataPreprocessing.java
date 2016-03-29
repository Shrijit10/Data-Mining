import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


public class DataPreprocessing {
	static String curDir;
	static HashMap<Integer, LinkedHashMap<String, Integer>> hashCategory;
	static HashMap<Integer, List<String>> hashData;
	static int[][] data;
	static int rows;
	static int cols;
	
	public static void init(){
		curDir = System.getProperty("user.dir");
		hashCategory = new HashMap<Integer, LinkedHashMap<String, Integer>>();
		hashData = new HashMap<Integer, List<String>>();
	}
	
	public static void buildHashCategory(String[] temp, int index){
		String category = temp[index];
		
		if(hashCategory.containsKey(index)){
			LinkedHashMap<String, Integer> hash = hashCategory.get(index);
			if(!hash.containsKey(category)){
				hash.put(category, hash.size());
				hashCategory.put(index, hash);
			}
		 }
		else{
			LinkedHashMap<String, Integer> hash = new LinkedHashMap<String, Integer>();
			hash.put(category, 0);
			hashCategory.put(index, hash);
		}
	}
	
	public static void readData(String path, String sep, boolean hasHeader) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(path));
		String s= "";
		int count = 0;
		
		if(hasHeader)
		  s = br.readLine();
		
		while((s=br.readLine())!=null){
			String[] temp = s.split(sep);
			List<String> listData = new ArrayList<String>();
			count++;
			
			for(int i=0;i<temp.length;i++){
				buildHashCategory(temp, i);
			    listData.add(temp[i]);
			}
			
			hashData.put(count, listData);
		}
		
		br.close();
		
	}
	
	public static int getDataColumns(){
	   int cols = 0;
	   
	   for(int col : hashCategory.keySet()){
		   cols += hashCategory.get(col).size();
	   }
	   
	   return cols;
	}
	   
	public static void setDataDimensions(){
	   rows = hashData.size();
	   cols = getDataColumns();
	   data = new int[rows][cols];
	}
	   
   public static void populateDiscreteValues(List<String> listData, int row){
	   int pos = -1;
	   int index = 0;
	   int length = 0;
	   
	   for(int i=0;i<listData.size();i++){
		   LinkedHashMap<String, Integer> lhash = hashCategory.get(i);
		   if(lhash.containsKey(listData.get(i))){
			   pos = lhash.get(listData.get(i));
		   }
		   
		   length += lhash.size();
		   
		   for(int j=index, k=0;j<length;j++,k++){
			   if(k==pos)
				   data[row][j]=1;
			   else
				   data[row][j]=0;
			   	   
		   }
		   
		   index+=lhash.size();
	   }
   }
	   
   public static void buildTransactions(){
	   for(int i=0;i<rows;i++){
		   List<String> listData = hashData.get(i+1);
		   populateDiscreteValues(listData, i);
	   }
   }
   
   public static void displayData(){
	   for(int i=0;i<data.length;i++){
		   for(int j=0;j<data[i].length;j++){
			   System.out.print(data[i][j]+" ");
		   }
		   System.out.println();
	   }
   }
	
	/*public static void main(String[] args) throws Exception{
		init();

		String filename = "car.data";
		String sep = ",";
		boolean hasHeader = false;
		String path = curDir+"\\"+filename;
		
		readData(path, sep, hasHeader);

	}*/

}
