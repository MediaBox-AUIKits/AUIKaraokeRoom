package com.aliyun.auikits.karaoke.room.util;

import android.text.TextUtils;

public class AvatarUtil {


    public static String getAvatarUrl(String user_id) {
        String [] avatarArray = new String[] {
                "https://img.alicdn.com/imgextra/i2/O1CN012r3m2f1DnaInMUyuY_!!6000000000261-0-tps-174-174.jpg",
                "https://img.alicdn.com/imgextra/i3/O1CN01wYpdpF1EQ3ZRm4pU2_!!6000000000345-0-tps-174-174.jpg",
                "https://img.alicdn.com/imgextra/i3/O1CN01syybHf1mmDfctPVxi_!!6000000004996-0-tps-174-174.jpg",
                "https://img.alicdn.com/imgextra/i2/O1CN01DqPptn227whKyXV2T_!!6000000007074-0-tps-174-174.jpg",
                "https://img.alicdn.com/imgextra/i2/O1CN01cQ2kjC25nZXgsYisr_!!6000000007571-0-tps-174-174.jpg",
                "https://img.alicdn.com/imgextra/i2/O1CN01VYKjf81Z1savmhLOr_!!6000000003135-0-tps-174-174.jpg",
                "https://img.alicdn.com/imgextra/i2/O1CN01Gyhm5f1tHGD4YItli_!!6000000005876-0-tps-174-174.jpg",
                "https://img.alicdn.com/imgextra/i4/O1CN012mogEX1WyYUM6nR6N_!!6000000002857-0-tps-174-174.jpg",
                "https://img.alicdn.com/imgextra/i1/O1CN01MMJAGR1XqbDvsquJG_!!6000000002975-0-tps-174-174.jpg"
        };
        if(TextUtils.isEmpty(user_id)) {
            return avatarArray[0];
        }
        int firstLetter = user_id.charAt(0);
        int index = firstLetter % 9;

        return avatarArray[index];
    }
}
