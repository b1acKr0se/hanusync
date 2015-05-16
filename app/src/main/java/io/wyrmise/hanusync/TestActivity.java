package io.wyrmise.hanusync;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


public class TestActivity extends ActionBarActivity {

    private String name;
    private String id;
    private TextView mTextView;
    private Toolbar mToolbar;
    private ProgressDialog mProgressDialog;
    private Map<String,String> cookies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mTextView = (TextView) findViewById(R.id.nameTextView);

        new GetNameAndID().execute();


    }

    private class GetNameAndID extends AsyncTask<Void,Void,String>{
        private String course = "";

        protected void onPreExecute(){
            mProgressDialog = new ProgressDialog(TestActivity.this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Intent intent = getIntent();
                cookies = (Map<String,String>)intent.getSerializableExtra("cookies");
                Document document = Jsoup.connect("http://fit.hanu.edu.vn/fitportal/").cookies(cookies).get();
                Element featureName = document.select("div.header-profilename").first();
                featureName.select("a[href]");
                String url = featureName.text();

                Elements courseName = document.select("div.content").select("ul.list").select("li.r0,li.r1").select("a[href]");
                for(Element e: courseName){
                    course += e.text()+"("+e.attr("abs:href")+")\n";
                }
                course += "Upcoming Events:\n";
                Elements eventName = document.select("div.content").select("div.event").select("a[href]");
                for(Element e: eventName){
                    course += e.text()+"\n";
                }



                return course;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mTextView.setText(result);
            mProgressDialog.dismiss();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
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
