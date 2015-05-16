package io.wyrmise.hanusync;

import android.app.ProgressDialog;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity implements CourseAdapter.OnItemClickListener{

    String TITLES[] = {"Settings"};
    int ICONS[] = {R.drawable.ic_action_settings};
    String NAME = "";
    String ID = "";

    private Toolbar toolbar;                              // Declaring the Toolbar Object
    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout Drawer;                                  // Declaring DrawerLayout
    ActionBarDrawerToggle mDrawerToggle;

    private Map<String,String> cookies;
    private Map<String,String> urls;

    ProgressDialog mProgressDialog;

    RecyclerView courseList;
    CourseAdapter courseAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new GetNameAndID().execute();

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size


        mLayoutManager = new LinearLayoutManager(MainActivity.this);    // Creating a layout Manager

        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager


        Drawer = (DrawerLayout) findViewById(R.id.drawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,Drawer,toolbar,R.string.drawer_open, R.string.drawer_close){

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

        courseList = (RecyclerView) findViewById(R.id.courseList);
        courseList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        courseList.setLayoutManager(llm);

    }

    private class GetNameAndID extends AsyncTask<Void,Void,ArrayList<String>>{

        protected void onPreExecute(){
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
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

                ArrayList<String> courses = new ArrayList<>();
                urls = new HashMap<>();

                Elements courseName = document.select("div.content").select("ul.list").select("li.r0,li.r1").select("a[href]");
                for(Element e: courseName){
                    courses.add(e.text());
                    urls.put(e.text(),e.attr("abs:href"));
                }

                return courses;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            mProgressDialog.dismiss();
            mAdapter = new NavAdapter(TITLES,ICONS,NAME,ID);
            mRecyclerView.setAdapter(mAdapter);
            courseAdapter = new CourseAdapter(result,MainActivity.this);
            courseList.setAdapter(courseAdapter);
        }
    }

    private int getIndexOfFirstNumber(String str){
        char[] arr = str.toCharArray();
        for(int i=0; i<arr.length;i++){
            if(Character.isDigit(arr[i]))
                return i;
        }
        return -1;
    }

    public void onClick(View view, int position){
        selectItem(position);
    }

    private void selectItem(int position) {
        String name = courseAdapter.get(position);
        if(urls.containsKey(name)){
            Intent intent = new Intent(MainActivity.this,CourseActivity.class);
            String subject = name.substring(name.lastIndexOf(": ")+2,name.length());
            intent.putExtra("subject",subject);
            intent.putExtra("url",urls.get(name));
            intent.putExtra("cookies", (java.io.Serializable) cookies);
            MainActivity.this.startActivity(intent);
        } else {
            Toast.makeText(MainActivity.this,"There's an error",Toast.LENGTH_LONG);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
