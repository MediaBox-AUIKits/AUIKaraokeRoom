package com.aliyun.auikits.karaoke.impl;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.aliyun.auikits.karaoke.ARTCKaraokeRoomActionCallback;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomActionFailCallback;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomMusicPlayingListService;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomMusicPlayingListServiceCallback;
import com.aliyun.auikits.karaoke.bean.KTVMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;
import com.aliyun.auikits.voiceroom.bean.RoomInfo;
import com.aliyun.auikits.voiceroom.module.seat.SeatInfo;
import com.aliyun.auikits.voiceroom.module.seat.protocol.Params;
import com.aliyun.auikits.voiceroom.network.HttpRequest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ARTCKaraokeRoomMusicPlayingListServiceImpl implements ARTCKaraokeRoomMusicPlayingListService {
    private static final String HOST = "https://ktv.h5video.vip";

    private static final String SELECT_SONG_URL = HOST + "/api/ktv/selectSong";
    private static final String DELETE_SONG_URL = HOST + "/api/ktv/deleteSong";
    private static final String PLAY_SONG_URL = HOST + "/api/ktv/playSong";
    private static final String PIN_SONG_URL = HOST + "/api/ktv/pinSong";
    private static final String LIST_SONGS_URL = HOST + "/api/ktv/listSongs";
    private static final String JOIN_SINGING_URL = HOST + "/api/ktv/joinInSinging";
    private static final String LEAVE_SINGING_URL = HOST + "/api/ktv/leaveSinging";
    private static final String GET_SINGING_JOINER_LIST_URL = HOST + "/api/ktv/getSinging"; // 合唱信息列表

    private static final String PARAMS_KEY_ROOM_ID = "room_id";
    private static final String PARAMS_KEY_CODE = "code";
    private static final String PARAMS_KEY_SONGS = "songs";
    private static final String PARAMS_KEY_SONG_ID = "song_id";
    private static final String PARAMS_KEY_USER_ID = "user_id";
    private static final String PARAMS_KEY_USER_EXTENDS = "user_extends";
    private static final String PARAMS_KEY_STATUS = "status";
    private static final String PARAMS_KEY_SONG_EXTENDS = "song_extends";
    private static final String PARAMS_KEY_ID = "id";
    private static final String PARAMS_KEY_SONG_NAME = "song_name";
    private static final String PARAMS_KEY_SINGER_NAME = "singer_name";
    private static final String PARAMS_KEY_ALBUM_IMG = "album_img";
    private static final String PARAMS_KEY_REMOTE_URL = "remote_url";
    private static final String PARAMS_KEY_DURATION = "duration";
    private static final String PARAMS_KEY_SONG_IDS = "song_ids";
    private static final String PARAMS_KEY_OPERATOR = "operator";
    private static final String PARAMS_KEY_SUCCESS = "success";
    private static final String PARAMS_KEY_REASON = "reason";
    private static final String PARAMS_KEY_DESC = "desc";
    private static final String PARAMS_KEY_MEMBERS = "members";
    private static final String PARAMS_KEY_JOIN_TIME = "join_time";

    private String mToken;
    private Looper mCallbackLooper;
    private Handler mCallbackHandler;
    private Map<String, String> mHeaders;
    private List<KTVPlayingMusicInfo> mCachedPlayingMusicInfoList;

    public ARTCKaraokeRoomMusicPlayingListServiceImpl(String serverToken, @NonNull Looper looper) {
        mCallbackLooper = looper;
        mCallbackHandler = new Handler(mCallbackLooper);
        this.mToken = serverToken;
        this.mHeaders = new Hashtable<>();
        mHeaders.put("Authorization", mToken);
    }

    @Override
    public void fetchMusicPlayingList(@NonNull RoomInfo roomInfo, ARTCKaraokeRoomMusicPlayingListServiceCallback callback) {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put(PARAMS_KEY_ROOM_ID, roomInfo.roomId);

            HttpRequest.getInstance().post(LIST_SONGS_URL, mHeaders, jsonObj, new CommonRequestHandler(callback) {
                @Override
                public void onSuccessResponse(JSONObject jsonObject) {
                    super.onSuccessResponse(jsonObject);
                    JSONArray songJsonArray = jsonObject.optJSONArray(PARAMS_KEY_SONGS);
                    List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList = parseKtvPlayingMusicInfoList(songJsonArray);
                    mCallbackHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setCachedMusicPlayingList(ktvPlayingMusicInfoList);
                            callback.onMusicPlayingListCallback(ktvPlayingMusicInfoList);
                        }
                    });
                }
            });
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void addMusic(@NonNull RoomInfo roomInfo, KTVMusicInfo musicInfo, SeatInfo seatInfo, ARTCKaraokeRoomActionCallback callback) {
        try {
            if (null != roomInfo && null != musicInfo && null != seatInfo) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(PARAMS_KEY_USER_ID, seatInfo.userId);
                jsonObject.put(PARAMS_KEY_SONG_ID, musicInfo.songID);
                jsonObject.put(PARAMS_KEY_ROOM_ID, roomInfo.roomId);
                jsonObject.put(PARAMS_KEY_USER_EXTENDS, serializeSeatInfo(seatInfo));
                jsonObject.put(PARAMS_KEY_SONG_EXTENDS, serializeKTVMusicInfo(musicInfo));

                HttpRequest.getInstance().post(SELECT_SONG_URL, mHeaders, jsonObject, new ActionRequestHandler(callback));
            } else {
                callback.onFail(-1, "param error with [roomInfo: " + roomInfo + ", musicInfo: " + musicInfo + ", seatInfo: " + seatInfo);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void removeMusic(@NonNull RoomInfo roomInfo, List<String> songIdList, SeatInfo seatInfo, SeatInfo actionSeatInfo, ARTCKaraokeRoomActionCallback callback) {
        try {
            if (null != roomInfo && null != songIdList && null != seatInfo && null != actionSeatInfo) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(PARAMS_KEY_ROOM_ID, roomInfo.roomId);
                jsonObject.put(PARAMS_KEY_USER_ID, seatInfo.userId);
                StringBuilder songIdsBuilder = new StringBuilder();
                for (String songId : songIdList) {
                    if (songIdsBuilder.length() > 0) {
                        songIdsBuilder.append(",");
                    }
                    songIdsBuilder.append(songId);
                }
                jsonObject.put(PARAMS_KEY_SONG_IDS, songIdsBuilder.toString());
                jsonObject.put(PARAMS_KEY_OPERATOR, actionSeatInfo.userId);

                HttpRequest.getInstance().post(DELETE_SONG_URL, mHeaders, jsonObject, new ActionRequestHandler(callback));
            } else {
                callback.onFail(-1, "param error with [roomInfo: " + roomInfo + ", actionSeatInfo: " + actionSeatInfo + ", seatInfo: " + seatInfo);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void pinMusic(@NonNull RoomInfo roomInfo, String songId, @NonNull SeatInfo seatInfo, @NonNull SeatInfo actionSeatInfo, ARTCKaraokeRoomActionCallback callback) {
        try {
            if (null != roomInfo && null != seatInfo && null != actionSeatInfo) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(PARAMS_KEY_USER_ID, seatInfo.userId);
                jsonObject.put(PARAMS_KEY_SONG_ID, songId);
                jsonObject.put(PARAMS_KEY_ROOM_ID, roomInfo.roomId);
                jsonObject.put(PARAMS_KEY_OPERATOR, actionSeatInfo.userId);

                HttpRequest.getInstance().post(PIN_SONG_URL, mHeaders, jsonObject, new ActionRequestHandler(callback));
            } else {
                callback.onFail(-1, "param error with [roomInfo: " + roomInfo + ", actionSeatInfo: " + actionSeatInfo + ", seatInfo: " + seatInfo);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void playMusic(@NonNull RoomInfo roomInfo, @NonNull KTVMusicInfo musicInfo, @NonNull SeatInfo seatInfo, @NonNull SeatInfo actionSeatInfo, ARTCKaraokeRoomActionCallback callback) {
        try {
            if (null != roomInfo && null != actionSeatInfo) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(PARAMS_KEY_USER_ID, null != seatInfo ? seatInfo.userId : "");
                jsonObject.put(PARAMS_KEY_SONG_ID, null != musicInfo ? musicInfo.songID : "");
                jsonObject.put(PARAMS_KEY_ROOM_ID, roomInfo.roomId);
                jsonObject.put(PARAMS_KEY_OPERATOR, actionSeatInfo.userId);

                HttpRequest.getInstance().post(PLAY_SONG_URL, mHeaders, jsonObject, new ActionRequestHandler(callback));
            } else {
                callback.onFail(-1, "param error with [roomInfo: " + roomInfo + ", musicInfo: " + musicInfo + ", actionSeatInfo: " + actionSeatInfo + ", seatInfo: " + seatInfo);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void joinSinging(@NonNull RoomInfo roomInfo, @NonNull KTVMusicInfo musicInfo, @NonNull SeatInfo seatInfo, ARTCKaraokeRoomActionCallback callback) {
        try {
            if (null != roomInfo && null != seatInfo && null != musicInfo) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(PARAMS_KEY_USER_ID, seatInfo.userId);
                jsonObject.put(PARAMS_KEY_SONG_ID, musicInfo.songID);
                jsonObject.put(PARAMS_KEY_ROOM_ID, roomInfo.roomId);

                HttpRequest.getInstance().post(JOIN_SINGING_URL, mHeaders, jsonObject, new ActionRequestHandler(callback));
            } else {
                callback.onFail(-1, "param error with [roomInfo: " + roomInfo + ", musicInfo: " + musicInfo + ", seatInfo: " + seatInfo);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void leaveSinging(@NonNull RoomInfo roomInfo, @NonNull KTVMusicInfo musicInfo, @NonNull SeatInfo seatInfo, ARTCKaraokeRoomActionCallback callback) {
        try {
            if (null != roomInfo && null != seatInfo && null != musicInfo) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(PARAMS_KEY_USER_ID, seatInfo.userId);
                jsonObject.put(PARAMS_KEY_SONG_ID, musicInfo.songID);
                jsonObject.put(PARAMS_KEY_ROOM_ID, roomInfo.roomId);

                HttpRequest.getInstance().post(LEAVE_SINGING_URL, mHeaders, jsonObject, new ActionRequestHandler(callback));
            } else {
                callback.onFail(-1, "param error with [roomInfo: " + roomInfo + ", musicInfo: " + musicInfo + ", seatInfo: " + seatInfo);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void fetchJoinerList(@NonNull RoomInfo roomInfo, @NonNull KTVPlayingMusicInfo musicInfo, ARTCKaraokeRoomMusicPlayingListServiceCallback callback) {
        try {
            if (null != roomInfo && null != musicInfo) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put(PARAMS_KEY_ROOM_ID, roomInfo.roomId);
                jsonObj.put(PARAMS_KEY_SONG_ID, musicInfo.songID);

                HttpRequest.getInstance().post(GET_SINGING_JOINER_LIST_URL, mHeaders, jsonObj, new CommonRequestHandler(callback) {
                    @Override
                    public void onSuccessResponse(JSONObject jsonObject) {
                        super.onSuccessResponse(jsonObject);

                        JSONArray memberJsonArray = jsonObject.optJSONArray(PARAMS_KEY_MEMBERS);
                        musicInfo.joinSingUserIdList = new ArrayList<>();
                        if (null != memberJsonArray && memberJsonArray.length() > 0) {
                            for (int i = 0; i < memberJsonArray.length(); i++) {
                                JSONObject memberJson = memberJsonArray.optJSONObject(i);
                                String joinedSingingUserId = memberJson.optString(PARAMS_KEY_USER_ID);
                                long joinedTime = memberJson.optLong(PARAMS_KEY_JOIN_TIME);
                                musicInfo.joinSingUserIdList.add(joinedSingingUserId);
                            }
                        }
                        mCallbackHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onMusicPlayingJoinerListCallback(musicInfo);
                            }
                        });
                    }
                });
            } else {
                callback.onFail(-1, "param error with [roomInfo: " + roomInfo + ", musicInfo: " + musicInfo);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized KTVPlayingMusicInfo getCurrentPlayingMusicInfo() {
        return getPlayingMusicInfo(0);
    }

    @Override
    public synchronized KTVPlayingMusicInfo getPlayingMusicInfo(int index) {
        if (null != mCachedPlayingMusicInfoList && index >= 0 && index < mCachedPlayingMusicInfoList.size()) {
            return mCachedPlayingMusicInfoList.get(index);
        }
        return null;
    }

    @Override
    public synchronized List<KTVPlayingMusicInfo> getCachedMusicPlayingList() {
        return mCachedPlayingMusicInfoList;
    }

    @Override
    public void destroy() {
        
    }

    private synchronized void setCachedMusicPlayingList(List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList) {
        mCachedPlayingMusicInfoList = ktvPlayingMusicInfoList;
    }

    private class ActionRequestHandler extends CommonRequestHandler {
        ARTCKaraokeRoomActionCallback mCallback = null;
        public ActionRequestHandler(@NotNull ARTCKaraokeRoomActionCallback callback) {
            super(callback);
            mCallback = callback;
        }

        @Override
        public void onSuccessResponse(JSONObject jsonObject) {
            super.onSuccessResponse(jsonObject);
            int reason = jsonObject.optInt(PARAMS_KEY_REASON, 0);
            boolean success = jsonObject.optBoolean(PARAMS_KEY_SUCCESS, true);
            String desc = jsonObject.optString(PARAMS_KEY_DESC);

            if (success) {
                notifySuccess();
            } else {
                notifyFail(-1, "server callback reason: " + reason + ", desc: " + desc);
            }
        }

        protected void notifySuccess() {
            mCallbackHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (null != mCallback) {
                        mCallback.onSuccess();
                    }
                }
            });
        }
    }

    private abstract class CommonRequestHandler implements Callback {
        ARTCKaraokeRoomActionFailCallback mFailCallback = null;

        public CommonRequestHandler(@NotNull ARTCKaraokeRoomActionFailCallback callback) {
            mFailCallback = callback;
        }

        protected void notifyFail(int errorCode, String errorMsg) {
            mCallbackHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (null != mFailCallback) {
                        mFailCallback.onFail(errorCode, errorMsg);
                    }
                }
            });
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            notifyFail(-1, "http exception: " + e.getMessage());
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) {
            if (response.code() != 200) {
                notifyFail(-1, "http code: " + response.code());
            } else {
                try {
                    JSONObject resp = new JSONObject(response.body().string());
                    int code = resp.optInt(PARAMS_KEY_CODE);
                    if (code == 200) {
                        onSuccessResponse(resp);
                    } else {
                        notifyFail(-1, "server error code: " + code);
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    notifyFail(-1, "json except: " + e.getMessage());
                    return;
                }
            }
        }

        public void onSuccessResponse(JSONObject jsonObject) {

        }
    }

    private static String serializeSeatInfo(SeatInfo seatInfo) {
        JSONObject jsonObj = new JSONObject();
        try {
            JSONObject extendObj = new JSONObject();
            extendObj.put(Params.KEY_USER_NICK, seatInfo.userName);
            extendObj.put(Params.KEY_USER_AVATAR, seatInfo.userAvatar);
            jsonObj.put(Params.KEY_ID, seatInfo.roomId);
            jsonObj.put(Params.KEY_USER_ID, seatInfo.userId);
            jsonObj.put(Params.KEY_EXTENDS, extendObj.toString());
            jsonObj.put(Params.KEY_INDEX, seatInfo.seatIndex);
            jsonObj.put(Params.KEY_JOINED, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObj.toString();
    }

    private static SeatInfo parseSeatInfo(String seatInfoStr) {
        SeatInfo seatInfo = null;
        try {
            JSONObject jsonObject = new JSONObject(seatInfoStr);
            String roomID = jsonObject.optString(Params.KEY_ID);
            String userId = jsonObject.optString(Params.KEY_USER_ID);
            int seatIndex = jsonObject.optInt(Params.KEY_INDEX);
            JSONObject extendJsonObject = new JSONObject(jsonObject.optString(Params.KEY_EXTENDS));
            String userName = extendJsonObject.optString(Params.KEY_USER_NICK);
            String userAvatar = extendJsonObject.optString(Params.KEY_USER_AVATAR);

            seatInfo = new SeatInfo();
            seatInfo.roomId = roomID;
            seatInfo.userId = userId;
            seatInfo.seatIndex = seatIndex;
            seatInfo.userName = userName;
            seatInfo.userAvatar = userAvatar;
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return seatInfo;
    }

    private static String serializeKTVMusicInfo(KTVMusicInfo ktvMusicInfo) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(PARAMS_KEY_ID, ktvMusicInfo.songID);
            jsonObj.put(PARAMS_KEY_SONG_NAME, ktvMusicInfo.songName);
            jsonObj.put(PARAMS_KEY_SINGER_NAME, ktvMusicInfo.singerName);
            jsonObj.put(PARAMS_KEY_ALBUM_IMG, ktvMusicInfo.albumImg);
            jsonObj.put(PARAMS_KEY_REMOTE_URL, ktvMusicInfo.remoteUrl);
            jsonObj.put(PARAMS_KEY_DURATION, ktvMusicInfo.duration);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObj.toString();
    }

    private static void parseKtvMusicInfo(String ktvMusicInfoStr, KTVPlayingMusicInfo ktvPlayingMusicInfo) {
        try {
            JSONObject jsonObject = new JSONObject(ktvMusicInfoStr);
            ktvPlayingMusicInfo.songID = jsonObject.optString(PARAMS_KEY_ID);
            ktvPlayingMusicInfo.songName = jsonObject.optString(PARAMS_KEY_SONG_NAME);
            ktvPlayingMusicInfo.singerName = jsonObject.optString(PARAMS_KEY_SINGER_NAME);
            ktvPlayingMusicInfo.albumImg = jsonObject.optString(PARAMS_KEY_ALBUM_IMG);
            ktvPlayingMusicInfo.remoteUrl = jsonObject.optString(PARAMS_KEY_REMOTE_URL);
            ktvPlayingMusicInfo.duration = jsonObject.optInt(PARAMS_KEY_DURATION);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private static KTVPlayingMusicInfo parseKtvPlayingMusicInfo(JSONObject jsonObject) {
        KTVPlayingMusicInfo ktvPlayingMusicInfo = new KTVPlayingMusicInfo();
        String songId = jsonObject.optString(PARAMS_KEY_SONG_ID);
        int userId = jsonObject.optInt(PARAMS_KEY_USER_ID);
        int status = jsonObject.optInt(PARAMS_KEY_STATUS);

        ktvPlayingMusicInfo.status = status;
        ktvPlayingMusicInfo.seatInfo = parseSeatInfo(jsonObject.optString(PARAMS_KEY_USER_EXTENDS));
        parseKtvMusicInfo(jsonObject.optString(PARAMS_KEY_SONG_EXTENDS), ktvPlayingMusicInfo);

        return ktvPlayingMusicInfo;
    }

    private static List<KTVPlayingMusicInfo> parseKtvPlayingMusicInfoList(JSONArray jsonArray) {
        List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList = new ArrayList<>();
        if (null != jsonArray && jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                KTVPlayingMusicInfo ktvPlayingMusicInfo = parseKtvPlayingMusicInfo(jsonArray.optJSONObject(i));
                ktvPlayingMusicInfoList.add(ktvPlayingMusicInfo);
            }
        }
        return ktvPlayingMusicInfoList;
    }
}
