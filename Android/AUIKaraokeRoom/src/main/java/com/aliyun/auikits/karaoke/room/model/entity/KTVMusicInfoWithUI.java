package com.aliyun.auikits.karaoke.room.model.entity;

import com.aliyun.auikits.karaoke.bean.KTVMusicInfo;

public class KTVMusicInfoWithUI {
    private KTVMusicInfo mMusicInfo;
    private boolean mIsChosen = false;

    public void setMusicInfo(KTVMusicInfo mMusicInfo) {
        this.mMusicInfo = mMusicInfo;
    }

    public KTVMusicInfo getMusicInfo() {
        return mMusicInfo;
    }

    public boolean isIsChosen() {
        return mIsChosen;
    }

    public void setIsChosen(boolean mIsChosen) {
        this.mIsChosen = mIsChosen;
    }
}
