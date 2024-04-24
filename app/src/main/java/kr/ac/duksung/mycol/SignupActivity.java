package kr.ac.duksung.mycol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

public class SignupActivity extends AppCompatActivity {

    private EditText signup_email, signup_pw, signup_confirmpw, signup_nickname;

    //파이어베이스
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // FirebaseAuth 초기화
        firebaseAuth = FirebaseAuth.getInstance();

        signup_email = findViewById(R.id.editTextSignupEmail);
        signup_pw = findViewById(R.id.editTextSignupPw);
        signup_confirmpw = findViewById(R.id.editTextConfirmPw);
        signup_nickname = findViewById(R.id.editTextNickname);
    }

    public void onSignupClick(View view) {
        final String email = signup_email.getText().toString().trim();
        String pw = signup_pw.getText().toString().trim();
        String confirmPw = signup_confirmpw.getText().toString().trim();
        String nickname = signup_nickname.getText().toString().trim();

        if (email.length() > 0 && pw.length() >= 6 && confirmPw.length() >= 6 && nickname.length() > 0) {
            if (pw.equals(confirmPw)) {
                // 비밀번호와 비밀번호 확인에 입력된 값이 같은지 확인
                firebaseAuth.createUserWithEmailAndPassword(email, pw)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // 회원가입 성공시
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "회원가입 성공",
                                            Toast.LENGTH_SHORT).show();
                                    // 회원가입이 성공하면 LoginActivity로 이동
                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish(); // 현재 액티비티 종료
                                } else {
                                    Toast.makeText(SignupActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                Toast.makeText(SignupActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 비밀번호 길이가 6자 이상이 아닐 경우 메시지 표시
            Toast.makeText(SignupActivity.this, "모든 필드를 작성해주세요. 비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
        }
    }
}