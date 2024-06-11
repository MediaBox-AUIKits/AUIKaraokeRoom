package com.aliyun.auikits.karaoke.bean;

public enum KTVMusicPlayState {
    /**
     * 未播放状态
     */
    Idle(1),
    /**
     * 播放中
     */
    Playing(2),
    /**
     * 暂停状态
     */
    Paused(3),
    /**
     * 播放完成，用户需要切换到播放下一首歌，或者结束演唱
     */
    Completed(4);


    private KTVMusicPlayState(int i) {
        value = i;
    }

    private final int value;

    public int toInt() {
        return value;
    }

    public static KTVMusicPlayState fromInt(int i) {
        switch (i) {
            case 1: return Idle;
            case 2: return Playing;
            case 3: return Paused;
            case 4: return Completed;
            default: return Idle;
        }
    }
}
