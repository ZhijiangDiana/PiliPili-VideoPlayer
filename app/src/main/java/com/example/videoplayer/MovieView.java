package com.example.videoplayer;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.VideoView;
import androidx.annotation.ColorInt;
import utils.DisplayUtils;
import utils.ViewUtils;

public class MovieView extends VideoView implements View.OnTouchListener, View.OnKeyListener{
    private static final String TAG = "MovieView";
    private Context mContext;
    private int screenWidth, screenHeight;
    private int videoWidth, videoHeight;
    private int realWidth, realHeight;
    private int mXpos, mYpos, mOffset=10;
    public static final int HIDE_TIME = 5000;
    private View mTopView;
    private View mBottomView;
    private Handler mHandler = new Handler();

    public MovieView(Context context) {
        super(context, null);
    }

    public MovieView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public MovieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        screenWidth = DisplayUtils.getScreenWidthPx(context);
        screenHeight = DisplayUtils.getScreenHeightPx(context);
        mOffset = ViewUtils.dp2px(mContext, 10);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(realWidth, widthMeasureSpec);
        int height = getDefaultSize(realHeight, heightMeasureSpec);
        if(realWidth>0 && realHeight>0) {
            if(realWidth*height > width*realHeight)
                height = width * realHeight / realWidth;
            else if(realWidth*height < width*realHeight)
                width = height * realWidth / realHeight;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 获取一次滑动最大距离
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mXpos = (int) event.getX();
                mYpos = (int) event.getY();
                break;
            case MotionEvent.ACTION_UP:
                if(Math.abs(event.getX()-mXpos)<mOffset && Math.abs(event.getY()-mYpos)<mOffset)
                    showOrHide();
                break;
            default:
                break;
        }
        return true;
    }

    public void prepare(View topView, View bottomView) {
        mTopView = topView;
        mBottomView = bottomView;
        setBackgroundColor(Color.BLACK);
    }

    public void begin(MediaPlayer mp) {
        setBackground(null);
        if(mp != null) {
            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();
        }
        realWidth = videoWidth;
        realHeight = videoHeight;
        start();
    }

    public void end(MediaPlayer mp) {
        setBackgroundColor(Color.BLACK);
        realWidth = screenWidth;
        realHeight = screenHeight;
    }

    public void showOrHide() {
        if(mTopView==null || mBottomView==null)
            return;
        if(mTopView.getVisibility() == View.VISIBLE) {
            mTopView.setVisibility(View.GONE);
            mBottomView.setVisibility(View.GONE);
        } else {
            mTopView.setVisibility(View.VISIBLE);
            mBottomView.setVisibility(View.VISIBLE);
            mHandler.removeCallbacks(mHide);
            mHandler.postDelayed(mHide, HIDE_TIME);
        }
    }

    private Runnable mHide = new Runnable() {
        @Override
        public void run() {
            showOrHide();
        }
    };
}
