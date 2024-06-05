package kr.ac.duksung.mycol;

import static kr.ac.duksung.mycol.HomeFragment.REQUEST_SCAN_QR;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String SELECTED_ITEM_KEY = "selected_item_key";
    private static final String TAG = "MainActivity";
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private HomeFragment fragmentHome = new HomeFragment();
    private RecommendFragment fragmentRecommend = new RecommendFragment();
    private MypageFragment fragmentMypage = new MypageFragment();
    private BottomNavigationView bottomNavigationView;
    private int selectedItemId = R.id.menu_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.menu_buttom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());

        // onCreate에서 인텐트 확인하여 스캔 결과 처리
        handleIntent(getIntent());
    }

    class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            selectedItemId = menuItem.getItemId();

            switch (menuItem.getItemId()) {
                case R.id.menu_home:
                    transaction.replace(R.id.menu_frame_layout, fragmentHome).commitAllowingStateLoss();
                    break;
                case R.id.menu_recommend:
                    transaction.replace(R.id.menu_frame_layout, fragmentRecommend).commitAllowingStateLoss();
                    break;
                case R.id.menu_makeup:
                    Intent intent = new Intent(MainActivity.this, ARMakeupActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.menu_mypage:
                    transaction.replace(R.id.menu_frame_layout, fragmentMypage).commitAllowingStateLoss();
                    break;
            }
            // transaction.commitAllowingStateLoss();
            return true;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("scan_result")) {
            String scanResult = intent.getStringExtra("scan_result");
            Log.d(TAG, "Received scan result: " + scanResult);

            // 이미 존재하는 HomeFragment의 인스턴스를 사용하여 텍스트를 업데이트
            if (fragmentHome != null) {
                fragmentHome.setScanResult(scanResult);
                fragmentHome.updateImageView(scanResult);
            }
        }
    }
}

