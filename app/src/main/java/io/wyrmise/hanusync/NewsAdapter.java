package io.wyrmise.hanusync;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Demise on 5/18/2015.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private ArrayList<News> newsList;
    OnItemClickListener mListener;

    public NewsAdapter(ArrayList<News> list, OnItemClickListener listener) {
        newsList = list;
        mListener = listener;
    }

    /**
     * Interface for receiving click events from cells.
     */
    public interface OnItemClickListener {
        public void onClick(View view, int position);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public News get(int position) {
        return newsList.get(position);
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.news_card_layout, viewGroup, false);

        return new NewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NewsViewHolder newsViewHolder, final int i) {
        newsViewHolder.topic.setText(newsList.get(i).topic);
        newsViewHolder.author.setText(newsList.get(i).author);
        String str = newsList.get(i).reply_number > 1 ? " replies" : " reply";
        newsViewHolder.reply_num.setText(newsList.get(i).reply_number + str);

        newsViewHolder.view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mListener.onClick(view, i);
            }
        });
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        protected TextView topic;
        protected TextView author;
        protected TextView reply_num;
        protected View view;

        public NewsViewHolder(View v) {
            super(v);
            view = v;
            topic = (TextView) v.findViewById(R.id.topic);
            author = (TextView) v.findViewById(R.id.author);
            reply_num = (TextView) v.findViewById(R.id.reply_num);
        }
    }
}