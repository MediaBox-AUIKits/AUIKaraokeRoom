package com.aliyun.auikits.karaoke.room.widget.card;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.karaoke.room.base.card.BaseCard;
import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.model.entity.KTVMusicInfoWithUI;
import com.aliyun.auikits.karaoke.room.vm.KtvSongCardViewModel;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.ktv.databinding.KtvLayoutChooseSongPlatformSongCardBinding;

public class KtvSongCard extends BaseCard {

    KtvLayoutChooseSongPlatformSongCardBinding mBinding;
    KtvSongCardViewModel mViewModel;

    public KtvSongCard(Context context) {
        super(context);

    }

    @Override
    public void onCreate(Context context) {
        mViewModel = new KtvSongCardViewModel();
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ktv_layout_choose_song_platform_song_card, this, true);
        this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        this.mBinding.setViewModel(mViewModel);
    }

    @Override
    public void onBind(CardEntity entity) {
        super.onBind(entity);

        this.mViewModel.bind(mBinding.getRoot().getContext(), (KTVMusicInfoWithUI) entity.bizData);
        this.mBinding.executePendingBindings();
    }
}
