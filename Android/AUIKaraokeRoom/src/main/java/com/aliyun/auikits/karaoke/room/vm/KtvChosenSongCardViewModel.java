package com.aliyun.auikits.karaoke.room.vm;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;
import com.aliyun.auikits.karaoke.room.model.entity.KtvChosenSong;

public class KtvChosenSongCardViewModel extends ViewModel {
    public ObservableField<String> songName = new ObservableField<>();
    public ObservableField<String> singerName = new ObservableField<>();
    public ObservableField<String> memberNameText = new ObservableField<>();
    public ObservableField<String> albumUrl = new ObservableField<>();
    public ObservableBoolean actionSkipVisibility = new ObservableBoolean(false);
    public ObservableBoolean actionTopVisibility = new ObservableBoolean(false);
    public ObservableBoolean actionTrashVisibility = new ObservableBoolean(false);
    public ObservableBoolean actionPlayingVisibility = new ObservableBoolean(false);
    public ObservableBoolean cardVisibility = new ObservableBoolean(false);

    public void bind(KtvChosenSong ktvChosenSong) {
        if (null != ktvChosenSong) {
            cardVisibility.set(true);
            songName.set(ktvChosenSong.getSongInfo().songName);
            albumUrl.set(ktvChosenSong.getSongInfo().albumImg);
            singerName.set("原唱 " + ktvChosenSong.getSongInfo().singerName);
            String singerPositionStr = ktvChosenSong.getChosenMember().seatIndex == 0 ?
                    "主持人" : (ktvChosenSong.getChosenMember().seatIndex+1)+"号麦";
            memberNameText.set(
                    singerPositionStr + ktvChosenSong.getChosenMember().userName + "点歌");

            if (ktvChosenSong.isPlaying() || ktvChosenSong.getSongOrder() == 0) {
                actionSkipVisibility.set(ktvChosenSong.isCanSkip());
                actionPlayingVisibility.set(true);
                actionTopVisibility.set(false);
                actionTrashVisibility.set(false);
            } else {
                actionSkipVisibility.set(false);
                actionPlayingVisibility.set(false);

                if (ktvChosenSong.getSongOrder() == 1) {
                    actionTopVisibility.set(false);
                } else {
                    actionTopVisibility.set(ktvChosenSong.isCanPin());
                }
                actionTrashVisibility.set(ktvChosenSong.isCanTrash());
            }
        } else {
            cardVisibility.set(false);
        }
    }
}
