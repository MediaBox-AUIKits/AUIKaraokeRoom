//
//  ARTCKaraokeRoomMusicLibrary.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/6.
//

import UIKit

#if canImport(AliVCSDK_ARTC)
import AliVCSDK_ARTC
#elseif canImport(AliVCSDK_InteractiveLive)
import AliVCSDK_InteractiveLive
#elseif canImport(AliVCSDK_Standard)
import AliVCSDK_Standard
#endif

/*=============================曲库===========================*/
@objcMembers open class ARTCKaraokeRoomMusicLibrary: NSObject {

    public static let shared: ARTCKaraokeRoomMusicLibrary = {
        let instance = ARTCKaraokeRoomMusicLibrary()
        return instance
    }()

    // 榜单列表
    public var musicChartList: [ARTCKaraokeRoomMusicChartInfo] = []
    
    // 歌词缓存列表
    public var musicLyricCache: [String: String] = [:]
    
    // 音高缓存列表
    public var musicPitchCache: [String: String] = [:]
    
    // 获取榜单列表
    public func fetchMusicChartList(completed: @escaping ARTCKaraokeRoomFetchMusicChartListCompleted) {
        DispatchQueue.main.async{
            let requestId = self.musicContentCenter?.getMusicCharts()
            if let requestId = requestId {
                self.fetchMusicChartListCallbackBlock[requestId] = completed
            } else {
                completed([], ARTCRoomError.createError(.Common, "fetchMusicChartList no requestID"))
            }
        }
    }
    
    // 获取榜单歌曲列表
    public func fetchMusicList(chartId: String, page: Int, pageSize: Int, completed: @escaping ARTCKaraokeRoomFetchMusicListCompleted) {
        DispatchQueue.main.async{
            let requestId = self.musicContentCenter?.getMusicCollection(byChartId: chartId, page: Int32(page), pageSize: Int32(pageSize), jsonOption: nil)
            if let requestId = requestId {
                self.fetchMusicListCompletedBlocks[requestId] = completed
            } else {
                completed([], ARTCRoomError.createError(.Common, "fetchMusicList no requestID"))
           }
        }
    }
    
    // 获取歌曲信息
    public func fetchMusicInfo(songID: String, completed: @escaping ARTCKaraokeRoomFetchMusicInfoCompleted) {
        DispatchQueue.main.async{
            let requestId = self.musicContentCenter?.getSongInfo(songID)
            if let requestId = requestId {
                self.fetchMusicInfoCompletedBlocks[requestId] = completed
            } else {
                completed(nil, ARTCRoomError.createError(.Common, "fetchMusicInfo no requestID"))
            }
        }
    }
    
    // 获取歌词
    public func fetchMusicLyric(songID: String, completed: @escaping ARTCKaraokeRoomFetchMusicLyricCompleted) {
        let lyric = ARTCKaraokeRoomMusicLibrary.shared.musicLyricCache[songID] ?? ""
        if !lyric.isEmpty {
            completed(songID, lyric, nil)
            return
        }
        DispatchQueue.main.async{
            let requestId = self.musicContentCenter?.getLyric(songID)
            if let requestId = requestId {
                self.fetchMusicLyricCompletedBlocks[requestId] = completed
            } else {
                completed(songID, nil, ARTCRoomError.createError(.Common, "fetchMusicLyric no requestID"))
            }
        }
    }
    
    // 获取歌曲标准音高
    public func fetchMusicPitch(songID: String, completed: @escaping ARTCKaraokeRoomFetchMusicPitchCompleted) {
        let pitchJson = ARTCKaraokeRoomMusicLibrary.shared.musicPitchCache[songID] ?? ""
        if !pitchJson.isEmpty {
            completed(songID, pitchJson, nil)
            return
        }
        DispatchQueue.main.async{
            let requestId = self.musicContentCenter?.getSongStandardPitch(songID)
            if let requestId = requestId {
                self.fetchMusicPitchCompletedBlocks[requestId] = completed
            } else {
                completed(songID, nil, ARTCRoomError.createError(.Common, "fetchMusicPitch no requestID"))
            }
        }
    }
    
    // 搜索歌曲
    public func searchMusic(keyword: String, page: Int, pageSize: Int, completed: @escaping ARTCKaraokeRoomSearchMusicCompleted) {
        DispatchQueue.main.async{
            let requestId = self.musicContentCenter?.searchMusic(keyword, vendorId: [0], preferVendor: 0, page: Int32(page), pageSize: Int32(pageSize), jsonOption: nil)
            if let requestId = requestId {
                self.searchMusicListCompletedBlocks[requestId] = completed
            } else {
                completed([], ARTCRoomError.createError(.Common, "searchMusic no requestID"))
            }
        }
    }
    
    // 下载歌曲
    public func downloadMusic(songID: String, completed: @escaping ARTCKaraokeRoomDownloadMusicCompleted) {
        if self.isMusicDownloaded(songID: songID) {
            completed(songID, nil)
            return
        }
        DispatchQueue.main.async{
            let requestId = self.musicContentCenter?.getSongResource(songID)
            if let requestId = requestId {
                self.downloadMusicCompletedBlocks[requestId] = completed
            } else {
                completed(songID, ARTCRoomError.createError(.Common, "downloadMusic no requestID"))
            }
        }
    }
    
    // 歌曲是否已经下载完成
    public func isMusicDownloaded(songID: String) -> Bool {
        return self.musicContentCenter?.getCacheState(songID) ?? AliMusicCacheState.none == AliMusicCacheState.done
    }
    
    // 歌曲是否下载中
    public func isMusicDownloading(songID: String) -> Bool {
        return self.musicContentCenter?.getCacheState(songID) ?? AliMusicCacheState.none == AliMusicCacheState.loading
    }
    
    // 获取歌曲的所有资源，包括歌词、音高、歌曲文件等
    public func fetchMusicAllResource(musicInfo: ARTCKaraokeRoomMusicInfo, completed: @escaping ARTCKaraokeRoomDownloadMusicCompleted) {
        self.downloadMusic(songID: musicInfo.songID) {[weak self] songID, error in
            guard let self = self else { return }
            if error != nil {
                completed(songID, error)
                return
            }
            if songID != nil && songID!.isEqual(musicInfo.songID) {
                musicInfo.resIsCached = true
            }
            self.fetchMusicLyric(songID: musicInfo.songID) { songID, lyric, error in
                if error != nil {
                    completed(songID, error)
                    return
                }
                if songID != nil && songID!.isEqual(musicInfo.songID) {
                    musicInfo.lyric = lyric ?? ""
                }
                self.fetchMusicPitch(songID: musicInfo.songID, completed: { songID, pitch, error in
                    if error != nil {
                        completed(songID, error)
                        return
                    }
                    if songID != nil && songID!.isEqual(musicInfo.songID) {
                        musicInfo.pitchJson = pitch ?? ""
                    }
                    completed(songID, nil)
                })
            }
        }
    }

    // 销毁曲库实例
    public func destroy() {
        self.musicLyricCache = [:]
        self.musicPitchCache = [:]
        DispatchQueue.main.async{
            self.fetchMusicChartListCallbackBlock.removeAll()
            self.fetchMusicListCompletedBlocks.removeAll()
            self.fetchMusicInfoCompletedBlocks.removeAll()
            self.fetchMusicLyricCompletedBlocks.removeAll()
            self.fetchMusicPitchCompletedBlocks.removeAll()
            self.searchMusicListCompletedBlocks.removeAll()
            self.downloadMusicCompletedBlocks.removeAll()
        }
        if let musicContentCenter = self._musicContentCenter {
            musicContentCenter.removeAllCache()
            musicContentCenter.destroy()
            self._musicContentCenter = nil
        }
    }
    
    public typealias ARTCKaraokeRoomFetchMusicChartListCompleted = (_ chartList: [ARTCKaraokeRoomMusicChartInfo], _ error: NSError?)->Void
    public typealias ARTCKaraokeRoomFetchMusicListCompleted = (_ musicList: [ARTCKaraokeRoomMusicInfo]?, _ error: NSError?) -> Void
    public typealias ARTCKaraokeRoomFetchMusicInfoCompleted = (_ musicInfo: ARTCKaraokeRoomMusicInfo?, _ error: NSError?) -> Void
    public typealias ARTCKaraokeRoomFetchMusicLyricCompleted = (_ songID: String?, _ lyric: String?, _ error: NSError?) -> Void
    public typealias ARTCKaraokeRoomFetchMusicPitchCompleted = (_ songID: String?, _ pitch: String?, _ error: NSError?) -> Void
    public typealias ARTCKaraokeRoomSearchMusicCompleted = (_ musicList: [ARTCKaraokeRoomMusicInfo]?, _ error: NSError?) -> Void
    public typealias ARTCKaraokeRoomDownloadMusicCompleted = (_ songID: String?, _ error: NSError?) -> Void
    
    private var _musicContentCenter: AliMusicContentCenter? = nil
    private var musicContentCenter: AliMusicContentCenter? {
        get {
            if _musicContentCenter == nil {
                let musicContentCenterConfig = AliMusicContentCenterConfiguration()
                musicContentCenterConfig.sceneId = 1
                musicContentCenterConfig.maxCache = 20
                musicContentCenterConfig.cacheDir =  NSHomeDirectory().appending("/Library/Caches/ALI_RTC_MUSIC")
                _musicContentCenter = AliMusicContentCenter.sharedInstance(self,
                                                                               config: musicContentCenterConfig,
                                                                               extras: nil)
            }
            return _musicContentCenter
        }
        set {
            _musicContentCenter = newValue
        }
    }
    
    private var fetchMusicChartListCallbackBlock = [String: ARTCKaraokeRoomFetchMusicChartListCompleted]()
    private var fetchMusicListCompletedBlocks = [String: ARTCKaraokeRoomFetchMusicListCompleted]()
    private var fetchMusicInfoCompletedBlocks = [String: ARTCKaraokeRoomFetchMusicInfoCompleted]()
    private var fetchMusicLyricCompletedBlocks = [String: ARTCKaraokeRoomFetchMusicLyricCompleted]()
    private var fetchMusicPitchCompletedBlocks = [String: ARTCKaraokeRoomFetchMusicPitchCompleted]()
    private var searchMusicListCompletedBlocks = [String: ARTCKaraokeRoomSearchMusicCompleted]()
    private var downloadMusicCompletedBlocks = [String: ARTCKaraokeRoomDownloadMusicCompleted]()
    
    private override init() {
        super.init()
    }
}

extension ARTCKaraokeRoomMusicLibrary: AliMusicContentCenterDelegate {
    
    public func onError(_ code: AliMusicContentCenterErrorCode) {
        self.destroy()
        return
    }
    
    public func onMusicChartsResult(_ requestId: String, charts: String?, errorCode: AliMusicContentCenterErrorCode) {
        self.musicChartList = ARTCKaraokeRoomMusicLibrary.analyzeChartData(json: charts) ?? []
        DispatchQueue.main.async{
            self.fetchMusicChartListCallbackBlock[requestId]?(self.musicChartList, errorCode == .success ? nil : ARTCRoomError.createError(code: Int(errorCode.rawValue), message: nil))
            self.fetchMusicChartListCallbackBlock.removeValue(forKey: requestId)
        }
    }
    
    public func onMusicCollectionResult(_ requestId: String, chartId: String?, musicInfos infos: [MusicInfo]?, page: Int32, pageSize: Int32, total: Int32, errorCode: AliMusicContentCenterErrorCode) {

        var musicInfos = [ARTCKaraokeRoomMusicInfo]()
        if let infos = infos {
            for info in infos {
                let musicInfo = ARTCKaraokeRoomMusicLibrary.musicInfoWithServerInfo(info: info)
                if musicInfo != nil {
                    musicInfos.append(musicInfo!)
                }
            }
        }
        DispatchQueue.main.async{
            self.fetchMusicListCompletedBlocks[requestId]?(musicInfos, errorCode == .success ? nil : ARTCRoomError.createError(code: Int(errorCode.rawValue), message: nil))
            self.fetchMusicListCompletedBlocks.removeValue(forKey: requestId)
        }
    }
    
    public func onSongInfoResult(_ requestId: String, songId: String?, musicInfo info: MusicInfo?, errorCode: AliMusicContentCenterErrorCode) {
        let musicInfo = ARTCKaraokeRoomMusicLibrary.musicInfoWithServerInfo(info: info)
        DispatchQueue.main.async{
            self.fetchMusicInfoCompletedBlocks[requestId]?(musicInfo, nil)
            self.fetchMusicInfoCompletedBlocks.removeValue(forKey: requestId)
        }
    }
    
    public func onSongResourceProgress(_ requestId: String, songId: String?, progress: Int32) {
        return
    }
    
    public func onSongResourceResult(_ requestId: String, songId: String?, errorCode: AliMusicContentCenterErrorCode) {
        guard let songID = songId else { return }
        DispatchQueue.main.async{
            self.downloadMusicCompletedBlocks[requestId]?(songID, nil)
            self.downloadMusicCompletedBlocks.removeValue(forKey: requestId)
        }
    }
    
    public func onSearchMusicResult(_ requestId: String, musicInfos infos: [MusicInfo]?, page: Int32, pageSize: Int32, total: Int32, errorCode: AliMusicContentCenterErrorCode) {
        
        var musicInfos = [ARTCKaraokeRoomMusicInfo]()
        if let infos = infos {
            for info in infos {
                let musicInfo = ARTCKaraokeRoomMusicLibrary.musicInfoWithServerInfo(info: info)
                if musicInfo != nil {
                    musicInfos.append(musicInfo!)
                }
            }
        }
        DispatchQueue.main.async{
            self.searchMusicListCompletedBlocks[requestId]?(musicInfos, nil)
            self.searchMusicListCompletedBlocks.removeValue(forKey: requestId)
        }
    }
    
    public func onSongStandardPitch(_ requestId: String, songId: String?, pitch: String?, offset: Int32, vtime: Int32, errorCode: AliMusicContentCenterErrorCode) {
        if songId != nil && !songId!.isEmpty
            && pitch != nil && !pitch!.isEmpty {
            self.musicPitchCache[songId!] = pitch
        }
        DispatchQueue.main.async{
            self.fetchMusicPitchCompletedBlocks[requestId]?(songId, pitch, nil)
            self.fetchMusicPitchCompletedBlocks.removeValue(forKey: requestId)
        }
    }
    
    public func onLyricResult(_ requestId: String, songId: String?, lyric: String?, lyricType: LyricType, errorCode: AliMusicContentCenterErrorCode) {
        if songId != nil && !songId!.isEmpty
            && lyric != nil && !lyric!.isEmpty {
            self.musicLyricCache[songId!] = lyric
        }
        DispatchQueue.main.async{
            self.fetchMusicLyricCompletedBlocks[requestId]?(songId, lyric, nil)
            self.fetchMusicLyricCompletedBlocks.removeValue(forKey: requestId)
        }
    }
}

extension ARTCKaraokeRoomMusicLibrary {

    private static func analyzeChartData(json: Any?) -> [ARTCKaraokeRoomMusicChartInfo]? {
        guard let json = json,
              let chartArray = self.arrayWithJSON(json) else { return nil }
        var chartInfos = [ARTCKaraokeRoomMusicChartInfo]()
        for dict in chartArray {
            let model = self.chartInfoWithDict(dict)!
            chartInfos.append(model)
        }
        return chartInfos
    }
    
    private static func chartInfoWithDict(_ dict: [String: Any]?) -> ARTCKaraokeRoomMusicChartInfo? {
        guard let dict = dict,
              !dict.isEmpty else { return nil }
        let chart = ARTCKaraokeRoomMusicChartInfo(chartId: dict["top_id"] as? String ?? "")
        chart.chartName = dict["name"] as? String ?? ""
        return chart
    }

    private static func dictionaryWithJSON(_ json: Any?) -> [String: Any]? {
        guard let validJson = json,
                !(json is NSNull) else { return nil }

        var dic: [String: Any]?
        var jsonData: Data?

        if let dict = validJson as? [String: Any] {
            dic = dict
        } else if let string = validJson as? String {
            jsonData = string.data(using: .utf8)
        } else if let data = validJson as? Data {
            jsonData = data
        }

        if let jsonData = jsonData {
            do {
                if let parsedDic = try JSONSerialization.jsonObject(with: jsonData, options: []) as? [String: Any] {
                    dic = parsedDic
                }
            } catch {
                print("Error parsing JSON: \(error)")
            }
        }
        return dic
    }
    
    private static func arrayWithJSON(_ json: Any?) -> [[String: Any]]? {
        guard let validJson = json,
                !(json is NSNull) else { return nil }

        var rspArr: [[String: Any]]?
        var jsonData: Data?

        if let array = validJson as? [[String: Any]] {
            rspArr = array
        } else if let string = validJson as? String {
            jsonData = string.data(using: .utf8)
        } else if let data = validJson as? Data {
            jsonData = data
        }

        if let jsonData = jsonData {
            do {
                if let parsedArr = try JSONSerialization.jsonObject(with: jsonData, options: []) as? [[String: Any]] {
                    rspArr = parsedArr
                }
            } catch {
                print("Error parsing JSON: \(error)")
            }
        }
        return rspArr
    }
    
    private static func musicInfoWithServerInfo(info: MusicInfo?) -> ARTCKaraokeRoomMusicInfo? {
        guard let info = info, info.songId != nil, !info.songId!.isEmpty else { return nil }
        let musicInfo = ARTCKaraokeRoomMusicInfo(songID: info.songId!)
        musicInfo.songName = info.songName ?? ""
        musicInfo.artist = info.singerName ?? ""
        musicInfo.albumImg = info.albumImg ?? ""
        musicInfo.duration = Int(info.duration)
        musicInfo.lyric = ARTCKaraokeRoomMusicLibrary.shared.musicLyricCache[musicInfo.songID] ?? ""
        musicInfo.pitchJson = ARTCKaraokeRoomMusicLibrary.shared.musicPitchCache[musicInfo.songID] ?? ""
        musicInfo.resIsCached = ARTCKaraokeRoomMusicLibrary.shared.isMusicDownloaded(songID: musicInfo.songID)
        return musicInfo
    }
}
