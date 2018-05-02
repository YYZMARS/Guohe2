package com.lyy.guohe.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.lyy.guohe.R;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.utils.NavigateUtil;
import com.lyy.guohe.utils.SpUtils;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

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
                NavigateUtil.navigateTo(this, MainActivity.class);
                finish();
            } else {
                NavigateUtil.navigateTo(this, LoginActivity.class);
                finish();
            }
        }, 0);
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

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("SplashScreen"); //手动统计页面("SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);
        StatService.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        MobclickAgent.onPause(this);
    }
}
