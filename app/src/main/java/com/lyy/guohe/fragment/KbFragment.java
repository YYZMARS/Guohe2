package com.lyy.guohe.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.lyy.guohe.R;
import com.lyy.guohe.view.Fab;

public class KbFragment extends Fragment implements View.OnClickListener {

    private Activity mContext;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if (getActivity() != null) {
            mContext = getActivity();
        }

        //初始化界面
        View view = inflater.inflate(R.layout.fragment_kb, container, false);
        initView(view);
        return view;
    }

    //初始化界面
    private void initView(View view) {
        initFab(view);      //初始化底部Fab
    }

    //初始化底部Fab
    private void initFab(View view) {
        Fab fab = view.findViewById(R.id.fab);
        View sheetView = view.findViewById(R.id.fab_sheet);
        View overlay = view.findViewById(R.id.overlay);
        int sheetColor = getResources().getColor(R.color.material_white_1000);
        int fabColor = getResources().getColor(R.color.material_white_1000);
        MaterialSheetFab materialSheetFab = new MaterialSheetFab(fab, sheetView, overlay, sheetColor, fabColor);
        LinearLayout ll_kb_update = view.findViewById(R.id.ll_kb_update);
        ll_kb_update.setOnClickListener(this);
        LinearLayout ll_kb_week_change = view.findViewById(R.id.ll_kb_week_change);
        ll_kb_week_change.setOnClickListener(this);
        LinearLayout ll_kb_year_change = view.findViewById(R.id.ll_kb_year_change);
        ll_kb_year_change.setOnClickListener(this);
        LinearLayout ll_kb_bg_change = view.findViewById(R.id.ll_kb_bg_change);
        ll_kb_bg_change.setOnClickListener(this);
        LinearLayout ll_kb_current_change = view.findViewById(R.id.ll_kb_current_change);
        ll_kb_current_change.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_kb_update:
                updateKb();     //更新课表
                break;
            case R.id.ll_kb_week_change:
                changeWeek();   //更改周次
                break;
            case R.id.ll_kb_year_change:
                changYear();    //更改学年
                break;
            case R.id.ll_kb_bg_change:
                changKbBg();    //更改课表背景
                break;
            case R.id.ll_kb_current_change:
                changeCurrentWeek();        //更改当前周
                break;
        }
    }

    //更改当前周
    private void changeCurrentWeek() {

    }

    //更改课表背景
    private void changKbBg() {

    }

    //更改学年
    private void changYear() {

    }

    //更新课表
    private void updateKb() {

    }

    //更改周次
    private void changeWeek() {

    }
}
