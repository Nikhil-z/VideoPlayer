package com.konka.videoplayer.engine.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.konka.videoplayer.R;
import com.konka.videoplayer.engine.KKMediaManager;
import com.konka.videoplayer.engine.KKVideoPlayerViewManager;
import com.konka.videoplayer.engine.PlayState;
import com.konka.videoplayer.engine.PlayStateManager;
import com.konka.videoplayer.engine.interfaces.IControlUi;
import com.konka.videoplayer.engine.interfaces.PlayStateListenerAdapter;
import com.konka.videoplayer.utils.JZUtils;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;

/**
 * Created by Nathen on 16/7/30.
 */
public abstract class KKVideoBaseView extends RelativeLayout {

    public static final String TAG = "chjKKVideoBaseView";
    public static final int FULL_SCREEN_NORMAL_DELAY = 300;
    public static final int SCREEN_WINDOW_NORMAL = 0;
    public static final int SCREEN_WINDOW_LIST = 1;
    public static final int SCREEN_WINDOW_FULLSCREEN = 2;

    protected PlayStateManager playStateManager;

    public PlayStateManager getPlayStateManager() {
        return playStateManager;
    }

    public static final String URL_KEY_DEFAULT = "URL_KEY_DEFAULT";//当播放的地址只有一个的时候的key
    public static boolean ACTION_BAR_EXIST = true;
    public static boolean TOOL_BAR_EXIST = true;
    public static int FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
    public static int NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    public static int VIDEO_IMAGE_DISPLAY_TYPE = 0;
    public static long CLICK_QUIT_FULLSCREEN_TIME = 0;
    public static AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {//是否新建个class，代码更规矩，并且变量的位置也很尴尬
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    releaseAllVideos();
                    Log.d(TAG, "AUDIOFOCUS_LOSS [" + this.hashCode() + "]");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    try {
                        KKVideoBaseView player = KKVideoPlayerViewManager.getCurrentJzvd();
                        if (player != null && player.playStateManager.isPlaying()) {
//                            player.startButton.performClick();
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT [" + this.hashCode() + "]");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };
    public int currentScreen = -1;
    public Object[] objects = null;
    public long seekToInAdvance = 0;
    public ViewGroup textureViewContainer;
    public int widthRatio = 0;
    public int heightRatio = 0;
    public Object[] dataSourceObjects;//这个参数原封不动直接通过JZMeidaManager传给JZMediaInterface。
    public int currentUrlMapIndex = 0;
    public int positionInList = -1;
    public int videoRotation = 0;
    protected int mScreenWidth;
    protected int mScreenHeight;
    protected AudioManager mAudioManager;

    protected UiController uiController;

    protected abstract UiController createUiController();

    public KKVideoBaseView(Context context) {
        super(context);
        init(context);
    }

    public KKVideoBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public static void releaseAllVideos() {
        if ((System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) > FULL_SCREEN_NORMAL_DELAY) {
            Log.d(TAG, "releaseAllVideos");
            KKVideoPlayerViewManager.completeAll();
            KKMediaManager.instance().positionInList = -1;
            KKMediaManager.instance().releaseMediaPlayer();
        }
    }

    public static void startFullscreen(Context context, Class<? extends KKVideoBaseView> _class, String url, Object... objects) {
        LinkedHashMap map = new LinkedHashMap();
        map.put(URL_KEY_DEFAULT, url);
        Object[] dataSourceObjects = new Object[1];
        dataSourceObjects[0] = map;
        startFullscreen(context, _class, dataSourceObjects, 0, objects);
    }

    public static void startFullscreen(Context context, Class<? extends KKVideoBaseView> _class, Object[] dataSourceObjects, int defaultUrlMapIndex, Object... objects) {
        hideSupportActionBar(context);
        JZUtils.setRequestedOrientation(context, FULLSCREEN_ORIENTATION);
        try {
            Constructor<? extends KKVideoBaseView> constructor = _class.getConstructor(Context.class);
            final KKVideoBaseView videoBaseView = constructor.newInstance(context);
            FullScreenFloatViewHelper.getInstance(context).addFullScreenView(videoBaseView);
            videoBaseView.setUp(dataSourceObjects, defaultUrlMapIndex, SCREEN_WINDOW_FULLSCREEN, objects);
            CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
            videoBaseView.startVideo();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean backPress() {
        Log.i(TAG, "backPress");
        if ((System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) < FULL_SCREEN_NORMAL_DELAY)
            return false;

        if (KKVideoPlayerViewManager.getSecondFloor() != null) {
            CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
            if (JZUtils.dataSourceObjectsContainsUri(KKVideoPlayerViewManager.getFirstFloor().dataSourceObjects, KKMediaManager.getCurrentDataSource())) {
                KKVideoPlayerViewManager.getFirstFloor().playOnThisJzvd();
            } else {
                quitFullscreen();
            }
            return true;
        } else if (KKVideoPlayerViewManager.getFirstFloor() != null &&
                (KKVideoPlayerViewManager.getFirstFloor().currentScreen == SCREEN_WINDOW_FULLSCREEN)) {
            CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
            quitFullscreen();
            return true;
        }
        return false;
    }

    public static void quitFullscreen() {
        //直接退出全屏和小窗
        KKVideoPlayerViewManager.getFirstFloor().clearFloatScreen();
        KKMediaManager.instance().releaseMediaPlayer();
        KKVideoPlayerViewManager.completeAll();
    }

    public void onSeekComplete() {

    }

    @SuppressLint("RestrictedApi")
    public static void showSupportActionBar(Context context) {
        if (ACTION_BAR_EXIST && JZUtils.getAppCompActivity(context) != null) {
            ActionBar ab = JZUtils.getAppCompActivity(context).getSupportActionBar();
            if (ab != null) {
                ab.setShowHideAnimationEnabled(false);
                ab.show();
            }
        }
        if (TOOL_BAR_EXIST) {
            JZUtils.getWindow(context).clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @SuppressLint("RestrictedApi")
    public static void hideSupportActionBar(Context context) {
        if (ACTION_BAR_EXIST && JZUtils.getAppCompActivity(context) != null) {
            ActionBar ab = JZUtils.getAppCompActivity(context).getSupportActionBar();
            if (ab != null) {
                ab.setShowHideAnimationEnabled(false);
                ab.hide();
            }
        }
        if (TOOL_BAR_EXIST) {
            JZUtils.getWindow(context).setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    protected void init(Context context) {
        setBackgroundColor(Color.BLACK);
        View.inflate(context, getLayoutId(), this);
        textureViewContainer = findViewById(R.id.surface_container);
        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        playStateManager = new PlayStateManager();
        playStateManager.addPlayStateListener(playStateListenerAdapter);
        uiController = createUiController();
        uiController.attachToPlayView(this);
        uiController.setControlUiListener(new IControlUi() {
            @Override
            public void startClick() {
                Log.i(TAG, "onClick start [" + this.hashCode() + "] ");
                if (dataSourceObjects == null || JZUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex) == null) {
                    Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (playStateManager.isNormal() || playStateManager.isAutoComplete() || playStateManager.isError()) {
                    startVideo();
                } else if (playStateManager.isPlaying()) {
                    Log.d(TAG, "pauseVideo [" + this.hashCode() + "] ");
                    KKMediaManager.pause();
                    playStateManager.setCurrentState(PlayState.CURRENT_STATE_PAUSE);
                } else if (playStateManager.isPause()) {
                    KKMediaManager.start();
                    playStateManager.setCurrentState(PlayState.CURRENT_STATE_PLAYING);
                }
            }

            @Override
            public void fullScreenClick() {
                if (currentScreen == SCREEN_WINDOW_FULLSCREEN)
                    backPress();
                else {
                    if (playStateManager.isPlaying() || playStateManager.isPause()) {
                        startWindowFullscreen();
                    }
                }
            }
        });
        try {
            if (isCurrentPlay()) {
                NORMAL_ORIENTATION = ((AppCompatActivity) context).getRequestedOrientation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUp(String url, int screen, Object... objects) {
        LinkedHashMap map = new LinkedHashMap();
        map.put(URL_KEY_DEFAULT, url);
        Object[] dataSourceObjects = new Object[1];
        dataSourceObjects[0] = map;
        setUp(dataSourceObjects, 0, screen, objects);
    }

    public void setUp(Object[] dataSourceObjects, int defaultUrlMapIndex, int screen, Object... objects) {
        if (this.dataSourceObjects != null && JZUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex) != null &&
                JZUtils.getCurrentFromDataSource(this.dataSourceObjects, currentUrlMapIndex).equals(JZUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex))) {
            //设置的数据与当前的数据一样，不处理
            return;
        }
        if (isCurrentJZVD() && JZUtils.dataSourceObjectsContainsUri(dataSourceObjects, KKMediaManager.getCurrentDataSource())) {
            //当前player处于前台并且设置的播放源里有当前正在播放的url
            long position = 0;
            try {
                position = KKMediaManager.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            if (position != 0) {
                JZUtils.saveProgress(getContext(), KKMediaManager.getCurrentDataSource(), position);
            }
            KKMediaManager.instance().releaseMediaPlayer();//？？
        }
        this.dataSourceObjects = dataSourceObjects;
        this.currentUrlMapIndex = defaultUrlMapIndex;
        this.currentScreen = screen;
        this.objects = objects;
        playStateManager.resetState();
    }

    public void startVideo() {
        View start = findViewById(R.id.start);
        if (start != null)
            start.setVisibility(GONE);
        uiController.showBuffer(true);
        KKVideoPlayerViewManager.completeAll();//释放所有的textureview
        Log.d(TAG, "startVideo [" + this.hashCode() + "] ");
        KKMediaManager.instance().initTextureView(getContext());//移除之前的textureview，重新创建一个
        addTextureView();//
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        JZUtils.scanForActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        KKMediaManager.setDataSource(dataSourceObjects);
        KKMediaManager.setCurrentDataSource(JZUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex));
        KKMediaManager.instance().positionInList = positionInList;
        KKVideoPlayerViewManager.setFirstFloor(this);
    }

    private PlayStateListenerAdapter playStateListenerAdapter = new PlayStateListenerAdapter() {

        @Override
        public void onStatePrepared() {
            if (seekToInAdvance != 0) {
                KKMediaManager.seekTo(seekToInAdvance);
                seekToInAdvance = 0;
            } else {
                long position = JZUtils.getSavedProgress(getContext(), JZUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex));
                if (position != 0) {
                    KKMediaManager.seekTo(position);
                }
            }
        }

        @Override
        public void onStateError() {
            if (isCurrentPlay()) {
                KKMediaManager.instance().releaseMediaPlayer();
            }
        }

        @Override
        public void onStateAutoComplete() {
            if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
                backPress();
            }
            KKMediaManager.instance().releaseMediaPlayer();
            JZUtils.saveProgress(getContext(), JZUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex), 0);
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (widthRatio != 0 && heightRatio != 0) {
            int specWidth = MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = (int) ((specWidth * (float) heightRatio) / widthRatio);
            setMeasuredDimension(specWidth, specHeight);

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(specHeight, MeasureSpec.EXACTLY);
            getChildAt(0).measure(childWidthMeasureSpec, childHeightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private long lastBackKeyClickTime;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d("chjdispatchKeyEvent", "[" + this.hashCode() + "]");
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    Log.d("chj", "back ");
                    if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
//                        long current = System.currentTimeMillis();
//                        if (current - lastBackKeyClickTime > 2500) {
//                            Toast.makeText(getContext(), "再按一次返回键退出", Toast.LENGTH_SHORT).show();
//                        } else {
//                            backPress();
//                        }
//                        lastBackKeyClickTime = current;
                        backPress();
                        return true;
                    }
            }
        }
        boolean uiControlConsume = false;
        if (uiController != null) {
            uiControlConsume = uiController.onHandleKeyEvent(event);
        }
        if (uiControlConsume) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    public void onCompletion() {
        Log.i(TAG, "onCompletion " + " [" + this.hashCode() + "] ");
        if (playStateManager.isPlaying() || playStateManager.isPause()) {
            long position = getCurrentPositionWhenPlaying();
            JZUtils.saveProgress(getContext(), JZUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex), position);
        }
        playStateManager.resetState();
        textureViewContainer.removeView(KKMediaManager.textureView);
        KKMediaManager.instance().currentVideoWidth = 0;
        KKMediaManager.instance().currentVideoHeight = 0;

        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        JZUtils.scanForActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        clearFullscreenLayout();
        JZUtils.setRequestedOrientation(getContext(), NORMAL_ORIENTATION);

        if (KKMediaManager.surface != null) KKMediaManager.surface.release();
        if (KKMediaManager.savedSurfaceTexture != null)
            KKMediaManager.savedSurfaceTexture.release();
        KKMediaManager.textureView = null;
        KKMediaManager.savedSurfaceTexture = null;
    }

    void addTextureView() {
        Log.d(TAG, "addTextureView [" + this.hashCode() + "] ");
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
        textureViewContainer.addView(KKMediaManager.textureView, layoutParams);
    }

    void clearFullscreenLayout() {
        FullScreenFloatViewHelper.getInstance(getContext()).removeWindowFullScreen();
        showSupportActionBar(getContext());
    }

    void clearFloatScreen() {
        JZUtils.setRequestedOrientation(getContext(), NORMAL_ORIENTATION);
        showSupportActionBar(getContext());
        KKVideoBaseView videoBaseView = FullScreenFloatViewHelper.getInstance(getContext()).getVideoBaseView();
        if (videoBaseView != null) {
            videoBaseView.textureViewContainer.removeView(KKMediaManager.textureView);
        }
        FullScreenFloatViewHelper.getInstance(getContext()).removeWindowFullScreen();
        KKVideoPlayerViewManager.setSecondFloor(null);
    }

    public void onVideoSizeChanged() {
        Log.i(TAG, "onVideoSizeChanged " + " [" + this.hashCode() + "] ");
        if (KKMediaManager.textureView != null) {
            if (videoRotation != 0) {
                KKMediaManager.textureView.setRotation(videoRotation);
            }
            KKMediaManager.textureView.setVideoSize(KKMediaManager.instance().currentVideoWidth, KKMediaManager.instance().currentVideoHeight);
        }
    }

    public void setBufferProgress(int bufferProgress) {
        if (uiController != null)
            uiController.onBufferingProgress(bufferProgress);
    }

    public long getCurrentPositionWhenPlaying() {
        long position = 0;
        //TODO 这块的判断应该根据MediaPlayer来
        if (playStateManager.isPlaying() || playStateManager.isPause()) {
            try {
                position = KKMediaManager.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }

    public void startWindowFullscreen() {
        Log.i(TAG, "startWindowFullscreen " + " [" + this.hashCode() + "] ");
        hideSupportActionBar(getContext());
        textureViewContainer.removeView(KKMediaManager.textureView);//先移除textureView
        try {
            Constructor<? extends KKVideoBaseView> constructor = getClass().getConstructor(Context.class);
            KKVideoBaseView videoBaseView = constructor.newInstance(getContext());
            FullScreenFloatViewHelper.getInstance(getContext()).addFullScreenView(videoBaseView);
            videoBaseView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
            videoBaseView.setUp(dataSourceObjects, currentUrlMapIndex, SCREEN_WINDOW_FULLSCREEN, objects);
            videoBaseView.playStateManager.setCurrentState(playStateManager.getCurrentState());
            videoBaseView.addTextureView();//将textureView添加至全屏播放，开始播放
            KKVideoPlayerViewManager.setSecondFloor(videoBaseView);
            JZUtils.setRequestedOrientation(getContext(), FULLSCREEN_ORIENTATION);
            CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isCurrentPlay() {
        return isCurrentJZVD()
                && JZUtils.dataSourceObjectsContainsUri(dataSourceObjects, KKMediaManager.getCurrentDataSource());//不仅正在播放的url不能一样，并且各个清晰度也不能一样
    }

    /**
     * @Description: 自己是否处于前台
     */
    public boolean isCurrentJZVD() {
        return KKVideoPlayerViewManager.getCurrentJzvd() != null
                && KKVideoPlayerViewManager.getCurrentJzvd() == this;
    }

    //退出全屏和小窗的方法
    public void playOnThisJzvd() {
        Log.i(TAG, "playOnThisJzvd " + " [" + this.hashCode() + "] ");
        //1.清空全屏和小窗的jzvd
        int secondFloorState = KKVideoPlayerViewManager.getSecondFloor().playStateManager.getCurrentState();
        currentUrlMapIndex = KKVideoPlayerViewManager.getSecondFloor().currentUrlMapIndex;
        clearFloatScreen();
        //2.在本jzvd上播放,从全屏切回小屏
        playStateManager.setCurrentState(secondFloorState);
        addTextureView();//把textrueview添加进来就可以播放了
    }

    public static void clearSavedProgress(Context context, String url) {
        JZUtils.clearSavedProgress(context, url);
    }

    public static void goOnPlayOnResume() {
        if (KKVideoPlayerViewManager.getCurrentJzvd() != null) {
            KKVideoBaseView jzvd = KKVideoPlayerViewManager.getCurrentJzvd();
            if (jzvd.playStateManager.isPause()) {
                KKMediaManager.start();
            }
        }
    }

    public static void goOnPlayOnPause() {
        if (KKVideoPlayerViewManager.getCurrentJzvd() != null) {
            KKVideoBaseView jzvd = KKVideoPlayerViewManager.getCurrentJzvd();
            int currentState = jzvd.playStateManager.getCurrentState();
            if (currentState == PlayState.CURRENT_STATE_AUTO_COMPLETE ||
                    currentState == PlayState.CURRENT_STATE_NORMAL ||
                    currentState == PlayState.CURRENT_STATE_ERROR) {
//                JZVideoPlayer.releaseAllVideos();
            } else {
                KKMediaManager.pause();
            }
        }
    }

    public static void setTextureViewRotation(int rotation) {
        if (KKMediaManager.textureView != null) {
            KKMediaManager.textureView.setRotation(rotation);
        }
    }

    public static void setVideoImageDisplayType(int type) {
        KKVideoBaseView.VIDEO_IMAGE_DISPLAY_TYPE = type;
        if (KKMediaManager.textureView != null) {
            KKMediaManager.textureView.requestLayout();
        }
    }

    public Object getCurrentUrl() {
        return JZUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex);
    }

    protected abstract int getLayoutId();

}