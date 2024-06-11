package com.aliyun.auikits.karaoke.room.widget.card;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.karaoke.room.base.card.BaseCard;
import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.model.entity.ChatRoomItem;
import com.aliyun.auikits.karaoke.room.vm.ChatRoomItemViewModel;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.ktv.databinding.KtvListRoomItemBinding;

public class ChatRoomCard extends BaseCard {
    private KtvListRoomItemBinding binding;
    private ChatRoomItemViewModel vm;

    public ChatRoomCard(Context context) {
        super(context);
    }

    @Override
    public void onCreate(Context context) {
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ktv_list_room_item, this, true);
        this.vm = new ChatRoomItemViewModel();
        this.binding.setViewModel(vm);
    }

    @Override
    public void onBind(CardEntity cardEntity) {
        this.vm.bind((ChatRoomItem) cardEntity.bizData);
        this.binding.executePendingBindings();
    }

    @Override
    public void onUnBind() {

    }
}
