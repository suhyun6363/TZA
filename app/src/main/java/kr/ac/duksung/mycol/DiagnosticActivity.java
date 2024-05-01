package kr.ac.duksung.mycol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DiagnosticActivity extends AppCompatActivity {

    private TextView tvScanResult;
    private Button btnRequestMoreInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic);

        tvScanResult = findViewById(R.id.tvScanResult);
        btnRequestMoreInfo = findViewById(R.id.btnRequestMoreInfo);

        // 인텐트에서 스캔 결과 가져오기
        String scanResult = getIntent().getStringExtra("scan_result");
        tvScanResult.setText(scanResult);

        // 추가 정보 요청 버튼 이벤트 처리
        btnRequestMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 추가 정보를 요청하는 로직 구현, 예를 들어 다른 액티비티나 서비스 호출 등
                requestMoreInfo();
            }
        });
    }

    private void requestMoreInfo() {
        // 여기에 추가 정보를 요청하는 코드를 구현
        // 예: API 호출, 다른 화면으로 이동 등
        // 일단은 예제로 Toast 메시지 출력
        Toast.makeText(this, "추가 정보를 요청하였습니다.", Toast.LENGTH_LONG).show();
    }
}
