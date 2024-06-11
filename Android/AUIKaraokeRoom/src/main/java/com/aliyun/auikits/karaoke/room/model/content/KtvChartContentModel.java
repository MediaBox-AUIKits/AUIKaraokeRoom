package com.aliyun.auikits.karaoke.room.model.content;

import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.base.feed.AbsContentModel;
import com.aliyun.auikits.karaoke.room.base.feed.BizParameter;
import com.aliyun.auikits.karaoke.room.base.feed.IBizCallback;
import com.aliyun.auikits.karaoke.room.model.entity.KtvChart;
import com.aliyun.auikits.karaoke.room.widget.card.CardTypeDef;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomMusicLibraryCallback;
import com.aliyun.auikits.karaoke.bean.KTVChartInfo;

import java.util.ArrayList;
import java.util.List;

public class KtvChartContentModel extends AbsContentModel<CardEntity> {
    private ARTCKaraokeRoomController mRoomController;

    public KtvChartContentModel(ARTCKaraokeRoomController roomController) {
        mRoomController = roomController;
    }

    @Override
    public void initData(BizParameter parameter, IBizCallback<CardEntity> callback) {
        mRoomController.fetchMusicChartList(new ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicChartCallback() {
            @Override
            public void onMusicChartCallback(List<KTVChartInfo> ktvChartInfoList) {
                List<CardEntity> cardEntities = new ArrayList<>();
                if (null != ktvChartInfoList && ktvChartInfoList.size() > 0) {
                    for (KTVChartInfo ktvChartInfo : ktvChartInfoList) {
                        CardEntity cardEntity = new CardEntity();
                        cardEntity.cardType = CardTypeDef.KTV_CHART_CARD;
                        KtvChart ktvChart = new KtvChart();
                        ktvChart.setChartId(ktvChartInfo.chartId);
                        ktvChart.setChartName(ktvChartInfo.chartName);
                        cardEntity.bizData = ktvChart;
                        cardEntities.add(cardEntity);
                    }
                }
                callback.onSuccess(cardEntities);
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
}
