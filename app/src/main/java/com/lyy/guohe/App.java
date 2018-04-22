package com.lyy.guohe;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;

import com.tencent.bugly.Bugly;
import com.tencent.smtt.sdk.QbSdk;

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
//        initPush();

        //初始化Bugly
        Bugly.init(getApplicationContext(), "1f3d59d6cb", false);

    }

    private boolean initPush() {

        return true;
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
