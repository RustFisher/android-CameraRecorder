# android-CameraRecorder

## 概述
本文目的：使用 Android Camera API 完成音视频的采集、编码、封包成 mp4 输出

基于`android.hardware.Camera`，创建一个横屏应用，实时预览摄像头图像，实现录像并输出MP4的功能。
目前（2018年5月18日）android Camera2在国内各手机系统上的稳定性还不是很好。

相关代码请参阅： https://github.com/RustFisher/android-CameraRecorder

## 申请权限
```xml
    <!-- 需要录制音视频权限和写外部存储权限 -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.CAMERA" />

    <uses-feature android:name="android.hardware.camera" />
```
在activity中动态申请权限
```java
    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
```

## 实现摄像头预览功能
使用`SurfaceView`来预览。新建`CameraPreview`类继承自`SurfaceView`并实现`SurfaceHolder.Callback`；
camera相关操作都放在这个View里。
```java
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "rustAppCameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static int mOptVideoWidth = 1920;  // 默认视频帧宽度
    private static int mOptVideoHeight = 1080;
    private Uri outputMediaFileUri;
    private String outputMediaFileType;


    public CameraPreview(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            Log.d(TAG, "camera is not available");
        }
        return c;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = getCameraInstance();
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            getCameraOptimalVideoSize(); // 找到最合适的分辨率
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    private void getCameraOptimalVideoSize() {
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
            List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
            Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                    mSupportedPreviewSizes, getWidth(), getHeight());
            mOptVideoWidth = optimalSize.width;
            mOptVideoHeight = optimalSize.height;
            Log.d(TAG, "prepareVideoRecorder: optimalSize:" + mOptVideoWidth + ", " + mOptVideoHeight);
        } catch (Exception e) {
            Log.e(TAG, "getCameraOptimalVideoSize: ", e);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder.removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }
}
```

### 在Fragment中显示摄像头预览
预置一个FrameLayout，实例化一个`CameraPreview`添加进去
```java
/**
 * 视频录制界面
 * Created by Rust on 2018/5/17.
 */
public class VideoRecordFragment extends Fragment {
    private static final String TAG = "rustAppVideoFrag";

    private Button mCaptureBtn;
    private CameraPreview mCameraPreview;

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
        mCaptureBtn = view.findViewById(R.id.capture_btn);
        //mCaptureBtn.setOnClickListener(mOnClickListener);// 录制键

        mCameraPreview = new CameraPreview(getContext());
        FrameLayout preview = view.findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);
    }
}
```

## 使用`MediaRecorder`录制
给`MediaRecorder`指定参数后，调用`start()`开始录制，`stop()`结束录制

录制开始前，需要`mCamera.unlock()`解锁；录制完毕后，清除`MediaRecorder`，`mCamera.lock()`

```java
private MediaRecorder mMediaRecorder;


    public boolean startRecording() {
        if (prepareVideoRecorder()) {
            mMediaRecorder.start();
            return true;
        } else {
            releaseMediaRecorder();
        }
        return false;
    }

    public void stopRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
        }
        releaseMediaRecorder();
    }

    public boolean isRecording() {
        return mMediaRecorder != null;
    }

    private boolean prepareVideoRecorder() {
        mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        mMediaRecorder.setVideoSize(mOptVideoWidth, mOptVideoHeight);
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
        mMediaRecorder.setPreviewDisplay(mHolder.getSurface());

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), TAG);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
            outputMediaFileType = "image/*";
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
            outputMediaFileType = "video/*";
        } else {
            return null;
        }
        outputMediaFileUri = Uri.fromFile(mediaFile);
        return mediaFile;
    }
```

### 后台返回时预览黑屏的问题
`CameraPreview`是我们在Fragment创建时实例化并添加进去的。
应用退到后台后，`CameraPreview`已经被销毁。应用回到前台时，我们应该在`onResume`方法中进行操作。恢复`CameraPreview`。

在Fragment中，判断销毁和重建预览的时机。
```java
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

```

## 参考资料
* [Android相机开发(三): 实现拍照录像和查看](https://www.polarxiong.com/archives/Android%E7%9B%B8%E6%9C%BA%E5%BC%80%E5%8F%91-%E4%B8%89-%E5%AE%9E%E7%8E%B0%E6%8B%8D%E7%85%A7%E5%BD%95%E5%83%8F%E5%92%8C%E6%9F%A5%E7%9C%8B.html)
* [googlesamples/android-MediaRecorder](https://github.com/googlesamples/android-MediaRecorder)
* [Controlling the Camera - Android Developer](https://developer.android.com/training/camera/cameradirect.html)
* [Camera API - Android Developer](https://developer.android.com/guide/topics/media/camera.html)
