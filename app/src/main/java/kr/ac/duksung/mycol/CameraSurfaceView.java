package kr.ac.duksung.mycol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import kr.ac.duksung.mycol.databinding.ActivityArmakeupBinding;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder surfaceHolder;

    public CameraSurfaceView(Context context) {
        super(context);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Surface가 생성되면, ARMakeupActivity에서 설정을 완료할 수 있도록 알림.
        // ARMakeupActivity에서 미리보기 설정을 담당
        if (surfaceCreatedCallback != null) {
            surfaceCreatedCallback.onSurfaceCreated(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Surface 크기나 형식이 변경될 때 필요 시 처리
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface가 파괴되면, ARMakeupActivity에서 카메라 정리 작업을 수행할 수 있도록 알림.
        if (surfaceCreatedCallback != null) {
            surfaceCreatedCallback.onSurfaceDestroyed();
        }
    }

    // SurfaceView 생성과 파괴 시 이벤트를 전달하기 위한 인터페이스 정의
    public interface SurfaceCreatedCallback {
        void onSurfaceCreated(SurfaceHolder holder);
        void onSurfaceDestroyed();
    }

    private SurfaceCreatedCallback surfaceCreatedCallback;

    public void setSurfaceCreatedCallback(SurfaceCreatedCallback callback) {
        this.surfaceCreatedCallback = callback;
    }
}
