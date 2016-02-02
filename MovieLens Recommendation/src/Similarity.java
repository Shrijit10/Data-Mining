import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Similarity {
	static List<UserDist> listUserDist; 
	static float wtGenre;
	static float wtRating;
	static float wtAgeGender;
	static float MAD;
	
	public static void init(float wtGenre, float wtRating, float wtAgeGender){
		listUserDist = new ArrayList<UserDist>();
		Similarity.wtGenre = wtGenre;
		Similarity.wtRating = wtRating;
		Similarity.wtAgeGender = wtAgeGender;
	}
	
	public static float getMAD(int true_rating, int predicted_rating){
		MAD += Math.abs(true_rating - predicted_rating);
		
		return MAD;
	}
	
	public static float getSimUserGenres(String user, String otherUser){
		Set<Integer> setGenres1 = ModifiedRecoDist.hashUserGenres.get(user).keySet();
		Iterator<Integer> itGenres1 = setGenres1.iterator();
		
		Set<Integer> setGenres2 = ModifiedRecoDist.hashUserGenres.get(otherUser).keySet();
		float dotproduct = 0;
		
		while(itGenres1.hasNext()){
		   Integer genre = itGenres1.next();
		    	
		   if(setGenres2.contains(genre))
		    	 dotproduct++;
		}
			
		return (float)(dotproduct/Math.sqrt(setGenres1.size())/Math.sqrt(setGenres2.size()) * wtGenre);
	}
	
	public static int getCategory(int age){
		if(age < 18)
			return 1;
		else if(age>=18 && age<=40)
			 return 2;
		else if(age>40 && age<=60)
			return 3;
		else return 4;
	}
	
	public static float getSimAgeGender(String user, String otherUser){
		List<Integer> listUser = ModifiedRecoDist.hashUserInfo.get(user);
		List<Integer> listOtherUser = ModifiedRecoDist.hashUserInfo.get(otherUser);
		float dotproduct = 0;
		
		int userGender = listUser.get(0);
		int userAge = listUser.get(1);
		int userCategory = getCategory(userAge);
		
		if(userGender == listOtherUser.get(0))
			dotproduct++;
		
		if(userCategory == getCategory(listOtherUser.get(1)))
			dotproduct++;
		
		return (float)(dotproduct/Math.sqrt(listUser.size())/Math.sqrt(listOtherUser.size()) * wtAgeGender);
					
	}
	
	public static float getNormRatingDist(float ratingDist, float minRating, float maxRating){
		return (ratingDist - minRating)/(maxRating-minRating);
	}
	
	public static void setUserTotalDist(float minRating, float maxRating){
		for(UserDist u : listUserDist){
			float normRatingDist = getNormRatingDist(u.ratingDist, minRating, maxRating);
			u.setRatingDist(1-normRatingDist);
			
			float totalDist = (u.ratingDist*wtRating + u.genreDist*wtGenre + u.ageGenderDist*wtAgeGender)
					           /(wtRating + wtGenre + wtAgeGender);
			u.setTotalDist(totalDist);
		}
	}
	
	public static void buildListUserDist(String userid, String metric, boolean isModifiedDist){
		  Set<String> setUsers = RecommendDistance.hashUserMovie.keySet();
		  Set<String> setUserMovies = RecommendDistance.hashUserMovie.get(userid);
		  
		  Iterator<String> it1 = setUsers.iterator();
		  
		  float sum = 0;
		  float ratingDist = 0;
		  float max = -1;
		  float minRating = Float.MAX_VALUE;
		  float maxRating = Float.MIN_VALUE;
		  boolean flag = false;
		  
		  while(it1.hasNext()){
			  String otherUser = it1.next();
			  
			  if(!otherUser.equals(userid)){
				Iterator<String> it2 = setUserMovies.iterator();  
			    
				sum = 0;
				flag = false;
				max = -1;
				while(it2.hasNext()){
				  String movie = it2.next();
	 
				  HashMap<String, Integer> hash = RecommendDistance.hashMovieUserRating.get(movie);
				  int user_rating = hash.get(userid);
				  
				  //System.out.println(movie+","+user_rating);
				  
				  if(hash.containsKey(otherUser)){
					 flag = true;
					 int other_user_rating = hash.get(otherUser);
					  
					 switch(metric){
					    case "E": sum+= RecommendDistance.getEuclideanDist(user_rating, other_user_rating);
					              break;
					              
					    case "M": sum+= RecommendDistance.getManhattanDist(user_rating, other_user_rating);
			                      break;
			                      
					    case "L": sum = RecommendDistance.getLmaxDist(user_rating, other_user_rating);
					              if(sum>=max)
					            	  max = sum;
					              break;
					  }
					}
				  }
				
				 
				  if(flag){
					if(metric.equals("E"))
						ratingDist = (float)Math.sqrt(sum);
					else if (metric.equals("M"))
						ratingDist = sum;
					else if(metric.equals("L"))
						ratingDist = max;
							  
					
					
					if(!isModifiedDist)
					   listUserDist.add(new UserDist(otherUser, ratingDist));
					else{
					   float genreDist =  getSimUserGenres(userid, otherUser);
					   float ageGenderDist = getSimAgeGender(userid, otherUser);	
					   listUserDist.add(new UserDist(otherUser, ratingDist, genreDist, ageGenderDist));
					   if(ratingDist >= maxRating)
						   maxRating = ratingDist;
					   
					   if(ratingDist <= minRating)
						   minRating = ratingDist;
					}
				  }
				 
			   }
			}
		  
		   if(isModifiedDist)
			   setUserTotalDist(minRating, maxRating);
			   
			   
	  }
	  
	  public static void getKSimilarUsers(String userid, String metric, boolean isModifiedDist){
		  buildListUserDist(userid, metric, isModifiedDist);
		  
		  Collections.sort(listUserDist, new Comparator<UserDist>(){
			  @Override
			  public int compare(UserDist u1, UserDist u2){
				  if(u1.totalDist == u2.totalDist)
					  return 0;
				  else if(!isModifiedDist)
					     return u1.totalDist < u2.totalDist ? -1:1;
				  else 
					     return u1.totalDist > u2.totalDist ? -1:1;
				  
			  }
		  });
		  
		  //for(UserDist u : listUserDist)
			 //System.out.println("User: "+u.user+", Dist: "+u.dist);
			  
	   }
	  
	  public static int getModeRating(){
		  Set<Integer> setRating = RecommendDistance.hashRatingCount.keySet();
		  Iterator<Integer> it = setRating.iterator();
		  int max = -1;
		  int mode = -1;
			 
		  while(it.hasNext()){
			int key = it.next();
			int value = RecommendDistance.hashRatingCount.get(key);
				 
			if(value>=max){
			   max = value;
			   mode = key;
		     }
		   }
			 
		   return mode;
	  }
	  
	  public static int predictUserRating(String userid, String movieid, int k){
		  int count = 0;
		  RecommendDistance.hashRatingCount.clear();
		  
		  for(int i=0;i<listUserDist.size();i++){
			  
			  UserDist u = listUserDist.get(i);
			  Set<String> setMovies = RecommendDistance.hashUserMovie.get(u.user);
			  
			  if(setMovies.contains(movieid)){
				count++;
				int rating = RecommendDistance.hashMovieUserRating.get(movieid).get(u.user);
					 
				if(RecommendDistance.hashRatingCount.containsKey(rating))
					RecommendDistance.hashRatingCount.put(rating, RecommendDistance.hashRatingCount.get(rating)+1);
				else
					RecommendDistance.hashRatingCount.put(rating, 1);
			   }
			  
			  if(count==k)
				break;
			  
		  }
		  
		  return getModeRating();
		  
	  }

}
