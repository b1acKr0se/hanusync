package io.wyrmise.hanusync.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.wyrmise.hanusync.R;
import io.wyrmise.hanusync.adapters.NewsAdapter;
import io.wyrmise.hanusync.objects.News;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment implements NewsAdapter.OnItemClickListener {

    private static final String ARG_NEWS_TYPE = "news_type";
    private int news_type = 1;

    private ProgressBar progressBar;
    private SwipeRefreshLayout newsSwipeLayout;
    private RecyclerView recyclerView;
    private Map<String, String> urls;
    private int number_of_topics = 10;
    private NewsAdapter newsAdapter;


    public NewsFragment() {
        // Required empty public constructor
    }

    public Fragment newInstance(int position) {
        Fragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int i = getArguments().getInt(ARG_NEWS_TYPE);
        switch (i) {
            case 1:
                news_type = 1;
                break;
            case 2:
                news_type = 2;
                break;
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        number_of_topics = Integer.parseInt(pref.getString(SettingsActivity.KEY_ENTRY_NUM, "10"));

        View view = inflater.inflate(R.layout.news_fragment, container, false);
        setHasOptionsMenu(false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        newsSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.news_swipe_refresh);

        newsSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNews();
            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.newsList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        new GetGeneralNews().execute();

        return view;
    }

    public void reload() {
        System.out.println("reloading");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        number_of_topics = Integer.parseInt(pref.getString(SettingsActivity.KEY_ENTRY_NUM, "10"));
        progressBar.setVisibility(ProgressBar.VISIBLE);
        recyclerView.setVisibility(RecyclerView.GONE);
        new GetGeneralNews().execute();
    }

    private void refreshNews() {
        new GetGeneralNews().execute();
    }

    public void onClick(View view, int position) {
        selectTopic(position);
    }

    private void selectTopic(int position) {
        News n = newsAdapter.get(position);
        String topic = n.topic;
        if (urls.containsKey(topic)) {
            Intent intent = new Intent(getActivity().getApplicationContext(), ThreadActivity.class);
            intent.putExtra("topic", urls.get(topic));
            NewsFragment.this.startActivity(intent);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "There's an error", Toast.LENGTH_LONG);
        }
    }

    private class GetGeneralNews extends AsyncTask<Void, Void, ArrayList<News>> {

        protected void onPreExecute() {

        }

        @Override
        protected ArrayList<News> doInBackground(Void... params) {
            try {
                Intent intent = getActivity().getIntent();
                Document document = null;

                switch (news_type) {
                    case 1:
                        document = Jsoup.connect("http://fit.hanu.edu.vn/fitportal/mod/forum/view.php?id=28").get();
                        break;
                    case 2:
                        document = Jsoup.connect("http://fit.hanu.edu.vn/fitportal/mod/forum/view.php?id=25").get();
                        break;
                }

                ArrayList<News> news = new ArrayList<>();
                urls = new HashMap<>();

                Element table = document.select("table.forumheaderlist").first();
                for (Element row : table.select("tr.discussion.r0,tr.discussion.r1")) {
                    if (news.size() <= number_of_topics) {
                        News n = new News();
                        n.topic = row.select("td.topic.starter").select("a[href]").text();
                        n.author = row.select("td.author").select("a[href]").text();
                        String reply = row.select("td.replies").select("a[href]").text();
                        n.reply_number = Integer.parseInt(reply);
                        urls.put(n.topic, row.select("td.topic.starter").select("a[href]").attr("abs:href"));
                        news.add(n);
                    } else break;
                }
                return news;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<News> result) {
            progressBar.setVisibility(ProgressBar.GONE);
            recyclerView.setVisibility(RecyclerView.VISIBLE);
            newsSwipeLayout.setRefreshing(false);
            if (result != null) {
                newsAdapter = new NewsAdapter(result, NewsFragment.this);
                recyclerView.setAdapter(newsAdapter);
            }
        }
    }


}
