package com.lyy.guohe.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lyy.guohe.R;

public class DialogActivity extends AppCompatActivity {

    private TextView mTvDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        initView();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.CENTER);

        Intent intent = getIntent();
        mTvDialog.setText(intent.getStringExtra("content"));
    }

    private void initView() {
        mTvDialog = (TextView) findViewById(R.id.tv_dialog);
    }
}
