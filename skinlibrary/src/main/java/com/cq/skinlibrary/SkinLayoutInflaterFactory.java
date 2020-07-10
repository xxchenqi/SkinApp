package com.cq.skinlibrary;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.cq.skinlibrary.utils.SkinThemeUtils;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SkinLayoutInflaterFactory implements LayoutInflater.Factory2, Observer {

    private static final String[] mClassPrefixList = {
            "android.widget.",
            "android.webkit.",
            "android.app.",
            "android.view."
    };

    private Activity activity;

    // 页面属性管理器
    private SkinAttribute skinAttribute;

    //VIEW构造函数的参数
    private static final Class<?>[] mConstructorSignature = new Class[]{
            Context.class, AttributeSet.class};

    //缓存View对应的构造方法
    private static final HashMap<String, Constructor<? extends View>> mConstructorMap =
            new HashMap<String, Constructor<? extends View>>();

    public SkinLayoutInflaterFactory(Activity activity) {
        this.activity = activity;
        skinAttribute = new SkinAttribute();
    }


    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        //布局里的所有view都会执行此方法
        //创建系统view
        View view = createSdkView(name, context, attrs);
        if (view == null) {
            //如果系统view没找到，创建自定义view
            view = createView(name, context, attrs);
        }
        if (view != null) {
            //添加属性
            skinAttribute.look(view, attrs);
        }
        return view;
    }

    /**
     * 加载系统view
     */
    private View createSdkView(String name, Context context, AttributeSet attrs) {
        //如果包含 . 则不是SDK中的view 可能是自定义view包括support库中的View
        if (-1 != name.indexOf('.')) {
            return null;
        }
        View view;
        //不包含就要在解析的 节点 name前，拼上： android.widget. 等尝试去反射
        for (int i = 0; i < mClassPrefixList.length; i++) {
            //mClassPrefixList[i] + name = android.view.TextView
            view = createView(mClassPrefixList[i] + name, context, attrs);
            if (view != null) return view;
        }
        return null;
    }

    /**
     * 加载自定义view
     */
    private View createView(String name, Context context, AttributeSet attrs) {
        try {
            Constructor<? extends View> constructor = findConstructor(context, name);
            return constructor.newInstance(context, attrs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 查询map里对应的构造方法
     */
    private Constructor<? extends View> findConstructor(Context context, String name) {
        Constructor<? extends View> constructor = mConstructorMap.get(name);
        if (constructor == null) {
            try {
                Class<? extends View> mClass = context.getClassLoader().loadClass(name)
                        .asSubclass(View.class);
                constructor = mClass.getConstructor(mConstructorSignature);
                mConstructorMap.put(name, constructor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return constructor;
    }


    //这个方法不走
    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        //更新状态栏
        SkinThemeUtils.updateStatusBarColor(activity);
        //更新皮肤
        skinAttribute.applySkin();
    }
}
