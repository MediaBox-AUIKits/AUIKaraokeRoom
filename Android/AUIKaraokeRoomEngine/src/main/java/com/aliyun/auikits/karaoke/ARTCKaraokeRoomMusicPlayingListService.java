package com.aliyun.auikits.karaoke;

import android.os.Looper;

import androidx.annotation.NonNull;

import com.aliyun.auikits.karaoke.bean.KTVMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;
import com.aliyun.auikits.voiceroom.bean.RoomInfo;
import com.aliyun.auikits.voiceroom.module.seat.SeatInfo;

import java.util.List;

public interface ARTCKaraokeRoomMusicPlayingListService {
    /**
     * 获取远端播放列表（当前播放中+未播放）
     * @param callback
     */
    void fetchMusicPlayingList(@NonNull RoomInfo roomInfo, @NonNull ARTCKaraokeRoomMusicPlayingListServiceCallback callback);

    /**
     * 点歌，歌曲下载完成后才可以加入到播放列表
     * @param musicInfo
     */
    void addMusic(@NonNull RoomInfo roomInfo, @NonNull KTVMusicInfo musicInfo, @NonNull SeatInfo seatInfo, ARTCKaraokeRoomActionCallback callback);

    /**
     * 删除已点歌曲
     * @param roomInfo
     * @param songIdList
     * @param seatInfo
     * @param actionSeatInfo
     * @param callback
     */
    void removeMusic(@NonNull RoomInfo roomInfo, @NonNull List<String> songIdList, @NonNull SeatInfo seatInfo, @NonNull SeatInfo actionSeatInfo, ARTCKaraokeRoomActionCallback callback);

    /**
     * 置顶已点歌曲
     * @param songId
     */
    void pinMusic(@NonNull RoomInfo roomInfo, @NonNull String songId, @NonNull SeatInfo seatInfo, @NonNull SeatInfo actionSeatInfo, ARTCKaraokeRoomActionCallback callback);

    /**
     * 通知远端开始播放歌曲
     * @param musicInfo
     * @param callback
     */
    void playMusic(@NonNull RoomInfo roomInfo, @NonNull KTVMusicInfo musicInfo, @NonNull SeatInfo seatInfo, @NonNull SeatInfo actionSeatInfo, ARTCKaraokeRoomActionCallback callback);

    /**
     * 加入合唱
     * @param musicInfo
     * @param seatInfo
     * @param callback
     */
    void joinSinging(@NonNull RoomInfo roomInfo, @NonNull KTVMusicInfo musicInfo, @NonNull SeatInfo seatInfo, ARTCKaraokeRoomActionCallback callback);

    /**
     * 退出合唱
     * @param musicInfo
     * @param seatInfo
     * @param callback
     */
    void leaveSinging(@NonNull RoomInfo roomInfo, @NonNull KTVMusicInfo musicInfo, @NonNull SeatInfo seatInfo, ARTCKaraokeRoomActionCallback callback);

    /**
     * 拉取合唱人列表
     * @param musicInfo
     * @param callback
     */
    void fetchJoinerList(@NonNull RoomInfo roomInfo, @NonNull KTVPlayingMusicInfo musicInfo, ARTCKaraokeRoomMusicPlayingListServiceCallback callback);

    /**
     * 获取当前歌单的第一首歌
     * @return
     */
    KTVPlayingMusicInfo getCurrentPlayingMusicInfo();

    /**
     * 获取当前播放中的歌曲
     * @return
     */
    KTVPlayingMusicInfo getPlayingMusicInfo(int index);

    /**
     * 获取缓存中的歌单列表
     * @return
     */
    List<KTVPlayingMusicInfo> getCachedMusicPlayingList();

    /**
     * 释放资源
     */
    void destroy();

    /**
     * 点歌服务工厂
     */
    interface ARTCKaraokeRoomMusicPlayingListServiceFactory {
        ARTCKaraokeRoomMusicPlayingListService createInstance(@NonNull Looper looper);
    }
}
