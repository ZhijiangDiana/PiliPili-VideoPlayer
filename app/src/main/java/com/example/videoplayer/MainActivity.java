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
    Toolbar toolbar;
    ImageView userIcon;
    public static MainActivity mainActivity;
    public static Connection conn = null;
    public static Statement stmt = null;
    public static ResultSet rs = null;
    public MainActivity() {
        mainActivity = MainActivity.this;
    }

    public static RecyclerView mRecyclerView;
    public static VideoPreviewAdapter mPreviewAdapter;
    private List<PreviewBean> mPreviewList = new ArrayList<>(114);
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

        // 初始化数据库连接
        try {
            conn = DriverManager.getConnection("jdbc:mysql://1.15.179.230:3306/VideoPlayer?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", "114514");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM VideoDB");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // 初始化RecyclerView
        mRecyclerView = findViewById(R.id.recyclerView);
        // 添加数据，一次添加三个
        try {
            for(int i=0;i<3;i++) {
                if (!rs.next())
                    return;
                PreviewBean pBean = getThreeBeans();
                mPreviewList.add(pBean);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // 初始化适配器
        mPreviewAdapter = new VideoPreviewAdapter(mPreviewList);
        mRecyclerView.addOnScrollListener(mPreviewAdapter.scrollToLastListener);
        // 设置适配器
        mRecyclerView.setAdapter(mPreviewAdapter);

        //todo 读取本地user信息，若登录已过期则将user设为null
        if (currentUser != null) {
            //todo 若已登录，则标题显示欢迎界面，点击人头按钮将弹出PopupMenu而不是进入登录界面
            toolbar.setTitle("欢迎" + currentUser.userNickName + "! ");
        }

    }

    public static PreviewBean getThreeBeans() throws SQLException {
        int id = rs.getInt("id");
        String videoName = rs.getString("videoName");
        String videoTag = rs.getString("videoTag");
        String videoDescription = rs.getString("videoDescription").replace("\\n", "\n");
        String uploadDate = rs.getString("uploadDate");
        byte[] videoThumb = rs.getBytes("videoThumb");
        int playCount = rs.getInt("playCount");
        int like = rs.getInt("like");
        int dispatchCount = rs.getInt("dispatchCount");

        PreviewBean pBean = new PreviewBean(id, videoName, videoTag, videoDescription, videoThumb,
                uploadDate, playCount, like, dispatchCount);
        return pBean;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            conn.close();
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void login(View view) {
        if (currentUser == null)
            startActivity(new Intent(mainActivity, LoginActivity.class));
    }
}