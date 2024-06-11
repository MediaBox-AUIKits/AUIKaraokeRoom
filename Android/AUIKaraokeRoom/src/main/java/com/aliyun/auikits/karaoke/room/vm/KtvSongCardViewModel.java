package com.aliyun.auikits.karaoke.room.vm;

import android.content.Context;

import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.karaoke.room.model.entity.KTVMusicInfoWithUI;
import com.aliyun.auikits.karaoke.room.util.DisplayTextUtil;
import com.aliyun.auikits.karaoke.bean.KTVMusicInfo;

public class KtvSongCardViewModel extends ViewModel {
    public ObservableField<String> songName = new ObservableField<>();
    public ObservableField<String> singerName = new ObservableField<>();
    public ObservableField<String> durationText = new ObservableField<>();
    public ObservableField<String> albumUrl = new ObservableField<>();
    public ObservableInt selectBtnBgRes = new ObservableInt();
    public ObservableField<String> selectBtnText = new ObservableField<>();

    public void bind(Context context, KTVMusicInfoWithUI ktvMusicInfoWithUI) {
        KTVMusicInfo ktvMusicInfo = ktvMusicInfoWithUI.getMusicInfo();
        songName.set(ktvMusicInfo.songName);
        singerName.set(ktvMusicInfo.singerName);
        durationText.set(DisplayTextUtil.formatDuration(ktvMusicInfo.duration));
        albumUrl.set(ktvMusicInfo.albumImg);
        selectBtnBgRes.set(ktvMusicInfoWithUI.isIsChosen() ?
                        R.drawable.ktv_btn_song_card_selector:
                        R.drawable.ktv_btn_create_room_selector
                );
        selectBtnText.set(ktvMusicInfoWithUI.isIsChosen() ?
                        context.getString(R.string.ktv_song_selected) :
                        context.getString(R.string.ktv_enter_song_selector)
                );
    }
}
