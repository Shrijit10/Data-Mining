import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RecommendDistance {
  static HashMap<String, Set<String>> hashUserMovie;
  static HashMap<String, HashMap<String, Float>> hashMovieUserRating;
  static HashMap<Float, Integer> hashRatingCount;
  static String curDir;
  final static String split = "\t";
  
  public static void init(float wtRating, float wtGenre, float wtAgeGender){
	  hashUserMovie = new HashMap<String, Set<String>>();
	  hashMovieUserRating = new HashMap<String, HashMap<String, Float>>();
	  hashRatingCount = new HashMap<Float, Integer>();
	  curDir = System.getProperty("user.dir");
	  
	  Similarity.init(wtRating, wtGenre, wtAgeGender);
  }
  
  public static void reset(){
	  hashUserMovie.clear();
	  hashMovieUserRating.clear();
	  hashRatingCount.clear();
	  Similarity.hashUserDist.clear();
	  Similarity.listUserDist.clear();
  }
  
  public static float getEuclideanDist(float rating1, float rating2){
	  return (float) Math.pow(rating1 - rating2, 2);
   }
  
  public static float getManhattanDist(float rating1, float rating2){
	  return (float) Math.abs(rating1-rating2);
   }
  
  public static float getLmaxDist(float rating1, float rating2){
	  return (float) Math.abs(rating1-rating2);
  }
  
  public static void readTestData(String fileName, String split, String metric, int k, boolean isModified, String evalType) throws IOException{
	  BufferedReader br = new BufferedReader(new FileReader(fileName));
	  String s = "";
	  int count=0;
	  
	  while((s=br.readLine())!=null){
		  String[] temp = s.split(split);
		  String userid = temp[0];
		  String movieid = temp[1];
		  float true_rating = Float.parseFloat(temp[2]);
		  
		  float predicted_rating = Similarity.predictUserRating(userid, movieid, k, evalType);
		  
		  //System.out.println("Count: "+count+", User: "+userid+", Movie: "+movieid+", Error: "+Math.abs(predicted_rating-true_rating));
		  Similarity.getMAD(true_rating, predicted_rating);
		  count++;
	  }
  }
  
  public static void readRatingData(String fileName, String split) throws IOException{
	  BufferedReader br = new BufferedReader(new FileReader(fileName));
	  HashMap<String, Float> hashUserRating;
	  Set<String> setMovies;
	  String s = "";
	  
	  while((s=br.readLine())!=null){
		  String[] temp = s.split(split);
		  String userid = temp[0];
		  String movieid = temp[1];
		  float rating = Float.parseFloat(temp[2]);
		  
		  if(hashUserMovie.containsKey(userid)){
			  setMovies = hashUserMovie.get(userid);
			  setMovies.add(movieid);
			  hashUserMovie.put(userid, setMovies);
		  }
		  else{
			  setMovies = new HashSet<String>();
			  setMovies.add(movieid);
			  hashUserMovie.put(userid, setMovies);
		  }
		  
		  if(hashMovieUserRating.containsKey(movieid)){
			  hashUserRating = hashMovieUserRating.get(movieid);
			  hashUserRating.put(userid, rating);
			  hashMovieUserRating.put(movieid, hashUserRating);
		  }
		  else{
			  hashUserRating = new HashMap<String, Float>();
			  hashUserRating.put(userid, rating);
			  hashMovieUserRating.put(movieid, hashUserRating);  
		  }
		  
	  }
	  
	  br.close();
	  
  }
  
  /*
   * Below method generates a list of similar user for the given user. 
   * The boolean parameter is used for incorporating the movie genre, age and gender.
   */
  public static void generateSimilarUserList(String metric, boolean isModified){
	  Set<String> setUsers = hashUserMovie.keySet();
	  Iterator<String> itUsers = setUsers.iterator();
	  
	  while(itUsers.hasNext()){
		 String userid = itUsers.next(); 
		 if(isModified)
			ModifiedRecoDist.buildHashUserGenres(userid);
	     
		 Similarity.getKSimilarUsers(userid, metric, isModified);
	  }
  }
  
 
    
  public static void main(String[] args) throws IOException{
	  
	  String fileName = "";
	  String metric = "L";
	  float wtRating = 0.3f;
	  float wtGenre = 0.6f;
	  float wtAgeGender = 0.1f;
	  int k;
	  String evalType = "Avg";
	  
	  System.out.println("Enter 1 for Euclidean Distance, 2 for Manhattan and 3 for Lmax: ");
	  BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	  String option = br.readLine();
	  
	  if(option.equals("1"))
		  metric = "E";
	  else if(option.equals("2"))
		  metric = "M";
	  else if(option.equals("3"))
		  metric = "L";
	  else{
		  System.out.println("Invalid option entered...System aborting");
		  return;
	  }
	  
	  System.out.println("Enter 1 to use Average, 2 to use Mode to predict the rating from set of similar users:");
	  option = br.readLine();
	  if(option.equals("1"))
		  evalType = "Avg";
	  else if(option.equals("2"))
		  metric = "Mode";
	  else{
		  System.out.println("Invalid option entered...System aborting");
		  return;
	  }
	  
	  List<Integer> list = new ArrayList<Integer>();
	  list.add(40);
	  list.add(50);
	  list.add(60);
	  
	  init(wtRating, wtGenre, wtAgeGender);
	  String path = curDir+"\\" ;  //"/l/b565/ml-100k/";
	  
	  for(int j : list){
		  k = j;
		  System.out.println("K: "+k);
		  
		  for(int i=1;i<=5;i++){
			fileName = path+"u"+i+".base";  
		    readRatingData(fileName, split);
		    
		    generateSimilarUserList(metric, false);
		    
		    fileName = path+"u"+i+".test";
		    readTestData(fileName, split, metric, k, false, evalType);
		    
		    reset();
		    
		    //System.out.println("MAD "+i+": "+Similarity.MAD);
		  }
		  
		  System.out.println("MAD: "+Similarity.MAD/100000);
		  Similarity.MAD = 0;
		  System.out.println("=============================");
	  }
	  
	}
	
}

class UserDist{
	String user;
	float ratingDist;
	float genreDist;
	float ageGenderDist;
	float totalDist;
	
	UserDist(String user, float ratingDist, float genreDist, float ageGenderDist){
		this.user = user;
		this.ratingDist = ratingDist;
		this.genreDist = genreDist;
		this.ageGenderDist = ageGenderDist;
	}
	
	UserDist(String user, float totalDist){
		this.user = user;
		this.totalDist = totalDist;
	}
	
	public void setTotalDist(float dist){
		totalDist = dist;
	}
	
	public void setRatingDist(float dist){
		ratingDist = dist;
	}
	
	public float getRatingDist(){
		return ratingDist;
	}
	
	public float getGenreDist(){
		return genreDist;
	}
	
	public float getAgeGenderDist(){
		return ageGenderDist;
	}
}