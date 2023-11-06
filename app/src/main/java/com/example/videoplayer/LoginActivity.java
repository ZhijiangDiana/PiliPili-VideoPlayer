package com.example.videoplayer;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "LoginActivity";
    public static LoginActivity loginActivity;
    public LoginActivity() {
        loginActivity = LoginActivity.this;
    }
    EditText userName;
    EditText password;
    TextView forgetPassword;
    CheckBox agree;
    AlertDialog.Builder waitingBuilder;
    AlertDialog waiting;
    AlertDialog.Builder agreementBuilder;
    AlertDialog.Builder blankLoginBuilder;
    String message;

    OkHttpClient okpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        okpClient = new OkHttpClient().newBuilder().build();
        userName = findViewById(R.id.login_userName);
        password = findViewById(R.id.login_password);
        forgetPassword = findViewById(R.id.login_forgetPassword);
        agree = findViewById(R.id.login_agree);


        waitingBuilder = new AlertDialog.Builder(loginActivity)
                .setView(R.layout.waiting_for_server)
                .setCancelable(false);
        agreementBuilder = new AlertDialog.Builder(loginActivity)
                .setMessage("114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514114514")
                .setTitle("用户协议");
        blankLoginBuilder = new AlertDialog.Builder(loginActivity);

    }

    public void Login(View view) {
        if (CheckReadyLogin() != VERIFIED) {
            return;
        }

        FormBody body = new FormBody.Builder()
                .add("userName", userName.getText().toString().trim())
                .add("password", password.getText().toString().trim())
                .build();

        Call call = okpClient.newCall(new Request.Builder()
                .url("http://" +Variable.mainServSocket+ "/" +Variable.war+ "/" + "_4_VideoPlayerLogin")
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
                    loginActivity.runOnUiThread(noInternet);
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
                    loginActivity.runOnUiThread(loginFailed);
                else
                    loginActivity.runOnUiThread(loginSucceeded);

            }
        }).start();

    }

    private final int VERIFIED = 0;
    private final int USER_BLANK = -1;
    private final int PASSWORD_BLANK = -2;
    private final int AGREE_BLANK = -3;

    private int CheckReadyLogin() {
        if (userName.getText().toString().trim().isEmpty()) {
            blankLoginBuilder.setMessage("用户名不能为空")
                    .show();
            return USER_BLANK;
        }
        if (password.getText().toString().trim().isEmpty()) {
            blankLoginBuilder.setMessage("密码不能为空")
                    .show();
            return PASSWORD_BLANK;
        }
        if (!agree.isChecked()) {
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
            Toast t = new Toast(loginActivity);
            t.setText("网络连接失败");
            t.show();
        }
    };

    Runnable loginFailed = new Runnable() {
        @Override
        public void run() {
            waiting.cancel();
            Toast t = new Toast(loginActivity);
            t.setText("用户名或密码错误");
            t.show();
        }
    };

    Runnable loginSucceeded = new Runnable() {
        @Override
        public void run() {
            waiting.cancel();
            Toast t = new Toast(loginActivity);
            t.setText("登录成功，正在跳转。。。");
            t.show();

            Gson json = new Gson();
            Log.e(TAG, message);
            Variable.currentUser = json.fromJson(message.trim(), UserBean.class);

            finish();
        }
    };

    public void PopupAgreement(View view) {
        agreementBuilder.show();
    }
}