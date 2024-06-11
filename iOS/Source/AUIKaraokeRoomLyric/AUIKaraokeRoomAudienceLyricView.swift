//
//  AUIKaraokeRoomAudienceLyricView.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/5/16.
//

import Foundation
import AUIFoundation
import SnapKit

private class AUIKaraokeRoomAudienceLyricLabel: UILabel {
    public var curIndex: Int = -1
}

@objcMembers class AUIKaraokeRoomAudienceLyricView: AUIKaraokeRoomView {
    
    private func updateDisplay(label: AUIKaraokeRoomAudienceLyricLabel, layerLabel: UILabel?, lyricIndex: Int, displayingLyricIndex: Int, progress: UInt32) {
        guard let line = self.lyricViewModel?.lyricModel?.lines else { return }
        if line.count <= lyricIndex || lyricIndex < 0 {
            label.text = ""
        } else {
            let index = lyricIndex
            let displaying = index == displayingLyricIndex
            if label.curIndex == lyricIndex && !displaying{
                return
            }
            label.curIndex = index
            let lyric = self.lyricViewModel?.getLyric(atIndex: index) ?? ""
            label.text = lyric
            guard let layerLabel = layerLabel else { return }
            layerLabel.text = lyric
            layerLabel.isHidden = true
            if displaying && !lyric.isEmpty {
                let line = line[index]
                if let words = line.words {
                    layerLabel.isHidden = false
                    
                    let attributes : [NSAttributedString.Key : Any] = [.font: label.font!]
                    let labelSize = label.text?.size(withAttributes: attributes) ?? CGSize.zero
                    let labelFrame = label.frame
                    layerLabel.frame = CGRect(x: labelFrame.origin.x, y: labelFrame.origin.y, width: 0, height: labelFrame.height)
                                    
                    let lineProgress = line.timeInfo?.progress ?? 0;
                    var endWidth: CGFloat = 0
                    for word in words {
                        let wordSize = word.context?.size(withAttributes: attributes) ?? CGSize.zero
                        let wordProgress = word.timeInfo?.progress ?? 0
                        let wordDuration = word.timeInfo?.duration ?? 0
                        let wordEndProgress = Int(wordProgress + wordDuration)
                        if (progress - lineProgress) >= wordEndProgress {
                            endWidth = endWidth + wordSize.width
                        } else {
                            if (progress - lineProgress) > wordProgress {
                                endWidth = endWidth + CGFloat((progress - lineProgress) - wordProgress) / CGFloat(wordDuration) * wordSize.width
                            }
                            break
                        }
                    }
                    
                    let labelSizeWidth = min(labelFrame.width, labelSize.width)
                    var layerLabelFrameX = labelFrame.origin.x
                    switch label.textAlignment {
                    case .left:
                        break
                    case .center:
                        layerLabelFrameX = (labelFrame.width - labelSizeWidth) / 2 + labelFrame.minX
                        break
                    case .right:
                        layerLabelFrameX = labelFrame.maxX - labelSizeWidth
                        break
                    case .justified:
                        break
                    case .natural:
                        break
                    @unknown default:
                        break
                    }
                    layerLabel.frame = CGRect(x: layerLabelFrameX,
                                              y: labelFrame.origin.y,
                                              width: endWidth,
                                              height: labelFrame.size.height)
                }
            }
        }
    }
    
    override func initSubView() {
        
        self.lyricViewModel?.onUpdateDisplayingIndex = {[weak self] displayingIndex, progress in
            guard let self = self else { return }
            var firstLabelIndex: Int = 0
            var secondLabelIndex: Int = displayingIndex
            var thirdLabelIndex: Int = 0
            var forthLabelIndex: Int = 0
            if displayingIndex < 0 {
                secondLabelIndex = 0
            }
            firstLabelIndex = secondLabelIndex - 1
            thirdLabelIndex = secondLabelIndex + 1
            forthLabelIndex = secondLabelIndex + 2
            self.updateDisplay(label: self.firstLabel, layerLabel: nil, lyricIndex: firstLabelIndex, displayingLyricIndex: displayingIndex, progress: progress)
            self.updateDisplay(label: self.secondLabel, layerLabel: self.secondLayerLabel, lyricIndex: secondLabelIndex, displayingLyricIndex: displayingIndex, progress: progress)
            self.updateDisplay(label: self.thirdLabel, layerLabel: nil, lyricIndex: thirdLabelIndex, displayingLyricIndex: displayingIndex, progress: progress)
            self.updateDisplay(label: self.forthLabel, layerLabel: nil, lyricIndex: forthLabelIndex, displayingLyricIndex: displayingIndex, progress: progress)
        }
        
        self.addSubview(self.firstLabel)
        self.firstLabel.snp.makeConstraints { make in
            make.top.equalTo(50)
            make.left.equalTo(14)
            make.right.equalTo(-14)
            make.height.equalTo(18)
        }
        
        self.addSubview(self.secondLabel)
        self.secondLabel.snp.makeConstraints { make in
            make.top.equalTo(self.firstLabel.snp.bottom).offset(4)
            make.left.equalTo(14)
            make.right.equalTo(-14)
            make.height.equalTo(24)
        }
        self.addSubview(self.secondLayerLabel)
        self.secondLayerLabel.isHidden = true
        
        self.addSubview(self.thirdLabel)
        self.thirdLabel.snp.makeConstraints { make in
            make.top.equalTo(self.secondLabel.snp.bottom).offset(4)
            make.left.equalTo(14)
            make.right.equalTo(-14)
            make.height.equalTo(18)
        }
        
        self.addSubview(self.forthLabel)
        self.forthLabel.snp.makeConstraints { make in
            make.top.equalTo(self.thirdLabel.snp.bottom).offset(4)
            make.left.equalTo(14)
            make.right.equalTo(-14)
            make.height.equalTo(18)
        }
    }
    
    override func updateSubViews() {
        self.firstLabel.text = ""
        self.secondLabel.text = ""
        self.secondLayerLabel.text = ""
        self.thirdLabel.text = ""
        self.forthLabel.text = ""
    }
    
    public func updateProgress(_ progress:UInt32) {
        DispatchQueue.main.async {
            self.lyricViewModel?.updateProgress(progress)
        }
    }
    
    public func isLineFinished() -> Bool {
        return self.lyricViewModel?.lineFinished ?? false
    }
    
    public func resetLineState() {
        self.lyricViewModel?.lineFinished = false
    }
    
    private var lyricViewModel: AUIKaraokeRoomLyricViewModel? {
        get
        {
            return self.viewModel as? AUIKaraokeRoomLyricViewModel
        }
    }
    
    private lazy var firstLabel: AUIKaraokeRoomAudienceLyricLabel = {
        let label = AUIKaraokeRoomAudienceLyricLabel(frame: CGRect.zero)
        label.textColor = AVTheme.text_weak
        label.font = AVTheme.regularFont(12)
        label.lineBreakMode = NSLineBreakMode.byClipping
        label.textAlignment = .center
        return label
    }()
    
    private lazy var secondLabel: AUIKaraokeRoomAudienceLyricLabel = {
        let label = AUIKaraokeRoomAudienceLyricLabel(frame:  CGRect.zero)
        label.textColor = AVTheme.text_strong
        label.font = AVTheme.regularFont(16)
        label.lineBreakMode = NSLineBreakMode.byClipping
        label.textAlignment = .center
        return label
    }()
    
    private lazy var secondLayerLabel: UILabel = {
        let label = UILabel(frame:  CGRect.zero)
        label.textColor = UIColor.av_color(withHexString: "#00BCD4")
        label.font = AVTheme.regularFont(16)
        label.lineBreakMode = NSLineBreakMode.byClipping
        label.textAlignment = .left
        return label
    }()
    
    private lazy var thirdLabel: AUIKaraokeRoomAudienceLyricLabel = {
        let label = AUIKaraokeRoomAudienceLyricLabel(frame:  CGRect.zero)
        label.textColor = AVTheme.text_weak
        label.font = AVTheme.regularFont(12)
        label.lineBreakMode = NSLineBreakMode.byClipping
        label.textAlignment = .center
        return label
    }()
    
    private lazy var forthLabel: AUIKaraokeRoomAudienceLyricLabel = {
        let label = AUIKaraokeRoomAudienceLyricLabel(frame:  CGRect.zero)
        label.textColor = AVTheme.text_weak
        label.font = AVTheme.regularFont(12)
        label.lineBreakMode = NSLineBreakMode.byClipping
        label.textAlignment = .center
        return label
    }()
}
