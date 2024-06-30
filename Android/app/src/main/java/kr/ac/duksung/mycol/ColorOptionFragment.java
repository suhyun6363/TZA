package kr.ac.duksung.mycol;
// BottomSheetFragment.java

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import android.widget.TextView;

public class ColorOptionFragment extends BottomSheetDialogFragment {

    private RecyclerView colorRecyclerView;
    private ColorAdapter colorAdapter;
    private TabLayout tabLayout;
    private FirebaseFirestore db;
    private List<ColorItem> itemList;
    private String selectedResult = "Spring warm light"; // 기본값 설정
    private String selectedCategory;
    private OverlayView overlayView;
    private OnSelectedListener selectedListener;
    private TextView textView;

    private static final String TAG = "ColorOptionFragment";

    public ColorOptionFragment(String selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Activity가 인터페이스를 구현하는지 확인
        selectedListener = (OnSelectedListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        selectedListener = null;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 다이얼로그가 시작될 때 배경을 투명하게 설정
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_selection_layout, container, false);

        colorRecyclerView = view.findViewById(R.id.colorRecyclerView);
        tabLayout = view.findViewById(R.id.tabLayout);
        textView = view.findViewById(R.id.categoryTextView);

        textView.setText(selectedCategory);
        if (selectedCategory == "베이스") {
            textView.setText("블러셔");
        }

        // RecyclerView 설정
        colorRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // 아이템 간격 조정 (예: 4dp 간격으로 설정)
        int spaceInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        colorRecyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(spaceInPixels));


        db = FirebaseFirestore.getInstance(); // FirebaseFirestore 객체 초기화
        itemList = new ArrayList<>();

        colorAdapter = new ColorAdapter(itemList, selectedCategory, selectedListener);
        colorRecyclerView.setAdapter(colorAdapter);

        // TabLayout 설정
        tabLayout.addTab(tabLayout.newTab().setText("Spring warm light"));
        tabLayout.addTab(tabLayout.newTab().setText("Spring warm bright"));
        tabLayout.addTab(tabLayout.newTab().setText("Summer cool light"));
        tabLayout.addTab(tabLayout.newTab().setText("Summer cool mute"));
        tabLayout.addTab(tabLayout.newTab().setText("Autumn warm mute"));
        tabLayout.addTab(tabLayout.newTab().setText("Autumn warm deep"));
        tabLayout.addTab(tabLayout.newTab().setText("Winter cool bright"));
        tabLayout.addTab(tabLayout.newTab().setText("Winter cool deep"));

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                View tabView = createTabView(tab.getText().toString()).getCustomView();
                tab.setCustomView(tabView);
            }
        }

        TabLayout.Tab initialTab = tabLayout.getTabAt(0);
        if (initialTab != null) {
            initialTab.select();
            View customView = initialTab.getCustomView();
            if (customView != null) {
                TextView tabTextView = customView.findViewById(R.id.tabTextView);
                tabTextView.setTextColor(getResources().getColor(android.R.color.black)); // 초기 선택된 탭의 텍스트 색상을 블랙으로 변경
            }
        }
        // TabLayout1 탭 선택 리스너 설정
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedResult = tab.getText().toString();
                fetchProductsFromFirestore(); // 탭 선택 시 데이터 가져오기 호출
                TextView tabTextView = tab.getCustomView().findViewById(R.id.tabTextView);
                tabTextView.setTextColor(getResources().getColor(android.R.color.black));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView tabTextView = tab.getCustomView().findViewById(R.id.tabTextView);
                tabTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });


        tabLayout.getTabAt(0).select(); // "Spring warm light" 탭 선택
        fetchProductsFromFirestore(); // 기본값으로 데이터 로드

        return view;
    }
    private TabLayout.Tab createTabView(String text) {
        TabLayout.Tab tab = tabLayout.newTab();
        View tabView = LayoutInflater.from(getContext()).inflate(R.layout.custom_tab, null);
        TextView tabTextView = tabView.findViewById(R.id.tabTextView);
        tabTextView.setText(text);
        tab.setCustomView(tabView);
        return tab;
    }
    private void fetchProductsFromFirestore() {
        // Firestore 쿼리 빌더 초기화
        com.google.firebase.firestore.Query query = db.collection("new_data")
                .whereGreaterThanOrEqualTo("average_rate", 4.7)
                .whereEqualTo("result", selectedResult)
                .whereEqualTo("category_list2", selectedCategory + "메이크업")
                .whereIn("moisturizing", Arrays.asList(0, 1))
                .limit(10);

        // Firestore 쿼리 실행
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            itemList.clear();
                            HashSet<String> productNames = new HashSet<>();
                            int count = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String productName = document.getString("name");
                                String optionName = document.getString("option_name");
                                List<Double> optionRgbList = (List<Double>) document.get("option_rgb"); // option_rgb 필드를 배열로 가져옴

                                // optionRgbList를 List<Long>로 변환
                                List<Long> optionRgbLongList = null;
                                if (optionRgbList != null) {
                                    optionRgbLongList = new ArrayList<>();
                                    for (Object rgbValue : optionRgbList) {
                                        if (rgbValue instanceof Long) {
                                            optionRgbLongList.add((Long) rgbValue);
                                        } else if (rgbValue instanceof Double) {
                                            optionRgbLongList.add(((Double) rgbValue).longValue());
                                        } else {
                                            // 다른 타입에 대한 처리
                                        }
                                    }
                                }

                                // List<Long>을 List<Integer>로 변환
                                List<Integer> optionRgbIntList = null;
                                if (optionRgbLongList != null) {
                                    optionRgbIntList = new ArrayList<>();
                                    for (Long rgbValue : optionRgbLongList) {
                                        optionRgbIntList.add(rgbValue.intValue());
                                    }
                                }

                                // optionRgbList를 문자열로 변환
                                String optionRgb = null;
                                if (optionRgbList != null) {
                                    optionRgb = optionRgbList.toString(); // 배열을 문자열로 변환
                                }

                                Log.d(TAG, "Product Name: " + productName + ", Option Name: " + optionName + ", Option RGB: " + optionRgb);

                                if (productName != null && optionName != null && !productNames.contains(productName)) {
                                    ColorItem colorItem = new ColorItem(optionRgbIntList, productName, optionName);
                                    itemList.add(colorItem);
                                    productNames.add(productName);
                                    count++;
                                }
                                if (count >= 10)
                                    break;
                            }
                            colorAdapter.notifyDataSetChanged();
                            colorRecyclerView.scrollToPosition(0);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            Toast.makeText(getContext(), "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

