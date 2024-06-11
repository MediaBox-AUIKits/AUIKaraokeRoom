//
//  AUIKaraokeRoomViewController.swift
//  Example
//
//  Created by Bingo on 2024/2/5.
//

import UIKit
import AUIFoundation
import SnapKit
import AUIRoomCore
import AUIVoiceRoom

@objcMembers open class AUIKaraokeRoomViewController: AUIVoiceRoomViewController {
    
    private override init(_ roomController: ARTCVoiceRoomEngine) {
        super.init(roomController)
    }
    
    public convenience init(ktvRoomController: ARTCKaraokeRoomController) {
        self.init(ktvRoomController)
        self.ktvRoomController.addObserver(karaoDelegate: self)
        self.ktvRoomController.getSingScoreBlock = {[weak self] in
            guard let self = self else { return 0 }
            return self.singingView.singScore
        }
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
        NSObject.cancelPreviousPerformRequests(withTarget: self)
        self.ktvRoomController.stopPlayMusic()
        self.musicLibrary.destroy()
        self.ktvRoomController.removeObserver(karaoDelegate: self)
        UIViewController.av_setIdleTimerDisabled(false)
        debugPrint("deinit: \(self)")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        self.setupSelfView()
        NotificationCenter.default.addObserver(self, selector: #selector(appWillTerminate), name: UIApplication.willTerminateNotification, object: nil)
    }
    
    open override var enableBgMusic: Bool {
        return false
    }
    
    open override var warningText: String {
        return "欢迎来到K歌房，房间禁止谈论政治、低俗色情、吸烟酗酒或发布虚假信息等内容，若有违反将踢出、封停账号。"
    }
    
    open override func onJoinedMic(seatIndex: Int32, user: ARTCRoomUser) {
        super.onJoinedMic(seatIndex: seatIndex, user: user)
        self.updateSingingViewDisplay()
        self.updateMicSeatViewDisplay()
    }
    
    open override func onLeavedMic(seatIndex: Int32, user: ARTCRoomUser) {
        super.onLeavedMic(seatIndex: seatIndex, user: user)
        self.updateSingingViewDisplay()
        self.updateMicSeatViewDisplay()
        
        let isAnchor = self.ktvRoomController.isAnchor
        let meUserID = self.ktvRoomController.me.userId
        if isAnchor && !user.userId.isEqual(meUserID) {
            self.ktvRoomController.removeMusic(who: user.userId) { error in
                if let error = error {
                    DispatchQueue.main.async {
                        AVAlertController.show("\(error.artcMessage)：\(error.code)")
                    }
                }
            }
        }
        if self.ktvRoomController.isJoinSinger {
            if let curMusicPlaying = self.ktvRoomController.curMusicPlaying {
                let leadingUserId = curMusicPlaying.userExtendInfo.userID
                if leadingUserId.isEqual(user.userId) || user.userId.isEqual(meUserID) {
                    self.ktvRoomController.leaveSinging { error in
//                        if let error = error {
//                            DispatchQueue.main.async {
//                                AVAlertController.show("\(error.artcMessage)：\(error.code)")
//                            }
//                        }
                    }
                }
            }
        }
    }
    
    open override func onJoinedRoom(user: ARTCRoomUser) {
        if self.ktvRoomController.isAnchor 
            && user.userId.isEqual(self.ktvRoomController.me.userId){
            self.insertTips("\(self.ktvRoomController.me.getFinalNick()) 创建房间")
            self.insertTips("\(self.ktvRoomController.me.getFinalNick()) 上 \(self.micSeatView.getDefaultMicName(seatIndex: 0))")
        } else {
            if user.userId.isEqual(self.ktvRoomController.me.userId) {
                self.ktvRoomController.requestRemoteKtvState()
            }
            self.insertTips("\(user.getFinalNick()) 进入房间")
        }
    }
    
    public func onWillLeaveRoom(user: ARTCRoomUser) {
        self.ktvRoomController.stopPlayMusic()
        self.musicLibrary.destroy()
        self.singingView.resetToNormalState()
    }
    
    private var ktvRoomController: ARTCKaraokeRoomController {
        get {
            return self.roomController as! ARTCKaraokeRoomController
        }
    }

    private lazy var musicLibrary: ARTCKaraokeRoomMusicLibrary = {
        let library = ARTCKaraokeRoomMusicLibrary.shared
        return library
    }()
    
    private lazy var singingView: AUIKaraokeRoomSingingView = {
        let view = AUIKaraokeRoomSingingView()
        return view
    }()
    
    private weak var musicListPanel: AUIKaraokeRoomMusicListPanel? = nil
}

extension AUIKaraokeRoomViewController {
        
    private func setupSelfView() {
        self.setupSingingView()
        
        self.micSeatView.isAnchorSeatViewMiddle = false
        self.micSeatView.snp.remakeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalTo(self.singingView.snp.bottom).offset(14)
            make.height.equalTo(160 + 36)
        }
    
        self.statusView.snp.remakeConstraints { make in
            make.left.equalTo(self.copyBtn.snp.right).offset(6)
            make.centerY.equalTo(self.copyBtn)
            make.height.equalTo(16)
            make.width.greaterThanOrEqualTo(40)
        }
    }
    
    private func setupSingingView() {
        self.view.addSubview(self.singingView)
        self.singingView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalTo(self.exitBtn.snp.bottom).offset(8)
            make.height.equalTo(190)
        }
        self.updateSingingViewDisplay()
        
        self.singingView.musicListEntryClickBlock = { [weak self] sender in
            guard let self = self else { return }
            self.showMusicListPanel()
        }
        self.singingView.playPauseBlock = {[weak self] pause in
            guard let self = self else { return }
            if self.ktvRoomController.ktvRoomInfo.roomState != .Playing {
                return
            }
            if pause {
                self.ktvRoomController.pauseMusic(completed: nil)
            } else {
                self.ktvRoomController.resumeMusic(completed: nil)
            }
        }
        self.singingView.skipMusicBlock = {[weak self] in
            guard let self = self else { return }
            self.ktvRoomController.skipMusic(completed: { error in
                if let error = error {
                    DispatchQueue.main.async {
                        AVAlertController.show("\(error.artcMessage)：\(error.code)")
                    }
                }
            })
        }
        self.singingView.trackModeChangeBlock = {[weak self] isBackTracking in
            guard let self = self else { return }
            if self.ktvRoomController.ktvRoomInfo.roomState != .Playing {
                return
            }
            self.ktvRoomController.setMusicAccompanimentMode(isAccompany: isBackTracking, completed: { error in
                if let error = error {
                    DispatchQueue.main.async {
                        AVAlertController.show("\(error.artcMessage)：\(error.code)")
                    }
                }
            })
        }
        self.singingView.singTogetherBlock = {[weak self] in
            guard let self = self else { return }
            if self.ktvRoomController.ktvRoomInfo.roomState != .Playing {
                return
            }
            self.ktvRoomController.joinSinging(completed: { error in
                if let error = error {
                    DispatchQueue.main.async {
                        AVAlertController.show("\(error.artcMessage)：\(error.code)")
                    }
                }
            })
        }
        self.singingView.quitSingTogetherBlock = {[weak self] in
            guard let self = self else { return }
            if self.ktvRoomController.ktvRoomInfo.roomState != .Playing {
                return
            }
            self.ktvRoomController.leaveSinging(completed: { error in
                if let error = error {
                    DispatchQueue.main.async {
                        AVAlertController.show("\(error.artcMessage)：\(error.code)")
                    }
                }
            })
        }
    }
    
    private func showMusicListPanel() {
        let panel = AUIKaraokeRoomMusicListPanel(frame: CGRect(x: 0, y: 0, width: self.view.av_width, height: 0))
        self.musicListPanel = panel
        panel.show(on: self.view, with: .clickToClose)
        panel.musicListView.addMusicBlock = {[weak self] musicInfo in
            musicInfo.addedState = .downloading
            guard let self = self else { return }
            self.addMusic(musicInfo, completed: { succeed in
                DispatchQueue.main.async {
                    musicInfo.addedState = succeed ? .added : .normal
                }
            })
        }
        panel.musicListView.requestDataBlock = { [weak self] chartId, page, pageSize, completed in
            guard let self = self else { return }
            self.musicLibrary.fetchMusicList(chartId: chartId, page: page, pageSize: pageSize) { musicList, error in
                if let error = error {
                    DispatchQueue.main.async {
                        AVAlertController.show("\(error.artcMessage)：\(error.code)")
                    }
                }
                let musicInfos = AUIKaraokeRoomMusicInfo.musicInfosWithServerMusicInfos(musicList)
                self.filterMusicListForAddState(musicInfos)
                DispatchQueue.main.async {
                    completed(musicInfos, error)
                }
            }
        }
        panel.searchEntryView.clickBlock = { [weak self] sender in
            guard let self = self else { return }
            self.showSearchPanel()
        }
        panel.addedMusicListView.skipMusicBlock = {[weak self] musicInfo in
            guard let self = self else { return }
            self.ktvRoomController.skipMusic(completed: { error in
                if let error = error {
                    DispatchQueue.main.async {
                        AVAlertController.show("\(error.artcMessage)：\(error.code)")
                    }
                }
            })
        }
        panel.addedMusicListView.pinMusicBlock = {[weak self] musicInfo in
            guard let self = self else { return }
            self.ktvRoomController.pinMusic(musicInfo: musicInfo, completed: { error in
                if let error = error {
                    DispatchQueue.main.async {
                        AVAlertController.show("\(error.artcMessage)：\(error.code)")
                    }
                }
            })
        }
        panel.addedMusicListView.deleteMusicBlock = {[weak self] musicInfo in
            guard let self = self else { return }
            self.ktvRoomController.removeMusic(musicInfo: musicInfo, completed: { error in
                if let error = error {
                    DispatchQueue.main.async {
                        AVAlertController.show("\(error.artcMessage)：\(error.code)")
                    }
                }
            })
        }
        self.updateMusicListPanelAddedData()

        self.musicLibrary.fetchMusicChartList {[weak self] chartList, error in
            guard let self = self else { return }
            if let error = error {
                DispatchQueue.main.async {
                    AVAlertController.show("\(error.artcMessage)：\(error.code)")
                }
            }
            let chartInfos = AUIKaraokeRoomChartInfo.chartInfosWithServerChartInfos(chartList)
            DispatchQueue.main.async {
                self.musicListPanel?.chartMenuView.itemList = chartInfos
            }
        }
    }
    
    private func showSearchPanel() {
        let panel = AUIKaraokeRoomSearchMusicPanel(frame: CGRect(x: 0, y: 0, width: self.view.av_width, height: 0))
        panel.show(on: self.view, with: .clickToClose)
        panel.activeSearchTextInput()
        
        panel.musicListView.searchMusicBlock = {[weak self] searchKeyword, page, pageSize, completed in
            guard let self = self else { return }
            self.musicLibrary.searchMusic(keyword: searchKeyword, page: page, pageSize: pageSize) { musicList, error in
                if let error = error {
                    DispatchQueue.main.async {
                        AVAlertController.show("\(error.artcMessage)：\(error.code)")
                    }
                }
                let musicInfos = AUIKaraokeRoomMusicInfo.musicInfosWithServerMusicInfos(musicList)
                self.filterMusicListForAddState(musicInfos)
                DispatchQueue.main.async {
                    completed(musicInfos, error)
                }
            }
        }
        panel.musicListView.addMusicBlock = {[weak self] musicInfo in
            musicInfo.addedState = .downloading
            guard let self = self else { return }
            self.addMusic(musicInfo, completed: { succeed in
                DispatchQueue.main.async {
                    musicInfo.addedState = succeed ? .added : .normal
                }
            })
        }
    }
    
    private func updateMusicListPanelAddedData() {
        guard let panel = self.musicListPanel else { return }
        let musicPlayingList = self.ktvRoomController.musicPlayingList
        let playingMusicInfos = AUIKaraokeRoomMusicInfo.musicInfosWithServerMusicInfos(musicPlayingList)
        panel.addedMusicListView.updateMusicInfos(musicInfos: playingMusicInfos)
        panel.musicListPanelHeaderView.addedMusicCount = playingMusicInfos.count
    }
    
    private func filterMusicListForAddState(_ musicInfos: [AUIKaraokeRoomMusicInfo]?) {
        guard let musicInfos = musicInfos, !musicInfos.isEmpty else { return }
        let musicPlayingList = self.ktvRoomController.musicPlayingList
        musicInfos.forEach { musicInfo in
            let addedMusicInfo = musicPlayingList.first { info in
                return info.singUserIsMe && info.songID.isEqual(musicInfo.songID)
            }
            if addedMusicInfo != nil {
                musicInfo.addedState = .added
            } else {
                musicInfo.addedState = self.musicLibrary.isMusicDownloading(songID: musicInfo.songID) ? .downloading : .normal
            }
        }
    }
    
    private func updateSingingViewDisplay() {
        self.singingView.updateViewDisplay(isJoinMic: self.ktvRoomController.isJoinMic,
                                           isAnchor: self.ktvRoomController.isAnchor,
                                           isSinging: self.ktvRoomController.curMusicPlaying != nil,
                                           isLeadingSinger: self.ktvRoomController.isLeadSinger,
                                           isJoinSinger: self.ktvRoomController.isJoinSinger,
                                           isWaiting: self.ktvRoomController.ktvRoomInfo.roomState == .Waiting)
    }
    
    private func updateMicSeatViewDisplay() {
        self.micSeatView.resetSingerRole()
        if let musicInfo = self.ktvRoomController.curMusicPlaying {
            self.micSeatView.updateSingerRole(uid: musicInfo.userExtendInfo.userID, singerRole: .LeadSinger)
            for userID in musicInfo.joinSingUserIds {
                self.micSeatView.updateSingerRole(uid: userID, singerRole: .JoinSinger)
            }
        }
    }
    
    private func addMusic(_ musicInfo: AUIKaraokeRoomMusicInfo, completed: @escaping ((_ succeed: Bool)->Void)) {
        if !self.ktvRoomController.checkCanAddMusic {
            AVToastView.show(AUIKaraokeRoomBundle.getString("必须上麦才能点歌哦")!, view: self.view, position: .mid)
            completed(false)
            return
        }
        
        AVToastView.show(AUIKaraokeRoomBundle.getString("歌曲正在下载中，请稍后")!, view: self.view, position: .mid)

        self.musicLibrary.fetchMusicAllResource(musicInfo: musicInfo) {[weak self] songID, error in
            guard let self = self else { return }
            if error != nil {
                DispatchQueue.main.async {
                    AVAlertController.show("\(error!.artcMessage)：\(error!.code)")
                }
                completed(false)
                return
            }
            self.ktvRoomController.addMusic(musicInfo: musicInfo) { error in
                if error != nil {
                    DispatchQueue.main.async {
                        AVAlertController.show("\(error!.artcMessage)：\(error!.code)")
                    }
                    completed(false)
                    return
                }
                completed(true)
            }
        }
    }
    
    private func showSingingCompleteView(_ singScore: Int) {
        for subView in self.view.subviews {
            if ((subView as? AUIKaraokeRoomSingingCompleteView) != nil) {
                return
            }
        }
        
        let view = AUIKaraokeRoomSingingCompleteView(frame: self.view.bounds)
        view.score = singScore
        if self.ktvRoomController.isJoinSinger {
            view.score = self.singingView.singScore
        }
        view.clickFinishBlock = {[weak self] in
            guard let self = self else { return }
            self.removeSingingCompleteView()
        }
        self.view.addSubview(view)
        self.perform(#selector(removeSingingCompleteView), with: nil, afterDelay: 3)
    }
    
    @objc private func removeSingingCompleteView() {
        NSObject.cancelPreviousPerformRequests(withTarget: self)
        for subView in self.view.subviews {
            if let singingCompleteView = subView as? AUIKaraokeRoomSingingCompleteView {
                singingCompleteView.removeFromSuperview()
                if self.ktvRoomController.isAnchor {
                    self.ktvRoomController.skipMusic { error in
                        if let error = error {
                            DispatchQueue.main.async {
                                AVAlertController.show("\(error.artcMessage)：\(error.code)")
                            }
                        }
                    }
                }
                break
            }
        }
    }
    
    @objc private func appWillTerminate() {
        self.ktvRoomController.stopPlayMusic()
        self.musicLibrary.destroy()
    }
}

extension AUIKaraokeRoomViewController: ARTCKaraokeRoomControllerDelegate {
    
    // 播放列表（当前播放中+未播放）更新：添加、删除、置顶、切歌、播放结束
    public func onMusicPlayingListUpdated(reason: ARTCKaraokeRoomUpdatePlayingMusicListReason, userID: String?, songID: String?) {
        self.updateMusicListPanelAddedData()
        if let panel = self.musicListPanel {
            let normalMusicList = panel.musicListView.itemList
            self.filterMusicListForAddState(normalMusicList)
            panel.musicListView.updateMusicInfos(musicInfos: normalMusicList)
        }
        self.updateSingingViewDisplay()
        if reason == .JoinSinging || reason == .LeaveSinging || reason == .Other {
            self.updateMicSeatViewDisplay()
        }
        
        let musicPlayingList = self.ktvRoomController.musicPlayingList
        if reason == .AddMusic
            && userID != nil && !userID!.isEmpty
            && songID != nil && !songID!.isEmpty {
            let musicInfo = musicPlayingList.first { info in
                return info.songID.isEqual(songID) && info.userExtendInfo.userID.isEqual(userID)
            }
            if let musicInfo = musicInfo {
                self.insertTips("\(musicInfo.userExtendInfo.micseatIndex + 1) 号麦 \(musicInfo.userExtendInfo.userNick) 点歌 <<\(musicInfo.songName)>>")
            }
        }
        if reason == .JoinSinging
            && userID != nil && !userID!.isEmpty {
            let musicInfo = self.ktvRoomController.curMusicPlaying
            if let musicInfo = musicInfo {
                if musicInfo.joinSingUserIds.count > 0 {
                    let newJoinerUserInfo = self.ktvRoomController.ktvRoomInfo.seatInfoList.first { seatInfo in
                        return seatInfo.user?.userId.isEqual(userID) ?? false
                    }
                    if newJoinerUserInfo != nil  && newJoinerUserInfo!.user != nil {
                        self.insertTips("\(newJoinerUserInfo!.user!.getFinalNick()) 进入合唱")
                    }
                }
            }
        }
        
        if userID != nil && !userID!.isEmpty
            && ((reason == .JoinSinging && userID!.isEqual(self.ktvRoomController.me.userId))
                 || (reason == .Other
                     && self.ktvRoomController.isJoinSinger
                     && self.ktvRoomController.mediaPrepareState() == .Unprepared
                     && userID!.isEqual(self.ktvRoomController.anchor.userId)
                    )) {
            var musicInfo = self.ktvRoomController.curMusicPlaying
            if songID != nil && !songID!.isEmpty {
                musicInfo = musicPlayingList.first { info in
                    return info.songID.isEqual(songID)
                }
            }
            if let serverMusicInfo = musicInfo {
                AVToastView.show(AUIKaraokeRoomBundle.getString("歌曲正在下载中，请稍后")!, view: self.view, position: .mid)
                self.musicLibrary.fetchMusicAllResource(musicInfo: serverMusicInfo) {[weak self] songID, error in
                    guard let self = self else { return }
                    if error != nil {
                        DispatchQueue.main.async {
                            AVAlertController.show("\(error!.artcMessage)：\(error!.code)")
                        }
                        return
                    }
                    DispatchQueue.main.async {
                        let playingMusicInfo = AUIKaraokeRoomMusicInfo.musicInfoWithServerMusicInfo(serverMusicInfo)!
                        self.singingView.curPlayingMusic = playingMusicInfo
                        self.singingView.resumeTrackMode()
                        self.ktvRoomController.switchMicrophone(off: false)
                        self.ktvRoomController.playMusic(false)
                    }
                }
            }
        } else if reason == .Other {
            if let serverMusicInfo = self.ktvRoomController.curMusicPlaying {
                if self.ktvRoomController.ktvRoomInfo.roomState != .Normal
                    && self.singingView.curPlayingMusic == nil {
                    
                    let playingMusicInfo = AUIKaraokeRoomMusicInfo.musicInfoWithServerMusicInfo(serverMusicInfo)!
                    self.singingView.curPlayingMusic = playingMusicInfo
                    
                    if playingMusicInfo.lyric.isEmpty {
                        let playingMusicSongID = serverMusicInfo.songID
                        self.musicLibrary.fetchMusicLyric(songID: playingMusicSongID) {[weak self] songID, lyric, error in
                            guard let self = self else { return }
                            if songID != nil && !songID!.isEmpty && playingMusicSongID.isEqual(songID) {
                                serverMusicInfo.lyric = lyric ?? ""
                                playingMusicInfo.lyric = lyric ?? ""
                                DispatchQueue.main.async {
                                    self.singingView.curPlayingMusic = playingMusicInfo
                                }
                            }
                        }
                    }
                    
                    if playingMusicInfo.pitchJson.isEmpty {
                        let playingMusicSongID = serverMusicInfo.songID
                        self.musicLibrary.fetchMusicPitch(songID: playingMusicSongID) {[weak self] songID, pitch, error in
                            guard let self = self else { return }
                            if songID != nil && !songID!.isEmpty && playingMusicSongID.isEqual(songID) {
                                serverMusicInfo.pitchJson = pitch ?? ""
                                playingMusicInfo.pitchJson = pitch ?? ""
                                DispatchQueue.main.async {
                                    self.singingView.curPlayingMusic = playingMusicInfo
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public func onMusicPrepareStateUpdate(state: ARTCKaraokeRoomMusicPrepareState) {
        if state == .Failed {
            DispatchQueue.main.async {
                AVToastView.show("歌曲加载异常", view: self.view, position: .mid)
            }
            if self.ktvRoomController.isLeadSinger {
                self.ktvRoomController.skipMusic { error in
                    if let error = error {
                        DispatchQueue.main.async {
                            AVAlertController.show("\(error.artcMessage)：\(error.code)")
                        }
                    }
                }
            } else if self.ktvRoomController.isJoinSinger {
                self.ktvRoomController.leaveSinging(completed: { error in
                    if let error = error {
                        DispatchQueue.main.async {
                            AVAlertController.show("\(error.artcMessage)：\(error.code)")
                        }
                    }
                })
            }
        }
    }
    
    // 播放进度更新
    public func onMusicPlayProgressChanged(musicInfo: ARTCKaraokeRoomMusicInfo, millisecond: Int64) {
        let playingMusicInfo = AUIKaraokeRoomMusicInfo.musicInfoWithServerMusicInfo(musicInfo)
        self.singingView.updatePlayProgress(progress: max(0, millisecond), duration: playingMusicInfo!.duration)
    }

    // 播放状态更新
    public func onMusicPlayStateChanged(state: ARTCKaraokeRoomMusicPlayState, singScore: Int) {
        self.singingView.musicPlaying = state == .Playing
        if state == .Completed {
            self.showSingingCompleteView(singScore)
        }
    }
    
    // 角色变化
    public func onMusicSingerRoleChanged(newRole: ARTCKaraokeRoomSingerRole, oldRole: ARTCKaraokeRoomSingerRole, userID: String) {
        self.micSeatView.updateSingerRole(uid: userID, singerRole: newRole)
    }
    
    public func onRoomStateChanged(oldRoomState: ARTCKaraokeRoomState, newRoomState: ARTCKaraokeRoomState, songID: String?) {
        guard oldRoomState != newRoomState else { return }
        self.singingView.resetToNormalState()
        self.updateSingingViewDisplay()
        self.updateMicSeatViewDisplay()
        if newRoomState != .Playing {
            self.ktvRoomController.stopPlayMusic()
        }
        for subView in self.view.subviews {
            if ((subView as? AUIKaraokeRoomSingingCompleteView) != nil) {
                subView.removeFromSuperview()
                break
            }
        }

        if newRoomState != .Normal && songID != nil && !songID!.isEmpty {
            let musicPlayingList = self.ktvRoomController.musicPlayingList
            let serverMusicInfo = musicPlayingList.first { info in
                return info.songID.isEqual(songID)
            }
            guard let serverMusicInfo = serverMusicInfo else { return }
            
            let playingMusicInfo = AUIKaraokeRoomMusicInfo.musicInfoWithServerMusicInfo(serverMusicInfo)!
            self.singingView.curPlayingMusic = playingMusicInfo
            
            if playingMusicInfo.lyric.isEmpty {
                let playingMusicSongID = songID
                self.musicLibrary.fetchMusicLyric(songID: playingMusicSongID!) {[weak self] songID, lyric, error in
                    guard let self = self else { return }
                    if songID != nil && !songID!.isEmpty && playingMusicSongID!.isEqual(songID) {
                        serverMusicInfo.lyric = lyric ?? ""
                        playingMusicInfo.lyric = lyric ?? ""
                        DispatchQueue.main.async {
                            self.singingView.curPlayingMusic = playingMusicInfo
                        }
                    }
                }
            }
            
            if playingMusicInfo.pitchJson.isEmpty {
                let playingMusicSongID = songID
                self.musicLibrary.fetchMusicPitch(songID: playingMusicSongID!) {[weak self] songID, pitch, error in
                    guard let self = self else { return }
                    if songID != nil && !songID!.isEmpty && playingMusicSongID!.isEqual(songID) {
                        serverMusicInfo.pitchJson = pitch ?? ""
                        playingMusicInfo.pitchJson = pitch ?? ""
                        DispatchQueue.main.async {
                            self.singingView.curPlayingMusic = playingMusicInfo
                        }
                    }
                }
            }
            
            if newRoomState == .Waiting {
                if playingMusicInfo.singUserIsMe {
                    AVToastView.show("到你的歌啦", view: self.view, position: .mid)
                    self.ktvRoomController.switchMicrophone(off: false)
                } else {
                    self.ktvRoomController.switchMicrophone(off: true)
                }
                
                self.singingView.startTimerReadyToPlay(singerName: playingMusicInfo.userExtendInfo.userNick,
                                                       musicName: playingMusicInfo.songName,
                                                       completed:{[weak self] in
                    guard let self = self else { return }
                    if self.ktvRoomController.isAnchor {
                        self.ktvRoomController.sendUpdateRoomStateCommand(newRoomState:.Playing, songID: songID, userID: nil, completed: nil)
                    }
                })
            }
        }
    }
    
    public func onAudioVolumeChanged(data: [String : Any]) {
        if let info = data["0"] as? [String: Any?] {
            if let pitch = info["pitch"] as? Int32 {
                self.singingView.singerPitch = pitch
                return
            }
        }
        self.singingView.singerPitch = 0
    }
}
