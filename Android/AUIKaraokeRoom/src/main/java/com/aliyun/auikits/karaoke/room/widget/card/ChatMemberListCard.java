package com.aliyun.auikits.karaoke.room.widget.card;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.karaoke.room.base.card.BaseCard;
import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.model.entity.ChatMember;
import com.aliyun.auikits.karaoke.room.vm.ChatMemberListViewModel;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.ktv.databinding.KtvChatMemberListCardBinding;

public class ChatMemberListCard extends BaseCard {
    private KtvChatMemberListCardBinding binding;
    private ChatMemberListViewModel vm;
    public ChatMemberListCard(Context context) {
        super(context);

    }

    @Override
    public void onCreate(Context context) {
        this.vm = new ChatMemberListViewModel();
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ktv_chat_member_list_card, this, true);
        this.binding.setViewModel(vm);
    }


    @Override
    public void onBind(CardEntity entity) {
        super.onBind(entity);
        this.vm.bind((ChatMember) entity.bizData);
        this.binding.executePendingBindings();
    }
}
