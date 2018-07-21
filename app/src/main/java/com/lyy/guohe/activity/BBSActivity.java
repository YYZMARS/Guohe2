package com.lyy.guohe.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.lyy.guohe.R;
import com.mob.bbssdk.gui.views.MainViewInterface;
import com.mob.bbssdk.theme1.BBSTheme1;
import com.mob.tools.utils.ResHelper;

public class BBSActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs);

        BBSTheme1.init();
        MainViewInterface mainView = (MainViewInterface) findViewById(ResHelper.getIdRes(this, "mainView"));

        mainView.loadData();
    }

}
