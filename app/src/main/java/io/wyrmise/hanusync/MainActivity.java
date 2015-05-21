package io.wyrmise.hanusync;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class MainActivity extends ActionBarActivity implements CourseAdapter.OnItemClickListener, NavAdapter.OnItemClickListener {

    private CharSequence title;
    String TITLES[] = {"General News", "Marktable Collection", "Courses", "Submissions", "Settings", "About", "Log out"};
    int ICONS[] = {R.drawable.ic_news, R.drawable.ic_mark, R.drawable.ic_courses, R.drawable.ic_submission, R.drawable.ic_settings, R.drawable.ic_about, R.drawable.ic_exit};
    String NAME = "";
    String ID = "";
    private Toolbar toolbar;
    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout drawerLayout;                                  // Declaring DrawerLayout
    ActionBarDrawerToggle mDrawerToggle;
    private Map<String, String> cookies;
    SharedPreferences prefs;
    ImageView header_image;

    SharedPreferences.OnSharedPreferenceChangeListener myPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            //the toolbar color
            if (key.equals(SettingsActivity.KEY_ENTRY_NUM)) {
                reload();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size


        mLayoutManager = new LinearLayoutManager(MainActivity.this);    // Creating a layout Manager

        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager


        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);        // drawerLayout object Assigned to the view
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }
        }; // drawerLayout Toggle Object Made
        drawerLayout.setDrawerListener(mDrawerToggle); // drawerLayout Listener set to the drawerLayout toggle
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State

        Intent intent = getIntent();
        ID = intent.getStringExtra("id");
        new GetInformation().execute();

        if (savedInstanceState == null) {
            getGeneralNews(1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        NavAdapter.activityResult(this,requestCode,resultCode,data);
    }

    private class GetInformation extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Intent intent = getIntent();
                cookies = (Map<String, String>) intent.getSerializableExtra("cookies");
                Document document = Jsoup.connect("http://fit.hanu.edu.vn/fitportal/").cookies(cookies).get();
                Element featureName = document.select("div.header-profilename").first();
                featureName.select("a[href]");
                String url = featureName.text();
                int t = getIndexOfFirstNumber(url);
                NAME = url.substring(0, t);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mAdapter = new NavAdapter(TITLES, ICONS, NAME, ID, MainActivity.this);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    private int getIndexOfFirstNumber(String str) {
        char[] arr = str.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            if (Character.isDigit(arr[i]))
                return i;
        }
        return -1;
    }

    public void onClick(View view, int position) {
        switch (position) {
            case 1:
                getGeneralNews(position);
                break;
            case 2:
                getGeneralNews(position);
                break;
            case 3:
                selectItem(position);
                break;
            case 4:
                selectItem(position);
                break;
            case 5:
                drawerLayout.closeDrawer(mRecyclerView);
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case 6:
                drawerLayout.closeDrawer(mRecyclerView);
                Intent about = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(about);
                break;
            case 7:
                showLogOutDialog();
                break;
        }
    }

    private void getGeneralNews(int position) {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putInt("news_type", position);
        fragment.setArguments(args);
        fragment.newInstance(position);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment,"NEWS");
        fragmentTransaction.commit();
        setTitle(TITLES[position - 1]);
        drawerLayout.closeDrawer(mRecyclerView);
    }

    private void showLogOutDialog() {
        drawerLayout.closeDrawer(mRecyclerView);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure you want to log out?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new LogOutAsyncTask().execute();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(myPrefListener);
    }

    private void reload(){
        NewsFragment myFragment = (NewsFragment) getFragmentManager().findFragmentByTag("NEWS");
        if (myFragment != null && myFragment.isVisible()) {
            myFragment.reload();
        }
    }


    private void selectItem(int position) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt("option_number", position);
        fragment.setArguments(args);
        fragment.newInstance(position);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.commit();
        setTitle(TITLES[position - 1]);
        drawerLayout.closeDrawer(mRecyclerView);
    }

    public void setTitle(CharSequence title) {
        this.title = title;
        getSupportActionBar().setTitle(this.title);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private class LogOutAsyncTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog progressDialog;

        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Logging out, please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Document document = Jsoup.connect("http://fit.hanu.edu.vn/fitportal/").cookies(cookies).get();
                Elements featureName = document.select("div.header-profileoptions").select("ul").select("li").select("a[href]");
                String url = featureName.get(2).attr("abs:href");
                Jsoup.connect(url).cookies(cookies).get();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                LoginActivity.isLoggedOut = true;
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(i);
            } else {
                Toast.makeText(getApplicationContext(), "There's an error while trying to log out!", Toast.LENGTH_LONG).show();
            }
        }
    }


}
