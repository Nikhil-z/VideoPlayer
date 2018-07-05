package com.konka.videoplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

import com.konka.videoplayer.engine.views.KKVideoBaseView;
import com.konka.videoplayer.engine.views.KKVideoStandardView;

public class MainActivity extends AppCompatActivity {

    private KKVideoStandardView kkVideoStandardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        kkVideoStandardView = findViewById(R.id.player);
        kkVideoStandardView.setUp(VideoConstant.videoUrlList[0], KKVideoBaseView.SCREEN_WINDOW_NORMAL, "长江后浪推前浪");
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                kkVideoStandardView.startVideo();
                break;
            case R.id.btn_start_window:
                kkVideoStandardView.startWindowFullscreen();
                break;
            case R.id.btn_start_full_screen:
                KKVideoBaseView.startFullscreen(this, KKVideoStandardView.class, VideoConstant.videoUrlList[0], "长江后浪推前浪");
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }
}
   