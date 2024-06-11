package com.aliyun.auikits.karaoke.room.widget.card;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.karaoke.room.base.card.BaseCard;
import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.model.entity.ChatMember;
import com.aliyun.auikits.karaoke.room.vm.ChatMicMemberViewModel;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.ktv.databinding.KtvChatMicMemberCardBinding;

public class ChatMicMemberCard extends BaseCard {
    private KtvChatMicMemberCardBinding binding;
    private ChatMicMemberViewModel vm;
    public ChatMicMemberCard(Context context) {
        super(context);

    }

    @Override
    public void onCreate(Context context) {
        this.vm = new ChatMicMemberViewModel();
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ktv_chat_mic_member_card, this, true);
        this.binding.setViewModel(vm);
    }


    @Override
    public void onBind(CardEntity entity) {
        super.onBind(entity);
        this.vm.bind((ChatMember) entity.bizData);
        this.binding.executePendingBindings();
    }
}
