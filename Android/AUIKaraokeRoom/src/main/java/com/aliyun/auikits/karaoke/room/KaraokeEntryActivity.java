package com.aliyun.auikits.karaoke.room;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alivc.auimessage.model.token.IMNewToken;
import com.aliyun.auikits.biz.ktv.KTVServerConstant;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.aliyun.auikits.karaoke.room.adapter.ChatItemDecoration;
import com.aliyun.auikits.karaoke.room.service.KTVRoomManager;
import com.aliyun.auikits.karaoke.room.service.KTVRoomService;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.karaoke.room.base.network.RetrofitManager;
import com.aliyun.auikits.ktv.databinding.KtvActivityEntryBinding;
import com.aliyun.auikits.ktv.databinding.KtvDialogLoadingBinding;
import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.base.card.CardListAdapter;
import com.aliyun.auikits.karaoke.room.base.feed.BizParameter;
import com.aliyun.auikits.karaoke.room.base.feed.ContentViewModel;
import com.aliyun.auikits.karaoke.room.base.feed.IBizCallback;
import com.aliyun.auikits.karaoke.room.model.api.KTVRoomApi;
import com.aliyun.auikits.karaoke.room.model.entity.ChatMember;
import com.aliyun.auikits.karaoke.room.model.entity.ChatRoomItem;
import com.aliyun.auikits.karaoke.room.model.content.KTVRoomContentModel;
import com.aliyun.auikits.karaoke.room.model.entity.network.KTVRoomRequest;
import com.aliyun.auikits.karaoke.room.model.entity.network.KTVRoomResponse;
import com.aliyun.auikits.karaoke.room.model.entity.network.RtcTokenRequest;
import com.aliyun.auikits.karaoke.room.model.entity.network.RtcTokenResponse;
import com.aliyun.auikits.karaoke.room.util.AvatarUtil;
import com.aliyun.auikits.karaoke.room.util.ToastHelper;
import com.aliyun.auikits.karaoke.room.widget.card.CardTypeDef;
import com.aliyun.auikits.karaoke.room.widget.card.ChatRoomCard;
import com.aliyun.auikits.karaoke.room.widget.card.DefaultCardViewFactory;
import com.aliyun.auikits.karaoke.room.util.DisplayUtil;
import com.aliyun.auikits.karaoke.room.widget.list.CustomViewHolder;
import com.aliyun.auikits.rtc.ClientMode;
import com.aliyun.auikits.single.Singleton;
import com.aliyun.auikits.single.server.Server;
import com.aliyun.auikits.voiceroom.bean.NetworkState;
import com.aliyun.auikits.voiceroom.bean.RoomInfo;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;
import com.aliyun.auikits.voiceroom.external.RtcInfo;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.jaeger.library.StatusBarUtil;
import com.orhanobut.dialogplus.DialogPlus;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Route(path = "/karaoke/KaraokeEntryActivity")
public class KaraokeEntryActivity extends AppCompatActivity implements ContentViewModel.OnDataUpdateCallback {
    private static final String TAG = "KtvEntryTag";

    //IM信息
    public static final String KEY_IM_TAG = "KTV_IM_TAG";
    public static final String KEY_AUTHORIZATION = "AUTHORIZATION";
    public static final String KEY_USER_ID = "USER_ID";
    public static final int REQUEST_CODE = 1002;

    private KtvActivityEntryBinding binding;
    private CardListAdapter chatRoomItemAdapter;
    private ContentViewModel contentViewModel;
    private KTVRoomContentModel chatRoomContentModel;

    private ChatMember currentUser;
    private IMNewToken im_token;
    private String authorization;
    private BizParameter bizParameter = new BizParameter();

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTransparent(this);
        RxJavaPlugins.setErrorHandler(Functions.<Throwable>emptyConsumer());

        binding = DataBindingUtil.setContentView(this, com.aliyun.auikits.ktv.R.layout.ktv_activity_entry);
        binding.setLifecycleOwner(this);

        Intent intent = getIntent();
        // 检查 Intent 的 action 和 category
        boolean isLaunchedFromHome = Intent.ACTION_MAIN.equals(intent.getAction()) &&
                intent.hasCategory(Intent.CATEGORY_LAUNCHER);

        if(isLaunchedFromHome) {
            findViewById(com.aliyun.auikits.ktv.R.id.back_btn).setVisibility(View.GONE);
        } else {
            findViewById(com.aliyun.auikits.ktv.R.id.back_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }

        im_token = (IMNewToken) getIntent().getSerializableExtra(KEY_IM_TAG);
        authorization = getIntent().getStringExtra(KEY_AUTHORIZATION);
        Singleton.getInstance(Server.class).setAuthorizeToken(authorization);
        String userId = getIntent().getStringExtra(KEY_USER_ID);
        if(im_token == null || authorization == null || userId == null) {
            Toast.makeText(KaraokeEntryActivity.this, com.aliyun.auikits.ktv.R.string.voicechat_invalid_param, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUser = new ChatMember(userId);

        DefaultCardViewFactory factory = new DefaultCardViewFactory();
        factory.registerCardView(CardTypeDef.CHAT_ROOM_CARD, ChatRoomCard.class);
        chatRoomItemAdapter = new CardListAdapter(factory);
        binding.rvChatRoomList.setLayoutManager(new GridLayoutManager(this,2));
        binding.rvChatRoomList.setAdapter(chatRoomItemAdapter);
        binding.rvChatRoomList.addItemDecoration(new ChatItemDecoration((int) DisplayUtil.convertDpToPixel(6, this), (int) DisplayUtil.convertDpToPixel(12, this)));

        chatRoomContentModel = new KTVRoomContentModel();
        contentViewModel = new ContentViewModel.Builder()
                        .setContentModel(chatRoomContentModel)
                        .setBizParameter(bizParameter)
                        .setLoadMoreEnable(false)
                        .setEmptyView(com.aliyun.auikits.ktv.R.layout.ktv_list_room_empty_view)
//                        .setLoadingView(R.layout.voicechat_loading_view)
                        .setErrorView(com.aliyun.auikits.ktv.R.layout.ktv_layout_error_view, com.aliyun.auikits.ktv.R.id.btn_retry)
                        .setOnDataUpdateCallback(this)
                        .build();


        chatRoomItemAdapter.addChildClickViewIds(com.aliyun.auikits.ktv.R.id.btn_chat_room_enter);
        chatRoomItemAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                CardEntity cardEntity = (CardEntity) adapter.getItem(position);
                joinChatActivity((ChatRoomItem) cardEntity.bizData);
            }
        });
        chatRoomItemAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                CardEntity cardEntity = (CardEntity) adapter.getItem(position);
                joinChatActivity((ChatRoomItem) cardEntity.bizData);
            }
        });

        binding.srlChatRoomList.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                contentViewModel.initData();
            }
        });

        binding.srlChatRoomList.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                contentViewModel.loadMore();
            }
        });

        onDataInit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contentViewModel.unBind();
        KTVRoomManager.getInstance().destroy();
    }

    private void onDataInit() {

        //目前名字设置成和ID一样，业务方可以根据自己的用户登录体系去改
        currentUser.setName(currentUser.getId());
        currentUser.setAvatar(AvatarUtil.getAvatarUrl(currentUser.getId()));
        currentUser.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_SELF);
        currentUser.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_DISABLE);
        currentUser.setNetworkStatus(NetworkState.NORMAL);

        bizParameter.append(KTVRoomContentModel.KEY_AUTHORIZATION, authorization);
        KTVRoomManager.getInstance().addGlobalParam(KaraokeActivity.KEY_AUTHORIZATION, authorization);
        contentViewModel.bindView(chatRoomItemAdapter);

//        DialogHelper.showFirstEnterDialog(this, currentUser);
    }


    public void onClickCreateRoom(View view) {

        ChatMember currentUser = getCurrentUser();

        DialogPlus dialog = createLoadingDialog();
        dialog.show();
        ChatRoomItem chatRoomItemVar[] = new ChatRoomItem[]{null};

        //创建房间
        Observable.create(new ObservableOnSubscribe<List<CardEntity>>() {
            @Override
            public void subscribe(@io.reactivex.rxjava3.annotations.NonNull ObservableEmitter<List<CardEntity>> emitter) throws Throwable {
                chatRoomContentModel.createRoom(KaraokeEntryActivity.this.authorization, currentUser, new IBizCallback<CardEntity>() {
                    @Override
                    public void onSuccess(List<CardEntity> data) {
                        if(data.size() > 0) {
                            Log.v(TAG, "CreateKtvRoom success");
                            emitter.onNext(data);
                            emitter.onComplete();
                        } else {
                            Log.v(TAG, "CreateKtvRoom fail");
                            emitter.onError(new RuntimeException(KaraokeEntryActivity.this.getString(com.aliyun.auikits.ktv.R.string.voicechat_create_room_failed)));
                        }
                    }

                    @Override
                    public void onError(int code, String msg) {
                        Log.v(TAG, "CreateKtvRoom fail [code: " + code + ", msg: " + msg + "]");
                        emitter.onError(new RuntimeException(KaraokeEntryActivity.this.getString(com.aliyun.auikits.ktv.R.string.voicechat_create_room_failed)));
                    }
                });
            }
        })
        //加入房间
        .flatMap(new Function<List<CardEntity>, ObservableSource<Pair<ChatRoomItem, Integer>>>() {
            @Override
            public ObservableSource<Pair<ChatRoomItem, Integer>> apply(List<CardEntity> cardEntities) throws Throwable {
                CardEntity cardEntity = cardEntities.get(0);
                ChatRoomItem chatRoomItem = (ChatRoomItem) cardEntity.bizData;
                chatRoomItemVar[0] = chatRoomItem;

                return Observable.zip(Observable.just(chatRoomItem), createJoinRoomObservable(chatRoomItem, currentUser), (chatRoomParam, joinRoomResponse) -> {
                    return new Pair<>(chatRoomParam, joinRoomResponse);
                });
            }
        }).timeout(30, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
                new Consumer<Pair<ChatRoomItem, Integer>>() {
                    @Override
                    public void accept(Pair<ChatRoomItem, Integer> data) throws Throwable {
                        dialog.dismiss();
                        if(data.second == KTVRoomManager.CODE_SUCCESS) {
                            jumpToChatActivity(data.first, currentUser);
                        } else {
                            ToastHelper.showToast(KaraokeEntryActivity.this, com.aliyun.auikits.ktv.R.string.voicechat_join_room_failed, Toast.LENGTH_SHORT);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        dialog.dismiss();
                        throwable.printStackTrace();
                        if (null != chatRoomItemVar[0]) {
                            KTVRoomManager.getInstance().destroyKTVRoomController(chatRoomItemVar[0].getRoomId());
                        }
                        if(throwable instanceof RuntimeException) {
                            ToastHelper.showToast(KaraokeEntryActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT);
                        } else {
                            ToastHelper.showToast(KaraokeEntryActivity.this, com.aliyun.auikits.ktv.R.string.voicechat_join_room_failed, Toast.LENGTH_SHORT);
                        }

                    }
                });
    }

    private Observable<Integer> createJoinRoomObservable(ChatRoomItem chatRoomItem, ChatMember currentUser) {
        RoomInfo roomInfo = new RoomInfo(chatRoomItem.getId());
        roomInfo.creator = chatRoomItem.getCompereUserInfo();
        String authorization = KaraokeEntryActivity.this.authorization;
        RtcTokenRequest rtcTokenRequest = new RtcTokenRequest();
        rtcTokenRequest.room_id = roomInfo.roomId;
        rtcTokenRequest.user_id = currentUser.getId();
        //获取RTC Token
        Observable<RtcInfo> rtcInfoObservable = RetrofitManager.getRetrofit(KTVServerConstant.HOST).create(KTVRoomApi.class).getRtcToken(authorization, rtcTokenRequest)
                .map(new Function<RtcTokenResponse, RtcInfo>() {
                    @Override
                    public RtcInfo apply(RtcTokenResponse response) throws Throwable {
                        Log.v(TAG, "JoinKtvRoom get rtc token  success");
                        return new RtcInfo(response.auth_token, response.timestamp, KTVServerConstant.RTC_GLSB);
                    }
                });

        //房间初始化
        Observable<ARTCKaraokeRoomController> roomInitObservable = Observable.create(new ObservableOnSubscribe<ARTCKaraokeRoomController>() {
            @Override
            public void subscribe(@io.reactivex.rxjava3.annotations.NonNull ObservableEmitter<ARTCKaraokeRoomController> emitter) throws Throwable {
                ARTCKaraokeRoomController auiKaraokeRoom = KTVRoomManager.getInstance().createKTVRoomController(roomInfo.roomId);
                UserInfo userInfo = new UserInfo(currentUser.getId(), currentUser.getId());
                userInfo.userName = currentUser.getName();
                userInfo.avatarUrl = currentUser.getAvatar();

                auiKaraokeRoom.init(KaraokeEntryActivity.this.getApplicationContext(), ClientMode.KTV, KTVServerConstant.APP_ID, userInfo, KaraokeEntryActivity.this.im_token, new ActionCallback() {
                    @Override
                    public void onResult(int code, String msg, Map<String, Object> params) {
                        if(code == KTVRoomManager.CODE_SUCCESS) {
                            Log.v(TAG, "JoinKtvRoom init room success");
                            emitter.onNext(auiKaraokeRoom);
                            emitter.onComplete();
                        } else {
                            KTVRoomManager.getInstance().destroyKTVRoomController(roomInfo.roomId);
                            Log.v(TAG, "JoinKtvRoom init room fail:code:" + code + ",msg:" + msg );
                            emitter.onError(new RuntimeException(msg));
                        }
                    }
                });
            }
        });

        KTVRoomRequest chatRoomRequest = new KTVRoomRequest();
        chatRoomRequest.id = chatRoomItem.getId();
        chatRoomRequest.user_id = currentUser.getId();
        Observable<KTVRoomResponse> getRoomObservable = RetrofitManager.getRetrofit(KTVServerConstant.HOST).create(KTVRoomApi.class).getRoomInfo(KaraokeEntryActivity.this.authorization, chatRoomRequest);

        //JoinRoom
        Observable<Integer> joinRoomObservable = Observable.zip(rtcInfoObservable, roomInitObservable, getRoomObservable, (rtcInfo, karaokeRoom, roomResponse) -> {
                    Log.v(TAG, "JoinKtvRoom data ready [rtcInfo: " + rtcInfo + ", roomController: " + karaokeRoom + ", roomInfo: " + roomResponse + "]");
                    return new Object[] {rtcInfo, karaokeRoom, roomResponse};
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<Object[], ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(Object[] rtcInfoAUIKaraokeRoomPair) throws Throwable {
                        if(((KTVRoomResponse)rtcInfoAUIKaraokeRoomPair[2]).isRoomValid()) {
                            Log.v(TAG, "JoinKtvRoom room is valid");
                            return KTVRoomService.joinRoom(((ARTCKaraokeRoomController)rtcInfoAUIKaraokeRoomPair[1]), roomInfo, (RtcInfo) rtcInfoAUIKaraokeRoomPair[0]);
                        }  else {
                            Log.v(TAG, "JoinKtvRoom room is closed");
                            return Observable.error(new RuntimeException(KaraokeEntryActivity.this.getString(com.aliyun.auikits.ktv.R.string.voicechat_room_closed)));
                        }
                    }
                }).doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer code) throws Throwable {
                        Log.v(TAG, "JoinKtvRoom end [code: " + code + "]");
                        if(code != KTVRoomManager.CODE_SUCCESS) {
                            KTVRoomManager.getInstance().destroyKTVRoomController(roomInfo.roomId);
                        }
                    }
                }).doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        Log.v(TAG, "JoinKtvRoom end with exception [throwable: " + throwable.getMessage() + "]");
                        KTVRoomManager.getInstance().destroyKTVRoomController(roomInfo.roomId);
                    }
                });
        return joinRoomObservable;
    }

    private void jumpToChatActivity(ChatRoomItem chatRoomItem, ChatMember currentUser) {
        Intent intent = new Intent(KaraokeEntryActivity.this, KaraokeActivity.class);
        intent.putExtra(KaraokeActivity.KEY_CHAT_ROOM_ENTITY, chatRoomItem);
        intent.putExtra(KaraokeActivity.KEY_CHAT_SELF_ENTITY, currentUser);
        intent.putExtra(KaraokeActivity.KEY_AUTHORIZATION, KaraokeEntryActivity.this.authorization);
        startActivityForResult(intent, REQUEST_CODE);
    }

    private void joinChatActivity(ChatRoomItem chatRoomItem, ChatMember currentUser) {
        DialogPlus dialog = createLoadingDialog();
        dialog.show();
        createJoinRoomObservable(chatRoomItem, currentUser)
                .timeout(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer code) throws Throwable {
                        dialog.dismiss();

                        if(code == KTVRoomManager.CODE_SUCCESS) {
                            Log.v(TAG, "join room is success");
                            jumpToChatActivity(chatRoomItem, currentUser);
                        } else {
                            Log.v(TAG, "join room is fail:" + code);
                            ToastHelper.showToast(KaraokeEntryActivity.this, com.aliyun.auikits.ktv.R.string.voicechat_join_room_failed, Toast.LENGTH_SHORT);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        dialog.dismiss();
                        throwable.printStackTrace();
                        if (null != chatRoomItem) {
                            KTVRoomManager.getInstance().destroyKTVRoomController(chatRoomItem.getRoomId());
                        }
                        if(throwable instanceof RuntimeException) {
                            ToastHelper.showToast(KaraokeEntryActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT);
                        } else {
                            ToastHelper.showToast(KaraokeEntryActivity.this, com.aliyun.auikits.ktv.R.string.voicechat_join_room_failed, Toast.LENGTH_SHORT);
                        }

                    }
                });
    }

    private void joinChatActivity(ChatRoomItem chatRoomItem) {
        joinChatActivity(chatRoomItem, getCurrentUser());
    }

    private ChatMember getCurrentUser() {
        return currentUser;
    }

    private DialogPlus createLoadingDialog() {
        KtvDialogLoadingBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.ktv_dialog_loading, null, false);
        binding.setLifecycleOwner(this);
        CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
        DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(viewHolder)
                .setGravity(Gravity.CENTER)
                .setExpanded(false)
                .setContentWidth(DisplayUtil.dip2px(112))
                .setContentBackgroundResource(android.R.color.transparent)
                .setOverlayBackgroundResource(android.R.color.transparent)
                .setCancelable(false)
                .create();
        return dialog;
    }

    @Override
    public void onInitStart() {


    }

    @Override
    public void onInitEnd(boolean success, List<CardEntity> cardEntities) {

        binding.srlChatRoomList.finishRefresh();

    }

    @Override
    public void onLoadMoreStart() {

    }

    @Override
    public void onLoadMoreEnd(boolean success, List<CardEntity> cardEntities) {
        binding.srlChatRoomList.finishLoadMore();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                boolean roomDismiss = data.getBooleanExtra(KaraokeActivity.KEY_ROOM_DISMISS, false);
                String roomID = data.getStringExtra(KaraokeActivity.KEY_ROOM_ID);
                if(!TextUtils.isEmpty(roomID)) {
                    //如果房间解散，则移除卡片上的数据
                    if(roomDismiss) {
                        List<CardEntity> cardEntityList = chatRoomItemAdapter.getData();
                        CardEntity targetCardEntity = null;
                        int targetCardPos = -1;
                        for(CardEntity cardEntity : cardEntityList) {
                            ChatRoomItem chatRoomItem = (ChatRoomItem) cardEntity.bizData;
                            targetCardPos++;
                            if(chatRoomItem.getId().equals(roomID)) {
                                targetCardEntity = cardEntity;
                                break;
                            }
                        }
                        if(targetCardEntity != null && targetCardPos >= 0) {
                            chatRoomItemAdapter.removeAt(targetCardPos);
                        }
                    } else {
                        KTVRoomManager.getInstance().destroyKTVRoomController(roomID);
                    }

                }
            }
        }
    }
}
