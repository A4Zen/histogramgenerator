package betabyter.histogramgenerator;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


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

    String keyFile;
    byte[] keyStream;

    private List<String> fileList = new ArrayList<>();

    //Array for converting byte array to printable format
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

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
}