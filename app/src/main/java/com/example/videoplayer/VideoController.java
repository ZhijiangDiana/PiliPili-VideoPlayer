package com.example.videoplayer;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import utils.Utils;

public class VideoController extends RelativeLayout implements OnClickListener, OnSeekBarChangeListener {
	private static final String TAG = "VideoController";
	private Context mContext;
	private ImageView mImagePlay;
	private TextView mCurrentTime;
	private TextView mTotalTime;
	private SeekBar mSeekBar;
	private int mBeginViewId = 0x7F24FFF0;
	private int dip_10, dip_40;

	private MovieView mMovieView;
	private int mCurrent = 0;
	private int mBuffer = 0;
	private int mDuration = 0;
	private boolean bPause = false;
	
	public VideoController(Context context) {
		this(context, null);
	}

	public VideoController(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		dip_10 = Utils.dip2px(mContext, 10);
		dip_40 = Utils.dip2px(mContext, 40);
		initView();
	}

	private TextView newTextView(Context context, int id) {
		TextView tv = new TextView(context);
		tv.setId(id);
		tv.setGravity(Gravity.CENTER);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		LayoutParams params = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		tv.setLayoutParams(params);
		return tv;
	}
	
	private void initView() {
		mImagePlay = new ImageView(mContext);
		LayoutParams imageParams = new LayoutParams(dip_40, dip_40);
		imageParams.addRule(RelativeLayout.CENTER_VERTICAL);
		mImagePlay.setLayoutParams(imageParams);
		mImagePlay.setId(mBeginViewId);
		mImagePlay.setOnClickListener(this);
		mImagePlay.setEnabled(false);
		
		mCurrentTime = newTextView(mContext, mBeginViewId+1);
		LayoutParams currentParams = (LayoutParams) mCurrentTime.getLayoutParams();
		currentParams.setMargins(dip_10, 0, 0, 0);
		currentParams.addRule(RelativeLayout.RIGHT_OF, mImagePlay.getId());
		mCurrentTime.setLayoutParams(currentParams);

		mTotalTime = newTextView(mContext, mBeginViewId+2);
		LayoutParams totalParams = (LayoutParams) mTotalTime.getLayoutParams();
		totalParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mTotalTime.setLayoutParams(totalParams);
		
		mSeekBar = new SeekBar(mContext);
		LayoutParams seekParams = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		totalParams.setMargins(dip_10, 0, dip_10, 0);
		seekParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		seekParams.addRule(RelativeLayout.RIGHT_OF, mCurrentTime.getId());
		seekParams.addRule(RelativeLayout.LEFT_OF, mTotalTime.getId());
		mSeekBar.setLayoutParams(seekParams);
		mSeekBar.setMax(100);
		mSeekBar.setMinimumHeight(100);
		mSeekBar.setThumbOffset(0);
		mSeekBar.setId(mBeginViewId+3);
		mSeekBar.setOnSeekBarChangeListener(this);
		mSeekBar.setEnabled(false);
	}

	private void reset() {
		if (mCurrent == 0 || bPause) {
			mImagePlay.setImageResource(R.drawable.btn_play);
		} else {
			mImagePlay.setImageResource(R.drawable.btn_pause);
		}
		mCurrentTime.setText(Utils.formatTime(mCurrent));
		mTotalTime.setText(Utils.formatTime(mDuration));
		mSeekBar.setProgress((mCurrent==0)?0:(mCurrent*100/mDuration));
		mSeekBar.setSecondaryProgress(mBuffer);
	}
	
	private void refresh() {
		invalidate();
		requestLayout();
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		removeAllViews();
		reset();
		addView(mImagePlay);
		addView(mCurrentTime);
		addView(mTotalTime);
		addView(mSeekBar);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			int time = progress * mDuration / 100;
			try {
				mMovieView.seekTo(time);
			} catch (NullPointerException ignore) {}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// 拖动进度条时要暂停视频
		if (mMovieView.isPlaying()) {
			mMovieView.pause();
			bPause = true;
		}
		mSeekListener.onStartSeek();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mSeekListener.onStopSeek();
		// 拖动进度条时要暂停视频
		if(!mMovieView.isPlaying()) {
			if (mCurrent == 0) {
				mMovieView.begin(null);
			}
			mMovieView.start();
			bPause = false;
		}
	}
	
	private OnSeekChangeListener mSeekListener;
	public static interface OnSeekChangeListener {
		public void onStartSeek();
		public void onStopSeek();
	}
	
	public void setOnSeekChangeListener(OnSeekChangeListener listener) {
		mSeekListener = listener;
	}

	@Override
	public void onClick(View v) {
		if(mMovieView == null)
			return;
		if (v.getId() == mImagePlay.getId()) {
			if (mMovieView.isPlaying()) {
				mMovieView.pause();
				bPause = true;
			} else {
				if (mCurrent == 0) {
					mMovieView.begin(null);
				}
				mMovieView.start();
				bPause = false;
			}
		}
		refresh();
	}
	
	public void enableController() {
		mImagePlay.setEnabled(true);
		mSeekBar.setEnabled(true);
	}
	
	public void setVideoView(MovieView view) {
		mMovieView = view;
		mDuration = mMovieView.getDuration();
	}
	
	public void setCurrentTime(int current_time, int buffer_time) {
		mCurrent = current_time;
		mBuffer = buffer_time;
		refresh();
	}

}
