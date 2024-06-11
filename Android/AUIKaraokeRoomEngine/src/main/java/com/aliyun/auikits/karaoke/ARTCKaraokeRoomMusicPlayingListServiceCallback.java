package com.aliyun.auikits.karaoke;

import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;

import java.util.List;

public interface ARTCKaraokeRoomMusicPlayingListServiceCallback extends ARTCKaraokeRoomActionFailCallback {

    /**
     * 新歌单列表回调
     * @param playingMusicInfoList 歌单列表
     */
    void onMusicPlayingListCallback(List<KTVPlayingMusicInfo> playingMusicInfoList);

    /**
     * 回调当前播放歌曲的合唱列表
     * @param ktvPlayingMusicInfo 当前播放歌曲
     */
    void onMusicPlayingJoinerListCallback(KTVPlayingMusicInfo ktvPlayingMusicInfo);
}
