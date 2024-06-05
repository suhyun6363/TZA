package kr.ac.duksung.mycol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanQRActivity extends AppCompatActivity {
    private static final String TAG = "ScanQRActivity";
    private IntentIntegrator qrScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "카메라 권한이 부여되지 않았습니다. 권한 요청 중.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            Log.d(TAG, "카메라 권한이 있습니다. QR 스캐너를 시작합니다.");
            initiateQRScanner();
        }
    }

    private void initiateQRScanner() {
        qrScan = new IntentIntegrator(this);
        qrScan.setOrientationLocked(false);
        qrScan.setPrompt("QR 코드를 스캔해주세요.");
        qrScan.initiateScan();
        Log.d(TAG, "QR 스캐너가 시작되었습니다.");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "카메라 권한이 허용되었습니다. QR 스캔을 시작할 수 있습니다.");
            initiateQRScanner();
        } else {
            Log.d(TAG, "카메라 권한이 거부되었습니다. QR 스캔을 진행할 수 없습니다.");
            Toast.makeText(this, "QR 코드 스캔을 위해 카메라 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d(TAG, "QR 스캔이 취소되었습니다.");
                Toast.makeText(this, "취소됨", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Log.d(TAG, "QR 스캔 성공. 결과: " + result.getContents());
                Toast.makeText(this, "진단 결과(스캔 결과): " + result.getContents(), Toast.LENGTH_LONG).show();
                handleScanResult(result.getContents());
            }
        } else {
            Log.d(TAG, "스캔 결과가 없습니다.");
        }
    }

    private void handleScanResult(String scanResult) {
        Log.d(TAG, "진단 결과를 DiagnosticActivity로 전달.");
        Intent intent = new Intent(this, DiagnosticActivity.class);
        intent.putExtra("scan_result", scanResult);
        startActivity(intent);
        finish();
    }
}
