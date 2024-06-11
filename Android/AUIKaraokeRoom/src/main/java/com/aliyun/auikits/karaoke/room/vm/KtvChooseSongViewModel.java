package com.aliyun.auikits.karaoke.room.vm;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.aliyun.auikits.karaoke.room.base.card.CardEntity;
import com.aliyun.auikits.karaoke.room.widget.helper.DialogHelper;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.karaoke.room.base.card.CardListAdapter;
import com.aliyun.auikits.karaoke.room.base.feed.BizParameter;
import com.aliyun.auikits.karaoke.room.base.feed.ContentViewModel;
import com.aliyun.auikits.karaoke.room.base.feed.IBizCallback;
import com.aliyun.auikits.ktv.databinding.KtvLayoutChooseSongListBinding;
import com.aliyun.auikits.ktv.databinding.KtvLayoutChooseSongPanelBinding;
import com.aliyun.auikits.karaoke.room.model.content.KtvChartContentModel;
import com.aliyun.auikits.karaoke.room.model.content.KtvChartSongListContentModel;
import com.aliyun.auikits.karaoke.room.model.entity.KTVMusicInfoWithUI;
import com.aliyun.auikits.karaoke.room.model.entity.KtvChart;
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

import java.util.ArrayList;
import java.util.List;

public class KtvChooseSongViewModel extends ViewModel {
    KtvChartContentModel mKtvChartContentModel;
    Context mContext;

    public void bind(Context context, KtvLayoutChooseSongPanelBinding binding, ARTCKaraokeRoomController roomController) {
        mContext = context;
        mKtvChartContentModel = new KtvChartContentModel(roomController);

        binding.tabSongRank.setupWithViewPager(binding.viewpagerSongList);
        setupViewPager(context, binding.viewpagerSongList, roomController);
    }

    public void unbind() {
        clearAllFragments();
    }

    public void clearAllFragments() {
        // 获取FragmentManager
        FragmentManager fragmentManager = ((AppCompatActivity)mContext).getSupportFragmentManager();

        // 开始一个Fragment的事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 获取当前FragmentManager中所有Fragment的列表
        List<Fragment> fragments = fragmentManager.getFragments();

        if (fragments != null) {
            // 遍历所有Fragment
            for(Fragment fragment : fragments) {
                if (fragment instanceof KtvChartFragment) {
                    // 对每个Fragment执行移除操作
                    transaction.remove(fragment);
                }
            }
            // 提交事务
            transaction.commitNow(); // 或者你可以调用commit()然后稍后手动调用executePendingTransactions()，如果你更需要更多控制
        }
    }

    private void setupViewPager(Context context, ViewPager viewPager, ARTCKaraokeRoomController roomController) {

        mKtvChartContentModel.initData(null, new IBizCallback<CardEntity>() {
            @Override
            public void onSuccess(List<CardEntity> data) {

                ViewPagerAdapter adapter = new ViewPagerAdapter(
                        ((AppCompatActivity)context).getSupportFragmentManager()
                );

                for (CardEntity entity : data) {
                    KtvChart ktvChart = (KtvChart) entity.bizData;
                    adapter.addFragment(new KtvChartFragment(ktvChart, roomController), ktvChart.getChartName());
                }

                viewPager.setAdapter(adapter);
            }

            @Override
            public void onError(int code, String msg) {

            }
        });

    }

    // FragmentPagerAdapter的一个简单实现
    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
    }

    public static class KtvChartFragment extends Fragment implements ContentViewModel.OnDataUpdateCallback {
        KtvLayoutChooseSongListBinding mBinding;
        KtvChart mKtvChart;
        CardListAdapter mCardListAdapter;
        KtvChartSongListContentModel mKtvChartSongListContentModel;
        ContentViewModel mContentViewModel;
        BizParameter mBizParameter;
        ARTCKaraokeRoomController mRoomController;

        public KtvChartFragment(KtvChart ktvChart, ARTCKaraokeRoomController roomController) {
            mKtvChart = ktvChart;
            printLifeCycle("KtvChartFragment");
            mRoomController = roomController;
            mBizParameter = new BizParameter();
            mBizParameter.append(KtvChartSongListContentModel.BIZ_PARAM_CHART_ID, null != mKtvChart ? mKtvChart.getChartId() : "");
            mBizParameter.append(KtvChartSongListContentModel.BIZ_PARAM_CHART_NAME, null != mKtvChart ? mKtvChart.getChartName() : "");
        }

        private void printLifeCycle(String lifeCycle) {
            if (null != mKtvChart) {
                Log.i(
                        "SampleFragment", lifeCycle + "@" + hashCode() + " [id: " + mKtvChart.getChartId() + ", name: " + mKtvChart.getChartName() + "]"
                );
            } else {
                Log.i(
                        "SampleFragment", lifeCycle + " null"
                );

            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            printLifeCycle("onCreateView");

            mBinding = DataBindingUtil.inflate(inflater, R.layout.ktv_layout_choose_song_list, container, false);

            DefaultCardViewFactory factory = new DefaultCardViewFactory();
            factory.registerCardView(CardTypeDef.KTV_SEARCH_SONG_CARD, KtvSongCard.class);
            mCardListAdapter = new CardListAdapter(factory);
            mBinding.rvChooseSongList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
            mBinding.rvChooseSongList.setAdapter(mCardListAdapter);
//            binding.rvChooseSongList.addItemDecoration(new ChatItemDecoration((int) DisplayUtil.convertDpToPixel(6, this), (int) DisplayUtil.convertDpToPixel(12, this)));

            mKtvChartSongListContentModel = new KtvChartSongListContentModel(mRoomController);
            mContentViewModel = new ContentViewModel.Builder()
                    .setContentModel(mKtvChartSongListContentModel)
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
                        mKtvChartSongListContentModel.updateContent(cardEntity, position);
                        DialogHelper.showAddMusicToast(getContext());
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
                        mKtvChartSongListContentModel.updateContent(cardEntity, position);
                        DialogHelper.showAddMusicToast(getContext());
                    }
                }
            });

            mBinding.srlChooseSongList.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshLayout) {
                    mContentViewModel.initData();
                }
            });

            mBinding.srlChooseSongList.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore( RefreshLayout refreshLayout) {
                    mContentViewModel.loadMore();
                }
            });
            mContentViewModel.bindView(mCardListAdapter);

            return mBinding.getRoot();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mContentViewModel.unBind();
            printLifeCycle("onDestroyView");
        }

        @Override
        public void onInitStart() {
            printLifeCycle("onInitStart");
        }

        @Override
        public void onInitEnd(boolean success, List<CardEntity> cardEntities) {
            printLifeCycle("onInitEnd");
            mBinding.srlChooseSongList.finishRefresh();
        }

        @Override
        public void onLoadMoreStart() {
            printLifeCycle("onLoadMoreStart");
        }

        @Override
        public void onLoadMoreEnd(boolean success, List<CardEntity> cardEntities) {
            printLifeCycle("onLoadMoreEnd");
            mBinding.srlChooseSongList.finishLoadMore();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            printLifeCycle("onCreate");
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onAttach(Context context) {
            printLifeCycle("onAttach");
            super.onAttach(context);
        }

        @Override
        public void onDetach() {
            printLifeCycle("onDetach");
            super.onDetach();
        }

        @Override
        public void onDestroy() {
            printLifeCycle("onDestroy");
            super.onDestroy();
        }

        @Override
        public void onStart() {
            printLifeCycle("onStart");
            super.onStart();
        }

        @Override
        public void onStop() {
            printLifeCycle("onStop");
            super.onStop();
        }

        @Override
        public void onResume() {
            printLifeCycle("onResume");
            super.onResume();
        }

        @Override
        public void onPause() {
            printLifeCycle("onPause");
            super.onPause();
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            printLifeCycle("onViewCreated");
            super.onViewCreated(view, savedInstanceState);
        }
    }
}
