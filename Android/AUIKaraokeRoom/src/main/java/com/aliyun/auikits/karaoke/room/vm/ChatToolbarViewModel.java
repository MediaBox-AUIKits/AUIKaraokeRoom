package com.aliyun.auikits.karaoke.room.vm;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aliyun.auikits.karaoke.room.adapter.ChatItemDecoration;
import com.aliyun.auikits.karaoke.room.service.KTVRoomManager;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.ktv.databinding.KtvDialogMusicBinding;
import com.aliyun.auikits.ktv.databinding.KtvDialogSettingBinding;
import com.aliyun.auikits.ktv.databinding.KtvDialogSoundEffectBinding;
import com.aliyun.auikits.karaoke.room.model.entity.ChatRoomCallback;
import com.aliyun.auikits.karaoke.room.model.entity.ChatSoundMix;
import com.aliyun.auikits.karaoke.room.base.card.CardListAdapter;
import com.aliyun.auikits.karaoke.room.base.feed.ContentViewModel;
import com.aliyun.auikits.karaoke.room.model.content.ChatSoundEffectContentModel;
import com.aliyun.auikits.karaoke.room.model.entity.ChatMember;
import com.aliyun.auikits.karaoke.room.model.content.ChatMusicContentModel;
import com.aliyun.auikits.karaoke.room.model.content.ChatReverbContentModel;
import com.aliyun.auikits.karaoke.room.model.content.ChatVoiceContentModel;
import com.aliyun.auikits.karaoke.room.util.DisplayUtil;
import com.aliyun.auikits.karaoke.room.util.ToastHelper;
import com.aliyun.auikits.karaoke.room.widget.card.CardTypeDef;
import com.aliyun.auikits.karaoke.room.widget.card.ChatMusicCard;
import com.aliyun.auikits.karaoke.room.widget.card.ChatSoundEffectCard;
import com.aliyun.auikits.karaoke.room.widget.card.ChatSoundMixCard;
import com.aliyun.auikits.karaoke.room.widget.card.DefaultCardViewFactory;
import com.aliyun.auikits.karaoke.room.widget.list.CustomViewHolder;
import com.aliyun.auikits.karaoke.room.widget.view.InputTextMsgDialog;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.aliyun.auikits.rtc.MixSoundType;
import com.aliyun.auikits.rtc.VoiceChangeType;
import com.aliyun.auikits.voice.AudioOutputType;
import com.aliyun.auikits.voiceroom.bean.MixSound;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.bean.VoiceChange;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.orhanobut.dialogplus.DialogPlus;

import java.lang.ref.WeakReference;
import java.util.Map;


public class ChatToolbarViewModel extends ViewModel {

    private ARTCKaraokeRoomController roomController;
    private ChatMember chatMember;
    public ObservableBoolean volumeSwitch = new ObservableBoolean(true);
    public ObservableBoolean musicEnable = new ObservableBoolean(false);
    public ObservableBoolean soundEffectEnable = new ObservableBoolean(false);
    public ObservableBoolean settingEnable = new ObservableBoolean(false);
    public ObservableBoolean showMicActionButton = new ObservableBoolean(false);
    public ObservableInt microphoneIconRes = new ObservableInt();
    private boolean connectMic = false;
    private ContentViewModel mAudioSettingViewModel;
    private DialogPlus mAudioSettingDialog;
    private ChatRoomCallback roomCallback;

    public void bind(ChatMember chatMember, ARTCKaraokeRoomController roomController) {
        this.chatMember = chatMember;
        this.roomController = roomController;
        connectMic = roomController.isAnchor();
        onChatConnectChanged(connectMic);
        updateMicrophoneIconRes(chatMember.getMicrophoneStatus());
        showMicActionButton.set(!roomController.isAnchor());
        this.roomCallback = new ChatRoomCallback() {
            @Override
            public void onJoinedMic(UserInfo user) {
                //非主持人模式下
                if(!roomController.isAnchor() && user.equals(roomController.getCurrentUser())) {
                    connectMic = true;
                    onChatConnectChanged(connectMic);
                }
            }

            @Override
            public void onLeavedMic(UserInfo user) {
                if(!roomController.isAnchor() && user.equals(roomController.getCurrentUser())) {
                    connectMic = false;
                    onChatConnectChanged(connectMic);
                }
            }

            @Override
            public void onMicUserMicrophoneChanged(UserInfo user, boolean open) {
                if (open) {
                    //当前用户的mic位变化,能收到这个，说明已经是连麦状态
                    if(connectMic && user.equals(roomController.getCurrentUser())) {
                        ChatToolbarViewModel.this.chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_ON);
                        updateMicrophoneIconRes(ChatToolbarViewModel.this.chatMember.getMicrophoneStatus());
                    }
                } else {
                    //当前用户的mic位变化,能收到这个，说明已经是连麦状态
                    if(connectMic && user.equals(roomController.getCurrentUser())) {
                        ChatToolbarViewModel.this.chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_OFF);
                        updateMicrophoneIconRes(ChatToolbarViewModel.this.chatMember.getMicrophoneStatus());
                    }
                }
            }
        };
        this.roomController.addObserver(roomCallback);
    }

    public void unBind() {
        this.roomController.removeObserver(roomCallback);
    }

    public void onInputMsgClick(View view) {

        Context context = view.getContext();
        Activity activity = (Activity) context;
        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        InputTextMsgDialog mInputTextMsgDialog = new InputTextMsgDialog(context);
        WindowManager.LayoutParams lp = mInputTextMsgDialog.getWindow().getAttributes();
        lp.width = display.getWidth();
        mInputTextMsgDialog.getWindow().setAttributes(lp);
        mInputTextMsgDialog.setCancelable(true);
        mInputTextMsgDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mInputTextMsgDialog.setOnTextSendListener(new InputTextMsgDialog.OnTextSendListener() {
            @Override
            public void onTextSend(String msg) {
                onSendMessage(new WeakReference<>(context), msg);
            }
        });
        mInputTextMsgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                showMicActionButton.set(!roomController.isAnchor());
            }
        });
        showMicActionButton.set(false);
        mInputTextMsgDialog.show();
    }

    private void onSendMessage(WeakReference<Context> contextRef, String msg) {
        this.roomController.sendTextMessage(msg, new ActionCallback() {
            @Override
            public void onResult(int code, String msg, Map<String, Object> params) {
                if(code != KTVRoomManager.CODE_SUCCESS) {
                    Context context = contextRef.get();
                    if(context != null) {
                        ToastHelper.showToast(context, R.string.voicechat_send_message_failed, Toast.LENGTH_SHORT);
                    }
                }
            }
        });
    }

    public void onVolumeSwitchChange(View view) {
        Context context = view.getContext();
        this.volumeSwitch.set(!this.volumeSwitch.get());

        this.roomController.setAudioOutputType(this.volumeSwitch.get() ? AudioOutputType.LOUDSPEAKER : AudioOutputType.HEADSET);
        ToastHelper.showToast(context, this.volumeSwitch.get() ? R.string.voicechat_chat_volume_on : R.string.voicechat_chat_volume_off, Toast.LENGTH_SHORT);
    }

    public void onMusicClick(View view) {
        Context context = view.getContext();
        KtvDialogMusicBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ktv_dialog_music, null, false);
        binding.setLifecycleOwner((LifecycleOwner) context);

        DefaultCardViewFactory factory = new DefaultCardViewFactory();
        factory.registerCardView(CardTypeDef.CHAT_MUSIC_CARD, ChatMusicCard.class);
        CardListAdapter musicCardListAdapter = new CardListAdapter(factory);

        binding.rvChatDataList.setLayoutManager(new LinearLayoutManager(context));
        binding.rvChatDataList.addItemDecoration(new ChatItemDecoration(0, 0));
        binding.rvChatDataList.setAdapter(musicCardListAdapter);

        ChatMusicContentModel musicReverbContentModel = new ChatMusicContentModel();
        ContentViewModel musicContentViewModel = new ContentViewModel.Builder()
                .setContentModel(musicReverbContentModel)
                .setLoadMoreEnable(false)
                .build();
        musicContentViewModel.bindView(musicCardListAdapter);

        musicCardListAdapter.addChildClickViewIds(R.id.btn_play, R.id.btn_apply);

        ChatMusicViewModel vm = new ChatMusicViewModel();
        //TODO APP 绑定音乐当前数据
        binding.setViewModel(vm);
        CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
        DialogPlus dialog = DialogPlus.newDialog(context)
                .setContentHolder(viewHolder)
                .setGravity(Gravity.BOTTOM)
                .setExpanded(false)
                .setOverlayBackgroundResource(android.R.color.transparent)
                .create();


        musicCardListAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {

            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                if(view.getId() == R.id.btn_play) {
                    //TODO SDK 对接播放和revert逻辑
                    musicReverbContentModel.playOrStopItem(position);
                } else if(view.getId() == R.id.btn_apply) {
                    //TODO SDK 对接应用背景音乐逻辑
                    dialog.dismiss();
                }
            }
        });


        dialog.show();

    }

    public void onSoundEffectClick(View view) {
        Context context = view.getContext();
        KtvDialogSoundEffectBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ktv_dialog_sound_effect, null, false);
        binding.setLifecycleOwner((LifecycleOwner) context);

        DefaultCardViewFactory factory = new DefaultCardViewFactory();
        factory.registerCardView(CardTypeDef.CHAT_SOUND_EFFECT_CARD, ChatSoundEffectCard.class);
        CardListAdapter soundEffectCardListAdapter = new CardListAdapter(factory);

        binding.rvChatDataList.setLayoutManager(new LinearLayoutManager(context));
        binding.rvChatDataList.addItemDecoration(new ChatItemDecoration(0, 0));
        binding.rvChatDataList.setAdapter(soundEffectCardListAdapter);

        ChatSoundEffectContentModel soundEffectContentModel = new ChatSoundEffectContentModel(context);
        ContentViewModel soundEffectContentViewModel = new ContentViewModel.Builder()
                .setContentModel(soundEffectContentModel)
                .setLoadMoreEnable(false)
                .build();
        soundEffectContentViewModel.bindView(soundEffectCardListAdapter);

        soundEffectCardListAdapter.addChildClickViewIds(R.id.btn_play, R.id.btn_apply);

        CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
        DialogPlus dialog = DialogPlus.newDialog(context)
                .setContentHolder(viewHolder)
                .setGravity(Gravity.BOTTOM)
                .setExpanded(false)
                .setOverlayBackgroundResource(android.R.color.transparent)
                .create();

        soundEffectCardListAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                if(view.getId() == R.id.btn_play) {
                    //TODO SDK 对接播放和revert逻辑
                    soundEffectContentModel.playOrStopItem(position);
                } else if(view.getId() == R.id.btn_apply) {
                    //TODO SDK 对接应用背景音乐逻辑
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    public void onSettingClick(View view) {
        if(mAudioSettingDialog != null){
            mAudioSettingDialog.show();
            return;
        }
        Context context = view.getContext();
        KtvDialogSettingBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ktv_dialog_setting, null, false);
        binding.setLifecycleOwner((LifecycleOwner) context);
        ChatSettingViewModel settingViewModel = new ChatSettingViewModel();
        settingViewModel.bind(this.roomController);
        binding.setViewModel(settingViewModel);
        CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());

        DefaultCardViewFactory factory = new DefaultCardViewFactory();
        factory.registerCardView(CardTypeDef.CHAT_SOUND_MIX_CARD, ChatSoundMixCard.class);
        CardListAdapter reverbCardListAdapter = new CardListAdapter(factory);

        binding.rvReverbList.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false));
        binding.rvReverbList.addItemDecoration(new ChatItemDecoration(DisplayUtil.dip2px(8), 0));
        binding.rvReverbList.setAdapter(reverbCardListAdapter);

        int lastReverbSelectPos = 0;
        ChatReverbContentModel chatReverbContentModel = new ChatReverbContentModel(lastReverbSelectPos);
        ContentViewModel reverbContentViewModel = new ContentViewModel.Builder()
                .setContentModel(chatReverbContentModel)
                .setLoadMoreEnable(false)
                .build();
        reverbContentViewModel.bindView(reverbCardListAdapter);
        reverbCardListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                chatReverbContentModel.selectItem(position);
                CardEntity entity = chatReverbContentModel.getSelectedItem();
                if(entity != null){
                    ChatSoundMix soundMix = (ChatSoundMix) entity.bizData;
                    if(roomController != null){
                        roomController.setAudioMixSound(new MixSound(MixSoundType.fromInt(Integer.parseInt(soundMix.getId()))));
                    }
                }
            }
        });

        CardListAdapter voiceCardListAdapter = new CardListAdapter(factory);
        binding.rvVoiceList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        binding.rvVoiceList.addItemDecoration(new ChatItemDecoration(DisplayUtil.dip2px(8), 0));
        binding.rvVoiceList.setAdapter(voiceCardListAdapter);

        int lastVoiceSelectPos = 0;
        ChatVoiceContentModel chatVoiceContentModel = new ChatVoiceContentModel(lastVoiceSelectPos);
        ContentViewModel voiceContentViewModel = new ContentViewModel.Builder()
                .setContentModel(chatVoiceContentModel)
                .setLoadMoreEnable(false)
                .build();
        voiceContentViewModel.bindView(voiceCardListAdapter);
        voiceCardListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                chatVoiceContentModel.selectItem(position);
                CardEntity entity = chatVoiceContentModel.getSelectedItem();
                if(entity != null){
                    ChatSoundMix soundMix = (ChatSoundMix) entity.bizData;
                    if(roomController != null){
                        roomController.setVoiceChange(new VoiceChange(VoiceChangeType.fromInt(Integer.parseInt(soundMix.getId()))));
                    }
                }
            }
        });

        mAudioSettingDialog = DialogPlus.newDialog(context)
                .setContentHolder(viewHolder)
                .setGravity(Gravity.BOTTOM)
                .setExpanded(false)
                .setOverlayBackgroundResource(android.R.color.transparent)
                .create();
        mAudioSettingDialog.show();
    }

    public void onMicrophoneChange(View view) {
        int microphoneStatus = chatMember.getMicrophoneStatus();
        if(microphoneStatus != ChatMember.MICROPHONE_STATUS_DISABLE) {
            if(microphoneStatus == ChatMember.MICROPHONE_STATUS_OFF) {
                microphoneStatus = ChatMember.MICROPHONE_STATUS_ON;
            } else {
                microphoneStatus = ChatMember.MICROPHONE_STATUS_OFF;
            }
            ToastHelper.showToast(view.getContext(), microphoneStatus == ChatMember.MICROPHONE_STATUS_OFF ? R.string.voicechat_chat_microphone_off : R.string.voicechat_chat_microphone_on, Toast.LENGTH_SHORT);
            chatMember.setMicrophoneStatus(microphoneStatus);
            this.roomController.switchMicrophone(microphoneStatus == ChatMember.MICROPHONE_STATUS_ON);
            updateMicrophoneIconRes(microphoneStatus);
        }
    }

    private void updateMicrophoneIconRes(int microphoneStatus) {
        if(microphoneStatus == ChatMember.MICROPHONE_STATUS_ON) {
            this.microphoneIconRes.set(R.drawable.voicechat_ic_microphone_on);
        } else if(microphoneStatus == ChatMember.MICROPHONE_STATUS_OFF) {
            this.microphoneIconRes.set(R.drawable.voicechat_ic_microphone_off);
        } else {
            this.microphoneIconRes.set(R.drawable.voicechat_ic_microphone_disabled);
        }

    }

    public void onChatConnectChanged(boolean connect) {
        this.settingEnable.set(connect);
        this.soundEffectEnable.set(connect);
        if(connect) {
            if(this.chatMember.getMicrophoneStatus() == ChatMember.MICROPHONE_STATUS_DISABLE) {
                this.chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_ON);
            }
        } else {
            this.chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_DISABLE);
        }
        updateMicrophoneIconRes(this.chatMember.getMicrophoneStatus());
    }
}
