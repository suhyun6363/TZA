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

public class ARMakeupActivity extends AppCompatActivity implements FaceLandmarkerHelper.LandmarkerListener {

    private static final String TAG = "Face Landmarker";
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private ActivityArmakeupBinding binding;
    private FaceLandmarkerHelper faceLandmarkerHelper;
    private MainViewModel viewModel;
    private Preview preview;
    private ImageAnalysis imageAnalyzer;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private int cameraFacing = CameraSelector.LENS_FACING_FRONT;
    private ExecutorService backgroundExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArmakeupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        backgroundExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            binding.viewFinder.post(() -> setUpCamera());
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.viewFinder.post(() -> setUpCamera());
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void setUpCamera() {
        Log.d(TAG, "Setting up camera...");
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Log.d(TAG, "Camera provider obtained.");
                bindCameraUseCases();
            } catch (Exception e) {
                Log.e(TAG, "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void bindCameraUseCases() {
        Log.d(TAG, "Binding camera use cases...");
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
            Log.d(TAG, "Camera use cases bound.");
            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
        }
    }

    private void detectFace(ImageProxy imageProxy) {
        faceLandmarkerHelper.detectLiveStream(
                imageProxy,
                cameraFacing == CameraSelector.LENS_FACING_FRONT
        );
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (imageAnalyzer != null) {
            imageAnalyzer.setTargetRotation(binding.viewFinder.getDisplay().getRotation());
        }
    }

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

    @Override
    public void onEmpty() {
        binding.overlay.clear();
        runOnUiThread(() -> {
            // faceBlendshapesResultAdapter.updateResults(null);
            // faceBlendshapesResultAdapter.notifyDataSetChanged();
        });
    }

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

    @Override
    protected void onResume() {
        super.onResume();

        backgroundExecutor.execute(() -> {
            if (faceLandmarkerHelper.isClose()) {
                faceLandmarkerHelper.setupFaceLandmarker();
            }
        });
    }

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
