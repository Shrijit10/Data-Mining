
public class Dimensions {

	public static float evalFun(float dmax, float dmin){
		return (float) Math.log10((dmax - dmin)/dmin);
	}
	
	public static float getRk(float[][] matrix, int k){
		float sum = 0f;
		float dist = 0f;
		float dmax = Float.MIN_VALUE;
		float dmin = Float.MAX_VALUE;
		
		for(int i=0;i<matrix.length-1;i++){
			for(int count=i+1;count<matrix.length;count++){
				sum = 0;
				for(int j=0;j<=k;j++){
					sum+=Math.pow(matrix[i][j] - matrix[count][j],2);
				}
				
				dist = (float) Math.sqrt(sum);
				//System.out.println("dist: "+dist);
				
				if(dist >= dmax)
					dmax = dist;
				
				if(dist <= dmin && dist!=0)
					dmin = dist;
			}
		}
		
		//System.out.println("dmax: "+dmax+", dmin: "+dmin);
		//System.out.println("******************************************");
		return evalFun(dmax, dmin);
	}
	
	
	public static void populateMatrix(float[][] matrix){
		float[] runs = new float[10];
		
		for(int k=0;k<100;k++){
		  for(int run=0;run<runs.length;run++){
			for(int i=0;i<matrix.length;i++){
			  for(int j=0;j<=k;j++){
				matrix[i][j] = (float)Math.random();
			  }
		    }
			
			//displayMatrix(matrix);
			//System.out.println("rk: "+ getRk(matrix, k));
		    runs[run] = getRk(matrix, k);
		
		    
		    
		  }
		  
		  //System.out.println("K: "+k);
		  System.out.println(getAvgRk(runs));
		  //displayRuns(runs);
		  //System.out.println("----------------------------------");
		}
		
	}
	
	public static float getAvgRk(float[] runs){
		float sum = 0f;
		for(float run : runs)
		  	sum+=run;
		
		return sum/runs.length;
	}
	
	public static void displayRuns(float[] runs){
		for(float run : runs){
			System.out.println(run);
		}
	}
	
	public static void displayMatrix(float[][] matrix){
		for(int i=0;i<matrix.length;i++){
			for(int j=0;j<matrix.length;j++){
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static void main(String[] args){
		float[][] matrix = new float[10000][100];
		
		populateMatrix(matrix);
	}
}
