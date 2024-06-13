package kr.ac.duksung.mycol;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import android.os.AsyncTask;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_MAKEUP_ACTIVITY = 300;
    private int cameraFacing = CameraSelector.LENS_FACING_FRONT;
    private PreviewView previewView;
    private Preview preview;
    private ImageCapture imageCapture;
    private LinearLayout captureButton, galleryButton, rotateButton;
    private ArrayList<String> imageList = new ArrayList<>(); // 이미지를 저장할 리스트

    private static final String TAG = "CameraActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tzalogo);

        previewView = findViewById(R.id.previewView);
        captureButton = findViewById(R.id.captureButton);
        galleryButton = findViewById(R.id.galleryButton);
        rotateButton = findViewById(R.id.rotateButton);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        galleryButton.setOnClickListener(v -> openGallery());
        captureButton.setOnClickListener(v -> takePhoto());
        rotateButton.setOnClickListener(v -> rotateCamera());
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();

                imageCapture = new ImageCapture.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(cameraFacing).build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraActivity", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // 갤러리 열기
    private void openGallery() {
        if (imageList.isEmpty()) {
            showToast(this, "캡쳐된 이미지가 없습니다.");
            return;
        }

        Intent intent = new Intent(this, GalleryActivity.class);
        intent.putParcelableArrayListExtra("imageUris", getUrisFromPaths());
        startActivity(intent);
    }

    // 파일 경로 리스트를 URI 리스트로 변환
    private ArrayList<Uri> getUrisFromPaths() {
        ArrayList<Uri> uris = new ArrayList<>();
        for (String path : imageList) {
            File file = new File(path);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            uris.add(uri);
        }
        return uris;
    }


    // 카메라 회전
    private void rotateCamera() {
        cameraFacing = (cameraFacing == CameraSelector.LENS_FACING_FRONT) ?
                CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT;
        startCamera();
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                imageList.clear();
                new ProcessImageTask(image).execute();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("CameraActivity", "Photo capture failed: " + exception.getMessage(), exception);
            }
        });
    }

    private class ProcessImageTask extends AsyncTask<Void, Void, String> {
        private ImageProxy image;

        ProcessImageTask(ImageProxy image) {
            this.image = image;
        }

        @Override
        protected String doInBackground(Void... voids) {
            Bitmap bitmap = imageProxyToBitmap(image);
            image.close();
            return saveBitmapToFile(bitmap);
        }

        @Override
        protected void onPostExecute(String filePath) {
            Intent intent = new Intent(CameraActivity.this, ARMakeupActivity.class);
            intent.putExtra("imageFilePath", filePath);
            startActivityForResult(intent, REQUEST_MAKEUP_ACTIVITY);
        }

        private String saveBitmapToFile(Bitmap bitmap) {
            File file = new File(getCacheDir(), "temp_image.jpg");
            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file.getAbsolutePath();
        }
    }



    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        // 이미지 회전 정보 가져오기
        int rotationDegrees = image.getImageInfo().getRotationDegrees();
        return rotateAndFlipBitmap(bitmap, rotationDegrees);
    }

    private Bitmap rotateAndFlipBitmap(Bitmap bitmap, int rotationDegrees) {
        Matrix matrix = new Matrix();
        // 이미지 회전
        matrix.postRotate(rotationDegrees);
        // 전면 카메라일 경우에만 이미지 수평 반전
        if (cameraFacing == CameraSelector.LENS_FACING_FRONT) {
            matrix.postScale(-1, 1, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                showToast(this, "카메라 권한을 요구하는 앱입니다.");
                finish();
            }
        }
    }

    // ARMakeupActivity에서 결과를 받아 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MAKEUP_ACTIVITY && resultCode == RESULT_OK) {
            ArrayList<String> savedImagePaths = data.getStringArrayListExtra("savedImagePaths");
            Log.d(TAG, "savedImagePaths: " + savedImagePaths);
            if (savedImagePaths != null) {
                imageList.addAll(savedImagePaths); // 이미지 리스트에 추가
                Log.d(TAG, "imageList: " + imageList);
            }
        }
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

