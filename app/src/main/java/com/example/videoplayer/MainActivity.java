package com.example.videoplayer;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.StrictMode;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.sql.*;

public class MainActivity extends AppCompatActivity {
    public static MainActivity mainActivity;
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    public MainActivity() {
        mainActivity = MainActivity.this;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }
    public void playVideo(View view) {
        //跳转到Player完成播放视频
        Intent openPlayer = new Intent(this, Player.class);
        Bundle bundle = new Bundle();
        try {
            //todo 仅供测试使用，正常情况是从listview里获取
            conn = DriverManager.getConnection("jdbc:mysql://1.15.179.230:3306/VideoPlayer?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", "114514");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM VideoDB WHERE id=3");
            rs.next();

            int id = rs.getInt("id");
            bundle.putInt("id", id);

            String videoName = rs.getString("videoName");
            bundle.putString("videoName", videoName);

            String videoTag = rs.getString("videoTag");
            bundle.putString("videoTag", videoTag);

            String videoDescription = rs.getString("videoDescription").replace("\\n", "\n");
            bundle.putString("videoDescription", videoDescription);

            String uploadDate = rs.getString("uploadDate");
            bundle.putString("uploadDate", uploadDate);

            byte[] videoThumb = rs.getBytes("videoThumb");
            bundle.putByteArray("thumbByte", videoThumb);

            int playCount = rs.getInt("playCount");
            bundle.putInt("playCount", playCount);

            int like = rs.getInt("like");
            bundle.putInt("like", like);

            int dispatchCount = rs.getInt("dispatchCount");
            bundle.putInt("dispatchCount", dispatchCount);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                conn.close();
                stmt.close();
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        openPlayer.putExtras(bundle);
        startActivity(openPlayer);
    }

    public void playVideoInVideoView(View view) {
        startActivity(new Intent(this, VideoViewTest.class));
    }
}