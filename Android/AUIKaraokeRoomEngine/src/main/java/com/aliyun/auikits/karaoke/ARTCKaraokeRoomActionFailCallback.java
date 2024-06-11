package com.aliyun.auikits.karaoke;

public interface ARTCKaraokeRoomActionFailCallback {
    /**
     * 失败回调
     * @param errorCode
     * @param errorMsg
     */
    void onFail(int errorCode, String errorMsg);
}
