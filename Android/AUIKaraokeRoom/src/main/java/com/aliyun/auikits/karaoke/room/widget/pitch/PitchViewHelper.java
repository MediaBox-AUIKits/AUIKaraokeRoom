package com.aliyun.auikits.karaoke.room.widget.pitch;

import android.graphics.Color;

import com.alibaba.fastjson.JSON;
import com.aliyun.auikits.karaoke.room.widget.pitch.model.MidiData;
import com.aliyun.auikits.karaoke.room.widget.pitch.model.MusicPitch;

import java.util.ArrayList;
import java.util.List;

public class PitchViewHelper {

    public static List<MusicPitch> parseMidiFile(String midiString) {
        List<MusicPitch> musicPitchList = new ArrayList<>();
        MidiData midiData = JSON.parseObject(midiString, MidiData.class);
        if (null != midiData && null != midiData.midiList && midiData.midiList.size() > 0) {
            for (MidiData.Midi midi : midiData.midiList) {
                long startMilli = (long) midi.start;
                long endMilli = (long) midi.end;
                MusicPitch musicPitch = new MusicPitch(
                        startMilli,
                        endMilli - startMilli,
                        midi.pitch
                );
                musicPitchList.add(musicPitch);
            }
        }
        return musicPitchList;
    }

    public static AliyunKTVPitchViewUIConfigBuilder getConfigBuilder() {
        return new AliyunKTVPitchViewUIConfigBuilder();
    }
    public static class AliyunKTVPitchViewUIConfigBuilder {

        private int standardPitchColor = Color.parseColor("#FF5D3B94");  //默认音高线颜色
        private int hitPitchColor = Color.parseColor("#FF3751");  // 击中音高线颜色
        private int pitchIndicatorColor = Color.parseColor("#FFFFFF");  // 音调指示器颜色
        private int staffColor = Color.parseColor("#33FFFFFF"); //五线谱横线颜色
        private int verticalLineColor = Color.parseColor("#FFA87BF1"); //竖线颜色
        private int scoreTextColor = Color.WHITE; //分数文本颜色

        /**
         * 设置默认音高线颜色
         */
        public AliyunKTVPitchViewUIConfigBuilder setStandardPitchColor(int standardPitchColor) {
            this.standardPitchColor = standardPitchColor;
            return this;
        }

        /**
         * 设置击中音高线颜色
         */
        public AliyunKTVPitchViewUIConfigBuilder setHitPitchColor(int hitPitchColor) {
            this.hitPitchColor = hitPitchColor;
            return this;
        }

        /**
         * 设置音调指示器颜色
         */
        public AliyunKTVPitchViewUIConfigBuilder setPitchIndicatorColor(int pitchIndicatorColor) {
            this.pitchIndicatorColor = pitchIndicatorColor;
            return this;
        }

        /**
         * 设置五线谱横线颜色
         */
        public AliyunKTVPitchViewUIConfigBuilder setStaffColor(int staffColor) {
            this.staffColor = staffColor;
            return this;
        }

        /**
         * 设置竖线颜色
         */
        public AliyunKTVPitchViewUIConfigBuilder setVerticalLineColor(int verticalLineColor) {
            this.verticalLineColor = verticalLineColor;
            return this;
        }

        /**
         * 设置分数文本颜色
         */
        public AliyunKTVPitchViewUIConfigBuilder setScoreTextColor(int scoreTextColor) {
            this.scoreTextColor = scoreTextColor;
            return this;
        }

        public AliyunKTVPitchViewUIConfig build(){
            AliyunKTVPitchViewUIConfig config = new AliyunKTVPitchViewUIConfig();
            config.setStandardPitchColor(standardPitchColor);
            config.setHitPitchColor(hitPitchColor);
            config.setPitchIndicatorColor(pitchIndicatorColor);
            config.setStaffColor(staffColor);
            config.setVerticalLineColor(verticalLineColor);
            config.setScoreTextColor(scoreTextColor);
            return config;
        }
    }


    public static class AliyunKTVPitchViewUIConfig {

        private int standardPitchColor = Color.parseColor("#FF5D3B94");  // 默认音高线颜色
        private int hitPitchColor = Color.parseColor("#FF3751");  // 击中音高线颜色
        private int pitchIndicatorColor = Color.parseColor("#FFFFFF");  // 音调指示器颜色
        private int staffColor = Color.parseColor("#33FFFFFF"); //五线谱横线颜色
        private int verticalLineColor = Color.parseColor("#FFA87BF1"); //竖线颜色
        private int scoreTextColor = Color.WHITE; //分数文本颜色

        public int getStandardPitchColor() {
            return standardPitchColor;
        }

        public void setStandardPitchColor(int standardPitchColor) {
            this.standardPitchColor = standardPitchColor;
        }

        public int getHitPitchColor() {
            return hitPitchColor;
        }

        public void setHitPitchColor(int hitPitchColor) {
            this.hitPitchColor = hitPitchColor;
        }

        public int getPitchIndicatorColor() {
            return pitchIndicatorColor;
        }

        public void setPitchIndicatorColor(int pitchIndicatorColor) {
            this.pitchIndicatorColor = pitchIndicatorColor;
        }

        public int getStaffColor() {
            return staffColor;
        }

        public void setStaffColor(int staffColor) {
            this.staffColor = staffColor;
        }

        public int getVerticalLineColor() {
            return verticalLineColor;
        }

        public void setVerticalLineColor(int verticalLineColor) {
            this.verticalLineColor = verticalLineColor;
        }

        public int getScoreTextColor() {
            return scoreTextColor;
        }

        public void setScoreTextColor(int scoreTextColor) {
            this.scoreTextColor = scoreTextColor;
        }
    }
}
