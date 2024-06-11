package com.aliyuncs.aui.dto.enums;

/**
 * 歌曲状态
 *
 * @author chunlei.zcl
 */
public enum SongStatus {

    /**
     * 已播放
     */
    PLAYED(1),

    /**
     * 正在播放
     */
    PLAYING(2),

    /**
     * 待播放
     */
    PENDING_PLAY(3),

    /**
     * 已删除
     */
    DELETED(4);

    private int val;

    public static SongStatus of(int val) {

        for (SongStatus value : SongStatus.values()) {
            if (val == value.getVal()) {
                return value;
            }
        }
        return null;
    }

    SongStatus(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

}
