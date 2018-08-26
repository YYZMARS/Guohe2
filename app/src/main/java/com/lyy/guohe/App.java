package com.lyy.guohe;

import android.app.Application;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.tencent.bugly.Bugly;
import com.tencent.mta.track.StatisticsDataAPI;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

import org.litepal.LitePal;

public class App extends Application {

    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();

        initX5WebView();

        LitePal.initialize(this);

        //腾讯MTA可视化埋点
        StatisticsDataAPI.instance(this.getApplicationContext());
        //初始化Bugly
        Bugly.init(this.getApplicationContext(), "1f3d59d6cb", false);

        Stetho.initializeWithDefaults(this);
    }

    //初始化腾讯X5WebView内核
    private void initX5WebView() {
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。

        QbSdk.reset(this.getApplicationContext());
        QbSdk.setDownloadWithoutWifi(true);

        QbSdk.initX5Environment(this.getApplicationContext(), new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                Log.d(TAG, "initX5WebView: ");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                Log.d(TAG, "initX5WebView: " + b);
            }
        });

        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                Log.d(TAG, "initX5WebView: " + i);
            }

            @Override
            public void onInstallFinish(int i) {
                Log.d(TAG, "initX5WebView: " + i);
            }

            @Override
            public void onDownloadProgress(int i) {
                Log.d(TAG, "initX5WebView: " + i);
            }
        });
        Log.d(TAG, "initX5WebView: " + QbSdk.canLoadX5(this.getApplicationContext()));
    }
}
