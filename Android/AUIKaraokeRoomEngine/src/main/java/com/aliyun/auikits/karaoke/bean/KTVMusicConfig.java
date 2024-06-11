package com.aliyun.auikits.karaoke.bean;

public class KTVMusicConfig {
    /**
     * 版权音乐ID
     */
    public String songId;
    /**
     * 本地或在线音乐地址
     */
    public String uri;
    /**
     * 开始播放的位置，单位：毫秒
     */
    public long startPosition = 0;
    /**
     * 设置为 true 后会在资源加载完成时自动开始播放音乐
     */
    public boolean autoPlay = false;
    /**
     * 当音频有多音轨时（如伴奏和原唱）需设置多音轨模式
     */
    public boolean isMultipleTrack = false;
}
