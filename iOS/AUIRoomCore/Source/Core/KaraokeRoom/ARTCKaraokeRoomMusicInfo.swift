//
//  ARTCKaraokeRoomMusicInfo.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/8.
//

import UIKit


@objc public enum ARTCKaraokeRoomMusicPlayoutType: Int {
    case Invalid = 0 /*! 不支持的播放形式 */
    case Single = 1  /*! 无伴奏、原唱分离 */
    case FileSeparate = 2       /*! 人声、伴奏分别对应两个文件 */
    case ChannelLeftFullRightAccompany = 3  /*! 左声道存储原唱+伴奏，右声道存储伴奏 */
    case ChannelLeftHumanRightAccompany = 4 /*! 左声道存储人声，右声道存储伴奏 */
    case MultipleTrack = 5 /*! 多音轨，假定单个供应商的人声音轨是固定的，与vendor ID配合 */
}


// 榜单信息
@objcMembers open class ARTCKaraokeRoomMusicChartInfo: NSObject {
    
    public init(chartId: String) {
        self.chartId = chartId
    }

    public let chartId: String
    open var chartName: String = ""
    
    open var musicList: [ARTCKaraokeRoomMusicInfo] = []
    open var page: Int = 0
}

// 歌曲

@objcMembers open class ARTCKaraokeRoomMusicUserExtendInfo: NSObject {
    open var userID: String = ""
    open var userNick: String = ""
    open var userAvatar: String = ""
    open var roomID: String = ""
    open var micseatIndex: Int = 0
    open var isJoined: Bool = false
}

@objcMembers open class ARTCKaraokeRoomMusicInfo: NSObject {
    
    public init(songID: String) {
        self.songID = songID
        super.init()
    }

    open var songID: String = ""
    open var status: Int = 0
    open var playoutType: ARTCKaraokeRoomMusicPlayoutType = .Invalid
    open var songName: String = ""
    open var artist: String = ""
    open var lyric: String = ""
    open var singerName: String = ""
    open var albumImg: String = ""
    open var duration: Int = 0
    open var remoteUrl: String = ""
    open var localPath: String = ""
    open var pickedTimeStamp: Int = 0
    open var networkTimeStamp: Int = 0
    open var isTwoTrackInOneFile: Bool = true
    open var isHttpFile: Bool = false
    open var pitchJson: String = ""
    open var singUserId: String = ""
    open var joinSingUserIds: [String] = []
    open var userExtendInfo: ARTCKaraokeRoomMusicUserExtendInfo = ARTCKaraokeRoomMusicUserExtendInfo()
    open var singUserIsMe: Bool = false
    open var singUserIsAnchor: Bool = false
    open var resIsCached: Bool = false
    open var resIsReady: Bool {
        get {
            if !self.localPath.isEmpty || (!self.songID.isEmpty && self.resIsCached) {
                return true
                } else {
                    return false
                }
        }
    }

}
