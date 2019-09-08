package com.quickquick.screenrecorder;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

public class App extends Application {

    private static App app;

    public static App getApp() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "5d7af20526", false);
        app = this;
    }
}
