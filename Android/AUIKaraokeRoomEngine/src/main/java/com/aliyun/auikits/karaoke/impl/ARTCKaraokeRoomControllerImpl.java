package com.aliyun.auikits.karaoke.impl;

import static com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_ADD_MUSIC;
import static com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_COMPLETE_MUSIC;
import static com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_JOIN_SINGING;
import static com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_LEAVE_JOIN_SINGING;
import static com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_OTHER;
import static com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_PIN_MUSIC;
import static com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_REMOVE_MUSIC;
import static com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_SKIP_MUSIC;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alivc.auimessage.model.base.AUIMessageModel;
import com.alivc.auimessage.model.token.IMNewToken;
import com.alivc.rtc.AliRtcEngine;
import com.aliyun.auikits.common.AliyunLog;
import com.aliyun.auikits.common.util.CommonUtil;
import com.aliyun.auikits.im.IMService;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomActionCallback;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomControllerDelegate;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomEngine;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomEngineCallback;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomMusicLibrary;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomMusicLibraryCallback;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomMusicPlayingListService;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomMusicPlayingListServiceCallback;
import com.aliyun.auikits.karaoke.bean.KTVMusicConfig;
import com.aliyun.auikits.karaoke.bean.KTVMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason;
import com.aliyun.auikits.karaoke.bean.KTVMusicPlayState;
import com.aliyun.auikits.karaoke.bean.KTVMusicPrepareState;
import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVRoomState;
import com.aliyun.auikits.karaoke.bean.KTVSingerRole;
import com.aliyun.auikits.rtc.ClientMode;
import com.aliyun.auikits.single.Singleton;
import com.aliyun.auikits.single.server.Server;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngineDelegate;
import com.aliyun.auikits.voiceroom.bean.RoomState;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;
import com.aliyun.auikits.voiceroom.impl.AUIVoiceRoomImplV2;
import com.aliyun.auikits.voiceroom.module.seat.SeatInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ARTCKaraokeRoomControllerImpl extends AUIVoiceRoomImplV2 implements ARTCKaraokeRoomController {
    private static final String TAG = "KaraokeRoomController";

    public static final int MSG_TYPE_MUSIC_PLAYING_LIST_UPDATED = 22001; //歌单更新
    public static final int MSG_TYPE_ROOM_STATE_UPDATED = 22002; // 房间状态更新：1 初始状态、2 等待状态、3 播放状态
    public static final int MSG_TYPE_PLAY_STATE_UPDATED = 22003; // 播放状态更新：1 未播放、2 播放中(带上启播时间戳)、3 暂停、4 播放完成
    public static final int MSG_TYPE_ALL_STATE_SYNC_REQUEST = 22004; // 请求同步房间状态、播放状态等

    private static final String MSG_KEY_TYPE = "type";
    private static final String MSG_KEY_OLD_ROOM_STATE = "old_room_state";
    private static final String MSG_KEY_NEW_ROOM_STATE = "new_room_state";
    private static final String MSG_KEY_SONG_ID = "song_id";
    private static final String MSG_KEY_OLD_PLAY_STATE = "old_play_state";
    private static final String MSG_KEY_NEW_PLAY_STATE = "new_play_state";
    private static final String MSG_KEY_SING_SCORE = "sing_score";
    private static final String MSG_KEY_REASON = "reason";
    private static final String MSG_KEY_USER_ID = "user_id";

    // 播放服务
    private ARTCKaraokeRoomEngineImpl mKTVEngine = null;
    // 点歌服务
    private ARTCKaraokeRoomMusicPlayingListService mPlayingListService = null;
    private ARTCKaraokeRoomMusicPlayingListService.ARTCKaraokeRoomMusicPlayingListServiceFactory mPlayingListServiceFactory = null;
    // 曲库服务
    private ARTCKaraokeRoomMusicLibrary mMusicLibrary = null;
    private ARTCKaraokeRoomMusicLibrary.ARTCKaraokeRoomMusicLibraryFactory mMusicLibraryFactory = null;
    private List<ARTCKaraokeRoomMusicLibraryCallback> mMusicLibraryCallbackList = new ArrayList<>();
    private List<ARTCKaraokeRoomEngineCallback> mRoomEngineCallbackList = new ArrayList<>();

    // 回调Looper
    private Looper mLooper;
    // 房间剧本导演
    private ARTCKaraokeRoomDirector mRoomDirector;

    private int mScore = 0;

    private ARTCKaraokeRoomMusicLibraryCallback mMusicLibraryCallbackWrapper = new ARTCKaraokeRoomMusicLibraryCallback() {
        private List<ARTCKaraokeRoomMusicLibraryCallback> getMusicLibraryCallbackList() {
            List<ARTCKaraokeRoomMusicLibraryCallback> musicLibraryCallbackList = new ArrayList<>();
            synchronized (ARTCKaraokeRoomControllerImpl.this) {
                musicLibraryCallbackList.addAll(mMusicLibraryCallbackList);
            }
            return musicLibraryCallbackList;
        }

        @Override
        public void onMusicDownloadCompleted(KTVMusicInfo musicInfo) {
            List<ARTCKaraokeRoomMusicLibraryCallback> musicLibraryCallbackList = getMusicLibraryCallbackList();
            if (musicLibraryCallbackList.size() > 0) {
                for (ARTCKaraokeRoomMusicLibraryCallback callback : musicLibraryCallbackList) {
                    callback.onMusicDownloadCompleted(musicInfo);
                }
            }

            CommonUtil.runOnUI(new Runnable() {
                @Override
                public void run() {
                    for (ARTCVoiceRoomEngineDelegate voiceRoomControllerDelegate : mRoomCallbacks) {
                        ARTCKaraokeRoomControllerDelegate ktvRoomControllerDelegate = (ARTCKaraokeRoomControllerDelegate) voiceRoomControllerDelegate;
                        ktvRoomControllerDelegate.onMusicDownloadCompleted(musicInfo);
                    }
                }
            });
        }

        @Override
        public void onFail(int errorCode, String errorMsg) {
            List<ARTCKaraokeRoomMusicLibraryCallback> musicLibraryCallbackList = getMusicLibraryCallbackList();
            if (musicLibraryCallbackList.size() > 0) {
                for (ARTCKaraokeRoomMusicLibraryCallback callback : musicLibraryCallbackList) {
                    callback.onFail(errorCode, errorMsg);
                }
            }
        }
    };

    private ARTCKaraokeRoomEngineCallback mRoomEngineCallbackWrapper = new ARTCKaraokeRoomEngineCallback() {
        private List<ARTCKaraokeRoomEngineCallback> getRoomEngineCallbackList() {
            List<ARTCKaraokeRoomEngineCallback> roomEngineCallbackList = new ArrayList<>();
            synchronized (ARTCKaraokeRoomControllerImpl.this) {
                roomEngineCallbackList.addAll(mRoomEngineCallbackList);
            }
            return roomEngineCallbackList;
        }

        @Override
        public void onMusicPrepareStateUpdate(KTVMusicPrepareState state) {
            List<ARTCKaraokeRoomEngineCallback> roomEngineCallbackList = getRoomEngineCallbackList();
            if (roomEngineCallbackList.size() > 0) {
                for (ARTCKaraokeRoomEngineCallback roomEngineCallback : roomEngineCallbackList) {
                    roomEngineCallback.onMusicPrepareStateUpdate(state);
                }
            }
        }

        @Override
        public void onMusicPlayStateUpdate(KTVMusicPlayState state) {
            List<ARTCKaraokeRoomEngineCallback> roomEngineCallbackList = getRoomEngineCallbackList();
            if (roomEngineCallbackList.size() > 0) {
                for (ARTCKaraokeRoomEngineCallback roomEngineCallback : roomEngineCallbackList) {
                    roomEngineCallback.onMusicPlayStateUpdate(state);
                }
            }
        }

        @Override
        public void onMusicPlayProgressUpdate(long millisecond) {
            List<ARTCKaraokeRoomEngineCallback> roomEngineCallbackList = getRoomEngineCallbackList();
            if (roomEngineCallbackList.size() > 0) {
                for (ARTCKaraokeRoomEngineCallback roomEngineCallback : roomEngineCallbackList) {
                    roomEngineCallback.onMusicPlayProgressUpdate(millisecond);
                }
            }
        }

        @Override
        public void onSingerRoleUpdate(KTVSingerRole oldRole, KTVSingerRole newRole) {
            List<ARTCKaraokeRoomEngineCallback> roomEngineCallbackList = getRoomEngineCallbackList();
            if (roomEngineCallbackList.size() > 0) {
                for (ARTCKaraokeRoomEngineCallback roomEngineCallback : roomEngineCallbackList) {
                    roomEngineCallback.onSingerRoleUpdate(oldRole, newRole);
                }
            }
        }
    };

    public ARTCKaraokeRoomControllerImpl(final Looper looper) {
        mLooper = looper;
        mRoomDirector = new ARTCKaraokeRoomDirector(this, looper);
        addObserver(mRoomDirector);
        addRoomEngineCallback(mRoomDirector);
    }

    @Override
    public ARTCKaraokeRoomEngine getKTVEngine() {
        if (mKTVEngine == null && mRoomState != RoomState.UN_INIT) {
            AliRtcEngine rtcEngine = mRTCService.getRTCEngine();
            rtcEngine.subscribeAllRemoteDualAudioStreams(true);
            mKTVEngine = new ARTCKaraokeRoomEngineImpl(rtcEngine);
            mKTVEngine.setCallback(mRoomEngineCallbackWrapper);
        }
        return mKTVEngine;
    }

    @Override
    public void init(Context context, ClientMode mode, String appId, UserInfo userInfo, IMNewToken token, ActionCallback callback) {
        super.init(context, mode, appId, userInfo, token, callback);
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                mPlayingListService = createMusicPlayingListService(Singleton.getInstance(Server.class).getAuthorizeToken());
                File musicCacheFile = context.getExternalFilesDir("aui_ktv_music_cache");
                String musicCachePath = null != musicCacheFile ? musicCacheFile.getAbsolutePath() : "";
                mMusicLibrary = createMusicLibrary(musicCachePath);
            }
        });
    }

    @Override
    public void fetchMusicPlayingList(ARTCKaraokeRoomMusicPlayingListServiceCallback callback) {
        if (null != mPlayingListService) {
            mPlayingListService.fetchMusicPlayingList(mRoomInfo, callback);
        }
    }

    @Override
    public boolean checkCanAddMusic() {
        return mRoomState == RoomState.IN_MIC || isAnchor();
    }

    @Override
    public boolean checkCanRemoveMusic(@NonNull KTVPlayingMusicInfo ktvPlayingMusicInfo) {
        return isAnchor() || isMusicSelectedByCurrentUser(ktvPlayingMusicInfo);
    }

    @Override
    public void addMusic(KTVMusicInfo musicInfo) {
        // 触发音乐下载，监听下载完成之后加到播放列表
        ARTCKaraokeRoomActionCallback addPlayListActionCallback = new ARTCKaraokeRoomActionCallback() {
            @Override
            public void onSuccess() {
                logInfo("PlayingListService addMusic with [songId: " + musicInfo.songID + "] success");
                // 通知各方刷新已点歌曲列表
                sendMusicPlayingListUpdateCommand(PLAYING_LIST_UPDATE_REASON_ADD_MUSIC.toInt(), musicInfo.songID, mCurrentUser.userId, null, null);
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                logInfo("PlayingListService addMusic with [songId: " + musicInfo.songID + "]", errorCode, errorMsg);
            }
        };
        ARTCKaraokeRoomActionCallback downloadMusicActionCallback = new ARTCKaraokeRoomActionCallback() {
            @Override
            public void onSuccess() {
                addMusicToPlayingListService(musicInfo, addPlayListActionCallback);
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {

            }
        };
        if (mMusicLibrary.isMusicDownloaded(musicInfo.songID)) {
            downloadMusicActionCallback.onSuccess();
        } else {
            downloadMusic(musicInfo.songID, downloadMusicActionCallback);
        }
    }

    @Override
    public void removeMusic(@NonNull KTVPlayingMusicInfo ktvPlayingMusicInfo) {
        if (null != mPlayingListService) {
            List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList = new ArrayList<>();
            ktvPlayingMusicInfoList.add(ktvPlayingMusicInfo);
            removeMusic(ktvPlayingMusicInfoList);
        }
    }

    @Override
    public void removeMusic(@NonNull List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList) {
        removeMusic(ktvPlayingMusicInfoList, true, null);
    }

    @Override
    public boolean checkCanPinMusic(@NonNull KTVPlayingMusicInfo ktvPlayingMusicInfo) {
        return isAnchor() || isMusicSelectedByCurrentUser(ktvPlayingMusicInfo);
    }

    @Override
    public void pinMusic(@NonNull KTVPlayingMusicInfo ktvPlayingMusicInfo) {
        if (null != mPlayingListService) {
            SeatInfo seatInfo = composeCurrentSeatInfo();
            logInfo("PlayingListService pinMusic with [songId: " + ktvPlayingMusicInfo.songID + "]");
            mPlayingListService.pinMusic(mRoomInfo, ktvPlayingMusicInfo.songID, seatInfo, seatInfo, new ARTCKaraokeRoomActionCallback() {
                @Override
                public void onSuccess() {
                    logInfo("PlayingListService pinMusic with [songId: " + ktvPlayingMusicInfo.songID + "] success");
                    sendMusicPlayingListUpdateCommand(PLAYING_LIST_UPDATE_REASON_PIN_MUSIC.toInt(), ktvPlayingMusicInfo.songID, mCurrentUser.userId, null, null);
                }

                @Override
                public void onFail(int errorCode, String errorMsg) {
                    logInfo("PlayingListService pinMusic with [songId: " + ktvPlayingMusicInfo.songID + "] fail", errorCode, errorMsg);
                }
            });
        }
    }

    @Override
    public KTVPlayingMusicInfo getCurrentPlayingMusicInfo() {
        KTVPlayingMusicInfo ktvPlayingMusicInfo = null;
        if (null != mPlayingListService) {
            ktvPlayingMusicInfo = mPlayingListService.getCurrentPlayingMusicInfo();
        }
        if (null == ktvPlayingMusicInfo) {
            logInfo("getCurrentPlayingMusicInfo", -1, "ktvPlayingMusicInfo is null");
        }
        return ktvPlayingMusicInfo;
    }

    @Override
    public List<KTVPlayingMusicInfo> getLocalPlayMusicInfoList() {
        return mPlayingListService.getCachedMusicPlayingList();
    }

    @Override
    public void fetchMusicChartList(ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicChartCallback callback) {
        if (null != mMusicLibrary) {
            mMusicLibrary.fetchMusicChartList(callback);
        }
    }

    @Override
    public void fetchMusicList(String chartId, int pageIndex, int pageSize, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback callback) {
        if (null != mMusicLibrary) {
            mMusicLibrary.fetchMusicList(chartId, pageIndex, pageSize, callback);
        }
    }

    @Override
    public void fetchMusicInfo(String songId, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoCallback callback) {
        if (null != mMusicLibrary) {
            mMusicLibrary.fetchMusicInfo(songId, callback);
        }
    }

    @Override
    public void fetchMusicLyric(String songId, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicLyricCallback callback) {
        if (null != mMusicLibrary) {
            mMusicLibrary.fetchMusicLyric(songId, callback);
        }
    }

    @Override
    public void fetchMusicPitch(String songId, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicPitchCallback callback) {
        if (null != mMusicLibrary) {
            mMusicLibrary.fetchMusicPitch(songId, callback);
        }
    }

    @Override
    public void searchMusic(String keyword, int pageIndex, int pageSize, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback callback) {
        if (null != mMusicLibrary) {
            mMusicLibrary.searchMusic(keyword, pageIndex, pageSize, callback);
        }
    }

    @Override
    public void downloadMusic(String songId, ARTCKaraokeRoomActionCallback actionCallback) {
        addDownloadCallback(new ARTCKaraokeRoomMusicLibraryCallback() {
            @Override
            public void onMusicDownloadCompleted(KTVMusicInfo musicInfo) {
                removeDownloadCallback(this);
                mMusicLibrary.fetchMusicPitch(musicInfo.songID, new ARTCKaraokeRoomMusicPitchCallback() {
                    @Override
                    public void onMusicPitchCallback(String pitch) {
                        mMusicLibrary.fetchMusicLyric(musicInfo.songID, new ARTCKaraokeRoomMusicLyricCallback() {
                            @Override
                            public void onMusicLyricCallback(String lyric, KTVMusicInfo.KTVLyricType lyricType) {
                                actionCallback.onSuccess();
                            }

                            @Override
                            public void onFail(int errorCode, String errorMsg) {
                                logInfo("fetchMusicLyric", errorCode, errorMsg);
                                actionCallback.onFail(errorCode, errorMsg);
                            }
                        });
                    }
                    @Override
                    public void onFail(int errorCode, String errorMsg) {
                        logInfo("fetchMusicPitch", errorCode, errorMsg);
                        actionCallback.onFail(errorCode, errorMsg);
                    }
                });
            }
            @Override
            public void onFail(int errorCode, String errorMsg) {
                logInfo("downloadMusic", errorCode, errorMsg);
                removeDownloadCallback(this);
                actionCallback.onFail(errorCode, errorMsg);
            }
        });

        if (null != mMusicLibrary) {
            mMusicLibrary.fetchMusicInfo(songId, new ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoCallback() {
                @Override
                public void onMusicInfoCallback(KTVMusicInfo musicInfo) {
                    mMusicLibrary.downloadMusic(songId);
                }

                @Override
                public void onFail(int errorCode, String errorMsg) {
                    logInfo("fetchMusicInfo", errorCode, errorMsg);
                    actionCallback.onFail(errorCode, errorMsg);
                }
            });
        }
    }

    @Override
    public void addDownloadCallback(ARTCKaraokeRoomMusicLibraryCallback callback) {
        synchronized (this) {
            if (!mMusicLibraryCallbackList.contains(callback)) {
                mMusicLibraryCallbackList.add(callback);
            }
        }
    }

    @Override
    public void removeDownloadCallback(@NonNull ARTCKaraokeRoomMusicLibraryCallback callback) {
        synchronized (this) {
            int index = mMusicLibraryCallbackList.indexOf(callback);
            if (index >= 0) {
                mMusicLibraryCallbackList.remove(index);
            }
        }
    }

    @Override
    public void addRoomEngineCallback(@Nullable ARTCKaraokeRoomEngineCallback callback) {
        synchronized (this) {
            if (!mRoomEngineCallbackList.contains(callback)) {
                mRoomEngineCallbackList.add(callback);
            }
        }
    }

    @Override
    public void removeRoomEngineCallback(@Nullable ARTCKaraokeRoomEngineCallback callback) {
        synchronized (this) {
            int index = mRoomEngineCallbackList.indexOf(callback);
            if (index >= 0) {
                mRoomEngineCallbackList.remove(index);
            }
        }
    }

    @Override
    public boolean setSingerRole(KTVSingerRole newRole) {
        if (null != getKTVEngine()) {
            return getKTVEngine().setSingerRole(newRole);
        }
        return false;
    }

    @Override
    public void loadMusic(String uri) {
        if (null != getKTVEngine()) {
            getKTVEngine().loadMusic(uri);
        }
    }

    @Override
    public void loadCopyrightMusic(@NonNull String songId) {
        if (null != getKTVEngine()) {
            getKTVEngine().loadCopyrightMusic(songId);
        }
    }

    @Override
    public void loadMusic(KTVMusicConfig config) {
        if (null != getKTVEngine()) {
            getKTVEngine().loadMusic(config);
        }
    }

    @Override
    public void playMusic() {
        if (null != getKTVEngine()) {
            getKTVEngine().playMusic();
        }
    }

    @Override
    public void stopMusic() {
        if (null != getKTVEngine()) {
            getKTVEngine().stopMusic();
        }
    }

    @Override
    public void resumeMusic() {
        if (null != getKTVEngine()) {
            getKTVEngine().resumeMusic();
        }
    }

    @Override
    public void pauseMusic() {
        if (null != getKTVEngine()) {
            getKTVEngine().pauseMusic();
        }
    }

    @Override
    public void seekMusicTo(long millisecond) {
        if (null != getKTVEngine()) {
            getKTVEngine().seekMusicTo(millisecond);
        }
    }

    @Override
    public long getMusicTotalDuration() {
        if (null != getKTVEngine()) {
            return getKTVEngine().getMusicTotalDuration();
        }
        return 0l;
    }

    @Override
    public long getMusicCurrentProgress() {
        if (null != getKTVEngine()) {
            return getKTVEngine().getMusicCurrentProgress();
        }
        return 0l;
    }

    @Override
    public void setMusicAccompanimentMode(boolean original) {
        if (null != getKTVEngine()) {
            getKTVEngine().setMusicAccompanimentMode(original);
        }
    }

    @Override
    public void setMusicVolume(int volume) {
        if (null != getKTVEngine()) {
            getKTVEngine().setMusicVolume(volume);
        }
    }

    @Override
    public boolean canChangeMusicAccompanimentMode() {
        // 检查当前用户是否当前播放歌曲的主唱人或者伴唱人
        KTVPlayingMusicInfo ktvPlayingMusicInfo = getCurrentPlayingMusicInfo();
        return isMusicSelectedByCurrentUser(ktvPlayingMusicInfo) ||
                isMusicJoinedByCurrentUser(ktvPlayingMusicInfo);
    }

    @Override
    public boolean canJoinSinging() {
        // 麦上且不是主唱
        return mRoomState == RoomState.IN_MIC && !isLeadSinger();
    }

    @Override
    public boolean isLeadSinger() {
        return isMusicSelectedByCurrentUser(getCurrentPlayingMusicInfo());
    }

    @Override
    public boolean isJoinSinger() {
        return isMusicJoinedByCurrentUser(getCurrentPlayingMusicInfo());
    }

    @Override
    public void joinSinging(ARTCKaraokeRoomActionCallback callback) {
        if (canJoinSinging()) {
            KTVPlayingMusicInfo ktvPlayingMusicInfo = getCurrentPlayingMusicInfo();
            ARTCKaraokeRoomActionCallback actionCallbackWrapper = new ARTCKaraokeRoomActionCallback() {
                @Override
                public void onSuccess() {

                    SeatInfo seatInfo = composeCurrentSeatInfo();
                    String userId = null != seatInfo ? seatInfo.userId : "";
                    logInfo("PlayingListService joinSinging with [songId: " + ktvPlayingMusicInfo.songID + ", userId: " + userId + "]");
                    mPlayingListService.joinSinging(mRoomInfo, ktvPlayingMusicInfo, seatInfo, new ARTCKaraokeRoomActionCallback() {
                        @Override
                        public void onSuccess() {
                            logInfo("PlayingListService joinSinging with [songId: " + ktvPlayingMusicInfo.songID + ", userId: " + userId + "] success");
                            sendMusicPlayingListUpdateCommand(PLAYING_LIST_UPDATE_REASON_JOIN_SINGING.toInt(), ktvPlayingMusicInfo.songID, userId, null, null);
                            if (null != callback) {
                                callback.onSuccess();
                            }
                        }

                        @Override
                        public void onFail(int errorCode, String errorMsg) {
                            logInfo("PlayingListService joinSinging with [songId: " + ktvPlayingMusicInfo.songID + ", userId: " + userId + "] fail", errorCode, errorMsg);
                            if (null != callback) {
                                callback.onFail(errorCode, errorMsg);
                            }
                        }
                    });
                }

                @Override
                public void onFail(int errorCode, String errorMsg) {
                    logInfo("PlayingListService joinSinging with [songId: " + ktvPlayingMusicInfo.songID + "]", errorCode, errorMsg);
                    if (null != callback) {
                        callback.onFail(errorCode, errorMsg);
                    }
                }
            };

            if (mMusicLibrary.isMusicDownloaded(ktvPlayingMusicInfo.songID)) {
                actionCallbackWrapper.onSuccess();
            } else {
                downloadMusic(ktvPlayingMusicInfo.songID, actionCallbackWrapper);
            }
        }
    }

    @Override
    public void leaveSinging(ARTCKaraokeRoomActionCallback callback) {
        if (isJoinSinger()) {
            stopMusic();
            KTVPlayingMusicInfo ktvPlayingMusicInfo = getCurrentPlayingMusicInfo();
            SeatInfo seatInfo = composeCurrentSeatInfo();
            logInfo("PlayingListService leaveSinging with [songId: " + ktvPlayingMusicInfo.songID + ", userId: " + seatInfo.userId + "]");
            mPlayingListService.leaveSinging(mRoomInfo, ktvPlayingMusicInfo, seatInfo, new ARTCKaraokeRoomActionCallback() {
                @Override
                public void onSuccess() {
                    logInfo("PlayingListService leaveSinging with [songId: " + ktvPlayingMusicInfo.songID + ", userId: " + seatInfo.userId + "] success");
                    sendMusicPlayingListUpdateCommand(PLAYING_LIST_UPDATE_REASON_LEAVE_JOIN_SINGING.toInt(), ktvPlayingMusicInfo.songID, seatInfo.userId, null, null);
                    if (null != callback) {
                        callback.onSuccess();
                    }
                }

                @Override
                public void onFail(int errorCode, String errorMsg) {
                    logInfo("PlayingListService leaveSinging with [songId: " + ktvPlayingMusicInfo.songID + ", userId: " + seatInfo.userId + "] fail", errorCode, errorMsg);
                    if (null != callback) {
                        callback.onFail(errorCode, errorMsg);
                    }
                }
            });
        }
    }

    @Override
    public void skipMusic() {
        syncPlayNextSongActionToServerAndAllUser(true);
    }

    @Override
    public boolean checkCanSkipMusic(KTVPlayingMusicInfo ktvPlayingMusicInfo) {
        return isAnchor() || isMusicSelectedByCurrentUser(ktvPlayingMusicInfo);
    }

    @MainThread
    @Override
    public void updateScore(int score) {
        mScore = score;
    }

    @Override
    public void requestRemoteKtvState() {
        if (!isAnchor()) {
            sendSyncAllStateRequestCommand(mRoomInfo.creator.userId, null);
        }
    }

    @Override
    public void onDataChannelMessage(final String uid, final AliRtcEngine.AliRtcDataChannelMsg msg) {
        super.onDataChannelMessage(uid, msg);
        if (getKTVEngine() != null) {
            mKTVEngine.onCustomDataMsgReceived(msg);
        }
    }

    @Override
    public void setMusicPlayingListServiceFactory(ARTCKaraokeRoomMusicPlayingListService.ARTCKaraokeRoomMusicPlayingListServiceFactory musicPlayingListServiceFactory) {
        mPlayingListServiceFactory = musicPlayingListServiceFactory;
    }

    @Override
    public void setMusicLibraryFactory(ARTCKaraokeRoomMusicLibrary.ARTCKaraokeRoomMusicLibraryFactory musicLibraryFactory) {
        mMusicLibraryFactory = musicLibraryFactory;
    }

    @Override
    public void onMessageReceived(AUIMessageModel<String> message) {
        super.onMessageReceived(message);
        if(!TextUtils.isEmpty(message.data)){
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(message.data);
            } catch (JSONException e) {
                e.printStackTrace();
                logInfo(e.getMessage());
                return;
            }
            switch (message.type){
                case MSG_TYPE_MUSIC_PLAYING_LIST_UPDATED:
                    onMusicPlayingListUpdated(message.senderInfo.userId, jsonObject);
                    break;
                case MSG_TYPE_ROOM_STATE_UPDATED:
                    onRoomStateChanged(message.senderInfo.userId, jsonObject);
                    break;
                case MSG_TYPE_PLAY_STATE_UPDATED:
                    onPlayStateChanged(message.senderInfo.userId, jsonObject);
                    break;
                case MSG_TYPE_ALL_STATE_SYNC_REQUEST:
                    onAllStateSyncRequest(message.senderInfo.userId, jsonObject);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void release() {
        super.release();
        stopMusic();
        mMusicLibrary.destroy();
        mPlayingListService.destroy();
    }

    private void fetchJoinerList(ARTCKaraokeRoomMusicPlayingListServiceCallback callback) {
        boolean requesting = false;
        if (null != mPlayingListService) {
            KTVPlayingMusicInfo ktvPlayingMusicInfo = getCurrentPlayingMusicInfo();
            if (null != ktvPlayingMusicInfo) {
                logInfo("PlayingListService fetchJoinerList with [songId: " + ktvPlayingMusicInfo.songID + ", roomId: " + mRoomInfo.roomId + "]");
                mPlayingListService.fetchJoinerList(mRoomInfo, ktvPlayingMusicInfo, callback);
                requesting = true;
            }
        }
        if (!requesting) {
            callback.onFail(-1, "not requesting");
        }
    }

    private void onMusicPlayingListUpdated(String sender, JSONObject data) {
        List<KTVPlayingMusicInfo> playingMusicInfoListBeforeFetch = mPlayingListService.getCachedMusicPlayingList();
        Runnable runAfterDataCallback = new Runnable() {
            @Override
            public void run() {
                KTVMusicPlayListUpdateReason updateReason = KTVMusicPlayListUpdateReason.fromInt(data.optInt(MSG_KEY_REASON, PLAYING_LIST_UPDATE_REASON_OTHER.toInt()));
                String songIdArrStr = data.optString(MSG_KEY_SONG_ID);
                String[] songIdArray = decomposeKTVPlayingMusicInfoList(songIdArrStr);
                String actionUserId = data.optString(MSG_KEY_USER_ID);
                UserInfo operateUserInfo = mMicUsers.get(actionUserId);
                if (null == operateUserInfo && TextUtils.equals(actionUserId, mRoomInfo.creator.userId)) {
                    operateUserInfo = mRoomInfo.creator;
                }
                if (null == operateUserInfo && isJoinMic() && TextUtils.equals(actionUserId, mCurrentUser.userId)) {
                    operateUserInfo = mCurrentUser;
                }

                List<KTVPlayingMusicInfo> kTVPlayingMusicInfoListUpdated = new ArrayList<>();
                List<KTVPlayingMusicInfo> playingMusicInfoList = mPlayingListService.getCachedMusicPlayingList();

                for (String songId : songIdArray) {
                    KTVPlayingMusicInfo ktvPlayingMusicInfoUpdated = null;
                    for (KTVPlayingMusicInfo ktvPlayingMusicInfo : playingMusicInfoList) {
                        if (TextUtils.equals(ktvPlayingMusicInfo.songID, songId)) {
                            ktvPlayingMusicInfoUpdated = ktvPlayingMusicInfo;
                            break;
                        }
                    }
                    if (null == ktvPlayingMusicInfoUpdated && null != playingMusicInfoListBeforeFetch) {
                        for (KTVPlayingMusicInfo ktvPlayingMusicInfo : playingMusicInfoListBeforeFetch) {
                            if (TextUtils.equals(ktvPlayingMusicInfo.songID, songId)) {
                                ktvPlayingMusicInfoUpdated = ktvPlayingMusicInfo;
                                break;
                            }
                        }
                    }
                    if (null != ktvPlayingMusicInfoUpdated) {
                        kTVPlayingMusicInfoListUpdated.add(ktvPlayingMusicInfoUpdated);
                    }
                }

                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    ARTCKaraokeRoomControllerDelegate roomControllerDelegate = (ARTCKaraokeRoomControllerDelegate) c;
                    roomControllerDelegate.onMusicPlayingUpdated(updateReason, operateUserInfo, kTVPlayingMusicInfoListUpdated, playingMusicInfoList);
                }
            }
        };

        fetchMusicPlayingList(new ARTCKaraokeRoomMusicPlayingListServiceCallback() {
            @Override
            public void onMusicPlayingListCallback(List<KTVPlayingMusicInfo> playingMusicInfoList) {
                fetchJoinerList(new ARTCKaraokeRoomMusicPlayingListServiceCallback() {
                    @Override
                    public void onMusicPlayingListCallback(List<KTVPlayingMusicInfo> playingMusicInfoList) {
                    }

                    @Override
                    public void onMusicPlayingJoinerListCallback(KTVPlayingMusicInfo ktvPlayingMusicInfo) {
                        CommonUtil.runOnUI(runAfterDataCallback);
                    }

                    @Override
                    public void onFail(int errorCode, String errorMsg) {
                        logInfo("fetchJoinerList", errorCode, errorMsg);
                        CommonUtil.runOnUI(runAfterDataCallback);
                    }
                });
            }

            @Override
            public void onMusicPlayingJoinerListCallback(KTVPlayingMusicInfo ktvPlayingMusicInfo) {

            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                logInfo("fetchMusicPlayingList", errorCode, errorMsg);
                CommonUtil.runOnUI(runAfterDataCallback);
            }
        });

    }

    private void onRoomStateChanged(String sender, JSONObject data) {
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                KTVRoomState oldState = KTVRoomState.fromInt(data.optInt(MSG_KEY_OLD_ROOM_STATE, KTVRoomState.Init.toInt()));
                KTVRoomState newState = KTVRoomState.fromInt(data.optInt(MSG_KEY_NEW_ROOM_STATE, KTVRoomState.Init.toInt()));
                String songId = data.optString(MSG_KEY_SONG_ID);
                for (ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    ARTCKaraokeRoomControllerDelegate roomControllerDelegate = (ARTCKaraokeRoomControllerDelegate) c;
                    roomControllerDelegate.onRoomStateChanged(oldState, newState, songId);
                }
            }
        });
    }

    private void onPlayStateChanged(String sender, JSONObject data) {
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                KTVMusicPlayState oldState = KTVMusicPlayState.fromInt(data.optInt(MSG_KEY_OLD_PLAY_STATE, KTVMusicPlayState.Idle.toInt()));
                KTVMusicPlayState newState = KTVMusicPlayState.fromInt(data.optInt(MSG_KEY_NEW_PLAY_STATE, KTVMusicPlayState.Idle.toInt()));
                int singScore = data.optInt(MSG_KEY_SING_SCORE, 0);
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    ARTCKaraokeRoomControllerDelegate roomControllerDelegate = (ARTCKaraokeRoomControllerDelegate) c;
                    roomControllerDelegate.onSingingPlayStateChanged(oldState, newState, singScore);
                }
            }
        });
    }

    private void onAllStateSyncRequest(String sender, JSONObject data) {
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    ARTCKaraokeRoomControllerDelegate roomControllerDelegate = (ARTCKaraokeRoomControllerDelegate) c;
                    roomControllerDelegate.onKTVStateSyncRequest(sender);
                }
            }
        });
    }

    private ARTCKaraokeRoomMusicLibrary createMusicLibrary(String cachePath) {
        ARTCKaraokeRoomMusicLibrary instance = null;
        if (mMusicLibraryFactory != null) {
            instance = mMusicLibraryFactory.createInstance(mLooper, cachePath);
        } else {
            instance = new ARTCKaraokeRoomMusicLibraryImpl(mLooper, cachePath);
        }
        instance.setDownloadCallback(mMusicLibraryCallbackWrapper);
        return instance;
    }

    private ARTCKaraokeRoomMusicPlayingListService createMusicPlayingListService(String authorization) {
        if (mPlayingListServiceFactory != null) {
            return mPlayingListServiceFactory.createInstance(mLooper);
        } else {
            return new ARTCKaraokeRoomMusicPlayingListServiceImpl(authorization, mLooper);
        }
    }

    private void addMusicToPlayingListService(KTVMusicInfo ktvMusicInfo, ARTCKaraokeRoomActionCallback callback) {
        if (null != ktvMusicInfo && null != mPlayingListService) {
            // 成功之后通过信令通知各方刷新已点歌曲列表
            mPlayingListService.addMusic(mRoomInfo, ktvMusicInfo, composeCurrentSeatInfo(), callback);
        }
    }

    private SeatInfo composeCurrentSeatInfo() {
        SeatInfo seatInfo = null;
        if (null != mCurrentUser) {
            seatInfo = new SeatInfo();
            seatInfo.roomId = mRoomInfo.roomId;
            seatInfo.userId = mCurrentUser.userId;
            seatInfo.userName = mCurrentUser.userName;
            seatInfo.userAvatar = mCurrentUser.avatarUrl;
            seatInfo.seatIndex = mCurrentUser.micPosition;
        }
        return seatInfo;
    }

    boolean isMusicSelectedByCurrentUser(KTVPlayingMusicInfo ktvPlayingMusicInfo) {
        if (null != ktvPlayingMusicInfo && null != ktvPlayingMusicInfo.seatInfo && null != mCurrentUser) {
            return TextUtils.equals(mCurrentUser.userId, ktvPlayingMusicInfo.seatInfo.userId);
        }
        return false;
    }

    boolean isMusicJoinedByCurrentUser(KTVPlayingMusicInfo ktvPlayingMusicInfo) {
        if (null != ktvPlayingMusicInfo && null != ktvPlayingMusicInfo.joinSingUserIdList && null != mCurrentUser) {
            if (!ktvPlayingMusicInfo.joinSingUserIdList.isEmpty()) {
                for (String joinUserId: ktvPlayingMusicInfo.joinSingUserIdList) {
                    if (TextUtils.equals(mCurrentUser.userId, joinUserId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    void logInfo(String logMsg) {
        AliyunLog.i(TAG, logMsg);
        debugInfo(logMsg);
    }

    void logInfo(String action, int errorCode, String errorMsg) {
        String logMsg = action + " [errorCode: " + errorCode + ", errorMsg: " + errorMsg + "]";
        logInfo(logMsg);
    }

    void sendMusicPlayingListUpdateCommand(int reason, String songId, String actionUserId, String uid, ActionCallback callback) {
        if (null == callback) {
            callback = new ActionCallback() {
                @Override
                public void onResult(int code, String msg, Map<String, Object> params) {
                    logInfo("sendMusicPlayingListUpdateCommand", code, msg);
                }
            };
        }
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(MSG_KEY_REASON, reason);
        paramsMap.put(MSG_KEY_SONG_ID, null != songId ? songId : "");
        paramsMap.put(MSG_KEY_USER_ID, null != actionUserId ? actionUserId : "");
        sendKtvCustomMessage(uid, MSG_TYPE_MUSIC_PLAYING_LIST_UPDATED, paramsMap, callback);
    }

    void sendRoomStateUpdatedCommand(String uid, KTVRoomState oldState, KTVRoomState newState, String songId, ActionCallback callback) {
        // 只有主持人可以
        if (isAnchor()) {
            if (null == callback) {
                callback = new ActionCallback() {
                    @Override
                    public void onResult(int code, String msg, Map<String, Object> params) {
                        logInfo("sendRoomStateUpdatedCommand", code, msg);
                    }
                };
            }
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put(MSG_KEY_OLD_ROOM_STATE, oldState.toInt());
            paramsMap.put(MSG_KEY_NEW_ROOM_STATE, newState.toInt());
            paramsMap.put(MSG_KEY_SONG_ID, songId);
            sendKtvCustomMessage(uid, MSG_TYPE_ROOM_STATE_UPDATED, paramsMap, callback);
        }
    }

    void sendPlayStateUpdatedCommand(String uid, KTVMusicPlayState oldState, KTVMusicPlayState newState, long currentProgress, ActionCallback callback) {
        if (null == callback) {
            callback = new ActionCallback() {
                @Override
                public void onResult(int code, String msg, Map<String, Object> params) {
                    logInfo("sendPlayStateUpdatedCommand", code, msg);
                }
            };
        }
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(MSG_KEY_OLD_PLAY_STATE, oldState.toInt());
        paramsMap.put(MSG_KEY_NEW_PLAY_STATE, newState.toInt());
        if (newState == KTVMusicPlayState.Completed) {
            paramsMap.put(MSG_KEY_SING_SCORE, mScore);
        }
        sendKtvCustomMessage(uid, MSG_TYPE_PLAY_STATE_UPDATED, paramsMap, callback);
    }

    void sendSyncAllStateRequestCommand(String uid, ActionCallback callback) {
        if (null == callback) {
            callback = new ActionCallback() {
                @Override
                public void onResult(int code, String msg, Map<String, Object> params) {
                    logInfo("sendSyncAllStateRequestCommand", code, msg);
                }
            };
        }
        Map<String, Object> paramsMap = new HashMap<>();
        sendKtvCustomMessage(uid, MSG_TYPE_ALL_STATE_SYNC_REQUEST, paramsMap, callback);
    }


    private void sendKtvCustomMessage(String uid, int msgType, Map<String, Object> paramsMap, ActionCallback callback) {
        if (null != mRoomInfo) {
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put(MSG_KEY_TYPE, msgType);
                if (null != paramsMap && !paramsMap.isEmpty()) {
                    for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                        jsonObj.put(entry.getKey(), entry.getValue());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                CommonUtil.actionCallback(callback, -1, "" + e.getMessage(), null);
                return;
            }
            if (TextUtils.isEmpty(uid)) {
                IMService.getInstance().sendGroupMessage(mRoomInfo.roomId, msgType, jsonObj.toString(), true, true);
            } else {
                IMService.getInstance().sendMessage(mRoomInfo.roomId, uid, msgType, jsonObj.toString(), true, false);
            }
        }
        CommonUtil.actionCallback(callback, 0, "");
    }

    void notifyMusicWillPlayNext(KTVPlayingMusicInfo ktvPlayingMusicInfo, long countDownMillis) {
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    ARTCKaraokeRoomControllerDelegate roomControllerDelegate = (ARTCKaraokeRoomControllerDelegate) c;
                    roomControllerDelegate.onMusicWillPlayNext(ktvPlayingMusicInfo, countDownMillis);
                }
            }
        });
    }

    void syncPlayNextSongActionToServerAndAllUser(boolean fromSkip) {
        KTVPlayingMusicInfo ktvPlayingMusicInfo = mPlayingListService.getCurrentPlayingMusicInfo();
        if (isAnchor() || isMusicSelectedByCurrentUser(ktvPlayingMusicInfo)) {
            KTVPlayingMusicInfo nextKtvPlayingMusicInfo = mPlayingListService.getPlayingMusicInfo(1);
            int reason = fromSkip ? PLAYING_LIST_UPDATE_REASON_SKIP_MUSIC.toInt() : PLAYING_LIST_UPDATE_REASON_COMPLETE_MUSIC.toInt();
            String currentSongId = null != ktvPlayingMusicInfo ? ktvPlayingMusicInfo.songID : null;
            boolean asyncRequesting = syncPlayMusicActionToServer(true, nextKtvPlayingMusicInfo, new ARTCKaraokeRoomActionCallback() {
                @Override
                public void onSuccess() {
                    String nextSongId = null != nextKtvPlayingMusicInfo ? nextKtvPlayingMusicInfo.songID : "";
                    logInfo("PlayingListService playMusic when request next song with [songId: " + nextSongId + ", skip: " + fromSkip + "] success");
                    sendMusicPlayingListUpdateCommand(reason, currentSongId, mCurrentUser.userId, null, null);
                }

                @Override
                public void onFail(int errorCode, String errorMsg) {
                    String songId = null != nextKtvPlayingMusicInfo ? nextKtvPlayingMusicInfo.songID : "";
                    logInfo("PlayingListService playMusic when request next song with [songId: " + songId + ", skip: " + fromSkip + "] fail", errorCode, errorMsg);
                }
            });
            if (!asyncRequesting) {
                sendMusicPlayingListUpdateCommand(reason, currentSongId, mCurrentUser.userId, null, null);
            }
        }
    }

    boolean syncPlayMusicActionToServer(boolean mustPlayNext, KTVPlayingMusicInfo ktvPlayingMusicInfo, ARTCKaraokeRoomActionCallback callback) {
        if (mustPlayNext) {
            SeatInfo actionSeatInfo = composeCurrentSeatInfo();
            String songId = null != ktvPlayingMusicInfo ? ktvPlayingMusicInfo.songID : null;
            SeatInfo songSeatInfo = null != ktvPlayingMusicInfo ? ktvPlayingMusicInfo.seatInfo : null;
            logInfo("PlayingListService playMusic with [songId: " + songId + ", userId: " + (null != songSeatInfo ? songSeatInfo.userId : null) + "]");
            mPlayingListService.playMusic(mRoomInfo, ktvPlayingMusicInfo,
                    songSeatInfo, actionSeatInfo, callback);
            return true;
        } else if (ktvPlayingMusicInfo != null) {
            if (!ktvPlayingMusicInfo.isPlaying()) {
                SeatInfo seatInfo = composeCurrentSeatInfo();
                logInfo("PlayingListService playMusic with [songId: " + ktvPlayingMusicInfo.songID + ", userId: " + seatInfo.userId + "]");
                mPlayingListService.playMusic(mRoomInfo, ktvPlayingMusicInfo, ktvPlayingMusicInfo.seatInfo, seatInfo, callback);
                return true;
            } else {
                logInfo("PlayingListService playMusic with [songId: " + ktvPlayingMusicInfo.songID + "] do nothing because music is playing");
                return false;
            }
        }
        return false;
    }

    KTVPlayingMusicInfo getKTVPlayingMusicInfoIfMatch(String songId) {
        if (null != songId) {
            List<KTVPlayingMusicInfo> playingMusicInfoList = mPlayingListService.getCachedMusicPlayingList();
            if (null != playingMusicInfoList) {
                for (KTVPlayingMusicInfo ktvPlayingMusicInfo : playingMusicInfoList) {
                    if (null != ktvPlayingMusicInfo && TextUtils.equals(ktvPlayingMusicInfo.songID, songId)) {
                        return ktvPlayingMusicInfo;
                    }
                }
            }
        }
        return null;
    }

    void removeMusic(@NonNull List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList, boolean needSendMusicPlayingListUpdateCommand, ARTCKaraokeRoomActionCallback callback) {
        if (null != mPlayingListService && !ktvPlayingMusicInfoList.isEmpty()) {
            List<String> songIdList = new ArrayList<>();
            SeatInfo songSeatInfo = null;
            for (KTVPlayingMusicInfo ktvPlayingMusicInfo : ktvPlayingMusicInfoList) {
                songSeatInfo = ktvPlayingMusicInfo.seatInfo;
                songIdList.add(ktvPlayingMusicInfo.songID);
            }
            SeatInfo operateSeatInfo = composeCurrentSeatInfo();
            String songIdArrayStr = composeSongIdArray(ktvPlayingMusicInfoList);
            logInfo("PlayingListService removeMusic with [songId: " + songIdArrayStr + "]");
            ARTCKaraokeRoomActionCallback callbackWrapper = new ARTCKaraokeRoomActionCallback() {
                @Override
                public void onSuccess() {
                    logInfo("PlayingListService removeMusic with [songId: " + songIdArrayStr + "] success");
                    if (needSendMusicPlayingListUpdateCommand) {
                        sendMusicPlayingListUpdateCommand(PLAYING_LIST_UPDATE_REASON_REMOVE_MUSIC.toInt(), songIdArrayStr, operateSeatInfo.userId, null, null);
                    }
                    if (null != callback) {
                        callback.onSuccess();
                    }
                }

                @Override
                public void onFail(int errorCode, String errorMsg) {
                    logInfo("PlayingListService removeMusic with [songId: " + songIdArrayStr + "] fail", errorCode, errorMsg);
                    if (null != callback) {
                        callback.onFail(errorCode, errorMsg);
                    }
                }
            };
            mPlayingListService.removeMusic(mRoomInfo, songIdList, songSeatInfo, operateSeatInfo, callbackWrapper);
        }
    }

    boolean isUserInMic(String userId) {
        boolean ret = false;
        if (null != mMicUsers && mMicUsers.size() > 0) {
            Set<String> userIdSet = mMicUsers.keySet();
            for (String userIdInSet : userIdSet) {
                if (TextUtils.equals(userId, userIdInSet)) {
                    ret = true;
                    break;
                }
            }
        }
        if (!ret) {
            if(TextUtils.equals(userId, mRoomInfo.creator.userId)) {
                ret = true;
            }
        }
        return ret;
    }

    private String composeSongIdArray(List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList) {
        StringBuilder idBuilder = new StringBuilder();
        for (KTVPlayingMusicInfo ktvPlayingMusicInfo : ktvPlayingMusicInfoList) {
            if (idBuilder.length() > 0) {
                idBuilder.append(",");
            }
            idBuilder.append(ktvPlayingMusicInfo.songID);
        }
        return idBuilder.toString();
    }

    private String[] decomposeKTVPlayingMusicInfoList(String idArrayStr) {
        String[] idArray = null;

        if (!idArrayStr.isEmpty()) {
            idArray = idArrayStr.split(",");
        } else {
            idArray = new String[]{};
        }

        return idArray;
    }

//    void playNextSong(KTVPlayingMusicInfo ktvPlayingMusicInfo, long countTimeToBegin) {
//        mPlayingListService.playMusic(mRoomInfo, ktvPlayingMusicInfo, ktvPlayingMusicInfo.seatInfo, composeCurrentSeatInfo(), new ARTCKaraokeRoomActionCallback() {
//            @Override
//            public void onSuccess() {
//                callbackRunOnTargetThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
//                            ARTCKaraokeRoomControllerDelegate roomControllerDelegate = (ARTCKaraokeRoomControllerDelegate) c;
//                            roomControllerDelegate.onMusicWillPlayNext(ktvPlayingMusicInfo, countTimeToBegin);
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void onFail(int errorCode, String errorMsg) {
//                logError("playMusic", errorCode, errorMsg);
//            }
//        });
//    }
}
