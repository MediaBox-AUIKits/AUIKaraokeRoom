package com.aliyun.auikits.karaoke.room.vm;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.ktv.databinding.KtvLayoutChooseSongPanelBinding;
import com.aliyun.auikits.karaoke.room.model.entity.ChatRoomCallback;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.aliyun.auikits.karaoke.bean.KTVMusicPlayListUpdateReason;
import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;
import com.aliyun.auikits.voiceroom.bean.UserInfo;

import java.util.List;

public class KtvChooseSongPanelViewModel extends ViewModel {

    public ObservableBoolean chooseSongPanelVisible = new ObservableBoolean(true);
    public ObservableInt panelHeaderVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt chooseSongPanelVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt searchSongPanelVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt searchEditTextVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt searchCancelButtonVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt chosenSongPanelVisibility = new ObservableInt(View.GONE);
    public ObservableField<String> chosenSongHeaderText = new ObservableField<>();

    public KtvChooseSongViewModel chooseSongViewModel;
    public KtvChosenSongViewModel chosenSongViewModel;
    public KtvSearchSongViewModel searchSongViewModel;

    private KtvLayoutChooseSongPanelBinding  mBinding;
    private Context mContext;
    private enum PanelMode {
        CHART,
        SEARCH,
        CHOSEN
    }
    public void bind(Context context, KtvLayoutChooseSongPanelBinding binding, ARTCKaraokeRoomController roomController) {
        mContext = context;
        mBinding = binding;

        ViewModelProvider viewModelProvider = new ViewModelProvider((ViewModelStoreOwner) context);

        chooseSongViewModel = viewModelProvider.get(KtvChooseSongViewModel.class);
        chosenSongViewModel = viewModelProvider.get(KtvChosenSongViewModel.class);
        searchSongViewModel = viewModelProvider.get(KtvSearchSongViewModel.class);

        chooseSongViewModel.bind(context, binding, roomController);
        chosenSongViewModel.bind(context, binding, roomController);
        searchSongViewModel.bind(context, binding, roomController);
        setChooseSongPanelVisible(PanelMode.CHART);
        mBinding.etSearchKeyword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.i("KtvChooseSongPanelViewModel", "OnFocusChangeListener: " + v.toString() + ", " + hasFocus);
                if (hasFocus) {
                    setChooseSongPanelVisible(PanelMode.SEARCH);
                }
            }
        });
        roomController.addObserver(new ChatRoomCallback() {
            @Override
            public void onMusicPlayingUpdated(KTVMusicPlayListUpdateReason reason, UserInfo operateUserInfo, List<KTVPlayingMusicInfo> kTVPlayingMusicInfoListUpdated, List<KTVPlayingMusicInfo> ktvPlayingMusicInfoList) {
                super.onMusicPlayingUpdated(reason, operateUserInfo, kTVPlayingMusicInfoListUpdated, ktvPlayingMusicInfoList);
                setChosenSongHeaderText(ktvPlayingMusicInfoList.size());
            }
        });
        List<KTVPlayingMusicInfo> localPlayingMusicInfoList = roomController.getLocalPlayMusicInfoList();
        setChosenSongHeaderText(null != localPlayingMusicInfoList ? localPlayingMusicInfoList.size() : 0);
    }

    public void unbind() {
        chooseSongViewModel.unbind();
        chosenSongViewModel.unbind();
        searchSongViewModel.unbind();
    }

    private void setChooseSongPanelVisible(PanelMode panelMode) {
        Log.i("KtvChooseSongPanelViewModel", "setChooseSongPanelVisible: " + panelMode);
        switch (panelMode) {
            case SEARCH:
                chooseSongPanelVisible.set(true);
                panelHeaderVisibility.set(View.GONE);
                chooseSongPanelVisibility.set(View.GONE);
                chosenSongPanelVisibility.set(View.GONE);
                searchSongPanelVisibility.set(View.VISIBLE);
                searchEditTextVisibility.set(View.VISIBLE);
                searchCancelButtonVisibility.set(View.VISIBLE);
                break;
            case CHOSEN:
                chooseSongPanelVisible.set(false);
                panelHeaderVisibility.set(View.VISIBLE);
                chooseSongPanelVisibility.set(View.GONE);
                chosenSongPanelVisibility.set(View.VISIBLE);
                searchSongPanelVisibility.set(View.GONE);
                searchEditTextVisibility.set(View.GONE);
                searchCancelButtonVisibility.set(View.GONE);
                break;
            case CHART:
            default:
                chooseSongPanelVisible.set(true);
                panelHeaderVisibility.set(View.VISIBLE);
                chooseSongPanelVisibility.set(View.VISIBLE);
                chosenSongPanelVisibility.set(View.GONE);
                searchSongPanelVisibility.set(View.GONE);
                searchEditTextVisibility.set(View.VISIBLE);
                searchCancelButtonVisibility.set(View.GONE);
                break;
        }
    }

    public void onChosenSongTitleClick(View view) {
        setChooseSongPanelVisible(PanelMode.CHOSEN);
    }

    public void onChooseSongTitleClick(View view) {
        setChooseSongPanelVisible(PanelMode.CHART);
    }

    public void onEdittextClick(View view) {
        setChooseSongPanelVisible(PanelMode.SEARCH);
    }

    public void onCancelClick(View view) {
        setChooseSongPanelVisible(PanelMode.CHART);
    }

    private void setChosenSongHeaderText(int num) {
        chosenSongHeaderText.set(
                mContext.getString(R.string.ktv_song_selected_with_number, String.valueOf(num))
        );
    }
}
