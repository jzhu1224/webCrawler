package com.zhujiang.mywebbrowser;

import android.app.Application;

/**
 * Created by zhujiang on 2016/11/26.
 */

public class MyApplication extends Application {

    private static final String TAG = "";

    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static MyApplication getInstance() {
        return instance;
    }
}
