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
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;


public class ThreadActivity extends SwipeBackActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ThreadAdapter adapter;
    private ProgressBar progressBar;
    private Map<String, String> cookies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);

        setDragEdge(SwipeBackLayout.DragEdge.LEFT);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("View Topic");

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        recyclerView = (RecyclerView) findViewById(R.id.commentList);
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

        new ExtractTopic().execute();
    }

    private void hideViews() {
        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    private void showViews() {
        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    private class ExtractTopic extends AsyncTask<Void, Void, ArrayList<Comment>> {

        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected ArrayList<Comment> doInBackground(Void... params) {
            try {
                Intent intent = getIntent();
                cookies = (Map<String, String>) intent.getSerializableExtra("cookies");

                String url = intent.getStringExtra("topic");

                ArrayList<Comment> comments = new ArrayList<>();

                Document document = Jsoup.connect(url).cookies(cookies).get();

                Element table = document.select("table.forumpost").first();

                for (Element row : table.select("tr.header")) {
                    Comment c = new Comment();
                    c.subject = row.select("div.subject").text();
                    c.author = row.select("div.author").text();
                    comments.add(c);
                }

                Elements table_content = document.select("table.forumpost").select("tr").select("td.content");
                Elements attachments = document.select("table.forumpost").select("tr").select("td.content").select("div.attachments").select("a[href]");


                for (int i = 0; i < comments.size(); i++) {
                    Comment c = comments.get(i);
                    c.content = table_content.get(i).select("div.posting").text();
                    if (attachments.attr("abs:href").length() > 0) {
                        c.hasAttachment = true;
                        String link = attachments.attr("abs:href");
                        String text = attachments.text();
                        c.attachment = "<a href=\"" + link + "\">" + text + "</a>";
                    } else {
                        c.hasAttachment = false;
                    }
                }
                return comments;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Comment> result) {
            progressBar.setVisibility(ProgressBar.GONE);
            recyclerView.setVisibility(RecyclerView.VISIBLE);
            if (result != null) {
                adapter = new ThreadAdapter(result);
                recyclerView.setAdapter(adapter);
            }
        }
    }

    public static String br2nl(String html) {
        if (html == null)
            return html;
        Document document = null;
        try {
            document = Jsoup.parse(new URL(html).openStream(), "UTF-8", html);
        } catch (IOException e) {
            e.printStackTrace();
        }
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));
        document.select("br").append("\\n");
        document.select("p").prepend("\\n");
        String s = document.html().replaceAll("\\\\n", "\n");
        return Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
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