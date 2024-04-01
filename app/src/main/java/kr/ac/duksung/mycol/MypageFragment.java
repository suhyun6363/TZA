package kr.ac.duksung.mycol;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class MypageFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mypage, container, false);

        // 로그아웃 버튼 클릭 이벤트 처리
        Button logoutButton = rootView.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FirebaseAuth를 사용하여 로그아웃
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getActivity(), "로그아웃", Toast.LENGTH_SHORT).show();

                // 로그인 화면으로 이동
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);

                // 현재 액티비티 종료
                getActivity().finish();
            }
        });

        return rootView;
    }
}
