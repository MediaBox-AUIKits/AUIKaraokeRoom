package com.aliyun.auikits.karaoke;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.aliyun.auikits.karaoke.bean.KTVMusicConfig;
import com.aliyun.auikits.karaoke.bean.KTVMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVSingerRole;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngine;

import java.util.List;

public interface ARTCKaraokeRoomController extends ARTCVoiceRoomEngine {
    /**
     * @return 在登录成功之后才能调用，否则返回 null
     */
    @MainThread
    ARTCKaraokeRoomEngine getKTVEngine();

    /**
     * 设置曲库服务工厂，在init之前调用
     * @param musicLibraryFactory 曲库服务工厂
     */
    @MainThread
    void setMusicLibraryFactory(
            ARTCKaraokeRoomMusicLibrary.ARTCKaraokeRoomMusicLibraryFactory musicLibraryFactory);

    /**
     * 设置点歌服务工厂，在init之前调用
     * @param musicPlayingListServiceFactory 点歌服务工厂
     */
    @MainThread
    void setMusicPlayingListServiceFactory(
            ARTCKaraokeRoomMusicPlayingListService.ARTCKaraokeRoomMusicPlayingListServiceFactory musicPlayingListServiceFactory);

    /////////////////////////
    // 点歌相关接口
    /////////////////////////
    /**
     * 获取远端播放列表（当前播放中+未播放）
     * @param callback 回调接口 {@link ARTCKaraokeRoomMusicPlayingListServiceCallback}
     */
    void fetchMusicPlayingList(@NonNull ARTCKaraokeRoomMusicPlayingListServiceCallback callback);

    /**
     * 能否点歌 <p>
     * 权限：麦上成员可以点歌
     * @return 是否有权限
     */
    boolean checkCanAddMusic();

    /**
     * 能否删歌 <p>
     * 权限：房主、点歌成员可以删歌
     * @param ktvPlayingMusicInfo 播放列表的歌曲
     * @return 是否有权限
     */
    boolean checkCanRemoveMusic(@NonNull KTVPlayingMusicInfo ktvPlayingMusicInfo);

    /**
     * 点歌，歌曲下载完成后才会加入到播放列表
     * @param musicInfo 歌曲
     */
    void addMusic(@NonNull KTVMusicInfo musicInfo);

    /**
     * 删除已点歌曲
     * @param ktvPlayingMusicInfo 播放列表的歌曲
     */
    void removeMusic(@NonNull KTVPlayingMusicInfo ktvPlayingMusicInfo);

    /**
     * 批量删除已点歌曲
     * @param ktvPlayingMusicInfoList 播放列表
     */
    void removeMusic(@NonNull List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList);


    /**
     * 能否置顶已点歌曲 <p>
     * 权限：房主、点歌成员可以置顶
     * @param ktvPlayingMusicInfo 播放列表的歌曲
     * @return 是否有权限
     */
    boolean checkCanPinMusic(@NonNull KTVPlayingMusicInfo ktvPlayingMusicInfo);

    /**
     * 置顶已点歌曲
     * @param ktvPlayingMusicInfo 播放列表的歌曲
     */
    void pinMusic(@NonNull KTVPlayingMusicInfo ktvPlayingMusicInfo);

    /**
     * 获取当前播放列表中正在播放的歌曲
     * @return 正在播放的歌曲
     */
    KTVPlayingMusicInfo getCurrentPlayingMusicInfo();

    /**
     * 获取本地播放列表
     * @return 播放列表
     */
    List<KTVPlayingMusicInfo> getLocalPlayMusicInfoList();

    /////////////////////////
    // 曲库相关接口
    /////////////////////////
    /**
     * 获取榜单列表
     * @param callback 回调接口 {@link ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicChartCallback}
     */
    void fetchMusicChartList(@NonNull ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicChartCallback callback);

    /**
     * 获取榜单歌曲列表
     * @param chartId 榜单ID
     * @param pageIndex 列表页index
     * @param pageSize 列表页面大小
     * @param callback 回调接口 {@link ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback}
     */
    void fetchMusicList(@NonNull String chartId, int pageIndex, int pageSize,
                        @NonNull ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback callback);

    /**
     * 获取歌曲信息
     * @param songId 歌曲ID
     * @param callback 回调接口 {@link ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoCallback}
     */
    void fetchMusicInfo(@NonNull String songId, @NonNull ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoCallback callback);

    /**
     * 获取歌词
     * @param songId 歌曲ID
     * @param callback 回调接口 {@link ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicLyricCallback}
     */
    void fetchMusicLyric(@NonNull String songId, @NonNull ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicLyricCallback callback);

    /**
     * 获取歌曲标准音高
     * @param songId 歌曲ID
     * @param callback 回调接口 {@link ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicPitchCallback}
     */
    void fetchMusicPitch(@NonNull String songId, @NonNull ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicPitchCallback callback);

    /**
     * 搜索歌曲
     * @param keyword 搜索关键字
     * @param pageIndex 歌曲列表页Index
     * @param pageSize 歌曲列表页Size
     * @param callback 回调接口 {@link ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback}
     */
    void searchMusic(@NonNull String keyword, int pageIndex, int pageSize,
                     @NonNull ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback callback);

    /**
     * 下载歌曲
     * @param songId 歌曲ID
     * @param actionCallback 回调接口 {@link ARTCKaraokeRoomActionCallback}
     */
    void downloadMusic(@NonNull String songId, ARTCKaraokeRoomActionCallback actionCallback);

    /**
     * 增加下载回调监听
     * @param callback 回调接口 {@link ARTCKaraokeRoomMusicLibraryCallback}
     */
    void addDownloadCallback(@NonNull ARTCKaraokeRoomMusicLibraryCallback callback);

    /**
     * 删除下载回调监听
     * @param callback 回调接口 {@link ARTCKaraokeRoomMusicLibraryCallback}
     */
    void removeDownloadCallback(@NonNull ARTCKaraokeRoomMusicLibraryCallback callback);


    /////////////////////////
    // 播放相关接口
    /////////////////////////

    /**
     * 增加播放引擎监听
     * @param callback 回调接口 {@link ARTCKaraokeRoomEngineCallback}
     */
    @MainThread
    void addRoomEngineCallback(@Nullable ARTCKaraokeRoomEngineCallback callback);

    /**
     * 删除播放引擎监听
     * @param callback {@link ARTCKaraokeRoomEngineCallback}
     */
    @MainThread
    void removeRoomEngineCallback(@Nullable ARTCKaraokeRoomEngineCallback callback);

    /*************************************************
     * 演唱角色管理
     *************************************************/

    /**
     * 转换演唱角色，以管理相应角色的推拉流逻辑。 <p>
     * 调用时机：开始演唱前（必须在调用 {@link #playMusic()} 前），或者结束演唱后
     *
     * @param newRole 目标转换角色
     * @return 是否支持转换到目标角色
     */
    @MainThread
    boolean setSingerRole(KTVSingerRole newRole);


    /*************************************************
     * 演唱者的媒体播放管理 <p>
     * 同一时间内只能有一首歌曲在加载和播放
     *************************************************/

    /**
     * 加载音乐资源，当收到加载成功回调后即可调用 {@link #playMusic()} 播放音乐
     *
     * @param uri 本地或在线音乐地址
     */
    @MainThread
    void loadMusic(@NonNull String uri);

    /**
     * 加载版权音乐资源，当收到加载成功回调后即可调用 {@link #playMusic()} 播放音乐
     *
     * @param songId 版权音乐ID
     */
    @MainThread
    void loadCopyrightMusic(@NonNull String songId);

    /**
     * 加载音乐资源，当收到加载成功回调后即可调用 {@link #playMusic()} 播放音乐 <p>
     * 如果设置了 {@link KTVMusicConfig#autoPlay} 为 true 则内部会自动调用 {@link #playMusic()}
     *
     * @param config 音乐资源加载配置
     */
    @MainThread
    void loadMusic(@NonNull KTVMusicConfig config);

    /**
     * 播放音乐。需要先调用 {@link #loadMusic} 并收到加载成功回调，才能调用此方法开始播放。 <p>
     * 如果设置了 {@link KTVMusicConfig#autoPlay} 为 true 则可以不调用此方法； <p>
     * 合唱者也可以不调用此方法，内部会自动同步主唱者播放状态
     */
    @MainThread
    void playMusic();

    /**
     * 停止播放音乐 <p>
     * {@link #setSingerRole(KTVSingerRole)} 设置为 {@link KTVSingerRole#Audience} 时也会自动停止播放
     */
    @MainThread
    void stopMusic();

    /**
     * 继续播放音乐 <p>
     * 外部需要约束只有主唱者才能调用
     */
    @MainThread
    void resumeMusic();

    /**
     * 暂停播放音乐 <p>
     * 外部需要约束只有主唱者才能调用
     */
    @MainThread
    void pauseMusic();

    /**
     * 跳到某个位置 <p>
     * 外部需要约束只有主唱者才能调用
     */
    @MainThread
    void seekMusicTo(long millisecond);

    /**
     * 获取音乐总时长，必须在加载音乐资源成功后调用才有效，否则返回 0
     */
    @MainThread
    long getMusicTotalDuration();

    /**
     * 获取当前播放进度，必须在加载音乐资源成功后调用才有效，否则返回 0
     */
    @MainThread
    long getMusicCurrentProgress();

    /**
     * 切换伴奏/原唱模式
     * @param original 是否原唱
     */
    @MainThread
    void setMusicAccompanimentMode(boolean original);

    /**
     * 设置伴奏（音乐播放）音量
     * @param volume 目标音量
     */
    @MainThread
    void setMusicVolume(int volume);

    /**
     * 能否切换播放模式，权限：主唱+伴唱
     * @return
     */
    boolean canChangeMusicAccompanimentMode();

    /**
     * 能否加入合唱 <p>
     * 权限：麦上且不是主唱
     * @return
     */
    boolean canJoinSinging();

    /**
     * 自己是否主唱人
     * @return
     */
    boolean isLeadSinger();

    /**
     * 自己是否是合唱者
     * @return
     */
    boolean isJoinSinger();

    /**
     * 加入合唱
     * @param callback
     */
    void joinSinging(ARTCKaraokeRoomActionCallback callback);

    /**
     * 退出合唱
     * @param callback
     */
    void leaveSinging(ARTCKaraokeRoomActionCallback callback);

    /**
     * 切歌
     */
    void skipMusic();

    /**
     * 能否删歌，权限：房主+点歌成员
     * @param ktvPlayingMusicInfo
     * @return
     */
    boolean checkCanSkipMusic(KTVPlayingMusicInfo ktvPlayingMusicInfo);

    /**
     * 同步演唱分数
     */
    void updateScore(int score);

    /**
     * 新用户进入房间可以要求同步当前的房间状态
     */
    void requestRemoteKtvState();
}
