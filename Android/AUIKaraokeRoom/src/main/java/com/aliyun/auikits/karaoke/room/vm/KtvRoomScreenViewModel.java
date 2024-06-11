package com.aliyun.auikits.karaoke.room.vm;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.karaoke.room.widget.helper.DialogHelper;
import com.aliyun.auikits.karaoke.room.widget.pitch.model.MusicPitch;
import com.aliyun.auikits.ktv.R;
import com.aliyun.auikits.ktv.databinding.KtvActivityRoomScreenBinding;
import com.aliyun.auikits.karaoke.room.model.entity.ChatRoom;
import com.aliyun.auikits.karaoke.room.model.entity.ChatRoomCallback;
import com.aliyun.auikits.karaoke.room.util.DisplayTextUtil;
import com.aliyun.auikits.karaoke.room.util.ToastHelper;
import com.aliyun.auikits.karaoke.room.widget.lyric.LyricView;
import com.aliyun.auikits.karaoke.room.widget.lyric.OnLyricLineFinishedListener;
import com.aliyun.auikits.karaoke.room.widget.lyric.model.LyricLine;
import com.aliyun.auikits.karaoke.room.widget.pitch.PitchViewHelper;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomController;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomEngineCallback;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomMusicLibraryCallback;
import com.aliyun.auikits.karaoke.ARTCKaraokeRoomMusicPlayingListServiceCallback;
import com.aliyun.auikits.karaoke.bean.KTVMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVMusicPlayState;
import com.aliyun.auikits.karaoke.bean.KTVMusicPrepareState;
import com.aliyun.auikits.karaoke.bean.KTVPlayingMusicInfo;
import com.aliyun.auikits.karaoke.bean.KTVRoomState;
import com.aliyun.auikits.karaoke.bean.KTVSingerRole;
import com.aliyun.auikits.voiceroom.bean.UserInfo;

import java.util.List;

public class KtvRoomScreenViewModel extends ViewModel {
    private static final String TAG = "KtvRoomScreenViewModel";
    private enum ScreenMode {
        Init, // 初始化界面
        InitMic, // 麦位用户（包含主持人）初始化界面
        SingerLead, // 主唱人界面
        SingerAccompany, // 合唱人界面
        AnchorJoinMic, // 主持人麦位界面
        AudienceJoinMic, // 麦位观众
        Audience // 普通观众
    }

    private enum ScreenPlayMode {
        Init,
        PrePlay,
        Play
    }

    public ObservableInt centerSongSelectorEntranceVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt centerTipsVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt rightSongSelectorEntranceVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt screenStatusBarVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt screenStatusBarScoreVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt screenPitchVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt screenLyricVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt screenSongNoticeVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt screenActionBarVisibility = new ObservableInt(View.VISIBLE);

    public ObservableInt actionButtonPlayVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt actionButtonSkipVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt actionButtonJoinVisibility = new ObservableInt(View.VISIBLE);
    public ObservableInt actionButtonAccompanimentVisibility = new ObservableInt(View.VISIBLE);

    public ObservableBoolean isSongUseAccompaniment = new ObservableBoolean(true);
    public ObservableBoolean isSongPlaying = new ObservableBoolean(true);

    public ObservableField<String> statusBarSongNameDesc = new ObservableField<>();
    public ObservableField<String> statusBarSongDurationDesc = new ObservableField<>();
    public ObservableField<String> statusBarSumScoreDesc = new ObservableField<>();
    public ObservableField<String> noticeSongNameDesc = new ObservableField<>();
    public ObservableField<String> noticeProgressDesc = new ObservableField<>();
    public ObservableField<String> btnJoinText = new ObservableField<>();

    private KtvActivityRoomScreenBinding mBinding;

    private ARTCKaraokeRoomController mRoomController;

    private String mCurrentSongId;
    private KTVPlayingMusicInfo mCurrentMusicInfo;

    private int mCurrentPitch = 0;
    private KTVRoomState mCurrentRoomState = KTVRoomState.Init;

    public void bind(KtvActivityRoomScreenBinding binding, ChatRoom chatRoom, ARTCKaraokeRoomController roomController) {
        mRoomController = roomController;
        mBinding = binding;
        switchScreenMode(ScreenMode.Init);
        mRoomController.addObserver(new ChatRoomCallback() {
            @Override
            public void onJoin(String roomId, String uid) {
                super.onJoin(roomId, uid);
                if (!mRoomController.isAnchor()) {
                    mRoomController.requestRemoteKtvState();
                }
            }

            @Override
            public void onMusicWillPlayNext(KTVPlayingMusicInfo ktvPlayingMusicInfo, long notifyMilliseconds) {
                super.onMusicWillPlayNext(ktvPlayingMusicInfo, notifyMilliseconds);
                mCurrentMusicInfo = ktvPlayingMusicInfo;
                startPreCountDownCountdown(ktvPlayingMusicInfo, notifyMilliseconds);
                loadPitch();
                isSongPlaying.set(true);
                isSongUseAccompaniment.set(true);
                if (mRoomController.isLeadSinger()) {
                    ToastHelper.showToast(mBinding.getRoot().getContext(), R.string.ktv_toast_lead_sing, Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onMicUserSpeakStateChanged(UserInfo user) {
                super.onMicUserSpeakStateChanged(user);
                int pitch = null != user ? user.pitch : 0;
                Log.i(TAG, "onMicUserSpeakStateChanged : " + pitch);
                mCurrentPitch = pitch;
            }

            @Override
            public void onSingingPlayStateChanged(KTVMusicPlayState oldState, KTVMusicPlayState newState, int score) {
                super.onSingingPlayStateChanged(oldState, newState, score);
                if (newState == KTVMusicPlayState.Completed) {
                    int showScore = score;
                    if (mRoomController.isJoinSinger()) {
                        showScore = mBinding.containerPitchView.getFinalScore();
                    }
                    DialogHelper.showScoreDialog(mBinding.getRoot().getContext(), showScore);
                }
            }

            @Override
            public void onRoomStateChanged(KTVRoomState oldState, KTVRoomState newState, String songId) {
                super.onRoomStateChanged(oldState, newState, songId);

                mCurrentRoomState = newState;
                updateScreenModeByRoomController();
                mCurrentSongId = songId;

                // 播放中途进入的用户主动加载歌曲信息
                if (newState == KTVRoomState.Singing && null == mCurrentMusicInfo) {
                    mRoomController.fetchMusicPlayingList(new ARTCKaraokeRoomMusicPlayingListServiceCallback() {
                        @Override
                        public void onMusicPlayingListCallback(List<KTVPlayingMusicInfo> playingMusicInfoList) {

                            if (null != playingMusicInfoList) {
                                for (KTVPlayingMusicInfo ktvPlayingMusicInfo : playingMusicInfoList) {
                                    if (null != ktvPlayingMusicInfo && TextUtils.equals(ktvPlayingMusicInfo.songID, mCurrentSongId)) {
                                        mCurrentMusicInfo = ktvPlayingMusicInfo;
                                        setStatusBarSongNameDesc();
                                        loadPitch();
                                        break;
                                    }
                                }
                            }
                        }

                        @Override
                        public void onMusicPlayingJoinerListCallback(KTVPlayingMusicInfo ktvPlayingMusicInfo) {

                        }

                        @Override
                        public void onFail(int errorCode, String errorMsg) {

                        }
                    });
                }
            }

            @Override
            public void onRoomMicListChanged(List<UserInfo> micUsers) {
                super.onRoomMicListChanged(micUsers);
                updateScreenModeByRoomController();
            }

            @Override
            public void onJoinedMic(UserInfo user) {
                super.onJoinedMic(user);
                updateScreenModeByRoomController();
            }

            @Override
            public void onLeavedMic(UserInfo user) {
                super.onLeavedMic(user);
                updateScreenModeByRoomController();
            }

            @Override
            public void onNetworkStateChanged(UserInfo user) {
                super.onNetworkStateChanged(user);
                if (mRoomController.isJoinSinger() || mRoomController.isLeadSinger()) {
                    ToastHelper.showToast(mBinding.getRoot().getContext(), R.string.ktv_toast_network_weak, Toast.LENGTH_SHORT);
                }
            }
        });
        mRoomController.addRoomEngineCallback(new ARTCKaraokeRoomEngineCallback() {
            @Override
            public void onMusicPrepareStateUpdate(KTVMusicPrepareState state) {
                Log.i(TAG, "onMusicPrepareStateUpdate : " + state);
            }

            @Override
            public void onMusicPlayStateUpdate(KTVMusicPlayState state) {
                Log.i(TAG, "onMusicPlayStateUpdate : " + state);
                if (state == KTVMusicPlayState.Playing) {
                    switchScreenPlayMode(ScreenPlayMode.Play);
                    isSongPlaying.set(true);
                } else if (state == KTVMusicPlayState.Paused) {
                    isSongPlaying.set(false);
                }
            }


            @Override
            public void onMusicPlayProgressUpdate(long millisecond) {
                Log.i(TAG, "onMusicPlayProgressUpdate : " + millisecond);
                mBinding.containerLyricView.setCurrentTimeMillis(millisecond);
                setStatusBarSongDurationDesc(millisecond);
                mBinding.containerPitchView.setCurrentSongProgress(millisecond, mCurrentPitch);
                mRoomController.updateScore(mBinding.containerPitchView.getFinalScore());
                setStatusBarSumScoreDesc(mBinding.containerPitchView.getFinalScore());
            }

            @Override
            public void onSingerRoleUpdate(KTVSingerRole oldRole, KTVSingerRole newRole) {
                Log.i(TAG, "onSingerRoleUpdate : " + oldRole + " -> " +  newRole);
                updateScreenModeByRoomController();
            }
        });
        if (!mRoomController.isAnchor() && mRoomController.isJoinRoom()) {
            mRoomController.requestRemoteKtvState();
        }
        mBinding.containerLyricView.setOnLyricFinishLineListener(new OnLyricLineFinishedListener() {
            @Override
            public void onLyricLineFinished(int position, LyricLine line) {
                Log.i(TAG, "onLyricLineFinished : " + position + " : " + line.content);
                mBinding.containerPitchView.addScore(mBinding.containerPitchView.getCurLineScore());
            }
        });
    }

    public void unbind() {

    }

    public void onChooseSongClick(View view) {
        DialogHelper.showChooseSongDialog(view.getContext(), mRoomController);
    }

    public void onButtonPlayClick(View view) {
        if (isSongPlaying.get()) {
            mRoomController.pauseMusic();
        } else {
            mRoomController.resumeMusic();
        }
    }

    public void onButtonSkipClick(View view) {
        mRoomController.skipMusic();
//        mRoomController.seekMusicTo(mRoomController.getMusicTotalDuration() - 10000);
    }

    public void onButtonJoinClick(View view) {
        if (mRoomController.isJoinSinger()) {
            mRoomController.leaveSinging(null);
        } else {
            mRoomController.joinSinging(null);
            ToastHelper.showToast(view.getContext(), R.string.ktv_toast_music_downloading, Toast.LENGTH_SHORT);
        }
    }

    public void onButtonAccompanimentClick(View view) {
        if (!isSongUseAccompaniment.get()) {
            isSongUseAccompaniment.set(true);
            mRoomController.setMusicAccompanimentMode(false);
        }
    }

    public void onButtonOriginalSingClick(View view) {
        if (isSongUseAccompaniment.get()) {
            isSongUseAccompaniment.set(false);
            mRoomController.setMusicAccompanimentMode(true);
        }
    }

    public void updateScreenModeByRoomController() {
        switch(mCurrentRoomState) {
            case Waiting:
            case Singing:
                if (mRoomController.isLeadSinger()) {
                    switchScreenMode(ScreenMode.SingerLead);
                } else if (mRoomController.isJoinSinger()) {
                    switchScreenMode(ScreenMode.SingerAccompany);
                } else if (mRoomController.isAnchor()) {
                    switchScreenMode(ScreenMode.AnchorJoinMic);
                } else if (mRoomController.isJoinMic()) {
                    switchScreenMode(ScreenMode.AudienceJoinMic);
                } else {
                    switchScreenMode(ScreenMode.Audience);
                }
                if (mCurrentRoomState == KTVRoomState.Waiting) {
                    switchScreenPlayMode(ScreenPlayMode.PrePlay);
                } else if (mCurrentRoomState == KTVRoomState.Singing) {
                    switchScreenPlayMode(ScreenPlayMode.Play);
                }
                break;
            case Init:
            default:
                if (mRoomController.isJoinMic()) {
                    switchScreenMode(ScreenMode.InitMic);
                } else {
                    switchScreenMode(ScreenMode.Init);
                }
                break;
        }
    }

    public void switchScreenMode(ScreenMode screenMode) {
        switch (screenMode) {
            case SingerLead:
                centerSongSelectorEntranceVisibility.set(View.GONE);
                centerTipsVisibility.set(View.GONE);
                screenStatusBarVisibility.set(View.VISIBLE);
                screenPitchVisibility.set(View.VISIBLE);
                switchScreenPlayMode(ScreenPlayMode.PrePlay);
                screenActionBarVisibility.set(View.VISIBLE);
                rightSongSelectorEntranceVisibility.set(View.VISIBLE);
                screenStatusBarScoreVisibility.set(View.VISIBLE);
                actionButtonPlayVisibility.set(View.VISIBLE);
                actionButtonSkipVisibility.set(View.VISIBLE);
                actionButtonJoinVisibility.set(View.GONE);
                actionButtonAccompanimentVisibility.set(View.VISIBLE);
                mBinding.containerLyricView.setLinesPattern(LyricView.LinesPattern.TWO_LINES);
                break;
            case SingerAccompany:
                centerSongSelectorEntranceVisibility.set(View.GONE);
                centerTipsVisibility.set(View.GONE);
                screenStatusBarVisibility.set(View.VISIBLE);
                screenPitchVisibility.set(View.VISIBLE);
                switchScreenPlayMode(ScreenPlayMode.PrePlay);
                screenActionBarVisibility.set(View.VISIBLE);
                rightSongSelectorEntranceVisibility.set(View.VISIBLE);
                screenStatusBarScoreVisibility.set(View.VISIBLE);
                actionButtonPlayVisibility.set(View.GONE);
                actionButtonSkipVisibility.set(View.GONE);
                actionButtonJoinVisibility.set(View.VISIBLE);
                btnJoinText.set(mBinding.getRoot().getContext().getString(R.string.ktv_leave));
                actionButtonAccompanimentVisibility.set(View.VISIBLE);
                mBinding.containerLyricView.setLinesPattern(LyricView.LinesPattern.TWO_LINES);
                break;
            case AnchorJoinMic:
                centerSongSelectorEntranceVisibility.set(View.GONE);
                centerTipsVisibility.set(View.GONE);
                screenStatusBarVisibility.set(View.VISIBLE);
                screenPitchVisibility.set(View.GONE);
                switchScreenPlayMode(ScreenPlayMode.PrePlay);
                screenActionBarVisibility.set(View.VISIBLE);
                rightSongSelectorEntranceVisibility.set(View.VISIBLE);
                screenStatusBarScoreVisibility.set(View.GONE);
                actionButtonPlayVisibility.set(View.GONE);
                actionButtonSkipVisibility.set(View.VISIBLE);
                actionButtonJoinVisibility.set(View.VISIBLE);
                btnJoinText.set(mBinding.getRoot().getContext().getString(R.string.ktv_join));
                actionButtonAccompanimentVisibility.set(View.GONE);
                mBinding.containerLyricView.setLinesPattern(LyricView.LinesPattern.Multi_LINES);
                break;
            case AudienceJoinMic:
                centerSongSelectorEntranceVisibility.set(View.GONE);
                centerTipsVisibility.set(View.GONE);
                screenStatusBarVisibility.set(View.VISIBLE);
                screenPitchVisibility.set(View.GONE);
                switchScreenPlayMode(ScreenPlayMode.PrePlay);
                screenActionBarVisibility.set(View.VISIBLE);
                rightSongSelectorEntranceVisibility.set(View.VISIBLE);
                screenStatusBarScoreVisibility.set(View.GONE);
                actionButtonPlayVisibility.set(View.GONE);
                actionButtonSkipVisibility.set(View.GONE);
                actionButtonJoinVisibility.set(View.VISIBLE);
                btnJoinText.set(mBinding.getRoot().getContext().getString(R.string.ktv_join));
                actionButtonAccompanimentVisibility.set(View.GONE);
                mBinding.containerLyricView.setLinesPattern(LyricView.LinesPattern.Multi_LINES);
                break;
            case Audience:
                centerSongSelectorEntranceVisibility.set(View.GONE);
                centerTipsVisibility.set(View.GONE);
                screenStatusBarVisibility.set(View.VISIBLE);
                screenPitchVisibility.set(View.GONE);
                switchScreenPlayMode(ScreenPlayMode.PrePlay);
                screenActionBarVisibility.set(View.VISIBLE);
                rightSongSelectorEntranceVisibility.set(View.GONE);
                screenStatusBarScoreVisibility.set(View.GONE);
                actionButtonPlayVisibility.set(View.GONE);
                actionButtonSkipVisibility.set(View.GONE);
                actionButtonJoinVisibility.set(View.GONE);
                actionButtonAccompanimentVisibility.set(View.GONE);
                mBinding.containerLyricView.setLinesPattern(LyricView.LinesPattern.Multi_LINES);
                break;
            case InitMic:
                centerSongSelectorEntranceVisibility.set(View.VISIBLE);
                centerTipsVisibility.set(View.GONE);
                screenStatusBarVisibility.set(View.GONE);
                screenPitchVisibility.set(View.GONE);
                switchScreenPlayMode(ScreenPlayMode.Init);
                screenActionBarVisibility.set(View.GONE);
                break;
            case Init:
            default:
                centerSongSelectorEntranceVisibility.set(View.GONE);
                centerTipsVisibility.set(View.VISIBLE);
                screenStatusBarVisibility.set(View.GONE);
                screenPitchVisibility.set(View.GONE);
                switchScreenPlayMode(ScreenPlayMode.Init);
                screenActionBarVisibility.set(View.GONE);
                break;
        }
    }

    public void switchScreenPlayMode(ScreenPlayMode screenPlayMode) {
        switch (screenPlayMode) {
            case PrePlay:
                screenLyricVisibility.set(View.GONE);
                screenSongNoticeVisibility.set(View.VISIBLE);
                break;
            case Play:
                screenLyricVisibility.set(View.VISIBLE);
                screenSongNoticeVisibility.set(View.GONE);
                break;
            case Init:
            default:
                screenLyricVisibility.set(View.GONE);
                screenSongNoticeVisibility.set(View.GONE);
                break;
        }
    }

    private long mPrePlayCountDownEndTimestamp = 0;
    private boolean mIsPrePlayCountDowning = false;
    private void startPreCountDownCountdown(KTVPlayingMusicInfo ktvPlayingMusicInfo, long countDownMillis) {
        mPrePlayCountDownEndTimestamp = SystemClock.uptimeMillis() + countDownMillis;
        mIsPrePlayCountDowning = true;
        mBinding.getRoot().post(new Runnable() {
            @Override
            public void run() {
                prePlayCountDown();
            }
        });
    }

    private void stopPrePlayCountdown() {
        mIsPrePlayCountDowning = false;
        mPrePlayCountDownEndTimestamp = 0;
    }

    private void prePlayCountDown() {
        if (mIsPrePlayCountDowning && mPrePlayCountDownEndTimestamp > 0 && null != mCurrentMusicInfo) {
            long countDownloadMillis = mPrePlayCountDownEndTimestamp - SystemClock.uptimeMillis();
            countDownloadMillis = Math.max(0, countDownloadMillis);
            setStatusBarSongNameDesc();
            noticeSongNameDesc.set(String.format("%s 即将演唱 %s", mCurrentMusicInfo.seatInfo.userName, mCurrentMusicInfo.songName));
            int countDownSeconds = (int) Math.ceil(countDownloadMillis/1000f);
            noticeProgressDesc.set(String.format("%d秒后开始", countDownSeconds));
            setStatusBarSongDurationDesc(0);
            setStatusBarSumScoreDesc(0);
            if (countDownSeconds > 0) {
                mBinding.getRoot().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        prePlayCountDown();
                    }
                }, 100);
            } else {
                stopPrePlayCountdown();
            }
        }
    }

    private void setStatusBarSongNameDesc() {
        if (null != mCurrentMusicInfo) {
            statusBarSongNameDesc.set(String.format("%s 《%s》", mCurrentMusicInfo.singerName, mCurrentMusicInfo.songName));
        }
    }

    private void setStatusBarSongDurationDesc(long progress) {
        long songDuration = null != mCurrentMusicInfo ? mCurrentMusicInfo.duration : 0;
        statusBarSongDurationDesc.set(String.format("%s/%s", DisplayTextUtil.formatDuration(progress), DisplayTextUtil.formatDuration(songDuration)));
    }

    private void setStatusBarSumScoreDesc(int score) {
        statusBarSumScoreDesc.set(String.format("%d 分", score));
    }

    private void loadPitch() {
        if (null != mCurrentMusicInfo) {
            mRoomController.fetchMusicPitch(mCurrentMusicInfo.songID, new ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicPitchCallback() {
                @Override
                public void onMusicPitchCallback(String pitch) {
                    List<MusicPitch> musicPitchList = PitchViewHelper.parseMidiFile(pitch);
                    mBinding.containerPitchView.setStandardPitch(musicPitchList);
                    long firstPitchStartTime = 0l;
                    if (null != musicPitchList && !musicPitchList.isEmpty()) {
                        firstPitchStartTime = musicPitchList.get(0).getStartTime();
                        loadLyric(firstPitchStartTime);
                    }
                }

                @Override
                public void onFail(int errorCode, String errorMsg) {
                    Log.i(TAG, "fetchMusicPitch onFail errorCode: " + errorCode + ", errorMsg: " + errorMsg);
                }
            });
        }
    }

    private void loadLyric(long firstPitchStartTime) {
        if (null != mCurrentMusicInfo) {
            mRoomController.fetchMusicLyric(mCurrentMusicInfo.songID, new ARTCKaraokeRoomMusicLibraryCallback.ARTCKaraokeRoomMusicLyricCallback() {
                @Override
                public void onMusicLyricCallback(String lyric, KTVMusicInfo.KTVLyricType lyricType) {
                    mBinding.containerLyricView.setupLyric(lyric, firstPitchStartTime, 0);
                }

                @Override
                public void onFail(int errorCode, String errorMsg) {
                    Log.i(TAG, "fetchMusicLyric onFail errorCode: " + errorCode + ", errorMsg: " + errorMsg);
                }
            });
        }
    }
}
