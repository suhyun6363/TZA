package kr.ac.duksung.mycol;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;

public class TotalRecommendFragment extends Fragment {
    private static final String TAG = TotalRecommendFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private TabLayout tabLayout1, tabLayout2;
    private List<Product> productList;
    private ProductAdapter adapter;
    private FirebaseFirestore db;
    private String selectedResult = "Spring warm light"; // 기본값 설정
    private String selectedCategory = "립메이크업"; // 기본값 설정

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 프래그먼트의 레이아웃을 inflate합니다.
        View view = inflater.inflate(R.layout.fragment_total_recommend, container, false);

        // RecyclerView, TabLayout 등의 뷰를 가져와서 초기화합니다.
        tabLayout1 = view.findViewById(R.id.tabLayout1);
        tabLayout2 = view.findViewById(R.id.tabLayout2);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productList = new ArrayList<>();
        adapter = new ProductAdapter(productList, getContext());
        recyclerView.setAdapter(adapter);

        // Firestore 인스턴스를 초기화합니다.
        db = FirebaseFirestore.getInstance();

        // TabLayout1에 탭들을 설정합니다.
        tabLayout1.addTab(tabLayout1.newTab().setText("Spring warm light").setContentDescription("Spring warm light"));
        tabLayout1.addTab(tabLayout1.newTab().setText("Spring warm bright").setContentDescription("Spring warm bright"));
        tabLayout1.addTab(tabLayout1.newTab().setText("Summer cool light").setContentDescription("Summer cool light"));
        tabLayout1.addTab(tabLayout1.newTab().setText("Summer cool mute").setContentDescription("Summer cool mute"));
        tabLayout1.addTab(tabLayout1.newTab().setText("Autumn warm mute").setContentDescription("Autumn warm mute"));
        tabLayout1.addTab(tabLayout1.newTab().setText("Autumn warm deep").setContentDescription("Autumn warm deep"));
        tabLayout1.addTab(tabLayout1.newTab().setText("Winter cool bright").setContentDescription("Winter cool bright"));
        tabLayout1.addTab(tabLayout1.newTab().setText("Winter cool deep").setContentDescription("Winter cool deep"));

        // TabLayout2에 탭들을 설정합니다.
        tabLayout2.addTab(tabLayout2.newTab().setText("립").setContentDescription("립"));
        tabLayout2.addTab(tabLayout2.newTab().setText("아이").setContentDescription("아이"));
        tabLayout2.addTab(tabLayout2.newTab().setText("블러셔").setContentDescription("블러셔"));
        // TabLayout1의 각 탭에 사용할 사용자 정의 뷰를 설정합니다.
        for (int i = 0; i < tabLayout1.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout1.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(createTabView(tab.getText().toString()));
            }
        }


        // TabLayout2의 각 탭에 사용할 사용자 정의 뷰를 설정합니다.
        for (int i = 0; i < tabLayout2.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout2.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(createTabView(tab.getText().toString()));
            }
        }

        // 기본 탭 설정 및 기본 데이터 로드
        TabLayout.Tab initialTab = tabLayout1.getTabAt(0);
        if (initialTab != null) {
            initialTab.select();
            View customView = initialTab.getCustomView();
            if (customView != null) {
                TextView tabTextView = customView.findViewById(R.id.tabTextView);
                tabTextView.setTextColor(getResources().getColor(android.R.color.black)); // 초기 선택된 탭의 텍스트 색상을 블랙으로 변경
            }
        }

        // 기본 탭 설정 및 기본 데이터 로드
        TabLayout.Tab initialTab2 = tabLayout2.getTabAt(0);
        if (initialTab2 != null) {
            initialTab2.select();
            View customView = initialTab2.getCustomView();
            if (customView != null) {
                TextView tabTextView = customView.findViewById(R.id.tabTextView);
                tabTextView.setTextColor(getResources().getColor(android.R.color.black)); // 초기 선택된 탭의 텍스트 색상을 블랙으로 변경
            }
        }



        // TabLayout1 탭 선택 리스너를 설정합니다.
        tabLayout1.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedResult = tab.getText().toString();
                fetchProductsFromFirestore();

                // 선택된 탭의 글씨 색을 검정색으로 변경합니다.
                TextView tabTextView = tab.getCustomView().findViewById(R.id.tabTextView);
                tabTextView.setTextColor(getResources().getColor(android.R.color.black));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 이전에 선택되었던 탭의 글씨 색을 기본 색으로 변경합니다.
                TextView tabTextView = tab.getCustomView().findViewById(R.id.tabTextView);
                tabTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

// TabLayout2 탭 선택 리스너를 설정합니다.
        tabLayout2.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String categoryText = tab.getText().toString();
                if (categoryText.equals("전체")) {
                    selectedCategory = "전체";
                } else if (categoryText.equals("블러셔")) {
                    selectedCategory = "베이스메이크업"; // "블러셔" 선택 시 "베이스"로 설정
                } else {
                    selectedCategory = categoryText + "메이크업"; // 카테고리 설정
                }
                fetchProductsFromFirestore();

                // 선택된 탭의 글씨 색을 검정색으로 변경합니다.
                TextView tabTextView = tab.getCustomView().findViewById(R.id.tabTextView);
                tabTextView.setTextColor(getResources().getColor(android.R.color.black));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 이전에 선택되었던 탭의 글씨 색을 기본 색으로 변경합니다.
                TextView tabTextView = tab.getCustomView().findViewById(R.id.tabTextView);
                tabTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 초기값으로 탭을 선택하고 데이터를 가져옵니다.
        tabLayout1.getTabAt(0).select();
        tabLayout2.getTabAt(0).select();
        fetchProductsFromFirestore();

        return view;
    }

    // 탭에 사용할 사용자 정의 뷰를 생성합니다.
    private View createTabView(String title) {
        View tabView = LayoutInflater.from(getContext()).inflate(R.layout.custom_tab, null);
        TextView tabTextView = tabView.findViewById(R.id.tabTextView);
        tabTextView.setText(title);
        tabTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.laundryregular));

        return tabView;
    }

    // Firestore에서 제품 데이터를 가져와서 RecyclerView에 표시합니다.
    private void fetchProductsFromFirestore() {
        com.google.firebase.firestore.Query query = db.collection("new_data")
                .whereGreaterThanOrEqualTo("average_rate", 4.7)
                .whereEqualTo("result", selectedResult)
                .whereIn("moisturizing", Arrays.asList(0, 1))
                .limit(10);

        if (!selectedCategory.equals("전체")) {
            query = query.whereEqualTo("category_list2", selectedCategory);
        }

        // Firestore 쿼리 실행
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            productList.clear();
                            HashSet<String> productNames = new HashSet<>();
                            int count = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String imageUrl = document.getString("img");
                                String productName = document.getString("name");
                                String optionName = document.getString("option_name");
                                String number = document.getString("number"); // number 필드 가져오기
                                List<Object> optionRgbList = (List<Object>) document.get("option_rgb"); // option_rgb 필드를 배열로 가져옴

                                // optionRgbList를 List<Integer>로 변환
                                List<Integer> optionRgbIntList = null;
                                if (optionRgbList != null) {
                                    optionRgbIntList = new ArrayList<>();
                                    for (Object rgbValue : optionRgbList) {
                                        if (rgbValue instanceof Long) {
                                            optionRgbIntList.add(((Long) rgbValue).intValue());
                                        } else if (rgbValue instanceof Double) {
                                            optionRgbIntList.add(((Double) rgbValue).intValue());
                                        }
                                    }
                                }

                                // optionRgbIntList를 문자열로 변환
                                String optionRgb = null;
                                if (optionRgbIntList != null) {
                                    optionRgb = optionRgbIntList.toString(); // 배열을 문자열로 변환
                                }

                                Log.d(TAG, "Product Name: " + productName + ", Option Name: " + optionName + ", Option RGB: " + optionRgb);

                                if (imageUrl != null && productName != null && optionName != null && number != null && !productNames.contains(productName)) {
                                    Product product = new Product(productName, optionName, imageUrl, number, optionRgb);
                                    productList.add(product);
                                    productNames.add(productName);
                                    count++;
                                }
                                if (count >= 10)
                                    break;
                            }
                            adapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(0);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            Toast.makeText(getContext(), "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }


                });
    }

}
