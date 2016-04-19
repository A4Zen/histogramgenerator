package betabyter.histogramgenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class DataPoint {
	private int timeSinceEpoch;
	private float displacementNorth;
	private float displacementEast;
	//MACs of other users who are in range at this time point
	private List<byte[]> MACsInRange;
	public static float distance(DataPoint A,DataPoint B){
		float result;
		float northdiff = A.north() - B.north();
		float eastdiff = A.east() - B.east();
		result = northdiff*northdiff + eastdiff*eastdiff;
		result = (float) Math.sqrt(result);
		return result;
	}
	public float north(){
		return this.displacementNorth;
	}
	public float east(){
		return this.displacementEast;
	}
	public int time(){
		return this.timeSinceEpoch;
	}
	public List<byte[]> getMACsInRange(){
		return MACsInRange;
	}
	public boolean isMACinRange(byte[] mac){
		int size = MACsInRange.size();
		for(int i = 0;i<size;i++){
			if(Arrays.equals(mac,MACsInRange.get(i))){
				return true;
			}
		}
		return false;
	}
	public void shift(float east, float north){
		this.displacementEast+=east;
		this.displacementNorth+=north;
	}
	public DataPoint(float east,float north,int time, Set<byte[]> inRange){
		displacementEast = east;
		displacementNorth = north;
		timeSinceEpoch = time;
		MACsInRange = new ArrayList<byte[]>();
		MACsInRange.addAll(inRange);
	}
}
