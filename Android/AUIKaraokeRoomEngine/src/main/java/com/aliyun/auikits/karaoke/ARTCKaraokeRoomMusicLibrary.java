package com.aliyun.auikits.karaoke;

import android.os.Looper;

import androidx.annotation.NonNull;

public interface ARTCKaraokeRoomMusicLibrary {

    /**
     * 获取榜单列表
     * @param callback 回调接口 {@link ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicChartCallback}
     */
    void fetchMusicChartList(ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicChartCallback callback);

    /**
     * 获取榜单歌曲列表
     * @param chartId 榜单ID
     * @param pageIndex 列表页index
     * @param pageSize 列表页面大小
     * @param callback 回调接口 {@link ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback}
     */
    void fetchMusicList(String chartId, int pageIndex, int pageSize,
                        ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback callback);

    /**
     * 获取歌曲信息
     * @param songId 歌曲ID
     * @param callback 回调接口 {@link ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoCallback}
     */
    void fetchMusicInfo(String songId, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoCallback callback);

    /**
     * 获取歌词
     * @param songId 歌曲ID
     * @param callback 回调接口 {@link ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicLyricCallback}
     */
    void fetchMusicLyric(String songId, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicLyricCallback callback);

    /**
     * 获取歌曲标准音高
     * @param songId 歌曲ID
     * @param callback 回调接口 {@link ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicPitchCallback}
     */
    void fetchMusicPitch(String songId, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicPitchCallback callback);

    /**
     * 搜索歌曲
     * @param keyword 搜索关键字
     * @param pageIndex 歌曲列表页Index
     * @param pageSize 歌曲列表页Size
     * @param callback 回调接口 {@link ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback}
     */
    void searchMusic(String keyword, int pageIndex, int pageSize,
                     ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback callback);

    /**
     * 下载歌曲
     * @param songId 歌曲ID
     */
    void downloadMusic(String songId);

    /**
     * 歌曲是否已经下载完成
     * @param songId
     * @return
     */
    boolean isMusicDownloaded(String songId);

    /**
     * 设置下载回调监听
     * @param callback {@link ARTCKaraokeRoomMusicLibraryCallback}
     */
    void setDownloadCallback(ARTCKaraokeRoomMusicLibraryCallback callback);

    /**
     * 销毁曲库实例
     */
    void destroy();

    /**
     * 曲库服务工厂
     */
    interface ARTCKaraokeRoomMusicLibraryFactory {
        ARTCKaraokeRoomMusicLibrary createInstance(@NonNull Looper looper, String cachePath);
    }
}
