package com.aliyun.auikits.karaoke.room.model.entity;

import com.aliyun.auikits.karaoke.ARTCKaraokeRoomControllerDelegate;
import com.aliyun.auikits.karaoke.bean.KTVMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason;
import com.aliyun.auikits.karaoke.bean.KTVMusicPlayState;
import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVRoomState;
import com.aliyun.auikits.voiceroom.bean.AccompanyPlayState;
import com.aliyun.auikits.voiceroom.bean.MicRequestResult;
import com.aliyun.auikits.voiceroom.bean.UserInfo;

import java.util.List;

public class ChatRoomCallback implements ARTCKaraokeRoomControllerDelegate {
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
    public void onLeavedRoom(UserInfo user) {

    }

    @Override
    public void onKickOutRoom() {

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
    public void onDismissRoom(String commander) {

    }

    @Override
    public void onJoinedMic(UserInfo user) {

    }

    @Override
    public void onLeavedMic(UserInfo user) {

    }

    @Override
    public void onExitGroup(String msg) {

    }

    @Override
    public void onRoomMicListChanged(List<UserInfo> micUsers) {

    }

    @Override
    public void onDataChannelMessage(String uid, com.alivc.rtc.AliRtcEngine.AliRtcDataChannelMsg msg) {

    }

    @Override
    public void onAccompanyStateChanged(AccompanyPlayState state) {

    }

    @Override
    public void onMemberCountChanged(int count) {

    }

    @Override
    public void onVoiceRoomDebugInfo(String msg) {

    }

    @Override
    public void onMusicDownloadCompleted(KTVMusicInfo ktvMusicInfo) {

    }

    @Override
    public void onMusicPlayingUpdated(KTVMusicPlayListUpdateReason reason, UserInfo operateUserInfo, List<KTVPlayingMusicInfo> kTVPlayingMusicInfoListUpdated, List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList) {

    }

    @Override
    public void onMusicWillPlayNext(KTVPlayingMusicInfo ktvPlayingMusicInfo, long notifyMilliseconds) {

    }

    @Override
    public void onRoomStateChanged(KTVRoomState oldState, KTVRoomState newState, String songId) {

    }

    @Override
    public void onSingingPlayStateChanged(KTVMusicPlayState oldState, KTVMusicPlayState newState, int score) {

    }

    @Override
    public void onKTVStateSyncRequest(String userId) {

    }
}
