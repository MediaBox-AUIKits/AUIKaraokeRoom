package com.aliyun.auikits.karaoke.room.vm;

import android.content.Context;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.aliyun.auikits.ktv.databinding.KtvActivityRoomBinding;
import com.aliyun.auikits.karaoke.room.model.entity.ChatRoom;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;


public class ChatViewModel extends ViewModel {

//    public ChatMicMemberViewModel compereViewModel;
    public ChatHeaderViewModel headerViewModel;
    public ChatToolbarViewModel toolbarViewModel;
    public ChatConnectViewModel chatConnectViewModel;
    public KtvRoomScreenViewModel ktvRoomScreenViewModel;
//    private ARTCKaraokeRoomControllerDelegate roomCallback;
    private ARTCKaraokeRoomController roomController;
    private ChatRoom chatRoom;

    private KtvActivityRoomBinding mBinding;

    public void bind(Context context, KtvActivityRoomBinding binding, ChatRoom room, ARTCKaraokeRoomController roomController) {
        mBinding = binding;
        this.chatRoom =  room;
//        //监听主持人的连麦及网络状态
//        this.roomCallback = new ChatRoomCallback() {
//
//            @Override
//            public void onMicUserMicrophoneChanged(UserInfo user, boolean open) {
//                if(open){
//                    if(roomController.isAnchor(user)) {
//                        ChatMember chatMember = ChatViewModel.this.chatRoom.getCompere();
//                        chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_ON);
//                        compereViewModel.bind(chatMember);
//                    }
//                }else{
//                    if(roomController.isAnchor(user)) {
//                        ChatMember chatMember = ChatViewModel.this.chatRoom.getCompere();
//                        chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_OFF);
//                        compereViewModel.bind(chatMember);
//                    }
//                }
//            }
//
//            @Override
//            public void onMicUserSpeakStateChanged(UserInfo user, int pitch) {
//                boolean compereSpeaking = ChatViewModel.this.roomController.isAnchor(user);
//                if(compereSpeaking) {
//                    ChatMember chatMember = ChatViewModel.this.chatRoom.getCompere();
//                    if(user.speaking != chatMember.isSpeaking()) {
//                        chatMember.setSpeaking(user.speaking);
//                        compereViewModel.bind(chatMember);
//                    }
//
//                }
//
//            }
//
//            @Override
//            public void onNetworkStateChanged(UserInfo user) {
//                if(ChatViewModel.this.roomController.isAnchor(user)) {
//                    ChatMember chatMember = ChatViewModel.this.chatRoom.getCompere();
//                    chatMember.setNetworkStatus(user.networkState);
//                    compereViewModel.bind(chatMember);
//                }
//            }
//
//        } ;
        this.roomController = roomController;
//        if (null != roomController) {
//            this.roomController.addObserver(this.roomCallback);
//        }

        ViewModelProvider viewModelProvider = new ViewModelProvider((ViewModelStoreOwner) context);
//        compereViewModel = viewModelProvider.get(ChatMicMemberViewModel.class);
        headerViewModel = viewModelProvider.get(ChatHeaderViewModel.class);
        toolbarViewModel = viewModelProvider.get(ChatToolbarViewModel.class);
        chatConnectViewModel = viewModelProvider.get(ChatConnectViewModel.class);
        ktvRoomScreenViewModel = viewModelProvider.get(KtvRoomScreenViewModel.class);

        headerViewModel.bind(room, this.roomController);
//        compereViewModel.bind(room.getCompere());
        toolbarViewModel.bind(room.getSelf(), this.roomController);
        chatConnectViewModel.bind(room, this.roomController);
        ktvRoomScreenViewModel.bind(mBinding.llayKtvRoomScreen, room, roomController);
    }

    public void unBind() {
        this.headerViewModel.unBind();
        this.toolbarViewModel.unBind();
        this.chatConnectViewModel.unBind();
        ktvRoomScreenViewModel.unbind();
//        this.roomController.removeObserver(this.roomCallback);
        roomController = null;
    }

    public ARTCKaraokeRoomController getRoomController() {
        return roomController;
    }
}
