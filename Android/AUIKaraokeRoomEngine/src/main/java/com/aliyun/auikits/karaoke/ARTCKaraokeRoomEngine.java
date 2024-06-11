package com.aliyun.auikits.karaoke;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.aliyun.auikits.karaoke.bean.KTVMusicConfig;
import com.aliyun.auikits.karaoke.bean.KTVSingerRole;

/**
 * 伴奏音乐播放和推流管理 <p>
 * 管理 KTV 场景的伴奏音乐播放/暂停、切换原唱等操作，内部会维持所有演唱者的伴奏进度精准对齐、播放状态同步 <p>
 * <p>
 * 1. 开始演唱调用流程： <p>
 * 方法一（推荐）：{@link #setSingerRole(KTVSingerRole)} 切换为主唱者或合唱者角色 <p>
 *       -> {@link #loadMusic(KTVMusicConfig)} <p>
 *       其中主唱者设置 {@link KTVMusicConfig#autoPlay} = true, 合唱者可以不用设置 <p>
 * <p>
 * 方法二：{@link #loadMusic(KTVMusicConfig)} <p>
 *        -> 收到加载成功回调后 <p>
 *        -> {@link #setSingerRole(KTVSingerRole)} 切换为主唱者或合唱者角色 <p>
 *        -> {@link #playMusic()}，其中合唱者可以不调用此方法 <p>
 * <p>
 * 2. 结束演唱调用流程（如播放列表为空）： <p>
 * 方法一（推荐）：{@link #setSingerRole(KTVSingerRole)} 切换到听众角色即可 <p>
 * <p>
 * 方法二：{@link #stopMusic()} <p>
 *      -> {@link #setSingerRole(KTVSingerRole)} 切换到听众角色 <p>
 */
public interface ARTCKaraokeRoomEngine {

    /**
     * 设置演唱监听接口
     * @param callback
     */
    @MainThread
    void setCallback(@Nullable ARTCKaraokeRoomEngineCallback callback);

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
     * 加载音乐资源，当收到加载成功回调后即可调用 {@link #playMusic()} 播放音乐
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
}
