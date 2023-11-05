package com.example.videoplayer;

import android.content.Intent;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import okhttp3.*;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
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
                .setView(R.layout.agreement_alertdialog);
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
                .url("http://" +Variable.mainServSocket+ "/" +Variable.war+ "/" + "_____")
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
                message = resp.body().toString().trim();
                if (message.contains("state=true"))
                    loginActivity.runOnUiThread(loginSucceeded);
                else
                    loginActivity.runOnUiThread(loginFailed);
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

            String[] entity = message.trim().split("&&");
            //todo 解析返回字符串，一共1+7项
            new UserBean(entity[1].split("=")[1],
                    entity[2].split("=")[1],
                    entity[3].split("=")[1],
                    entity[4].split("=")[1],
                    entity[5].split("=")[1],
                    Integer.parseInt(entity[6].split("=")[1]),
                    entity[7].split("=")[1]);
            Variable.currentUser = new UserBean("", "", "", "", "", 0, "");
            startActivity(new Intent(loginActivity, MainActivity.class));
        }
    };

    public void PopupAgreement(View view) {
        agreementBuilder.show();
    }
}