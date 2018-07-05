package com.konka.videoplayer.engine.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.konka.videoplayer.R;
import com.konka.videoplayer.engine.KKMediaManager;
import com.konka.videoplayer.engine.PlayStateManager;
import com.konka.videoplayer.engine.interfaces.IControlUi;
import com.konka.videoplayer.engine.interfaces.PlayStateListener;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by HwanJ.Choi on 2018-6-12.
 */

public abstract class UiController extends RelativeLayout implements PlayStateListener, View.OnClickListener {

    protected PlayStateManager playStateManager;
    protected static Timer UPDATE_PROGRESS_TIMER;
    protected ProgressTimerTask progressTimerTask;
    protected KKVideoBaseView kkVideoBaseView;
    protected IControlUi mControlUi;

    public void setControlUiListener(IControlUi listener) {
        mControlUi = listener;
    }

    public UiController(Context context) {
        this(context, null, 0);
    }

    public UiController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UiController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        View.inflate(getContext(), getLayout(), this);
    }

    public void attachToPlayView(KKVideoBaseView videoBaseView) {
        kkVideoBaseView = videoBaseView;
        playStateManager = videoBaseView.getPlayStateManager();
        videoBaseView.getPlayStateManager().addPlayStateListener(this);
        kkVideoBaseView.addView(this);
    }

    protected abstract int getLayout();

    public abstract void showBuffer(boolean show);

    public abstract void updateProgressAndText(int progress, long position, long duration);

    public abstract void resetProgressAndTime();

    public abstract void setTitleText(String title);

    public abstract boolean onHandleKeyEvent(KeyEvent keyEvent);

    public void onBufferingProgress(int progress) {
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start) {
            if (mControlUi != null) {
                mControlUi.startClick();
                int currentState = playStateManager.getCurrentState();
                Log.d("chj", "currenstate" + currentState);
            }
        }
    }

    @Override
    public void onStateReset() {
        cancelProgressTimer();
    }

    @Override
    public void onStatePrepared() {
        resetProgressAndTime();
        startProgressTimer();
    }

    @Override
    public void onStatePause() {
        showBuffer(false);
        startProgressTimer();
    }

    @Override
    public void onStatePlaying() {
        showBuffer(false);
        startProgressTimer();
    }

    @Override
    public void onStateError() {
        showBuffer(false);
        cancelProgressTimer();
    }

    @Override
    public void onStateError(int what, int extra) {

    }

    @Override
    public void onStateBuffering() {
        showBuffer(true);
    }

    @Override
    public void onStateAutoComplete() {
        showBuffer(false);
        cancelProgressTimer();
    }

    protected void startProgressTimer() {
        cancelProgressTimer();
        UPDATE_PROGRESS_TIMER = new Timer();
        progressTimerTask = new ProgressTimerTask();
        UPDATE_PROGRESS_TIMER.schedule(progressTimerTask, 0, 300);
    }

    protected void cancelProgressTimer() {
        if (UPDATE_PROGRESS_TIMER != null) {
            UPDATE_PROGRESS_TIMER.cancel();
        }
        if (progressTimerTask != null) {
            progressTimerTask.cancel();
        }
    }

    long getDuration() {
        long duration = 0;
        //TODO MediaPlayer 判空的问题
//        if (KKMediaManager.instance().mediaPlayer == null) return duration;
        try {
            duration = KKMediaManager.getDuration();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    long getCurrentPositionWhenPlaying() {
        long position = 0;
        //TODO 这块的判断应该根据MediaPlayer来
        if (playStateManager.isPlaying() || playStateManager.isPause()) {
            try {
                position = KKMediaManager.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }

    class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            if (playStateManager.isPlaying() || playStateManager.isPause()) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        long position = getCurrentPositionWhenPlaying();
                        long duration = getDuration();
                        int progress = (int) (position * 100 / (duration == 0 ? 1 : duration));
                        updateProgressAndText(progress, position, duration);
                    }
                });
            }
        }
    }
}
