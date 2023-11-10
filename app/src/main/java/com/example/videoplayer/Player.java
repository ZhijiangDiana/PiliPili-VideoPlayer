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
import com.danikula.videocache.HttpProxyCacheServer;
import layout.FlowLayout;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.CacheUtils;
import utils.DisplayUtils;
import utils.ViewUtils;

import java.io.IOException;
import java.util.*;

import static com.example.videoplayer.Variable.*;

public class Player extends AppCompatActivity implements
        View.OnClickListener, VideoController.OnSeekChangeListener {

    private static final String TAG = "MoviePlayerActivity";
    private MovieView mv_content;
    private MarqueeTextView tv_open;
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
    private TextView likeCnt;
    private Button likeButton;
    private TextView shareCnt;
    OkHttpClient okp;
    int videoID;
    HttpProxyCacheServer proxy;

    @SuppressLint("ResourceAsColor")
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
        likeCnt = findViewById(R.id.likeCnt);
        shareCnt = findViewById(R.id.shareCnt);
        likeButton = findViewById(R.id.player_likeButton);


        qualityPopupView = getLayoutInflater().inflate(R.layout.quality_popup, null);
        qualityPopupWindow = new PopupWindow(qualityPopupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        okp = new OkHttpClient().newBuilder().build();
        proxy = CacheUtils.getProxy(playerActivity);


        // 当有保存的进度时，继续播放
        if(savedInstanceState != null) {
            vc_play.enableController();
            playVideo(savedInstanceState.getString("source"));
            mv_content.seekTo(savedInstanceState.getInt("progress"));
        }

        // 接收MainActivity传送的Bundle
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        videoID = bundle.getInt("id");
        String videoName = bundle.getString("videoName");
        String videoTag = bundle.getString("videoTag");
        String videoDescription = bundle.getString("videoDescription");
        byte[] videoThumb = bundle.getByteArray("thumbByte");
        String uploadDate = bundle.getString("uploadDate");
        int playCount = bundle.getInt("playCount") + 1;
        int like = bundle.getInt("like");
        int dispatchCount = bundle.getInt("dispatchCount");
//        videoPath = "http://" + videoServSocket + "/vod/" + videoName + "_1080p.mp4" + "/index.m3u8"; //"http://121.41.1.13:8082/hls/CaiLiu.m3u8";
        videoPath = "http://" + videoServSocket + "/" + videoName + "_1080p.mp4";

        FormBody body = new FormBody.Builder()
                .add("videoID", String.valueOf(videoID))
                .add("videoParam", "playCount")
                .build();

        // 播放量+1
        okp.newCall(new Request.Builder()
                .post(body)
                .url("http://" +mainServSocket+ "/" +war+ "/" + "_4_VideoData")
                .build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        playerActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(playerActivity, "网络连接失败", Toast.LENGTH_SHORT)
                                        .show();
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                    }
                });

        // setText方法必须传入String类型，否则会抛出找不到控件的异常
        ((TextView)findViewById(R.id.tv_open)).setText(videoName);
        ((TextView)findViewById(R.id.videoId)).setText(String.valueOf(videoID));
        ((TextView)findViewById(R.id.videoName)).setText(videoName);
        FlowLayout tagView = findViewById(R.id.videoTags);
        ((TextView)findViewById(R.id.playCnt)).setText(String.valueOf(playCount));
        ((TextView)findViewById(R.id.videoDes)).setText(videoDescription);
        ((TextView)findViewById(R.id.uploadDate)).setText(uploadDate);
        likeCnt.setText(String.valueOf(like));
        shareCnt.setText(String.valueOf(dispatchCount));

        List<String> list = Arrays.asList(videoTag.split("&&"));
        tagView.setAlignByCenter(FlowLayout.AlienState.LEFT);
        tagView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        tagView.setAdapter(list, R.layout.tag, new FlowLayout.ItemView<String>() {
            @Override
            public void getCover(String item, FlowLayout.ViewHolder holder, View inflate, int position) {
                holder.setText(R.id.tv_label_name,item);
            }
        });

        String cachePath = proxy.getProxyUrl(videoPath);
        play(cachePath);
//        play(videoPath);

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
            mv_content.hide();
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
//            play("http://49.235.108.192:8080/super/v1.mp4");
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
            tv_open.getLayoutParams().width = DisplayUtils.dip2px(playerActivity, 200);
            if (thumb.getWidth() > thumb.getHeight()) {
                videoInfo.setVisibility(View.VISIBLE);
                player.setLayoutParams(portraitParam);
                fullscreen.setImageResource(R.drawable.fullscreen_24);
                isFullScreen = false;
            }
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(thumb.getWidth() > thumb.getHeight()) {
                tv_open.getLayoutParams().width = DisplayUtils.dip2px(playerActivity, 800);
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
        String cachePath = proxy.getProxyUrl(videoPath);
        play(cachePath);
//        play(videoPath);
        Toast.makeText(playerActivity, videoPath, Toast.LENGTH_SHORT);
    }

    public void switchHighQuality(View view) {
        if (videoPath == null)
            return;
        if (videoPath.contains("_1080p.mp4"))
            return;
        videoPath = videoPath.replaceAll("_480p.mp4", "_1080p.mp4");
        String cachePath = proxy.getProxyUrl(videoPath);
        play(cachePath);
//        play(videoPath);
        Toast.makeText(playerActivity, videoPath, Toast.LENGTH_SHORT);
    }

    public void modifyVideoData(View view) {
        int viewID = view.getId();
        FormBody postBody = null;
        Call call = null;
        if (viewID == R.id.player_likeButton) {
            postBody = new FormBody.Builder()
                    .add("videoID", String.valueOf(videoID))
                    .add("videoParam", "like")
                    .build();
            call = okp.newCall(new Request.Builder()
                            .url("http://" +mainServSocket+ "/" +war+ "/" + "_4_VideoData")
                            .post(postBody).build());
        } else if (viewID == R.id.player_shareButton) {
            postBody = new FormBody.Builder()
                    .add("videoID", String.valueOf(videoID))
                    .add("videoParam", "dispatchCount")
                    .build();
            call = okp.newCall(new Request.Builder()
                    .url("http://" +mainServSocket+ "/" +war+ "/" + "_4_VideoData")
                    .post(postBody).build());
        }
        Call finalCall = call;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 点赞或转发后返回的消息
                    Response resp = finalCall.execute();
                    if (!resp.isSuccessful()) // 判断是否发送成功
                        throw new IOException();

                    // 确认已修改成功，发送刷新请求
                    Response refreshResp = okp.newCall(new Request.Builder()
                                    .url("http://" +mainServSocket+ "/" +war+ "/" + "_4_VideoData?videoID=" + videoID)
                                    .get()
                                    .build())
                                    .execute();
                    if (!refreshResp.isSuccessful()) // 判断是否发送成功
                        throw new IOException();
                    String[] respText = refreshResp.body().string().trim().split("&&");

                    // 根据点击的按钮刷新
                    if (viewID == R.id.player_likeButton) {
                        playerActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                likeCnt.setText(respText[1].trim().split("=")[1]);
                                likeButton.setBackgroundResource(R.drawable.baseline_thumb_up_24_hasliked);
                                likeButton.setClickable(false);
                                Toast t = new Toast(playerActivity);
                                t.setText("哼，才不需要你点赞呢！");
                                t.show();
                            }
                        });
                    } else if (viewID == R.id.player_shareButton) {
                        playerActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                shareCnt.setText(respText[2].trim().split("=")[1]);
                                Toast t = new Toast(playerActivity);
                                t.setText("哼，才不需要你转发呢！");
                                t.show();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    playerActivity.runOnUiThread(noInternet);
                }
            }
        }).start();


    }
    Runnable noInternet = new Runnable() {
        @Override
        public void run() {
            Toast t = new Toast(playerActivity);
            t.setText("欧尼桑的网络真是条杂鱼呢！");
            t.show();
        }
    };

}