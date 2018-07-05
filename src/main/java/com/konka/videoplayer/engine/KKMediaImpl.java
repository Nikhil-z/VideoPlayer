package com.konka.videoplayer.engine;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Surface;

import com.konka.videoplayer.engine.interfaces.KKMediaInterface;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by HwanJ.Choi on 2018-6-7.
 */
public class KKMediaImpl extends KKMediaInterface implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener {

    private static final String TAG = "chjKKMediaImpl";
    private MediaPlayer mediaPlayer;

    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public void prepare() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            if (dataSourceObjects.length > 1) {
                mediaPlayer.setLooping((boolean) dataSourceObjects[1]);
            }
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            Class<MediaPlayer> clazz = MediaPlayer.class;
            Method method = clazz.getDeclaredMethod("setDataSource", String.class, Map.class);
            if (dataSourceObjects.length > 2) {
                method.invoke(mediaPlayer, currentDataSource.toString(), dataSourceObjects[2]);
            } else {
                method.invoke(mediaPlayer, currentDataSource.toString(), null);
            }
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(long time) {
        try {
            mediaPlayer.seekTo((int) time);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() {
        if (mediaPlayer != null)
            mediaPlayer.release();
    }

    @Override
    public long getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    @Override
    public long getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    @Override
    public void setSurface(Surface surface) {
        mediaPlayer.setSurface(surface);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        mediaPlayer.setVolume(leftVolume, rightVolume);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared");
        mediaPlayer.start();
        KKMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (KKVideoPlayerViewManager.getCurrentJzvd() != null) {
                    KKVideoPlayerViewManager.getCurrentJzvd().getPlayStateManager().onStatePrepared();
                }
            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onCompletion");
        KKMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (KKVideoPlayerViewManager.getCurrentJzvd() != null) {
                    KKVideoPlayerViewManager.getCurrentJzvd().getPlayStateManager().onStateAutoComplete();
                }
            }
        });
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, final int percent) {
        KKMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (KKVideoPlayerViewManager.getCurrentJzvd() != null) {
                    KKVideoPlayerViewManager.getCurrentJzvd().setBufferProgress(percent);
                }
            }
        });
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        KKMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (KKVideoPlayerViewManager.getCurrentJzvd() != null) {
                    KKVideoPlayerViewManager.getCurrentJzvd().onSeekComplete();
                }
            }
        });
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, final int what, final int extra) {
        Log.d(TAG, "onError");
        KKMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (KKVideoPlayerViewManager.getCurrentJzvd() != null) {
                    KKVideoPlayerViewManager.getCurrentJzvd().getPlayStateManager().onStateError(what, extra);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, final int what, final int extra) {
        Log.d(TAG, "onInfo");
        KKMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (KKVideoPlayerViewManager.getCurrentJzvd() != null) {
//                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
//                        if (KKVideoPlayerViewManager.getCurrentJzvd().currentState == PlayStateManager.CURRENT_STATE_PREPARING) {
//                            KKVideoPlayerViewManager.getCurrentJzvd().getPlayStateManager().onStatePrepared();
//                        }
//                    }
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                            Log.d("chj", "MEDIA_INFO_VIDEO_RENDERING_START");
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            KKVideoPlayerViewManager.getCurrentJzvd().getPlayStateManager().onStateBuffering();
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            KKVideoPlayerViewManager.getCurrentJzvd().getPlayStateManager().onStatePlaying();
                            break;
                    }
                }
            }
        });
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
        KKMediaManager.instance().currentVideoWidth = width;
        KKMediaManager.instance().currentVideoHeight = height;
        KKMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (KKVideoPlayerViewManager.getCurrentJzvd() != null) {
                    KKVideoPlayerViewManager.getCurrentJzvd().onVideoSizeChanged();
                }
            }
        });
    }
}
