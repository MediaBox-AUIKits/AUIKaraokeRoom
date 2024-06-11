package com.aliyun.auikits.karaoke.impl;

import static com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_JOIN_SINGING;
import static com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_LEAVE_JOIN_SINGING;
import static com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_OTHER;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.alivc.rtc.AliRtcEngine;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomActionCallback;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomControllerDelegate;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomEngineCallback;
import com.aliyun.auikits.karaoke.bean.KTVMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason;
import com.aliyun.auikits.karaoke.bean.KTVMusicPlayState;
import com.aliyun.auikits.karaoke.bean.KTVMusicPrepareState;
import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVRoomState;
import com.aliyun.auikits.karaoke.bean.KTVSingerRole;

import com.aliyun.auikits.voiceroom.bean.AccompanyPlayState;
import com.aliyun.auikits.voiceroom.bean.MicRequestResult;
import com.aliyun.auikits.voiceroom.bean.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class ARTCKaraokeRoomDirector implements ARTCKaraokeRoomControllerDelegate, ARTCKaraokeRoomEngineCallback {
    private static final long DEFAULT_SING_COUNT_DOWN_TIME = 3000;
    private static final long DEFAULT_SCORE_SHOW_TIME = 3000;
    private ARTCKaraokeRoomControllerImpl mRoomController;
    private Looper mTargetLooper;
    private Handler mTargetHandler;
    private KTVRoomStateMachine mRoomStateMachine = new KTVRoomStateMachine();
    private KTVPlayingMusicInfo mKtvPlayingMusicInfoInProcess;
    private KTVMusicPlayState mLastMusicPlayState = KTVMusicPlayState.Idle;
    private long mLastPlayProgress = 0l;

    public ARTCKaraokeRoomDirector(@NonNull ARTCKaraokeRoomControllerImpl roomController, @NonNull Looper looper) {
        mRoomController = roomController;
        mTargetLooper = looper;
        mTargetHandler = new Handler(mTargetLooper);
    }

    @Override
    public void onMusicDownloadCompleted(KTVMusicInfo ktvMusicInfo) {

    }

    @Override
    public void onMusicPlayingUpdated(KTVMusicPlayListUpdateReason reason, UserInfo operateUserInfo, List<KTVPlayingMusicInfo> kTVPlayingMusicInfoListUpdated, List<KTVPlayingMusicInfo> playingMusicInfoList) {
        printPlayMusicInfoList(playingMusicInfoList);
        // 主持人才可以
        if (mRoomController.isAnchor()) {
            KTVRoomState oldState = mRoomStateMachine.getKtvRoomState();
            KTVRoomState newState = mRoomStateMachine.judgeNewStateOnPlayingListUpdated(playingMusicInfoList, mKtvPlayingMusicInfoInProcess);
            boolean isFirstMusicInProgress = mRoomStateMachine.isFirstMusicInProgress(playingMusicInfoList, mKtvPlayingMusicInfoInProcess);
            KTVPlayingMusicInfo ktvPlayingMusicInfo = null != playingMusicInfoList && !playingMusicInfoList.isEmpty() ? playingMusicInfoList.get(0) : null;
            if (null != ktvPlayingMusicInfo && !isKTVPlayingMusicInfoSelectedUserInMic(ktvPlayingMusicInfo)) { //这首歌的主唱人已经下麦
                if (ktvPlayingMusicInfo.isPlaying()) {
                    mRoomController.skipMusic();
                } else {
                    mRoomController.removeMusic(ktvPlayingMusicInfo);
                }
            } else if (newState == KTVRoomState.Waiting) {
                // 能够进入等待状态，那么播放列表肯定有歌
                if (!isFirstMusicInProgress) {
                    mRoomController.logInfo("PlayingListService playMusic when playinglist updated [songId: " + ktvPlayingMusicInfo.songID + ", playStatus: " + ktvPlayingMusicInfo.status + "]");
                    boolean asyncRequesting = mRoomController.syncPlayMusicActionToServer(false, ktvPlayingMusicInfo, new ARTCKaraokeRoomActionCallback() {
                        @Override
                        public void onSuccess() {
                            mRoomController.logInfo("PlayingListService playMusic when playinglist updated [songId: " + ktvPlayingMusicInfo.songID + "] success");
                            mRoomController.sendRoomStateUpdatedCommand(null, oldState, newState, ktvPlayingMusicInfo.songID, null);
                        }

                        @Override
                        public void onFail(int errorCode, String errorMsg) {
                            mRoomController.logInfo("PlayingListService playMusic when playinglist updated [songId: " + ktvPlayingMusicInfo.songID + "] fail", errorCode, errorMsg);
                        }
                    });
                    if (!asyncRequesting) {
                        mRoomController.sendRoomStateUpdatedCommand(null, oldState, newState, ktvPlayingMusicInfo.songID, null);
                    }
                }
            } else if (newState == KTVRoomState.Init) {
                mRoomController.sendRoomStateUpdatedCommand(null, oldState, newState, "", null);
            }
        }

        onJoinedSingingListUpdated(reason, kTVPlayingMusicInfoListUpdated);
    }

    @Override
    public void onMusicWillPlayNext(KTVPlayingMusicInfo ktvPlayingMusicInfo, long notifyMilliseconds) {
        actionRunOnTargetThreadDelay(new Runnable() {
            @Override
            public void run() {
                // 避免在等待状态被切歌
                if (mKtvPlayingMusicInfoInProcess != null && ktvPlayingMusicInfo.songID.equals(mKtvPlayingMusicInfoInProcess.songID)) {
                    KTVRoomState oldState = mRoomStateMachine.getKtvRoomState();
                    KTVRoomState newState = mRoomStateMachine.judgeNewStateOnWaitingEnd();
                    mRoomController.sendRoomStateUpdatedCommand(null, oldState, newState, ktvPlayingMusicInfo.songID, null);
                }
            }
        }, notifyMilliseconds);
    }

    @Override
    public void onMusicPrepareStateUpdate(KTVMusicPrepareState state) {
        if (state == KTVMusicPrepareState.Prepared) {
            mRoomController.playMusic();
        } else if (state == KTVMusicPrepareState.Failed) {
            if (mRoomController.isLeadSinger()) {
                mRoomController.skipMusic();
            } else if (mRoomController.isJoinSinger()) {
                mRoomController.leaveSinging(null);
            }
        }
    }

    @Override
    public void onMusicPlayStateUpdate(KTVMusicPlayState state) {
        long progress = mRoomController.getMusicCurrentProgress();
        switch (state) {
            case Playing:
                break;
            case Paused:
                break;
            case Completed:
                break;
            case Idle:
            default:
                break;
        }
        if (mRoomController.isLeadSinger()) {
            mRoomController.sendPlayStateUpdatedCommand(null, mLastMusicPlayState, state, progress, null);
        }
        mLastMusicPlayState = state;
    }

    @Override
    public void onMusicPlayProgressUpdate(long millisecond) {
        mLastPlayProgress = millisecond;
    }

    @Override
    public void onSingerRoleUpdate(KTVSingerRole oldRole, KTVSingerRole newRole) {

    }

    @Override
    public void onJoin(String roomId, String uid) {

    }

    @Override
    public void onLeave() {

    }

    @Override
    public void onJoinedRoom(UserInfo user) {

    }

    @Override
    public void onKTVStateSyncRequest(String userId) {
        if (mRoomController.isAnchor()) {
            mRoomController.sendMusicPlayingListUpdateCommand(PLAYING_LIST_UPDATE_REASON_OTHER.toInt(), "", mRoomController.getCurrentUser().userId, userId, null);

            String songId = null != mKtvPlayingMusicInfoInProcess ? mKtvPlayingMusicInfoInProcess.songID : null;
            mRoomController.sendRoomStateUpdatedCommand(userId, mRoomStateMachine.getKtvRoomState(), mRoomStateMachine.getKtvRoomState(), songId, null);

            mRoomController.sendPlayStateUpdatedCommand(userId, mLastMusicPlayState, mLastMusicPlayState, mLastPlayProgress, null);
        }
    }

    @Override
    public void onLeavedRoom(UserInfo user) {

    }

    @Override
    public void onKickOutRoom() {

    }

    @Override
    public void onDismissRoom(String commander) {

    }

    @Override
    public void onJoinedMic(UserInfo user) {

    }

    @Override
    public void onLeavedMic(UserInfo user) {
        // 主持人 收到 非主持人 下麦通知
        if (mRoomController.isAnchor() && !mRoomController.isAnchor(user)) {
            KTVPlayingMusicInfo currentPlayingMusicInfo = mRoomController.getCurrentPlayingMusicInfo();
            String currentSongID = null;
            boolean isCurrentSongSelectByUser = false;
            if (null != currentPlayingMusicInfo) {
                currentSongID = currentPlayingMusicInfo.songID;
                // 不管第一首歌是否下麦用户主唱都不切第一首歌
//                isCurrentSongSelectByUser = TextUtils.equals(currentPlayingMusicInfo.seatInfo.userId, user.userId);
            }
            List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList = mRoomController.getLocalPlayMusicInfoList();
            if (null != ktvPlayingMusicInfoList) {
                List<KTVPlayingMusicInfo> ktvPlayingMusicInfoListToRemove = new ArrayList<>();
                for (KTVPlayingMusicInfo ktvPlayingMusicInfo : ktvPlayingMusicInfoList) {
                    if (TextUtils.equals(ktvPlayingMusicInfo.seatInfo.userId, user.userId) &&
                            !TextUtils.equals(currentSongID, ktvPlayingMusicInfo.songID)) {
                        ktvPlayingMusicInfoListToRemove.add(ktvPlayingMusicInfo);
                    }
                }
                // 删掉下麦用户的下播歌曲
                mRoomController.removeMusic(ktvPlayingMusicInfoListToRemove);
            }
        }

        boolean isCurrentUserJoinSinging = TextUtils.equals(mRoomController.getCurrentUser().userId, user.userId) &&
                mRoomController.isJoinSinger();
        if (isCurrentUserJoinSinging) {
            mRoomController.leaveSinging(null);
        }
    }

    @Override
    public void onResponseMic(MicRequestResult rs) {

    }

    @Override
    public void onReceivedTextMessage(UserInfo user, String text) {

    }

    @Override
    public void onMicUserMicrophoneChanged(UserInfo user, boolean open) {

    }


    @Override
    public void onMicUserSpeakStateChanged(UserInfo user) {

    }

    @Override
    public void onNetworkStateChanged(UserInfo user) {

    }

    @Override
    public void onError(int code, String msg) {

    }

    @Override
    public void onMute(boolean mute) {

    }

    @Override
    public void onExitGroup(String msg) {

    }

    @Override
    public void onRoomMicListChanged(List<UserInfo> micUsers) {

    }

    @Override
    public void onDataChannelMessage(String uid, AliRtcEngine.AliRtcDataChannelMsg msg) {

    }

    @Override
    public void onVoiceRoomDebugInfo(String msg) {

    }

    @Override
    public void onRoomStateChanged(KTVRoomState oldState, KTVRoomState newState, String songId) {
        if (mRoomStateMachine.getKtvRoomState() == newState) {
            return;
        }
        KTVPlayingMusicInfo ktvPlayingMusicInfo = mRoomController.getKTVPlayingMusicInfoIfMatch(songId);
        mKtvPlayingMusicInfoInProcess = ktvPlayingMusicInfo;
        mRoomStateMachine.setKtvRoomState(newState);
        if (newState == KTVRoomState.Waiting || newState == KTVRoomState.Init) {
            mRoomController.stopMusic();
        }
        if (newState == KTVRoomState.Waiting) {
            if (null != ktvPlayingMusicInfo) {
                mRoomController.notifyMusicWillPlayNext(ktvPlayingMusicInfo, DEFAULT_SING_COUNT_DOWN_TIME);
            } else {
                // 歌曲不命中第一首歌，异常情况
            }
        } else if (newState == KTVRoomState.Singing) {
            switchSingerRoleByKTVPlayingMusicInfo(ktvPlayingMusicInfo);
            if (mRoomController.isLeadSinger() || mRoomController.isJoinSinger()) {
                loadKtvMusicPlayingInfo(ktvPlayingMusicInfo);
                mRoomController.switchMicrophone(true);
            } else if (mRoomController.isJoinRoom()) {
                mRoomController.switchMicrophone(false);
            }
        }
    }

    @Override
    public void onSingingPlayStateChanged(KTVMusicPlayState oldState, KTVMusicPlayState newState, int score) {
        if (mRoomController.isAnchor()) {
            if (newState == KTVMusicPlayState.Completed) {
                actionRunOnTargetThreadDelay(new Runnable() {
                    @Override
                    public void run() {
                        mRoomController.syncPlayNextSongActionToServerAndAllUser(false);
                    }
                }, DEFAULT_SCORE_SHOW_TIME);
            }
        }
    }

    @Override
    public void onAccompanyStateChanged(AccompanyPlayState state) {

    }

    @Override
    public void onMemberCountChanged(int count) {

    }

    private void onJoinedSingingListUpdated(KTVMusicPlayListUpdateReason reason, List<KTVPlayingMusicInfo> kTVPlayingMusicInfoListUpdated) {
        if (reason == PLAYING_LIST_UPDATE_REASON_JOIN_SINGING || reason == PLAYING_LIST_UPDATE_REASON_LEAVE_JOIN_SINGING) {
            KTVPlayingMusicInfo ktvPlayingMusicInfo = null != kTVPlayingMusicInfoListUpdated && !kTVPlayingMusicInfoListUpdated.isEmpty() ?
                    kTVPlayingMusicInfoListUpdated.get(0) : null;
            printPlayMusicInfo(ktvPlayingMusicInfo);
            switchSingerRoleByKTVPlayingMusicInfo(ktvPlayingMusicInfo);
            if (mRoomController.isJoinSinger()) {
                loadKtvMusicPlayingInfo(ktvPlayingMusicInfo);
            }
        }
    }

    private void switchSingerRoleByKTVPlayingMusicInfo(KTVPlayingMusicInfo ktvPlayingMusicInfo) {
        if (mRoomController.isMusicSelectedByCurrentUser(ktvPlayingMusicInfo)) {
            mRoomController.setSingerRole(KTVSingerRole.LeadSinger);
        } else if (mRoomController.isMusicJoinedByCurrentUser(ktvPlayingMusicInfo)) {
            mRoomController.setSingerRole(KTVSingerRole.Choristers);
        } else {
            mRoomController.setSingerRole(KTVSingerRole.Audience);
        }
    }

    private void actionRunOnTargetThread(Runnable runnable){
        if(mTargetHandler.getLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            mTargetHandler.post(runnable);
        }
    }

    private void actionRunOnTargetThreadDelay(Runnable runnable, long delay){
        mTargetHandler.postDelayed(runnable, delay);
    }

    private void loadKtvMusicPlayingInfo(KTVPlayingMusicInfo ktvPlayingMusicInfo) {
        if (null == ktvPlayingMusicInfo) {
            return;
        }
        mRoomController.loadCopyrightMusic(ktvPlayingMusicInfo.songID);
    }

    private boolean isKTVPlayingMusicInfoSelectedUserInMic(KTVPlayingMusicInfo playingMusicInfo) {
        boolean ret = false;
        if (null != playingMusicInfo) {
            ret = mRoomController.isUserInMic(playingMusicInfo.seatInfo.userId);
        }
        return ret;
    }

    private void printPlayMusicInfo(KTVPlayingMusicInfo playingMusicInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        if (null != playingMusicInfo) {
            stringBuilder.append("{songId: ").append(playingMusicInfo.songID)
                    .append(", joinerList: [");
            for (String userId : playingMusicInfo.joinSingUserIdList) {
                stringBuilder.append(userId).append(", ");
            }

            stringBuilder.append("]").append("}");
        } else {
            stringBuilder.append("empty");
        }
        mRoomController.logInfo("PlayingListService fetchJoinerList when joinerList updated :" + stringBuilder.toString());
    }
    private void printPlayMusicInfoList(List<KTVPlayingMusicInfo> playingMusicInfoList) {
        StringBuilder stringBuilder = new StringBuilder();
        if (null != playingMusicInfoList && !playingMusicInfoList.isEmpty()) {
            stringBuilder.append("{");
            for (KTVPlayingMusicInfo ktvPlayingMusicInfo : playingMusicInfoList) {
                stringBuilder.append("[").append(ktvPlayingMusicInfo.songID).append(":").append(ktvPlayingMusicInfo.status).append("]");
            }
            stringBuilder.append("}");
        } else {
            stringBuilder.append("empty");
        }
        mRoomController.logInfo("PlayingListService fetchMusicPlayingList when playinglist updated :" + stringBuilder.toString());
    }

    private static class KTVRoomStateMachine {
        private KTVRoomState mKtvRoomState = KTVRoomState.Init;

        private boolean isFirstMusicInProgress(List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList,
                                               KTVPlayingMusicInfo ktvPlayingMusicInfoInProcess) {
            boolean ret = false;
            KTVPlayingMusicInfo firstPlayingMusicInfo = ktvPlayingMusicInfoList != null  && !ktvPlayingMusicInfoList.isEmpty() ?
                    ktvPlayingMusicInfoList.get(0) : null;
            // 演唱过程中被切歌了
            if (firstPlayingMusicInfo != null && ktvPlayingMusicInfoInProcess != null && firstPlayingMusicInfo.songID.equals(ktvPlayingMusicInfoInProcess.songID)) {
                ret = true;
            }
            return ret;
        }

        private KTVRoomState judgeNewStateOnPlayingListUpdated(List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList,
                                                               KTVPlayingMusicInfo ktvPlayingMusicInfoInProcess) {
            KTVRoomState nextRoomState = mKtvRoomState;
            boolean hasNextSong = ktvPlayingMusicInfoList != null && !ktvPlayingMusicInfoList.isEmpty();
            switch (mKtvRoomState) {
                case Init:
                case Waiting:
                    if (hasNextSong) {
                        nextRoomState = KTVRoomState.Waiting;
                    } else {
                        nextRoomState = KTVRoomState.Init;
                    }
                    break;
                case Singing: {
                    if (hasNextSong) {
                        if (!isFirstMusicInProgress(ktvPlayingMusicInfoList, ktvPlayingMusicInfoInProcess)) {
                            // 演唱过程中被切歌了
                            nextRoomState = KTVRoomState.Waiting;
                        }
                    } else {
                        nextRoomState = KTVRoomState.Init;
                    }
                    break;
                }
                default:
                    break;
            }
            return nextRoomState;
        }

        private KTVRoomState judgeNewStateOnWaitingEnd() {
            return KTVRoomState.Singing;
        }

        public void setKtvRoomState(KTVRoomState mKtvRoomState) {
            this.mKtvRoomState = mKtvRoomState;
        }

        public KTVRoomState getKtvRoomState() {
            return mKtvRoomState;
        }
    }
}
