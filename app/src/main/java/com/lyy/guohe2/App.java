package com.lyy.guohe2;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePalApplication;

public class App extends Application {

    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePalApplication.initialize(context);
    }
}
