package io.wyrmise.hanusync;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
    private String course_url = "";
    private String grade_url = "";


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

                course_url = intent.getStringExtra("url");

                ArrayList<Content> contents = new ArrayList<>();

                Document document = Jsoup.connect(course_url).cookies(cookies).get();

                Element grade = document.select("div.block_admin.sideblock").select("div.content").select("ul.list").select("li.r0").select("div.column.c1").select("a[href]").first();
                grade_url = grade.attr("abs:href");

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
            if(result!=null) {
                adapter = new ContentAdapter(result);
                recyclerView.setAdapter(adapter);
            } else {
                Toast.makeText(getApplicationContext(),"There's an error while trying to read the course!",Toast.LENGTH_LONG).show();
            }
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

    public void displayGrade() {
        if (!grade_url.equals("")) {
            new RetrieveGrade().execute();
        }
    }

    private class RetrieveGrade extends AsyncTask<Void, Void, ArrayList<Grade>> {
        ProgressDialog progressDialog;

        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ContentActivity.this);
            progressDialog.setMessage("Getting your grade...");
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected ArrayList<Grade> doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect(grade_url).cookies(cookies).get();

                ArrayList<Grade> grades = new ArrayList<>();

                for (Element table : doc.select("table.boxaligncenter.generaltable.user-grade")) {
                    for (Element row : table.select("tr")) {
                        Elements tds = row.select("td.item.b1b");
                        Grade grade = new Grade();
                        for (int i = 0; i < tds.size() - 1; i++) {
                            if (i == 0)
                                grade.name = tds.get(i).text();
                            if (i == 1)
                                grade.grade = tds.get(i).text();
                            if (i == 3)
                                grade.percentage = tds.get(i).text();
                        }
                        if(grade.name!=null)
                            grades.add(grade);
                    }
                }
                return grades;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Grade> result) {
            progressDialog.dismiss();
            if (result != null)
                showGradeTable(result);
            else
                Toast.makeText(getApplicationContext(),"There's an error while trying to get your grade, please try again.",Toast.LENGTH_LONG).show();
        }
    }

    void showGradeTable(ArrayList<Grade> list) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ContentActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.table_layout, null);
        alertDialog.setView(convertView);
        ListView lv = (ListView) convertView.findViewById(R.id.gradeListView);
        GradeAdapter adapter = new GradeAdapter(this, android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);
        alertDialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_grade:
                displayGrade();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}