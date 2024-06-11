package com.aliyun.auikits.karaoke.room.widget.pitch.model;

public class MusicPitch {
    public long startTime = 0;
    public long duration = 0;
    public int pitch = 0;

    public MusicPitch(long startTime, long duration, int pitch){
        this.startTime = startTime;
        this.duration = duration;
        this.pitch = pitch;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }
}
