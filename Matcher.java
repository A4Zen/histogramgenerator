import java.awt.List;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

class Matcher {
	private static byte[] MACstringToByteArray(String s) {
		String macAddress = s;
		String[] macAddressParts = macAddress.split(":");
		// convert hex string to byte values
		byte[] macAddressBytes = new byte[6];
		for(int i=0; i<6; i++){
		    Integer hex = Integer.parseInt(macAddressParts[i], 16);
		    macAddressBytes[i] = hex.byteValue();
		}
		return macAddressBytes;
	}

	private static boolean[] test() {
		// TODO: populate test data
		
		byte[] MAC_A = MACstringToByteArray("AA:BB:CC:DD:EE:FF");
		byte[] MAC_B = MACstringToByteArray("FF:EE:DD:CC:BB:AA");
		ArrayList<DataPoint> A_testdata = new ArrayList<>();
		ArrayList<DataPoint> B_testdata = new ArrayList<>();
		Set<byte[]> in_range_of_A = new HashSet<>();
		Set<byte[]> in_range_of_B = new HashSet<>();
		in_range_of_A.add(MAC_B);
		in_range_of_B.add(MAC_A);
		int i = 0;
		for (; i < 1; i++) {
			DataPoint forA = new DataPoint(i, 0, i, in_range_of_A);
			DataPoint forB = new DataPoint(i*-1,0,i,in_range_of_B);
			A_testdata.add(forA);
			B_testdata.add(forB);
		}
		in_range_of_A = new HashSet<>();
		in_range_of_B = new HashSet<>();
		for (; i < 7; i++) {
			DataPoint forA = new DataPoint(i, 0, i, in_range_of_A);
			DataPoint forB = new DataPoint(i*-1,0,i,in_range_of_B);
			A_testdata.add(forA);
			B_testdata.add(forB);
		}
		Path A = new Path(MAC_A, A_testdata, 10f);
		Path B = new Path(MAC_B, B_testdata, 10f);
		int precision = 12;
		return Path.validDirections(A, B, precision);
	}
	public static void main(String[] args) {
		boolean[] result = test();
		PrintWriter out = new PrintWriter(System.out, true);
		out.println("=============RESULTS=================");
		for(int i=0;i<result.length;i++){
			out.println(Integer.toString(i));
			if(result[i]){
				out.println("True");
			}else{
				out.println("False");
			}
		}
	}
}
