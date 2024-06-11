package com.aliyun.auikits.karaoke.room.vm;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.auikits.karaoke.room.widget.helper.DialogHelper;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.base.card.CardListAdapter;
import com.aliyun.auikits.karaoke.room.base.feed.BizParameter;
import com.aliyun.auikits.karaoke.room.base.feed.ContentViewModel;
import com.aliyun.auikits.ktv.databinding.KtvLayoutChooseSongPanelBinding;
import com.aliyun.auikits.karaoke.room.model.content.KtvSearchSongListContentModel;
import com.aliyun.auikits.karaoke.room.model.entity.KTVMusicInfoWithUI;
import com.aliyun.auikits.karaoke.room.widget.card.CardTypeDef;
import com.aliyun.auikits.karaoke.room.widget.card.DefaultCardViewFactory;
import com.aliyun.auikits.karaoke.room.widget.card.KtvSongCard;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.List;

public class KtvSearchSongViewModel extends ViewModel implements ContentViewModel.OnDataUpdateCallback{
    KtvLayoutChooseSongPanelBinding mBinding;
    CardListAdapter mCardListAdapter;
    KtvSearchSongListContentModel mKtvSearchSongListContentModel;
    ContentViewModel mContentViewModel;
    BizParameter mBizParameter;
    ARTCKaraokeRoomController mRoomController;

    public void bind(Context context, KtvLayoutChooseSongPanelBinding binding, ARTCKaraokeRoomController roomController) {
        mBinding = binding;
        mRoomController = roomController;

        DefaultCardViewFactory factory = new DefaultCardViewFactory();
        factory.registerCardView(CardTypeDef.KTV_SEARCH_SONG_CARD, KtvSongCard.class);
        mCardListAdapter = new CardListAdapter(factory);
        mBinding.rvSearchSongList.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        mBinding.rvSearchSongList.setAdapter(mCardListAdapter);
//            binding.rvChooseSongList.addItemDecoration(new ChatItemDecoration((int) DisplayUtil.convertDpToPixel(6, this), (int) DisplayUtil.convertDpToPixel(12, this)));


        mKtvSearchSongListContentModel = new KtvSearchSongListContentModel(roomController);
        mContentViewModel = new ContentViewModel.Builder()
                .setContentModel(mKtvSearchSongListContentModel)
                .setBizParameter(mBizParameter)
                .setLoadMoreEnable(false)
                .setEmptyView(R.layout.ktv_search_song_empty_view)
                .setLoadingView(R.layout.ktv_loading_view)
                .setErrorView(R.layout.ktv_layout_error_view, R.id.btn_retry)
                .setOnDataUpdateCallback(this)
                .build();

        mCardListAdapter.addChildClickViewIds(R.id.btn_select_song);
        mCardListAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                CardEntity cardEntity = (CardEntity)adapter.getItem(position);
                KTVMusicInfoWithUI ktvMusicInfoWithUI = (KTVMusicInfoWithUI) cardEntity.bizData;

                if (!ktvMusicInfoWithUI.isIsChosen()) {
                    mRoomController.addMusic(ktvMusicInfoWithUI.getMusicInfo());
                    ktvMusicInfoWithUI.setIsChosen(true);
                    mKtvSearchSongListContentModel.updateContent(cardEntity, position);
                    DialogHelper.showAddMusicToast(mBinding.getRoot().getContext());
                }
            }
        });
        mCardListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter<?, ?> adapter, View view, int position) {
                CardEntity cardEntity = (CardEntity)adapter.getItem(position);
                KTVMusicInfoWithUI ktvMusicInfoWithUI = (KTVMusicInfoWithUI) cardEntity.bizData;

                if (!ktvMusicInfoWithUI.isIsChosen()) {
                    mRoomController.addMusic(ktvMusicInfoWithUI.getMusicInfo());
                    ktvMusicInfoWithUI.setIsChosen(true);
                    mKtvSearchSongListContentModel.updateContent(cardEntity, position);
                    DialogHelper.showAddMusicToast(mBinding.getRoot().getContext());
                }
            }
        });

        mBinding.srlSearchSongList.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                mContentViewModel.initData();
            }
        });

        mBinding.srlSearchSongList.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore( RefreshLayout refreshLayout) {
                mContentViewModel.loadMore();
            }
        });
        mContentViewModel.bindView(mCardListAdapter);

        mBinding.etSearchKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i("KtvSearchSongViewModel", "beforeTextChanged: " + s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("KtvSearchSongViewModel", "onTextChanged: " + s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("KtvSearchSongViewModel", "afterTextChanged: " + s);
                mKtvSearchSongListContentModel.setSearchKeyword(s.toString().trim());
                mContentViewModel.initData();
            }
        });
    }

    public void unbind() {

    }

    @Override
    public void onInitStart() {

    }

    @Override
    public void onInitEnd(boolean success, List<CardEntity> cardEntities) {
        mBinding.srlSearchSongList.finishRefresh();
    }

    @Override
    public void onLoadMoreStart() {
    }

    @Override
    public void onLoadMoreEnd(boolean success, List<CardEntity> cardEntities) {
        mBinding.srlSearchSongList.finishLoadMore();
    }

}
