package com.konka.videoplayer.engine.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.konka.videoplayer.R;
import com.konka.videoplayer.engine.KKMediaManager;
import com.konka.videoplayer.engine.interfaces.IControlUi;
import com.konka.videoplayer.utils.JZUtils;

/**
 * Created by HwanJ.Choi on 2018-6-12.
 */

public class StandardUiController extends UiController implements View.OnClickListener {

    private static final String TAG = "StandardUiController";

    private ImageView startButton, fullScreenBtn;
    private SeekBar progressBar;
    private TextView currentTimeTextView, totalTimeTextView;
    private ViewGroup topContainer, bottomContainer;
    private IControlUi mControlUi;

    private ProgressBar bottomProgressBar;
    private View loadingView;
    private TextView mReplayText;
    private View retryLayout;

    public StandardUiController(Context context) {
        super(context);
    }

    public StandardUiController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StandardUiController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setControlUiListener(IControlUi controlUi) {
        mControlUi = controlUi;
    }

    @Override
    protected void init() {
        super.init();
        startButton = findViewById(R.id.start);
        progressBar = findViewById(R.id.bottom_seek_progress);
        currentTimeTextView = findViewById(R.id.current);
        totalTimeTextView = findViewById(R.id.total);
        bottomContainer = findViewById(R.id.layout_bottom);
        topContainer = findViewById(R.id.layout_top);
        fullScreenBtn = findViewById(R.id.btn_fullscreen);

        bottomProgressBar = findViewById(R.id.bottom_progress);
        loadingView = findViewById(R.id.loading);
        mReplayText = findViewById(R.id.replay_text);
        retryLayout = findViewById(R.id.retry_layout);

        startButton.setOnClickListener(this);
        fullScreenBtn.setOnClickListener(this);
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
    public void onBufferingProgress(int bufferProgress) {
        super.onBufferingProgress(bufferProgress);
        if (bufferProgress != 0) bottomProgressBar.setSecondaryProgress(bufferProgress);
    }

    @Override
    public void showBottomPanel(boolean show) {

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start) {
            if (mControlUi != null) {
                mControlUi.startClick();
            }
        } else if (i == R.id.btn_fullscreen) {
            mControlUi.fullScreenClick();
        }
    }

    @Override
    public void onStateAutoComplete() {
        super.onStateAutoComplete();
        progressBar.setProgress(100);
        currentTimeTextView.setText(totalTimeTextView.getText());
    }

    @Override
    public void onStateError() {
        super.onStateError();
        startButton.setVisibility(INVISIBLE);
        retryLayout.setVisibility(VISIBLE);
    }

    @Override
    public void updateProgressAndText(int progress, long position, long duration) {
//        Log.d(TAG, "updateProgressAndText: progress=" + progress + " position=" + position + " duration=" + duration);
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
}
