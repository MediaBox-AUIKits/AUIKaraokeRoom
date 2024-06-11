//
//  ARTCKaraokeRoomInfo.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/8.
//

import UIKit

@objc public enum ARTCKaraokeRoomState: Int {
    case Normal = 1           // 初始状态
    case Waiting            // 等待状态
    case Playing             // 播放状态
}

@objc public enum ARTCKaraokeRoomUpdatePlayingMusicListReason: Int {
    case Unknown = -1
    case AddMusic = 1           // 点歌
    case RemoveMusic            // 删除歌曲
    case PinMusic               // 置顶歌曲
    case CutMusic               // 切歌
    case PlayingComplete        // 播放结束
    case JoinSinging            // 加入合唱
    case LeaveSinging           // 退出合唱
    case Other                  // 其他
}

@objcMembers open class ARTCKaraokeRoomInfo: ARTCVoiceRoomInfo {

    public var roomState: ARTCKaraokeRoomState = .Normal
    
    open override func getMode() -> ARTCRoomSceneType {
        return .KTV
    }

}
