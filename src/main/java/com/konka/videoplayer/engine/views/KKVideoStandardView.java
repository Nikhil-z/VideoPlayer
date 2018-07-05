package com.konka.videoplayer.engine.views;

import android.content.Context;
import android.util.AttributeSet;

import com.konka.videoplayer.R;

/**
 * Created by HwanJ.Choi on 2018-6-7.
 */

public class KKVideoStandardView extends KKVideoBaseView {

    @Override
    protected UiController createUiController() {
        return new StandardUiController(getContext());
    }

    public KKVideoStandardView(Context context) {
        super(context);
    }

    public KKVideoStandardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void init(Context context) {
        super.init(context);
    }

    @Override
    public void setUp(String url, int screen, Object... objects) {
        super.setUp(url, screen, objects);
        if (objects.length > 0) {
            uiController.setTitleText(objects[0].toString());
        }
        if (screen == KKVideoBaseView.SCREEN_WINDOW_NORMAL) {
            ((StandardUiController) uiController).setCanControlBarShow(false);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_player_standard;
    }
}
