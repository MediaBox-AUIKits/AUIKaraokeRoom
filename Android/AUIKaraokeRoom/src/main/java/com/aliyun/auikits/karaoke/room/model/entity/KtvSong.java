//package com.aliyun.auikits.ktv.model.entity;
//
//import com.aliyun.auikits.ktvroom.bean.KTVMusicInfo;
//
//public class KtvSong {
//    private String mSongId;
//    private String mSongName;
//    private String mSingerName;
//    private String mAlbumUrl;
//    private long mDurationInMillis = 0;
//
//    public String getSongId() {
//        return mSongId;
//    }
//
//    public String getSongName() {
//        return mSongName;
//    }
//
//    public String getSingerName() {
//        return mSingerName;
//    }
//
//    public String getAlbumUrl() {
//        return mAlbumUrl;
//    }
//
//    public long getDurationInMillis() {
//        return mDurationInMillis;
//    }
//
//    public void setSongId(String mSongId) {
//        this.mSongId = mSongId;
//    }
//
//    public void setSongName(String mSongName) {
//        this.mSongName = mSongName;
//    }
//
//    public void setSingerName(String mSingerName) {
//        this.mSingerName = mSingerName;
//    }
//
//    public void setAlbumUrl(String mAlbumUrl) {
//        this.mAlbumUrl = mAlbumUrl;
//    }
//
//    public void setDurationInMillis(long mDurationInMillis) {
//        this.mDurationInMillis = mDurationInMillis;
//    }
//
//    public static KtvSong fromKtvMusicInfo(KTVMusicInfo ktvMusicInfo) {
//        KtvSong ktvSong = new KtvSong();
//        ktvSong.setSongId(ktvMusicInfo.songID);
//        ktvSong.setSongName(ktvMusicInfo.songName);
//        ktvSong.setSingerName(ktvMusicInfo.singerName);
//        ktvSong.setAlbumUrl(ktvMusicInfo.albumImg);
//        ktvSong.setDurationInMillis(ktvMusicInfo.duration);
//        return ktvSong;
//    }
//}
