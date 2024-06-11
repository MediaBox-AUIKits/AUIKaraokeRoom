package com.aliyun.auikits.karaoke.room.vm;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.base.card.CardListAdapter;
import com.aliyun.auikits.karaoke.room.base.feed.BizParameter;
import com.aliyun.auikits.karaoke.room.base.feed.ContentViewModel;
import com.aliyun.auikits.ktv.databinding.KtvLayoutChooseSongPanelBinding;
import com.aliyun.auikits.karaoke.room.model.content.KtvChosenSongListContentModel;
import com.aliyun.auikits.karaoke.room.model.entity.ChatRoomCallback;
import com.aliyun.auikits.karaoke.room.model.entity.KtvChosenSong;
import com.aliyun.auikits.karaoke.room.widget.card.CardTypeDef;
import com.aliyun.auikits.karaoke.room.widget.card.DefaultCardViewFactory;
import com.aliyun.auikits.karaoke.room.widget.card.KtvChosenSongCard;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason;
import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.List;

public class KtvChosenSongViewModel extends ViewModel implements ContentViewModel.OnDataUpdateCallback{
    KtvLayoutChooseSongPanelBinding mBinding;
    CardListAdapter mCardListAdapter;
    KtvChosenSongListContentModel mKtvChosenSongListContentModel;
    ContentViewModel mContentViewModel;
    BizParameter mBizParameter;
    ARTCKaraokeRoomController mRoomController;
    public KtvChosenSongCardViewModel currentPlayingSongCardViewModel = new KtvChosenSongCardViewModel();

    public void bind(Context context, KtvLayoutChooseSongPanelBinding binding, ARTCKaraokeRoomController roomController) {
        mRoomController = roomController;
        mBinding = binding;

        DefaultCardViewFactory factory = new DefaultCardViewFactory();
        factory.registerCardView(CardTypeDef.KTV_CHOSEN_SONG_CARD, KtvChosenSongCard.class);
        mCardListAdapter = new CardListAdapter(factory);
        mBinding.rvAlreadyChosenSongList.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        mBinding.rvAlreadyChosenSongList.setAdapter(mCardListAdapter);
//            binding.rvChooseSongList.addItemDecoration(new ChatItemDecoration((int) DisplayUtil.convertDpToPixel(6, this), (int) DisplayUtil.convertDpToPixel(12, this)));
        mBinding.srlAlreadyChosenSongList.setEnableLoadMore(false);

        mKtvChosenSongListContentModel = new KtvChosenSongListContentModel(roomController);
        mContentViewModel = new ContentViewModel.Builder()
                .setContentModel(mKtvChosenSongListContentModel)
                .setBizParameter(mBizParameter)
                .setLoadMoreEnable(false)
                .setEmptyView(R.layout.ktv_chosen_song_empty_view)
                .setLoadingView(R.layout.ktv_loading_view)
                .setErrorView(R.layout.ktv_layout_error_view, R.id.btn_retry)
                .setOnDataUpdateCallback(this)
                .build();

        mCardListAdapter.addChildClickViewIds(R.id.iv_action_skip);
        mCardListAdapter.addChildClickViewIds(R.id.iv_action_top);
        mCardListAdapter.addChildClickViewIds(R.id.iv_action_trash);
        mCardListAdapter.addChildClickViewIds(R.id.iv_action_playing);
        mCardListAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

                KtvChosenSong ktvChosenSong = (KtvChosenSong)((CardEntity) adapter.getItem(position)).bizData;
                if (view.getId() == R.id.iv_action_skip) {
                    mRoomController.skipMusic();
                } else if (view.getId() == R.id.iv_action_trash) {
                    mRoomController.removeMusic(ktvChosenSong.getSongInfo());
                } else if (view.getId() == R.id.iv_action_top) {
                    mRoomController.pinMusic(ktvChosenSong.getSongInfo());
                }
            }
        });
        mCardListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter<?, ?> adapter, View view, int position) {

            }
        });

        mBinding.srlAlreadyChosenSongList.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                mContentViewModel.initData();
            }
        });

        mBinding.srlAlreadyChosenSongList.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore( RefreshLayout refreshLayout) {
                mContentViewModel.loadMore();
            }
        });
        mContentViewModel.bindView(mCardListAdapter);

        roomController.addObserver(new ChatRoomCallback() {

            @Override
            public void onMusicPlayingUpdated(KTVMusicPlayListUpdateReason reason, UserInfo operateUserInfo, List<KTVPlayingMusicInfo> kTVPlayingMusicInfoListUpdated, List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList) {
                mKtvChosenSongListContentModel.updatePlayingMusicInfoList(ktvPlayingMusicInfoList);

                if (!ktvPlayingMusicInfoList.isEmpty()) {
                    KTVPlayingMusicInfo currentPlayingMusicInfo = ktvPlayingMusicInfoList.get(0);
                    setCurrentPlayingMusicInfo(currentPlayingMusicInfo);
                    mCardListAdapter.setEmptyView(R.layout.ktv_chosen_song_transparent_empty_view);
                } else {
                    currentPlayingSongCardViewModel.bind(null);
                    mCardListAdapter.setEmptyView(R.layout.ktv_chosen_song_empty_view);
                }
            }
        });
        setCurrentPlayingMusicInfo(roomController.getCurrentPlayingMusicInfo());

        mBinding.llCurrentPlayingCard.ivActionSkip.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mRoomController.skipMusic();
            }
            return false;
        });
    }

    public void unbind() {
    }

    @Override
    public void onInitStart() {

    }

    @Override
    public void onInitEnd(boolean success, List<CardEntity> cardEntities) {
        mBinding.srlAlreadyChosenSongList.finishRefresh();
    }

    @Override
    public void onLoadMoreStart() {
    }

    @Override
    public void onLoadMoreEnd(boolean success, List<CardEntity> cardEntities) {
        mBinding.srlAlreadyChosenSongList.finishLoadMore();
    }

    private void setCurrentPlayingMusicInfo(KTVPlayingMusicInfo currentPlayingMusicInfo) {
        if (null != currentPlayingMusicInfo) {
            KtvChosenSong ktvChosenSong = KtvChosenSong.fromKtvPlayingMusicInfo(currentPlayingMusicInfo);
            ktvChosenSong.setSongOrder(0);
            ktvChosenSong.setCanPin(mRoomController.checkCanPinMusic(currentPlayingMusicInfo));
            ktvChosenSong.setCanTrash(mRoomController.checkCanRemoveMusic(currentPlayingMusicInfo));
            ktvChosenSong.setCanSkip(mRoomController.checkCanSkipMusic(currentPlayingMusicInfo));
            currentPlayingSongCardViewModel.bind(ktvChosenSong);
        } else {
            currentPlayingSongCardViewModel.bind(null);
        }
    }

}
