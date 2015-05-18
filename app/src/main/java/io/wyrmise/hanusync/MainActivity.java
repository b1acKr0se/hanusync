package io.wyrmise.hanusync;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Map;

public class MainActivity extends ActionBarActivity implements CourseAdapter.OnItemClickListener, NavAdapter.OnItemClickListener {

    private CharSequence title;
    String TITLES[] = {"Courses","Submissions","Settings"};
    int ICONS[] = {R.drawable.ic_action_settings,R.drawable.ic_action_settings,R.drawable.ic_action_settings};
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

        new GetInformation().execute();

        if (savedInstanceState == null) {
            selectItem(1);
        }

    }

    private class GetInformation extends AsyncTask<Void,Void,Void>{

        protected void onPreExecute(){
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Intent intent = getIntent();
                cookies = (Map<String,String>)intent.getSerializableExtra("cookies");
                Document document = Jsoup.connect("http://fit.hanu.edu.vn/fitportal/").cookies(cookies).get();
                Element featureName = document.select("div.header-profilename").first();
                featureName.select("a[href]");
                String url = featureName.text();
                int t = getIndexOfFirstNumber(url);
                NAME = url.substring(0,t);
                ID = intent.getStringExtra("id");

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mAdapter = new NavAdapter(TITLES,ICONS,NAME,ID,MainActivity.this);
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
        selectItem(position);

    }

    private void selectItem(int position) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt("option_number",position);
        fragment.setArguments(args);
        fragment.newInstance(position);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame,fragment);
        fragmentTransaction.commit();
        setTitle(TITLES[position-1]);
        Drawer.closeDrawer(mRecyclerView);
    }

    public void setTitle(CharSequence title){
        this.title = title;
        getSupportActionBar().setTitle(this.title);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


}
