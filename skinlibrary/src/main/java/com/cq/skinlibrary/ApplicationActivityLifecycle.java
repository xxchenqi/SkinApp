package com.cq.skinlibrary;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.LayoutInflater;

import com.cq.skinlibrary.utils.SkinThemeUtils;

import java.lang.reflect.Field;
import java.util.Observable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.LayoutInflaterCompat;

public class ApplicationActivityLifecycle implements Application.ActivityLifecycleCallbacks {

    private Observable mObservable;
    private ArrayMap<Activity, SkinLayoutInflaterFactory> map = new ArrayMap<>();

    public ApplicationActivityLifecycle(Observable observable) {
        this.mObservable = observable;
    }


    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        SkinLayoutInflaterFactory skinLayoutInflaterFactory = new SkinLayoutInflaterFactory(activity);
        LayoutInflater layoutInflater = activity.getLayoutInflater();

        //更新状态栏
        SkinThemeUtils.updateStatusBarColor(activity);
        //反射修改
        try {
            Field mFactorySetField = LayoutInflater.class.getDeclaredField("mFactorySet");
            mFactorySetField.setAccessible(true);
            mFactorySetField.set(layoutInflater, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //更新布局视图
//        LayoutInflaterCompat.setFactory2(layoutInflater, skinLayoutInflaterFactory);
        layoutInflater.setFactory2(skinLayoutInflaterFactory);
        //缓存
        map.put(activity, skinLayoutInflaterFactory);
        //添加观察者
        mObservable.addObserver(skinLayoutInflaterFactory);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        //缓存中移除
        SkinLayoutInflaterFactory factory = map.remove(activity);
        //移除观察者
        mObservable.deleteObserver(factory);
    }
}
