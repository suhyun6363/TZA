package kr.ac.duksung.mycol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;

import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_MAKEUP = 100; // 메이크업 Activity 요청 코드

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private HomeFragment fragmentHome = new HomeFragment();
    private RecommendFragment fragmentRecommend = new RecommendFragment();
    private MakeupFragment fragmentMakeup = new MakeupFragment();
    private MypageFragment fragmentMypage = new MypageFragment();

    private BottomNavigationView bottomNavigationView;
    private int selectedItemId = R.id.menu_home; // 기본 선택 항목 ID
    private int previousItemId = R.id.menu_home; // 메이크업 탭을 누르기 전 선택 항목 ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tzalogo);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.menu_frame_layout, fragmentHome).commitAllowingStateLoss();

        bottomNavigationView = findViewById(R.id.menu_buttom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());


        // onCreate에서 인텐트 확인하여 스캔 결과 처리
        handleIntent(getIntent());

    }

    class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            switch (menuItem.getItemId()) {
                case R.id.menu_home:
                    selectedItemId = menuItem.getItemId(); // 선택된 항목 ID를 저장
                    transaction.replace(R.id.menu_frame_layout, fragmentHome).commitAllowingStateLoss();
                    break;
                case R.id.menu_recommend:
                    selectedItemId = menuItem.getItemId(); // 선택된 항목 ID를 저장
                    transaction.replace(R.id.menu_frame_layout, fragmentRecommend).commitAllowingStateLoss();
                    break;
                case R.id.menu_makeup:
                    previousItemId = selectedItemId; // 메이크업 탭을 누르기 전에 선택된 항목 ID를 저장
                    Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                    startActivityForResult(intent, REQUEST_MAKEUP); // startActivityForResult로 메이크업 Activity 시작
                    return true;
                case R.id.menu_mypage:
                    selectedItemId = menuItem.getItemId(); // 선택된 항목 ID를 저장
                    transaction.replace(R.id.menu_frame_layout, fragmentMypage).commitAllowingStateLoss();
                    break;
            }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MAKEUP) {
            // 메이크업 Activity에서 돌아온 경우, 이전의 선택 상태를 복원
            bottomNavigationView.setSelectedItemId(previousItemId);
        }
    }
}

