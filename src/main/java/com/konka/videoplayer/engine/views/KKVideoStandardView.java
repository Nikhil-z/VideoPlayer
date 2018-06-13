package com.konka.videoplayer.engine.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.videoplayer.R;
import com.konka.videoplayer.engine.interfaces.PlayStateListenerAdapter;

/**
 * Created by HwanJ.Choi on 2018-6-7.
 */

public class KKVideoStandardView extends KKVideoBaseView {

    private ProgressBar bottomProgressBar;
    private View loadingView;
    private TextView mReplayText;
    private View retryLayout;

    public KKVideoStandardView(Context context) {
        super(context);
    }

    public KKVideoStandardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void init(Context context) {
        super.init(context);
        bottomProgressBar = findViewById(R.id.bottom_progress);
        loadingView = findViewById(R.id.loading);
        mReplayText = findViewById(R.id.replay_text);
        retryLayout = findViewById(R.id.retry_layout);
        View back = findViewById(R.id.back);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backPress();
            }
        });
        getPlayStateManager().addPlayStateListener(new StateListener());
    }

    @Override
    protected void showBufferLoading(boolean show) {
        loadingView.setVisibility(show ? VISIBLE : INVISIBLE);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.jz_layout_standard;
    }

    @Override
    protected void setProgressAndText(int progress, long position, long duration) {
        super.setProgressAndText(progress, position, duration);
        if (progress != 0) bottomProgressBar.setProgress(progress);
    }

    @Override
    public void setBufferProgress(int bufferProgress) {
        super.setBufferProgress(bufferProgress);
        if (bufferProgress != 0) bottomProgressBar.setSecondaryProgress(bufferProgress);
    }

    @Override
    protected void resetProgressAndTime() {
        super.resetProgressAndTime();
        bottomProgressBar.setProgress(0);
        bottomProgressBar.setSecondaryProgress(0);
    }

    class StateListener extends PlayStateListenerAdapter {

        @Override
        public void onStateError() {
            startButton.setVisibility(INVISIBLE);
            retryLayout.setVisibility(VISIBLE);
        }

        @Override
        public void onStatePause() {
            Log.d("chj", "onStatePause");
            Toast.makeText(getContext(),"onStatePause",Toast.LENGTH_SHORT).show();
            startButton.setImageResource(R.drawable.jz_click_play_selector);
            startButton.setVisibility(VISIBLE);
        }

        @Override
        public void onStateReset() {
            Log.d("chj", "onStateReset");
            startButton.setVisibility(VISIBLE);
            startButton.setImageResource(R.drawable.jz_click_play_selector);
        }

        @Override
        public void onStateAutoComplete() {
            Log.d("chj", "onStateAutoComplete");
            startButton.setImageResource(R.drawable.jz_click_replay_selector);
            startButton.setVisibility(VISIBLE);
        }

        @Override
        public void onStatePlaying() {
            Log.d("chj", "onStatePlaying");
            Toast.makeText(getContext(), "onStatePlaying", Toast.LENGTH_SHORT).show();
            startButton.setImageResource(R.drawable.jz_click_pause_selector);
            startButton.setVisibility(INVISIBLE);
        }
    }
}
