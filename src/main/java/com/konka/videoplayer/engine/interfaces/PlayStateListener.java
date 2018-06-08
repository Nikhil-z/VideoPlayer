package com.konka.videoplayer.engine.interfaces;

/**
 * Created by HwanJ.Choi on 2018-6-8.
 */

public interface PlayStateListener {

    void onStatePrepared();

    void onStatePause();

    void onStateError(int what, int extra);

    void onStateError();

    void onStateAutoComplete();

    void onStateReset();

}
