package io.wyrmise.hanusync.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import io.wyrmise.hanusync.objects.Comment;
import io.wyrmise.hanusync.HidingScrollListener;
import io.wyrmise.hanusync.swipe.MySwipeBackActivity;
import io.wyrmise.hanusync.R;
import io.wyrmise.hanusync.adapters.ThreadAdapter;
import me.imid.swipebacklayout.lib.SwipeBackLayout;


public class ThreadActivity extends MySwipeBackActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ThreadAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean swipe_back = sharedPreferences.getBoolean(SettingsActivity.ENABLE_SWIPE_BACK, true);

        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        if (swipe_back)
            mSwipeBackLayout.setEnableGesture(true);
        else
            mSwipeBackLayout.setEnableGesture(false);

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

                String url = intent.getStringExtra("topic");

                ArrayList<Comment> comments = new ArrayList<>();

                Document document = Jsoup.connect(url).get();

                Elements table = document.select("table.forumpost").select("tr.header");
                System.out.println(table.size());
                Elements table_content = document.select("table.forumpost").select("tr").not("tr.header");
                System.out.println(table_content.size());
                Elements attachments = document.select("table.forumpost").select("tr").not("tr.header");
                System.out.println(attachments.size());
                for (int i = 0; i < table.size(); i++) {
                    Comment c = new Comment();
                    c.subject = table.get(i).select("div.subject").text();
                    c.author = table.get(i).select("div.author").text();

                    c.content = br2nl(table_content.get(i).select("td.content > div.posting").html());
                    if (attachments.get(i).select("td.content").select("div.attachments").select("a[href]").attr("abs:href").length() > 0) {
                        c.hasAttachment = true;
                        String link = attachments.get(i).select("td.content").select("div.attachments").select("a[href]").attr("abs:href");
                        String text = attachments.get(i).select("td.content").select("div.attachments").select("a[href]").text();
                        c.attachment = "<a href=\"" + link + "\">" + text + "</a>";
                    } else {
                        c.hasAttachment = false;
                    }
                    comments.add(c);
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
            } else
                Toast.makeText(getApplicationContext(), "There's an error while trying to access the topic!", Toast.LENGTH_LONG).show();
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
                .replaceAll("(?m)(^ *| +(?= |$))", "")
                .replaceAll("(?m)^$([\r\n]+?)(^$[\r\n]+?^)+", "$1");
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