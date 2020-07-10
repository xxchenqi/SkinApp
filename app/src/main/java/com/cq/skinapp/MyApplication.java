package com.cq.skinapp;

import android.app.Application;

import com.cq.skinlibrary.SkinManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SkinManager.init(this);
    }
}
