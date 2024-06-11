package com.aliyun.auikits.karaoke.bean;

public class KTVMusicInfo {

    /** 歌曲id */
    public String songID;
    /** 资源载体类型，播放器需此信息 */
    public KTVMusicPlayoutType playOutType;
    /** 歌曲名 */
    public String songName;
    /** 歌曲发行时间 */
    public String releaseTime;
    /** 版权方id */
    public int vendorId;
    /** 歌手名 */
    public String singerName;
    /** 歌手头像 */
    public String singerImg;
    /** 歌曲所属专辑名 */
    public String albumName;
    /** 专辑封面 */
    public String albumImg;
    /** 歌曲时长，单位:毫秒 */
    public int duration;
    /** 伴奏时长，单位:毫秒 */
    public int accompanyDuration;
    /** 歌词类型 */
    public KTVLyricType lyricType;
    /** 是否有高潮片段 */
    public boolean hasClip;
    /** 是否有短分片高潮片段 */
    public boolean hasShortSegment;
    /** 是否有基准音高线 */
    public boolean hasStandardPitch;
    /** 远端资源链接 */
    public String remoteUrl;
    /** 本地资源路径 */
    public String localPath;

    public enum KTVMusicPlayoutType {
        /*! 不支持的播放形式 */
        Invalid(0),
        /*! 无伴奏、原唱分离 */
        Single(1),
        /*! 人声、伴奏分别对应两个文件 */
        FileSeparate(2),
        /*! 左声道存储原唱+伴奏，右声道存储伴奏 */
        ChannelLeftFullRightAccompany(3),
        /*! 左声道存储人声，右声道存储伴奏 */
        ChannelLeftHumanRightAccompany(4),
        /*! 多音轨，假定单个供应商的人声音轨是固定的，与vendor ID配合 */
        MultipleTrack(5);
        private int value;
        KTVMusicPlayoutType(int v) { value = v; }
        public int getValue() { return value; }
        public static KTVMusicPlayoutType fromIndex(int index) {
            KTVMusicPlayoutType ret = null;
            switch (index) {
                case 1:
                    ret = KTVMusicPlayoutType.Single;
                    break;
                case 2:
                    ret = KTVMusicPlayoutType.FileSeparate;
                    break;
                case 3:
                    ret = KTVMusicPlayoutType.ChannelLeftFullRightAccompany;
                    break;
                case 4:
                    ret = KTVMusicPlayoutType.ChannelLeftHumanRightAccompany;
                    break;
                case 5:
                    ret = KTVMusicPlayoutType.MultipleTrack;
                    break;
                default:
                    ret = KTVMusicPlayoutType.Invalid;
                    break;
            }
            return ret;
        }
    }

    public enum KTVLyricType {
        /*! 无效格式 */
        LyricInvalid(0),
        /*! LRC格式 */
        LyricLrc(1),
        /*! KRC格式 */
        LyricKrc(2);
        private int value;
        KTVLyricType(int v) { value = v; }
        public int getValue() { return value; }
        public static KTVLyricType fromIndex(int index) {
            KTVLyricType ret = null;
            switch (index) {
                case 1:
                    ret = KTVLyricType.LyricLrc;
                    break;
                case 2:
                    ret = KTVLyricType.LyricKrc;
                    break;
                default:
                    ret = KTVLyricType.LyricInvalid;
                    break;
            }
            return ret;
        }
    }
}
