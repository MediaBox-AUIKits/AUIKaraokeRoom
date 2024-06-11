//package com.aliyun.auikits.ktv.util;
//
//import android.content.Context;
//import android.text.TextUtils;
//
//import androidx.annotation.NonNull;
//
//import com.alivc.auicommon.common.base.util.CommonUtil;
//import com.alivc.auimessage.model.token.IMNewToken;
//import com.alivc.auimessage.model.token.IMNewTokenAuth;
//import com.alivc.rtc.AliRtcEngine;
//import com.aliyun.auikits.ktv.model.entity.ChatMember;
//import com.aliyun.auikits.ktv.model.entity.ChatRoomItem;
//import com.aliyun.auikits.ktv.model.entity.KtvChart;
//import com.aliyun.auikits.ktv.model.entity.KtvChosenSong;
//import com.aliyun.auikits.ktv.model.entity.KtvSong;
//import com.aliyun.auikits.ktvroom.ARTCKaraokeRoomActionCallback;
//import com.aliyun.auikits.ktvroom.ARTCKaraokeRoomController;
//import com.aliyun.auikits.ktvroom.ARTCKaraokeRoomEngine;
//import com.aliyun.auikits.ktvroom.ARTCKaraokeRoomEngineCallback;
//import com.aliyun.auikits.ktvroom.ARTCKaraokeRoomMusicLibrary;
//import com.aliyun.auikits.ktvroom.ARTCKaraokeRoomMusicLibraryCallback;
//import com.aliyun.auikits.ktvroom.ARTCKaraokeRoomMusicPlayingListService;
//import com.aliyun.auikits.ktvroom.ARTCKaraokeRoomMusicPlayingListServiceCallback;
//import com.aliyun.auikits.ktvroom.bean.KTVMusicConfig;
//import com.aliyun.auikits.ktvroom.bean.KTVMusicInfo;
//import com.aliyun.auikits.ktvroom.bean.KTVPlayingMusicInfo;
//import com.aliyun.auikits.ktvroom.bean.KTVSingerRole;
//import com.aliyun.auikits.voice.ARTCVoiceRoomEngineDelegate;
//import com.aliyun.auikits.voice.AudioOutputType;
//import com.aliyun.auikits.voiceroom.bean.AudioEffect;
//import com.aliyun.auikits.voiceroom.bean.MicInfo;
//import com.aliyun.auikits.voiceroom.bean.MixSound;
//import com.aliyun.auikits.voiceroom.bean.Music;
//import com.aliyun.auikits.voiceroom.bean.RoomInfo;
//import com.aliyun.auikits.voiceroom.bean.RoomState;
//import com.aliyun.auikits.voiceroom.bean.UserInfo;
//import com.aliyun.auikits.voiceroom.bean.VoiceChange;
//import com.aliyun.auikits.voiceroom.callback.ActionCallback;
//import com.aliyun.auikits.voiceroom.external.RtcInfo;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//public class MockUtil {
//    public static String getFormatTime() {
//        return CommonUtil.parseFormatString(System.currentTimeMillis(), "YYmmDD_HHmmSS");
//    }
//
//    public static long sChartId = 1;
//    public static KtvChart mockKtvChart() {
//        KtvChart ktvChart = new KtvChart();
//        ktvChart.setChartId("ktv_chart_id_"+sChartId);
//        ktvChart.setChartName("ktv_chart_name_"+sChartId);
//        sChartId++;
//        return ktvChart;
//    }
//
//    public static long sSongId = 1;
//    public static KtvSong mockKtvSong(String songNamePrefix) {
//        KtvSong ktvSong = new KtvSong();
//        ktvSong.setSongId("ktv_song_id_"+sSongId);
//        if (TextUtils.isEmpty(songNamePrefix)) {
//            songNamePrefix = "ktv_song_name_";
//        }
//        ktvSong.setSongName(songNamePrefix+sSongId);
//        sSongId++;
//        ktvSong.setAlbumUrl("https://image.uisdc.com/wp-content/uploads/2018/12/uisdc-jl-20181224-36.jpg");
//        ktvSong.setSingerName("周饼伦");
//        ktvSong.setDurationInMillis(234000);
//        return ktvSong;
//    }
//
//    public static KtvChosenSong mockKtvChosenSong(int songOrder, boolean isPlaying) {
//        KtvChosenSong ktvChosenSong = new KtvChosenSong();
//        ktvChosenSong.setSongInfo(mockKtvSong(null));
//        ktvChosenSong.setSongOrder(songOrder);
//        ktvChosenSong.setIsPlaying(isPlaying);
////        ktvChosenSong.setChosenMember(mockChatMember(""));
//        return ktvChosenSong;
//    }
//
//    public static String mockAvatarUrl() {
//        return "https://img.alicdn.com/imgextra/i4/O1CN012mogEX1WyYUM6nR6N_!!6000000002857-0-tps-174-174.jpg";
//    }
//
//    public static String mockAuthorization() {
//        return "authorization_mock";
//    }
//
//    public static IMNewToken mockIMNewToken() {
//        IMNewToken imNewToken = new IMNewToken();
//        imNewToken.app_id = "app_id_mock";
//        imNewToken.app_sign = "app_sign_mock";
//        imNewToken.app_token = "app_token_mock";
//        imNewToken.auth = mockIMNewTokenAuth();
//        return imNewToken;
//    }
//
//    public static IMNewTokenAuth mockIMNewTokenAuth() {
//        IMNewTokenAuth auth = new IMNewTokenAuth();
//        auth.user_id = "user_id_mock";
//        auth.nonce = "noce_mock";
//        auth.role = "role_mock";
//        auth.timestamp = System.currentTimeMillis();
//        return auth;
//    }
//
//    public static long sMockChatRoomId = System.currentTimeMillis();
//    public static ChatRoomItem mockChatRoomItem() {
//        int memNum = 2000;
//        ChatRoomItem chatRoomItem = new ChatRoomItem();
//        chatRoomItem.setId("08fbd929442d476c84d5fc0e7e20e1b9");
//        chatRoomItem.setRoomId(String.valueOf(sMockChatRoomId++));
//        chatRoomItem.setTitle("sunny的K歌房");
//        chatRoomItem.setMemberNum(memNum);
//        chatRoomItem.setCompere(mockChatMember(""));
//        List<String> avatarUrlList = new ArrayList<>();
//        for (int i = 0; i < memNum; i++) {
//            avatarUrlList.add(mockAvatarUrl());
//        }
//        chatRoomItem.setAvatarList(avatarUrlList);
//        return chatRoomItem;
//    }
//
//    public static ChatMember mockChatMember(String userId) {
//        if (TextUtils.isEmpty(userId)) {
//            userId = "sunny";
//        }
//        UserInfo userInfo = mockUserInfo(userId);
//        ChatMember chatMember = new ChatMember(userInfo);
//        return chatMember;
//    }
//
//    public static UserInfo mockUserInfo(String userId) {
//        if (TextUtils.isEmpty(userId)) {
//            userId = "sunny";
//        }
//        UserInfo userInfo = new UserInfo(userId, userId);
//        userInfo.userName = userId;
//        userInfo.avatarUrl = mockAvatarUrl();
//        userInfo.micPosition = 0;
//        userInfo.speaking = false;
//
//        return userInfo;
//    }
//}
