package kr.ac.duksung.mycol;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        View view = inflater.inflate(R.layout.fragment_total_recommend, container, false);

        tabLayout1 = view.findViewById(R.id.tabLayout1);
        tabLayout2 = view.findViewById(R.id.tabLayout2);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productList = new ArrayList<>();
        adapter = new ProductAdapter(productList, getContext());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance(); // FirebaseFirestore 객체 초기화

        // TabLayout1 설정
        tabLayout1.addTab(tabLayout1.newTab().setText("Spring warm light").setContentDescription("Spring warm light"));
        tabLayout1.addTab(tabLayout1.newTab().setText("Spring warm bright").setContentDescription("Spring warm bright"));
        tabLayout1.addTab(tabLayout1.newTab().setText("Summer cool light").setContentDescription("Summer cool light"));
        tabLayout1.addTab(tabLayout1.newTab().setText("Summer cool mute").setContentDescription("Summer cool mute"));
        tabLayout1.addTab(tabLayout1.newTab().setText("Autumn warm mute").setContentDescription("Autumn warm mute"));
        tabLayout1.addTab(tabLayout1.newTab().setText("Autumn warm deep").setContentDescription("Autumn warm deep"));
        tabLayout1.addTab(tabLayout1.newTab().setText("Winter cool bright").setContentDescription("Winter cool bright"));
        tabLayout1.addTab(tabLayout1.newTab().setText("Winter cool deep").setContentDescription("Winter cool deep"));

        // TabLayout2 설정
        tabLayout2.addTab(tabLayout2.newTab().setText("립").setContentDescription("립"));
        tabLayout2.addTab(tabLayout2.newTab().setText("아이").setContentDescription("아이"));
        tabLayout2.addTab(tabLayout2.newTab().setText("베이스").setContentDescription("베이스"));

        // TabLayout1 탭 선택 리스너 설정
        tabLayout1.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedResult = tab.getText().toString();
                fetchProductsFromFirestore(); // 탭 선택 시 데이터 가져오기 호출
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // TabLayout2 탭 선택 리스너 설정
        tabLayout2.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String categoryText = tab.getText().toString();
                if (categoryText.equals("전체")) {
                    selectedCategory = "전체"; // 전체 선택 시 전체로 설정
                } else {
                    selectedCategory = categoryText + "메이크업"; // 카테고리 설정
                }
                fetchProductsFromFirestore(); // 탭 선택 시 데이터 가져오기 호출
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 기본 탭 선택 및 데이터 로드
        tabLayout1.getTabAt(0).select(); // "Spring warm light" 탭 선택
        tabLayout2.getTabAt(0).select(); // "립" 탭 선택
        fetchProductsFromFirestore(); // 기본값으로 데이터 로드

        return view;
    }

    private void fetchProductsFromFirestore() {
        // Firestore 쿼리 빌더 초기화
        com.google.firebase.firestore.Query query = db.collection("new_data")
                .whereGreaterThanOrEqualTo("average_rate", 4.7)
                .whereEqualTo("result", selectedResult)
                .whereIn("moisturizing", Arrays.asList(0, 1))
                .limit(10);

        // 카테고리가 "전체"가 아닌 경우에만 category_list2 필터 추가
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
                                Log.d(TAG, "Product Name: " + productName + ", Option Name: " + optionName);

                                // 중복 체크
                                if (imageUrl != null && productName != null && optionName != null && !productNames.contains(productName)) {
                                    Product product = new Product(productName, optionName, imageUrl);
                                    productList.add(product);
                                    productNames.add(productName); // 중복을 막기 위해 상품명 추가
                                    count++; // 상품 개수 증가
                                }
                                if (count >= 10) // 이미 10개의 상품을 가져왔으면 더 이상 반복할 필요 없음
                                    break;
                            }
                            adapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(0); // 스크롤을 상단으로 이동
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            Toast.makeText(getContext(), "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
