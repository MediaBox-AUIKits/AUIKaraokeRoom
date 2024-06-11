package com.aliyun.auikits.karaoke.room.model.content;

import android.text.TextUtils;

import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.base.feed.AbsContentModel;
import com.aliyun.auikits.karaoke.room.base.feed.BizParameter;
import com.aliyun.auikits.karaoke.room.base.feed.IBizCallback;
import com.aliyun.auikits.karaoke.room.model.entity.KTVMusicInfoWithUI;
import com.aliyun.auikits.karaoke.room.widget.card.CardTypeDef;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomMusicLibraryCallback;
import com.aliyun.auikits.karaoke.bean.KTVMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;

import java.util.ArrayList;
import java.util.List;

public class KtvSearchSongListContentModel extends AbsContentModel<CardEntity> {
    ARTCKaraokeRoomController mRoomController;
    private String mSearchKeyword = "";

    private int mPageIndex = 1;
    private static final int PAGE_SIZE = 10;

    public KtvSearchSongListContentModel(ARTCKaraokeRoomController roomController) {
        mRoomController = roomController;
    }

    public void setSearchKeyword(String mSearchKeyword) {
        this.mSearchKeyword = mSearchKeyword;
    }

    public String getSearchKeyword() {
        return mSearchKeyword;
    }

    @Override
    public void initData(BizParameter parameter, IBizCallback<CardEntity> callback) {
        mPageIndex = 1;
        fetchSearchResult(parameter, callback);
    }

    @Override
    public void fetchData(boolean isPullToRefresh, BizParameter parameter, IBizCallback<CardEntity> callback) {
        fetchSearchResult(parameter, callback);
    }

    private void fetchSearchResult(BizParameter parameter, IBizCallback<CardEntity> callback) {
        if (TextUtils.isEmpty(mSearchKeyword)) {
            return;
        }
        mRoomController.searchMusic(mSearchKeyword, mPageIndex, PAGE_SIZE, new ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback() {
            @Override
            public void onMusicInfoCallback(List<KTVMusicInfo> musicInfoList) {
                List<KTVPlayingMusicInfo> chosenMusicInfoList = mRoomController.getLocalPlayMusicInfoList();
                List<CardEntity> cardEntities = new ArrayList<>();
                for (KTVMusicInfo ktvMusicInfo : musicInfoList) {
                    CardEntity cardEntity = new CardEntity();
                    cardEntity.cardType = CardTypeDef.KTV_SEARCH_SONG_CARD;
                    KTVMusicInfoWithUI ktvMusicInfoWithUI = new KTVMusicInfoWithUI();
                    ktvMusicInfoWithUI.setMusicInfo(ktvMusicInfo);
                    ktvMusicInfoWithUI.setIsChosen(isMusicInfoChosen(ktvMusicInfo, chosenMusicInfoList));
                    cardEntity.bizData = ktvMusicInfoWithUI;
                    cardEntities.add(cardEntity);
                }
                mPageIndex++;
                callback.onSuccess(cardEntities);
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                callback.onError(errorCode, errorMsg);
            }
        });
        mPageIndex++;
    }

    @Override
    public void updateContent(CardEntity data, int pos) {
        super.updateContent(data, pos);
    }

    private boolean isMusicInfoChosen(KTVMusicInfo ktvMusicInfo, List<KTVPlayingMusicInfo> chosenMusicInfoList) {
        if (null != ktvMusicInfo && null != chosenMusicInfoList && !chosenMusicInfoList.isEmpty()) {
            for (KTVPlayingMusicInfo ktvPlayingMusicInfo : chosenMusicInfoList) {
                if (TextUtils.equals(ktvPlayingMusicInfo.songID, ktvMusicInfo.songID)) {
                    return true;
                }
            }
        }
        return false;
    }
}
