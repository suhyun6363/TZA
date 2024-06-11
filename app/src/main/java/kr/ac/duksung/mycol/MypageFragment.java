package kr.ac.duksung.mycol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class MypageFragment extends Fragment implements View.OnClickListener {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextView customerServiceText;
    private TextView oneOnOneInquiryText;
    private TextView companyName;
    private TextView instagramId;

    // 추가된 TextView
    private TextView customerServiceInfo;
    private TextView oneOnOneInquiryInfo;

    // 추가된 상태 변수
    private boolean isCustomerServiceInfoVisible = false;
    private boolean isOneOnOneInquiryInfoVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mypage, container, false);

        // SharedPreferences 초기화
        sharedPreferences = getActivity().getSharedPreferences("loginPrefs", getActivity().MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // 고객센터/공지사항 텍스트 뷰 클릭 이벤트 설정
        customerServiceText = rootView.findViewById(R.id.customerServiceText);
        customerServiceText.setOnClickListener(this);

        // 1:1문의 텍스트 뷰 클릭 이벤트 설정
        oneOnOneInquiryText = rootView.findViewById(R.id.oneOnOneInquiryText);
        oneOnOneInquiryText.setOnClickListener(this);

        // 추가된 TextView 초기화
        customerServiceInfo = rootView.findViewById(R.id.customerServiceInfo);
        oneOnOneInquiryInfo = rootView.findViewById(R.id.oneOnOneInquiryInfo);

        // 로그아웃 버튼 클릭 이벤트 처리
        Button logoutButton = rootView.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FirebaseAuth를 사용하여 로그아웃
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getActivity(), "로그아웃", Toast.LENGTH_SHORT).show();

                // 자동 로그인 정보 삭제
                editor.clear();
                editor.apply();

                // 로그인 화면으로 이동
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);

                // 현재 액티비티 종료
                getActivity().finish();
            }
        });

        // 인스타그램 로고 클릭 이벤트 처리
        ImageView instagramLogo = rootView.findViewById(R.id.instagramLogo);
        instagramLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 인스타그램 페이지로 이동
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/lanosrep_official"));
                startActivity(intent);
            }
        });


        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.customerServiceText:
                // 고객센터/공지사항 클릭 시 처리할 코드
                if (isCustomerServiceInfoVisible) {
                    customerServiceInfo.setVisibility(View.GONE); // 숨기기
                    isCustomerServiceInfoVisible = false;
                } else {
                    customerServiceInfo.setVisibility(View.VISIBLE); // 보이기
                    customerServiceInfo.setText("자주 묻는 질문과\n 서비스 공지를 확인하실 수 있습니다.");
                    isCustomerServiceInfoVisible = true;
                }
                break;
            case R.id.oneOnOneInquiryText:
                // 1:1문의 클릭 시 처리할 코드
                if (isOneOnOneInquiryInfoVisible) {
                    oneOnOneInquiryInfo.setVisibility(View.GONE); // 숨기기
                    isOneOnOneInquiryInfoVisible = false;
                } else {
                    oneOnOneInquiryInfo.setVisibility(View.VISIBLE); // 보이기
                    oneOnOneInquiryInfo.setText("lanosrep.color@gmail.com 으로 문의 주시면\n 빠른 안내 도와드리겠습니다.");
                    isOneOnOneInquiryInfoVisible = true;
                }
                break;
        }
    }
}
