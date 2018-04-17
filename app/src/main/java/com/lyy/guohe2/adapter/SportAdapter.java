package com.lyy.guohe2.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lyy.guohe2.R;
import com.lyy.guohe2.model.Sport;

import java.util.List;

/**
 * Created by lyy on 2017/11/22.
 */

public class SportAdapter extends ArrayAdapter<Sport> {

    private int resourceId;

    public SportAdapter(Context context, int resource, List<Sport> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Sport sport = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView tv_number = (TextView) view.findViewById(R.id.tv_number);
        TextView tv_time = (TextView) view.findViewById(R.id.tv_time);
        TextView tv_date = (TextView) view.findViewById(R.id.tv_date);

        tv_time.setText(sport.getTime());
        tv_number.setText(sport.getNumber());
        tv_date.setText(sport.getDate());

        return view;
    }
}
