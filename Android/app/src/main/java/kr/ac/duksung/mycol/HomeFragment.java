package kr.ac.duksung.mycol;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    public static final int REQUEST_SCAN_QR = 1;

    private Button scanQRButton;
    private TextView scanQRResult;
    private String scannedResult;
    private ViewPager viewPager;
    private ImagePagerAdapter imagePagerAdapter;
    private SharedViewModel sharedViewModel;

    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        scanQRButton = rootView.findViewById(R.id.scanQRButton);
        //scanQRResult = rootView.findViewById(R.id.scanQRResult);
        viewPager = rootView.findViewById(R.id.viewPager);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Firebase Database 초기화
        databaseReference = FirebaseDatabase.getInstance("https://mycol-6b49b-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        // 이미지 어댑터 초기화
        imagePagerAdapter = new ImagePagerAdapter(getChildFragmentManager());
        // default_image를 사용하여 초기화
        List<Integer> defaultImageResIds = new ArrayList<>();
        Context context = getContext();
        if (context != null) {
            int defaultImageResId = context.getResources().getIdentifier("default_image", "drawable", context.getPackageName());
            defaultImageResIds.add(defaultImageResId);
        }
        imagePagerAdapter.updateImage(defaultImageResIds);
        viewPager.setAdapter(imagePagerAdapter);

        scanQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ScanQRActivity.class);
                startActivityForResult(intent, REQUEST_SCAN_QR);
            }
        });

        // 사용자 personal_color 불러오기
        loadPersonalColor();

        // ViewPager의 페이지 변경을 감지하고 선택된 페이지에 해당하는 인디케이터의 색상을 변경
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // 페이지가 스크롤되는 동안 호출됩니다.
            }

            @Override
            public void onPageSelected(int position) {
                // 새로운 페이지가 선택될 때 호출됩니다.
                //updateIndicators(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // 페이지 스크롤 상태가 변경될 때 호출됩니다.
            }
        });

        return rootView;
    }

    private void loadPersonalColor() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = databaseReference.child("users").child(userId).child("personal_color");
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String personalColor = snapshot.getValue(String.class);
                        if (personalColor != null) {
                            scannedResult = personalColor;
                            setScanResult(personalColor);
                        }
                    } else {
                        Log.d("HomeFragment", "No personal_color found for user.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("HomeFragment", "Failed to read personal_color from database.", error.toException());
                }
            });
        } else {
            Log.e("HomeFragment", "User is not authenticated.");
        }
    }

    public void setScanResult(String scanResult) {
        Log.d("HomeFragment", "Received scan result: " + scanResult);

        scannedResult = scanResult;

        // 스캔 결과를 설정하는 부분
//        if (scanQRResult != null) {
//            if (scannedResult.startsWith("N-")) {
//                scanQRResult.setText("Neutral Tone");
//            } else {
//                scanQRResult.setText(scannedResult);
//            }
//        } else {
//            Log.e("HomeFragment", "scanQRResult TextView is null.");
//        }


        // 이미지 업데이트
        updateImageView(scanResult);

        // ViewModel에 결과 설정
        sharedViewModel.setScannedResult(scanResult);

        // Firebase Database에 저장
        saveScanResultToDatabase(scanResult);
    }

    @Override
    public void onResume() {
        super.onResume();

        // 스캔 결과가 있는 경우에도 이미지 업데이트
        if (scannedResult != null) {
            updateImageView(scannedResult);
        }
    }

    public void updateImageView(String scanResult) {
        // 이미지 어댑터에 이미지 업데이트
        if (imagePagerAdapter != null) {
            Context context = getContext();
            if (context != null) {
                // 공백을 "_"로 대체하고 소문자로 변환하여 이미지 이름 생성
                String resourceName = scanResult.replaceAll("\\s+", "_").toLowerCase();
                List<Integer> imageResIds = new ArrayList<>();
                int resId1;
                if (resourceName.startsWith("n")) {
                    // resourceName이 "n"으로 시작하는 경우, "drawable/neutral" 이미지를 사용
                    resId1 = context.getResources().getIdentifier("neutral", "drawable", context.getPackageName());
                } else {
                    resId1 = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
                }

                if (resId1 != 0) {
                    imageResIds.add(resId1);
                    imagePagerAdapter.updateImage(imageResIds);

                    // 데이터 변경 후 viewpager 강제 재로드
                    imagePagerAdapter.notifyDataSetChanged();
                    viewPager.setAdapter(imagePagerAdapter);
                }
            }
        }
    }

    private void saveScanResultToDatabase(String scanResult) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = databaseReference.child("users").child(userId).child("personal_color");
            userRef.setValue(scanResult).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("HomeFragment", "Scan result saved to database.");
                } else {
                    Log.e("HomeFragment", "Failed to save scan result to database.", task.getException());
                }
            });
        } else {
            Log.e("HomeFragment", "User is not authenticated.");
        }
    }
}
