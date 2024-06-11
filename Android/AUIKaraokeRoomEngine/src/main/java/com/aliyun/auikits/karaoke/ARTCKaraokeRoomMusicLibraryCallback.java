package com.aliyun.auikits.karaoke;

import com.aliyun.auikits.karaoke.bean.KTVChartInfo;
import com.aliyun.auikits.karaoke.bean.KTVMusicInfo;

import java.util.List;

public interface ARTCKaraokeRoomMusicLibraryCallback extends ARTCKaraokeRoomActionFailCallback {
    /**
     * 歌曲下载成功
     * @param musicInfo 歌曲
     */
    void onMusicDownloadCompleted(KTVMusicInfo musicInfo);

    interface ARTCKaraokeRoomMusicChartCallback extends ARTCKaraokeRoomActionFailCallback {
        /**
         * 榜单信息回调
         * @param ktvChartInfoList
         */
        void onMusicChartCallback(List<KTVChartInfo> ktvChartInfoList);
    }

    interface ARTCKaraokeRoomMusicInfoListCallback extends ARTCKaraokeRoomActionFailCallback {
        /**
         * 歌曲列表回调
         * @param musicInfoList
         */
        void onMusicInfoCallback(List<KTVMusicInfo> musicInfoList);
    }

    interface ARTCKaraokeRoomMusicInfoCallback extends ARTCKaraokeRoomActionFailCallback {
        /**
         * 歌曲回调
         * @param musicInfo
         */
        void onMusicInfoCallback(KTVMusicInfo musicInfo);
    }

    interface ARTCKaraokeRoomMusicLyricCallback extends ARTCKaraokeRoomActionFailCallback {
        /**
         * 歌词回调
         * @param lyric 歌词
         * @param lyricType 歌词类型
         */
        void onMusicLyricCallback(String lyric, KTVMusicInfo.KTVLyricType lyricType);
    }

    interface ARTCKaraokeRoomMusicPitchCallback extends ARTCKaraokeRoomActionFailCallback {
        /**
         * 标准音高回调
         * @param pitch
         */
        void onMusicPitchCallback(String pitch);
    }
}
