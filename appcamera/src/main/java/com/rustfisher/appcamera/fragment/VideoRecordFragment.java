package com.rustfisher.appcamera.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.rustfisher.appcamera.R;
import com.rustfisher.appcamera.view.CameraPreview;

/**
 * 视频录制界面
 * Created by Rust on 2018/5/17.
 */
public class VideoRecordFragment extends Fragment {
    private static final String TAG = "rustAppVideoFrag";

    private Button mCaptureBtn;
    private CameraPreview mCameraPreview;
    private View mRoot; // fragment根视图
    public static VideoRecordFragment newInstance() {
        return new VideoRecordFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "frag onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "frag onCreateView");
        return inflater.inflate(R.layout.frag_video_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "frag onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        mRoot = view;
        mCaptureBtn = view.findViewById(R.id.capture_btn);
        mCaptureBtn.setOnClickListener(mOnClickListener);
        initCameraPreview();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: 销毁预览");
        mCameraPreview = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: 回到前台");
        if (null == mCameraPreview) {
            initCameraPreview();
        }
    }

    private void initCameraPreview() {
        mCameraPreview = new CameraPreview(getContext());
        FrameLayout preview = mRoot.findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.capture_btn:
                    if (mCameraPreview.isRecording()) {
                        mCameraPreview.stopRecording();
                        mCaptureBtn.setText("录像");
                    } else {
                        if (mCameraPreview.startRecording()) {
                            mCaptureBtn.setText("停止");
                        }
                    }
                    break;
            }
        }
    };

}
