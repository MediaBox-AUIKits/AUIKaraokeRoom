//
//  ARTCKaraokeRoomController.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/6.
//

import UIKit
import AUIMessage

@objcMembers open class ARTCKaraokeRoomController: ARTCVoiceRoomEngine {
    
    private override init(_ roomInfo: ARTCVoiceRoomInfo) {
        super.init(roomInfo)
        print("\(self.ktvEngine)")
    }
    
    public convenience init(ktvRoomInfo: ARTCKaraokeRoomInfo) {
        self.init(ktvRoomInfo)
    }
    
    public func addObserver(karaoDelegate: ARTCKaraokeRoomControllerDelegate) {
        self.addObserver(delegate: karaoDelegate)
    }
    
    public func removeObserver(karaoDelegate: ARTCVoiceRoomEngineDelegate) {
        self.removeObserver(delegate: karaoDelegate)
    }
    
    public var ktvRoomInfo: ARTCKaraokeRoomInfo {
        get {
            return self.roomInfo as! ARTCKaraokeRoomInfo
        }
    }
    
    public var getSingScoreBlock: (()->Int)? = nil
    
    public func sendUpdatePlayingListCommand(reason: ARTCKaraokeRoomUpdatePlayingMusicListReason, songID: String?, userID: String?, completed: ARTCRoomCompleted?) {
        let data: [String : Any] = [
            "reason" : reason.rawValue,
            "user_id" : self.me.userId,
            "song_id" : songID ?? ""
        ]
        self.sendCommand(type: ARTCRoomMessageType.PlayingListUpdate, data: data, userId: userID) { error in
            completed?(error)
        }
    }
    
    public func sendUpdateRoomStateCommand(newRoomState: ARTCKaraokeRoomState, songID: String?, userID: String?, completed: ARTCRoomCompleted?) {
        let data: [String : Any] = [
            "old_room_state": self.ktvRoomInfo.roomState.rawValue,
            "new_room_state": newRoomState.rawValue,
            "song_id": songID ?? ""
        ]
        self.sendCommand(type: ARTCRoomMessageType.RoomStateUpdated, data: data, userId: userID) { error in
            completed?(error)
        }
    }
    
    public func sendUpdatePlayStateCommand(newPlayState: ARTCKaraokeRoomMusicPlayState, userID: String?, completed: ARTCRoomCompleted?) {
        let data: [String : Any] = [
            "old_play_state": self.ktvEngine.mediaPlayState.rawValue,
            "new_play_state": newPlayState.rawValue,
            "sing_score": self.getSingScoreBlock?() ?? 0
        ]
        self.sendCommand(type: ARTCRoomMessageType.PlayStateUpdated, data: data, userId: userID) { error in
            completed?(error)
        }
    }
    
    public func sendAllStateSyncRequestCommand(userID: String?, completed: ARTCRoomCompleted?) {
        self.sendCommand(type: ARTCRoomMessageType.AllStateSyncRequest, data: nil, userId: userID) { error in
            completed?(error)
        }
    }
    
    /*=============================点歌===========================*/
    // 播放列表（当前播放中+未播放）
    public private(set) var musicPlayingList: [ARTCKaraokeRoomMusicInfo] = []
    
    // 取远端播放列表（当前播放中+未播放）
    public func fetchMusicPlayingList(reason: ARTCKaraokeRoomUpdatePlayingMusicListReason, userID: String?, songID: String?) {
        self.playingList.fetchMusicPlayingList {[weak self] musicInfoList, curr, error in
            guard let self = self else { return }
            self.musicPlayingList = musicInfoList
            self.curMusicPlaying = curr
            var waitFetchJoinerList = false
            if let curMusicPlaying = self.curMusicPlaying {
                waitFetchJoinerList = true
                self.playingList.fetchJoinerList(musicInfo: curMusicPlaying, completed: { joinerIdList, error in
                    curMusicPlaying.joinSingUserIds = error != nil ? [] : joinerIdList
                    self.playingListFinishUpdate(reason:reason, userID: userID, songID: songID)
                })
            }
            if !waitFetchJoinerList {
                self.playingListFinishUpdate(reason:reason, userID: userID, songID: songID)
            }
        }
    }
    
    private func playingListFinishUpdate(reason: ARTCKaraokeRoomUpdatePlayingMusicListReason, userID: String?, songID: String?) {
        self.notifyOnMusicPlayingListUpdated(reason:reason, userID: userID, songID: songID)
        if self.isAnchor {
            if reason == .AddMusic {
                if self.curMusicPlaying == nil && self.musicPlayingList.count > 0 {
                    let toPlayMusicInfo = self.musicPlayingList.first
                    self.playingList.playMusic(musicInfo: toPlayMusicInfo!, completed: { nextSongID, error in
                        if error != nil {
                            return
                        }
                        self.curMusicPlaying = toPlayMusicInfo
                        self.sendUpdateRoomStateCommand(newRoomState: ARTCKaraokeRoomState.Waiting, songID: toPlayMusicInfo?.songID, userID: nil, completed: nil)
                    })
                }
            } else if reason == .CutMusic {
                let toPlayMusicInfo = self.musicPlayingList.count > 0 ? self.musicPlayingList.first! : nil
                let newRoomState = toPlayMusicInfo != nil && !toPlayMusicInfo!.songID.isEmpty ? ARTCKaraokeRoomState.Waiting : ARTCKaraokeRoomState.Normal
                let oldRoomState = self.ktvRoomInfo.roomState
                self.ktvRoomInfo.roomState = ARTCKaraokeRoomState.Normal
                self.notifyOnRoomStateChanged(oldRoomState: oldRoomState, newRoomState: ARTCKaraokeRoomState.Normal, songID: toPlayMusicInfo?.songID)
                self.sendUpdateRoomStateCommand(newRoomState: newRoomState, songID: toPlayMusicInfo?.songID, userID: nil, completed: nil)
            } else if reason == .RemoveMusic {
                if let curPlayingMusic = self.curMusicPlaying {
                    let micSeatInfo = self.roomInfo.getMicSeatInfo(uid: curPlayingMusic.userExtendInfo.userID)
                    if micSeatInfo == nil {
                        self.skipMusic { error in
                        }
                    }
                }
            }
        }
    }
    
    // 能否点歌，权限：麦上成员可以点歌
    public var checkCanAddMusic: Bool {
        get {
            if self.isJoinMic {
                return true
            }
            return false
        }
    }
    
    // 能否删歌，权限：房主、点歌成员可以删歌
    public func checkCanRemoveMusic(songID: String) -> Bool {
        if self.isAnchor {
            return true
        } else {
            let musicPlayingList = self.musicPlayingList
            let addedMusicInfo = musicPlayingList.first { info in
                return info.singUserIsMe && info.songID.isEqual(songID)
            }
            if addedMusicInfo != nil {
                return addedMusicInfo!.singUserIsMe
            }
        }
        return false
    }
    
    // 点歌，歌曲下载完成后才能加入到播放列表
    public func addMusic(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        if self.checkCanAddMusic == false {
            completed?(ARTCRoomError.createError(.NoPermission, "必须上麦才能点歌哦"))
            return
        }
        self.playingList.addMusic(musicInfo: musicInfo) {[weak self] error in
            guard let self = self else { return }
            if error != nil {
                completed?(error)
                return
            }
            self.sendUpdatePlayingListCommand(reason: .AddMusic, songID: musicInfo.songID, userID: nil, completed: completed)
        }
    }
    
    // 删除已点歌曲
    public func removeMusic(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        if !self.checkCanRemoveMusic(songID: musicInfo.songID){
            completed?(ARTCRoomError.createError(.NoPermission, "仅能删除自己点的歌曲"))
            return
        }
        self.playingList.removeMusic(musicInfo: musicInfo) {[weak self] error in
            if error != nil {
                completed?(ARTCRoomError.createError(code: error!.code, message: error!.artcMessage))
                return
            }
            self?.sendUpdatePlayingListCommand(reason: .RemoveMusic, songID: musicInfo.songID, userID: nil, completed: completed)
        }
    }
    
    // 批量删除已点歌曲
    public func removeMusic(who: String, completed: ARTCRoomCompleted?) {
        let playingMusicList = self.musicPlayingList
        self.playingList.removeMusic(playingMusicList: playingMusicList, who: who, curPlayingMusic: self.curMusicPlaying, completed: {[weak self] removedMusicList, error in
            guard let self = self else { return }
            if error != nil {
                completed?(error)
                return
            }
            self.sendUpdatePlayingListCommand(reason: .RemoveMusic, songID: "", userID: nil, completed: completed)
            completed?(error)
        })
    }
    
    // 能否置顶已点歌曲，权限：房主、点歌成员可以置顶
    public func checkCanPinMusic(songID: String) -> Bool {
        if self.isAnchor {
            return true
        } else {
            let musicPlayingList = self.musicPlayingList
            let addedMusicInfo = musicPlayingList.first { info in
                return info.singUserIsMe && info.songID.isEqual(songID)
            }
            if addedMusicInfo != nil {
                return addedMusicInfo!.singUserIsMe
            }
        }
        return false
    }
    
    // 置顶已点歌曲
    public func pinMusic(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        if self.checkCanPinMusic(songID: musicInfo.songID) == false {
            completed?(ARTCRoomError.createError(.NoPermission, "仅能置顶自己点的歌曲"))
            return
        }
        self.playingList.pinMusic(musicInfo: musicInfo) {[weak self] error in
            if error != nil {
                completed?(ARTCRoomError.createError(code: error!.code, message: error!.artcMessage))
                return
            }
            self?.sendUpdatePlayingListCommand(reason: .PinMusic, songID: musicInfo.songID, userID: nil, completed: completed)
        }
    }
    
    /*=============================播放&演唱===========================*/
    // 当前播放歌曲
    public private(set) var curMusicPlaying: ARTCKaraokeRoomMusicInfo? = nil
    
    // 自己是否是主唱
    public var isLeadSinger: Bool {
        get {
            if self.musicPlayingList.count > 0
                && self.musicPlayingList.first!.userExtendInfo.userID.isEqual(self.me.userId) {
                return true
            }
            return false
        }
    }
    
    // 能否切歌/播放下一首，权限：房主+主唱
    public var checkCanSkipMusic: Bool {
        get {
            if self.isLeadSinger || self.isAnchor {
                return true
            }
            return false
        }
    }
    
    // 切歌/播放下一首
    public func skipMusic(completed: ARTCRoomCompleted?) {
        if !self.checkCanSkipMusic {
            completed?(ARTCRoomError.createError(.NoPermission, "仅能点歌人或房主才能切歌"))
            return
        }
        if self.musicPlayingList.count == 0 {
            completed?(ARTCRoomError.createError(.NoPermission, "没有下一首歌了"))
            return
        }
        
        var music: ARTCKaraokeRoomMusicInfo? = nil
        if self.curMusicPlaying == nil {
            music = self.musicPlayingList.first
        } else if self.musicPlayingList.count > 1 {
            music = self.musicPlayingList[1]
        }
        
        self.playingList.playMusic(musicInfo: music) {[weak self] nextSongId, error in
            guard let self = self else { return }
            self.sendUpdatePlayingListCommand(reason: .CutMusic, songID: music?.songID ?? "", userID: nil, completed: completed)
        }
    }
    
    // 开始播放
    public func playMusic(_ autoPlay: Bool = true) {
        if let music = self.curMusicPlaying {
            if self.isLeadSinger {
                self.ktvEngine.switchSingerRole(newRole: .LeadSinger)
            } else if self.isJoinSinger {
                self.ktvEngine.switchSingerRole(newRole: .JoinSinger)
            }
            let musicConfig = ARTCKaraokeRoomMusicConfig()
            musicConfig.uri = music.localPath
            musicConfig.songID = music.songID
            musicConfig.isMultipleTrack = music.isTwoTrackInOneFile
            musicConfig.startPosition = 0
            musicConfig.autoPlay = autoPlay
            self.ktvEngine.loadMusicWithConfig(config: musicConfig)
        }
    }
    
    public func mediaPrepareState() -> ARTCKaraokeRoomMusicPrepareState {
        return self.ktvEngine.mediaPrepareState
    }
    
    // 能否暂停/继续播放，权限：主唱
    public var canPauseMusic: Bool {
        get {
            if self.ktvEngine.singerRole == .LeadSinger {
                return true
            }
            return false
        }
    }
    
    // 暂停播放
    public func pauseMusic(completed: ARTCRoomCompleted?) {
        if !self.canPauseMusic {
            return
        }
        self.ktvEngine.pauseMusic()
        self.sendUpdatePlayStateCommand(newPlayState: .Paused, userID: nil, completed: nil)
    }
    
    // 继续播放
    public func resumeMusic(completed: ARTCRoomCompleted?) {
        if !self.canPauseMusic {
            return
        }
        self.ktvEngine.resumeMusic()
        self.sendUpdatePlayStateCommand(newPlayState: .Playing, userID: nil, completed: nil)
    }
    
    public func stopPlayMusic() {
        self.ktvEngine.stopMusic()
        self.ktvEngine.switchSingerRole(newRole: .Audience)
    }
    
    // 查询歌曲是否可以伴奏
    public func checkMusicCanAccompany(songID: String) -> Bool {
        let res = self.musicPlayingList.first { info in
            return info.songID.isEqual(songID)
        }
        return res?.isTwoTrackInOneFile ?? false
    }
    
    // 能否切换播放模式，权限：主唱+伴唱
    public var canChangeMusicAccompanimentMode: Bool {
        get {
            if self.isLeadSinger || self.isJoinSinger {
                return true
            }
            return false
        }
    }
    
    // 切换当前的播放列表播放模式：伴奏/原声
    public func setMusicAccompanimentMode(isAccompany: Bool, completed: ARTCRoomCompleted?) {
        if !self.canChangeMusicAccompanimentMode {
            completed?(ARTCRoomError.createError(.NoPermission, "仅能主唱或者伴唱能切换模式"))
            return
        }
        if !self.checkMusicCanAccompany(songID: self.curMusicPlaying?.songID ?? "") {
            completed?(ARTCRoomError.createError(.NoPermission, "歌曲只有单音轨"))
            return
        }
        self.ktvEngine.setMusicAccompanimentMode(original: !isAccompany)
        completed?(nil)
    }
    
    // 能否加入合唱，权限：麦上且不是主唱
    public var canJoinSinging: Bool {
        get {
            if self.isJoinMic && !self.isLeadSinger && !self.isJoinSinger {
                return true
            }
            return false
        }
    }
    
    // 自己是否是合唱者
    public var isJoinSinger: Bool {
        get {
            if self.musicPlayingList.count > 0
                && self.musicPlayingList.first!.joinSingUserIds.contains(self.me.userId) {
                return true
            }
            return false
        }
    }
    
    // 加入合唱
    public func joinSinging(completed: ARTCRoomCompleted?) {
        if self.canJoinSinging && self.curMusicPlaying != nil {
            self.playingList.joinSinging(musicInfo: self.curMusicPlaying!) {[weak self] error in
                guard let self = self else { return }
                if error != nil {
                    completed?(error)
                    return
                }
                self.sendUpdatePlayingListCommand(reason: .JoinSinging, songID: self.curMusicPlaying?.songID, userID: nil, completed: completed)
            }
        }
    }
    
    // 退出合唱
    public func leaveSinging(completed: ARTCRoomCompleted?) {
        if self.curMusicPlaying != nil && self.isJoinSinger {
            self.playingList.leaveSinging(musicInfo: self.curMusicPlaying!) {[weak self] error in
                guard let self = self else { return }
                if error != nil {
                    completed?(error)
                    return
                }
                self.stopPlayMusic()
                self.sendUpdatePlayingListCommand(reason: .LeaveSinging, songID: self.curMusicPlaying?.songID, userID: nil, completed: completed)
            }
        }
    }
    
    // 转换演唱角色，以管理相应角色的推拉流逻辑。调用时机：开始演唱前，或者结束演唱后
    public func setSingerRole(newRole: ARTCKaraokeRoomSingerRole) {
        self.ktvEngine.switchSingerRole(newRole: newRole)
    }
    
    // 跳到某个位置
    public func seekMusicTo(millisecond: Int64) {
        self.ktvEngine.seekMusicTo(millisecond: millisecond)
    }
    
    // 取音乐总时长，必须在加载音乐资源成功后调用才有效，否则返回 0
    public func getMusicTotalDuration() -> Int64 {
        return self.ktvEngine.getMusicTotalDuration()
    }
    
    // 获取当前播放进度，必须在加载音乐资源成功后调用才有效，否则返回 0
    public func getMusicCurrentProgress() -> Int64 {
        return self.ktvEngine.getMusicCurrentProgress()
    }
    
    // 设置伴奏（音乐播放）音量
    public func setMusicVolume(volume: Int32) {
        self.ktvEngine.setMusicVolume(volume: volume)
    }
    
    // 新用户进入房间可以要求同步当前的房间状态、演唱状态
    public func requestRemoteKtvState() {
        self.sendAllStateSyncRequestCommand(userID:self.anchor.userId, completed: nil)
    }
    
    private lazy var ktvEngine: ARTCKaraokeRoomEngine = {
        let service = ARTCKaraokeRoomEngine()
        service.delegate = self
        self.rtcService.bridgeDelegate = self
        return service
    }()
    
    private lazy var playingList: ARTCKaraokeRoomMusicPlayingList = {
        let service = ARTCKaraokeRoomMusicPlayingList(self.me ,self.roomInfo as! ARTCKaraokeRoomInfo, (self.roomService as? ARTCRoomServiceImpl)!.roomAppServer as! ARTCKaraokeRoomAppServer)
        return service
    }()
    
    public override func joinRoom(completed: ARTCRoomCompleted?) {
        super.joinRoom { error in
            if let error = error {
                completed?(error)
                return
            }
            self.notifyOnJoinedRoom(user: self.me)
            completed?(nil)
        }
    }
    
    public override func leaveRoom(_ completed: (() -> Void)? = nil) {
        self.notifyOnWillLeaveRoom(user: self.me)
        super.leaveRoom(completed)
    }
}


extension ARTCKaraokeRoomController: ARTCRoomRTCServiceBridgeDelegate {
    
    public func onSetupRtcEngine(rtcEngine: AnyObject?) {
        self.ktvEngine.setupRtcEngine(rtcEngine: rtcEngine)
    }
    
    public func onWillReleaseEngine() {
        self.ktvEngine.releaseRtcEngine()
    }
    
    public func onDataChannelMessage(uid: String, controlMsg: AnyObject) {
        self.ktvEngine.onDataChannelMessage(uid: uid, controlMsg: controlMsg)
    }
    
}

extension ARTCKaraokeRoomController: ARTCKaraokeRoomEngineDelegate {
    
    public func onMusicPrepareStateUpdate(state: ARTCKaraokeRoomMusicPrepareState) {
        self.notifyOnMusicPrepareStateUpdate(state: state)
    }
    
    public func onMusicPlayStateUpdate(state: ARTCKaraokeRoomMusicPlayState) {
        if state == .Completed {
            self.ktvEngine.switchSingerRole(newRole: .Audience)
            if self.isLeadSinger {
                self.sendUpdatePlayStateCommand(newPlayState: .Completed, userID: nil) { error in
                    if error != nil {
                        return
                    }
                }
            }
        }
        else {
            self.notifyOnMusicPlayStateChanged(state: state, singScore: 0)
        }
    }
    
    public func onMusicPlayProgressUpdate(millisecond: Int64, uid: String, isLocalProgress: Bool) {
        if let musicInfo = self.curMusicPlaying {
            let leadingSinger = musicInfo.userExtendInfo.userID
            if isLocalProgress || uid.isEqual(leadingSinger) {
                self.notifyOnMusicPlayProgressChanged(musicInfo: musicInfo, millisecond: millisecond)
            }
        }
    }
    
    public func onSingerRoleUpdate(newRole: ARTCKaraokeRoomSingerRole, oldRole: ARTCKaraokeRoomSingerRole) {
        self.notifyOnMusicSingerRoleChanged(newRole:newRole, oldRole:oldRole, userID: self.me.userId)
    }
    
    public func anchorUidForKaraokeRoomEngine() -> String {
        if let curMusicPlaying = self.curMusicPlaying {
            return curMusicPlaying.userExtendInfo.userID
        }
        return ""
    }
}

extension ARTCKaraokeRoomController {
    
    public override func onAudioVolumeChanged(data: [String : Any]) {
        super.onAudioVolumeChanged(data: data)
        self.notifyOnAudioVolumeChanged(data: data)
    }
    
    public override func onReceivedMessage(model: ARTCRoomMessageReceiveModel) {
        super.onReceivedMessage(model: model)
        
        let sender = model.sender ?? ARTCRoomUser("")
        let data:[AnyHashable : Any] = model.data != nil ? model.data! : [:]
        debugPrint("AUIKaraokeRoomController onMessageReceived:\(data.artcJsonString)")
        
        switch model.type {
        case .PlayingListUpdate:
            let songID = data["song_id"] as? String
            let reason = data["reason"] as? Int
            let userID = data["user_id"] as? String
            self.fetchMusicPlayingList(reason: ARTCKaraokeRoomUpdatePlayingMusicListReason(rawValue: reason!) ?? ARTCKaraokeRoomUpdatePlayingMusicListReason.Unknown, userID: userID, songID: songID)
            break
        case .RoomStateUpdated:
            var old_room_state = data["old_room_state"] as? Int
            let new_room_state = data["new_room_state"] as? Int
            let songID = data["song_id"] as? String
            if old_room_state == new_room_state {
                old_room_state = self.ktvRoomInfo.roomState.rawValue
            }
            self.ktvRoomInfo.roomState = ARTCKaraokeRoomState(rawValue: new_room_state!)!
            self.notifyOnRoomStateChanged(oldRoomState: ARTCKaraokeRoomState(rawValue: old_room_state!)!, newRoomState: ARTCKaraokeRoomState(rawValue: new_room_state!)!, songID: songID)
            
            if self.ktvRoomInfo.roomState == .Waiting && self.curMusicPlaying == nil && songID != nil && !songID!.isEmpty && self.musicPlayingList.count > 0 && self.musicPlayingList.first!.songID.isEqual(songID) {
                self.curMusicPlaying = self.musicPlayingList.first
            }
            
            if old_room_state != self.ktvRoomInfo.roomState.rawValue {
                if self.isLeadSinger
                    && self.ktvRoomInfo.roomState == .Playing {
                    self.playMusic()
                    self.sendUpdatePlayStateCommand(newPlayState: .Playing, userID: nil, completed: nil)
                }
            }
            break
        case .PlayStateUpdated:
//            let old_play_state = data["old_play_state"] as? Int
            let new_play_state = data["new_play_state"] as? Int
            let sing_score = data["sing_score"] as? Int
            if self.isJoinSinger {
                if new_play_state == ARTCKaraokeRoomMusicPlayState.Paused.rawValue {
                    self.ktvEngine.pauseMusic()
                } else if new_play_state == ARTCKaraokeRoomMusicPlayState.Playing.rawValue {
                    self.ktvEngine.resumeMusic()
                }
            }
            if new_play_state == ARTCKaraokeRoomMusicPlayState.Completed.rawValue {
                self.notifyOnMusicPlayStateChanged(state: .Completed, singScore: sing_score ?? 0)
            }
            break
        case .AllStateSyncRequest:
            if self.isAnchor {
                var songID = ""
                if self.curMusicPlaying != nil {
                    songID = self.curMusicPlaying?.songID ?? ""
                } else if self.musicPlayingList.count > 0 {
                    songID = self.musicPlayingList.first?.songID ?? ""
                }
                self.sendUpdatePlayingListCommand(reason: ARTCKaraokeRoomUpdatePlayingMusicListReason.Other, songID: songID, userID: sender.userId, completed: nil)
                self.sendUpdateRoomStateCommand(newRoomState: self.ktvRoomInfo.roomState, songID: songID, userID: sender.userId, completed: nil)
                self.sendUpdatePlayStateCommand(newPlayState: self.ktvEngine.mediaPlayState, userID:  sender.userId, completed: nil)
            }
        default: break
            
        }
    }
}
