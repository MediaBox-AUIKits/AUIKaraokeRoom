package com.aliyun.auikits.karaoke.room.widget.pitch.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class MidiData {
    @JSONField
    public List<Midi> midiList;

    @JSONField
    public float confidence;

    public static class Midi {
        @JSONField
        public float start;

        @JSONField
        public float end;

        @JSONField
        public int pitch;

        @JSONField
        public int velocity;
    }
}
