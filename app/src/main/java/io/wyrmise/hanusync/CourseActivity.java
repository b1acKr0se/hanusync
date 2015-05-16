package io.wyrmise.hanusync;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;


public class CourseActivity extends ActionBarActivity{


    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ContentAdapter adapter;
    private ProgressBar progressBar;
    private Map<String,String> cookies;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        recyclerView = (RecyclerView) findViewById(R.id.courseList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        new GetContent().execute();
    }

    private class GetContent extends AsyncTask<Void,Void,ArrayList<Content>> {

        protected void onPreExecute(){
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected ArrayList<Content> doInBackground(Void... params) {
            try {
                Intent intent = getIntent();
                cookies = (Map<String,String>)intent.getSerializableExtra("cookies");

                String url = intent.getStringExtra("url");

                ArrayList<Content> contents = new ArrayList<>();

                Document document = Jsoup.connect(url).cookies(cookies).get();
                for(int i = 1; i<20;i++) {
                    Element weekly_content = document.select("table.weeks").select("tr#section-"+i).select("td.content").first();
                    Content content = new Content();
                    content.date = weekly_content.select("h3.weekdates").text();
                    content.summary = document.select("table.weeks").select("tr#section-"+i).select("td.content").select("div.summary").text();
                    contents.add(content);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Content> result) {
            progressBar.setVisibility(ProgressBar.GONE);
            recyclerView.setVisibility(RecyclerView.VISIBLE);

            adapter = new ContentAdapter(result);
            recyclerView.setAdapter(adapter);
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_course, menu);
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
}
