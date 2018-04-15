package com.lyy.guohe2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lyy.guohe2.R;
import com.lyy.guohe2.model.Course;

import java.util.List;

public class CourseAdapter extends ArrayAdapter<Course> {

    private int resourceId;

    public CourseAdapter(@NonNull Context context, int resource, @NonNull List<Course> courses) {
        super(context, resource, courses);

        resourceId = resource;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Course course = getItem(position);
        @SuppressLint("ViewHolder") View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);

        @SuppressLint("CutPasteId") TextView tv_jieci = view.findViewById(R.id.tv_jieci);
        @SuppressLint("CutPasteId") TextView tv_courseName = view.findViewById(R.id.tv_courseName);
        @SuppressLint("CutPasteId") TextView tv_classRoom = view.findViewById(R.id.tv_classRoom);

        assert course != null;
        tv_jieci.setText(course.getJieci() + "-" + (course.getJieci() + 1));
        tv_courseName.setText(course.getClassName());
        tv_classRoom.setText(course.getClassRoomName());

        return view;

    }
}
