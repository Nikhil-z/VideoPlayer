package com.konka.videoplayer.engine;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import com.konka.videoplayer.engine.interfaces.KKMediaInterface;
import com.konka.videoplayer.engine.views.KKResizeTextureView;


/**
 * 这个类用来和jzvd互相调用，当jzvd需要调用Media的时候调用这个类，当MediaPlayer有回调的时候，通过这个类回调JZVD
 * Created by Nathen on 2017/11/18.
 */
public class KKMediaManager implements TextureView.SurfaceTextureListener {

    public static final String TAG = "JiaoZiVideoPlayer";
    public static final int HANDLER_PREPARE = 0;
    public static final int HANDLER_RELEASE = 2;

    public static KKResizeTextureView textureView;
    public static SurfaceTexture savedSurfaceTexture;
    public static Surface surface;
    public int positionInList = -1;
    public KKMediaInterface kkMediaInterface;
    public int currentVideoWidth = 0;
    public int currentVideoHeight = 0;

    public HandlerThread mMediaHandlerThread;
    public MediaHandler mMediaHandler;
    public Handler mainThreadHandler;

    public KKMediaManager() {
        mMediaHandlerThread = new HandlerThread(TAG);
        mMediaHandlerThread.start();
        mMediaHandler = new MediaHandler(mMediaHandlerThread.getLooper());
        mainThreadHandler = new Handler();
        if (kkMediaInterface == null)
            kkMediaInterface = new KKMediaIjkplayer();
    }

    static class SingleTon {
        static KKMediaManager jzMediaManager = new KKMediaManager();
    }

    public static KKMediaManager instance() {
//        if (jzMediaManager == null) {
//            jzMediaManager = new KKMediaManager();
//        }
        return SingleTon.jzMediaManager;
    }

    public static Object[] getDataSource() {
        return instance().kkMediaInterface.dataSourceObjects;
    }

    //这几个方法是不是多余了，为了不让其他地方动MediaInterface的方法
    public static void setDataSource(Object[] dataSourceObjects) {
        instance().kkMediaInterface.dataSourceObjects = dataSourceObjects;
    }

    //正在播放的url或者uri
    public static Object getCurrentDataSource() {
        return instance().kkMediaInterface.currentDataSource;
    }

    public static void setCurrentDataSource(Object currentDataSource) {
        instance().kkMediaInterface.currentDataSource = currentDataSource;
    }

    public static long getCurrentPosition() {
        return instance().kkMediaInterface.getCurrentPosition();
    }

    public static long getDuration() {
        return instance().kkMediaInterface.getDuration();
    }

    public static void seekTo(long time) {
        instance().kkMediaInterface.seekTo(time);
    }

    public static void pause() {
        instance().kkMediaInterface.pause();
    }

    public static void start() {
        instance().kkMediaInterface.start();
    }

    public static boolean isPlaying() {
        return instance().kkMediaInterface.isPlaying();
    }

    public void releaseMediaPlayer() {
        mMediaHandler.removeCallbacksAndMessages(null);
        Message msg = new Message();
        msg.what = HANDLER_RELEASE;
        mMediaHandler.sendMessage(msg);
    }

    public void prepare() {
        releaseMediaPlayer();
        Message msg = new Message();
        msg.what = HANDLER_PREPARE;
        mMediaHandler.sendMessage(msg);
    }

    public void initTextureView(Context context) {
        savedSurfaceTexture = null;
        if (textureView != null && textureView.getParent() != null) {
            ((ViewGroup) textureView.getParent()).removeView(textureView);
        }
        textureView = new KKResizeTextureView(context);
        textureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
//        Log.i(TAG, "onSurfaceTextureAvailable [" + KKVideoPlayerViewManager.getCurrentJzvd().hashCode() + "] ");
        if (savedSurfaceTexture == null) {
            savedSurfaceTexture = surfaceTexture;
            prepare();
        } else {
            textureView.setSurfaceTexture(savedSurfaceTexture);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return savedSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    public class MediaHandler extends Handler {
        public MediaHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_PREPARE:
                    currentVideoWidth = 0;
                    currentVideoHeight = 0;
                    kkMediaInterface.prepare();//初始化mediaplayer

                    if (savedSurfaceTexture != null) {
                        if (surface != null) { //为何要在这里释放，mediaplayer.release不会释放？
                            surface.release();
                        }
                        surface = new Surface(savedSurfaceTexture);//
                        kkMediaInterface.setSurface(surface);
                    }
                    break;
                case HANDLER_RELEASE:
                    kkMediaInterface.release();
                    break;
            }
        }
    }
}
