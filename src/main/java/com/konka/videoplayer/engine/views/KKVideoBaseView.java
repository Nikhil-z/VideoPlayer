package com.konka.videoplayer.engine.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.videoplayer.R;
import com.konka.videoplayer.engine.KKMediaManager;
import com.konka.videoplayer.engine.KKVideoPlayerViewManager;
import com.konka.videoplayer.engine.PlayStateManager;
import com.konka.videoplayer.engine.interfaces.KKMediaInterface;
import com.konka.videoplayer.engine.interfaces.PlayStateListenerAdapter;
import com.konka.videoplayer.utils.JZUtils;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nathen on 16/7/30.
 */
public abstract class KKVideoBaseView extends FrameLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnTouchListener {

    public static final String TAG = "chjKKVideoBaseView";
    public static final int FULL_SCREEN_NORMAL_DELAY = 300;

    public static final int SCREEN_WINDOW_NORMAL = 0;
    public static final int SCREEN_WINDOW_LIST = 1;
    public static final int SCREEN_WINDOW_FULLSCREEN = 2;
    public static final int SCREEN_WINDOW_TINY = 3;

    protected PlayStateManager playStateManager;

    public PlayStateManager getPlayStateManager() {
        return playStateManager;
    }

    public static final String URL_KEY_DEFAULT = "URL_KEY_DEFAULT";//当播放的地址只有一个的时候的key
    public static boolean ACTION_BAR_EXIST = true;
    public static boolean TOOL_BAR_EXIST = true;
    public static int FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
    public static int NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
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
                            player.startButton.performClick();
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
    protected static Timer UPDATE_PROGRESS_TIMER;
    public int currentScreen = -1;
    public Object[] objects = null;
    public long seekToInAdvance = 0;
    public ImageView startButton;
    public SeekBar progressBar;
    public TextView currentTimeTextView, totalTimeTextView;
    public ViewGroup textureViewContainer;
    public ViewGroup topContainer, bottomContainer;
    public int widthRatio = 0;
    public int heightRatio = 0;
    public Object[] dataSourceObjects;//这个参数原封不动直接通过JZMeidaManager传给JZMediaInterface。
    public int currentUrlMapIndex = 0;
    public int positionInList = -1;
    public int videoRotation = 0;
    protected int mScreenWidth;
    protected int mScreenHeight;
    protected AudioManager mAudioManager;
    protected ProgressTimerTask mProgressTimerTask;
    protected boolean mTouchingProgressBar;
    boolean tmp_test_back = false;

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
        ViewGroup vp = (JZUtils.scanForActivity(context))//.getWindow().getDecorView();
                .findViewById(Window.ID_ANDROID_CONTENT);
        View old = vp.findViewById(R.id.jz_fullscreen_id);
        if (old != null) {
            vp.removeView(old);
        }
        try {
            Constructor<? extends KKVideoBaseView> constructor = _class.getConstructor(Context.class);
            final KKVideoBaseView KKVideoBaseView = constructor.newInstance(context);
            KKVideoBaseView.setId(R.id.jz_fullscreen_id);
            LayoutParams lp = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            vp.addView(KKVideoBaseView, lp);
//            final Animation ra = AnimationUtils.loadAnimation(context, R.anim.start_fullscreen);
//            jzVideoPlayer.setAnimation(ra);
            KKVideoBaseView.setUp(dataSourceObjects, defaultUrlMapIndex, SCREEN_WINDOW_FULLSCREEN, objects);
            CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
//            jzVideoPlayer.startButton.performClick();
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
                quitFullscreenOrTinyWindow();
            }
            return true;
        } else if (KKVideoPlayerViewManager.getFirstFloor() != null &&
                (KKVideoPlayerViewManager.getFirstFloor().currentScreen == SCREEN_WINDOW_FULLSCREEN ||
                        KKVideoPlayerViewManager.getFirstFloor().currentScreen == SCREEN_WINDOW_TINY)) {//以前我总想把这两个判断写到一起，这分明是两个独立是逻辑
            CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
            quitFullscreenOrTinyWindow();
            return true;
        }
        return false;
    }

    public static void quitFullscreenOrTinyWindow() {
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
            if (currentState == PlayStateManager.CURRENT_STATE_AUTO_COMPLETE ||
                    currentState == PlayStateManager.CURRENT_STATE_NORMAL ||
                    currentState == PlayStateManager.CURRENT_STATE_ERROR) {
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

    protected void init(Context context) {
        View.inflate(context, getLayoutId(), this);
        startButton = findViewById(R.id.start);
        progressBar = findViewById(R.id.bottom_seek_progress);
        currentTimeTextView = findViewById(R.id.current);
        totalTimeTextView = findViewById(R.id.total);
        bottomContainer = findViewById(R.id.layout_bottom);
        textureViewContainer = findViewById(R.id.surface_container);
        topContainer = findViewById(R.id.layout_top);

        startButton.setOnClickListener(this);
        progressBar.setOnSeekBarChangeListener(this);
        bottomContainer.setOnClickListener(this);
        textureViewContainer.setOnClickListener(this);
        textureViewContainer.setOnTouchListener(this);

        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        playStateManager = new PlayStateManager();
        playStateManager.addPlayStateListener(playStateListenerAdapter);
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
        } else if (isCurrentJZVD() && !JZUtils.dataSourceObjectsContainsUri(dataSourceObjects, KKMediaManager.getCurrentDataSource())) {
            //当前player处于前台，并且设置的播放源含有当前正在播放的url
            startWindowTiny();
        } else if (!isCurrentJZVD() && JZUtils.dataSourceObjectsContainsUri(dataSourceObjects, KKMediaManager.getCurrentDataSource())) {
            //当前player不是处于前台，设置的播放源含有当前正在播放的url
            if (KKVideoPlayerViewManager.getCurrentJzvd() != null &&
                    KKVideoPlayerViewManager.getCurrentJzvd().currentScreen == KKVideoBaseView.SCREEN_WINDOW_TINY) {
                //需要退出小窗退到我这里，我这里是第一层级
                tmp_test_back = true;
            }
        } else if (!isCurrentJZVD() && !JZUtils.dataSourceObjectsContainsUri(dataSourceObjects, KKMediaManager.getCurrentDataSource())) {
            //当前player不是处于前台，设置的播放源不含有当前正在播放的url
        }
        this.dataSourceObjects = dataSourceObjects;
        this.currentUrlMapIndex = defaultUrlMapIndex;
        this.currentScreen = screen;
        this.objects = objects;
        playStateManager.resetState();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start) {
            Log.i(TAG, "onClick start [" + this.hashCode() + "] ");
            if (dataSourceObjects == null || JZUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex) == null) {
                Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                return;
            }
            if (playStateManager.isNomal() || playStateManager.isAutoComplete()) {
                startVideo();
            } else if (playStateManager.isPlaying()) {
                Log.d(TAG, "pauseVideo [" + this.hashCode() + "] ");
                KKMediaManager.pause();
            } else if (playStateManager.isPause()) {
                KKMediaManager.start();
            }
        }
    }

    public void startVideo() {
        showBufferLoading(true);
        startButton.setVisibility(GONE);
        KKVideoPlayerViewManager.completeAll();//释放所有的textureview
        Log.d(TAG, "startVideo [" + this.hashCode() + "] ");
        initTextureView();//移除之前的textureview，重新创建一个
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
        public void onStateReset() {
            cancelProgressTimer();
        }

        @Override
        public void onStatePrepared() {
            resetProgressAndTime();
            if (seekToInAdvance != 0) {
                KKMediaManager.seekTo(seekToInAdvance);
                seekToInAdvance = 0;
            } else {
                long position = JZUtils.getSavedProgress(getContext(), JZUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex));
                if (position != 0) {
                    KKMediaManager.seekTo(position);
                }
            }
            startProgressTimer();
        }

        @Override
        public void onStatePause() {
            startProgressTimer();
        }

        @Override
        public void onStateError() {
            cancelProgressTimer();
            if (isCurrentPlay()) {
                KKMediaManager.instance().releaseMediaPlayer();
            }
        }

        @Override
        public void onStateAutoComplete() {
            cancelProgressTimer();
            progressBar.setProgress(100);
            currentTimeTextView.setText(totalTimeTextView.getText());
            if (currentScreen == SCREEN_WINDOW_FULLSCREEN || currentScreen == SCREEN_WINDOW_TINY) {
                backPress();
            }
            KKMediaManager.instance().releaseMediaPlayer();
            JZUtils.saveProgress(getContext(), JZUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex), 0);
        }
    };

    public void onInfo(int what, int extra) {
        Log.d(TAG, "onInfo what - " + what + " extra - " + extra);
        switch (what) {
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                showBufferLoading(false);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                showBufferLoading(true);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                showBufferLoading(false);
                break;
        }
    }

    protected abstract void showBufferLoading(boolean show);

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN || currentScreen == SCREEN_WINDOW_TINY) {
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

    public void onCompletion() {
        Log.i(TAG, "onCompletion " + " [" + this.hashCode() + "] ");
        if (playStateManager.isPlaying() || playStateManager.isPause()) {
            long position = getCurrentPositionWhenPlaying();
            JZUtils.saveProgress(getContext(), JZUtils.getCurrentFromDataSource(dataSourceObjects, currentUrlMapIndex), position);
        }
        cancelProgressTimer();
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

    void initTextureView() {
        removeTextureView();
        KKMediaManager.textureView = new KKResizeTextureView(getContext());
        KKMediaManager.textureView.setSurfaceTextureListener(KKMediaManager.instance());
    }

    void addTextureView() {
        Log.d(TAG, "addTextureView [" + this.hashCode() + "] ");
        LayoutParams layoutParams =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
        textureViewContainer.addView(KKMediaManager.textureView, layoutParams);
    }

    void removeTextureView() {
        KKMediaManager.savedSurfaceTexture = null;
        if (KKMediaManager.textureView != null && KKMediaManager.textureView.getParent() != null) {
            ((ViewGroup) KKMediaManager.textureView.getParent()).removeView(KKMediaManager.textureView);
        }
    }

    void clearFullscreenLayout() {
        ViewGroup vp = (JZUtils.scanForActivity(getContext()))//.getWindow().getDecorView();
                .findViewById(Window.ID_ANDROID_CONTENT);
        View oldF = vp.findViewById(R.id.jz_fullscreen_id);
        View oldT = vp.findViewById(R.id.jz_tiny_id);
        if (oldF != null) {
            vp.removeView(oldF);
        }
        if (oldT != null) {
            vp.removeView(oldT);
        }
        showSupportActionBar(getContext());
    }

    void clearFloatScreen() {
        JZUtils.setRequestedOrientation(getContext(), NORMAL_ORIENTATION);
        showSupportActionBar(getContext());
        ViewGroup vp = (JZUtils.scanForActivity(getContext()))//.getWindow().getDecorView();
                .findViewById(Window.ID_ANDROID_CONTENT);
        KKVideoBaseView fullJzvd = vp.findViewById(R.id.jz_fullscreen_id);
        KKVideoBaseView tinyJzvd = vp.findViewById(R.id.jz_tiny_id);

        if (fullJzvd != null) {
            vp.removeView(fullJzvd);
            if (fullJzvd.textureViewContainer != null)
                fullJzvd.textureViewContainer.removeView(KKMediaManager.textureView);
        }
        if (tinyJzvd != null) {
            vp.removeView(tinyJzvd);
            if (tinyJzvd.textureViewContainer != null)
                tinyJzvd.textureViewContainer.removeView(KKMediaManager.textureView);
        }
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

    void startProgressTimer() {
        Log.i(TAG, "startProgressTimer: " + " [" + this.hashCode() + "] ");
        cancelProgressTimer();
        UPDATE_PROGRESS_TIMER = new Timer();
        mProgressTimerTask = new ProgressTimerTask();
        UPDATE_PROGRESS_TIMER.schedule(mProgressTimerTask, 0, 300);
    }

    void cancelProgressTimer() {
        if (UPDATE_PROGRESS_TIMER != null) {
            UPDATE_PROGRESS_TIMER.cancel();
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
        }
    }

    protected void setProgressAndText(int progress, long position, long duration) {
//        Log.d(TAG, "setProgressAndText: progress=" + progress + " position=" + position + " duration=" + duration);
        if (!mTouchingProgressBar) {
            if (progress != 0) progressBar.setProgress(progress);
        }
        if (position != 0) currentTimeTextView.setText(JZUtils.stringForTime(position));
        totalTimeTextView.setText(JZUtils.stringForTime(duration));
    }

    public void setBufferProgress(int bufferProgress) {
        if (bufferProgress != 0) progressBar.setSecondaryProgress(bufferProgress);
    }

    protected void resetProgressAndTime() {
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(0);
        currentTimeTextView.setText(JZUtils.stringForTime(0));
        totalTimeTextView.setText(JZUtils.stringForTime(0));
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

    long getDuration() {
        long duration = 0;
        //TODO MediaPlayer 判空的问题
//        if (KKMediaManager.instance().mediaPlayer == null) return duration;
        try {
            duration = KKMediaManager.getDuration();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            //设置这个progres对应的时间，给textview
            long duration = getDuration();
            currentTimeTextView.setText(JZUtils.stringForTime(progress * duration / 100));
        }
    }

    public void startWindowFullscreen() {
        Log.i(TAG, "startWindowFullscreen " + " [" + this.hashCode() + "] ");
        hideSupportActionBar(getContext());

        ViewGroup vp = (JZUtils.scanForActivity(getContext()))//.getWindow().getDecorView();
                .findViewById(Window.ID_ANDROID_CONTENT);
        View old = vp.findViewById(R.id.jz_fullscreen_id);
        if (old != null) {
            vp.removeView(old);
        }
        textureViewContainer.removeView(KKMediaManager.textureView);//这一句就会导致小窗SurfaceTexture被回收，停止播放
        try {
            Constructor<KKVideoBaseView> constructor = (Constructor<KKVideoBaseView>) KKVideoBaseView.this.getClass().getConstructor(Context.class);
            KKVideoBaseView videoBaseView = constructor.newInstance(getContext());
            videoBaseView.setId(R.id.jz_fullscreen_id);
            LayoutParams lp = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            vp.addView(videoBaseView, lp);
            videoBaseView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
            videoBaseView.setUp(dataSourceObjects, currentUrlMapIndex, SCREEN_WINDOW_FULLSCREEN, objects);
//            videoBaseView.setState(currentState);//这里需要共享状态
            videoBaseView.playStateManager.setCurrentState(playStateManager.getCurrentState());
            videoBaseView.addTextureView();//这一句使全屏播放的surfacetexture初始化，开始播放
            KKVideoPlayerViewManager.setSecondFloor(videoBaseView);
            JZUtils.setRequestedOrientation(getContext(), FULLSCREEN_ORIENTATION);
            videoBaseView.playStateManager.resetState();
            videoBaseView.progressBar.setSecondaryProgress(progressBar.getSecondaryProgress());
            videoBaseView.startProgressTimer();
            CLICK_QUIT_FULLSCREEN_TIME = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startWindowTiny() {
        Log.i(TAG, "startWindowTiny " + " [" + this.hashCode() + "] ");
        if (playStateManager.isNomal() || playStateManager.isError() || playStateManager.isAutoComplete())
            return;
        ViewGroup vp = (JZUtils.scanForActivity(getContext()))//.getWindow().getDecorView();
                .findViewById(Window.ID_ANDROID_CONTENT);
        View old = vp.findViewById(R.id.jz_tiny_id);
        if (old != null) {
            vp.removeView(old);
        }
        textureViewContainer.removeView(KKMediaManager.textureView);

        try {
            Constructor<? extends KKVideoBaseView> constructor = KKVideoBaseView.this.getClass().getConstructor(Context.class);
            KKVideoBaseView videoBaseView = constructor.newInstance(getContext());
            videoBaseView.setId(R.id.jz_tiny_id);
            LayoutParams lp = new LayoutParams(400, 400);
            lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            vp.addView(videoBaseView, lp);
            videoBaseView.setUp(dataSourceObjects, currentUrlMapIndex, SCREEN_WINDOW_TINY, objects);
            videoBaseView.playStateManager.setCurrentState(playStateManager.getCurrentState());
            videoBaseView.addTextureView();
            KKVideoPlayerViewManager.setSecondFloor(videoBaseView);
            playStateManager.resetState();
        } catch (InstantiationException e) {
            e.printStackTrace();
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

    public static void setMediaInterface(KKMediaInterface mediaInterface) {
        KKMediaManager.instance().kkMediaInterface = mediaInterface;
    }

    public class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            Log.d("chj", "progress timer run");
            if (playStateManager.isPlaying() || playStateManager.isPause()) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        long position = getCurrentPositionWhenPlaying();
                        long duration = getDuration();
                        int progress = (int) (position * 100 / (duration == 0 ? 1 : duration));
                        setProgressAndText(progress, position, duration);
                    }
                });
            }
        }
    }

}