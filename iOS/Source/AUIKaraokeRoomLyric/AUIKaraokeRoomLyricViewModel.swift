//
//  AUIKaraokeRoomLyricViewModel.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/5/6.
//

import Foundation

class AUIKaraokeRoomLyricViewModel: AUIKaraokeRoomViewModel {
    public var progress: UInt32 = 0
    public var displayingIndex: NSInteger = 0
    public var lineFinished: Bool = false
    public var onUpdateDisplayingIndex: ((_ displayingIndex: Int, _ progress: UInt32) -> Void)? = nil
    public var lyricModel: AUIKaraokeRoomLyricModel? {
        get {
            return self.model as? AUIKaraokeRoomLyricModel
        }
    }
    
    public func loadLyric(withSongID songID: String) {
        self.sungWords = []
        var info: [String] = []
        var linesArrM: [AUIKaraokeRoomLyricLine] = []
        if let filePath = Bundle.main.path(forResource: songID, ofType: "krc"),
           let lyricString = try? String(contentsOfFile: filePath, encoding: .utf8) {
            let linesArr = lyricString.components(separatedBy: "[")
            for lineString in linesArr {
                guard !lineString.isEmpty else { continue }
                
                if lineString.contains(":") {
                    info.append(lineString.replacingOccurrences(of: "]", with: ""))
                } else {
                    let tmpArr = lineString.components(separatedBy: "]")
                    guard tmpArr.count > 0 else { continue }
                    let lineTimeInfo = self.getTimeInfoFromLyric(tmpArr[0])
                    let line = AUIKaraokeRoomLyricLine()
                    line.timeInfo = lineTimeInfo
                    
                    let wordsString = tmpArr.last!
                    let wordsArrComponents = wordsString.components(separatedBy: "(")
                    var wordsArr = [AUIKaraokeRoomLyricWord]()
                    for wordString in wordsArrComponents {
                        guard !wordString.isEmpty else { continue }
                        let word = AUIKaraokeRoomLyricWord()
                        word.timeInfo = self.getTimeInfoFromLyric(wordString)
                        word.context = self.getWordFromLyric(wordString)
                        wordsArr.append(word)
                    }
                    
                    line.words = wordsArr
                    linesArrM.append(line)
                }
            }
        }
        
        guard let model = self.lyricModel else { return }
        model.info = info
        model.lines = linesArrM
        self.dataDidUpdateBlock?()
        
        self.displayingIndex = -1
        self.onUpdateDisplayingIndex?(self.displayingIndex, 0)
    }
    
    public func loadLyric(_ lyricString: String?) {
        self.sungWords = []
        var info: [String] = []
        var linesArrM: [AUIKaraokeRoomLyricLine] = []
        if let lyricString = lyricString {
            let linesArr = lyricString.components(separatedBy: "[")
            for lineString in linesArr {
                guard !lineString.isEmpty else { continue }
                
                if lineString.contains(":") {
                    info.append(lineString.replacingOccurrences(of: "]", with: ""))
                } else {
                    let tmpArr = lineString.components(separatedBy: "]")
                    guard tmpArr.count > 0 else { continue }
                    let lineTimeInfo = self.getTimeInfoFromLyric(tmpArr[0])
                    let line = AUIKaraokeRoomLyricLine()
                    line.timeInfo = lineTimeInfo
                    
                    let wordsString = tmpArr.last!
                    let wordsArrComponents = wordsString.components(separatedBy: "<")
                    var wordsArr = [AUIKaraokeRoomLyricWord]()
                    for wordString in wordsArrComponents {
                        guard !wordString.isEmpty else { continue }
                        let word = AUIKaraokeRoomLyricWord()
                        word.timeInfo = self.getTimeInfoFromLyric(wordString)
                        word.context = self.getWordFromLyric(wordString)
                        wordsArr.append(word)
                    }
                    
                    line.words = wordsArr
                    linesArrM.append(line)
                }
            }
        }
        
        guard let model = self.lyricModel else { return }
        model.info = info
        model.lines = linesArrM
        self.dataDidUpdateBlock?()
        
        self.displayingIndex = -1
        self.onUpdateDisplayingIndex?(self.displayingIndex, 0)
    }
    
    public func updateProgress(_ progress: UInt32) {
        self.lastProgress = self.progress
        self.progress = progress
        guard let lyricModel = self.lyricModel,
              let lines = lyricModel.lines,
              lines.count > 0 else { return }
        
        var currentLine = lines.first!
        if self.displayingIndex == -1 {
            if self.progress < (currentLine.timeInfo?.progress ?? 0) {
                return
            }
            self.displayingIndex += 1
        } else {
            if self.progress < self.lastProgress {
                self.displayingIndex = 0
            }
            for i in self.displayingIndex ..< lines.count {
                currentLine = lines[i]
                if self.isPositionBetweenTimeInfo(currentLine.timeInfo),
                   let words = currentLine.words {
                    for word in words {
                        if self.isPositionBetweenTimeInfo(word.timeInfo),
                           (self.sungWords?.contains(word) ?? false){
                            self.sungWords?.append(word)
                            print("KTV lyric word:\(String(describing: word.context)) position:\(String(describing: word.timeInfo?.duration)) duration:\(String(describing: word.timeInfo?.duration)) progress:\(progress)")
                        }
                    }
                    
                    if self.displayingIndex == i {
                        break
                    } else {
                        self.lineFinished = true
                        self.displayingIndex = i
                        break
                    }
                }
            }
        }
        
        self.sungWords = []
        self.onUpdateDisplayingIndex?(self.displayingIndex, progress)
    }
    
    public func getInfo() -> String {
        var context = ""
        if let info = self.lyricModel?.info {
            for string in info {
                context.append(string)
            }
        }
        return context
    }
    
    public func getLyric(atIndex index: Int) -> String {
        guard let lyricModel = self.lyricModel else { return "" }
        if (lyricModel.lines?.count ?? 0) > index && index >= 0 {
            let line = lyricModel.lines![index]
            var context = ""
            if line.words != nil {
                for word in line.words! {
                    context.append(word.context ?? "")
                }
            }
            return context
        } else {
            return ""
        }
    }
    
    public func getSungWords() -> String {
        var words = ""
        if let sungWords = self.sungWords {
            for word in sungWords {
                words.append(word.context ?? "")
            }
        }
        return words
    }
    
    public func cleanLyric() {
        self.lyricModel?.info = []
        self.lyricModel?.lines = []
        self.dataDidUpdateBlock?()
    }
    
    private var lastProgress: UInt32 = 0
    private var sungWords: [AUIKaraokeRoomLyricWord]? = nil
    
    private func getWordFromLyric(_ lyric: String?) -> String? {
        guard var word = lyric else { return lyric }
        if word.contains(")") {
            word = word.components(separatedBy: ")").last!
        }
        if word.contains(">") {
            word = word.components(separatedBy: ">").last!
        }
        if word.contains("\n") {
            word = word.replacingOccurrences(of: "\n", with: "")
        }
        return word
    }
    
    private func getTimeInfoFromLyric(_ lyric: String?) -> AUIKaraokeRoomLyricTimeInfo {
        var timeInfo = AUIKaraokeRoomLyricTimeInfo(duration: 0, progress: 0)
        
        guard let lyric = lyric, lyric.contains(",") else { return timeInfo}
        
        var tmpArr: [String] = []
        if lyric.contains(")") {
            tmpArr = lyric.components(separatedBy: ")").first!.components(separatedBy: ",")
        } else if (lyric.contains("]")) {
            tmpArr = lyric.replacingOccurrences(of: "]", with: "").components(separatedBy: ",")
        } else if lyric.contains(">") {
            tmpArr = lyric.components(separatedBy: ">").first!.components(separatedBy: ",")
        } else {
            tmpArr = lyric.components(separatedBy: ",")
        }
        timeInfo.progress = UInt32((tmpArr.first! as NSString).integerValue)
        timeInfo.duration = UInt32((tmpArr[1] as NSString).integerValue)
        
        return timeInfo
    }
    
    private func isPositionBetweenTimeInfo(_ timeInfo: AUIKaraokeRoomLyricTimeInfo?) -> Bool {
        guard let timeInfo = timeInfo else { return false }
        return timeInfo.progress <= self.progress && self.progress <= (timeInfo.progress + timeInfo.duration)
    }
}

