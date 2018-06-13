package com.konka.videoplayer.engine.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by HwanJ.Choi on 2018-6-12.
 */

public class StandardUiController extends UiController {

    public StandardUiController(Context context) {
        super(context);
    }

    public StandardUiController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StandardUiController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayout() {
        return 0;
    }

    @Override
    public void showBuffer() {

    }
}
