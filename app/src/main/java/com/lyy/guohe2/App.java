package com.lyy.guohe2;

import android.app.Application;
import android.content.Context;

import com.tencent.bugly.Bugly;
import com.tencent.smtt.sdk.QbSdk;

import org.litepal.LitePalApplication;

public class App extends Application {

    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePalApplication.initialize(context);

        initX5WebView();

        //初始化Bugly
        Bugly.init(getApplicationContext(), "5675fbf964", false);

    }

    //初始化腾讯X5WebView内核
    private void initX5WebView() {
        //腾讯X5WebView内核
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
            }

            @Override
            public void onCoreInitFinished() {
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);
    }
}
