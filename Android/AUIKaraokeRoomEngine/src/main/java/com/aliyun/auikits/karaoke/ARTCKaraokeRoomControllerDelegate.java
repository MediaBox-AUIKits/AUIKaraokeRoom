package com.aliyun.auikits.karaoke;

import com.aliyun.auikits.karaoke.bean.KTVMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason;
import com.aliyun.auikits.karaoke.bean.KTVMusicPlayState;
import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVRoomState;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngineDelegate;
import com.aliyun.auikits.voiceroom.bean.UserInfo;

import java.util.List;

public interface ARTCKaraokeRoomControllerDelegate extends ARTCVoiceRoomEngineDelegate {

    /**
     * 歌曲下载完成
     * @param ktvMusicInfo 下载完成的歌曲
     */
    void onMusicDownloadCompleted(KTVMusicInfo ktvMusicInfo);

    /**
     * 已点歌曲列表更新
     * @param reason 歌单更新的原因 {@link KTVMusicPlayListUpdateReason}
     * @param operateUserInfo 触发动作的用户，如切歌的用户
     * @param kTVPlayingMusicInfoListUpdated 触发歌单更新相关的歌曲列表，如添加的歌曲
     * @param ktvPlayingMusicInfoList 完整歌单
     */
    void onMusicPlayingUpdated(KTVMusicPlayListUpdateReason reason, UserInfo operateUserInfo, List<KTVPlayingMusicInfo> kTVPlayingMusicInfoListUpdated, List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList);

    /**
     * 准备播放下一首歌曲
     * @param ktvPlayingMusicInfo 准备播放的歌曲
     * @param notifyMilliseconds 等待的时长
     */
    void onMusicWillPlayNext(KTVPlayingMusicInfo ktvPlayingMusicInfo, long notifyMilliseconds);

    /**
     * 房间状态更新
     * @param oldState 旧房间状态
     * @param newState 新房间状态
     * @param songId 歌曲ID
     */
    void onRoomStateChanged(KTVRoomState oldState, KTVRoomState newState, String songId);

    /**
     * 演唱状态更新
     * @param oldState 旧演唱状态
     * @param newState 新演唱状态
     * @param score 演唱分数，演唱结束时同步
     */
    void onSingingPlayStateChanged(KTVMusicPlayState oldState, KTVMusicPlayState newState, int score);

    /**
     * 新用户请求发送房间当前状态
     * @param userId 用户ID
     */
    void onKTVStateSyncRequest(String userId);
}
