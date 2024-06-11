package com.aliyun.auikits.karaoke.room.widget.lyric.model;

import java.util.ArrayList;

public class LyricLine {
    public long start;
    public long duration;
    public String content;
    public ArrayList<LyricWord> wordList = new ArrayList<>();
}
