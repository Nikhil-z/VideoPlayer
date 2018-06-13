package com.konka.videoplayer.engine.views;

import android.content.Context;
import android.view.WindowManager;

/**
 * Created by HwanJ.Choi on 2018-6-11.
 */

public class FullScreenFloatViewHelper {

    private Context mContext;

    private WindowManager mWindowManager;

    private WindowManager.LayoutParams mLayoutParams;

    private KKVideoBaseView videoBaseView;

    public FullScreenFloatViewHelper(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
//        mLayoutParams.flags = WindowManager.LayoutParams.;
    }

    public void addFullScreenView(KKVideoBaseView view) {
        if (view == null)
            return;
        mWindowManager.addView(view, mLayoutParams);
        videoBaseView = view;
    }

    public void removeWindowFullScreen() {
        mWindowManager.removeView(videoBaseView);
    }

    public KKVideoBaseView getVideoBaseView() {
        return videoBaseView;
    }


}
