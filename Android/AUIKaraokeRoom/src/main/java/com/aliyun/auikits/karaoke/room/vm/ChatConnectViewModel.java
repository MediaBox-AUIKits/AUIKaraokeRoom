package com.aliyun.auikits.karaoke.room.vm;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.karaoke.room.service.KTVRoomManager;
import com.aliyun.auikits.karaoke.room.service.KTVRoomService;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.ktv.databinding.KtvDialogConnectBinding;
import com.aliyun.auikits.karaoke.room.model.entity.ChatRoomCallback;
import com.aliyun.auikits.karaoke.room.model.entity.ChatRoom;
import com.aliyun.auikits.karaoke.room.util.DisplayUtil;
import com.aliyun.auikits.karaoke.room.util.ToastHelper;
import com.aliyun.auikits.karaoke.room.widget.list.CustomViewHolder;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomControllerDelegate;
import com.aliyun.auikits.voiceroom.bean.MicInfo;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ChatConnectViewModel extends ViewModel {
    //是否主持人模式
    public ObservableBoolean isCompereMode = new ObservableBoolean();
    //是否上麦 - 非主持人模式使用
    public ObservableBoolean isChatConnected = new ObservableBoolean(false);
    private ARTCKaraokeRoomControllerDelegate roomCallback;
    private ARTCKaraokeRoomController roomController;

    public void bind(ChatRoom chatRoom, ARTCKaraokeRoomController roomController) {
        this.isCompereMode.set(chatRoom.isCompere());
        this.isChatConnected.set(false);
        this.roomController = roomController;
        this.roomCallback = new ChatRoomCallback() {
            @Override
            public void onRoomMicListChanged(List<UserInfo> micUsers) {
                //当首次查询连麦列表后，如果发现当前用户已经在麦上，则直接上麦，发起上麦请求
                boolean isCurrentUserOnMic = false;
                UserInfo currentUser = roomController.getCurrentUser();
                UserInfo targetMicUser = null;
                for(UserInfo micUser : micUsers) {
                    isCurrentUserOnMic = micUser.equals(currentUser);
                    if(isCurrentUserOnMic) {
                        targetMicUser = micUser;
                        break;
                    }
                }

                if(targetMicUser != null) {
                    //设置连麦状态
                    ChatConnectViewModel.this.isChatConnected.set(true);
                    MicInfo micInfo = new MicInfo(targetMicUser.micPosition, false);
                    roomController.joinMic(micInfo, new ActionCallback() {
                        @Override
                        public void onResult(int code, String msg, Map<String, Object> params) {
                        }
                    });
                }

                ChatConnectViewModel.this.roomController.removeObserver(this);
                ChatConnectViewModel.this.roomCallback = null;
            }

        };
        this.roomController.addObserver(this.roomCallback);


    }

    public void unBind() {
        if(this.roomCallback != null) {
            this.roomController.removeObserver(this.roomCallback);
        }
    }

    public void onConnectSwitch(View view) {
        final boolean isConnected = this.isChatConnected.get();
        Context context = view.getContext();

        if(isConnected) {
            KTVRoomService.leaveMic(this.roomController)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer code) throws Throwable {
                            if(code == KTVRoomManager.CODE_SUCCESS) {
                                ToastHelper.showToast(context, R.string.voicechat_chat_disconnect_tips, Toast.LENGTH_SHORT);
                                ChatConnectViewModel.this.isChatConnected.set(false);
                            } else {
                                ToastHelper.showToast(context, R.string.voicechat_network_failed, Toast.LENGTH_SHORT);
                            }
                        }
                    });
        } else {

            KtvDialogConnectBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ktv_dialog_connect, null, false);
            binding.setLifecycleOwner((LifecycleOwner) context);
            CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
            final Boolean [] microphoneSwitch = new Boolean[1];
            microphoneSwitch[0] = true;
            DialogPlus dialog = DialogPlus.newDialog(context)
                    .setContentHolder(viewHolder)
                    .setGravity(Gravity.BOTTOM)
                    .setExpanded(false)
                    .setOverlayBackgroundResource(android.R.color.transparent)
                    .setContentBackgroundResource(R.color.voicechat_background)
                    .setPadding(0, 0 ,0 , DisplayUtil.getNavigationBarHeight(context))
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(DialogPlus dialog, View view) {
                            if(view.getId() == R.id.iv_connect_op) {
                                microphoneSwitch[0] = !microphoneSwitch[0];
                                if(microphoneSwitch[0]) {
                                    binding.ivConnectOp.setImageResource(R.drawable.voicechat_ic_microphone_on);
                                } else {
                                    binding.ivConnectOp.setImageResource(R.drawable.voicechat_ic_microphone_off);
                                }

                            } else if(view.getId() == R.id.btn_chat_connect_confirm) {
                                KTVRoomService.joinMic(ChatConnectViewModel.this.roomController, microphoneSwitch[0])
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<Integer>() {
                                            @Override
                                            public void accept(Integer code) throws Throwable {
                                                if(code == KTVRoomManager.CODE_SUCCESS) {
                                                    ChatConnectViewModel.this.isChatConnected.set(true);
                                                } else if(code == KTVRoomService.RST_JOIN_MIC_ALREADY_JOIN){
                                                    ToastHelper.showToast(context, R.string.voicechat_join_mic_full, Toast.LENGTH_SHORT);
                                                } else {
                                                    ToastHelper.showToast(context, R.string.voicechat_join_mic_failure, Toast.LENGTH_SHORT);
                                                }
                                                dialog.dismiss();
                                            }
                                        });
                            } else if(view.getId() == R.id.btn_chat_connect_cancel) {
                                dialog.dismiss();
                            }
                        }
                    })
                    .create();
            dialog.show();
        }


    }
}
