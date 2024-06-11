package com.aliyun.auikits.karaoke.bean;

/**
 * KTV 场景中的麦上用户角色 <p>
 * 注意： <p>
 * 1. 合唱者角色只有合唱玩法需要，独唱玩法不需要 <p>
 * 2. 一首歌曲播放过程中主唱者角色不应该发生变化，业务需要自己做好控制 <p>
 */
public enum KTVSingerRole {
    /**
     * 听众（默认状态）
     */
    Audience,
    /**
     * 主唱者（点歌用户）
     */
    LeadSinger,
    /**
     * 合唱者 <p>
     * 如果是独唱玩法则不需要这个角色
     */
    Choristers
}
