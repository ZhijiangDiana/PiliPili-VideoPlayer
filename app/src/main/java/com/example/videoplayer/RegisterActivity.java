package com.example.videoplayer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "Register";
    public static RegisterActivity registerActivity;
    public RegisterActivity() {
        registerActivity = RegisterActivity.this;
    }
    private EditText userName;
    private EditText email;
    private EditText password1;
    private EditText password2;
    private EditText nickName;
    private CheckBox agreement;

    AlertDialog.Builder waitingBuilder;
    AlertDialog waiting;
    AlertDialog.Builder agreementBuilder;
    AlertDialog.Builder blankLoginBuilder;
    OkHttpClient okpClient;
    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        okpClient = new OkHttpClient.Builder().build();
        userName = findViewById(R.id.register_userName);
        password1 = findViewById(R.id.register_password1);
        password2 = findViewById(R.id.register_password2);
        email = findViewById(R.id.register_email);
        nickName = findViewById(R.id.register_nickname);
        agreement = findViewById(R.id.register_userAgreementCheckBox);

        waitingBuilder = new AlertDialog.Builder(registerActivity)
                .setView(R.layout.waiting_for_server)
                .setCancelable(false);
        agreementBuilder = new AlertDialog.Builder(registerActivity)
                .setMessage("114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514")
                .setTitle("用户协议");
        blankLoginBuilder = new AlertDialog.Builder(registerActivity);
    }

    private final int VERIFIED = 0;
    private final int USER_BLANK = -1;
    private final int PASSWORD_BLANK = -2;
    private final int AGREE_BLANK = -3;
    private final int NICKNAME_BLANK = -4;
    private final int EMAIL_BLANK = -5;
    private final int PASSWORD_NOT_DUPLICATE = -5;


    private int CheckReadyLogin() {
        if (userName.getText().toString().trim().isEmpty()) {
            blankLoginBuilder.setMessage("用户名不能为空")
                    .show();
            return USER_BLANK;
        }
        if (password1.getText().toString().trim().isEmpty() ||
                password2.getText().toString().trim().isEmpty()) {
            blankLoginBuilder.setMessage("密码不能为空")
                    .show();
            return PASSWORD_BLANK;
        }
        if (nickName.getText().toString().trim().isEmpty()) {
            blankLoginBuilder.setMessage("昵称不能为空")
                    .show();
            return NICKNAME_BLANK;
        }
        if (email.getText().toString().trim().isEmpty()) {
            blankLoginBuilder.setMessage("邮箱不能为空")
                    .show();
            return EMAIL_BLANK;
        }
        if (!password1.getText().toString().trim().equals(password2.getText().toString().trim())) {
            blankLoginBuilder.setMessage("两次输入的密码不一致")
                    .show();
            return PASSWORD_NOT_DUPLICATE;
        }
        if (!agreement.isChecked()) {
            blankLoginBuilder.setMessage("请同意用户协议")
                    .show();
            return AGREE_BLANK;
        }
        return VERIFIED;
    }

    Runnable noInternet = new Runnable() {
        @Override
        public void run() {
            waiting.cancel();
            Toast t = new Toast(registerActivity);
            t.setText("网络连接失败");
            t.show();
        }
    };

    Runnable registerFailed = new Runnable() {
        @Override
        public void run() {
            waiting.cancel();
            Toast t = new Toast(registerActivity);
            t.setText("用户名重复，请重新输入");
            t.show();
        }
    };

    Runnable loginSucceeded = new Runnable() {
        @Override
        public void run() {
            waiting.cancel();
            Toast t = new Toast(registerActivity);
            t.setText("注册成功，请登录。");
            t.show();

            finish();
        }
    };

    public void PopupAgreement(View view) {
        agreementBuilder.show();
    }

    public void Register(View view) {
        if (CheckReadyLogin() != VERIFIED) {
            return;
        }

        FormBody body = new FormBody.Builder()
                .add("userName", userName.getText().toString().trim())
                .add("password", password1.getText().toString().trim())
                .add("userEmail", email.getText().toString().trim())
                .add("userNickName", nickName.getText().toString().trim())
                .build();

        Call call = okpClient.newCall(new Request.Builder()
                .url("http://" +Variable.mainServSocket+ "/" +Variable.war+ "/" + "_4_VideoPlayerRegister")
                .post(body)
                .build());
        waiting = waitingBuilder.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response resp = null;
                try {
                    resp = call.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    registerActivity.runOnUiThread(noInternet);
                    return;
                }
                try {
                    message = resp.body().string().trim();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    resp.close();
                }
                if (message.equals("false"))
                    registerActivity.runOnUiThread(registerFailed);
                else
                    registerActivity.runOnUiThread(loginSucceeded);

            }
        }).start();
    }
}
