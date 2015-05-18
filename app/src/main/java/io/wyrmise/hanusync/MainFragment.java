package io.wyrmise.hanusync;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements CourseAdapter.OnItemClickListener {

    private static final String ARG_OPTION_NUMBER = "option_number";
    private ProgressBar progressBar;
    private ProgressBar submissionProgressBar;
    private RecyclerView recyclerView;
    private GridView gridView;
    private CourseAdapter courseAdapter;
    private SubmissionAdapter submissionAdapter;
    private ArrayList<Submission> submissionList;
    private Map<String, String> cookies;
    private Map<String, String> urls;
    private SwipeRefreshLayout courseSwipeLayout;
    private SwipeRefreshLayout submissionSwipeLayout;

    private TextView no_submission;

    private static final int SUBMISSION_SUBMITTED = 2;

    public MainFragment() {
        // Required empty public constructor
    }

    public Fragment newInstance(int position) {
        Fragment fragment = new MainFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = null;
        int i = getArguments().getInt(ARG_OPTION_NUMBER);
        switch (i) {
            case 2:
                view = inflater.inflate(R.layout.fragment_course, container, false);
                setHasOptionsMenu(false);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
                courseSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.course_swipe_refresh);

                courseSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshCourse();
                    }
                });

                recyclerView = (RecyclerView) view.findViewById(R.id.courseList);
                recyclerView.setHasFixedSize(true);
                LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(llm);
                progressBar.setVisibility(ProgressBar.VISIBLE);
                new GetCourse().execute();
                break;
            case 3:
                view = inflater.inflate(R.layout.fragment_submission, container, false);
                submissionProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
                gridView = (GridView) view.findViewById(R.id.grid_view);
                no_submission = (TextView) view.findViewById(R.id.no_submission);

                submissionSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.submission_swipe_refresh);
                submissionSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshSubmission();
                    }
                });
                setHasOptionsMenu(true);
                submissionProgressBar.setVisibility(ProgressBar.VISIBLE);
                new GetSubmission().execute();
                break;
        }
        return view;
    }

    private void refreshSubmission() {
        Toast.makeText(getActivity().getApplicationContext(),"Refreshing submission list",Toast.LENGTH_SHORT).show();
        new GetSubmission().execute();
    }

    private void refreshCourse() {
        Toast.makeText(getActivity().getApplicationContext(),"Refreshing course list",Toast.LENGTH_SHORT).show();
        new GetCourse().execute();
    }

    private class GetCourse extends AsyncTask<Void, Void, ArrayList<Course>> {

        protected void onPreExecute() {

        }

        @Override
        protected ArrayList<Course> doInBackground(Void... params) {
            try {
                Intent intent = getActivity().getIntent();
                cookies = (Map<String, String>) intent.getSerializableExtra("cookies");
                Document document = Jsoup.connect("http://fit.hanu.edu.vn/fitportal/").cookies(cookies).get();

                ArrayList<Course> courses = new ArrayList<>();
                urls = new HashMap<>();

                Elements courseName = document.select("div.content").select("ul.list").select("li.r0,li.r1").select("a[href]");

                for (Element e : courseName) {
                    Course c = new Course();
                    c.year = e.text().substring(0, 3);
                    c.name = e.text().substring(e.text().lastIndexOf(": ") + 2, e.text().length());
                    courses.add(c);
                    urls.put(c.name, e.attr("abs:href"));
                }

                for (int i = 0; i < courses.size(); i++) {
                    Course c = courses.get(i);
                    if (c.name.equals("tudent's Forums"))
                        courses.remove(i);
                }

                return courses;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Course> result) {
            progressBar.setVisibility(ProgressBar.GONE);
            recyclerView.setVisibility(RecyclerView.VISIBLE);
            courseSwipeLayout.setRefreshing(false);
            if(result.size()>0 && result!=null) {
                courseAdapter = new CourseAdapter(result, MainFragment.this);
                recyclerView.setAdapter(courseAdapter);
            }
        }
    }

    private class GetSubmission extends AsyncTask<Void, Void, ArrayList<Submission>> {

        protected void onPreExecute() {

        }

        @Override
        protected ArrayList<Submission> doInBackground(Void... params) {
            try {
                Intent intent = getActivity().getIntent();
                cookies = (Map<String, String>) intent.getSerializableExtra("cookies");
                Document document = Jsoup.connect("http://fit.hanu.edu.vn/fitportal/").cookies(cookies).get();

                ArrayList<Submission> submissions = new ArrayList<>();
                urls = new HashMap<>();

                Elements eventName = document.select("div.content").select("div.event").select("a[href]");
                for (int i = 0; i < eventName.size(); i++) {
                    if (i % 2 == 0) {
                        Submission s = new Submission();
                        s.name = eventName.get(i).text();
                        urls.put(s.name, eventName.get(i).attr("abs:href"));
                        System.out.println(urls.get(s.name));
                        submissions.add(s);
                    }
                }

                Elements eventDate = document.select("div.content").select("div.event").select("div.date").select("a[href]");
                for (int i = 0; i < eventDate.size(); i++) {
                    Submission s = submissions.get(i);
                    s.date = eventDate.get(i).text();
                }
                return submissions;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Submission> result) {
            submissionList = result;
            new GetSubmissionStatus().execute();
        }
    }

    private class GetSubmissionStatus extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (int i = 0; i < submissionList.size(); i++) {
                    Submission s = submissionList.get(i);
                    Document document = Jsoup.connect(urls.get(s.name)).cookies(cookies).get();
                    Elements elements = document.select("div#content").select("div.box.generalbox.generalboxcontent.boxaligncenter").select("div.files").select("a[href]");
                    for (Element e : elements) {
                        if (e.text().length() > 0) {
                            s.status = SUBMISSION_SUBMITTED;
                        }
                    }
                    String url = "";

                    Element subject_url = document.select("div.navbar.clearfix").select("div.breadcrumb").select("ul").select("li").not("li.first").select("a[href]").first();
                    url = subject_url.attr("abs:href");
                    System.out.println(url);

                    Document subject_document = Jsoup.connect(url).cookies(cookies).get();
                    String title_raw = subject_document.title();
                    String title = title_raw.substring(title_raw.lastIndexOf(": ") + 2, title_raw.length());

                    s.subject = title;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            submissionProgressBar.setVisibility(ProgressBar.GONE);
            submissionSwipeLayout.setRefreshing(false);
            if (submissionList.size() > 0 || submissionList != null) {
                gridView.setVisibility(GridView.VISIBLE);
                submissionAdapter = new SubmissionAdapter(submissionList);
                gridView.setAdapter(submissionAdapter);
            } else {
                no_submission.setVisibility(TextView.VISIBLE);
            }
        }
    }

    public void onClick(View view, int position) {
        switch (view.getId()) {
            case R.id.card_view:
                selectCourse(position);
                break;
        }

    }

    private void selectCourse(int position) {
        Course c = courseAdapter.get(position);
        String name = c.name;
        if (urls.containsKey(name)) {
            Intent intent = new Intent(getActivity().getApplicationContext(), ContentActivity.class);
            String subject = name;
            intent.putExtra("subject", subject);
            intent.putExtra("url", urls.get(name));
            intent.putExtra("cookies", (java.io.Serializable) cookies);
            MainFragment.this.startActivity(intent);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "There's an error", Toast.LENGTH_LONG);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                showInfo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showInfo() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Information");
        alertDialog.setMessage(getResources().getString(R.string.intro_message));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

}
