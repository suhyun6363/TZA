package kr.ac.duksung.mycol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import kr.ac.duksung.mycol.databinding.ActivityArmakeupBinding;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.tasks.vision.core.RunningMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.List;

// CameraActivity 클래스는 카메라를 초기화하고, 얼굴 랜드마커를 설정하는 역할을 합니다.
public class ARMakeupActivity extends AppCompatActivity implements FaceLandmarkerHelper.LandmarkerListener, OnColorSelectedListener {

    private static final String TAG = "Face Landmarker";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_GALLERY = 201;

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
    private LinearLayout blushIcon, lipstickIcon, galleryButton, captureButton, rotateButton;
    private OverlayView overlayView;
    private ImageCapture imageCapture;
    private String lastPhotoPath;  // 방금 찍은 사진 경로를 저장하기 위한 변수

    // onCreate는 액티비티가 생성될 때 호출되며, 초기 설정 및 뷰 바인딩을 처리합니다.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArmakeupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tzalogo);

        // OverlayView 초기화
        overlayView = findViewById(R.id.overlayView);

        // ViewModelProvider를 통해 뷰모델을 초기화합니다.
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // 백그라운드 작업을 처리할 ExecutorService를 초기화합니다.
        backgroundExecutor = Executors.newSingleThreadExecutor();

        // 카메라 권한이 있는지 확인하고, 없다면 요청합니다.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            binding.previewView.post(() -> setUpCamera());
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

        blushIcon = findViewById(R.id.blushIcon);
        lipstickIcon = findViewById(R.id.lipstickIcon);

        blushIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorOptionFragment bottomSheet = new ColorOptionFragment("베이스");
                bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            }
        });

        lipstickIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorOptionFragment bottomSheet = new ColorOptionFragment("립");
                bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            }
        });

        // 버튼 초기화 및 클릭 리스너 설정
        galleryButton = findViewById(R.id.galleryButton);
        captureButton = findViewById(R.id.captureButton);
        rotateButton = findViewById(R.id.rotateButton);

        galleryButton.setOnClickListener(v -> openGallery());
        captureButton.setOnClickListener(v -> capturePhoto());
        rotateButton.setOnClickListener(v -> rotateCamera());
    }

    public OverlayView getOverlayView() {
        return overlayView;
    }

    @Override
    public void onColorSelected(String selectedCategory, int color) {
        switch (selectedCategory) {
            case "베이스":
                // 블러셔 색상 선택
                overlayView.setBlushColor(color);
                break;
            case "립":
                // 립 색상 선택
                overlayView.setLipColor(color);
                break;
            default:
                // 기타 타입 처리
                break;
        }
    }

    // 갤러리 열기
    private void openGallery() {
        if (lastPhotoPath == null || lastPhotoPath.isEmpty()) {
            Toast.makeText(this, "캡쳐된 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(lastPhotoPath);
        Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(photoUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Intent를 사용할 수 있는 앱이 있는지 확인
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "No app available to view the photo.", Toast.LENGTH_SHORT).show();
        }
    }

    // 이미지 캡처
    private void capturePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + "_photo.png");

        lastPhotoPath = photoFile.getAbsolutePath();  // 방금 찍은 사진 경로 저장

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                // 이미지가 성공적으로 저장된 경우
                // overlay된 비트맵을 가져와서 저장
                Bitmap overlayBitmap = getOverlayBitmap();
                saveOverlayBitmap(photoFile, overlayBitmap);
                Toast.makeText(ARMakeupActivity.this, "캡쳐 성공, 저장 완료", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                // 이미지 저장 중 오류가 발생한 경우
                Log.e(TAG, "캡쳐 실패" + exception.getMessage(), exception);
            }
        });
    }

    // overlay에 표시된 그래픽을 비트맵으로 변환하는 메서드
    private Bitmap getOverlayBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(overlayView.getWidth(), overlayView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        overlayView.draw(canvas);
        return bitmap;
    }

    // overlay된 비트맵을 저장하는 메서드
    private void saveOverlayBitmap(File photoFile, Bitmap overlayBitmap) {
        try {
            FileOutputStream fos = new FileOutputStream(photoFile);
            overlayBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to save overlay bitmap", e);
        }
    }

    // 카메라 회전
    private void rotateCamera() {
        cameraFacing = (cameraFacing == CameraSelector.LENS_FACING_FRONT) ?
                CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT;
        setUpCamera();
    }

    // 카메라 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode ==REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
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
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(binding.previewView.getDisplay().getRotation())
                .build();

        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(binding.previewView.getDisplay().getRotation())
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageCapture = new ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(binding.previewView.getDisplay().getRotation())
                .build();

        imageAnalyzer.setAnalyzer(backgroundExecutor, this::detectFace);

        cameraProvider.unbindAll();

        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer, imageCapture);
            preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
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
            imageAnalyzer.setTargetRotation(binding.previewView.getDisplay().getRotation());
        }
    }

    // 얼굴 감지 결과를 처리하는 메서드
    @Override
    public void onResults(@NonNull FaceLandmarkerHelper.ResultBundle resultBundle) {
        runOnUiThread(() -> {
            if (binding != null) {
                binding.overlayView.setResults(
                        resultBundle.getResult(),
                        resultBundle.getBitmap(),
                        resultBundle.getInputImageHeight(),
                        resultBundle.getInputImageWidth(),
                        RunningMode.LIVE_STREAM
                );
                binding.overlayView.invalidate();
            }
        });
    }

    // 얼굴 감지 결과가 없을 때 호출되는 메서드
    @Override
    public void onEmpty() {
        binding.overlayView.clear();
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