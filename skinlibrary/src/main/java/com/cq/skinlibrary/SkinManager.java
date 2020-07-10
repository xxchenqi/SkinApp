package com.cq.skinlibrary;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.text.TextUtils;

import com.cq.skinlibrary.utils.SkinPreference;

import java.lang.reflect.Method;
import java.util.Observable;

public class SkinManager extends Observable {

    private volatile static SkinManager instance;
    //生命周期管理
    private ApplicationActivityLifecycle skinActivityLifecycle;
    private Application mContext;

    public static void init(Application application) {
        if (instance == null) {
            synchronized (SkinManager.class) {
                if (instance == null) {
                    instance = new SkinManager(application);
                }
            }
        }
    }

    public SkinManager(Application application) {
        mContext = application;
        //共享首选项 用于记录当前使用的皮肤
        SkinPreference.init(application);
        //资源管理类 用于从 app/皮肤 中加载资源
        SkinResources.init(application);
        //注册Activity生命周期,并设置被观察者
        skinActivityLifecycle = new ApplicationActivityLifecycle(this);
        application.registerActivityLifecycleCallbacks(skinActivityLifecycle);
        //加载上次使用保存的皮肤
        loadSkin(SkinPreference.getInstance().getSkin());
    }

    public static SkinManager getInstance() {
        return instance;
    }

    /**
     * 加载皮肤资源包
     *
     * @param skinPath 皮肤路径
     */
    public void loadSkin(String skinPath) {
        if (TextUtils.isEmpty(skinPath)) {
            //还原默认皮肤
            SkinPreference.getInstance().reset();
            SkinResources.getInstance().reset();
        } else {
            try {
                Resources appResources = mContext.getResources();

                //反射创建AssetManager 与 Resource
                AssetManager assetManager = AssetManager.class.newInstance();
                //资源路径设置 目录或压缩包
                Method addAssetPathMethod = assetManager.getClass()
                        .getMethod("addAssetPath", String.class);
                addAssetPathMethod.invoke(assetManager, skinPath);

                //根据当前的设备显示器信息 与 配置(横竖屏、语言等) 创建Resources
                Resources skinResources = new Resources(assetManager, appResources.getDisplayMetrics(),
                        appResources.getConfiguration());

                //获取皮肤包的包名
                PackageManager packageManager = mContext.getPackageManager();
                PackageInfo packageArchiveInfo = packageManager
                        .getPackageArchiveInfo(skinPath, PackageManager.GET_ACTIVITIES);

                SkinResources.getInstance().applySkin(skinResources, packageArchiveInfo.packageName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //加载完成后通知更新
        setChanged();
        notifyObservers();
    }
}
