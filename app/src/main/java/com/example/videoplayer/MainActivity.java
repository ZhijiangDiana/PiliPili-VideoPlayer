package com.example.videoplayer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static MainActivity mainActivity;
    public MainActivity() {
        mainActivity = MainActivity.this;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void playVideo(View view) {
        //跳转到VideoActivity完成播放视频
        startActivity(new Intent(this, Player.class));
    }

    public void playVideoInVideoView(View view) {
        startActivity(new Intent(this, VideoViewTest.class));
    }
}