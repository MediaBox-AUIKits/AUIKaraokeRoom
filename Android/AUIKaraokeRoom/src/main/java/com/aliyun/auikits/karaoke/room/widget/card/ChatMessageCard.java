package com.aliyun.auikits.karaoke.room.widget.card;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.karaoke.room.base.card.BaseCard;
import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.model.entity.ChatMessage;
import com.aliyun.auikits.karaoke.room.vm.ChatMessageViewModel;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.ktv.databinding.KtvChatMessageCardBinding;

public class ChatMessageCard extends BaseCard {
    private KtvChatMessageCardBinding binding;
    private ChatMessageViewModel vm;
    public ChatMessageCard(Context context) {
        super(context);
    }

    @Override
    public void onCreate(Context context) {
        this.vm = new ChatMessageViewModel();
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ktv_chat_message_card, this, true);
        this.binding.setViewModel(this.vm);
    }


    @Override
    public void onBind(CardEntity entity) {
        super.onBind(entity);
        this.vm.bind(getContext(), (ChatMessage) entity.bizData);
        this.binding.executePendingBindings();
    }
}
