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
import io.wyrmise.hanusync.objects.Course;

/**
 * Created by Demise on 5/16/2015.
 */
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private Context context;
    private ArrayList<Course> courseList;
    OnItemClickListener mListener;
    private int lastPosition = -1;

    public CourseAdapter(Context c, ArrayList<Course> list, OnItemClickListener listener) {
        context = c;
        courseList = list;
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
        return courseList.size();
    }

    public Course get(int position) {
        return courseList.get(position);
    }

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);

        return new CourseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CourseViewHolder courseViewHolder, final int i) {
        courseViewHolder.courseDate.setText(courseList.get(i).year);
        courseViewHolder.courseName.setText(courseList.get(i).name);
        setAnimation(courseViewHolder.view, i);
        courseViewHolder.view.setOnClickListener(new View.OnClickListener() {
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


    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        protected TextView courseName;
        protected TextView courseDate;
        protected View view;

        public CourseViewHolder(View v) {
            super(v);
            view = v;
            courseName = (TextView) v.findViewById(R.id.courseName);
            courseDate = (TextView) v.findViewById(R.id.courseDate);
        }
    }
}
