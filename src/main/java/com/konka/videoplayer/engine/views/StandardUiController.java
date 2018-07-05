package com.konka.videoplayer.engine.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.konka.videoplayer.R;
import com.konka.videoplayer.engine.KKMediaManager;
import com.konka.videoplayer.utils.JZUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by HwanJ.Choi on 2018-6-12.
 */

public class StandardUiController extends UiController {

    private static final String TAG = "StandardUiController";
    private static Timer DISMISS_CONTROL_VIEW_TIMER;
    private DismissTimerTask mDismissTimerTask;


    private ImageView startButton, bottomStart;
    private SeekBar progressBar;
    private TextView currentTimeTextView, totalTimeTextView, titleTextView;
    private ViewGroup topContainer, bottomContainer;

    private ProgressBar bottomProgressBar;
    private View loadingView;
    private TextView mReplayText;
    private View retryLayout;
    private boolean canControlBarShow = true;

    public void setCanControlBarShow(boolean canShow) {
        canControlBarShow = canShow;
    }

    public StandardUiController(Context context) {
        super(context);
    }

    public StandardUiController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StandardUiController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
        startButton = findViewById(R.id.start);
        bottomStart = findViewById(R.id.iv_bottom_start);
        progressBar = findViewById(R.id.bottom_seek_progress);
        currentTimeTextView = findViewById(R.id.current);
        totalTimeTextView = findViewById(R.id.total);
        bottomContainer = findViewById(R.id.layout_bottom);
        topContainer = findViewById(R.id.layout_top);

        bottomProgressBar = findViewById(R.id.bottom_progress);
        loadingView = findViewById(R.id.loading);
        mReplayText = findViewById(R.id.replay_text);
        retryLayout = findViewById(R.id.retry_layout);
        titleTextView = findViewById(R.id.title);

        topContainer.setVisibility(GONE);
        bottomContainer.setVisibility(GONE);
        startButton.setOnClickListener(this);
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("chj", "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("chj", "onStopTrackingTouch");
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Log.d("chj", "onProgressChanged");
                    //设置这个progres对应的时间，给textview
                    long duration = getDuration();
                    currentTimeTextView.setText(JZUtils.stringForTime(progress * duration / 100));
                }
            }
        });

        ((CustomSeekBar) progressBar).setOnKeySeekBarChangeListener(new CustomSeekBar.OnKeySeekBarChangeListener() {
            private boolean isTracking;

            @Override
            public void onKeyStartTrackingTouch() {
                if (!isTracking) {
                    cancelProgressTimer();
                    isTracking = true;
                    progressBar.setNextFocusRightId(R.id.bottom_seek_progress);
                }
            }

            @Override
            public void onKeyStopTrackingTouch() {
                startProgressTimer();
                long time = (long) (progressBar.getProgress() * getDuration() * 1.0f / 100);
                long totalDuration = KKMediaManager.getDuration() - 1000;
                KKMediaManager.seekTo(Math.min(totalDuration, time));
                isTracking = false;
                progressBar.setNextFocusRightId(View.NO_ID);
            }
        });
    }

    @Override
    protected int getLayout() {
        return R.layout.layout_standard_controll;
    }

    @Override
    public void showBuffer(boolean show) {
        loadingView.setVisibility(show ? VISIBLE : INVISIBLE);
    }

    @Override
    public void onStatePause() {
        super.onStatePause();
        startButton.setImageResource(R.drawable.play_big);
        bottomStart.setImageResource(R.drawable.play);
        startButton.setVisibility(VISIBLE);
    }

    @Override
    public void onStateBuffering() {
        super.onStateBuffering();
        startButton.setVisibility(GONE);
    }

    @Override
    public void onStatePlaying() {
        super.onStatePlaying();
        bottomStart.setImageResource(R.drawable.pause);
        startButton.setVisibility(INVISIBLE);
    }

    @Override
    public void onStateReset() {
        super.onStateReset();
        retryLayout.setVisibility(GONE);
    }

    @Override
    public void onBufferingProgress(int bufferProgress) {
        super.onBufferingProgress(bufferProgress);
        if (bufferProgress != 0) bottomProgressBar.setSecondaryProgress(bufferProgress);
    }

    @Override
    public void onStateAutoComplete() {
        super.onStateAutoComplete();
        showBuffer(false);
        progressBar.setProgress(100);
        currentTimeTextView.setText(totalTimeTextView.getText());
        startButton.setImageResource(R.drawable.jz_click_replay_selector);
        startButton.setVisibility(VISIBLE);
    }

    @Override
    public void onStateError() {
        super.onStateError();
        showBuffer(false);
        startButton.setVisibility(INVISIBLE);
        retryLayout.setVisibility(VISIBLE);
    }

    @Override
    public void updateProgressAndText(int progress, long position, long duration) {
        if (progress != 0) progressBar.setProgress(progress);
        if (position != 0) currentTimeTextView.setText(JZUtils.stringForTime(position));
        totalTimeTextView.setText(JZUtils.stringForTime(duration));
        if (progress != 0) bottomProgressBar.setProgress(progress);
    }

    @Override
    public void resetProgressAndTime() {
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(0);
        currentTimeTextView.setText(JZUtils.stringForTime(0));
        totalTimeTextView.setText(JZUtils.stringForTime(0));

        bottomProgressBar.setProgress(0);
        bottomProgressBar.setSecondaryProgress(0);
    }

    @Override
    public void setTitleText(String titleText) {
        titleTextView.setText(titleText);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d("chj" + TAG, "dispatchKeyEvent");
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onHandleKeyEvent(KeyEvent event) {
        Log.d("chj", "canshow:" + canControlBarShow);
        if (!canControlBarShow)
            return false;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    cancelDismissTask();
                    showBottomControlBar(true);
                    showTopContainer(true);
                    return true;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    cancelDismissTask();
                    showBottomControlBar(true);
                    showTopContainer(true);
                    return true;
            }

        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    startDismissControlViewTask();
                    return true;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    startDismissControlViewTask();
                    if (mControlUi != null) {
                        mControlUi.startClick();
                    }
                    return true;
            }
        }
        return false;
    }


    private void toggleBar() {
        if (!canControlBarShow)
            return;
        if (topContainer.getVisibility() != VISIBLE) {
            topContainer.setVisibility(VISIBLE);
        } else {
            topContainer.setVisibility(GONE);
        }
        if (bottomContainer.getVisibility() != VISIBLE) {
            bottomContainer.setVisibility(VISIBLE);
        } else {
            bottomContainer.setVisibility(GONE);
        }
    }

    void showBottomControlBar(final boolean show) {
        if (!canControlBarShow)
            return;
        post(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    bottomContainer.setVisibility(VISIBLE);
                } else {
                    bottomContainer.setVisibility(GONE);
                }
            }
        });
    }

    void showTopContainer(final boolean show) {
        if (!canControlBarShow)
            return;
        post(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    topContainer.setVisibility(VISIBLE);
                } else {
                    topContainer.setVisibility(GONE);
                }
            }
        });
    }

    void startDismissControlViewTask() {
        cancelDismissTask();
        mDismissTimerTask = new DismissTimerTask();
        DISMISS_CONTROL_VIEW_TIMER = new Timer();
        DISMISS_CONTROL_VIEW_TIMER.schedule(mDismissTimerTask, 5000);
    }

    void cancelDismissTask() {
        if (mDismissTimerTask != null)
            mDismissTimerTask.cancel();
        if (DISMISS_CONTROL_VIEW_TIMER != null)
            DISMISS_CONTROL_VIEW_TIMER.cancel();
    }

    class DismissTimerTask extends TimerTask {

        @Override
        public void run() {
            showTopContainer(false);
            showBottomControlBar(false);
        }
    }
}
