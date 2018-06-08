package com.konka.videoplayer.engine;

import com.konka.videoplayer.engine.views.KKVideoBaseView;

/**
 * Put JZVideoPlayer into layout
 * From a JZVideoPlayer to another JZVideoPlayer
 * Created by Nathen on 16/7/26.
 */
public class KKVideoPlayerViewManager {

    public static KKVideoBaseView FIRST_FLOOR_JZVD;
    public static KKVideoBaseView SECOND_FLOOR_JZVD;

    public static KKVideoBaseView getFirstFloor() {
        return FIRST_FLOOR_JZVD;
    }

    public static void setFirstFloor(KKVideoBaseView KKVideoBaseView) {
        FIRST_FLOOR_JZVD = KKVideoBaseView;
    }

    public static KKVideoBaseView getSecondFloor() {
        return SECOND_FLOOR_JZVD;
    }

    public static void setSecondFloor(KKVideoBaseView KKVideoBaseView) {
        SECOND_FLOOR_JZVD = KKVideoBaseView;
    }

    public static KKVideoBaseView getCurrentJzvd() {
        if (getSecondFloor() != null) {
            return getSecondFloor();
        }
        return getFirstFloor();
    }

    public static void completeAll() {
        if (SECOND_FLOOR_JZVD != null) {
            SECOND_FLOOR_JZVD.onCompletion();
            SECOND_FLOOR_JZVD = null;
        }
        if (FIRST_FLOOR_JZVD != null) {
            FIRST_FLOOR_JZVD.onCompletion();
            FIRST_FLOOR_JZVD = null;
        }
    }
}
