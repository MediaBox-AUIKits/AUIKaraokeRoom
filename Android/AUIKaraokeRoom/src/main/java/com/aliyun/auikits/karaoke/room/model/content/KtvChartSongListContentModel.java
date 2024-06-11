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

public class KtvChartSongListContentModel extends AbsContentModel<CardEntity> {
    private ARTCKaraokeRoomController mRoomController;

    public static String BIZ_PARAM_CHART_ID = "biz_param_chart_id";
    public static String BIZ_PARAM_CHART_NAME = "biz_param_chart_name";

    private static int PAGE_SIZE = 10;
    private int mPageIndex = 1;

    public KtvChartSongListContentModel(ARTCKaraokeRoomController roomController) {
        mRoomController = roomController;
    }

    @Override
    public void initData(BizParameter parameter, IBizCallback<CardEntity> callback) {
        mPageIndex = 1;
        fetchMusicList(parameter, callback);
    }

    @Override
    public void fetchData(boolean isPullToRefresh, BizParameter parameter, IBizCallback<CardEntity> callback) {
        fetchMusicList(parameter, callback);
    }

    @Override
    public void updateContent(CardEntity data, int pos) {
        super.updateContent(data, pos);
    }

    private void fetchMusicList(BizParameter parameter, IBizCallback<CardEntity> callback) {
        String ktvChartId = parameter.getQuerySet().get(BIZ_PARAM_CHART_ID);
        mRoomController.fetchMusicList(ktvChartId, mPageIndex, PAGE_SIZE, new ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicInfoListCallback() {
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
                callback.onError(-1, errorMsg);
            }
        });
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
