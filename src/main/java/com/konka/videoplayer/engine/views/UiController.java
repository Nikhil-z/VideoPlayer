package com.konka.videoplayer.engine.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.konka.videoplayer.engine.PlayStateManager;

/**
 * Created by HwanJ.Choi on 2018-6-12.
 */

public abstract class UiController extends RelativeLayout {

    private PlayStateManager mPlayStateManager;

    public void setPlayStateManager(PlayStateManager mPlayStateManager) {
        this.mPlayStateManager = mPlayStateManager;
    }

    public UiController(Context context) {
        this(context, null, 0);
    }

    public UiController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UiController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        View.inflate(getContext(), getLayout(), this);
    }

    protected abstract int getLayout();

    public abstract void showBuffer();
}
