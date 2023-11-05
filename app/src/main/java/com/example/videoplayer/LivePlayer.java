package com.example.videoplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import layout.FlowLayout;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.DisplayUtils;
import utils.ViewUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.videoplayer.Variable.*;

public class LivePlayer extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LivePlayerActivity";
    private MovieView mv_content;
    private MarqueeTextView tv_open;
    private RelativeLayout rl_top;
    private TextView vc_play;
    private Handler mHandler = new Handler();
    private ScrollView videoInfo;
    private RelativeLayout player;
    public static LivePlayer livePlayerActivity;
    private ImageView fullscreen;
    public LivePlayer() {
        livePlayerActivity = LivePlayer.this;
    }
    String videoPath;
    boolean isFullScreen;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        setContentView(R.layout.activity_live_player);
        mv_content = findViewById(R.id.mv_content);
        tv_open = findViewById(R.id.tv_open);
        rl_top = findViewById(R.id.rl_top);
        vc_play = findViewById(R.id.vc_play);
        mv_content.prepare(rl_top, vc_play);
        tv_open.setOnClickListener(this);
        videoInfo = findViewById(R.id.videoInfo);
        player = findViewById(R.id.player);
        fullscreen = findViewById(R.id.fullscreen);


        // 接收MainActivity传送的Bundle
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        int videoID = bundle.getInt("id", 0);
        String videoName = bundle.getString("videoName", "");
        String videoDescription = bundle.getString("videoDescription", "");
        videoPath = "http://121.41.1.13:8082/hls/CaiLiu.m3u8";


        // setText方法必须传入String类型，否则会抛出找不到控件的异常
        ((TextView)findViewById(R.id.tv_open)).setText(videoName);
        ((TextView)findViewById(R.id.videoId)).setText(String.valueOf(videoID));
        ((TextView)findViewById(R.id.videoName)).setText(videoName);
        ((TextView)findViewById(R.id.videoDes)).setText(videoDescription);

        play(videoPath);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void playVideo(String video_path) {
        mv_content.setVideoPath(video_path);
        mv_content.requestFocus();
        mv_content.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mv_content.begin(mp);

                mHandler.removeCallbacks(mHide);
                mHandler.postDelayed(mHide, MovieView.HIDE_TIME);
            }
        });
        mv_content.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mv_content.end(mp);
            }
        });
        mv_content.setOnTouchListener(mv_content);
        mv_content.setOnKeyListener(mv_content);
    }

    private Runnable mHide = new Runnable() {
        @Override
        public void run() {
            mv_content.hide();
        }
    };

    private void play(String file_path) {
        Log.d(TAG, "file_path=" + file_path);
        playVideo(file_path);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_open) {
//            play("http://1.15.179.230:8081/TruE-%E9%BB%84%E9%BE%84_480p.mp4");
        }
    }

    public void fullScreen(View view) {
        if (isFullScreen) { // 退出全屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            player.setLayoutParams(portraitParam);
            videoInfo.setVisibility(View.VISIBLE);
            fullscreen.setImageResource(R.drawable.fullscreen_24);
            isFullScreen = false;
        } else { // 进入全屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            player.setLayoutParams(fullScreenParam);
            videoInfo.setVisibility(View.GONE);
            fullscreen.setImageResource(R.drawable.baseline_fullscreen_exit_24);
            isFullScreen = true;
        }
    }

    RelativeLayout.LayoutParams fullScreenParam = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    RelativeLayout.LayoutParams portraitParam = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewUtils.dp2px(MainActivity.mainActivity, 250));
    @Override
    public void onConfigurationChanged(@NonNull @NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            tv_open.getLayoutParams().width = DisplayUtils.dip2px(livePlayerActivity, 200);
            videoInfo.setVisibility(View.VISIBLE);
            player.setLayoutParams(portraitParam);
            fullscreen.setImageResource(R.drawable.fullscreen_24);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            tv_open.getLayoutParams().width = DisplayUtils.dip2px(livePlayerActivity, 800);
            player.setLayoutParams(fullScreenParam);
            videoInfo.setVisibility(View.GONE);
            fullscreen.setImageResource(R.drawable.baseline_fullscreen_exit_24);
        }
    }
}