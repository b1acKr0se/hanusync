package io.wyrmise.hanusync.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;

import io.wyrmise.hanusync.R;
import io.wyrmise.hanusync.objects.News;

/**
 * Created by Demise on 5/18/2015.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private ArrayList<News> newsList;
    OnItemClickListener mListener;
    private int lastPosition = -1;


    public NewsAdapter(Context context, ArrayList<News> list, OnItemClickListener listener) {
        this.context = context;
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

        setAnimation(newsViewHolder.view, i);

        newsViewHolder.view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mListener.onClick(view, i);
            }
        });
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        protected TextView topic;
        protected TextView author;
        protected TextView reply_num;
        protected View view;

        public NewsViewHolder(View v) {
            super(v);
            view = v;
            view.setClickable(true);
            topic = (TextView) v.findViewById(R.id.topic);
            author = (TextView) v.findViewById(R.id.author);
            reply_num = (TextView) v.findViewById(R.id.reply_num);
        }
    }
}