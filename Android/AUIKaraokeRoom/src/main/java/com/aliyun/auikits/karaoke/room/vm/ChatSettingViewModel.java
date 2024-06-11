package com.aliyun.auikits.karaoke.room.vm;

import android.widget.CompoundButton;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;


public class ChatSettingViewModel extends ViewModel {

    public ObservableBoolean earbackSwitch = new ObservableBoolean(false);
    private ARTCKaraokeRoomController mRoomController;

    public void bind(ARTCKaraokeRoomController roomController) {
        mRoomController = roomController;
    }

    public void onEarbackSwitchChange(CompoundButton btn, boolean checked) {
        this.earbackSwitch.set(!this.earbackSwitch.get());
        if (null != mRoomController) {
            mRoomController.enableEarBack(checked);
        }
    }
}
