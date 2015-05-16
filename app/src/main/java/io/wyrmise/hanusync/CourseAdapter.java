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

    private ArrayList<String> courseList;
    private OnItemClickListener mListener;

    /**
     * Interface for receiving click events from cells.
     */
    public interface OnItemClickListener {
        public void onClick(View view, int position);
    }

    public CourseAdapter(ArrayList<String> list, OnItemClickListener listener){
        courseList = list;
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public String get(int position){
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
    public void onBindViewHolder(CourseViewHolder courseViewHolder, int i) {
        courseViewHolder.courseName.setText(courseList.get(i));
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder{
        protected TextView courseName;

        public CourseViewHolder(View v){
            super(v);
            courseName = (TextView) v.findViewById(R.id.courseName);
        }
    }
}
