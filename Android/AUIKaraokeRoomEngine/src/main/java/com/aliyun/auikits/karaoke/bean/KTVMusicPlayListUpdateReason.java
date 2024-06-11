package com.aliyun.auikits.karaoke.bean;

public enum KTVMusicPlayListUpdateReason {
    PLAYING_LIST_UPDATE_REASON_ADD_MUSIC(1),
    PLAYING_LIST_UPDATE_REASON_REMOVE_MUSIC(2),
    PLAYING_LIST_UPDATE_REASON_PIN_MUSIC(3),
    PLAYING_LIST_UPDATE_REASON_SKIP_MUSIC(4),
    PLAYING_LIST_UPDATE_REASON_COMPLETE_MUSIC(5),
    PLAYING_LIST_UPDATE_REASON_JOIN_SINGING(6),
    PLAYING_LIST_UPDATE_REASON_LEAVE_JOIN_SINGING(7),
    PLAYING_LIST_UPDATE_REASON_OTHER(8);

    private KTVMusicPlayListUpdateReason(int i) {
        value = i;
    }

    private final int value;

    public int toInt() {
        return value;
    }

    public static KTVMusicPlayListUpdateReason fromInt(int i) {
        switch (i) {
            case 1: return PLAYING_LIST_UPDATE_REASON_ADD_MUSIC;
            case 2: return PLAYING_LIST_UPDATE_REASON_REMOVE_MUSIC;
            case 3: return PLAYING_LIST_UPDATE_REASON_PIN_MUSIC;
            case 4: return PLAYING_LIST_UPDATE_REASON_SKIP_MUSIC;
            case 5: return PLAYING_LIST_UPDATE_REASON_COMPLETE_MUSIC;
            case 6: return PLAYING_LIST_UPDATE_REASON_JOIN_SINGING;
            case 7: return PLAYING_LIST_UPDATE_REASON_LEAVE_JOIN_SINGING;
            case 8:
            default:
                return PLAYING_LIST_UPDATE_REASON_OTHER;
        }
    }
}
