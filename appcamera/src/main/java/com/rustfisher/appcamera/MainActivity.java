package com.rustfisher.appcamera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.rustfisher.appcamera.fragment.VideoRecordFragment;

/**
 * 主界面
 * 在这申请相关权限
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "rustAppMainAct";
    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (hasPermission(getApplicationContext(), VIDEO_PERMISSIONS)) {
            if (null == savedInstanceState) {
                Log.d(TAG, "onCreate: load fragment");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, VideoRecordFragment.newInstance()).commit();
            }
        } else {
            Log.d(TAG, "onCreate: requestPermissions");
            ActivityCompat.requestPermissions(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_VIDEO_PERMISSIONS && permissions.length == VIDEO_PERMISSIONS.length) {
            boolean gotPermission = true;
            for (int resCode : grantResults) {
                gotPermission = gotPermission && (PackageManager.PERMISSION_GRANTED == resCode);
            }
            if (gotPermission) {
                Log.d(TAG, "onRequestPermissionsResult: 获得所需全部权限");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, VideoRecordFragment.newInstance()).commitAllowingStateLoss();
            } else {
                Toast.makeText(getApplicationContext(), "Please grant the permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean hasPermission(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (PackageManager.PERMISSION_GRANTED !=
                    ActivityCompat.checkSelfPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }
}
