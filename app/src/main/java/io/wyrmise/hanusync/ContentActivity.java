package io.wyrmise.hanusync;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

import com.liuguangqiang.swipeback.SwipeBackActivity;
import com.liuguangqiang.swipeback.SwipeBackLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;


public class ContentActivity extends SwipeBackActivity {


    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ContentAdapter adapter;
    private ProgressBar progressBar;
    private Map<String, String> cookies;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_course);

        setDragEdge(SwipeBackLayout.DragEdge.LEFT);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String name = intent.getStringExtra("subject");
        setTitle(name);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        recyclerView = (RecyclerView) findViewById(R.id.courseList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        recyclerView.setOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                hideViews();
            }

            @Override
            public void onShow() {
                showViews();
            }
        });

        new GetContent().execute();
    }

    private void hideViews() {
        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    private void showViews() {
        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    private class GetContent extends AsyncTask<Void, Void, ArrayList<Content>> {

        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected ArrayList<Content> doInBackground(Void... params) {
            try {
                Intent intent = getIntent();
                cookies = (Map<String, String>) intent.getSerializableExtra("cookies");

                String url = intent.getStringExtra("url");

                ArrayList<Content> contents = new ArrayList<>();

                Document document = Jsoup.connect(url).cookies(cookies).get();

                Elements weekly_content = document.select("table.weeks").select("h3.weekdates");

                Content first = new Content();
                contents.add(first);

                for (Element e : weekly_content) {
                    Content content = new Content();
                    content.date = br2nl(e.html());
                    contents.add(content);
                }

                Elements weekly_summary = document.select("table.weeks").select("div.summary");

                try {
                    for (int i = 0; i < weekly_summary.size(); i++) {
                        Content content = contents.get(i);
                        String summary = br2nl(weekly_summary.get(i).html());
                        if (summary.equals("")) content.summary = "No summary";
                        else content.summary = summary;
                    }
                } catch (IndexOutOfBoundsException e) {

                }
                return contents;
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

    public static String br2nl(String html) {
        if (html == null)
            return html;
        Document document = Jsoup.parse(html);

        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        document.outputSettings().charset("UTF-8");
        document.select("br").append("\\n");
        document.select("p").prepend("\\n");
        String s = document.html().replaceAll("\\\\n", "\n");

        Document.OutputSettings outputSettings = new Document.OutputSettings();
        outputSettings.escapeMode(Entities.EscapeMode.xhtml);
        outputSettings.charset("UTF-8");
        outputSettings.prettyPrint(false);

        String str = Jsoup.clean(s, "", Whitelist.none(), outputSettings);
        str = str.replaceAll("&quot;", "\"")
                .replaceAll("&apos;", "\'")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("[\\s&&[^\\n]]+", " ")
                .replaceAll("(?m)^\\s|\\s$", "")
                .replaceAll("\\n+", "\n")
                .replaceAll("^\n|\n$", "");
        return str;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}