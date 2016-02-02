import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ModifiedRecoDist {
    final static String movieFile = "u.item";
    final static String userFile = "u.user";
    static HashMap<String, Set<Integer>> hashMovieGenres;
    static HashMap<String, HashMap<Integer, Integer>> hashUserGenres;
    static HashMap<String, List<Integer>> hashUserInfo;
    
    public static void init(){
    	hashMovieGenres = new HashMap<String, Set<Integer>>();
    	hashUserGenres = new HashMap<String, HashMap<Integer, Integer>>();
    	hashUserInfo = new HashMap<String, List<Integer>>();
    }
    
    public static void readUserData(String fileName) throws Exception{
    	BufferedReader br = new BufferedReader(new FileReader(RecommendDistance.curDir+"\\"+fileName));
		String s= "";
		
		while((s=br.readLine())!=null){
			String[] temp = s.split("\\|");
			String userid = temp[0];
			
			List<Integer> list = new ArrayList<Integer>();
			list.add(temp[2].equals("M")?1:0);
			list.add(Integer.parseInt(temp[1]));
			
			hashUserInfo.put(userid, list);
			
		}
    }
	
	public static void readMovieData(String fileName) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(RecommendDistance.curDir+"\\"+fileName));
		String s= "";

		while((s = br.readLine())!=null){
			String[] temp = s.split("\\|");
			String movie_id = temp[0];
			
			for(int i=5;i<temp.length;i++){
				if(temp[i].equals("1")){
					
					if(!hashMovieGenres.containsKey(movie_id)){
						Set<Integer> setGenres = new HashSet<Integer>();
						setGenres.add(i);
						hashMovieGenres.put(movie_id, setGenres);
					}
					else{
						Set<Integer> setGenres = hashMovieGenres.get(movie_id);
						setGenres.add(i);
						hashMovieGenres.put(movie_id, setGenres);
					}
				}
					
			}
		}
		
	}
	
	public static void buildHashUserGenres(String userid){
		Set<String> setMovies = RecommendDistance.hashUserMovie.get(userid);
		Iterator<String> itMovies = setMovies.iterator();
		
		while(itMovies.hasNext()){
			String movie = itMovies.next();
			
			HashMap<String, Integer> hashUserRating = RecommendDistance.hashMovieUserRating.get(movie);
			Set<String> setUsers = hashUserRating.keySet();
			Iterator<String> itUsers = setUsers.iterator();
			
			Set<Integer> setGenres = hashMovieGenres.get(movie);
			
			while(itUsers.hasNext()){
				String user = itUsers.next();
				Iterator<Integer> itGenres = setGenres.iterator();
				
				if(!hashUserGenres.containsKey(user)){
					HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
					
					while(itGenres.hasNext())
						hash.put(itGenres.next(), 1);
					
					hashUserGenres.put(user, hash);
				 }
				else{
					HashMap<Integer, Integer> hash = hashUserGenres.get(user);
					
					while(itGenres.hasNext()){
						Integer genre = itGenres.next();
						
						if(hash.containsKey(genre))
						   hash.put(genre, hash.get(genre)+1);
						else
						   hash.put(genre, 1);
						
					}
					hashUserGenres.put(user, hash);
				}
				
			}
		}
	}
	
	public static void reset(){
		RecommendDistance.reset();
		hashMovieGenres.clear();
		hashUserGenres.clear();
		hashUserInfo.clear();
	}
	
	public static void main(String[] args) throws Exception{
		String fileName = "";
		String metric = "M";
		float wtRating = 3;
		float wtGenre = 2;
		float wtAgeGender = 1;
		int k = 50;
		
		RecommendDistance.init(wtRating, wtGenre, wtAgeGender);
		init();
		
		readMovieData(movieFile);
		readUserData(userFile);
		
		for(int i=1;i<=5;i++){
			fileName = "u"+i+".base";  
			RecommendDistance.readRatingData(fileName);
		    
		    fileName = "u"+i+".test";
		    RecommendDistance.readTestData(fileName, metric, k, true);
		    
		    RecommendDistance.reset();
		  }
		
	}
}
