package io.wyrmise.hanusync.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.wyrmise.hanusync.R;
import io.wyrmise.hanusync.objects.Submission;

/**
 * Created by Demise on 5/17/2015.
 */
public class SubmissionAdapter extends BaseAdapter{

    private ArrayList<Submission> submissions;

    private static final int SUBMISSION_PENDING = 1;

    private static final int SUBMISSION_SUBMITTED = 2;

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
        ((TextView) convertView.findViewById(R.id.submissionSubject)).setText(
                submissions.get(position).subject);
        ((TextView) convertView.findViewById(R.id.submissionDate)).setText(
                submissions.get(position).date);
        if(submissions.get(position).status==SUBMISSION_SUBMITTED) {
            ((ImageView) convertView.findViewById(R.id.submissionStatus)).setImageDrawable(convertView.getResources().getDrawable(R.drawable.submitted));
        } else {
            ((ImageView) convertView.findViewById(R.id.submissionStatus)).setImageDrawable(convertView.getResources().getDrawable(R.drawable.pending));
        }
        return convertView;
    }
}
