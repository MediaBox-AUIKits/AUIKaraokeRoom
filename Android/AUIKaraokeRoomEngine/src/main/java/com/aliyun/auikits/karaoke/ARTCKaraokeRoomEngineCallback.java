package com.aliyun.auikits.karaoke;

import com.aliyun.auikits.karaoke.bean.KTVMusicPlayState;
import com.aliyun.auikits.karaoke.bean.KTVMusicPrepareState;
import com.aliyun.auikits.karaoke.bean.KTVSingerRole;

public interface ARTCKaraokeRoomEngineCallback {
    /**
     * 音乐资源准备状态回调 <p>
     * 听众角色不会收到这个回调
     * @param state 准备状态
     */
    void onMusicPrepareStateUpdate(KTVMusicPrepareState state);
    /**
     * 音乐播放状态回调 <p>
     * 听众角色不会收到这个回调
     */
    void onMusicPlayStateUpdate(KTVMusicPlayState state);

    /**
     * 音乐播放进度回调
     * @param millisecond 音乐当前播放的位置
     */
    void onMusicPlayProgressUpdate(long millisecond);

    /**
     * 演唱角色变化回调
     * @param oldRole 旧角色
     * @param newRole 新角色
     */
    void onSingerRoleUpdate(KTVSingerRole oldRole, KTVSingerRole newRole);
}
