package com.konka.videoplayer.engine;

import android.support.annotation.IntDef;

/**
 * Created by HwanJ.Choi on 2018-6-12.
 */

@IntDef({PlayState.CURRENT_STATE_NORMAL, PlayState.CURRENT_STATE_PREPARING, PlayState.CURRENT_STATE_PLAYING
        , PlayState.CURRENT_STATE_PAUSE, PlayState.CURRENT_STATE_AUTO_COMPLETE, PlayState.CURRENT_STATE_ERROR,})
public @interface PlayState {
    int CURRENT_STATE_NORMAL = 0;
    int CURRENT_STATE_PREPARING = 1;
    int CURRENT_STATE_PLAYING = 2;
    int CURRENT_STATE_PAUSE = 5;
    int CURRENT_STATE_AUTO_COMPLETE = 6;
    int CURRENT_STATE_ERROR = 7;
}
