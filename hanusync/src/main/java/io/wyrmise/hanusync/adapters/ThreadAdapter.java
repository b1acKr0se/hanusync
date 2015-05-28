package io.wyrmise.hanusync.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;

import io.wyrmise.hanusync.R;
import io.wyrmise.hanusync.objects.Comment;

/**
 * Created by Demise on 5/18/2015.
 */
public class ThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private static final int TYPE_HEADER = 2;
    private static final int TYPE_ITEM = 1;

    private int lastPosition = -1;
    private ArrayList<Comment> commentList;

    public ThreadAdapter(Context c, ArrayList<Comment> list) {
        context = c;
        commentList = list;
    }

    public int getBasicItemCount() {
        return commentList == null ? 0 : commentList.size();
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
                    inflate(R.layout.thread_card_layout, viewGroup, false);
            return new ThreadItemViewHolder(itemView);
        } else if (viewType == TYPE_HEADER) {
            itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_header, viewGroup, false);
            return new ThreadIHeaderViewHolder(itemView);
        }
        throw new RuntimeException("There's no type for that view");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder commentViewHolder, int position) {
        if (!isPositionHeader(position)) {
            ThreadItemViewHolder holder = (ThreadItemViewHolder) commentViewHolder;
            holder.subject.setText(commentList.get(position-1).subject);
            holder.poster.setText(commentList.get(position-1).author);
            holder.body.setText(commentList.get(position-1).content);
            setAnimation(holder.view, position);
            if(commentList.get(position-1).hasAttachment){
                holder.divider.setVisibility(View.VISIBLE);
                holder.attachment.setVisibility(TextView.VISIBLE);
                holder.attachment.setText(Html.fromHtml(commentList.get(position - 1).attachment));
                holder.attachment.setClickable(true);
                holder.attachment.setMovementMethod (LinkMovementMethod.getInstance());
                System.out.println(holder.attachment.getText());
            } else {
                holder.divider.setVisibility(View.GONE);
                holder.attachment.setVisibility(TextView.GONE);

            }
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


    public static class ThreadItemViewHolder extends RecyclerView.ViewHolder {
        protected TextView subject;
        protected TextView poster;
        protected TextView body;
        protected TextView attachment;
        protected View divider;
        protected View view;

        public ThreadItemViewHolder(View v) {
            super(v);
            view = v;
            subject = (TextView) v.findViewById(R.id.subject);
            poster = (TextView) v.findViewById(R.id.poster);
            body = (TextView) v.findViewById(R.id.body);
            attachment = (TextView) v.findViewById(R.id.attachment);
            divider= (View) v.findViewById(R.id.bottomDivider);
        }
    }

    public static class ThreadIHeaderViewHolder extends RecyclerView.ViewHolder {
        public ThreadIHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}