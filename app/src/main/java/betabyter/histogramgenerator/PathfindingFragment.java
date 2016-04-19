package betabyter.histogramgenerator;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PathfindingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PathfindingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PathfindingFragment extends Fragment {
    private static String LOGTAG = "PathfindingFragment";

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PathfindingFragment.
     */
    public static PathfindingFragment newInstance() {
        PathfindingFragment fragment = new PathfindingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public PathfindingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pathfinding, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        final TextView resultView = (TextView) v.findViewById(R.id.resultView);

        boolean[] result = test();
        resultView.setText("");
        for(int i=0; i < result.length; i++){
            resultView.append(Integer.toString(i));
            if(result[i]){
                resultView.append(" True\n");
            }else{
                resultView.append(" False\n");
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        ((MainActivity) activity).onSectionAttached(1);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
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
}