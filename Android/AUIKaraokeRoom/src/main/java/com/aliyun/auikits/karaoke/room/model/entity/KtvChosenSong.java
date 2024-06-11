package com.aliyun.auikits.karaoke.room.model.entity;

import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;
import com.aliyun.auikits.voiceroom.module.seat.SeatInfo;

public class KtvChosenSong {
    private KTVPlayingMusicInfo mKtvPlayingMusicInfo;
    private int mSongOrder;
    private SeatInfo mChosenMember;
    private boolean mCanSkip = false;
    private boolean mCanPin = false;
    private boolean mCanTrash = false;

    public KTVPlayingMusicInfo getSongInfo() {
        return mKtvPlayingMusicInfo;
    }

    public SeatInfo getChosenMember() {
        return mChosenMember;
    }

    public int getSongOrder() {
        return mSongOrder;
    }

    public boolean isPlaying() {
        return mKtvPlayingMusicInfo.isPlaying();
    }

    public void setSongInfo(KTVPlayingMusicInfo mSongInfo) {
        this.mKtvPlayingMusicInfo = mSongInfo;
    }

    public void setSongOrder(int mSongOrder) {
        this.mSongOrder = mSongOrder;
    }

    public void setChosenMember(SeatInfo mChosenMember) {
        this.mChosenMember = mChosenMember;
    }

    public void setCanPin(boolean mCanPin) {
        this.mCanPin = mCanPin;
    }

    public void setCanSkip(boolean mCanSkip) {
        this.mCanSkip = mCanSkip;
    }

    public void setCanTrash(boolean mCanTrash) {
        this.mCanTrash = mCanTrash;
    }

    public boolean isCanPin() {
        return mCanPin;
    }

    public boolean isCanSkip() {
        return mCanSkip;
    }

    public boolean isCanTrash() {
        return mCanTrash;
    }

    public static KtvChosenSong fromKtvPlayingMusicInfo(KTVPlayingMusicInfo ktvPlayingMusicInfo) {
        KtvChosenSong ktvChosenSong = new KtvChosenSong();
        ktvChosenSong.setSongInfo(ktvPlayingMusicInfo);
        ktvChosenSong.setChosenMember(ktvPlayingMusicInfo.seatInfo);
        return ktvChosenSong;
    }
}
