package com.lyy.guohe.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lyy.guohe.R;
import com.mob.bbssdk.gui.views.MainViewInterface;
import com.mob.bbssdk.theme1.BBSTheme1;
import com.mob.tools.utils.ResHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment {

    private Context mContext;

    private View view;

    public NewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            mContext = getActivity();
            view = getActivity().getLayoutInflater().inflate(R.layout.fragment_news, null);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return view;
    }
}
