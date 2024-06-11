package com.aliyun.auikits.karaoke.bean;

import com.aliyun.auikits.voiceroom.module.seat.SeatInfo;

import java.util.ArrayList;
import java.util.List;

public class KTVPlayingMusicInfo extends KTVMusicInfo {
    private static final int SONG_STATUS_PLAYING = 2;
    /**
     *
     */
    private static final int SONG_STATUS_IN_QUEUE = 3;

    /**
     * 主唱人信息
     */
    public SeatInfo seatInfo;

    /**
     * 合唱人ID列表
     */
    public List<String> joinSingUserIdList = new ArrayList<>();

    /**
     * 歌曲的状态：<p>
     * 2:  正在播放 <p>
     * 3:  待播放
     */
    public int status = 3;

    public boolean isPlaying() {
        return status == 2;
    }
}
