package com.konka.videoplayer.engine;

import android.media.AudioManager;
import android.util.Log;
import android.view.Surface;

import com.konka.videoplayer.engine.interfaces.KKMediaInterface;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 *
 */

public class KKMediaIjkplayer extends KKMediaInterface implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnTimedTextListener {

    private static final String TAG = "KKMediaIjkplayer";

    private IjkMediaPlayer ijkMediaPlayer;

    @Override
    public void start() {
        ijkMediaPlayer.start();
    }

    @Override
    public void prepare() {
        ijkMediaPlayer = new IjkMediaPlayer();
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 1024 * 1024);

        ijkMediaPlayer.setOnPreparedListener(KKMediaIjkplayer.this);
        ijkMediaPlayer.setOnVideoSizeChangedListener(KKMediaIjkplayer.this);
        ijkMediaPlayer.setOnCompletionListener(KKMediaIjkplayer.this);
        ijkMediaPlayer.setOnErrorListener(KKMediaIjkplayer.this);
        ijkMediaPlayer.setOnInfoListener(KKMediaIjkplayer.this);
        ijkMediaPlayer.setOnBufferingUpdateListener(KKMediaIjkplayer.this);
        ijkMediaPlayer.setOnSeekCompleteListener(KKMediaIjkplayer.this);
        ijkMediaPlayer.setOnTimedTextListener(KKMediaIjkplayer.this);

        try {
            ijkMediaPlayer.setDataSource(currentDataSource.toString());
            ijkMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            ijkMediaPlayer.setScreenOnWhilePlaying(true);
            ijkMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        ijkMediaPlayer.pause();
    }

    @Override
    public boolean isPlaying() {
        return ijkMediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(long time) {
        ijkMediaPlayer.seekTo(time);
    }

    @Override
    public void release() {
        if (ijkMediaPlayer != null)
            ijkMediaPlayer.release();
    }

    @Override
    public long getCurrentPosition() {
        return ijkMediaPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return ijkMediaPlayer.getDuration();
    }

    @Override
    public void setSurface(Surface surface) {
        ijkMediaPlayer.setSurface(surface);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        ijkMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        ijkMediaPlayer.start();
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
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
        KKMediaManager.instance().currentVideoWidth = iMediaPlayer.getVideoWidth();
        KKMediaManager.instance().currentVideoHeight = iMediaPlayer.getVideoHeight();
        KKMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (KKVideoPlayerViewManager.getCurrentJzvd() != null) {
                    KKVideoPlayerViewManager.getCurrentJzvd().onVideoSizeChanged();
                }
            }
        });
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
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
    public boolean onError(IMediaPlayer iMediaPlayer, final int what, final int extra) {
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
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, final int percent) {
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
    public boolean onInfo(IMediaPlayer mediaPlayer, final int what, final int extra) {
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
                    KKVideoPlayerViewManager.getCurrentJzvd().onInfo(what, extra);
                }
            }
        });
        return false;
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
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
    public void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText) {

    }
}
