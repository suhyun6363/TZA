package kr.ac.duksung.mycol;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.mediapipe.tasks.vision.core.RunningMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kr.ac.duksung.mycol.databinding.ActivityArmakeupBinding;

public class ARMakeupActivity extends AppCompatActivity implements FaceLandmarkerHelper.LandmarkerListener, OnSelectedListener {

    private ActivityArmakeupBinding binding;
    private FaceLandmarkerHelper faceLandmarkerHelper;
    private MainViewModel viewModel;
    private boolean isBlushMakeup = false;
    private boolean isLipMakeup = false;
    private Bitmap bitmap;
    private ExecutorService backgroundExecutor;
    private ArrayList<String> savedImagePaths = new ArrayList<>();  // 저장된 이미지 경로를 저장할 리스트

    private static final String TAG = "ARMakeupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 뷰 바인딩 초기화
        binding = ActivityArmakeupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tzalogo);

        binding.blushIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorOptionFragment bottomSheet = new ColorOptionFragment("베이스");
                bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            }
        });

        binding.lipstickIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorOptionFragment bottomSheet = new ColorOptionFragment("립");
                bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            }
        });

        // ViewModelProvider를 통해 뷰모델을 초기화합니다.
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        Intent intent = getIntent();
        String imageFilePath = intent.getStringExtra("imageFilePath");
        if (imageFilePath != null) {
            bitmap = BitmapFactory.decodeFile(imageFilePath);
            binding.imageView.setImageBitmap(bitmap);

            // 백그라운드 작업을 처리할 ExecutorService를 초기화합니다.
            backgroundExecutor = Executors.newSingleThreadExecutor();

            // Initialize FaceLandmarkerHelper
            backgroundExecutor.execute(() -> {
                faceLandmarkerHelper = new FaceLandmarkerHelper(
                        viewModel.getCurrentMinFaceDetectionConfidence(),
                        viewModel.getCurrentMinFaceTrackingConfidence(),
                        viewModel.getCurrentMinFacePresenceConfidence(),
                        viewModel.getCurrentMaxFaces(),
                        viewModel.getCurrentDelegate(),
                        RunningMode.IMAGE,
                        this,
                        this
                );

                // Run face detection after initialization
                runFaceDetection(bitmap);
            });
        }
    }

    private void runFaceDetection(Bitmap bitmap) {
        Log.d("ARMakeupActivity", "Running face detection");
        if (faceLandmarkerHelper != null) {
            faceLandmarkerHelper.detectImage(bitmap);
        } else {
            Log.e("ARMakeupActivity", "FaceLandmarkerHelper is not initialized");
        }
    }

    @Override
    public void onError(String error, int errorCode) {
        runOnUiThread(() -> {
            Log.e("ARMakeupActivity", "Face detection error: " + error);
        });
    }

    @Override
    public void onResults(FaceLandmarkerHelper.ResultBundle resultBundle) {
        runOnUiThread(() -> {
            if (resultBundle != null && resultBundle.getResult().faceLandmarks().size() > 0) {
                binding.overlayView.setResults(
                        resultBundle.getResult(),
                        bitmap,
                        resultBundle.getInputImageHeight(),
                        resultBundle.getInputImageWidth(),
                        RunningMode.IMAGE
                );
                showToast(this, "얼굴 인식 완료");
                Log.d("ARMakeupActivity", "Face detected");
            } else {
                showToast(this, "얼굴 인식 실패");
                Log.d("ARMakeupActivity", "No face detected");
            }
        });
    }

    @Override
    public void onEmpty() {
        runOnUiThread(() -> {
            showToast(this, "얼굴 인식 실패");
            Log.d("ARMakeupActivity", "No face detected");
        });
    }

    @Override
    public void onColorSelected(String selectedCategory, int color) {
        switch (selectedCategory) {
            case "베이스":
                // 블러셔 색상 선택
                binding.overlayView.setBlushColor(color);
                break;
            case "립":
                // 립 색상 선택
                binding.overlayView.setLipColor(color);
                break;
            default:
                // 기타 타입 처리
                break;
        }
    }

    @Override
    public void onMakeupSelected(String selectedCategory) {
        switch (selectedCategory) {
            case "베이스":
                isBlushMakeup = true;
                break;
            case "립":
                isLipMakeup = true;
                break;
            default:
                break;
        }
        // 상태를 OverlayView에 전달
        binding.overlayView.setMakeupStates(isBlushMakeup, isLipMakeup);
    }

    @Override
    public void onNoneSelected(String selectedCategory) {
        switch (selectedCategory) {
            case "베이스":
                isBlushMakeup = false;
                break;
            case "립":
                isLipMakeup = false;
                break;
            default:
                break;
        }
        // 상태를 OverlayView에 전달
        binding.overlayView.setMakeupStates(isBlushMakeup, isLipMakeup);
        binding.overlayView.invalidate();
    }

    // 뒤로가기 버튼 클릭 핸들러
    public void onBackButtonClick(View view) {
        onBackPressed();
    }

    // 저장 버튼 클릭 핸들러
    public void onSaveButtonClick(View view) {
        // OverlayView와 ImageView를 합친 비트맵을 저장
        saveMakeupOverlayImage();
    }

    // OverlayView와 ImageView를 합친 비트맵을 저장하는 메서드
    private void saveMakeupOverlayImage() {
        // ImageView와 OverlayView의 크기 확인
        int width = binding.imageView.getWidth();
        int height = binding.imageView.getHeight();

        // 결합된 비트맵 생성
        Bitmap combinedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(combinedBitmap);

        // ImageView의 내용을 그리기
        binding.imageView.draw(canvas);

        // OverlayView의 내용을 그리기
        binding.overlayView.draw(canvas);

        // 결합된 비트맵을 파일로 저장
        File photoFile = new File(getOutputDirectory(), new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + "_makeup.jpg");
        try (FileOutputStream outputStream = new FileOutputStream(photoFile)) {
            combinedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            showToast(this, "이미지 저장 완료");
            String savedImagePath = photoFile.getAbsolutePath();
            savedImagePaths.add(savedImagePath);
        } catch (IOException e) {
            e.printStackTrace();
            showToast(this, "이미지 저장 실패");
        }
    }

    // 저장할 디렉토리 가져오기
    private File getOutputDirectory() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "TZA");
        if (!directory.exists()) directory.mkdirs();
        return directory;
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra("savedImagePaths", savedImagePaths);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra("savedImagePaths", savedImagePaths);
        setResult(RESULT_OK, resultIntent);
        super.onDestroy();
    }

    private void showToast(Context context, String message) {
        // 커스텀 토스트 레이아웃 인플레이트
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        // 메시지를 설정
        TextView textView = layout.findViewById(R.id.toast_text);
        textView.setText(message);

        // 토스트 생성 및 표시
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}

