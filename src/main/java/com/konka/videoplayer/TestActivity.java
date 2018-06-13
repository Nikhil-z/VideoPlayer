package com.konka.videoplayer;

import android.os.Bundle;
import android.util.Log;

import com.konka.videoplayer.engine.views.MySeekBar;

/**
 * Created by HwanJ.Choi on 2018-6-12.
 */

public class TestActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        MySeekBar seekBar = findViewById(R.id.progressSeekView);
        seekBar.setmOnKeySeekBarChangeListener(new MySeekBar.OnKeySeekBarChangeListener() {
            @Override
            public void onKeyStartTrackingTouch() {
                Log.d("chj", "onKeyStartTrackingTouch");
            }

            @Override
            public void onKeyStopTrackingTouch() {
                Log.d("chj", "onKeyStopTrackingTouch");
            }
        });
    }
}
