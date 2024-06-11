package com.aliyun.auikits.karaoke.room.widget.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.biz.ktv.KTVServerConstant;
import com.aliyun.auikits.karaoke.room.KaraokeActivity;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.karaoke.room.base.network.RetrofitManager;
import com.aliyun.auikits.ktv.databinding.KtvDialogFirstEnterBinding;
import com.aliyun.auikits.ktv.databinding.KtvDialogKtvScoreBinding;
import com.aliyun.auikits.ktv.databinding.KtvLayoutChooseSongPanelBinding;
import com.aliyun.auikits.ktv.databinding.KtvDialogExitBinding;
import com.aliyun.auikits.karaoke.room.model.api.KTVRoomApi;
import com.aliyun.auikits.karaoke.room.model.entity.ChatMember;
import com.aliyun.auikits.karaoke.room.model.entity.network.CloseRoomRequest;
import com.aliyun.auikits.karaoke.room.model.entity.network.CloseRoomResponse;
import com.aliyun.auikits.karaoke.room.service.KTVRoomManager;
import com.aliyun.auikits.karaoke.room.service.KTVRoomService;
import com.aliyun.auikits.karaoke.room.util.DisplayUtil;
import com.aliyun.auikits.karaoke.room.util.ImageTools;
import com.aliyun.auikits.karaoke.room.util.ToastHelper;
import com.aliyun.auikits.karaoke.room.vm.KtvChooseSongPanelViewModel;
import com.aliyun.auikits.karaoke.room.vm.KtvScoreDialogViewModel;
import com.aliyun.auikits.karaoke.room.widget.list.CustomViewHolder;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DialogHelper {

    public static void showCloseDialog(Context context, ARTCKaraokeRoomController roomController, boolean isCompere) {
        if(context instanceof Activity) {

            KtvDialogExitBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ktv_dialog_exit, null, false);
            if(isCompere) {
                binding.tvDialogTips.setText(R.string.voicechat_exit_room_tip_for_compere);
            } else {
                binding.tvDialogTips.setText(R.string.voicechat_exit_room_tip_for_other);
            }
            CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
            DialogPlus dialog = DialogPlus.newDialog(context)
                    .setContentHolder(viewHolder)
                    .setGravity(Gravity.CENTER)
                    .setExpanded(false)
                    .setOverlayBackgroundResource(android.R.color.transparent)
                    .setContentBackgroundResource(R.drawable.voicechat_dialog_exit_bg)
                    .setOnClickListener((dialog1, v) -> {
                        if(v.getId() == R.id.btn_confirm) {
                            Observable<Integer> exitRoomObservable = null;
                            if(isCompere) {
                                String authorization = (String) KTVRoomManager.getInstance().getGlobalParam(KaraokeActivity.KEY_AUTHORIZATION);
                                if(authorization != null) {
                                    CloseRoomRequest closeRoomRequest = new CloseRoomRequest();
                                    closeRoomRequest.id = roomController.getRoomInfo().roomId;
                                    closeRoomRequest.user_id = roomController.getCurrentUser().userId;
                                    exitRoomObservable = RetrofitManager.getRetrofit(KTVServerConstant.HOST).create(KTVRoomApi.class)
                                        .dismissRoom(authorization, closeRoomRequest)
                                            .flatMap(new Function<CloseRoomResponse, ObservableSource<Integer>>() {
                                                @Override
                                                public ObservableSource<Integer> apply(CloseRoomResponse closeRoomResponse) throws Throwable {
                                                    if(closeRoomResponse.success) {
                                                        return KTVRoomService.exitRoom(roomController, isCompere);
                                                    }
                                                    return Observable.just(-1);
                                                }
                                            });
                                } else {
                                    exitRoomObservable = Observable.just(-1);
                                }
                            } else {
                                exitRoomObservable = KTVRoomService.exitRoom(roomController, isCompere);
                            }
                            exitRoomObservable
                              .subscribeOn(Schedulers.io())
                             .observeOn(AndroidSchedulers.mainThread())
                             .subscribe(new Consumer<Integer>() {
                                 @Override
                                 public void accept(Integer code) throws Throwable {
                                     Log.v("DialogHelper", "exit Room:" + code);
                                 }
                             }, new Consumer<Throwable>() {
                                 @Override
                                 public void accept(Throwable throwable) throws Throwable {
                                     throwable.printStackTrace();
                                 }
                             });

                            Intent returnIntent = new Intent();
                            if(isCompere) {
                                returnIntent.putExtra(KaraokeActivity.KEY_ROOM_DISMISS, true);
                            }
                            returnIntent.putExtra(KaraokeActivity.KEY_ROOM_ID, roomController.getRoomInfo().roomId);
                            ((Activity) context).setResult(Activity.RESULT_OK , returnIntent);
                            dialog1.dismiss();
                            ((Activity) context).finish();
                        } else if(v.getId() == R.id.btn_cancel) {
                            dialog1.dismiss();
                        }
                    })
                    .create();
            dialog.show();


        }
    }

    public static void showFirstEnterDialog(Context context, ChatMember chatMember) {
        if(context instanceof Activity) {
            KtvDialogFirstEnterBinding  binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ktv_dialog_first_enter, null, false);
            ImageTools.loadImage(binding.civProfileImage,
                    chatMember.getAvatar(),
                    context.getDrawable(R.drawable.voicechat_ic_avatar_default),
                    context.getDrawable(R.drawable.voicechat_ic_avatar_default),
                    null);
            binding.tvProfileName.setText(chatMember.getName());
            CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
            DialogPlus dialog = DialogPlus.newDialog(context)
                    .setContentHolder(viewHolder)
                    .setGravity(Gravity.CENTER)
                    .setExpanded(false)
                    .setOverlayBackgroundResource(android.R.color.transparent)
                    .setContentBackgroundResource(R.drawable.voicechat_dialog_exit_bg)
                    .setOnClickListener((dialog1, v) -> {
                        dialog1.dismiss();
                    })
                    .create();
            dialog.show();
        }
    }

    // 增加房间信息，获取已点歌曲
    public static void showChooseSongDialog(Context context, ARTCKaraokeRoomController roomController) {
        if(context instanceof Activity) {
            KtvLayoutChooseSongPanelBinding  binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ktv_layout_choose_song_panel, null, false);

            KtvChooseSongPanelViewModel ktvChooseSongPanelViewModel = new KtvChooseSongPanelViewModel();
            ktvChooseSongPanelViewModel.bind(context, binding, roomController);
            binding.setViewModel(ktvChooseSongPanelViewModel);

            CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
            DialogPlus dialog = DialogPlus.newDialog(context)
                    .setContentHolder(viewHolder)
                    .setGravity(Gravity.BOTTOM)
                    .setExpanded(true, DisplayUtil.dip2px(570))
                    .setOverlayBackgroundResource(android.R.color.transparent)
                    .setContentBackgroundResource(R.color.voicechat_background)
                    .setOnClickListener((dialog1, v) -> {
//                        dialog1.dismiss();
                    })
                    .setOnDismissListener(new OnDismissListener() {
                        @Override
                        public void onDismiss(DialogPlus dialog) {
                            ktvChooseSongPanelViewModel.unbind();
                        }
                    })
                    .create();
            dialog.show();
        }
    }

    public static void showScoreDialog(Context context, int score) {
        if(context instanceof Activity) {
            KtvDialogKtvScoreBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ktv_dialog_ktv_score, null, false);

            KtvScoreDialogViewModel ktvScoreDialogViewModel = new KtvScoreDialogViewModel();
            ktvScoreDialogViewModel.bind(score);
            binding.setViewModel(ktvScoreDialogViewModel);

            CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
            DialogPlus dialog = DialogPlus.newDialog(context)
                    .setContentHolder(viewHolder)
                    .setGravity(Gravity.CENTER)
                    .setExpanded(false)
                    .setOverlayBackgroundResource(android.R.color.transparent)
//                    .setContentBackgroundResource(R.drawable.voicechat_dialog_exit_bg)
                    .setContentBackgroundResource(android.R.color.transparent)
                    .setOnClickListener((dialog1, v) -> {
                        dialog1.dismiss();
                    })
                    .create();
            dialog.show();
            binding.getRoot().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            }, 3000);
        }
    }

    public static void showAddMusicToast(Context context) {
        if (null != context) {
            ToastHelper.showToast(context, R.string.ktv_toast_music_downloading, Toast.LENGTH_SHORT);
        }
    }
}
