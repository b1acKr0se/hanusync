package io.wyrmise.hanusync.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;

import io.wyrmise.hanusync.R;
import io.wyrmise.hanusync.objects.Content;

/**
 * Created by Demise on 5/16/2015.
 */
public class ContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 2;
    private static final int TYPE_ITEM = 1;

    private Context context;
    private int lastPosition = -1;

    private ArrayList<Content> contentList;

    public ContentAdapter(Context c, ArrayList<Content> list) {
        context = c;
        contentList = list;
    }

    public int getBasicItemCount() {
        return contentList == null ? 0 : contentList.size();
    }

    @Override
    public int getItemCount() {
        return getBasicItemCount()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView;
        if (viewType == TYPE_ITEM) {
            itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.course_card_layout, viewGroup, false);
            return new ContentItemViewHolder(itemView);
        } else if (viewType == TYPE_HEADER) {
            itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_header, viewGroup, false);
            return new ContentHeaderViewHolder(itemView);
        }
        throw new RuntimeException("There's no type for that view");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder contentViewHolder, int position) {
        if (!isPositionHeader(position)) {
            ContentItemViewHolder holder = (ContentItemViewHolder) contentViewHolder;
            holder.contentDate.setText(contentList.get(position-1).date);
            holder.contentSummary.setText(contentList.get(position-1).summary);
            holder.contentResources.setText(contentList.get(position-1).resources);
            setAnimation(holder.view, position);
        }
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

    public static class ContentItemViewHolder extends RecyclerView.ViewHolder {
        protected TextView contentDate;
        protected TextView contentSummary;
        protected TextView contentResources;
        protected View view;

        public ContentItemViewHolder(View v) {
            super(v);
            view = v;
            contentDate = (TextView) v.findViewById(R.id.content_time);
            contentSummary = (TextView) v.findViewById(R.id.content_summary);
            contentResources = (TextView) v.findViewById(R.id.content_resources);
        }
    }

    public static class ContentHeaderViewHolder extends RecyclerView.ViewHolder {
        public ContentHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}
