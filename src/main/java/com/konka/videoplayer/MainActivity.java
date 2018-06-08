package com.konka.videoplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
        kkVideoStandardView.setUp(VideoConstant.videoUrlList[0], KKVideoBaseView.SCREEN_WINDOW_NORMAL, null);
    }

    public void onClick(View view) {
        kkVideoStandardView.startVideo();
    }
}
