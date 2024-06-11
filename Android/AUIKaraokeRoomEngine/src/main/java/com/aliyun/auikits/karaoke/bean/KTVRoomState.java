package com.aliyun.auikits.karaoke.bean;

public enum KTVRoomState {
    /**
     * 初始状态
     */
    Init(1),
    /**
     * 等待状态
     */
    Waiting(2),
    /**
     * 演唱状态
     */
    Singing(3);

    KTVRoomState(int i) {
        value = i;
    }

    private final int value;

    public int toInt() {
        return value;
    }

    public static KTVRoomState fromInt(int i) {
        switch (i) {
            case 1:
            default:
                return Init;
            case 2:
                return Waiting;
            case 3:
                return Singing;
        }
    }
}
