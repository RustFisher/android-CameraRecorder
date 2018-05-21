# android-CameraRecorder

* API 19

## 概述
本文目的：使用 Android Camera API 完成音视频的采集、编码、封包成 mp4 输出

基于`android.hardware.Camera`，创建一个横屏应用，实时预览摄像头图像，实现录像并输出MP4的功能。
目前（2018年5月18日）android Camera2在国内各手机系统上的稳定性还不是很好。

相关说明请参阅： https://github.com/RustFisher/RustNotes/blob/master/Android_note/Android-camera_record_video.md

## 申请权限
需要录制音视频权限和写外部存储权限
```xml
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

* `surfaceCreated`中获取Camera实例，启动预览；设置预览相关参数
* `surfaceDestroyed`释放Camera

### 在Fragment中显示摄像头预览
预置一个FrameLayout，实例化一个`CameraPreview`添加进去


## 使用`MediaRecorder`录制
给`MediaRecorder`指定参数后，调用`start()`开始录制，`stop()`结束录制

录制开始前，需要`mCamera.unlock()`解锁；录制完毕后，清除`MediaRecorder`，`mCamera.lock()`


### 后台返回时预览黑屏的问题
`CameraPreview`是我们在Fragment创建时实例化并添加进去的。
应用退到后台后，`CameraPreview`已经被销毁。应用回到前台时，我们应该在`onResume`方法中进行操作。恢复`CameraPreview`。

在Fragment中，判断销毁和重建预览的时机。


## 参考资料
* [Android相机开发(三): 实现拍照录像和查看](https://www.polarxiong.com/archives/Android%E7%9B%B8%E6%9C%BA%E5%BC%80%E5%8F%91-%E4%B8%89-%E5%AE%9E%E7%8E%B0%E6%8B%8D%E7%85%A7%E5%BD%95%E5%83%8F%E5%92%8C%E6%9F%A5%E7%9C%8B.html)
* [googlesamples/android-MediaRecorder](https://github.com/googlesamples/android-MediaRecorder)
* [Controlling the Camera - Android Developer](https://developer.android.com/training/camera/cameradirect.html)
* [Camera API - Android Developer](https://developer.android.com/guide/topics/media/camera.html)
