package com.aliyun.auikits.karaoke.impl;

import static com.alivc.rtc.AliMusicContentCenter.MusicCacheState.MusicCacheStateDone;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alivc.rtc.AliMusicContentCenter;
import com.alivc.rtc.AliMusicContentCenterEventListener;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomMusicLibrary;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomMusicLibraryCallback;
import com.aliyun.auikits.karaoke.bean.KTVChartInfo;
import com.aliyun.auikits.karaoke.bean.KTVMusicInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ARTCKaraokeRoomMusicLibraryImpl implements AliMusicContentCenterEventListener, ARTCKaraokeRoomMusicLibrary  {

    private AliMusicContentCenter mAliMusicContentCenter = null;
    private List<KTVChartInfo> mKtvChartInfoList = null;
    private Map<String, KTVMusicInfo> mMusicInfoStorage = new HashMap<>();

    private Map<String, List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicChartCallback>> mMusicChartCallbackMap = new HashMap<>();
    private Map<String, List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback>> mMusicInfoListCallbackMap = new HashMap<>();
    private Map<String, List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback>> mSearchMusicInfoListCallbackMap = new HashMap<>();
    private Map<String, List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoCallback>> mMusicInfoCallbackMap = new HashMap<>();
    private Map<String, List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicLyricCallback>> mMusicLyricCallbackMap = new HashMap<>();
    private Map<String, List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicPitchCallback>> mMusicPitchCallbackMap = new HashMap<>();

    private ARTCKaraokeRoomMusicLibraryCallback mMusicLibraryCallback = null;
    private Looper mCallbackLooper;
    private Handler mCallbackHandler;

    private String mCachePath = "";

    public ARTCKaraokeRoomMusicLibraryImpl(@NonNull Looper looper, String cachePath) {
        mCachePath = cachePath;
        mCallbackLooper = looper;
        mCallbackHandler = new Handler(mCallbackLooper);
    }

    @Override
    public void fetchMusicChartList(ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicChartCallback callback) {
        ensureAliMusicContentCenter();
        String requestId = mAliMusicContentCenter.getMusicCharts();

        addCallback(requestId, callback);
    }

    @Override
    public void fetchMusicList(String chartId, int pageIndex, int pageSize, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback callback) {
        ensureAliMusicContentCenter();
        String requestId = mAliMusicContentCenter.getMusicCollectionByChartId(chartId, pageIndex, pageSize, null);

        addCallback(requestId, callback);
    }

    @Override
    public void fetchMusicInfo(String songId, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoCallback callback) {
        ensureAliMusicContentCenter();
        String requestId = mAliMusicContentCenter.getSongInfo(songId);

        addCallback(requestId, callback);
    }

    @Override
    public void fetchMusicLyric(String songId, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicLyricCallback callback) {
        ensureAliMusicContentCenter();
        String requestId = mAliMusicContentCenter.getLyric(songId);

        addCallback(requestId, callback);
    }

    @Override
    public void fetchMusicPitch(String songId, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicPitchCallback callback) {
        ensureAliMusicContentCenter();
        String requestId = mAliMusicContentCenter.getSongStandardPitch(songId);

        addCallback(requestId, callback);
    }

    @Override
    public void searchMusic(String key, int pageIndex, int pageSize, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback callback) {
        ensureAliMusicContentCenter();
        int[] vendorId = new int[0];
        String requestId = mAliMusicContentCenter.searchMusic(key, vendorId, 0, pageIndex, pageSize, null);

        addSearchCallback(requestId, callback);
    }

    @Override
    public void downloadMusic(String songId) {
        ensureAliMusicContentCenter();
        String requestId = mAliMusicContentCenter.getSongResource(songId);
    }

    @Override
    public boolean isMusicDownloaded(String songId) {
        ensureAliMusicContentCenter();
        return MusicCacheStateDone == mAliMusicContentCenter.getCacheState(songId);
    }

    @Override
    public void setDownloadCallback(ARTCKaraokeRoomMusicLibraryCallback callback) {
        mMusicLibraryCallback = callback;
    }

    @Override
    public void onError(AliMusicContentCenter.AliMusicContentCenterErrorCode errorCode) {
        Log.e("MusicRoomMusicLibrary", "onError: " + errorCode);
    }

    @Override
    public void onSongInfoResult(String requestId, String songId, AliMusicContentCenter.MusicInfo info, AliMusicContentCenter.AliMusicContentCenterErrorCode errorCode) {
        mCallbackHandler.post(new Runnable() {
            @Override
            public void run() {
                List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoCallback> callbackList = null;
                synchronized (this) {
                    callbackList = mMusicInfoCallbackMap.remove(requestId);
                }

                KTVMusicInfo ktvMusicInfo = convertMusicInfo(info);
                mMusicInfoStorage.put(songId, ktvMusicInfo);
                for (ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoCallback callback : callbackList) {
                    callback.onMusicInfoCallback(ktvMusicInfo);
                }
            }
        });
    }

    @Override
    public void onSongResourceProgress(String requestId, String songId, int progress) {

    }

    @Override
    public void onSongResourceResult(String requestId, String songId, AliMusicContentCenter.AliMusicContentCenterErrorCode errorCode) {
        mCallbackHandler.post(new Runnable() {
            @Override
            public void run() {
                // 根据songID从缓存列表中获取MusicInfo
                if (mMusicLibraryCallback != null) {
                    KTVMusicInfo ktvMusicInfo = mMusicInfoStorage.get(songId);
                    mMusicLibraryCallback.onMusicDownloadCompleted(ktvMusicInfo);
                }
            }
        });
    }

    @Override
    public void onSongStandardPitch(String requestId, String songId, String jsonPitch, int offset, int vtime, AliMusicContentCenter.AliMusicContentCenterErrorCode errorCode) {
        mCallbackHandler.post(new Runnable() {
            @Override
            public void run() {
                List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicPitchCallback> callbackList = null;
                synchronized (this) {
                    callbackList = mMusicPitchCallbackMap.remove(requestId);
                }

                for (ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicPitchCallback callback : callbackList) {
                    callback.onMusicPitchCallback(jsonPitch);
                }
            }
        });
    }

    @Override
    public void onLyricResult(String requestId, String songId, String lyric, AliMusicContentCenter.LyricType lyricType, AliMusicContentCenter.AliMusicContentCenterErrorCode errorCode) {
        mCallbackHandler.post(new Runnable() {
            @Override
            public void run() {
                List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicLyricCallback> callbackList = null;
                synchronized (this) {
                    callbackList = mMusicLyricCallbackMap.remove(requestId);
                }

                for (ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicLyricCallback callback : callbackList) {
                    callback.onMusicLyricCallback(lyric, convertLyricType(lyricType));
                }
            }
        });
    }

    @Override
    public void onMusicChartsResult(String requestId, String charts, AliMusicContentCenter.AliMusicContentCenterErrorCode errorCode) {
        mCallbackHandler.post(new Runnable() {
            @Override
            public void run() {
                // 将charts反序列化为数组
                List<KTVChartInfo> ktvChartInfoList = parseChartResultJson(charts);

                List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicChartCallback> callbackList = null;
                synchronized (this) {
                    callbackList = mMusicChartCallbackMap.remove(requestId);
                }

                mKtvChartInfoList = ktvChartInfoList;

                for (ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicChartCallback callback : callbackList) {
                    callback.onMusicChartCallback(ktvChartInfoList);
                }
            }
        });
    }

    @Override
    public void onMusicCollectionResult(String requestId, String chartId, AliMusicContentCenter.MusicInfo[] infos, int page, int pageSize, int total, AliMusicContentCenter.AliMusicContentCenterErrorCode errorCode) {
        mCallbackHandler.post(new Runnable() {
            @Override
            public void run() {
                List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback> callbackList = null;
                synchronized (this) {
                    callbackList = mMusicInfoListCallbackMap.remove(requestId);
                }

                List<KTVMusicInfo> musicInfoList = convertMusicInfoArray(infos);
                if (musicInfoList != null) {
                    for (int i = 0; i < musicInfoList.size(); i++) {
                        mMusicInfoStorage.put(infos[i].songID, musicInfoList.get(i));
                    }
                }
                for (ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback callback : callbackList) {
                    callback.onMusicInfoCallback(musicInfoList);
                }
            }
        });
    }

    @Override
    public void onSearchMusicResult(String requestId, AliMusicContentCenter.MusicInfo[] infos, int page, int pageSize, int total, AliMusicContentCenter.AliMusicContentCenterErrorCode errorCode) {
        mCallbackHandler.post(new Runnable() {
            @Override
            public void run() {
                List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback> callbackList = null;
                synchronized (this) {
                    callbackList = mSearchMusicInfoListCallbackMap.remove(requestId);
                }

                List<KTVMusicInfo> musicInfoList = convertMusicInfoArray(infos);
                if (musicInfoList != null) {
                    for (int i = 0; i < musicInfoList.size(); i++) {
                        mMusicInfoStorage.put(infos[i].songID, musicInfoList.get(i));
                    }
                }
                for (ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback callback : callbackList) {
                    callback.onMusicInfoCallback(musicInfoList);
                }
            }
        });
    }

    @Override
    public void destroy() {
        if (null != mAliMusicContentCenter) {
            mAliMusicContentCenter.unInitialize();
        }
    }

    private void addCallback(String requestId, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicChartCallback callback) {
        synchronized (mMusicChartCallbackMap) {
            List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicChartCallback> callbackList = null;
            if (mMusicChartCallbackMap.containsKey(requestId)) {
                callbackList = mMusicChartCallbackMap.get(requestId);
            } else {
                callbackList = new ArrayList<>();
                mMusicChartCallbackMap.put(requestId, callbackList);
            }
            callbackList.add(callback);
        }
    }

    private void addCallback(String requestId, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback callback) {
        synchronized (mMusicInfoListCallbackMap) {
            List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback> callbackList = null;
            if (mMusicInfoListCallbackMap.containsKey(requestId)) {
                callbackList = mMusicInfoListCallbackMap.get(requestId);
            } else {
                callbackList = new ArrayList<>();
                mMusicInfoListCallbackMap.put(requestId, callbackList);
            }
            callbackList.add(callback);
        }
    }

    private void addSearchCallback(String requestId, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback callback) {
        synchronized (mSearchMusicInfoListCallbackMap) {
            List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback> callbackList = null;
            if (mSearchMusicInfoListCallbackMap.containsKey(requestId)) {
                callbackList = mSearchMusicInfoListCallbackMap.get(requestId);
            } else {
                callbackList = new ArrayList<>();
                mSearchMusicInfoListCallbackMap.put(requestId, callbackList);
            }
            callbackList.add(callback);
        }
    }

    private void addCallback(String requestId, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoCallback callback) {
        synchronized (mMusicInfoCallbackMap) {
            List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoCallback> callbackList = null;
            if (mMusicInfoCallbackMap.containsKey(requestId)) {
                callbackList = mMusicInfoCallbackMap.get(requestId);
            } else {
                callbackList = new ArrayList<>();
                mMusicInfoCallbackMap.put(requestId, callbackList);
            }
            callbackList.add(callback);
        }
    }

    private void addCallback(String requestId, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicLyricCallback callback) {
        synchronized (mMusicLyricCallbackMap) {
            List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicLyricCallback> callbackList = null;
            if (mMusicLyricCallbackMap.containsKey(requestId)) {
                callbackList = mMusicLyricCallbackMap.get(requestId);
            } else {
                callbackList = new ArrayList<>();
                mMusicLyricCallbackMap.put(requestId, callbackList);
            }
            callbackList.add(callback);
        }
    }

    private void addCallback(String requestId, ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicPitchCallback callback) {
        synchronized (mMusicPitchCallbackMap) {
            List<ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicPitchCallback> callbackList = null;
            if (mMusicPitchCallbackMap.containsKey(requestId)) {
                callbackList = mMusicPitchCallbackMap.get(requestId);
            } else {
                callbackList = new ArrayList<>();
                mMusicPitchCallbackMap.put(requestId, callbackList);
            }
            callbackList.add(callback);
        }
    }

    private List<KTVMusicInfo> convertMusicInfoArray(AliMusicContentCenter.MusicInfo[] musicInfoArray) {
        List<KTVMusicInfo> ktvMusicInfoList = new ArrayList<>();

        if (null != musicInfoArray && musicInfoArray.length > 0) {
            for (AliMusicContentCenter.MusicInfo musicInfo : musicInfoArray) {
                ktvMusicInfoList.add(convertMusicInfo(musicInfo));
            }
        }

        return ktvMusicInfoList;
    }

    private List<KTVMusicInfo> convertMusicInfoList(List<AliMusicContentCenter.MusicInfo> musicInfoList) {
        List<KTVMusicInfo> ktvMusicInfoList = new ArrayList<>();

        if (null != musicInfoList && musicInfoList.size() > 0) {
            for (AliMusicContentCenter.MusicInfo musicInfo : musicInfoList) {
                ktvMusicInfoList.add(convertMusicInfo(musicInfo));
            }
        }

        return ktvMusicInfoList;
    }

    private KTVMusicInfo convertMusicInfo(AliMusicContentCenter.MusicInfo musicInfo) {
        KTVMusicInfo ktvMusicInfo = new KTVMusicInfo();

        ktvMusicInfo.songID = musicInfo.songID;
        ktvMusicInfo.songName = musicInfo.songName;
        ktvMusicInfo.releaseTime = musicInfo.releaseTime;
        ktvMusicInfo.vendorId = musicInfo.vendorId;
        ktvMusicInfo.singerName = musicInfo.singerName;
        ktvMusicInfo.singerImg = musicInfo.singerImg;
        ktvMusicInfo.albumName = musicInfo.albumName;
        ktvMusicInfo.albumImg = musicInfo.albumImg;
        ktvMusicInfo.duration = musicInfo.duration;
        ktvMusicInfo.accompanyDuration = musicInfo.accompanyDuration;
        ktvMusicInfo.lyricType = convertLyricType(musicInfo.lyricType);
        ktvMusicInfo.hasClip = musicInfo.hasClip;
        ktvMusicInfo.hasShortSegment = musicInfo.hasShortSegment;
        ktvMusicInfo.hasStandardPitch = musicInfo.hasStandardPitch;
        ktvMusicInfo.remoteUrl = "";
        ktvMusicInfo.localPath = "";

        return ktvMusicInfo;
    }

    private KTVMusicInfo.KTVLyricType convertLyricType(AliMusicContentCenter.LyricType lyricType) {
        return KTVMusicInfo.KTVLyricType.fromIndex(lyricType.getValue());
    }

    private void ensureAliMusicContentCenter() {
        if (mAliMusicContentCenter == null) {
            mAliMusicContentCenter = AliMusicContentCenter.create();
            AliMusicContentCenter.AliMusicContentCenterConfiguration contentCenterConfiguration = new AliMusicContentCenter.AliMusicContentCenterConfiguration();
            contentCenterConfiguration.maxCache = 20;
            contentCenterConfiguration.cacheDir = mCachePath;
            mAliMusicContentCenter.initialize(this, contentCenterConfiguration, "");
            mAliMusicContentCenter.registerEventHandler(this);
        }
    }

    private List<KTVChartInfo> parseChartResultJson(String chartJson) {
        List<KTVChartInfo> ktvChartInfoList = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(chartJson);
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                    KTVChartInfo ktvChartInfo = new KTVChartInfo();
                    ktvChartInfo.chartId = jsonObject.optString("top_id");
                    ktvChartInfo.chartName = jsonObject.optString("name");
                    ktvChartInfoList.add(ktvChartInfo);
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return ktvChartInfoList;
    }
}
