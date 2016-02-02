import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RecommendDistance {
  static HashMap<String, Set<String>> hashUserMovie;
  static HashMap<String, HashMap<String, Integer>> hashMovieUserRating;
  static HashMap<Integer, Integer> hashRatingCount;
  static String curDir;
  
  public static void init(float wtRating, float wtGenre, float wtAgeGender){
	  hashUserMovie = new HashMap<String, Set<String>>();
	  hashMovieUserRating = new HashMap<String, HashMap<String, Integer>>();
	  hashRatingCount = new HashMap<Integer, Integer>();
	  curDir = System.getProperty("user.dir");
	  
	  Similarity.init(wtRating, wtGenre, wtAgeGender);
  }
  
  public static void readTestData(String fileName, String metric, int k, boolean isModified) throws IOException{
	  BufferedReader br = new BufferedReader(new FileReader(curDir+"\\"+fileName));
	  String s = "";
	  int count=0;
	  
	  while((s=br.readLine())!=null){
		  String[] temp = s.split("\t");
		  String userid = temp[0];
		  String movieid = temp[1];
		  int true_rating = Integer.parseInt(temp[2]);
		  
		  if(isModified)
			  ModifiedRecoDist.buildHashUserGenres(userid);
		  
		  Similarity.getKSimilarUsers(userid, metric, isModified);
		  int predicted_rating = Similarity.predictUserRating(userid, movieid, k);
		  
		  System.out.println("Count: "+count+", True Rating: "+true_rating+", Predicted Rating: "+predicted_rating);
		  Similarity.getMAD(true_rating, predicted_rating);
		  count++;
	  }
	  
	  Similarity.MAD = Similarity.MAD/count;
	  System.out.println("MAD: "+Similarity.MAD);
	  
  }
  
  public static void readRatingData(String fileName) throws IOException{
	  BufferedReader br = new BufferedReader(new FileReader(curDir+"\\"+fileName));
	  HashMap<String, Integer> hashUserRating;
	  Set<String> setMovies;
	  String s = "";
	  
	  while((s=br.readLine())!=null){
		  String[] temp = s.split("\t");
		  String userid = temp[0];
		  String movieid = temp[1];
		  int rating = Integer.parseInt(temp[2]);
		  
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
			  hashUserRating = new HashMap<String, Integer>();
			  hashUserRating.put(userid, rating);
			  hashMovieUserRating.put(movieid, hashUserRating);  
		  }
		  
	  }
	  
	  br.close();
	  
  }
  
  public static float getEuclideanDist(int rating1, int rating2){
	  return (float) Math.pow(rating1 - rating2, 2);
   }
  
  public static float getManhattanDist(int rating1, int rating2){
	  return (float) Math.abs(rating1-rating2);
   }
  
  public static float getLmaxDist(int rating1, int rating2){
	  return (float) Math.abs(rating1-rating2);
  } 
  
  public static void reset(){
	  hashUserMovie.clear();
	  hashMovieUserRating.clear();
	  hashRatingCount.clear();
	  Similarity.listUserDist.clear();
  }
    
  public static void main(String[] args) throws IOException{
	  String fileName = "";
	  String metric = "E";
	  float wtRating = 3;
	  float wtGenre = 2;
	  float wtAgeGender = 1;
	  int k = 50;
	  
	  init(wtRating, wtGenre, wtAgeGender);
	  
	  for(int i=1;i<=5;i++){
		fileName = "u"+i+".base";  
	    readRatingData(fileName);
	    
	    fileName = "u"+i+".test";
	    readTestData(fileName, metric, k, false);
	    
	    reset();
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