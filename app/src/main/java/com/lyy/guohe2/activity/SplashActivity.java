package com.lyy.guohe2.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lyy.guohe2.R;
import com.lyy.guohe2.constant.SpConstant;
import com.lyy.guohe2.utils.SpUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //延迟跳转
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isLogIn = SpUtils.getBoolean(getApplicationContext(), SpConstant.IS_LOGIN);
                if (isLogIn) {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000);
    }
}
