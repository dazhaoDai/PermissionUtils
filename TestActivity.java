package com.ddz.lifestyle.view.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.ddz.lifestyle.R;
import com.ddz.lifestyle.utils.PermissionUtils;

/**
 * @Author: ddz
 * Creation time: 17.8.21 16:07
 * describe:()
 */

public class TestActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initPremission();
    }

    private void initPremission() {
        if (PermissionUtils.getInstance().checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            toast(null);
        } else {
            PermissionUtils.getInstance().requestPermissiion(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PermissionUtils.requestCode, new PermissionUtils.RequestPermissionListener() {
                @Override
                public void requestConfirm() {
                    //申请成功
                    toast(null);
                }

                @Override
                public void requestCancel() {
                    //用户拒绝，对用户解释申请理由

                    //如果想使用封装好的弹窗提示
                    PermissionUtils.getInstance().requestDialog(TestActivity.this, "申请权限", "需要位置权限", null, null);
                }

                @Override
                public void requestCancelAgain() {
                    //用户勾选不再提示并拒绝，申请打开应用设置页面申请权限，具体逻辑自己写

                    //使用默认封装好的提示,并打开设置页面
                    PermissionUtils.getInstance().requestDialogAgain(TestActivity.this, "申请权限", "去设置页面打开位置限才能正常使用", null, null);
                }

                @Override
                public void requestFailed() {
                    //申请失败
                    toast("对不起，没有权限，退出");
                }
            });
        }
    }

    //一定要对权限回调进行处理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionUtils.getInstance().onRequestPermissionResult(TestActivity.this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //如果打开了设置页面申请权限，一定要对回调进行处理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PermissionUtils.getInstance().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void toast(String message) {
        Toast.makeText(TestActivity.this, message == null ? "权限申请成功" : message, Toast.LENGTH_SHORT).show();
    }
}
