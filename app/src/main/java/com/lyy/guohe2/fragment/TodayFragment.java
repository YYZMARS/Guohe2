package com.lyy.guohe2.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lyy.guohe2.R;

public class TodayFragment extends Fragment {
    private View view;
    private static final String KEY = "title";
    private TextView tvKbShow;
    private TextView tvMessage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_today, container, false);
        tvKbShow = (TextView) view.findViewById(R.id.tv_kb_show);
        tvMessage = view.findViewById(R.id.tv_message);
        tvKbShow.setText("无课，不欺");
        tvMessage.setText("反馈的同学请留下QQ或者微信联系方式，否则难以沟通解决问题，谢谢。");
//        String string = getArguments().getString(KEY);
//        tvContent.setText(string);
//        tvContent.setTextColor(Color.BLUE);
//        tvContent.setTextSize(30);
        return view;
    }

    /**
     * fragment静态传值
     */
    public static TodayFragment newInstance(String str) {
        TodayFragment fragment = new TodayFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY, str);
        fragment.setArguments(bundle);

        return fragment;
    }
}