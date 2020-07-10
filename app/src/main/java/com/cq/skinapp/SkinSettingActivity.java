package com.cq.skinapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.cq.skinlibrary.SkinManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SkinSettingActivity extends Activity {
    int CODE_FOR_WRITE_PERMISSION = 666;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }

    public void defaultClick(View view) {
        SkinManager.getInstance().loadSkin(null);
    }

    public void modifyClick(View view) {

        //使用兼容库就无需判断系统版本
        int hasWriteStoragePermission = ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
            //拥有权限，执行操作
            SkinManager.getInstance().loadSkin("/data/data/com.cq.skinapp/skin/skin-debug.apk");
        } else {
            //没有权限，向用户请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_FOR_WRITE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //通过requestCode来识别是否同一个请求
        if (requestCode == CODE_FOR_WRITE_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //用户同意，执行操作
                SkinManager.getInstance().loadSkin("/data/data/com.cq.skinapp/skin/skin-debug.apk");
            }else{
                //用户不同意，向用户展示该权限作用
                Toast.makeText(this, "呵呵", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
