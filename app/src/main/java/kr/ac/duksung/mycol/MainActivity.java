package kr.ac.duksung.mycol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String SELECTED_ITEM_KEY = "selected_item_key";
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

        if (savedInstanceState != null) {
            selectedItemId = savedInstanceState.getInt(SELECTED_ITEM_KEY, R.id.menu_home);
        }

        bottomNavigationView.setSelectedItemId(selectedItemId);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM_KEY, bottomNavigationView.getSelectedItemId());
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
}

