package com.aliyun.auikits.karaoke.impl;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.AliRtcEngineMediaPlayer;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomEngine;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomEngineCallback;
import com.aliyun.auikits.karaoke.bean.KTVMusicConfig;
import com.aliyun.auikits.karaoke.bean.KTVMusicPlayState;
import com.aliyun.auikits.karaoke.bean.KTVMusicPrepareState;
import com.aliyun.auikits.karaoke.bean.KTVSingerRole;
//import com.aliyun.auikits.ktvroom.rtc.AUIRTCService;
//import com.aliyun.auikits.ktvroom.rtc.AUIRTCServiceCallback;
import com.google.gson.Gson;

public class ARTCKaraokeRoomEngineImpl implements ARTCKaraokeRoomEngine, AliRtcEngineMediaPlayer.IAliRtcMediaPlayerEventHandler {

    private static class PlayStateMsg {
        @Keep
        final int playState;

        public PlayStateMsg(int state) {
            playState = state;
        }
    }

    private static final String TAG = "KTVEngine";

    private static final int MSG_ID_LOCAL_PREPARE_STATE_UPDATE = 0;
    private static final int MSG_ID_LOCAL_PLAY_PROGRESS_UPDATE = 1;
    private static final int MSG_ID_LOCAL_PLAY_STATE_UPDATE = 2;
    private static final int MSG_ID_REMOTE_DATA_MSG_RECEIVED = 3;

    @NonNull
    private final AliRtcEngine mRtcEngine;
    @Nullable
    private ARTCKaraokeRoomEngineCallback mCallback;
    private KTVSingerRole mSingerRole = KTVSingerRole.Audience;
    @Nullable
    private AliRtcEngineMediaPlayer mMediaPlayer;
    private KTVMusicPrepareState mMediaPrepareState = KTVMusicPrepareState.Unprepared;
    private KTVMusicPlayState mMediaPlayState = KTVMusicPlayState.Idle;
    private long mMusicTotalDuration = 0;
    private long mLastAutoSeekTime = 0;
    private boolean mHasAutoStartPlay = false;
    private boolean mIsMultipleTrack = false;
    private final Gson mGson = new Gson();

    /**
     * RTC 的回调方法都在子线程，需要通过这个 Handler 抛到主线程处理
     */
    private final Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull final Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ID_LOCAL_PREPARE_STATE_UPDATE:
                    if (msg.obj instanceof KTVMusicPrepareState) {
                        onLocalPrepareStateUpdate((KTVMusicPrepareState) msg.obj, msg.arg1 == 1);
                    }
                    break;
                case MSG_ID_LOCAL_PLAY_PROGRESS_UPDATE:
                    if (msg.obj instanceof Long) {
                        onLocalPlayProgressUpdate((Long) msg.obj);
                    }
                    break;
                case MSG_ID_LOCAL_PLAY_STATE_UPDATE:
                    if (msg.obj instanceof KTVMusicPlayState) {
                        onLocalPlayStateUpdate((KTVMusicPlayState) msg.obj);
                    }
                    break;
                case MSG_ID_REMOTE_DATA_MSG_RECEIVED:
                    if (msg.obj instanceof AliRtcEngine.AliRtcDataChannelMsg) {
                        onCustomDataMsgReceived((AliRtcEngine.AliRtcDataChannelMsg) msg.obj);
                    }
                    break;
            }
        }
    };


//    /**
//     * @param rtcService {@link AUIRTCService} 实例，生命周期需要比当前类的生命周期更长
//     */
//    public ARTCKaraokeRoomEngineImpl(@NonNull AUIRTCService rtcService) {
//        mRtcEngine = rtcService.getRtcEngine();
//        rtcService.addCallback(new AUIRTCServiceCallback() {
//            @Override
//            public void onDataMsgReceived(String uid, AliRtcEngine.AliRtcDataChannelMsg channelMsg) {
//                Message.obtain(mMainHandler, MSG_ID_REMOTE_DATA_MSG_RECEIVED, channelMsg).sendToTarget();
//            }
//        });
//    }

    /**
     * @param rtcEngine {@link AliRtcEngine} 实例，生命周期需要比当前类的生命周期更长。使用这个构造方法时，
     *                  需要自己调用 {@link #onCustomDataMsgReceived} 将 data channel 收到的消息传进来
     */
    public ARTCKaraokeRoomEngineImpl(@NonNull AliRtcEngine rtcEngine) {
        mRtcEngine = rtcEngine;
    }

    @Override
    public void setCallback(@Nullable ARTCKaraokeRoomEngineCallback callback) {
        mCallback = callback;
    }

    @Override
    public boolean setSingerRole(final KTVSingerRole newRole) {
        final KTVSingerRole oldRole = mSingerRole;
        if (newRole == oldRole) {
            return true;
        }

        switch (newRole) {
            case LeadSinger:
                mRtcEngine.publishLocalDualAudioStream(true);
                mRtcEngine.subscribeAllRemoteDualAudioStreams(false);
                break;
            case Choristers:
                // 合唱者本地播放音乐，不需要拉主唱者的伴奏流
                mRtcEngine.publishLocalDualAudioStream(false);
                mRtcEngine.subscribeAllRemoteDualAudioStreams(false);
                break;
            case Audience:
                mRtcEngine.publishLocalDualAudioStream(false);
                mRtcEngine.subscribeAllRemoteDualAudioStreams(true);
                stopMusic();
                break;
        }

        Log.d(TAG, "set singer role from " + oldRole + " to " + newRole);
        mSingerRole = newRole;
        ARTCKaraokeRoomEngineCallback callback = mCallback;
        if (callback != null) {
            callback.onSingerRoleUpdate(oldRole, newRole);
        }
        return true;
    }


    @Override
    public void loadMusic(@NonNull final String uri) {
        KTVMusicConfig config = new KTVMusicConfig();
        config.uri = uri;
        loadMusic(config);
    }

    @Override
    public void loadCopyrightMusic(@NonNull String songId) {
        KTVMusicConfig config = new KTVMusicConfig();
        config.songId = songId;
        loadMusic(config);
    }

    @Override
    public void loadMusic(@NonNull final KTVMusicConfig config) {
        Log.d(TAG, "load music resource uri: " + config.uri + ", songId: " + config.songId + ", auto play: " + config.autoPlay);
        stopMusic();
        AliRtcEngineMediaPlayer player = mRtcEngine.createMediaPlayer();
        if (player != null) {
            player.setEventHandler(this);
            player.enablePublishByDualStream(true); // 如果仅需独唱玩法则可以不调用
            player.enableAux(true);
            if (config.isMultipleTrack) {
                player.setAudioDualChannelMode(AliRtcEngineMediaPlayer.AliRtcMediaPlayerAudioMode.AliRtcMediaPlayerAudioModeTrackSwitch);
            } else {
                player.setAudioDualChannelMode(AliRtcEngineMediaPlayer.AliRtcMediaPlayerAudioMode.AliRtcMediaPlayerAudioModeChannelSwitch);
            }
            player.setProgressInterval(50);
            mIsMultipleTrack = config.isMultipleTrack;
            mMediaPlayer = player;

            updateMusicPrepareStateAndNotify(KTVMusicPrepareState.Preparing);

            final boolean autoPlay = config.autoPlay;
            AliRtcEngineMediaPlayer.IAliRtcMediaPlayerLoadResourceCallback loadResourceCallback = new AliRtcEngineMediaPlayer.IAliRtcMediaPlayerLoadResourceCallback() {
                @Override
                public void onLoadResourceCallback(int errorCode) {
                    Log.d(TAG, "load resource finish, result = " + errorCode);
                    boolean success = errorCode == 0;
                    KTVMusicPrepareState newState = success ? KTVMusicPrepareState.Prepared : KTVMusicPrepareState.Failed;
                    Message.obtain(mMainHandler, MSG_ID_LOCAL_PREPARE_STATE_UPDATE, autoPlay ? 1 : 0, 0, newState).sendToTarget();
                }
            };
            if (!TextUtils.isEmpty(config.uri)) {
                player.loadResourceWithPosition(config.uri, config.startPosition, loadResourceCallback);
            } else if (!TextUtils.isEmpty(config.songId)) {
                player.loadCopyrightedMusicResourceWithPosition(config.songId, config.startPosition, loadResourceCallback);
            }
            setMusicAccompanimentMode(false);
        }
    }

    @Override
    public void playMusic() {
        AliRtcEngineMediaPlayer player = mMediaPlayer;
        if (player != null && mMediaPrepareState == KTVMusicPrepareState.Prepared) {
            player.start();
        }
    }

    @Override
    public void stopMusic() {
        AliRtcEngineMediaPlayer player = mMediaPlayer;
        if (player != null) {
            player.stop();
            mRtcEngine.destroyMediaPlayer(player);
            mMediaPlayer = null;
            mMusicTotalDuration = 0;
            mLastAutoSeekTime = 0;
            mHasAutoStartPlay = false;
            mIsMultipleTrack = false;

            mMainHandler.removeMessages(MSG_ID_LOCAL_PREPARE_STATE_UPDATE);
            mMainHandler.removeMessages(MSG_ID_LOCAL_PLAY_PROGRESS_UPDATE);
            mMainHandler.removeMessages(MSG_ID_LOCAL_PLAY_STATE_UPDATE);
            updateMusicPrepareStateAndNotify(KTVMusicPrepareState.Unprepared);
            updateMusicPlayStateAndNotify(KTVMusicPlayState.Idle);
        }
    }

    @Override
    public void resumeMusic() {
        AliRtcEngineMediaPlayer player = mMediaPlayer;
        if (player != null && mMediaPrepareState == KTVMusicPrepareState.Prepared) {
            player.resume();
        }
    }

    @Override
    public void pauseMusic() {
        AliRtcEngineMediaPlayer player = mMediaPlayer;
        if (player != null && mMediaPrepareState == KTVMusicPrepareState.Prepared) {
            player.pause();
        }
    }

    @Override
    public void seekMusicTo(final long millisecond) {
        if (millisecond >= 0 && mMediaPrepareState == KTVMusicPrepareState.Prepared) {
            AliRtcEngineMediaPlayer player = mMediaPlayer;
            if (player != null) {
                player.seekTo(millisecond, errorCode -> {
                    if (errorCode != 0) {
                        Log.d(TAG, "seek music to position(ms) " + millisecond + " error: " + errorCode);
                    }
                });
            }
        }
    }

    @Override
    public long getMusicTotalDuration() {
        AliRtcEngineMediaPlayer player = mMediaPlayer;
        if (player != null && mMediaPrepareState == KTVMusicPrepareState.Prepared) {
            if (mMusicTotalDuration == 0) {
                mMusicTotalDuration = player.getTotalDuration();
            }
            return mMusicTotalDuration;
        }
        return 0;
    }

    @Override
    public long getMusicCurrentProgress() {
        AliRtcEngineMediaPlayer player = mMediaPlayer;
        if (player != null && mMediaPrepareState == KTVMusicPrepareState.Prepared) {
            return player.getCurrentProgress();
        }
        return 0;
    }

    @Override
    public void setMusicAccompanimentMode(final boolean original) {
        AliRtcEngineMediaPlayer player = mMediaPlayer;
        if (player != null) {
            if (mIsMultipleTrack) {
                player.setAudioTrackIndex(original ? 0 : 1);
            } else {
                player.setAudioTrackIndex(original ? 1 : 0);
            }
        }
    }

    @Override
    public void setMusicVolume(final int volume) {
        AliRtcEngineMediaPlayer player = mMediaPlayer;
        if (player != null) {
            player.setPlayVolume(volume);
        }
    }


    /**********************************************************************************************
     * AliRtcEngineMediaPlayer.IAliRtcMediaPlayerEventHandler 回调方法开始（都在子线程回调）
     *********************************************************************************************/

    @Override
    public void onMediaPlayerStateUpdate(AliRtcEngineMediaPlayer mediaPlayer, AliRtcEngineMediaPlayer.AliRtcMediaPlayerState state, int errorCode) {
        Log.d(TAG, "onMediaPlayerStateUpdate, state: " + state.getValue() + ", error code:" + errorCode);
        KTVMusicPlayState newState = transformPlayerState(state);
        Message.obtain(mMainHandler, MSG_ID_LOCAL_PLAY_STATE_UPDATE, newState).sendToTarget();
    }

    @Override
    public void onMediaPlayerNetworkEvent(AliRtcEngineMediaPlayer mediaPlayer, AliRtcEngineMediaPlayer.AliRtcMediaPlayerNetworkEvent networkEvent) {
    }

    @Override
    public void onMediaPlayerPlayingProgress(AliRtcEngineMediaPlayer mediaPlayer, long millisecond) {
        //Log.v(TAG, "onMediaPlayerPlayingProgress, position(ms): " + millisecond);
        Message.obtain(mMainHandler, MSG_ID_LOCAL_PLAY_PROGRESS_UPDATE, millisecond).sendToTarget();
    }

    @Override
    public void onMediaPlayerFirstFrameEvent(AliRtcEngineMediaPlayer mediaPlayer, AliRtcEngineMediaPlayer.AliRtcMediaPlayerFirstFrameEvent event) {
    }

    @Override
    public void onMediaPlayerLocalCache(AliRtcEngineMediaPlayer mediaPlayer, int errorCode, String resource, String cachedFile) {
    }

    /**********************************************************************************************
     * AliRtcEngineMediaPlayer.IAliRtcMediaPlayerEventHandler 回调方法结束
     *********************************************************************************************/


    private static KTVMusicPlayState transformPlayerState(AliRtcEngineMediaPlayer.AliRtcMediaPlayerState playerState) {
        switch (playerState) {
            case AliRtcPlayerStateNoPlay: return KTVMusicPlayState.Idle;
            case AliRtcPlayerStatePlaying: return KTVMusicPlayState.Playing;
            case AliRtcPlayerStatePausing: return KTVMusicPlayState.Paused;
            case AliRtcPlayerStatePlayEnded: return KTVMusicPlayState.Completed;
            default:
                Log.e(TAG, "Unknown media player state!: " + playerState.getValue());
                return KTVMusicPlayState.Idle;
        }
    }

    private void notifyPlayProgressUpdate(final long millisecond) {
        ARTCKaraokeRoomEngineCallback callback = mCallback;
        if (callback != null) {
            callback.onMusicPlayProgressUpdate(millisecond);
        }
    }

    private void onLocalPlayProgressUpdate(final long millisecond) {
        notifyPlayProgressUpdate(millisecond);

        // 主唱者发送进度给其他用户同步，合唱者发送假进度（RTC SDK 内部实现需要用到）
        //if (mSingerRole == KTVSingerRole.LeadSinger) {
            AliRtcEngine.AliRtcDataChannelMsg msg = new AliRtcEngine.AliRtcDataChannelMsg();
            msg.type = AliRtcEngine.AliRtcDataMsgType.AliEngineDataMsgMusicProgress;
            msg.networkTime = mRtcEngine.getNetworkTime();
            msg.progress = mSingerRole == KTVSingerRole.LeadSinger ? (int) millisecond : -1;
            mRtcEngine.sendDataChannelMsg(msg);
        //}
    }

    private void onLocalPlayStateUpdate(KTVMusicPlayState newState) {
        updateMusicPlayStateAndNotify(newState);

        if (mSingerRole == KTVSingerRole.LeadSinger) {
            AliRtcEngine.AliRtcDataChannelMsg msg = new AliRtcEngine.AliRtcDataChannelMsg();
            msg.type = AliRtcEngine.AliRtcDataMsgType.AliEngineDataMsgCustom;
            msg.networkTime = mRtcEngine.getNetworkTime();
            msg.data = mGson.toJson(new PlayStateMsg(newState.toInt())).getBytes();
            mRtcEngine.sendDataChannelMsg(msg);
        }
    }


    @MainThread
    public void onCustomDataMsgReceived(AliRtcEngine.AliRtcDataChannelMsg msg) {
        if (msg.type == AliRtcEngine.AliRtcDataMsgType.AliEngineDataMsgMusicProgress) {
            if (msg.progress >= 0) { // 忽略合唱者发送的进度消息
                onRemotePlayProgressUpdate(msg.progress, msg.networkTime);
            }
        } else if (msg.type == AliRtcEngine.AliRtcDataMsgType.AliEngineDataMsgCustom) {
            onRemotePlayStateUpdate(new String(msg.data));
        }
    }

    /**
     * 接收到主唱者发送的播放进度消息
     */
    private void onRemotePlayProgressUpdate(long remoteProgress, long remoteNtp) {
        if (mSingerRole == KTVSingerRole.Audience) {
            notifyPlayProgressUpdate(remoteProgress);
        } else if (mSingerRole == KTVSingerRole.Choristers && mMediaPrepareState == KTVMusicPrepareState.Prepared) {
            // 1）合唱者未开始播放，则主动开始播放音乐
            if (mMediaPlayState == KTVMusicPlayState.Idle) {
                handleAutoStartPlay();
                Log.i(TAG, "Chorister: auto start play");
            } else if (mMediaPlayState == KTVMusicPlayState.Playing) {
                // 2）合唱者已在本地播放，则根据主唱者播放进度调整本地播放进度
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - mLastAutoSeekTime >= 1000) { // 避免频繁 seek 导致卡顿
                    long expectProgress = remoteProgress + (mRtcEngine.getNetworkTime() - remoteNtp);
                    long currentProgress = getMusicCurrentProgress();
                    if (Math.abs(expectProgress - currentProgress) > 100) {
                        seekMusicTo(expectProgress);
                        mLastAutoSeekTime = currentTimeMillis;
                        Log.d(TAG, "Chorister: remote playing position diff with local (ms): " + (expectProgress - currentProgress));
                    }
                }
            }
        }
    }

    /**
     * 接收到主唱者发送的播放状态变更消息
     */
    private void onRemotePlayStateUpdate(String msgData) {
        if (mSingerRole == KTVSingerRole.Choristers) {
            final PlayStateMsg dataMsg = mGson.fromJson(msgData, PlayStateMsg.class);
            final KTVMusicPlayState remoteState = KTVMusicPlayState.fromInt(dataMsg.playState);
            if (remoteState != mMediaPlayState) {
                if (remoteState == KTVMusicPlayState.Paused) {
                    pauseMusic();
                } else if (remoteState == KTVMusicPlayState.Playing && mMediaPlayState == KTVMusicPlayState.Paused) {
                    resumeMusic();
                }
            }
        }
    }

    private void onLocalPrepareStateUpdate(KTVMusicPrepareState state, boolean autoPlay) {
        updateMusicPrepareStateAndNotify(state);
        if (state == KTVMusicPrepareState.Prepared && autoPlay) {
            handleAutoStartPlay();
        }
    }

    private void handleAutoStartPlay() {
        if (mMediaPlayState == KTVMusicPlayState.Idle && !mHasAutoStartPlay) {
            playMusic();
            mHasAutoStartPlay = true;
        }
    }


    private void updateMusicPrepareStateAndNotify(KTVMusicPrepareState state) {
        if (mMediaPrepareState != state) {
            mMediaPrepareState = state;
            ARTCKaraokeRoomEngineCallback callback = mCallback;
            if (callback != null) {
                callback.onMusicPrepareStateUpdate(state);
            }
        }
    }

    private void updateMusicPlayStateAndNotify(KTVMusicPlayState state) {
        if (mMediaPlayState != state) {
            mMediaPlayState = state;
            ARTCKaraokeRoomEngineCallback callback = mCallback;
            if (callback != null) {
                callback.onMusicPlayStateUpdate(state);
            }
        }
    }
}
