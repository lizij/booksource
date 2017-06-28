package com.lizij.locationtest;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by Lizij on 2017/6/28.
 */

public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
    }
}
