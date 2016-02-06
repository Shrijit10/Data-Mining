import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Recommend10M {
     final static String movieFile = "movies.dat";
	 final static String split = "::";
	 
	 public static void init(float wtRating, float wtGenre, float wtAgeGender){
    	 ModifiedRecoDist.init(wtRating, wtGenre, wtAgeGender);
     }
	 
	 public static void readMovieData10M(String fileName, String split) throws IOException{
		 BufferedReader br = new BufferedReader(new FileReader(RecommendDistance.curDir+"\\"+fileName));
	     String s= "";

		 while((s = br.readLine())!=null){
			String[] temp = s.split(split);
			String movie_id = temp[0];
			String genres = temp[2];
			
			temp = genres.split("\\|");
			
			for(int i=0;i<temp.length;i++){
               if(!ModifiedRecoDist.hashMovieGenres.containsKey(movie_id)){
				   Set<String> setGenres = new HashSet<String>();
				   setGenres.add(temp[i]);
				   ModifiedRecoDist.hashMovieGenres.put(movie_id, setGenres);
				}
			   else{
					 Set<String> setGenres = ModifiedRecoDist.hashMovieGenres.get(movie_id);
					 setGenres.add(temp[i]);
					 ModifiedRecoDist.hashMovieGenres.put(movie_id, setGenres);
				   }
			 }
		 }
	 }
	
	 public static void main(String[] args) throws IOException{
		 
		 String fileName = "";
		 String metric = "E";
		 float wtRating = 0.3f;    // weights are changed manually
		 float wtGenre = 0.6f;
		 float wtAgeGender = 0.1f;
		 int k = 40;  // value of k changed manually...for ease of running in hulk            
		 String evalType = "Avg";  // kept constant...Average gave better results than Mode
		 
		 init(wtRating, wtGenre, wtAgeGender);
		 String path = RecommendDistance.curDir+"\\"; // "/l/b565/ml-10M100K/";
		 
	     readMovieData10M(movieFile, split);
	     
	     for(int i=1;i<=5;i++){
		   fileName = path+"r"+i+".train";  
		   RecommendDistance.readRatingData(fileName, split);
		
		   RecommendDistance.generateSimilarUserList(metric, true);
		   fileName = path+"r"+i+".test";
		   RecommendDistance.readTestData(fileName, split, metric, k, true, evalType);
			    
		   RecommendDistance.reset(); 
		   
		   //System.out.println("MAD "+i+": "+Similarity.MAD);
		 }
	     System.out.println("MAD: "+Similarity.MAD/10000000);
		 Similarity.MAD = 0;
		 System.out.println("=============================");
		 
	 }
}
