//
//  AUIKaraokeRoomLyricModel.swift
//  Pods
//
//  Created by aliyun on 2024/5/6.
//

import Foundation

struct AUIKaraokeRoomLyricTimeInfo {
    public var duration: UInt32
    public var progress: UInt32
}

class AUIKaraokeRoomLyricWord: AUIKaraokeRoomModel {
//    public var position: UInt32
//    public var duration: UInt32
    public var timeInfo: AUIKaraokeRoomLyricTimeInfo? = nil
    public var context: String? = nil
}

class AUIKaraokeRoomLyricLine: AUIKaraokeRoomLyricWord {
    public var words: [AUIKaraokeRoomLyricWord]? = nil
}

class AUIKaraokeRoomLyricModel: AUIKaraokeRoomModel {
//    public var ar: String? = nil
//    public var ti: String? = nil
//    public var co: String? = nil
//    public var lr: String? = nil
    public var info: [String]? = nil
    public var lines: [AUIKaraokeRoomLyricLine]? = nil
}
