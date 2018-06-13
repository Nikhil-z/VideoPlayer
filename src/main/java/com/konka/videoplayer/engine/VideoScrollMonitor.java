package com.konka.videoplayer.engine;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.konka.videoplayer.engine.views.KKVideoBaseView;
import com.konka.videoplayer.utils.JZUtils;

/**
 * Created by HwanJ.Choi on 2018-6-7.
 */

public class VideoScrollMonitor {

    private static final String TAG = "VideoScrollMonitor";

    public static void onScrollReleaseAllVideos(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int lastVisibleItem = firstVisibleItem + visibleItemCount;
        int currentPlayPosition = KKMediaManager.instance().positionInList;
        Log.e(TAG, "onScrollReleaseAllVideos: " +
                currentPlayPosition + " " + firstVisibleItem + " " + currentPlayPosition + " " + lastVisibleItem);
        if (currentPlayPosition >= 0) {
            if ((currentPlayPosition < firstVisibleItem || currentPlayPosition > (lastVisibleItem - 1))) {
                if (KKVideoPlayerViewManager.getCurrentJzvd().currentScreen != KKVideoBaseView.SCREEN_WINDOW_FULLSCREEN) {
                    KKVideoBaseView.releaseAllVideos();//为什么最后一个视频横屏会调用这个，其他地方不会
                }
            }
        }
    }

    public static void onChildViewAttachedToWindow(View view, int jzvdId) {
        if (KKVideoPlayerViewManager.getCurrentJzvd() != null) {
            KKVideoBaseView videoPlayer = view.findViewById(jzvdId);
            if (videoPlayer != null && JZUtils.getCurrentFromDataSource(videoPlayer.dataSourceObjects, videoPlayer.currentUrlMapIndex).equals(KKMediaManager.getCurrentDataSource())) {
                KKVideoBaseView.backPress();
            }
        }
    }

    public static void onChildViewDetachedFromWindow(View view) {
        if (KKVideoPlayerViewManager.getCurrentJzvd() != null) {
            KKVideoBaseView videoPlayer = KKVideoPlayerViewManager.getCurrentJzvd();
            if (((ViewGroup) view).indexOfChild(videoPlayer) != -1) {
                if (videoPlayer.getPlayStateManager().isPause()) {
                    KKVideoBaseView.releaseAllVideos();
                }
            }
        }
    }
}
