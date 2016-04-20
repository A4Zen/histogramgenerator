package betabyter.histogramgenerator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistogramFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistogramFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistogramFragment extends Fragment {
    private static String LOGTAG = "HistogramFragment";

    private static int RESULT_LOAD_SOURCE = 1;
    private static int RESULT_LOAD_REF = 2;
    private String sourcePath;
    private String refPath;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HistogramFragment.
     */
    public static HistogramFragment newInstance() {
        HistogramFragment fragment = new HistogramFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public HistogramFragment() {
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
        return inflater.inflate(R.layout.fragment_histogram, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        final TextView resView = (TextView) v.findViewById(R.id.resultView);

        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        Button buttonLoadSource = (Button) v.findViewById(R.id.loadPictureButton);
        buttonLoadSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean isSource = true;
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                getActivity().startActivityForResult(i, RESULT_LOAD_SOURCE);
            }
        });

        Button buttonLoadRef = (Button) v.findViewById(R.id.loadRefButton);
        buttonLoadRef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean isSource = false;
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                getActivity().startActivityForResult(i, RESULT_LOAD_REF);
            }
        });

        Button buttonAnalyze = (Button) v.findViewById(R.id.analyzeButton);
        buttonAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                File source = new File(sourcePath);
                File ref = new File(refPath);

                // Create the source and reference image.
                Bitmap SourceImage = decodeFile(source);
                Bitmap refImage = decodeFile(ref);

                // Create Mats for the HSV values of the reference and source.
                Mat hsvRef = new Mat();
                Mat hsvSource = new Mat();

                // Convert reference and source bitmaps into Mats.
                Mat srcRef = new Mat(refImage.getHeight(), refImage.getWidth(), CvType.CV_8U, new Scalar(4));
                Utils.bitmapToMat(refImage, srcRef);
                Mat srcSource = new Mat(SourceImage.getHeight(), SourceImage.getWidth(), CvType.CV_8U, new Scalar(4));
                Utils.bitmapToMat(SourceImage, srcSource);

                /// Convert to HSV
                Imgproc.cvtColor(srcRef, hsvRef, Imgproc.COLOR_BGR2HSV);
                Imgproc.cvtColor(srcSource, hsvSource, Imgproc.COLOR_BGR2HSV);

                /// Using 200 bins for hue and 240 for saturation
                int hBins = 150;
                int sBins = 190;
                MatOfInt histSize = new MatOfInt(hBins, sBins);

                // hue varies from 0 to 179, saturation from 0 to 255
                MatOfFloat ranges = new MatOfFloat(0f, 180f, 0f, 256f);

                // we compute the histogram from the 0-th and 1-st channels
                MatOfInt channels = new MatOfInt(0, 1);

                Mat histRef = new Mat();
                Mat histSource = new Mat();

                ArrayList<Mat> histImages = new ArrayList<>();
                histImages.add(hsvRef);
                Imgproc.calcHist(histImages, channels, new Mat(), histRef, histSize, ranges, false);
                Core.normalize(histRef, histRef, 0, 1, Core.NORM_MINMAX, -1, new Mat());

                histImages = new ArrayList<>();
                histImages.add(hsvSource);
                Imgproc.calcHist(histImages, channels, new Mat(), histSource, histSize, ranges, false);
                Core.normalize(histSource, histSource, 0, 1, Core.NORM_MINMAX, -1, new Mat());

                // Compares the two histograms and reports the result to the resultView. Note that
                // the compare threshold can be edited to be more lax by decreasing the threshold,
                // and vice versa.
                double compareResult = Imgproc.compareHist(histSource, histRef, Imgproc.CV_COMP_CORREL);
                if (compareResult < 0) {
                    compareResult = 0.0;
                }
                Log.d("Analyze:", "Compare Result was: " + compareResult);
                resView.setText("Correlation Result: \n" + compareResult);
            }
        });
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

    private Bitmap decodeFile(File f){
        Bitmap b;
        Bitmap error = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        FileInputStream fis;
        int IMAGE_MAX_SIZE = 100;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        o.inPreferredConfig = Bitmap.Config.ARGB_8888;

        try {
            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
        } catch (FileNotFoundException e) {
            Log.d("Analyze:", "Unable to find the file ." + f.getAbsolutePath());
            e.printStackTrace();
            return error;
        }

        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = (int)Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        try {
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
        } catch (FileNotFoundException e) {
            Log.d("Analyze:", "Unable to find the file ." + f.getAbsolutePath());
            e.printStackTrace();
            return error;
        }

        try {
            fis.close();
        } catch (IOException e) {
            Log.d("Analyze:", "Unable to close the file input stream.");
            e.printStackTrace();
            System.exit(0);
        }
        return b;
    }

    public void setSourcePath(String source) {
        sourcePath = source;
    }

    public void setRefPath(String ref) {
        refPath = ref;
    }
}