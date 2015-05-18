package io.wyrmise.hanusync;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;

public class MainActivity extends ActionBarActivity implements CourseAdapter.OnItemClickListener, NavAdapter.OnItemClickListener {

    private CharSequence title;
    String TITLES[] = {"General News", "Courses", "Submissions", "Settings", "Log out"};
    int ICONS[] = {R.drawable.ic_action_copy,R.drawable.ic_action_copy,R.drawable.ic_action_paste, R.drawable.ic_action_settings, R.drawable.ic_action_undo};
    String NAME = "";
    String ID = "";
    private Toolbar toolbar;
    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout Drawer;                                  // Declaring DrawerLayout
    ActionBarDrawerToggle mDrawerToggle;
    private Map<String, String> cookies;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size


        mLayoutManager = new LinearLayoutManager(MainActivity.this);    // Creating a layout Manager

        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager


        Drawer = (DrawerLayout) findViewById(R.id.drawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, Drawer, toolbar, R.string.drawer_open, R.string.drawer_close) {

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
        }; // Drawer Toggle Object Made
        Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State

        Intent intent = getIntent();
        ID = intent.getStringExtra("id");
        new GetInformation().execute();

        if (savedInstanceState == null) {
            getGeneralNews(1);
        }

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
        switch(position){
            case 1:
                getGeneralNews(position);
                break;
            case 2:
                selectItem(position);
                break;
            case 3:
                selectItem(position);
                break;
            case 4:

                break;
            case 5:
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
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.commit();
        setTitle(TITLES[position - 1]);
        Drawer.closeDrawer(mRecyclerView);
    }

    private void showLogOutDialog() {
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
        Drawer.closeDrawer(mRecyclerView);
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
            progressDialog.setMessage("Loging out, please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Document document = Jsoup.connect("http://fit.hanu.edu.vn/fitportal/").cookies(cookies).get();
                Elements featureName = document.select("div.header-profileoptions").select("ul").select("li").select("a[href]");
                String url = featureName.get(2).attr("abs:href");
                System.out.println("Get url: "+url);
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
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(i);
            } else {
                Toast.makeText(getApplicationContext(),"There's an error while trying to logging out!",Toast.LENGTH_LONG).show();
            }
        }
    }


}
