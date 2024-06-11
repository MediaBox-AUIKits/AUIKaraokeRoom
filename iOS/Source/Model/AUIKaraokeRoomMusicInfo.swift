//
//  AUIKaraokeRoomMusicInfo.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/5/8.
//

import Foundation
import AUIRoomCore

@objcMembers open class AUIKaraokeRoomMusicInfo: ARTCKaraokeRoomMusicInfo {
    enum MusicInfoAddState {
        case normal
        case downloading
        case added
    }
    var addedState: MusicInfoAddState = .normal {
        didSet {
            self.addedStateUpdate?(self.addedState)
        }
    }
    var addedStateUpdate: ((_ addedState: MusicInfoAddState)->Void)? = nil
    
    var itemIndex: Int = 0
    
    public static func musicInfoWithServerMusicInfo(_ serverMusicInfo: ARTCKaraokeRoomMusicInfo?) -> AUIKaraokeRoomMusicInfo? {
        guard let serverMusicInfo = serverMusicInfo else { return nil }
        let music = AUIKaraokeRoomMusicInfo(songID: serverMusicInfo.songID)
        music.songName = serverMusicInfo.songName
        music.artist = serverMusicInfo.artist
        music.albumImg = serverMusicInfo.albumImg
        music.duration = serverMusicInfo.duration
        music.isTwoTrackInOneFile = serverMusicInfo.isTwoTrackInOneFile
        music.singUserIsMe = serverMusicInfo.singUserIsMe
        music.singUserIsAnchor = serverMusicInfo.singUserIsAnchor
        music.singerName = serverMusicInfo.singerName
        music.userExtendInfo = serverMusicInfo.userExtendInfo
        music.singUserId = serverMusicInfo.singUserId
        music.pitchJson = serverMusicInfo.pitchJson
        music.lyric = serverMusicInfo.lyric
        music.resIsCached = serverMusicInfo.resIsCached
        return music
    }
    
    public static func musicInfosWithServerMusicInfos(_ serverMusicInfos: [ARTCKaraokeRoomMusicInfo]?) -> [AUIKaraokeRoomMusicInfo] {
        var musicInfos = [AUIKaraokeRoomMusicInfo]()
        guard let serverMusicInfos = serverMusicInfos else { return musicInfos }
        for serverMusicInfo in serverMusicInfos {
            let musicInfo = AUIKaraokeRoomMusicInfo.musicInfoWithServerMusicInfo(serverMusicInfo)!
            musicInfos.append(musicInfo)
        }
        return musicInfos
    }
}
