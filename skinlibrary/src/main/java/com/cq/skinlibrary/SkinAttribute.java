package com.cq.skinlibrary;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cq.skinlibrary.utils.SkinThemeUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.core.view.ViewCompat;

public class SkinAttribute {
    private static final List<String> mAttributes = new ArrayList<>();

    static {
        mAttributes.add("background");
        mAttributes.add("src");
        mAttributes.add("textColor");
        mAttributes.add("drawableLeft");
        mAttributes.add("drawableTop");
        mAttributes.add("drawableRight");
        mAttributes.add("drawableBottom");
    }

    //记录换肤需要操作的View与属性信息
    private List<SkinView> mSkinViews = new ArrayList<>();

    public void look(View view, AttributeSet attrs) {
        List<SkinPair> skinPairs = new ArrayList<>();
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            //获得属性名  textColor/background
            String attributeName = attrs.getAttributeName(i);

            if (mAttributes.contains(attributeName)) {
                // #
                // ?722727272
                // @722727272
                String attributeValue = attrs.getAttributeValue(i);
                // 比如color 以#开头表示写死的颜色 不可用于换肤
                if (attributeValue.startsWith("#")) {
                    continue;
                }
                int resId;
                if (attributeValue.startsWith("?")) {
                    // 以 ？开头的表示使用 属性
                    int attrId = Integer.parseInt(attributeValue.substring(1));
                    resId = SkinThemeUtils.getResId(view.getContext(), new int[]{attrId})[0];
                } else {
                    // 正常以 @ 开头
                    resId = Integer.valueOf(attributeValue.substring(1));
                }
                skinPairs.add(new SkinPair(attributeName, resId));
            }
        }

        if (!skinPairs.isEmpty() || view instanceof SkinViewSupport) {
            SkinView skinView = new SkinView(view, skinPairs);
            // 如果选择过皮肤 ，调用 一次 applySkin 加载皮肤的资源
            skinView.applySkin();
            mSkinViews.add(skinView);
        }
    }


    /**
     * 对所有的view中的所有的属性进行皮肤修改
     */
    public void applySkin() {
        for (SkinView mSkinView : mSkinViews) {
            mSkinView.applySkin();
        }
    }

    static class SkinView {
        View view;
        //这个View的能被 换肤的属性与它对应的id 集合
        List<SkinPair> skinPairs;

        public SkinView(View view, List<SkinPair> skinPairs) {
            this.view = view;
            this.skinPairs = skinPairs;
        }


        public void applySkin() {
            applySkinSupport();
            Drawable drawableLeft = null, drawableRight = null, drawableTop = null, drawableBottom = null;
            for (SkinPair skinPair : skinPairs) {
                switch (skinPair.attributeName) {
                    case "background":
                        Object background = SkinResources.getInstance().getBackground(skinPair.resId);
                        if (background instanceof Integer) {
                            view.setBackgroundColor((int) background);
                        } else {
                            ViewCompat.setBackground(view, (Drawable) background);
                        }
                        break;
                    case "src":
                        background = SkinResources.getInstance().getBackground(skinPair
                                .resId);
                        if (background instanceof Integer) {
                            ((ImageView) view).setImageDrawable(new ColorDrawable((Integer) background));
                        } else {
                            ((ImageView) view).setImageDrawable((Drawable) background);
                        }
                        break;
                    case "textColor":
                        ((TextView) view).setTextColor(SkinResources.getInstance()
                                .getColorStateList(skinPair.resId));
                        break;
                    case "drawableLeft":
                        drawableLeft = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                    case "drawableTop":
                        drawableTop = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                    case "drawableRight":
                        drawableRight = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                    case "drawableBottom":
                        drawableBottom = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                }
                if (drawableLeft != null || drawableRight != null || drawableTop != null || drawableBottom != null) {
                    ((TextView) view).setCompoundDrawables(drawableLeft, drawableTop, drawableRight, drawableBottom);
                }
            }
        }

        private void applySkinSupport() {
            if (view instanceof SkinViewSupport) {
                ((SkinViewSupport) view).applySkin();
            }
        }
    }


    static class SkinPair {
        //属性名
        String attributeName;
        //对应的资源id
        int resId;

        public SkinPair(String attributeName, int resId) {
            this.attributeName = attributeName;
            this.resId = resId;
        }
    }

}
