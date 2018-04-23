package com.lyy.guohe.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.lyy.guohe.utils.SpUtils;
import com.lyy.guohe.R;
import com.lyy.guohe.constant.SpConstant;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatService;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        //延迟跳转
        new Handler().postDelayed(() -> {

            initMTA();

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
        }, 2000);
    }

    //初始化MTA统计
    private void initMTA() {
        String appkey = "A94QDN7G9NXW";
        // 初始化并启动MTA
        try {
            // 第三个参数必须为：com.tencent.stat.common.StatConstants.VERSION
            StatService.startStatService(this, appkey,
                    com.tencent.stat.common.StatConstants.VERSION);
            Log.d("MTA", "MTA初始化成功");
        } catch (MtaSDkException e) {
            // MTA初始化失败
            Log.d("MTA", "MTA初始化失败" + e);
        }
    }
}
