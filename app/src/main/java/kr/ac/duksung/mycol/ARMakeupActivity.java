package kr.ac.duksung.mycol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import kr.ac.duksung.mycol.databinding.ActivityArmakeupBinding;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.tasks.vision.core.RunningMode;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// CameraActivity 클래스는 카메라를 초기화하고, 얼굴 랜드마커를 설정하는 역할을 합니다.
public class ARMakeupActivity extends AppCompatActivity implements FaceLandmarkerHelper.LandmarkerListener {

    private static final String TAG = "Face Landmarker";
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private ActivityArmakeupBinding binding;
    private FaceLandmarkerHelper faceLandmarkerHelper;
    private MainViewModel viewModel;
    // private FaceBlendshapesResultAdapter faceBlendshapesResultAdapter;
    private Preview preview;
    private ImageAnalysis imageAnalyzer;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private int cameraFacing = CameraSelector.LENS_FACING_FRONT;
    private ExecutorService backgroundExecutor;

    // onCreate는 액티비티가 생성될 때 호출되며, 초기 설정 및 뷰 바인딩을 처리합니다.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArmakeupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ViewModelProvider를 통해 뷰모델을 초기화합니다.
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // 백그라운드 작업을 처리할 ExecutorService를 초기화합니다.
        backgroundExecutor = Executors.newSingleThreadExecutor();

        // 카메라 권한이 있는지 확인하고, 없다면 요청합니다.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            binding.viewFinder.post(() -> setUpCamera());
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        // 백그라운드에서 FaceLandmarkerHelper를 초기화합니다.
        backgroundExecutor.execute(() -> {
            faceLandmarkerHelper = new FaceLandmarkerHelper(
                    viewModel.getCurrentMinFaceDetectionConfidence(),
                    viewModel.getCurrentMinFaceTrackingConfidence(),
                    viewModel.getCurrentMinFacePresenceConfidence(),
                    viewModel.getCurrentMaxFaces(),
                    viewModel.getCurrentDelegate(),
                    RunningMode.LIVE_STREAM,
                    getApplicationContext(),
                    ARMakeupActivity.this
            );
        });
    }

    // UI 업데이트 메서드. 현재 설정 값을 UI에 반영합니다.
    private void updateControlsUi() {

        // 백그라운드에서 FaceLandmarkerHelper를 초기화합니다.
        backgroundExecutor.execute(() -> {
            faceLandmarkerHelper.clearFaceLandmarker();
            faceLandmarkerHelper.setupFaceLandmarker();
        });

        // 화면 오버레이를 초기화합니다.
        binding.overlay.clear();
    }

    // 카메라를 설정하는 메서드
    @SuppressLint("MissingPermission")
    private void setUpCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (Exception e) {
                Log.e(TAG, "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // 카메라의 유스 케이스를 바인딩하는 메서드
    @SuppressLint("UnsafeOptInUsageError")
    private void bindCameraUseCases() {
        if (cameraProvider == null) {
            throw new IllegalStateException("Camera initialization failed.");
        }

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(cameraFacing).build();

        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(binding.viewFinder.getDisplay().getRotation())
                .build();

        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(binding.viewFinder.getDisplay().getRotation())
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalyzer.setAnalyzer(backgroundExecutor, this::detectFace);

        cameraProvider.unbindAll();

        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer);
            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
        }
    }

    // 얼굴을 감지하는 메서드
    private void detectFace(ImageProxy imageProxy) {
        faceLandmarkerHelper.detectLiveStream(
                imageProxy,
                cameraFacing == CameraSelector.LENS_FACING_FRONT
        );
    }

    // 구성 변경이 발생했을 때 호출되는 메서드
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (imageAnalyzer != null) {
            imageAnalyzer.setTargetRotation(binding.viewFinder.getDisplay().getRotation());
        }
    }

    // 얼굴 감지 결과를 처리하는 메서드
    @Override
    public void onResults(@NonNull FaceLandmarkerHelper.ResultBundle resultBundle) {
        runOnUiThread(() -> {
            if (binding != null) {
                binding.overlay.setResults(
                        resultBundle.getResult(),
                        resultBundle.getBitmap(),
                        resultBundle.getInputImageHeight(),
                        resultBundle.getInputImageWidth(),
                        RunningMode.LIVE_STREAM
                );
                binding.overlay.invalidate();
            }
        });
    }

    // 얼굴 감지 결과가 없을 때 호출되는 메서드
    @Override
    public void onEmpty() {
        binding.overlay.clear();
        runOnUiThread(() -> {
            // faceBlendshapesResultAdapter.updateResults(null);
            // faceBlendshapesResultAdapter.notifyDataSetChanged();
        });
    }

    // 얼굴 감지 오류가 발생했을 때 호출되는 메서드
    @Override
    public void onError(String error, int errorCode) {
        runOnUiThread(() -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            // faceBlendshapesResultAdapter.updateResults(null);
            // faceBlendshapesResultAdapter.notifyDataSetChanged();

            if (errorCode == FaceLandmarkerHelper.GPU_ERROR) {
                // binding.bottomSheetLayout.spinnerDelegate.setSelection(FaceLandmarkerHelper.DELEGATE_CPU, false);
            }
        });
    }

    // 액티비티가 재개될 때 호출되는 메서드
    @Override
    protected void onResume() {
        super.onResume();

        backgroundExecutor.execute(() -> {
            if (faceLandmarkerHelper.isClose()) {
                faceLandmarkerHelper.setupFaceLandmarker();
            }
        });
    }

    // 액티비티가 일시 정지될 때 호출되는 메서드
    @Override
    protected void onPause() {
        super.onPause();
        if (faceLandmarkerHelper != null) {
            viewModel.setMaxFaces(faceLandmarkerHelper.maxNumFaces);
            viewModel.setMinFaceDetectionConfidence(faceLandmarkerHelper.minFaceDetectionConfidence);
            viewModel.setMinFaceTrackingConfidence(faceLandmarkerHelper.minFaceTrackingConfidence);
            viewModel.setMinFacePresenceConfidence(faceLandmarkerHelper.minFacePresenceConfidence);
            viewModel.setDelegate(faceLandmarkerHelper.currentDelegate);

            backgroundExecutor.execute(faceLandmarkerHelper::clearFaceLandmarker);
        }
    }

    // 액티비티가 종료될 때 호출되는 메서드
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundExecutor != null) {
            backgroundExecutor.shutdown();
            try {
                backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error shutting down background executor", e);
            }
        }
    }
}
