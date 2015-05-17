package io.wyrmise.hanusync;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
public class CourseFragment extends Fragment implements CourseAdapter.OnItemClickListener {

    private static final String ARG_OPTION_NUMBER = "option_number";
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private CourseAdapter courseAdapter;
    private Map<String, String> cookies;
    private Map<String, String> urls;

    public CourseFragment() {
        // Required empty public constructor
    }

    public Fragment newInstance(int position) {
        Fragment fragment = new CourseFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_course, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) view.findViewById(R.id.courseList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        int i = getArguments().getInt(ARG_OPTION_NUMBER);
        switch (i) {
            case 1:
                new GetNameAndID().execute();
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
            courseAdapter = new CourseAdapter(result, CourseFragment.this);
            recyclerView.setAdapter(courseAdapter);
        }
    }

    public void onClick(View view, int position) {
        selectCourse(position);

    }

    private void selectCourse(int position) {
        String name = courseAdapter.get(position);
        if (urls.containsKey(name)) {
            Intent intent = new Intent(getActivity().getApplicationContext(), CourseActivity.class);
            String subject = name.substring(name.lastIndexOf(": ") + 2, name.length());
            intent.putExtra("subject", subject);
            intent.putExtra("url", urls.get(name));
            intent.putExtra("cookies", (java.io.Serializable) cookies);
            CourseFragment.this.startActivity(intent);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "There's an error", Toast.LENGTH_LONG);
        }
    }

}
