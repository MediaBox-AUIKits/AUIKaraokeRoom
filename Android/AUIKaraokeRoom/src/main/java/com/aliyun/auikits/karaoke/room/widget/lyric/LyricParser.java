package com.aliyun.auikits.karaoke.room.widget.lyric;

import  android.text.Html;

import com.aliyun.auikits.karaoke.room.widget.lyric.model.Lyric;
import com.aliyun.auikits.karaoke.room.widget.lyric.model.LyricLine;
import com.aliyun.auikits.karaoke.room.widget.lyric.model.LyricWord;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricParser {

    public static float calculateCurrentKrcProcess(long currentTimeMillis, LyricLine lyricLine) {
        float krcProcess = 0f;

        ArrayList<LyricWord> wordList = lyricLine.wordList;
        int wordListSize = lyricLine.wordList.size();

        long lineOffsetMilli = currentTimeMillis - lyricLine.start;

        if (lineOffsetMilli < lyricLine.duration) {
            for (int wordCnt = 0; wordCnt < wordListSize; wordCnt++) {
                LyricWord currentLyricWord = lyricLine.wordList.get(wordCnt);
                if (lineOffsetMilli >= currentLyricWord.start && lineOffsetMilli <= currentLyricWord.start + currentLyricWord.duration) {
                    // 在这个词组区间内
                    // 算出之前所有词组的时间占比
                    float progressBefore = wordCnt*1.0f / wordListSize;
                    // 这个词组占的比重，按份数来算
                    float percent = 1.0f / wordListSize;
                    // 在计算当前时间戳在这个词组内的时间占比，线性
                    float progressCurrentWord =
                            (lineOffsetMilli - currentLyricWord.start)*1.0f / currentLyricWord.duration;
                    // 这两个progress加起来就是总的时间百分比
                    krcProcess = progressBefore + progressCurrentWord * percent;
                } else if (wordCnt < wordListSize-1) {
                    LyricWord nextLyricWord = wordList.get(wordCnt+1);
                    if (lineOffsetMilli > currentLyricWord.start+currentLyricWord.duration && lineOffsetMilli < nextLyricWord.start) {
                        krcProcess = (wordCnt+1)*1.0f/wordListSize;
                    }
                }
            }
        } else {
            krcProcess = 100f;
        }

        return krcProcess;
    }

    public static Lyric parseLyric(String lyricText, long rangeStart, long rangeEnd) {
        Lyric lyric = new Lyric();

        try {
            String[] lineArray = lyricText.contains("\r\n") ? lyricText.split("\r\n") : lyricText.split("\n");

            Pattern linePattern = Pattern.compile("\\[(\\d+),(\\d+)\\]");
            Pattern wordPattern = Pattern.compile("\\<(\\d+),(\\d+),(\\d+)\\>");

            for (String line : lineArray) {
                // Create a matcher for the input string
                Matcher lineMatcher = linePattern.matcher(line);

                // Find all matches
                if (lineMatcher.find()) {
                    LyricLine lyricLine = new LyricLine();
                    if (lineMatcher.groupCount() == 2) {
                        lyricLine.start = Long.parseLong(lineMatcher.group(1));
                        lyricLine.duration = Long.parseLong(lineMatcher.group(2));
                        lyricLine.content = line.replace(lineMatcher.group(0), "");
                    }
                    Matcher wordMatcher = wordPattern.matcher(line);
                    while (wordMatcher.find()) {
                        if (wordMatcher.groupCount() == 3) {
                            LyricWord lyricWord = new LyricWord();

                            lyricWord.start = Long.parseLong(wordMatcher.group(1));// - lyricLine.start;
                            lyricWord.duration = Long.parseLong(wordMatcher.group(2));
                            lyricLine.wordList.add(lyricWord);
                            lyricLine.content = lyricLine.content.replace(wordMatcher.group(0), "");
                            lyricLine.content = Html.fromHtml(lyricLine.content).toString();
                        }
                    }

                    if (lyricLine.start >= rangeStart &&
                            (rangeEnd == 0 ||(lyricLine.start+ lyricLine.duration) <= rangeEnd) ) {
                        lyric.lineList.add(lyricLine);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return lyric;
    }

    public static long getScrollDuration(Lyric lyric, int fromPosition, int toPosition) {
        long duration = 0l;
        try {
            LyricLine fromLineInfo = lyric.lineList.get(fromPosition);
            LyricLine toLineInfo = lyric.lineList.get(toPosition);
            duration = toLineInfo.start - (fromLineInfo.start + fromLineInfo.duration);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return duration > 0l ? duration : 0l;
    }
}
