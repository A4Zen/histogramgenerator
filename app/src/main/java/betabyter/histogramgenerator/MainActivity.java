package betabyter.histogramgenerator;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static int RESULT_LOAD_IMAGE = 1;
    private String sourcePath;
    private String refPath;
    private boolean isSource = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView srcView = (ImageView) findViewById(R.id.sourceView);
        ImageView refView = (ImageView) findViewById(R.id.refView);
        final TextView resView = (TextView) findViewById(R.id.resultView);

        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        Button buttonLoadSource = (Button) findViewById(R.id.loadPictureButton);
        buttonLoadSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                isSource = true;
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        Button buttonLoadRef = (Button) findViewById(R.id.loadRefButton);
        buttonLoadRef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                isSource = false;
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        Button buttonAnalyze = (Button) findViewById(R.id.analyzeButton);
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

                /// Using 50 bins for hue and 60 for saturation
                int hBins = 50;
                int sBins = 60;
                MatOfInt histSize = new MatOfInt( hBins,  sBins);

                // hue varies from 0 to 179, saturation from 0 to 255
                MatOfFloat ranges =  new MatOfFloat( 0f,180f,0f,256f );

                // we compute the histogram from the 0-th and 1-st channels
                MatOfInt channels = new MatOfInt(0, 1);

                Mat histRef = new Mat();
                Mat histSource = new Mat();

                ArrayList<Mat> histImages=new ArrayList<>();
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
                Log.d("Analyze:", "Compare Result was: " + compareResult);
                if (compareResult >= .50) {
                    resView.setText("Sure Match!");
                } else if (compareResult >= .20 && compareResult < .50)  {
                    resView.setText("Images are likely of the same people.");
                }
                else {
                    resView.setText("Images are of different people.");
                }

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picPath = cursor.getString(columnIndex);
            cursor.close();
            if(isSource) {
                sourcePath = picPath;
                ImageView imageView = (ImageView) findViewById(R.id.sourceView);
                imageView.setImageBitmap(BitmapFactory.decodeFile(sourcePath));
            } else {
                refPath = picPath;
                ImageView imageView = (ImageView) findViewById(R.id.refView);
                imageView.setImageBitmap(BitmapFactory.decodeFile(refPath));
            }
        }
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
}
