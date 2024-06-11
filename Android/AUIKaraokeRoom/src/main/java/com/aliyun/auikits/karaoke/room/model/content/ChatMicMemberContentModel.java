package com.aliyun.auikits.karaoke.room.model.content;

import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.base.feed.AbsContentModel;
import com.aliyun.auikits.karaoke.room.base.feed.BizParameter;
import com.aliyun.auikits.karaoke.room.base.feed.IBizCallback;
import com.aliyun.auikits.karaoke.room.model.entity.ChatMember;
import com.aliyun.auikits.karaoke.room.model.entity.ChatRoomCallback;
import com.aliyun.auikits.karaoke.room.widget.card.CardTypeDef;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomControllerDelegate;
import com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason;
import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVRoomState;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ChatMicMemberContentModel extends AbsContentModel<CardEntity> {
    public static final int MAX_MEMBER_COUNT = 8;
    private ChatMember self = null;
    private ChatMember mCompere = null;
    private ARTCKaraokeRoomController mRoomController = null;
    private ARTCKaraokeRoomControllerDelegate roomCallback;
    private List<CardEntity> roomMemberList = new ArrayList<>();
    private int lastSpeakingPos = -1;

    public ChatMicMemberContentModel(ChatMember self, ChatMember compere, ARTCKaraokeRoomController roomController) {
        this.self = self;
        mCompere = compere;

        for(int i = 1; i <= MAX_MEMBER_COUNT; i++) {
            CardEntity cardEntity = new CardEntity();
            ChatMember chatMember = null;
            if (i == 1) {
                cardEntity.cardType = CardTypeDef.CHAT_MEMBER_CARD;
                chatMember = this.mCompere;
                chatMember.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_COMPERE);
            } else {
                cardEntity.cardType = CardTypeDef.CHAT_MEMBER_EMPTY_CARD;
                chatMember = new ChatMember();
            }
            chatMember.setIndex(i);
            cardEntity.bizData = chatMember;
            roomMemberList.add(cardEntity);
        }

        this.roomCallback = new ChatRoomCallback() {
            @Override
            public void onRoomStateChanged(KTVRoomState oldState, KTVRoomState newState, String songId) {
                super.onRoomStateChanged(oldState, newState, songId);

                // 先消除状态
                for (int micPosition = 0; micPosition < MAX_MEMBER_COUNT; micPosition++) {
                    CardEntity cardEntity = roomMemberList.get(micPosition);
                    ChatMember chatMember = (ChatMember)  cardEntity.bizData;
                    chatMember.setSingerFlag(ChatMember.SINGER_FLAG_NONE);
                }
                updateContent(roomMemberList);

                if (newState != KTVRoomState.Init) {
                    findLeadSingerAndSetFlag();
                    findOtherJoinSingerAndSetFlag();
                }
            }

            @Override
            public void onMusicPlayingUpdated(KTVMusicPlayListUpdateReason reason, UserInfo operateUserInfo, List<KTVPlayingMusicInfo> kTVPlayingMusicInfoListUpdated, List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList) {
                super.onMusicPlayingUpdated(reason, operateUserInfo, kTVPlayingMusicInfoListUpdated, ktvPlayingMusicInfoList);

                if (reason == KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_OTHER) {
                    findLeadSingerAndSetFlag();
                    findOtherJoinSingerAndSetFlag();
                }

                if (reason == KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_JOIN_SINGING || reason == KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_LEAVE_JOIN_SINGING) {
                    String userId = null != operateUserInfo ? operateUserInfo.userId : null;
                    findMicUserCardEntity(userId, new IMicUserCardEntityHandler() {
                        @Override
                        public boolean handleUserCardEntity(CardEntity cardEntity) {
                            int singerFlag = ChatMember.SINGER_FLAG_NONE;
                            if (reason == KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_JOIN_SINGING) {
                                singerFlag = ChatMember.SINGER_FLAG_JOIN;
                            } else if (reason == KTVMusicPlayListUpdateReason.PLAYING_LIST_UPDATE_REASON_LEAVE_JOIN_SINGING) {
                                singerFlag = ChatMember.SINGER_FLAG_NONE;
                            }
                            ChatMember chatMember = (ChatMember) cardEntity.bizData;
                            if (chatMember.getSingerFlag() != singerFlag) {
                                chatMember.setSingerFlag(singerFlag);
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });
                }
            }

            @Override
            public void onJoinedMic(UserInfo user) {
                findMicUserCardEntity(user, cardEntity -> {
                    cardEntity.cardType = CardTypeDef.CHAT_MEMBER_CARD;
                    ChatMember chatMember;
                    chatMember = new ChatMember(user);
                    cardEntity.bizData = chatMember;
                    if(user.equals(mRoomController.getCurrentUser())) {
                        chatMember.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_SELF);
                    }
                    return true;
                });
            }

            @Override
            public void onLeavedMic(UserInfo user) {
                findMicUserCardEntity(user, cardEntity -> {
                    cardEntity.cardType = CardTypeDef.CHAT_MEMBER_EMPTY_CARD;
                    ChatMember chatMember = new ChatMember();
                    chatMember.setIndex(user.micPosition+1);
                    cardEntity.bizData = chatMember;
                    return true;
                });
            }

            @Override
            public void onMicUserMicrophoneChanged(UserInfo user, boolean open) {
                findMicUserCardEntity(user, cardEntity -> {
                    if(Objects.equals(cardEntity.cardType, CardTypeDef.CHAT_MEMBER_CARD)) {
                        ChatMember chatMember = (ChatMember) cardEntity.bizData;
                        if(user.userId.equals(chatMember.getId())) {
                            chatMember.setMicrophoneStatus(open ? ChatMember.MICROPHONE_STATUS_ON : ChatMember.MICROPHONE_STATUS_OFF);
                            return true;
                        }
                    }
                    return false;
                });
            }

            @Override
            public void onMicUserSpeakStateChanged(UserInfo user) {
                //有人在讲话，且和上次讲话的用户不是同一个,则把上次讲话的状态设置成false
                if(user.speaking && lastSpeakingPos >= 0 && lastSpeakingPos != user.micPosition) {
                    findMicUserCardEntity(lastSpeakingPos, new IMicUserCardEntityHandler() {
                        @Override
                        public boolean handleUserCardEntity(CardEntity cardEntity) {
                            if(Objects.equals(cardEntity.cardType, CardTypeDef.CHAT_MEMBER_CARD)) {
                                ChatMember chatMember = (ChatMember) cardEntity.bizData;
                                if(chatMember.isSpeaking()) {
                                    chatMember.setSpeaking(false);
                                    return true;
                                }
                            }
                            return false;
                        }
                    });
                }

                //如果是连麦客户的讲话状态变化
                findMicUserCardEntity(user, new IMicUserCardEntityHandler() {
                    @Override
                    public boolean handleUserCardEntity(CardEntity cardEntity) {
                        if(Objects.equals(cardEntity.cardType, CardTypeDef.CHAT_MEMBER_CARD)) {
                            ChatMember chatMember = (ChatMember) cardEntity.bizData;
                            boolean userIdEqual = user.userId.equals(chatMember.getId());
                            boolean speakProNotEqual = user.speaking != chatMember.isSpeaking();
                            if(userIdEqual && speakProNotEqual) {
                                chatMember.setSpeaking(user.speaking);
                                return true;
                            }
                        }
                        return false;
                    }
                });

                //更新讲话的麦位： 有可能是主持人及连麦客户
                if(user.speaking) {
                    //如果有用户在讲话，判断当前用户是否是麦上用户，如果是则更新位置
                    if(user.micPosition >= 0 && user.micPosition < MAX_MEMBER_COUNT) {
                        lastSpeakingPos = user.micPosition;
                    } else {
                        //不是麦上用户（主持人），则设置成-1
                        lastSpeakingPos = -1;
                    }
                } else {
                    //如果没有讲话，则设置成-1
                    lastSpeakingPos = -1;
                }
            }

            @Override
            public void onNetworkStateChanged(UserInfo user) {
                findMicUserCardEntity(user, cardEntity -> {
                    if(Objects.equals(cardEntity.cardType, CardTypeDef.CHAT_MEMBER_CARD)) {
                        ChatMember chatMember = (ChatMember) cardEntity.bizData;
                        if(user.userId.equals(chatMember.getId())) {
                            chatMember.setNetworkStatus(user.networkState);
                            return true;
                        }
                    }
                    return false;
                });
            }

            @Override
            public void onRoomMicListChanged(List<UserInfo> micUsers) {
                for(UserInfo micUser : micUsers) {
                    findMicUserCardEntity(micUser, cardEntity -> {
                        ChatMember chatMember = new ChatMember(micUser);
                        cardEntity.bizData = chatMember;
                        if(micUser.equals(mRoomController.getCurrentUser())) {
                            chatMember.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_SELF);
                        }
                        cardEntity.cardType = CardTypeDef.CHAT_MEMBER_CARD;
                        return false;
                    });
                }
                updateContent(roomMemberList);
            }
        };
        this.mRoomController = roomController;
        this.mRoomController.addObserver(this.roomCallback);
    }

    private void findLeadSingerAndSetFlag() {
        String leadSingerUserId = null != mRoomController && null != mRoomController.getCurrentPlayingMusicInfo() ?
                mRoomController.getCurrentPlayingMusicInfo().seatInfo.userId : null;

        findMicUserCardEntity(leadSingerUserId, new IMicUserCardEntityHandler() {
            @Override
            public boolean handleUserCardEntity(CardEntity cardEntity) {
                ChatMember chatMember = (ChatMember)  cardEntity.bizData;
                chatMember.setSingerFlag(ChatMember.SINGER_FLAG_LEAD);
                return true;
            }
        });
    }

    private void findOtherJoinSingerAndSetFlag() {
        List<String> joinSingerIdList = null != mRoomController && null != mRoomController.getCurrentPlayingMusicInfo() ?
                mRoomController.getCurrentPlayingMusicInfo().joinSingUserIdList : null;
        if (null != joinSingerIdList && !joinSingerIdList.isEmpty()) {
            for (String joinUserId: joinSingerIdList) {
                findMicUserCardEntity(joinUserId, new IMicUserCardEntityHandler() {
                    @Override
                    public boolean handleUserCardEntity(CardEntity cardEntity) {
                        ChatMember chatMember = (ChatMember)  cardEntity.bizData;
                        chatMember.setSingerFlag(ChatMember.SINGER_FLAG_JOIN);
                        return true;
                    }
                });
            }
        }
    }

    private interface IMicUserCardEntityHandler {
        boolean handleUserCardEntity(CardEntity cardEntity);
    }

    private void findMicUserCardEntity(UserInfo micUser, IMicUserCardEntityHandler handler) {
        if (null != micUser) {
            findMicUserCardEntity(micUser.micPosition, handler);
        }
    }

    private void findMicUserCardEntity(int micPosition, IMicUserCardEntityHandler handler) {
        if(micPosition >= 0 && micPosition < MAX_MEMBER_COUNT) {
            CardEntity cardEntity = roomMemberList.get(micPosition);

            boolean needUpdate = handler.handleUserCardEntity(cardEntity);

            if (needUpdate) {
                updateContent(cardEntity, micPosition);
            }
        }
    }

    private void findMicUserCardEntity(String userId, IMicUserCardEntityHandler handler) {
        if (null != userId) {
            for (int micPosition = 0; micPosition < MAX_MEMBER_COUNT; micPosition++) {
                CardEntity cardEntity = roomMemberList.get(micPosition);
                ChatMember chatMember = (ChatMember)  cardEntity.bizData;
                if (chatMember.getId().equals(userId)) {
                    boolean needUpdate = handler.handleUserCardEntity(cardEntity);
                    if (needUpdate) {
                        updateContent(cardEntity, micPosition);
                    }
                    break;
                }
            }
        }
    }

    public void release() {
        this.mRoomController.removeObserver(this.roomCallback);
    }


    @Override
    public void initData(BizParameter parameter, IBizCallback<CardEntity> callback) {

        if(callback != null) {
            callback.onSuccess(roomMemberList);
        }

        //只是发起请求，会在onRoomMicListChanged中返回
        mRoomController.listMicUserList(new ActionCallback() {
            @Override
            public void onResult(int code, String msg, Map<String, Object> params) {
            }
        });

    }

    @Override
    public void fetchData(boolean isPullToRefresh, BizParameter parameter, IBizCallback<CardEntity> callback) {

    }

}
