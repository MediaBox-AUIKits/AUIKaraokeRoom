package com.aliyun.auikits.karaoke.room.model.content;

import com.alivc.auicommon.common.base.util.ThreadUtil;
import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.base.feed.AbsContentModel;
import com.aliyun.auikits.karaoke.room.base.feed.BizParameter;
import com.aliyun.auikits.karaoke.room.base.feed.IBizCallback;
import com.aliyun.auikits.karaoke.room.base.feed.IContentObserver;
import com.aliyun.auikits.karaoke.room.model.entity.ChatSoundMix;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.karaoke.room.widget.card.CardTypeDef;

import java.util.ArrayList;
import java.util.List;

/**
 * 调音台混响内容数据
 */
public class ChatVoiceContentModel extends AbsContentModel<CardEntity> {

    private ChatSoundMix[] effectArray = new ChatSoundMix[] {
            new ChatSoundMix("0", R.string.voicechat_none, R.drawable.voicechat_ic_none),
            new ChatSoundMix("1", R.string.voicechat_voice_dashu, R.drawable.voicechat_ic_voice_dashu),
            new ChatSoundMix("3", R.string.voicechat_voice_luoli, R.drawable.voicechat_ic_voice_luoli),
            new ChatSoundMix("4", R.string.voicechat_voice_jiqiren, R.drawable.voicechat_ic_voice_jiqiren),
            new ChatSoundMix("5", R.string.voicechat_voice_damowang, R.drawable.voicechat_ic_voice_damowang),
            new ChatSoundMix("6", R.string.voicechat_voice_ktv, R.drawable.voicechat_ic_voice_ktv),
            new ChatSoundMix("7", R.string.voicechat_voice_huisheng, R.drawable.voicechat_ic_voice_huisheng),
            new ChatSoundMix("8", R.string.voicechat_voice_fangyan, R.drawable.voicechat_ic_voice_fangyan),
    };

    private int selectPos = 0;

    public ChatVoiceContentModel(int selectPosition) {
        this.selectPos = selectPosition;
    }

    @Override
    public void initData(BizParameter parameter, IBizCallback<CardEntity> callback) {
        ThreadUtil.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                List<CardEntity> chatRoomItemList = getCardDataList();

                ThreadUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(callback != null) {
                            callback.onSuccess(chatRoomItemList);
                        }
                    }
                });

            }
        });


    }

    @Override
    public void fetchData(boolean isPullToRefresh, BizParameter parameter, IBizCallback<CardEntity> callback) {
    }

    public void selectItem(int position) {
        this.selectPos = position;
        ThreadUtil.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                List<CardEntity> cardEntityList = getCardDataList();

                ThreadUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(IContentObserver<CardEntity> observer : ChatVoiceContentModel.this.observers) {
                            observer.onContentUpdate(cardEntityList);
                        }
                    }
                });

            }
        });
    }

    private List<CardEntity> getCardDataList() {
        List<CardEntity> cardDataList = new ArrayList<>();
        for(int i = 0; i < effectArray.length; i++) {
            CardEntity cardEntity = new CardEntity();
            cardEntity.cardType = CardTypeDef.CHAT_SOUND_MIX_CARD;
            ChatSoundMix chatSoundEffects = effectArray[i];
            chatSoundEffects.setSelected(false);
            cardEntity.bizData = chatSoundEffects;
            cardDataList.add(cardEntity);
        }
        ((ChatSoundMix)cardDataList.get(selectPos).bizData).setSelected(true);

        return cardDataList;
    }

    //获取选中的列表项
    public CardEntity getSelectedItem(){
        List<CardEntity> cardList = getCardDataList();
        if(selectPos < cardList.size())
            return cardList.get(selectPos);
        return null;
    }
}
