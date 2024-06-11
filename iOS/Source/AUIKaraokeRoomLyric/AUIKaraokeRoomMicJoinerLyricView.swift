//
//  AUIKaraokeRoomMicJoinerLyricView.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/5/16.
//

import Foundation
import AUIFoundation
import SnapKit

private class AUIKaraokeRoomMicJoinerLyricLabel: UILabel {
    public var curIndex: Int = -1
}

@objcMembers class AUIKaraokeRoomMicJoinerLyricView: AUIKaraokeRoomView {
    
    private func updateDisplay(label: AUIKaraokeRoomMicJoinerLyricLabel, layerLabel: UILabel, lyricIndex: Int, displayingLyricIndex: Int, progress: UInt32) {
        guard let line = self.lyricViewModel?.lyricModel?.lines else { return }
        if line.count > lyricIndex {
            let index = lyricIndex
            let displaying = index == displayingLyricIndex
            if label.curIndex == lyricIndex && !displaying{
                return
            }
            label.curIndex = index
            let lyric = self.lyricViewModel?.getLyric(atIndex: index) ?? ""
            label.text = lyric
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
            var firstLabelIndex: Int
            var secondLabelIndex: Int
            if displayingIndex < 0 {
                firstLabelIndex = 0
                secondLabelIndex = 1
            } else if displayingIndex % 2 == 1 {
                secondLabelIndex = displayingIndex
                firstLabelIndex = displayingIndex + 1
            } else {
                firstLabelIndex = displayingIndex
                secondLabelIndex = displayingIndex + 1
            }
            self.updateDisplay(label: self.firstLabel, layerLabel: self.firstLayerLabel, lyricIndex: firstLabelIndex, displayingLyricIndex: displayingIndex, progress: progress)
            self.updateDisplay(label: self.secondLabel, layerLabel: self.secondLayerLabel, lyricIndex: secondLabelIndex, displayingLyricIndex: displayingIndex, progress: progress)
        }
        
        self.addSubview(self.firstLabel)
        self.firstLabel.snp.makeConstraints { make in
            make.top.equalTo(0)
            make.left.equalTo(14)
            make.right.equalTo(-14)
            make.height.equalTo(22)
        }
        self.addSubview(self.firstLayerLabel)
        self.firstLayerLabel.isHidden = true
        
        self.addSubview(self.secondLabel)
        self.secondLabel.snp.makeConstraints { make in
            make.bottom.equalToSuperview()
            make.left.equalTo(14)
            make.right.equalTo(-14)
            make.height.equalTo(22)
        }
        self.addSubview(self.secondLayerLabel)
        self.secondLayerLabel.isHidden = true
    }
    
    override func updateSubViews() {
        self.firstLabel.text = ""
        self.firstLayerLabel.text = ""
        self.secondLabel.text = ""
        self.secondLayerLabel.text = ""
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
    
    private lazy var firstLabel: AUIKaraokeRoomMicJoinerLyricLabel = {
        let label = AUIKaraokeRoomMicJoinerLyricLabel(frame: CGRect.zero)
        label.textColor = AVTheme.text_strong
        label.font = AVTheme.regularFont(14)
        label.lineBreakMode = NSLineBreakMode.byClipping
        label.textAlignment = .left
        return label
    }()
    
    private lazy var firstLayerLabel: UILabel = {
        let label = UILabel(frame: CGRect.zero)
        label.textColor = UIColor.av_color(withHexString: "#00BCD4")
        label.font = AVTheme.regularFont(14)
        label.lineBreakMode = NSLineBreakMode.byClipping
        label.textAlignment = .left
        return label
    }()
    
    private lazy var secondLabel: AUIKaraokeRoomMicJoinerLyricLabel = {
        let label = AUIKaraokeRoomMicJoinerLyricLabel(frame: CGRect.zero)
        label.textColor = AVTheme.text_strong
        label.font = AVTheme.regularFont(14)
        label.lineBreakMode = NSLineBreakMode.byClipping
        label.textAlignment = .right
        return label
    }()
    
    private lazy var secondLayerLabel: UILabel = {
        let label = UILabel(frame: CGRect.zero)
        label.textColor = UIColor.av_color(withHexString: "#00BCD4")
        label.font = AVTheme.regularFont(14)
        label.lineBreakMode = NSLineBreakMode.byClipping
        label.textAlignment = .left
        return label
    }()
}
