package com.quickquick.screenrecorder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.quickquick.screenrecorder.databinding.ActivityMainBinding;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int DISPLAY_WIDTH = 720;
    private static final int DISPLAY_HEIGHT = 1280;
//    private static final int DISPLAY_WIDTH = 1280;
//    private static final int DISPLAY_HEIGHT = 720;
    private static final int RECORD_REQUEST_CODE = 100;
    private int mScreenDensity;
    boolean isRecording = false;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private MediaProjectionCallback mMediaProjectionCallback;
    private static final SparseIntArray ORIENTTIONS = new SparseIntArray();
    private final String sdcard = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

    ActivityMainBinding binding;

    static {
        ORIENTTIONS.append(Surface.ROTATION_0, 90);
        ORIENTTIONS.append(Surface.ROTATION_90, 0);
        ORIENTTIONS.append(Surface.ROTATION_180, 270);
        ORIENTTIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.setOnClickListener(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mMediaRecorder = new MediaRecorder();
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_screen_recorder:
                RxPermissions rxPermissions = new RxPermissions(this);
                rxPermissions.request(Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if(aBoolean) {
                                    isStartRecordScreen();
                                } else {
                                    Log.d("anlddev", "获取权限失败");
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.d("anlddev", "error:" + throwable.getMessage());
                            }
                        });
                break;
            default:
                break;
        }
    }

    //是否开启录制
    private void isStartRecordScreen() {
        if (!isRecording) {
            initRecorder();
            recordScreen();
        } else {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            stopRecordScreen();
        }
    }
    //初始化录制参数
    private void initRecorder() {
        try {
            if (mMediaRecorder == null) {
                Log.d(TAG, "initRecorder: MediaRecorder为空啊---");
                return;
            }
            File dir = new File(sdcard + "/ScreenRecorder/");
            if(!dir.exists()) {
                dir.mkdirs();
            }
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 音频源
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);// 视频源
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//视频输出格式
            //这里的路径我是直接写死了。。。
            mMediaRecorder.setOutputFile(sdcard + "/ScreenRecorder/" + System.currentTimeMillis() + ".mp4");//存储路径
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);// 设置分辨率
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);// 视频录制格式
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);// 音频格式
            mMediaRecorder.setVideoFrameRate(16);//帧率
            mMediaRecorder.setVideoEncodingBitRate(5242880);//视频清晰度
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientataion = ORIENTTIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientataion);//设置旋转方向
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //开始录制
    private void recordScreen() {
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), RECORD_REQUEST_CODE);
            return;
        }

        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        isRecording = true;
        changeText();
    }
    //停止录制
    private void stopRecordScreen() {
        if (mVirtualDisplay == null) {
            return;
        }

        mVirtualDisplay.release();
        destroyMediaProjection();
        isRecording = false;
        changeText();
    }
    //释放录制的资源
    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("ScreenRecorder", DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);
    }

    private void changeText() {
        if (isRecording) {
            binding.btnScreenRecorder.setText("停止录屏");
        } else {
            binding.btnScreenRecorder.setText("开始录屏");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECORD_REQUEST_CODE) {

            if (resultCode != RESULT_OK) {
                Toast.makeText(MainActivity.this, "录屏权限被禁止了啊", Toast.LENGTH_SHORT).show();
//                isRecording = false;
//                changeText();
                mMediaRecorder.reset();
                stopRecordScreen();
                return;
            }

            mMediaProjectionCallback = new MediaProjectionCallback();
            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            mMediaProjection.registerCallback(mMediaProjectionCallback, null);
            mVirtualDisplay = createVirtualDisplay();
            mMediaRecorder.start();
            isRecording = true;
            changeText();
        }

    }

    @Override
    public void onBackPressed() {
        if (isRecording) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("当前正在录屏，是否停止录屏？")
                    .setPositiveButton("停止录屏", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mMediaRecorder.stop();
                            mMediaRecorder.reset();
                            stopRecordScreen();
                            finish();
                        }
                    }).setNegativeButton("保持录屏", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    moveTaskToBack(true);
                }
            }).create().show();

        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止录制时，释放资源
        destroyMediaProjection();
    }

    //录制回调
    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (isRecording) {
                isRecording = false;
                changeText();
                mMediaRecorder.stop();
                mMediaRecorder.reset();
            }
            mMediaProjection = null;
            stopRecordScreen();
        }
    }
}
