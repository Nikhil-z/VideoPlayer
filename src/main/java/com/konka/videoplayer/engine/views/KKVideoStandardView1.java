package com.konka.videoplayer.engine.views;

import android.content.Context;
import android.util.AttributeSet;

import com.konka.videoplayer.R;

/**
 * Created by HwanJ.Choi on 2018-6-7.
 */

public class KKVideoStandardView1 extends KKVideoBaseView {

    @Override
    protected UiController createUiController() {
        return new StandardUiController(getContext());
    }

    public KKVideoStandardView1(Context context) {
        super(context);
    }

    public KKVideoStandardView1(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void init(Context context) {
        super.init(context);
    }

    @Override
    protected void showBufferLoading(boolean show) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_player_standard;
    }
}
