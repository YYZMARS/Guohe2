package com.lyy.guohe;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.tencent.bugly.Bugly;
import com.tencent.mta.track.StatisticsDataAPI;
import com.tencent.smtt.sdk.QbSdk;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

import org.android.agoo.xiaomi.MiPushRegistar;
import org.litepal.LitePalApplication;

import java.util.List;

public class App extends Application {

    private Context context;

    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePalApplication.initialize(context);

        initX5WebView();

        initUMApp();

        //腾讯MTA可视化埋点
        StatisticsDataAPI.instance(this);
        //初始化Bugly
        Bugly.init(getApplicationContext(), "1f3d59d6cb", false);

    }

    //初始化友盟
    private void initUMApp() {
        //设置友盟推送
        UMConfigure.init(this, "5ae5a594f43e4852f6000243", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "9ed662dd03c2461ae07fce862110ae7f");
        //设置小米推送
        MiPushRegistar.register(this, "2882303761517775126", "5791777593126");
        PushAgent mPushAgent = PushAgent.getInstance(this);
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回device token
                Log.d(TAG, "onSuccess: " + deviceToken);
            }

            @Override
            public void onFailure(String s, String s1) {
            }
        });
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

    /**
     * 获取当前进程名称
     *
     * @return processName
     */
    public String getCurrentProcessName() {
        int currentProcessId = Process.myPid();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
            if (runningAppProcess.pid == currentProcessId) {
                return runningAppProcess.processName;
            }
        }
        return null;
    }
}
