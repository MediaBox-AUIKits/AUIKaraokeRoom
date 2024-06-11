package com.aliyun.auikits.karaoke.room.widget.lyric;

import com.aliyun.auikits.karaoke.room.widget.lyric.model.LyricLine;

public interface OnLyricLineFinishedListener {
    void onLyricLineFinished(int position, LyricLine line);
}
