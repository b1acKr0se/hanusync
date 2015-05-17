package io.wyrmise.hanusync;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
    private Map<String, String> cookies;
    private Map<String, String> urls;


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
            case 1:
                view = inflater.inflate(R.layout.fragment_course, container, false);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
                recyclerView = (RecyclerView) view.findViewById(R.id.courseList);
                recyclerView.setHasFixedSize(true);
                LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(llm);
                new GetNameAndID().execute();
                break;
            case 2:
                view = inflater.inflate(R.layout.fragment_submission, container, false);
                submissionProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
                gridView = (GridView) view.findViewById(R.id.grid_view);
                new GetSubmission().execute();
                break;
        }
        return view;
    }

    private class GetNameAndID extends AsyncTask<Void, Void, ArrayList<String>> {

        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            try {
                Intent intent = getActivity().getIntent();
                cookies = (Map<String, String>) intent.getSerializableExtra("cookies");
                Document document = Jsoup.connect("http://fit.hanu.edu.vn/fitportal/").cookies(cookies).get();

                ArrayList<String> courses = new ArrayList<>();
                urls = new HashMap<>();

                Elements courseName = document.select("div.content").select("ul.list").select("li.r0,li.r1").select("a[href]");
                for (Element e : courseName) {
                    courses.add(e.text());
                    urls.put(e.text(), e.attr("abs:href"));
                }
                return courses;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            progressBar.setVisibility(ProgressBar.GONE);
            recyclerView.setVisibility(RecyclerView.VISIBLE);
            courseAdapter = new CourseAdapter(result, MainFragment.this);
            recyclerView.setAdapter(courseAdapter);
        }
    }

    private class GetSubmission extends AsyncTask<Void, Void, ArrayList<Submission>> {

        protected void onPreExecute() {
            submissionProgressBar.setVisibility(ProgressBar.VISIBLE);
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
                for (int i = 0; i<eventName.size();i++) {
                    if(i%2==0) {
                        Submission s = new Submission();
                        s.name = eventName.get(i).text();
                        submissions.add(s);
                    }
                }

                Elements eventDate= document.select("div.content").select("div.event").select("div.date").select("a[href]");
                for(int i = 0; i<eventDate.size();i++){
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
            submissionProgressBar.setVisibility(ProgressBar.GONE);
            gridView.setVisibility(GridView.VISIBLE);
            submissionAdapter = new SubmissionAdapter(result);
            gridView.setAdapter(submissionAdapter);
        }
    }

    public void onClick(View view, int position) {
        switch(view.getId()){
            case R.id.card_view:
                selectCourse(position);
                break;
            case R.id.submission_view:
                break;
        }

    }

    private void selectCourse(int position) {
        String name = courseAdapter.get(position);
        if (urls.containsKey(name)) {
            Intent intent = new Intent(getActivity().getApplicationContext(), ContentActivity.class);
            String subject = name.substring(name.lastIndexOf(": ") + 2, name.length());
            intent.putExtra("subject", subject);
            intent.putExtra("url", urls.get(name));
            intent.putExtra("cookies", (java.io.Serializable) cookies);
            MainFragment.this.startActivity(intent);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "There's an error", Toast.LENGTH_LONG);
        }
    }

}
