package com.lyy.guohe.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mob.MobSDK;
import com.tencent.bugly.Bugly;
import com.tencent.mta.track.StatisticsDataAPI;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

import org.android.agoo.xiaomi.MiPushRegistar;
import org.litepal.LitePal;

public class InitializeService extends IntentService {

    private static final String ACTION_INIT_WHEN_APP_CREATE = "com.lyy.guohe.service.action.INIT";

    private static final String TAG = "App";

    public InitializeService() {
        super("InitializeService");
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, InitializeService.class);
        intent.setAction(ACTION_INIT_WHEN_APP_CREATE);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INIT_WHEN_APP_CREATE.equals(action)) {
                performInit();
            }
        }
    }

    private void performInit() {
        LitePal.initialize(this.getApplicationContext());

        initX5WebView();



        //腾讯MTA可视化埋点
        StatisticsDataAPI.instance(this.getApplicationContext());
        //初始化Bugly
        Bugly.init(this.getApplicationContext(), "1f3d59d6cb", false);

        MobSDK.init(this.getApplicationContext());

    }



    //初始化腾讯X5WebView内核
    private void initX5WebView() {
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。

        Log.d(TAG, "initX5WebView: " + "执行");

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
