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
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private ArrayList<Course> courseList;
    OnItemClickListener mListener;

    public CourseAdapter(ArrayList<Course> list, OnItemClickListener listener) {
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
        courseViewHolder.view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mListener.onClick(view, i);
            }
        });
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