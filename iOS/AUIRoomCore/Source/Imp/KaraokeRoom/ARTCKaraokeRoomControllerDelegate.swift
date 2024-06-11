//
//  ARTCKaraokeRoomControllerDelegate.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/11.
//

import UIKit

@objc public enum ARTCKaraokeRoomMusicPlayCompletedReason: Int {
    case PlayEnd = 0 // 播放完成
    case PlayFailed  // 播放出错
    case PlaySkip    // 切歌
}

@objc public protocol ARTCKaraokeRoomControllerDelegate: ARTCVoiceRoomEngineDelegate {
    
    // 播放列表（当前播放中+未播放）更新：添加、删除、置顶、切歌、播放结束
    @objc optional func onMusicPlayingListUpdated(reason: ARTCKaraokeRoomUpdatePlayingMusicListReason, userID: String?, songID: String?)
    
    // 当前歌曲结束播放，原因：0：完成播放，1：播放
    @objc optional func onMusicPlayCompleted(musicInfo: ARTCKaraokeRoomMusicInfo, reason: ARTCKaraokeRoomMusicPlayCompletedReason)
    
    // 即将播放下一首歌曲
    @objc optional func onMusicWillPlayNext(musicInfo: ARTCKaraokeRoomMusicInfo)
    
    // 音乐资源准备状态回调
    @objc optional func onMusicPrepareStateUpdate(state: ARTCKaraokeRoomMusicPrepareState)
    
    // 播放进度更新
    @objc optional func onMusicPlayProgressChanged(musicInfo: ARTCKaraokeRoomMusicInfo, millisecond: Int64)

    // 播放状态更新，只针对伴唱成员
    @objc optional func onMusicPlayStateChanged(state: ARTCKaraokeRoomMusicPlayState, singScore: Int)
    
    // 角色变化
    @objc optional func onMusicSingerRoleChanged(newRole: ARTCKaraokeRoomSingerRole, oldRole: ARTCKaraokeRoomSingerRole, userID: String)
    
    // 房间状态变化
    @objc optional func onRoomStateChanged(oldRoomState: ARTCKaraokeRoomState, newRoomState: ARTCKaraokeRoomState, songID: String?)
    
    // 音量变化
    @objc optional func onAudioVolumeChanged(data: [String: Any])
    
    // 有人即将离开语聊房
    @objc optional func onWillLeaveRoom(user: ARTCRoomUser)
}

extension ARTCKaraokeRoomController {
    
    func notifyOnMusicPlayingListUpdated(reason: ARTCKaraokeRoomUpdatePlayingMusicListReason, userID: String?, songID: String?) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                let delegate = delegate as? ARTCKaraokeRoomControllerDelegate
                delegate?.onMusicPlayingListUpdated?(reason: reason, userID: userID, songID: songID)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnMusicPlayingListUpdated(reason: reason, userID: userID, songID: songID)
            }
        }
    }
    
    func notifyOnMusicPlayCompleted(musicInfo: ARTCKaraokeRoomMusicInfo, reason: ARTCKaraokeRoomMusicPlayCompletedReason) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                let delegate = delegate as? ARTCKaraokeRoomControllerDelegate
                delegate?.onMusicPlayCompleted?(musicInfo: musicInfo, reason: reason)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnMusicPlayCompleted(musicInfo: musicInfo, reason: reason)
            }
        }
    }
    
    func notifyOnMusicWillPlayNext(musicInfo: ARTCKaraokeRoomMusicInfo) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                let delegate = delegate as? ARTCKaraokeRoomControllerDelegate
                delegate?.onMusicWillPlayNext?(musicInfo: musicInfo)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnMusicWillPlayNext(musicInfo: musicInfo)
            }
        }
    }
    
    func notifyOnMusicPrepareStateUpdate(state: ARTCKaraokeRoomMusicPrepareState) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                let delegate = delegate as? ARTCKaraokeRoomControllerDelegate
                delegate?.onMusicPrepareStateUpdate?(state: state)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnMusicPrepareStateUpdate(state: state)
            }
        }
    }
    
    func notifyOnMusicPlayProgressChanged(musicInfo: ARTCKaraokeRoomMusicInfo, millisecond: Int64) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                let delegate = delegate as? ARTCKaraokeRoomControllerDelegate
                delegate?.onMusicPlayProgressChanged?(musicInfo: musicInfo, millisecond: millisecond)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnMusicPlayProgressChanged(musicInfo: musicInfo, millisecond: millisecond)
            }
        }
    }
    
    func notifyOnMusicPlayStateChanged(state: ARTCKaraokeRoomMusicPlayState, singScore: Int) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                let delegate = delegate as? ARTCKaraokeRoomControllerDelegate
                delegate?.onMusicPlayStateChanged?(state: state, singScore: singScore)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnMusicPlayStateChanged(state: state, singScore: singScore)
            }
        }
    }
    
    func notifyOnMusicSingerRoleChanged(newRole: ARTCKaraokeRoomSingerRole, oldRole: ARTCKaraokeRoomSingerRole, userID: String) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                let delegate = delegate as? ARTCKaraokeRoomControllerDelegate
                delegate?.onMusicSingerRoleChanged?(newRole: newRole, oldRole: oldRole, userID: userID)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnMusicSingerRoleChanged(newRole: newRole, oldRole: oldRole, userID: userID)
            }
        }
    }
    
    func notifyOnRoomStateChanged(oldRoomState: ARTCKaraokeRoomState, newRoomState: ARTCKaraokeRoomState, songID: String?) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                let delegate = delegate as? ARTCKaraokeRoomControllerDelegate
                delegate?.onRoomStateChanged?(oldRoomState: oldRoomState, newRoomState: newRoomState, songID: songID)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnRoomStateChanged(oldRoomState: oldRoomState, newRoomState: newRoomState, songID: songID)
            }
        }
    }
    
    func notifyOnAudioVolumeChanged(data: [String: Any]) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                let delegate = delegate as? ARTCKaraokeRoomControllerDelegate
                delegate?.onAudioVolumeChanged?(data: data)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnAudioVolumeChanged(data: data)
            }
        }
    }
    
    func notifyOnWillLeaveRoom(user: ARTCRoomUser) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                let delegate = delegate as? ARTCKaraokeRoomControllerDelegate
                delegate?.onWillLeaveRoom?(user: user)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnWillLeaveRoom(user: user)
            }
        }
    }
}
