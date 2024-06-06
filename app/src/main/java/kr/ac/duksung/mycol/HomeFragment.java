// HomeFragment.java
package kr.ac.duksung.mycol;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    public static final int REQUEST_SCAN_QR = 1;

    private Button scanQRButton;
    private TextView scanQRResult;
    private String scannedResult;
    private ViewPager viewPager;
    private ImagePagerAdapter imagePagerAdapter;
    private LinearLayout indicatorLayout; // 인디케이터를 위한 레이아웃 추가
    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        scanQRButton = rootView.findViewById(R.id.scanQRButton);
        scanQRResult = rootView.findViewById(R.id.scanQRResult);
        viewPager = rootView.findViewById(R.id.viewPager);
        //indicatorLayout = rootView.findViewById(R.id.indicatorLayout);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

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

        // 스캔 결과가 있는 경우에도 textview와 이미지 업데이트
        if (scannedResult != null) {
            setScanResult(scannedResult);
        }

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

    public void setScanResult(String scanResult) {
        Log.d("HomeFragment", "Received scan result: " + scanResult);

        scannedResult = scanResult;

        if (scanQRResult != null) {
            scanQRResult.setText(scannedResult);
        } else {
            Log.e("HomeFragment", "scanQRResult TextView is null.");
        }

        // 이미지 업데이트
        updateImageView(scanResult);

        // ViewModel에 결과 설정
        sharedViewModel.setScannedResult(scanResult);
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
                //int resId2;
                if (resourceName.startsWith("n")) {
                    // resourceName이 "n"으로 시작하는 경우, "drawable/neutral" 이미지를 사용
                    resId1 = context.getResources().getIdentifier("neutral", "drawable", context.getPackageName());
                   // resId2 = context.getResources().getIdentifier("neutral2", "drawable", context.getPackageName());
                } else {
                    resId1 = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
                    //resId2 = context.getResources().getIdentifier(resourceName + "2", "drawable", context.getPackageName());
                }
//                if (resId1 != 0 && resId2 != 0) {
//                    imageResIds.add(resId1);
//                    imageResIds.add(resId2);
//                    imagePagerAdapter.updateImage(imageResIds);
//
//                    // 데이터 변경 후 viewpager 강제 재로드
//                    imagePagerAdapter.notifyDataSetChanged();
//                    viewPager.setAdapter(imagePagerAdapter);
//                }
                if (resId1 != 0 ) {
                    imageResIds.add(resId1);
                    //imageResIds.add(resId2);
                    imagePagerAdapter.updateImage(imageResIds);

                    // 데이터 변경 후 viewpager 강제 재로드
                    imagePagerAdapter.notifyDataSetChanged();
                    viewPager.setAdapter(imagePagerAdapter);
                }
            }
        }
    }

    // 선택된 페이지에 해당하는 인디케이터의 색상을 변경하는 메서드
//    private void updateIndicators(int position) {
//        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
//            View indicator = indicatorLayout.getChildAt(i);
//            if (i == position) {
//                // 선택된 페이지일 때 선택된 원의 배경색을 설정합니다.
//                indicator.setBackgroundResource(R.drawable.selected_circle_shape);
//            } else {
//                // 선택되지 않은 페이지일 때 선택되지 않은 원의 배경색을 설정합니다.
//                indicator.setBackgroundResource(R.drawable.unselected_circle_shape);
//            }
//        }
//    }
}
