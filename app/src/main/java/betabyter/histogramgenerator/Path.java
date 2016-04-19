package betabyter.histogramgenerator;

import java.util.ArrayList;

class Path {
	//<(N,E,T),...
	private ArrayList<DataPoint> history;
	private static float potentialErrorPerTimeUnit = 0f;
	private float detectionRange = 0f;
	private byte[] MAC;
	
	public byte[] getMAC(){
		return MAC;
	}
	public float getDetectionRange(){
		return detectionRange;
	}

	public DataPoint get(int index){
		return history.get(index);
	}
	public int size(){
		return history.size();
	}
	public void shift(float east,float north){
		int size = history.size();
		for(int i = 0;i<size;i++){
			history.get(i).shift(east,north);
		}
	}
	
	public int mostRecentTime(){
		if (this.size() < 1) return -1;
		return this.get(0).time();
	}
	public float leastRecentTime(){
		if (this.size() < 1) return -1f;
		return this.get(this.size()-1).time();
	}
	private static boolean isInvalidPair(Path A, Path B){
		//TODO: for testing purposes assume the starting times are the same
		//also assume that the sizes are the same
		//also assume time is sampled at same rate
		assert A.mostRecentTime() == B.mostRecentTime();
		assert A.size() == B.size();
		
		int baseTime = A.mostRecentTime();
		int size = A.size();
		int timeSinceBase = 0;
		for(int i=0; i < size; i++){
			timeSinceBase = A.get(i).time() - baseTime;
			float distance = DataPoint.distance(A.get(i), B.get(i));
			System.out.println("    current path depth: " + Integer.toString(i));
			System.out.println("    distance: " + Float.toString(distance));
			if(A.get(i).isMACinRange(B.getMAC())){
				System.out.println("        B IS in A's MAC list");
				if(distance > A.getDetectionRange() + potentialErrorPerTimeUnit*((float)timeSinceBase)){
					System.out.println("            ... but it shouldn't be based on distance NOT VALID");
					return true;
				}
			}else{
				System.out.println("        B IS NOT in A's MAC list");
				if(distance < A.getDetectionRange() - potentialErrorPerTimeUnit){
					System.out.println("            ... but it should be based on distance NOT VALID");
					return true;
				}
			}
			
		}
		return false;
	}
	private static void calculateDesiredLocation(float[] location,float[] base,int precision,int current,float radius){
		location[0] = base[0];
		location[1] = base[1]+radius;
		float downshift = (float) (radius - Math.cos(((float)current)*2*Math.PI/precision)*radius);
		location[1] -= downshift;
		float eastshift = (float) Math.sin(((float)current)*2*Math.PI/precision) * radius;
		location[0] += eastshift;
	}
	public static boolean[] validDirections(Path A, Path B, int precision){
		boolean[] result;
		Path copyOfB; 
		result = new boolean[precision];
		for(int i = 0;i<precision;i++){
			result[i] = true;
		}
		float[] shiftBsMostRecentPointHere = new float[2];
		float[] AmostRecent = new float[2];
		float[] shiftAmount = new float[2];
		AmostRecent[0] = A.get(0).east();
		AmostRecent[1] = A.get(0).north();
		for(int i = 0;i < precision;i++){
			copyOfB = new Path(B.getMAC(),B.history,B.getDetectionRange());
			calculateDesiredLocation(shiftBsMostRecentPointHere,AmostRecent,precision,i,A.getDetectionRange());
			shiftAmount[0] = shiftBsMostRecentPointHere[0]-copyOfB.get(0).east();
			shiftAmount[1] = shiftBsMostRecentPointHere[1]-copyOfB.get(0).north();
			copyOfB.shift(shiftAmount[0], shiftAmount[1]);
			System.out.println("testing at precision point "+Integer.toString(i));
			if(isInvalidPair(A,copyOfB)){
				result[i] = false;
			}
		}
		return result;
	}
	public Path(byte[] myMAC, ArrayList<DataPoint> input, float detect){
		potentialErrorPerTimeUnit = 0.01f;
		detectionRange = detect;
		int size = myMAC.length;
		MAC = new byte[size];
		for(int i=0;i<size;i++){
			MAC[i]=myMAC[i];
		}
		history = new ArrayList<DataPoint>();
		if(input!=null && input.size()>0){
			history.addAll(input);
		}		
		//TODO: sort by time (most recent at front)
	}
}