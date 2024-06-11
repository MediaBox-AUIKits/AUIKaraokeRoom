package com.aliyun.auikits.karaoke.room.service;


import android.util.Log;

import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.aliyun.auikits.karaoke.factory.ARTCKaraokeRoomFactory;

import java.util.concurrent.ConcurrentHashMap;

public class KTVRoomManager {
    private static final String TAG = "KTVRoomManager";
    public static final int CODE_SUCCESS = 0;
    private ConcurrentHashMap<String, Object> globalParams = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ARTCKaraokeRoomController> roomMap = new ConcurrentHashMap<>();

    private KTVRoomManager() {

    }

    private static class KTVRoomManagerInstance {
        private static KTVRoomManager instance = new KTVRoomManager();
    }

    public static KTVRoomManager getInstance() {
        return KTVRoomManagerInstance.instance;
    }

    public ARTCKaraokeRoomController createKTVRoomController(String roomId) {
        ARTCKaraokeRoomController room = this.roomMap.get(roomId);
        if(room == null) {
            room = ARTCKaraokeRoomFactory.createKaraokeRoom();

            Log.v(TAG, "create RoomController :" + room.hashCode());
            this.roomMap.put(roomId, room);
        } else {
            Log.v(TAG, "hit cache RoomController :" + roomId);
        }

        return room;
    }

    public ARTCKaraokeRoomController getKTVRoomController(String roomId) {
        return this.roomMap.get(roomId);
    }

    public void destroyKTVRoomController(ARTCKaraokeRoomController roomController) {
        destroyKTVRoomController(roomController.getRoomInfo().roomId);
    }

    public void destroyKTVRoomController(String roomId) {
        ARTCKaraokeRoomController ktvRoomController = getKTVRoomController(roomId);
        if(ktvRoomController != null) {
            ktvRoomController.release();
            Log.v(TAG, "destroy RoomController :" + ktvRoomController.hashCode());
            this.roomMap.remove(roomId);
        }
    }

    public void destroyAllKTVRoomController() {
        for(ARTCKaraokeRoomController room : this.roomMap.values()) {
            room.release();
        }
        this.roomMap.clear();
    }

    public void addGlobalParam(String key, Object value) {
        globalParams.put(key, value);
    }

    public Object getGlobalParam(String key) {
        return globalParams.get(key);
    }

    public void destroy() {
        for(ARTCKaraokeRoomController room : this.roomMap.values()) {
            room.release();
        }
        this.roomMap.clear();
        this.globalParams.clear();
    }

}
