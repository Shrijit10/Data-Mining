import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Similarity {
	static List<UserDist> listUserDist; 
	static HashMap<String, List<String>> hashUserDist;
	static float wtGenre;
	static float wtRating;
	static float wtAgeGender;
	static float MAD;
	
	public static void init(float wtGenre, float wtRating, float wtAgeGender){
		listUserDist = new ArrayList<UserDist>();
		hashUserDist = new HashMap<String, List<String>>();
		Similarity.wtGenre = wtGenre;
		Similarity.wtRating = wtRating;
		Similarity.wtAgeGender = wtAgeGender;
	}
	
	public static float getMAD(float true_rating, float predicted_rating){
		MAD += Math.abs(true_rating - predicted_rating);
		
		return MAD;
	}
	
	/*
	 * Below method uses Cosine Similarity to find similarity between users using movie genre
	 */
	public static float getSimUserGenres(String user, String otherUser){
		Set<String> setGenres1 = ModifiedRecoDist.hashUserGenres.get(user);
		Iterator<String> itGenres1 = setGenres1.iterator();
		
		Set<String> setGenres2 = ModifiedRecoDist.hashUserGenres.get(otherUser);
		float dotproduct = 0;
		
		while(itGenres1.hasNext()){
		   String genre = itGenres1.next();
		    	
		   if(setGenres2.contains(genre))
		    	 dotproduct++;
		}
			
		return (float)(dotproduct/Math.sqrt(setGenres1.size())/Math.sqrt(setGenres2.size()));
	}
	
	/*
	 * Below method categorizes the age for computing similarity
	 */
	public static int getCategory(int age){
		if(age < 18)
			return 1;
		else if(age>=18 && age<=40)
			 return 2;
		else if(age>40 && age<=60)
			return 3;
		else return 4;
	}
	
	/*
	 * Below method uses Cosine Similarity to find similarity between users using age and gender 
	 */
	public static float getSimAgeGender(String user, String otherUser){
		
		if(ModifiedRecoDist.hashUserInfo.size()==0)
		   return 0;
			
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
		
		return (float)(dotproduct/Math.sqrt(listUser.size())/Math.sqrt(listOtherUser.size()));
					
	}
	
	/*
	 * Below method is used to normalize the different parameters to bring it to scale of 0 to 1.
	 */
	public static float getNormRatingDist(float ratingDist, float minRating, float maxRating){
		return (ratingDist - minRating)/(maxRating-minRating);
	}
	
	
	public static void setUserTotalDist(float minRating, float maxRating){
		for(UserDist u : listUserDist){
			float normRatingDist = getNormRatingDist(u.ratingDist, minRating, maxRating);
			u.setRatingDist(1-normRatingDist);
			
			float totalDist = u.ratingDist*wtRating + u.genreDist*wtGenre + u.ageGenderDist*wtAgeGender;
					           
			u.setTotalDist(totalDist);
		}
	}
	
	/*
	 * Below method builds the list of users who are closest to the given user. When the isModifiedDist parameter is
	 * passed as true, then genre, age and gender information is also included in the calculation.
	 */
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
	 
				  HashMap<String, Float> hash = RecommendDistance.hashMovieUserRating.get(movie);
				  float user_rating = hash.get(userid);
				  
				  if(hash.containsKey(otherUser)){
					 flag = true;
					 float other_user_rating = hash.get(otherUser);
					  
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
	  
	/*
	 * Below method gets the K similar users. This is done by building a list of users similar to the given user.
	 * The boolean parameter is for incorporating genre, age and gender.
	 * When using only the ratings, the distance list(listUserDist) is sorted in ascending order & 
	 * it is sorted in descending order when the genre, age and gender parameters are included.
	 */
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
		  
		  List<String> list = new ArrayList<String>();
		  for(UserDist u : listUserDist)
			  list.add(u.user);
		  
		  hashUserDist.put(userid, list);
		  listUserDist.clear();
			  
	   }
	  
	 public static float getModeRating(){
		  Set<Float> setRating = RecommendDistance.hashRatingCount.keySet();
		  Iterator<Float> it = setRating.iterator();
		  int max = -1;
		  float mode = 3f;
			 
		  while(it.hasNext()){
			float key = it.next();
			int value = RecommendDistance.hashRatingCount.get(key);
				 
			if(value>=max){
			   max = value;
			   mode = key;
		     }
		   }
			 
		   return mode;
	  }
	  
	  public static int getAvgRating(){
		  Set<Float> setRating = RecommendDistance.hashRatingCount.keySet();
		  Iterator<Float> it = setRating.iterator();
		  float sum = 0;
		  int default_rating = 3;
		  int valueCount = 0;
		  
		  while(it.hasNext()){
			float key = it.next();
			int value = RecommendDistance.hashRatingCount.get(key);
				 
			sum+= key*value;
			valueCount += value;
		   }
		
		   if(sum==0)
			   return default_rating;
		   
		   return Math.round(sum/valueCount);
	  }
	  
	  public static int getMedianRating(){
		  List<Float> list = new ArrayList<Float>();
		  Set<Float> setRating = RecommendDistance.hashRatingCount.keySet();
		  Iterator<Float> it = setRating.iterator();
		  int default_rating = 3;
		  
		  if(setRating.size()==0)
			  return default_rating;
		  
		  while(it.hasNext()){
			  float key = it.next();
			  int value = RecommendDistance.hashRatingCount.get(key);
			  
			  for(int i=0;i<value;i++)
				  list.add(key);
		  }
		  
		  Collections.sort(list);
		  if(list.size()%2!=0)
			  return Math.round(list.get(list.size()/2));
		  else
			  return Math.round((list.get(list.size()/2) + list.get((list.size()-1)/2))/2); 
	  }
	  
	  public static float predictUserRating(String userid, String movieid, int k, String evalType){
		  int count = 0;
		  
		  if(!RecommendDistance.hashMovieUserRating.containsKey(movieid) || !hashUserDist.containsKey(userid))
			  return 3; // default rating of 3 given as it has minimum distance to other ratings if movie or user is present in... 
		                // test set but not in train set
		  
		  RecommendDistance.hashRatingCount.clear();
		  List<String> listUsers = hashUserDist.get(userid);
		  
		  for(int i=0;i<listUsers.size();i++){
			  
			  String simUser = listUsers.get(i);
			  Set<String> setMovies = RecommendDistance.hashUserMovie.get(simUser);
			  
			  if(setMovies.contains(movieid)){
				count++;
				float rating = RecommendDistance.hashMovieUserRating.get(movieid).get(simUser);
					 
				if(RecommendDistance.hashRatingCount.containsKey(rating))
					RecommendDistance.hashRatingCount.put(rating, RecommendDistance.hashRatingCount.get(rating)+1);
				else
					RecommendDistance.hashRatingCount.put(rating, 1);
			   }
			  
			  if(count==k)
				break;
			  
		  }
		  
		  // 'K' similar users not found. Get users who have the seen the movie until 'K' users are found.
		  if(count < k){
			 HashMap<String, Float> hash = RecommendDistance.hashMovieUserRating.get(movieid);
			 Set<String> setUsers = hash.keySet();
			 Iterator<String> itUsers = setUsers.iterator();
			 
			 while(itUsers.hasNext()){
				 String key = itUsers.next();
				 float rating = hash.get(key);
				 
				 if(RecommendDistance.hashRatingCount.containsKey(rating))
					RecommendDistance.hashRatingCount.put(rating, RecommendDistance.hashRatingCount.get(rating)+1);
				 else
					RecommendDistance.hashRatingCount.put(rating, 1);
				 
				 count++;
				 
				 if(count == k)
				   break;
			 }
			 
		  } 
		  
		  if(evalType.equals("Mode"))
		     return getModeRating();
		  else if(evalType.equals("Avg"))
		         return getAvgRating();
		  else
			  return getMedianRating();
		  
	  }

}
