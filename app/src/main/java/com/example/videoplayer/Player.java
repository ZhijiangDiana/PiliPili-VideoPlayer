package com.example.videoplayer;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import org.jetbrains.annotations.NotNull;
import utils.DisplayUtils;
import utils.ViewUtils;

import java.util.Timer;
import java.util.TimerTask;

public class Player extends AppCompatActivity implements
        View.OnClickListener, VideoController.OnSeekChangeListener {

    private static final String TAG = "MoviePlayerActivity";
    private MovieView mv_content;
    private TextView tv_open;
    private RelativeLayout rl_top;
    private VideoController vc_play;
    private Handler mHandler = new Handler();
    private ScrollView videoInfo;
    private RelativeLayout player;
    public static Player playerActivity;
    private boolean isFullScreen = false;
    private ImageView fullscreen;
    public Player() {
        playerActivity = Player.this;
    }
    private Bitmap thumb;
    private View qualityPopupView;
    private PopupWindow qualityPopupWindow;
    private ImageView quality;
    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mv_content = findViewById(R.id.mv_content);
        vc_play = findViewById(R.id.vc_play);
        tv_open = findViewById(R.id.tv_open);
        rl_top = findViewById(R.id.rl_top);
        mv_content.prepare(rl_top, vc_play);
        tv_open.setOnClickListener(this);
        vc_play.setOnSeekChangeListener(this);
        videoInfo = findViewById(R.id.videoInfo);
        player = findViewById(R.id.player);
        fullscreen = findViewById(R.id.fullscreen);
        quality = findViewById(R.id.quality);
        qualityPopupView = getLayoutInflater().inflate(R.layout.quality_popup, null);
        qualityPopupWindow = new PopupWindow(qualityPopupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);


        // 当有保存的进度时，继续播放
        if(savedInstanceState != null) {
            vc_play.enableController();
            playVideo(savedInstanceState.getString("source"));
            mv_content.seekTo(savedInstanceState.getInt("progress"));
        }

        // 接收MainActivity传送的Bundle
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        int id = bundle.getInt("id");
        String videoName = bundle.getString("videoName");
        String videoTag = bundle.getString("videoTag");
        String videoDescription = bundle.getString("videoDescription");
        byte[] videoThumb = bundle.getByteArray("thumbByte");
        String uploadDate = bundle.getString("uploadDate");
        int playCount = bundle.getInt("playCount");
        int like = bundle.getInt("like");
        int dispatchCount = bundle.getInt("dispatchCount");
        videoPath = "http://1.15.179.230:8081/vod/" + videoName + "_1080p.mp4" + "/index.m3u8";

        // setText方法必须传入String类型，否则会抛出找不到控件的异常
        ((TextView)findViewById(R.id.tv_open)).setText(videoName);
        ((TextView)findViewById(R.id.videoId)).setText(String.valueOf(id));
        ((TextView)findViewById(R.id.videoName)).setText(videoName);
//        ((TextView)findViewById(R.id.videoTag)).setText(videoTag);
        ((TextView)findViewById(R.id.playCnt)).setText(String.valueOf(playCount));
        ((TextView)findViewById(R.id.videoDes)).setText(videoDescription);
        ((TextView)findViewById(R.id.uploadDate)).setText(uploadDate);
        play(videoPath);

        // 根据视频缩略图制定全屏规则
        thumb = BitmapFactory.decodeByteArray(videoThumb, 0, videoThumb.length, new BitmapFactory.Options());
        if(thumb.getWidth() <= thumb.getHeight()) { // 竖屏视频锁定竖屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else // 横屏视频不限制
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
                vc_play.setVideoView(mv_content);

                mHandler.removeCallbacks(mHide);
                mHandler.postDelayed(mHide, MovieView.HIDE_TIME);
                mHandler.post(mRefresh);
            }
        });
        mv_content.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mv_content.end(mp);
                vc_play.setCurrentTime(0, 0);
            }
        });
        mv_content.setOnTouchListener(mv_content);
        mv_content.setOnKeyListener(mv_content);
    }

    private Runnable mHide = new Runnable() {
        @Override
        public void run() {
//            mv_content.showOrHide();
        }
    };

    private Runnable mRefresh = new Runnable() {
        @Override
        public void run() {
            if (mv_content.isPlaying()) {
                vc_play.setCurrentTime(mv_content.getCurrentPosition(),
                        mv_content.getBufferPercentage());
            }
            mHandler.postDelayed(this, 500);
        }
    };

    private void play(String file_path) {
        Log.d(TAG, "file_path=" + file_path);
        vc_play.enableController();
        playVideo(file_path);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_open) {
//            play("http://1.15.179.230:8081/TruE-%E9%BB%84%E9%BE%84_480p.mp4");
        }
    }

    @Override
    public void onStartSeek() {
        mHandler.removeCallbacks(mHide);
    }

    @Override
    public void onStopSeek() {
        mHandler.postDelayed(mHide, MovieView.HIDE_TIME);
    }

    // 重新加载时保留进度
    @Override
    protected void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        outState.putInt("progress", mv_content.getCurrentPosition());
        outState.putString("source", mv_content.getVideoPath());
        super.onSaveInstanceState(outState);
    }

    RelativeLayout.LayoutParams fullScreenParam = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    RelativeLayout.LayoutParams portraitParam = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewUtils.dp2px(MainActivity.mainActivity, 250));
    @Override
    public void onConfigurationChanged(@NonNull @NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            tv_open.setWidth(DisplayUtils.dip2px(playerActivity, 200));
            if (thumb.getWidth() > thumb.getHeight()) {
                videoInfo.setVisibility(View.VISIBLE);
                player.setLayoutParams(portraitParam);
                fullscreen.setImageResource(R.drawable.fullscreen_24);
                isFullScreen = false;
            }
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(thumb.getWidth() > thumb.getHeight()) {
                tv_open.setWidth(DisplayUtils.dip2px(playerActivity, 800));
                player.setLayoutParams(fullScreenParam);
                videoInfo.setVisibility(View.GONE);
                fullscreen.setImageResource(R.drawable.baseline_fullscreen_exit_24);
                isFullScreen = true;
            }
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
            if (thumb.getWidth() > thumb.getHeight()) {
                TimerTask unlockSensor = new TimerTask() {
                    @Override
                    public void run() {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                };
                new Timer().schedule(unlockSensor, 5000);
            }
        } else { // 进入全屏
            if (thumb.getWidth() > thumb.getHeight()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                TimerTask unlockSensor = new TimerTask() {
                    @Override
                    public void run() {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                };
                new Timer().schedule(unlockSensor, 5000);
            }
            else
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            player.setLayoutParams(fullScreenParam);
            videoInfo.setVisibility(View.GONE);
            fullscreen.setImageResource(R.drawable.baseline_fullscreen_exit_24);
            isFullScreen = true;
        }
    }

    public void quality(View view) {
        qualityPopupWindow.showAsDropDown(quality);
    }

    public void switchLowQuality(View view) {
        if (videoPath == null)
            return;
        if (videoPath.contains("_480p.mp4"))
            return;
        videoPath = videoPath.replaceAll("_1080p.mp4", "_480p.mp4");
        play(videoPath);
        Toast.makeText(playerActivity, videoPath, Toast.LENGTH_SHORT);
    }

    public void switchHighQuality(View view) {
        if (videoPath == null)
            return;
        if (videoPath.contains("_1080p.mp4"))
            return;
        videoPath = videoPath.replaceAll("_480p.mp4", "_1080p.mp4");
        play(videoPath);
        Toast.makeText(playerActivity, videoPath, Toast.LENGTH_SHORT);
    }
}