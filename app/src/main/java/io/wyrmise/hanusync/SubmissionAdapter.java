package io.wyrmise.hanusync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Demise on 5/17/2015.
 */
public class SubmissionAdapter extends BaseAdapter{

    private ArrayList<Submission> submissions;

    public SubmissionAdapter(ArrayList<Submission> list){
        submissions = list;
    }

    @Override
    public int getCount() {
        return submissions.size();
    }

    @Override
    public Object getItem(int position) {
        return submissions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return submissions.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            convertView = LayoutInflater.
                    from(container.getContext()).inflate(R.layout.submission_grid,
                    container, false);
        }

        ((TextView) convertView.findViewById(R.id.submissionName)).setText(
                submissions.get(position).name);
        ((TextView) convertView.findViewById(R.id.submissionDate)).setText(
                submissions.get(position).date);
        return convertView;
    }
}
