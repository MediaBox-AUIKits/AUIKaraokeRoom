//
//  ARTCKaraokeRoomMusicPlayingList.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/6.
//

import UIKit

/*=============================播放列表===========================*/
@objcMembers open class ARTCKaraokeRoomMusicPlayingList: NSObject {
    
    init(_ userInfo: ARTCRoomUser, _ roomInfo: ARTCKaraokeRoomInfo, _ roomAppServer: ARTCKaraokeRoomAppServer) {
        self.userInfo = userInfo
        self.roomInfo = roomInfo
        self.roomAppServer = roomAppServer
    }
    
    public let roomInfo: ARTCKaraokeRoomInfo
    public let userInfo: ARTCRoomUser
    public let roomAppServer: ARTCKaraokeRoomAppServer
    
    // 播放列表（当前播放中+未播放）
    public private(set) var cachedMusicPlayingList: [ARTCKaraokeRoomMusicInfo] = []
    // 当前播放歌曲
    public private(set) var currentPlayingMusicInfo: ARTCKaraokeRoomMusicInfo? = nil
    
    // 获取远端播放列表（当前播放中+未播放）
    public func fetchMusicPlayingList(completed: @escaping (_ musicInfoList: [ARTCKaraokeRoomMusicInfo], _ currPlaying: ARTCKaraokeRoomMusicInfo?, _ error: NSError?) -> Void) {
        
        self.roomAppServer.getMusicList(uid: self.userInfo.userId, roomId: self.roomInfo.roomId) { songListData, error in
            var array: [ARTCKaraokeRoomMusicInfo] = []
            var curr: ARTCKaraokeRoomMusicInfo? = nil
            songListData?.forEach({ songData in
                let musicInfo = ARTCKaraokeRoomMusicInfo.create(playingData: songData)
                if let musicInfo = musicInfo {
                    musicInfo.singUserIsMe = self.userInfo.userId.isEqual(musicInfo.userExtendInfo.userID)
                    musicInfo.singUserIsAnchor = self.userInfo.userId.isEqual(self.roomInfo.anchor.userId)
                    array.append(musicInfo)
                }
                if curr == nil && songData["status"] as? Int == 2 {
                    curr = musicInfo
                }
            })
            self.cachedMusicPlayingList = array
            self.currentPlayingMusicInfo = curr
            completed(array, curr, error)
        }
    }
    
    // 点歌，歌曲下载完成后才可以加入到播放列表
    public func addMusic(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        
        let micSeatInfo = self.roomInfo.getMicSeatInfo(uid: self.userInfo.userId)
        guard let micSeatInfo = micSeatInfo else {
            completed?(ARTCRoomError.createError(.Common, "please join room first"))
            return
        }
        
        self.roomAppServer.addMusic(uid: self.userInfo.userId, roomId: self.roomInfo.roomId, songID: musicInfo.songID, userExtends: micSeatInfo.toData().artcJsonString, songExtends: musicInfo.toPlayingExtentData().artcJsonString) { error in
            completed?(error)
        }
    }
    
    // 批量删除已点歌曲
    public func removeMusic(playingMusicList: [ARTCKaraokeRoomMusicInfo], who: String, curPlayingMusic: ARTCKaraokeRoomMusicInfo?, completed: ((_ removedMusicList: [ARTCKaraokeRoomMusicInfo], _ error: NSError?) -> Void)?) {
        
        var removedMusicList: [ARTCKaraokeRoomMusicInfo] = []
        var songIDs: [String] = []
        playingMusicList.forEach { pmi in
            if pmi.userExtendInfo.userID == who {
                if let curPlayingMusic = curPlayingMusic {
                    if !(pmi.songID.isEqual(curPlayingMusic.songID)
                        && pmi.userExtendInfo.userID.isEqual(curPlayingMusic.userExtendInfo.userID)) {
                        removedMusicList.append(pmi)
                        songIDs.append(pmi.songID)
                    }
                } else {
                    removedMusicList.append(pmi)
                    songIDs.append(pmi.songID)
                }
            }
        }
        if songIDs.isEmpty {
            completed?(removedMusicList, nil)
            return
        }
        
        self.roomAppServer.removeMusic(uid: self.userInfo.userId, roomId: self.roomInfo.roomId, songIDs: songIDs, singUserId: who) { error in
            completed?(removedMusicList, error)
        }
    }

    // 删除单首已点歌曲
    public func removeMusic(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        self.roomAppServer.removeMusic(uid: self.userInfo.userId, roomId: self.roomInfo.roomId, songIDs: [musicInfo.songID], singUserId: musicInfo.userExtendInfo.userID) { error in
            completed?(error)
        }
    }
    
    // 置顶已点歌曲
    public func pinMusic(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        
        self.roomAppServer.pinMusic(uid: self.userInfo.userId, roomId: self.roomInfo.roomId, songID: musicInfo.songID, singUserId: musicInfo.userExtendInfo.userID) { error in
            completed?(error)
        }
    }
    
    // 通知远端开始播放歌曲，切歌/播放结束则传入下一首歌的歌曲信息
    public func playMusic(musicInfo: ARTCKaraokeRoomMusicInfo?, completed: ((_ nextSongId: String?, _ error: NSError?) -> Void)?) {
        
        self.roomAppServer.playMusic(uid: self.userInfo.userId, roomId: self.roomInfo.roomId, songID: musicInfo?.songID ?? "", singUserId: musicInfo?.userExtendInfo.userID ?? "") { nextSongId, error in
            completed?(nextSongId, error)
        }
        
    }

    // 加入合唱
    public func joinSinging(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        
        self.roomAppServer.joinSinging(uid: self.userInfo.userId, roomId: self.roomInfo.roomId, songID: musicInfo.songID) { error in
            completed?(error)
        }
    }
    
    // 退出合唱
    public func leaveSinging(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        self.roomAppServer.leaveSinging(uid: self.userInfo.userId, roomId: self.roomInfo.roomId, songID: musicInfo.songID) { error in
            completed?(error)
        }
    }

    // 拉取合唱人列表
    public func fetchJoinerList(musicInfo: ARTCKaraokeRoomMusicInfo, completed: @escaping (_ joinerIdList: [String], _ error: NSError?) -> Void) {
        
        self.roomAppServer.getJoinerList(uid: self.userInfo.userId, roomId: self.roomInfo.roomId, songID: musicInfo.songID) { joinerDataList, error in
            var joinerIdList: [String] = []
            joinerDataList?.forEach({ data in
                let uid = data["user_id"] as? String
                if let uid = uid {
                    joinerIdList.append(uid)
                }
            })
            completed(joinerIdList, error)
        }
    }
}

extension ARTCKaraokeRoomMusicInfo {
    
    open func toPlayingExtentData() -> [AnyHashable: Any] {
        return [
            "id": self.songID,
            "song_name": self.songName,
            "singer_name": self.artist,
            "album_img": self.albumImg,
            "remote_url": self.remoteUrl,
            "duration": self.duration
        ]
    }
    
    open func parseUserExtentData(singerData: [AnyHashable: Any]?) {
        self.userExtendInfo.userID = singerData?["user_id"] as? String ?? ""
        self.userExtendInfo.roomID = singerData?["id"] as? String ?? ""
        self.userExtendInfo.micseatIndex = singerData?["index"] as? Int ?? 0
        self.userExtendInfo.isJoined = singerData?["joined"] as? Bool ?? false
        
        if let extends = singerData?["extends"] as? String {
            let extendsData = (try? JSONSerialization.jsonObject(with: extends.data(using: .utf8)!, options: .allowFragments)) as? [String : Any]
            self.userExtendInfo.userNick = extendsData?["user_nick"] as? String ?? ""
            self.userExtendInfo.userAvatar = extendsData?["user_avatar"] as? String ?? ""
        }
    }
    
    open func parseExtentData(playingData: [AnyHashable: Any]?) {
        let songID = playingData?["id"] as? String
        if self.songID == songID {
            self.songName = playingData?["song_name"] as? String ?? ""
            self.artist = playingData?["singer_name"] as? String ?? ""
            self.albumImg = playingData?["album_img"] as? String ?? ""
            self.remoteUrl = playingData?["remote_url"] as? String ?? ""
            self.duration = playingData?["duration"] as? Int ?? 0
        }
    }
    
    public static func create(playingData: [AnyHashable: Any]) -> ARTCKaraokeRoomMusicInfo? {
        let songID = playingData["song_id"] as? String
        if let songID = songID {
            let musicInfo = ARTCKaraokeRoomMusicInfo(songID: songID)
            musicInfo.singUserId = playingData["user_id"] as? String ?? ""
            musicInfo.status = playingData["status"] as? Int ?? 1
            if let user_extends = playingData["user_extends"] as? String {
                let singerData = (try? JSONSerialization.jsonObject(with: user_extends.data(using: .utf8)!, options: .allowFragments)) as? [String : Any]
                musicInfo.parseUserExtentData(singerData: singerData)
            }
            if let song_extends = playingData["song_extends"] as? String {
                let playingData = (try? JSONSerialization.jsonObject(with: song_extends.data(using: .utf8)!, options: .allowFragments)) as? [String : Any]
                musicInfo.parseExtentData(playingData: playingData)
            }
            return musicInfo
        }
        return nil
    }
}
