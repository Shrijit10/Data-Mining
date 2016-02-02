import java.util.HashMap;

public class demo {
  boolean hasSeen;
  int a;
  static HashMap<String, HashMap<Integer, Integer>> hash = new HashMap<String, HashMap<Integer, Integer>>();
  
  public static void main(String[] args){
	  demo d = new demo();
	  
	  HashMap<Integer, Integer> hash1 = new HashMap<Integer, Integer>();
	  hash1.put(10, 10);
	  hash1.put(20, 40);
	  hash1.put(30, 50);
	  
	  hash.put("a", hash1);
	  hash.put("b", hash1);
	  
	  hash.clear();
	  System.out.println(hash.size());
  }
  
}
