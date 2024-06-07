package com.example.libisbest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.screencapture.ScreenCapture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getName();
    private MyNanoHTTPDServer myServer;
    public ScreenCapture mScreenCapture;

    private Runnable screenCaptureRunnable;

    private RootShellCmd rootShellCmd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. 创建服务器
        File rootDir = new File(getFilesDir(), "www"); // 设置根目录
        if (!rootDir.exists()) {
            rootDir.mkdirs();
            // 将index.html复制到 rootDir
            copyAsset("index.html", new File(rootDir, "index.html"));
        }
        try {
            myServer = new MyNanoHTTPDServer(8080, rootDir,this,  handler);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // 2. 设置截屏
        if (mScreenCapture == null) {
            mScreenCapture =new ScreenCapture(this);
            mScreenCapture.setCaptureListener(new ScreenCapture.OnCaptureListener() {
                @Override
                public void onScreenCaptureSuccess(Bitmap bitmap, String savePath) {
                    Log.d(TAG, "onScreenCaptureSuccess savePath:" + savePath);
                }

                @Override
                public void onScreenCaptureFailed(String errorMsg) {
                    Log.d(TAG, "onScreenCaptureFailed errorMsg:" + errorMsg);
                }

                @Override
                public void onScreenRecordStart() {
                    Log.d(TAG, "onScreenRecordStart");
                }

                @Override
                public void onScreenRecordStop() {
                    Log.d(TAG, "onScreenRecordStop");
                }

                @Override
                public void onScreenRecordSuccess(String savePath) {
                    Log.d(TAG, "onScreenRecordSuccess savePath:" + savePath);
                }

                @Override
                public void onScreenRecordFailed(String errorMsg) {
                    Log.d(TAG, "onScreenRecordFailed errorMsg:" + errorMsg);
                }
            });

            // You can set file path or not
//            mScreenCapture.setImagePath(getFilesDir() + "/www/", "image_screen.png");
//            mScreenCapture.setRecordPath(Environment.getExternalStorageDirectory().getPath() + "/ScreenCapture/record/", "recording_screen.mp4");

        }

        // 3. 申请root
        rootShellCmd = new RootShellCmd();

        // 4.初始化并启动定时任务，每秒截屏一次
        screenCaptureRunnable = new Runnable() {
            @Override
            public void run() {
                // 执行 screenCapture 方法
                if (mScreenCapture != null) {
                    mScreenCapture.screenCapture();
                }
                // 继续下一次执行
//                handler.postDelayed(this, 2000); // 延迟2秒
            }
        };
//
//        // 开始第一次执行
//        handler.post(screenCaptureRunnable);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 移除定时任务，防止内存泄漏
        handler.removeCallbacks(screenCaptureRunnable);
        if (mScreenCapture != null) {
            mScreenCapture.cleanup();
            mScreenCapture = null;
        }
        if (myServer != null) {
            myServer.stop();
        }
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            handler.postDelayed(screenCaptureRunnable, 1400); // 过渡动画1500应该是够的把
            if (msg.what == 1) {
                // 模拟点击屏幕
                int x = msg.arg1;
                int y = msg.arg2;
                // 在这里调用Simuclick方法或其他逻辑
                rootShellCmd.SimuClick(x, y);
            }else if (msg.what == 2) // back
            {
                rootShellCmd.exec("input keyevent KEYCODE_BACK\n");
            }else if (msg.what == 3) // scrollup
            {
                rootShellCmd.exec("input swipe 300 300 300 600 200\n");
            }else if (msg.what == 4) // scrolldown
            {
                rootShellCmd.exec("input swipe 300 600 300 300 200\n");
            }
        }
    };
    // 提供一个公共方法来获取 handler
    public Handler getHandler() {
        return handler;
    }


    private void copyAsset(String assetFileName, File outFile) {
        try (InputStream in = getAssets().open(assetFileName);
             OutputStream out = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startCapture(View v) {
        if (mScreenCapture != null) {
            mScreenCapture.screenCapture();
        }
    }

    /**
     * Handle permission here which caused by MediaProjectionManager.createScreenCaptureIntent()
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mScreenCapture != null) {
            mScreenCapture.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Handle permission here. Like Manifest.permission.WRITE_EXTERNAL_STORAGE
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mScreenCapture != null) {
            mScreenCapture.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}