import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ModifiedRecoDist {
    final static String movieFile = "u.item";
    final static String userFile = "u.user";
    static HashMap<String, Set<String>> hashMovieGenres;
    static HashMap<String, Set<String>> hashUserGenres;
    static HashMap<String, List<Integer>> hashUserInfo;
	final static String split = "\t";
    
    public static void init(float wtRating, float wtGenre, float wtAgeGender){
    	RecommendDistance.init(wtRating, wtGenre, wtAgeGender);
    	
    	hashMovieGenres = new HashMap<String, Set<String>>();
    	hashUserGenres = new HashMap<String, Set<String>>();
    	hashUserInfo = new HashMap<String, List<Integer>>();
    }
    
    public static void readUserData(String fileName) throws Exception{
    	BufferedReader br = new BufferedReader(new FileReader(fileName));
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
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String s= "";

		while((s = br.readLine())!=null){
			String[] temp = s.split("\\|");
			String movie_id = temp[0];
			
			for(int i=5;i<temp.length;i++){
				if(temp[i].equals("1")){
					
					if(!hashMovieGenres.containsKey(movie_id)){
						Set<String> setGenres = new HashSet<String>();
						setGenres.add(i+"");
						hashMovieGenres.put(movie_id, setGenres);
					}
					else{
						Set<String> setGenres = hashMovieGenres.get(movie_id);
						setGenres.add(i+"");
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
			
			HashMap<String, Float> hashUserRating = RecommendDistance.hashMovieUserRating.get(movie);
			Set<String> setUsers = hashUserRating.keySet();
			Iterator<String> itUsers = setUsers.iterator();
			
			Set<String> setGenres = hashMovieGenres.get(movie);
			
			while(itUsers.hasNext()){
				String user = itUsers.next();
				
				if(!hashUserGenres.containsKey(user)){
					Set<String> set = new HashSet<String>();
					set.addAll(setGenres);
					hashUserGenres.put(user, set);
				 }
				else{
					Set<String> set = hashUserGenres.get(user);
					set.addAll(setGenres);
					hashUserGenres.put(user, set);
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
		String metric = "E";
		float wtRating = 0.3f;       // weights are changed manually to test different combinations
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
		String path = RecommendDistance.curDir+"\\" ;  // when running in unix, then path = "/l/b565/ml-100k/";
		
		readMovieData(movieFile);  // when running in UNIX, then readMovieData(path+movieFile)  [path = "/l/b565/ml-100k/"]
		readUserData(userFile);    // when running in UNIX, then readMovieData(path+userFile) 
		
		for(int j : list){
			k = j;
			System.out.println("K: "+k);
			  
			for(int i=1;i<=5;i++){
			  fileName = path+"u"+i+".base";  
			  RecommendDistance.readRatingData(fileName, split);
			  
			  RecommendDistance.generateSimilarUserList(metric, true);
			    
			  fileName = path+"u"+i+".test";
			  RecommendDistance.readTestData(fileName, split, metric, k, true, evalType);
			    
			  RecommendDistance.reset(); 
			  
			  //System.out.println("MAD "+i+": "+Similarity.MAD);
			}
			
			System.out.println("MAD: "+Similarity.MAD/100000);
			Similarity.MAD = 0;
			System.out.println("=============================");
		}
		
	}
}
