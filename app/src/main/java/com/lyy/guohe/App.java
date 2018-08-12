package com.lyy.guohe;

import android.app.Application;
import android.content.Context;
import android.os.Debug;

import com.facebook.stetho.Stetho;
import com.lyy.guohe.service.InitializeService;

import org.litepal.LitePal;

public class App extends Application {

    public static Context getContext() {
        return context;
    }

    private static Context context;

    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        Debug.startMethodTracing("Guohe");

        context = getApplicationContext();

        LitePal.initialize(this);

        InitializeService.start(this);

        Stetho.initializeWithDefaults(this);

        Debug.stopMethodTracing();
    }
}
