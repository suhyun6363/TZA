package kr.ac.duksung.mycol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private HomeFragment fragmentHome = new HomeFragment();
    private RecommendFragment fragmentRecommend = new RecommendFragment();
    private MakeupFragment fragmentMakeup = new MakeupFragment();
    private MypageFragment fragmentMypage = new MypageFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.menu_frame_layout, fragmentHome).commitAllowingStateLoss();

        BottomNavigationView bottomNavigationView = findViewById(R.id.menu_buttom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());


    }

    class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            switch (menuItem.getItemId()) {
                case R.id.menu_home:
                    transaction.replace(R.id.menu_frame_layout, fragmentHome).commitAllowingStateLoss();
                    break;
                case R.id.menu_recommend:
                    transaction.replace(R.id.menu_frame_layout, fragmentRecommend).commitAllowingStateLoss();
                    break;
                case R.id.menu_makeup:
                    transaction.replace(R.id.menu_frame_layout, fragmentMakeup).commitAllowingStateLoss();
                    break;
                case R.id.menu_mypage:
                    transaction.replace(R.id.menu_frame_layout, fragmentMypage).commitAllowingStateLoss();
                    break;
            }
            return true;
        }
    }
}
