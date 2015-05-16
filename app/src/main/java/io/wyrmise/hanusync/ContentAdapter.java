package io.wyrmise.hanusync;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Demise on 5/16/2015.
 */
public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentViewHolder> {

    private ArrayList<Content> contentList;

    public ContentAdapter(ArrayList<Content> list){
        contentList = list;
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    @Override
    public ContentViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.course_card_layout, viewGroup, false);

        return new ContentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ContentViewHolder contentViewHolder, int i) {
        contentViewHolder.contentDate.setText(contentList.get(i).date);
        contentViewHolder.contentSummary.setText(contentList.get(i).summary);
        contentViewHolder.contentResources.setText(contentList.get(i).resources);
    }

    public static class ContentViewHolder extends RecyclerView.ViewHolder{
        protected TextView contentDate;
        protected TextView contentSummary;
        protected TextView contentResources;

        public ContentViewHolder(View v){
            super(v);
            contentDate = (TextView) v.findViewById(R.id.content_time);
            contentSummary = (TextView) v.findViewById(R.id.content_summary);
            contentResources = (TextView) v.findViewById(R.id.content_resources);
        }
    }
}
