//
//  AUIKaraokeRoomPitchCanvas.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/4/22.
//

import UIKit

protocol AUIKaraokeRoomPitchCanvasProtocol: NSObjectProtocol {
    func updatePitchIndicatorPositionWithEndPoint(_ endPoint: CGPoint)
}

class AUIKaraokeRoomPitchCanvas: UIView {
    public weak var delegate: AUIKaraokeRoomPitchCanvasProtocol?
    
    public func setConfig(_ config: AUIKaraokeRoomPitchViewConfig?) {
        self.config = config
        self.setupUIProperties()
    }
    
    public func drawWithProgress(_ progress: NSInteger, _ stdPitchModels: [AUIKaraokeRoomPitchModel]?, _ hitPitchModels: [AUIKaraokeRoomPitchModel]?, _ pitch: Int) {
        self.progress = progress
        self.stdPitchModelsToDraw = stdPitchModels
        self.hitPitchModelsToDraw = hitPitchModels
        self.pitch = pitch
        self.setNeedsDisplay()
    }
    
    public func clearAll() {
        self.stdPitchModelsToDraw = nil
        self.hitPitchModelsToDraw = nil
        self.progress = 0
        self.setNeedsDisplay()
    }
    
    private var config: AUIKaraokeRoomPitchViewConfig?
    private var stdPitchModelsToDraw: [AUIKaraokeRoomPitchModel]?
    private var hitPitchModelsToDraw: [AUIKaraokeRoomPitchModel]?
    private var pitch: Int = 0 // 10 - 90
    private var progress: NSInteger = 0
    private var msWidth: CGFloat = 0.0
    private var pitchHeight: CGFloat = 0.0
    private var vlineOffsetX: CGFloat = 0.0
}

extension AUIKaraokeRoomPitchCanvas {
    override func draw(_ rect: CGRect) {
        // 五线谱
        self.drawStaff()
        // 标准音高线
        self.drawStandardPitchModels()
        // 击中音高线
        self.drawHitPitchModels()
        // 竖线
        self.drawVerticalLine()
        // 音调指示器
        self.drawPitchIndicator()
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        self.setupUIProperties()
    }
    
    private func setupUIProperties() {
        guard let config = self.config else {return}
        self.backgroundColor = config.backgroundColor
        
        let selfHeight = bounds.height
        let selfWidth = bounds.width
        
        self.msWidth = selfWidth / CGFloat(config.timeElapsedOnScreen + config.timeToPlayOnScreen)
        self.pitchHeight = selfHeight / CGFloat(config.pitchNum)
    }
    
    private func getPitchRectCenterYWithPitch(pitch: Int) -> CGFloat {
        let selfHeight = bounds.height
        if pitch < self.config?.minPitch ?? 0 {
            return selfHeight
        }
        if pitch > self.config?.maxPitch ?? 0 {
            return 0
        }
        var adjustedPitch = pitch
        if pitch == self.config?.minPitch ?? 0 {
            adjustedPitch += 1
        }
        
        let num1 = ((self.config?.maxPitch ?? 0) - adjustedPitch) * (self.config?.pitchNum ?? 0)
        let num2 = (self.config?.maxPitch ?? 0) - (self.config?.minPitch ?? 0)
        return CGFloat(num1 / num2) * self.pitchHeight + self.pitchHeight * 0.50
    }
    
    private func drawStaff() {
        let selfHeight = bounds.height
        let selfWidth = bounds.width
        let lineHeight: CGFloat = 1
        let spaceY = (selfHeight - 5) / 4
        for i in 0..<5 {
            let y = CGFloat(i) * (spaceY + lineHeight)
            let linePath = UIBezierPath(rect: CGRect(x: 0, y: y, width: selfWidth, height: lineHeight))
            let lineColor = self.config?.staffColor ?? .clear
            lineColor.setFill()
            linePath.fill()
        }
    }
    
    private func drawVerticalLine() {
        let selfHeight = bounds.height
        let selfWidth = bounds.width
        let lineWidth: CGFloat = 0.5
        
        let vLineOffsetX = selfWidth * CGFloat(self.config?.timeElapsedOnScreen ?? 0) / CGFloat((self.config?.timeElapsedOnScreen ?? 0) + (self.config?.timeToPlayOnScreen ?? 0))
        self.vlineOffsetX = vLineOffsetX
        let vlineRect = CGRect(x: vLineOffsetX, y: 0, width: lineWidth, height: selfHeight)
        let vlinePath = UIBezierPath(rect: vlineRect)
        let vlineColor = self.config?.verticalLineColor ?? .clear
        vlineColor.setFill()
        vlinePath.fill()
    }
    
    private func drawStandardPitchModels() {
        self.drawPitchModels(pitchModels: self.stdPitchModelsToDraw, fillColor: self.config?.standardRectColor, validate: false)
    }
    
    private func drawHitPitchModels() {
        self.drawPitchModels(pitchModels: self.hitPitchModelsToDraw, fillColor: self.config?.hitRectColor, validate: false)
    }
    
    private func drawPitchModels(pitchModels: [AUIKaraokeRoomPitchModel]?, fillColor: UIColor?, validate: Bool) {
        guard let pitchModels = pitchModels,
              pitchModels.count > 0 else { return }
        #if Release
        let needValidate = false
        #else
        let needValidate = validate
        #endif
        let msWidth = self.msWidth
        let pitchHeight = self.pitchHeight
        var prev: AUIKaraokeRoomPitchModel? = nil
        for pitchModel in pitchModels {
            if prev != nil && needValidate && pitchModel.beginTime > prev!.beginTime + prev!.duration {
//                NSLog("[KTV_DEBUG_PITCH_TEST] prev_end:%ld, cur_begin:%ld", (long)(prev.beginTime + prev.duration), (long)pitchModel.beginTime);
            }
            let beginTime = pitchModel.beginTime
            let duration = pitchModel.duration
            let pitch = pitchModel.value
            
            let x = msWidth * CGFloat((beginTime - (self.progress - (self.config?.timeElapsedOnScreen ?? 0))))
            let y = self.getPitchRectCenterYWithPitch(pitch: pitch) - CGFloat(self.pitchHeight) * 0.5
            let w = msWidth * CGFloat(duration)
            var h = pitchHeight
            
            if needValidate {
                h /= 2
            }
            
            let pitchRect = CGRect(x: x, y: y, width: w, height: h)
            
            var linePath: UIBezierPath? = nil
            let rounded = true

            if rounded {
                linePath = UIBezierPath(roundedRect: pitchRect, cornerRadius: pitchHeight * 0.5)
            }else {
                linePath = UIBezierPath(rect: pitchRect)
            }
//            print("[KTV_DEBUG_PITCH_DRAW] self bounds:\(String(describing: self.bounds)), pitch rect:\(String(describing: pitchRect))")

            let lineColor = fillColor;
            lineColor?.setFill()
            linePath?.fill()
            
            prev = pitchModel
        }
    }
    
    private func drawPitchIndicator() {
        if let delegate = self.delegate {
            let y = self.getPitchRectCenterYWithPitch(pitch: self.pitch)
            let endPoint = CGPoint(x: self.vlineOffsetX, y: y)
            delegate.updatePitchIndicatorPositionWithEndPoint(endPoint)
        }
    }
}






