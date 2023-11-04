package com.example.videoplayer;

import android.content.Intent;
import android.os.StrictMode;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static MainActivity mainActivity;
    public MainActivity() {
        mainActivity = MainActivity.this;
    }
    Toolbar toolbar;
    ImageView userIcon;
    UserBean currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // 初始化阶段
        toolbar = findViewById(R.id.main_toolbar);
        userIcon = findViewById(R.id.main_userIcon);

        //todo 读取本地user信息，若登录已过期则将user设为null
        if (currentUser != null) {
            //todo 若已登录，则标题显示欢迎界面，点击人头按钮将弹出PopupMenu而不是进入登录界面
            toolbar.setTitle("欢迎" + currentUser.userNickName + "! ");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void login(View view) {
        if (currentUser == null)
            startActivity(new Intent(mainActivity, LoginActivity.class));
    }
}