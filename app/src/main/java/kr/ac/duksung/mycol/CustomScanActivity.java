package kr.ac.duksung.mycol;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Arrays;
import java.util.List;

public class CustomScanActivity extends AppCompatActivity {
    private DecoratedBarcodeView barcodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_scan);

        barcodeView = findViewById(R.id.zxing_barcode_scanner);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(Arrays.asList(BarcodeFormat.QR_CODE)));
        barcodeView.setStatusText("");
        barcodeView.initializeFromIntent(getIntent());
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                handleScanResult(result.getText());
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
            }
        });

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomScanActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void handleScanResult(String scanResult) {
        Log.d("CustomScanActivity", "Received scan result: " + scanResult);
        Intent intent = new Intent();
        intent.putExtra("scan_result", scanResult);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}
