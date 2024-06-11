package com.aliyun.auikits.karaoke.room.model.content;

import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.base.feed.AbsContentModel;
import com.aliyun.auikits.karaoke.room.base.feed.BizParameter;
import com.aliyun.auikits.karaoke.room.base.feed.IBizCallback;
import com.aliyun.auikits.karaoke.room.model.entity.KtvChosenSong;
import com.aliyun.auikits.karaoke.room.widget.card.CardTypeDef;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomMusicPlayingListServiceCallback;
import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;

import java.util.ArrayList;
import java.util.List;

public class KtvChosenSongListContentModel extends AbsContentModel<CardEntity> {

    private ARTCKaraokeRoomController mRoomController;

    public KtvChosenSongListContentModel(ARTCKaraokeRoomController roomController) {
        mRoomController = roomController;
    }

    @Override
    public void initData(BizParameter parameter, IBizCallback<CardEntity> callback) {
        mRoomController.fetchMusicPlayingList(new ARTCKaraokeRoomMusicPlayingListServiceCallback() {
            @Override
            public void onMusicPlayingListCallback(List<KTVPlayingMusicInfo> playingMusicInfoList) {
                List<CardEntity> cardEntities = convert(playingMusicInfoList);
                callback.onSuccess(cardEntities);
            }

            @Override
            public void onMusicPlayingJoinerListCallback(KTVPlayingMusicInfo ktvPlayingMusicInfo) {

            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                callback.onError(errorCode, errorMsg);
            }
        });
    }

    @Override
    public void fetchData(boolean isPullToRefresh, BizParameter parameter, IBizCallback<CardEntity> callback) {

    }

    public void updatePlayingMusicInfoList(List<KTVPlayingMusicInfo> playingMusicInfoList) {
        List<CardEntity> cardEntities = convert(playingMusicInfoList);
        updateContent(cardEntities);
    }

    private List<CardEntity> convert(List<KTVPlayingMusicInfo> playingMusicInfoList) {
        List<CardEntity> cardEntities = new ArrayList<>();
        int songOrder = 0;
        for (KTVPlayingMusicInfo ktvPlayingMusicInfo : playingMusicInfoList) {
            if (songOrder > 0) {
                CardEntity cardEntity = new CardEntity();
                cardEntity.cardType = CardTypeDef.KTV_CHOSEN_SONG_CARD;
                KtvChosenSong ktvChosenSong = KtvChosenSong.fromKtvPlayingMusicInfo(ktvPlayingMusicInfo);
                ktvChosenSong.setSongOrder(songOrder);
                ktvChosenSong.setCanPin(mRoomController.checkCanPinMusic(ktvPlayingMusicInfo));
                ktvChosenSong.setCanTrash(mRoomController.checkCanRemoveMusic(ktvPlayingMusicInfo));
                ktvChosenSong.setCanSkip(mRoomController.checkCanSkipMusic(ktvPlayingMusicInfo));
                cardEntity.bizData = ktvChosenSong;
                cardEntities.add(cardEntity);
            }
            songOrder++;
        }
        return cardEntities;
    }
}
