package com.konka.videoplayer.engine;

import android.util.Log;

import com.konka.videoplayer.engine.interfaces.PlayStateListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HwanJ.Choi on 2018-6-8.
 */

public class PlayStateManager implements PlayStateListener {

    private static final String TAG = "PlayStateManager";
//    public static final int CURRENT_STATE_NORMAL = 0;
//    public static final int CURRENT_STATE_PREPARING = 1;
//    public static final int CURRENT_STATE_PLAYING = 2;
//    public static final int CURRENT_STATE_PAUSE = 5;
//    public static final int CURRENT_STATE_AUTO_COMPLETE = 6;
//    public static final int CURRENT_STATE_ERROR = 7;

    private int mCurrentState = PlayState.CURRENT_STATE_NORMAL;
    private List<PlayStateListener> mListeners = new ArrayList<>();

    public int getCurrentState() {
        return mCurrentState;
    }

    public void setCurrentState(@PlayState int state) {
        this.mCurrentState = state;
        onStateChange(state);
    }

    public void resetState() {
        mCurrentState = PlayState.CURRENT_STATE_NORMAL;
        onStateChange(mCurrentState);
    }

    @Override
    public void onStatePrepared() {
        Log.i(TAG, "onStatePrepared " + " [" + this.hashCode() + "] ");
        mCurrentState = PlayState.CURRENT_STATE_PREPARING;
        onStateChange(mCurrentState);
        onStatePlaying();
    }

    @Override
    public void onStatePlaying() {
        mCurrentState = PlayState.CURRENT_STATE_PLAYING;
        onStateChange(mCurrentState);
    }

    @Override
    public void onStatePause() {
        Log.i(TAG, "onStatePause " + " [" + this.hashCode() + "] ");
        mCurrentState = PlayState.CURRENT_STATE_PAUSE;
        onStateChange(mCurrentState);
    }

    @Override
    public void onStateError(int what, int extra) {
        Log.e(TAG, "onError " + what + " - " + extra + " [" + this.hashCode() + "] ");
        if (what != 38 && extra != -38 && what != -38 && extra != 38 && extra != -19) {
            mCurrentState = PlayState.CURRENT_STATE_ERROR;
            onStateChange(mCurrentState);
        }
    }

    @Override
    public void onStateError() {

    }

    @Override
    public void onStateAutoComplete() {
        Log.i(TAG, "onStateAutoComplete " + " [" + this.hashCode() + "] ");
        mCurrentState = PlayState.CURRENT_STATE_AUTO_COMPLETE;
        onStateChange(mCurrentState);
    }

    @Override
    public void onStateReset() {
        mCurrentState = PlayState.CURRENT_STATE_NORMAL;
        onStateChange(mCurrentState);
    }

    public void addPlayStateListener(PlayStateListener playStateListener) {
        if (!mListeners.contains(playStateListener))
            mListeners.add(playStateListener);
    }

    private void onStateChange(@PlayState int state) {
        for (PlayStateListener listener : mListeners) {
            switch (state) {
                case PlayState.CURRENT_STATE_NORMAL:
                    listener.onStateReset();
                    break;
                case PlayState.CURRENT_STATE_PREPARING:
                    listener.onStatePrepared();
                    break;
                case PlayState.CURRENT_STATE_PLAYING:
                    listener.onStatePlaying();
                    break;
                case PlayState.CURRENT_STATE_PAUSE:
                    listener.onStatePause();
                    break;
                case PlayState.CURRENT_STATE_AUTO_COMPLETE:
                    listener.onStateAutoComplete();
                    break;
                case PlayState.CURRENT_STATE_ERROR:
                    listener.onStateError();
            }
        }
    }

    public boolean isPlaying() {
        return mCurrentState == PlayState.CURRENT_STATE_PLAYING;
    }

    public boolean isPause() {
        return mCurrentState == PlayState.CURRENT_STATE_PAUSE;
    }

    public boolean isError() {
        return mCurrentState == PlayState.CURRENT_STATE_ERROR;
    }

    public boolean isNomal() {
        return mCurrentState == PlayState.CURRENT_STATE_NORMAL;
    }

    public boolean isAutoComplete() {
        return mCurrentState == PlayState.CURRENT_STATE_AUTO_COMPLETE;
    }
}
