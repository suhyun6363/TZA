package kr.ac.duksung.mycol;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ScanQRActivity extends AppCompatActivity {
    private static final String TAG = "ScanQRActivity";
    public static final int REQUEST_SCAN_QR = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "카메라 권한이 부여되지 않았습니다. 권한 요청 중.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            Log.d(TAG, "카메라 권한이 있습니다. QR 스캐너를 시작합니다.");
            startCustomScanActivity();
        }
    }

    private void startCustomScanActivity() {
        Intent intent = new Intent(this, CustomScanActivity.class);
        startActivityForResult(intent, REQUEST_SCAN_QR);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "카메라 권한이 허용되었습니다. QR 스캔을 시작할 수 있습니다.");
            startCustomScanActivity();
        } else {
            Log.d(TAG, "카메라 권한이 거부되었습니다. QR 스캔을 진행할 수 없습니다.");
            showToast("QR 코드 스캔을 위해 카메라 권한이 필요합니다.");
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SCAN_QR && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("scan_result")) {
                String scanResult = data.getStringExtra("scan_result");
                Log.d(TAG, "Received scan result: " + scanResult);

                // MainActivity로 결과 전달 (이미 실행 중인 MainActivity의 인스턴스를 재사용)
                Intent resultIntent = new Intent(this, MainActivity.class);
                resultIntent.putExtra("scan_result", scanResult);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(resultIntent);
                finish();
            }
        }
    }

    private void showToast(String message) {
        // 커스텀 토스트 레이아웃 인플레이트
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container));

        // 메시지를 설정
        TextView textView = layout.findViewById(R.id.toast_text);
        textView.setText(message);

        // 토스트 생성 및 표시
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
