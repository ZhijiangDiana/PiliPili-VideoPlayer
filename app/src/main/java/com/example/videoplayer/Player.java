package com.example.videoplayer;

import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import org.jetbrains.annotations.NotNull;

public class Player extends AppCompatActivity implements
        View.OnClickListener, VideoController.OnSeekChangeListener {

    private static final String TAG = "MoviePlayerActivity";
    private MovieView mv_content;
    private TextView tv_open;
    private RelativeLayout rl_top;
    private VideoController vc_play;
    private Handler mHandler = new Handler();

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

        // 当有保存的进度时，继续播放
        if(savedInstanceState != null) {
            vc_play.enableController();
            playVideo(savedInstanceState.getString("source"));
            mv_content.seekTo(savedInstanceState.getInt("progress"));
        }

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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_open) {
            String file_path = "http://1.15.179.230:8081/TruE-%E9%BB%84%E9%BE%84_480p.mp4";
            Log.d(TAG, "file_path=" + file_path);
            vc_play.enableController();
            playVideo(file_path);
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
}