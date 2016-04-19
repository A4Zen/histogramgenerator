package betabyter.histogramgenerator;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private String TAG = "Main Activity";
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    private static int RESULT_LOAD_SOURCE = 1;
    private static int RESULT_LOAD_REF = 2;
    private String sourcePath;
    private String refPath;

    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

    }

    //NAVIGATION DRAWER
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        Log.d(TAG, "Position: " + position);

        Fragment fragment;
        if (position == 0) {
            fragment = HistogramFragment.newInstance();
        } else {
            fragment = PathfindingFragment.newInstance();
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment).commit();

    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.com_hist);
                break;
            case 2:
                mTitle = getString(R.string.pathfind);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    void processIntent(Intent intent) {
        Log.d(TAG, "Intent is being processed.");

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // Called when the user selects an image in the Histogram Fragment.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Image selected in Histogram Fragment.");

        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picPath = cursor.getString(columnIndex);
        cursor.close();
        HistogramFragment fragment = (HistogramFragment)
                getSupportFragmentManager().findFragmentById(R.id.container);
        if(requestCode == RESULT_LOAD_SOURCE) {
            sourcePath = picPath;
            fragment.setSourcePath(sourcePath);
            ImageView imageView = (ImageView) findViewById(R.id.sourceView);
            imageView.setImageBitmap(BitmapFactory.decodeFile(sourcePath));
        } else {
            refPath = picPath;
            fragment.setRefPath(refPath);
            ImageView imageView = (ImageView) findViewById(R.id.refView);
            imageView.setImageBitmap(BitmapFactory.decodeFile(refPath));
        }
    }
}

