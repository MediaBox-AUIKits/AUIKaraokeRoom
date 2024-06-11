package com.aliyun.auikits.karaoke.room.util;

public class DisplayTextUtil {
    public static String formatDuration(long durationInMillis) {
        String formatDuration = "";

        long seconds = durationInMillis / 1000;
        int secondsStr = (int)(seconds%60);

        long minutes = seconds / 60;
        int minutesStr = (int)(minutes%60);

        int hoursStr = (int)(minutes / 60);

        if (hoursStr > 0) {
            formatDuration = String.format("%02d:%02d:%02d", hoursStr, minutesStr, secondsStr);
        } else {
            formatDuration = String.format("%02d:%02d", minutesStr, secondsStr);
        }

        return formatDuration;
    }
}
