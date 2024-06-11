package com.aliyun.auikits.karaoke.room.widget.card;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.karaoke.room.base.card.BaseCard;
import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.model.entity.ChatMusicItem;
import com.aliyun.auikits.karaoke.room.vm.ChatSoundEffectViewModel;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.ktv.databinding.KtvSoundEffectCardBinding;

public class ChatSoundEffectCard extends BaseCard {
    private KtvSoundEffectCardBinding binding;
    private ChatSoundEffectViewModel vm;
    public ChatSoundEffectCard(Context context) {
        super(context);

    }

    @Override
    public void onCreate(Context context) {
        this.vm = new ChatSoundEffectViewModel();
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ktv_sound_effect_card, this, true);
        this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        this.binding.setViewModel(vm);
    }


    @Override
    public void onBind(CardEntity entity) {
        super.onBind(entity);
        this.vm.bind((ChatMusicItem) entity.bizData);
        this.binding.executePendingBindings();
    }
}
