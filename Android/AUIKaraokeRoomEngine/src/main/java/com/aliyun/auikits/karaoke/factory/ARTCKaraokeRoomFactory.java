package com.aliyun.auikits.karaoke.factory;

import android.os.Looper;

import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.aliyun.auikits.karaoke.impl.ARTCKaraokeRoomControllerImpl;

public class ARTCKaraokeRoomFactory {
    public static ARTCKaraokeRoomController createKaraokeRoom(){
        return createKaraokeRoom(Looper.getMainLooper());
    }

    public static ARTCKaraokeRoomController createKaraokeRoom(Looper looper){
        return new ARTCKaraokeRoomControllerImpl(looper);
    }
}
