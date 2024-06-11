package com.aliyun.auikits.karaoke.room.model.api;

import com.aliyun.auikits.biz.ktv.KTVServerConstant;
import com.aliyun.auikits.karaoke.room.model.entity.network.CloseRoomResponse;
import com.aliyun.auikits.karaoke.room.model.entity.network.LoginRequest;
import com.aliyun.auikits.karaoke.room.model.entity.network.LoginResponse;
import com.aliyun.auikits.karaoke.room.model.entity.network.KTVRoomListRequest;
import com.aliyun.auikits.karaoke.room.model.entity.network.KTVRoomListResponse;
import com.aliyun.auikits.karaoke.room.model.entity.network.KTVRoomRequest;
import com.aliyun.auikits.karaoke.room.model.entity.network.KTVRoomResponse;
import com.aliyun.auikits.karaoke.room.model.entity.network.CloseRoomRequest;
import com.aliyun.auikits.karaoke.room.model.entity.network.CreateRoomRequest;
import com.aliyun.auikits.karaoke.room.model.entity.network.CreateRoomResponse;
import com.aliyun.auikits.karaoke.room.model.entity.network.ImTokenRequest;
import com.aliyun.auikits.karaoke.room.model.entity.network.ImTokenResponse;
import com.aliyun.auikits.karaoke.room.model.entity.network.RtcTokenRequest;
import com.aliyun.auikits.karaoke.room.model.entity.network.RtcTokenResponse;


import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface KTVRoomApi {
    @POST(KTVServerConstant.LOGIN_URL)
    Observable<LoginResponse> login(
            @Body
            LoginRequest request
    );

    @POST(KTVServerConstant.GET_IM_TOKEN_URL)
    Observable<ImTokenResponse> getImToken(
            @Header("Authorization")
            String authorization,
            @Body
            ImTokenRequest request
    );

    @POST(KTVServerConstant.GET_RTC_TOKEN_URL)
    Observable<RtcTokenResponse> getRtcToken(
            @Header("Authorization")
            String authorization,
            @Body
            RtcTokenRequest request
    );

    @POST(KTVServerConstant.GET_CHAT_ROOM_LIST_URL)
    Observable<KTVRoomListResponse> fetchRoomList(
            @Header("Authorization")
            String authorization,
            @Body KTVRoomListRequest request);

    @POST(KTVServerConstant.GET_CHAT_ROOM_INFO_URL)
    Observable<KTVRoomResponse> getRoomInfo(
            @Header("Authorization") String authorization,
            @Body KTVRoomRequest request
    );

    @POST(KTVServerConstant.DISMISS_CHAT_ROOM_URL)
    Observable<CloseRoomResponse> dismissRoom(@Header("Authorization") String authorization, @Body CloseRoomRequest request);

    @POST(KTVServerConstant.CREATE_CHAT_ROOM_URL)
    Observable<CreateRoomResponse> createRoom(@Header("Authorization") String authorization, @Body CreateRoomRequest request);

}
