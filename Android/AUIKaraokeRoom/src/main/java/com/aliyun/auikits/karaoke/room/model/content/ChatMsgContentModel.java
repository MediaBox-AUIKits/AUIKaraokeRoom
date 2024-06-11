package com.aliyun.auikits.karaoke.room.model.content;

import android.content.Context;
import android.text.TextUtils;

import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.base.feed.AbsContentModel;
import com.aliyun.auikits.karaoke.room.base.feed.BizParameter;
import com.aliyun.auikits.karaoke.room.base.feed.IBizCallback;
import com.aliyun.auikits.karaoke.room.model.entity.ChatMember;
import com.aliyun.auikits.karaoke.room.model.entity.ChatMessage;
import com.aliyun.auikits.karaoke.room.model.entity.ChatRoom;
import com.aliyun.auikits.karaoke.room.model.entity.ChatRoomCallback;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.karaoke.room.widget.card.CardTypeDef;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomControllerDelegate;
import com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason;
import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVRoomState;
import com.aliyun.auikits.voiceroom.bean.UserInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ChatMsgContentModel extends AbsContentModel<CardEntity> {
    private ChatRoom mChatRoom;
    private ARTCKaraokeRoomController mRoomController;
    private ARTCKaraokeRoomControllerDelegate mRoomCallback;
    private WeakReference<Context> mContextRef;
    private KTVPlayingMusicInfo mCurrentMusicInfoInProcess;

    public ChatMsgContentModel(Context context, ChatRoom chatRoom, ARTCKaraokeRoomController roomController) {
        super();
        this.mChatRoom = chatRoom;
        this.mRoomController = roomController;
        this.mContextRef = new WeakReference<>(context);
        this.mRoomCallback = new ChatRoomCallback() {

            @Override
            public void onRoomStateChanged(KTVRoomState oldState, KTVRoomState newState, String songId) {
                super.onRoomStateChanged(oldState, newState, songId);

                mCurrentMusicInfoInProcess = mRoomController.getCurrentPlayingMusicInfo();
            }

            @Override
            public void onMusicPlayingUpdated(KTVMusicPlayListUpdateReason reason, UserInfo operateUserInfo, List<KTVPlayingMusicInfo> kTVPlayingMusicInfoListUpdated, List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList) {
                super.onMusicPlayingUpdated(reason, operateUserInfo, kTVPlayingMusicInfoListUpdated, ktvPlayingMusicInfoList);
                Context context = ChatMsgContentModel.this.mContextRef.get();
                if (null != context && null != kTVPlayingMusicInfoListUpdated) {
                    for (KTVPlayingMusicInfo ktvPlayingMusicInfoUpdated : kTVPlayingMusicInfoListUpdated) {
                        insertNoticeMsg(composePlayingListUpdateNoticeMsg(context, reason, operateUserInfo, ktvPlayingMusicInfoUpdated));
                    }
                }
            }

            @Override
            public void onJoinedMic(UserInfo user) {
                Context context1 = ChatMsgContentModel.this.mContextRef.get();

                //主持人加入麦不添加进消息列表
                if(context1 != null && user.micPosition > 0) {
                    String noticeMsg = String.format(context1.getString(R.string.voicechat_join_mic_suffix), user.userName, user.micPosition+1);
                    insertNoticeMsg(noticeMsg);
                }
            }

            @Override
            public void onLeavedMic(UserInfo user) {
                Context context1 = ChatMsgContentModel.this.mContextRef.get();
                //主持人离开麦不添加进消息列表
                if(context1 != null && user.micPosition > 0) {
                    String noticeMsg = String.format(context1.getString(R.string.voicechat_leave_mic_suffix), user.userName, user.micPosition+1);
                    insertNoticeMsg(noticeMsg);
                }
            }

            @Override
            public void onJoinedRoom(UserInfo user) {
                Context context1 = ChatMsgContentModel.this.mContextRef.get();

                if(context1 != null) {
                    String noticeMsg = String.format(context1.getString(R.string.voicechat_join_room_suffix), user.userName);
                    insertNoticeMsg(noticeMsg);
                }
            }

            @Override
            public void onReceivedTextMessage(UserInfo user, String text) {
                CardEntity cardEntity = new CardEntity();
                cardEntity.cardType = CardTypeDef.CHAT_MESSAGE_CARD;

                ChatMember chatMember = new ChatMember(user);
                if(user.userId.equals(chatRoom.getCompere().getId())) {
                    chatMember.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_COMPERE);
                } else if(user.userId.equals(chatRoom.getSelf().getId())) {
                    chatMember.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_SELF);
                }

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(ChatMessage.TYPE_CHAT_MSG);
                chatMessage.setMember(chatMember);
                chatMessage.setContent(text);
                cardEntity.bizData = chatMessage;

                List<CardEntity> cardEntityList = new ArrayList<>();
                cardEntityList.add(cardEntity);
                insertContent(cardEntityList);
            }

        };
        this.mRoomController.addObserver(this.mRoomCallback);
    }

    @Override
    public void release() {
        super.release();
        this.mRoomController.removeObserver(this.mRoomCallback);
    }

    @Override
    public void initData(BizParameter parameter, IBizCallback<CardEntity> callback) {

        List<CardEntity> chatMsgItemList = new ArrayList<>();
        if(mContextRef != null && mContextRef.get() != null) {
            Context context = mContextRef.get();
            String anchorUserName = mChatRoom.getCompere().getName();
            String []initTipsArray = new String[] {
                context.getString(R.string.ktv_msg_tips),
                context.getString(R.string.ktv_msg_microphone_tips),
                mRoomController.isAnchor() ? context.getString(R.string.ktv_create_room_anchor_tips, anchorUserName) : "",
                mRoomController.isAnchor() ? context.getString(R.string.ktv_join_mic_anchor_tips, anchorUserName) : ""
            };

            for (String tip : initTipsArray) {
                if (tip.length() > 0) {
                    CardEntity cardEntity = generateMsgCardEntity(tip, ChatMessage.TYPE_NOTICE);
                    chatMsgItemList.add(cardEntity);
                }
            }
        }

        if(callback != null) {
            callback.onSuccess(chatMsgItemList);
        }
    }

    @Override
    public void fetchData(boolean isPullToRefresh, BizParameter parameter, IBizCallback<CardEntity> callback) {

    }

    private CardEntity generateMsgCardEntity(String msg, int msgType) {
        CardEntity cardEntity = new CardEntity();
        cardEntity.cardType = CardTypeDef.CHAT_MESSAGE_CARD;
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(msgType);
        chatMessage.setContent(msg);
        cardEntity.bizData = chatMessage;
        return cardEntity;
    }

    private void insertNoticeMsg(String noticeMsg) {
        if (!TextUtils.isEmpty(noticeMsg)) {
            CardEntity cardEntity = generateMsgCardEntity(noticeMsg, ChatMessage.TYPE_NOTICE);

            List<CardEntity> cardEntityList = new ArrayList<>();
            cardEntityList.add(cardEntity);
            insertContent(cardEntityList);
        }
    }

    private String composePlayingListUpdateNoticeMsg(Context context, KTVMusicPlayListUpdateReason updateReason, UserInfo operateUserInfo, KTVPlayingMusicInfo ktvPlayingMusicInfo) {
        String msg = "";
        if (null != context && null != updateReason && null != ktvPlayingMusicInfo && null != operateUserInfo) {
            switch (updateReason) {
                case PLAYING_LIST_UPDATE_REASON_ADD_MUSIC:
                    msg = context.getString(R.string.ktv_notice_msg_add_music, operateUserInfo.userName, ktvPlayingMusicInfo.songName);
                    break;
                case PLAYING_LIST_UPDATE_REASON_REMOVE_MUSIC:
                    msg = context.getString(R.string.ktv_notice_msg_remove_music, operateUserInfo.userName, ktvPlayingMusicInfo.songName);
                    break;
                case PLAYING_LIST_UPDATE_REASON_PIN_MUSIC:
                    msg = context.getString(R.string.ktv_notice_msg_pin_music, operateUserInfo.userName, ktvPlayingMusicInfo.songName);
                    break;
                case PLAYING_LIST_UPDATE_REASON_SKIP_MUSIC:
                    msg = context.getString(R.string.ktv_notice_msg_skip_music, operateUserInfo.userName, ktvPlayingMusicInfo.songName);
                    break;
                case PLAYING_LIST_UPDATE_REASON_COMPLETE_MUSIC:
                    msg = context.getString(R.string.ktv_notice_msg_complete_music, ktvPlayingMusicInfo.seatInfo.userName, ktvPlayingMusicInfo.songName);
                    break;
                case PLAYING_LIST_UPDATE_REASON_JOIN_SINGING:
                    msg = context.getString(R.string.ktv_notice_msg_join_sing, operateUserInfo.userName, ktvPlayingMusicInfo.songName);
                    break;
                case PLAYING_LIST_UPDATE_REASON_LEAVE_JOIN_SINGING:
                    msg = context.getString(R.string.ktv_notice_msg_leave_join_sing, operateUserInfo.userName, ktvPlayingMusicInfo.songName);
                    break;
                case PLAYING_LIST_UPDATE_REASON_OTHER:
                default:
                    break;
            }
        }
        return msg;
    }
}
