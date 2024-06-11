package com.aliyun.auikits.karaoke.room.widget.card;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.karaoke.room.base.card.BaseCard;
import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.model.entity.ChatSoundMix;
import com.aliyun.auikits.karaoke.room.vm.ChatSoundMixViewModel;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.ktv.databinding.KtvSoundMixCardBinding;

public class ChatSoundMixCard extends BaseCard {
    private KtvSoundMixCardBinding binding;
    private ChatSoundMixViewModel vm;
    public ChatSoundMixCard(Context context) {
        super(context);

    }

    @Override
    public void onCreate(Context context) {
        this.vm = new ChatSoundMixViewModel();
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ktv_sound_mix_card, this, true);
        this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        this.binding.setViewModel(vm);
    }


    @Override
    public void onBind(CardEntity entity) {
        super.onBind(entity);
        this.vm.bind((ChatSoundMix) entity.bizData);
        this.binding.executePendingBindings();
    }
}
