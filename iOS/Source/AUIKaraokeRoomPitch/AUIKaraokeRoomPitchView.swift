//
//  AUIKaraokeRoomPitchView.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/4/22.
//

import Foundation
import SnapKit

class AUIKaraokeRoomPitchCountDownView: UIView {
    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = .clear
        self.setup()
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setup() {
        self.addSubview(self.firstIndicator)
        self.firstIndicator.snp.makeConstraints { make in
            make.width.height.equalTo(4)
            make.left.equalTo(14)
            make.centerY.equalToSuperview()
        }
        self.firstIndicator.layer.cornerRadius = 2
        
        self.addSubview(self.secondIndicator)
        self.secondIndicator.snp.makeConstraints { make in
            make.width.height.equalTo(4)
            make.left.equalTo(firstIndicator.snp.right).offset(8)
            make.centerY.equalToSuperview()
        }
        self.secondIndicator.layer.cornerRadius = 2
        
        self.addSubview(self.thirdIndicator)
        self.thirdIndicator.snp.makeConstraints { make in
            make.width.height.equalTo(4)
            make.left.equalTo(secondIndicator.snp.right).offset(8)
            make.centerY.equalToSuperview()
        }
        self.thirdIndicator.layer.cornerRadius = 2
    }
    
    
    private lazy var firstIndicator: UIView = {
        let view = UIView(frame: CGRect.zero)
        view.backgroundColor = .white
        view.clipsToBounds = true
        return view
    }()
    
    private lazy var secondIndicator: UIView = {
        let view = UIView(frame: CGRect.zero)
        view.backgroundColor = .white
        view.clipsToBounds = true
        return view
    }()
    
    private lazy var thirdIndicator: UIView = {
        let view = UIView(frame: CGRect.zero)
        view.backgroundColor = .white
        view.clipsToBounds = true
        return view
    }()
    
    public func updateDisplay(progress: Int, duration: Int) {
        guard duration > 0 else { return }
        self.firstIndicator.isHidden = false
        self.secondIndicator.isHidden = false
        self.thirdIndicator.isHidden = false
        if progress >= duration {
            self.firstIndicator.isHidden = true
            self.secondIndicator.isHidden = true
            self.thirdIndicator.isHidden = true
            return
        }
        let passed = Float(progress) / Float(duration)
        if passed > 2.0 / 3 {
            self.secondIndicator.isHidden = true
            self.thirdIndicator.isHidden = true
        } else if passed > 1.0 / 3 {
            self.thirdIndicator.isHidden = true
        }
    }
}

class AUIKaraokeRoomPitchView: UIView {
    
    public func setConfig(_ config: AUIKaraokeRoomPitchViewConfig?) {
        let tmpConfig = config ?? AUIKaraokeRoomPitchViewConfig.defaultConfig()
        self.config = tmpConfig
        self.canvas?.setConfig(self.config)
    }
    
    public func setStandardPitchModels(_ standardPitchModels: [AUIKaraokeRoomPitchModel]?) {
        self.reset()
        self.stdPitchModels = standardPitchModels
    }
    
    public func getPitchStartTime() -> NSInteger {
        guard let stdPitchModels = self.stdPitchModels,
              stdPitchModels.count > 0 else {
            return 0
        }
        
        let firstPitchModel = stdPitchModels.first!
        return firstPitchModel.beginTime
    }
    
    public func setCurrentSongProgress(_ progress: NSInteger, _ pitch: Int) {
        self.setAccompanimentClipCurrentSongProgress(progress, pitch, 0, 0)
        if (progress > 0) {
            self.pitchCountDownView.updateDisplay(progress: progress, duration: self.getPitchStartTime())
            self.pitchCountDownView.isHidden = false
        }
    }
    
    public func setAccompanimentClipCurrentSongProgress(_ progress: NSInteger, _ pitch: Int, _ segBeginTime: NSInteger, _ krcFormatOffset: NSInteger) {
        let adjustProgress = progress + segBeginTime - krcFormatOffset
        self.curProgress = progress
        let beginTime = self.beginTimeOnViewWithProgress(adjustProgress)
        let endTime = self.endTimeOnViewWithProgress(adjustProgress)

        let adjustPitch = self.validatePitch(pitch)
        self.curSingPitch = adjustPitch

        let stdPitchModelsToDraw: [AUIKaraokeRoomPitchModel]? = self.filterPitchModels(self.stdPitchModels, beginTime, endTime)
        self.updateCurrentSingPitchAndHitPitchModelsIfNeededWithProgress(progress: progress, pitch: adjustPitch, stdPitchModelsOnView: stdPitchModelsToDraw)
        let hitPitchModelsToDraw: [AUIKaraokeRoomPitchModel]? = self.filterPitchModels(self.hitPitchModels, beginTime, progress)
        self.canvas?.drawWithProgress(progress, stdPitchModelsToDraw, hitPitchModelsToDraw, self.curSingPitch)
    }
    
    public func addScore(_ score: Int) {
        if self.isProgressBeforeFirstPitchModel() {
            return
        }
        let scoreLabel = self.createAScoreLabel()!
        if (self.scoresPerLine?.count ?? 0) > 0 {
            scoreLabel.text = String(format: "+%d", score)
        } else {
            scoreLabel.text = String(format: "%d", score)
        }
        self.animateScoreLabel(scoreLabel)
    }
    
    public func calculateScore(inputPitch: Int, stdPitch: Int) {
        let inputTone = inputPitch // pitchToTone(inputPitch)
        let stdTone = stdPitch // pitchToTone(stdPitch)
        
        var match = 1.0 - (Double(self.config?.scoreLevel ?? 0) / 100.0) * fabs(Double(inputTone - stdTone)) + Double(self.config?.scoreCompensationOffsetLevel ?? 0) / 100.0
        match = max(0.0, match)
        match = min(1.0, match)
        let curScore = Int(round(match * 100.0))
        self.curLineScores?.append(curScore)
    }
    
    public func updateLineScore() -> Int {
        guard let curLineScores = self.curLineScores,
              !curLineScores.isEmpty else { return 0 }
        var sum: Int = 0
        for score in curLineScores {
            sum += score
        }
        let average = Double(sum) / Double(curLineScores.count)
        let lineScore = Int(round(average))
        self.curLineScores?.removeAll()
        self.scoresPerLine?.append(lineScore)
        return lineScore
    }
    
    public func reset() {
        self.stdPitchModels = nil
        self.hitPitchModels = nil
        self.curProgress = 0
        self.hitInPeriod = false
        self.curLineScores?.removeAll()
        self.scoresPerLine?.removeAll()
        self.canvas?.clearAll()
        self.pitchCountDownView.updateDisplay(progress: 0, duration: 1)
        self.pitchCountDownView.isHidden = true
    }
    
    /// UI
    private var canvas: AUIKaraokeRoomPitchCanvas? = nil
    private var pitchIndicatorLayer: CAShapeLayer? = nil
    private var pitchIndicatorOriginPosition: CGPoint = CGPoint.zero
    private var animationLabels: [UILabel]? = nil
    private lazy var scoreLabelFont: UIFont = {
        let font = UIFont.systemFont(ofSize: 12, weight: UIFont.Weight.semibold)
        return font
    }()
    private lazy var pitchCountDownView: AUIKaraokeRoomPitchCountDownView = {
        let view = AUIKaraokeRoomPitchCountDownView()
        return view
    }()
    
    /// Data
    private var config: AUIKaraokeRoomPitchViewConfig? = nil
    private var stdPitchModels: [AUIKaraokeRoomPitchModel]? = nil
    private var _hitPitchModels: [AUIKaraokeRoomPitchModel]? = nil
    private var hitPitchModels: [AUIKaraokeRoomPitchModel]? {
        get {
            if _hitPitchModels == nil {
                _hitPitchModels = []
            }
            return _hitPitchModels
        }
        set {
            _hitPitchModels = newValue
        }
    }
    private var curProgress: NSInteger = 0
    private var curSingPitch: Int = 0
    private var hitInPeriod: Bool = false

    /// Score Algorithm
    private var curLineScores: [Int]? = nil
    private var scoresPerLine: [Int]? = nil

    /// Animation
    private lazy var anim1: CAAnimation = {
        let anim = self.appearAnimationWithDuration(duration: 0.2)
        return anim
    }()
    
    private lazy var anim2: CAAnimation = {
        let anim = self.stayAnimationWithOpacity(opacity: 1, duration: 0.4)
        return anim
    }()
    
    private lazy var anim4: CAAnimation = {
        let anim = self.stayAnimationWithOpacity(opacity: 1, duration: 0.01)
        return anim
    }()
    
    private lazy var anim5: CAAnimation = {
        let anim = self.disappearAnimationWithDuration(duration: 0.3)
        return anim
    }()
    
    private lazy var riseAnim: CAKeyframeAnimation = {
        let animation = CAKeyframeAnimation.init(keyPath: "position.y")
        let function = CAMediaTimingFunction.init(name: CAMediaTimingFunctionName.linear)
        animation.timingFunction = function
        animation.fillMode = .forwards
        return animation
    }()
    
    private func pitchToTone(_ pitch: Int) -> Double {
        let eps: Double = 1e-6
        return max(0, log(Double(pitch) / 55 + eps) / log(2)) * 12
    }
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.setup()
    }
    
    public required init?(coder: NSCoder) {
        super.init(coder: coder)
        self.setup()
    }
    
    private func animateScoreLabel(_ label: UILabel?) {
        guard let label = label else { return }
        let triangleOrigin = self.pitchIndicatorOriginPosition
        let x = triangleOrigin.x
        let y = min(triangleOrigin.y, self.bounds.height)
        
        let labelSize = self.calculateSizeForScoreLabel(label: label)
        let labelRect = CGRect(x: x, y: y - labelSize.height / 2, width: labelSize.width, height: labelSize.height)
        label.frame = labelRect
        
        let anim = self.groupedAnimationMoveOnAxisYFrom(y, toY: 10)
        label.layer.add(anim, forKey: nil)
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            self.animationLabels?.append(label)
            label.removeFromSuperview()
        }
    }
    
    private func setup() {
        _hitPitchModels = []
        let canvas = AUIKaraokeRoomPitchCanvas()
        canvas.delegate = self
        self.addSubview(canvas)
        canvas.snp.makeConstraints { make in
            make.top.equalTo(4)
            make.left.width.equalToSuperview()
            make.bottom.equalToSuperview().offset(-10)
        }
        self.canvas = canvas
        
        self.addSubview(self.pitchCountDownView)
        self.pitchCountDownView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalTo(self.canvas!.snp.bottom)
            make.bottom.equalToSuperview()
        }
        
        self.animationLabels = [UILabel]()
        self.curLineScores = [Int]()
        self.scoresPerLine = [Int]()
    }
    
    private func validatePitch(_ pitch: Int) -> Int {
        var curPitch = pitch
        if pitch < 0 {
            curPitch = 0
        } else if (pitch > 5 && pitch < (self.config?.minPitch ?? 0)) {
            curPitch = self.config?.minPitch ?? 0
        } else if (pitch > (self.config?.maxPitch ?? 0)) {
            curPitch = self.config?.maxPitch ?? 0
        }
        return curPitch
    }
    
    private func filterPitchModels(_ pitchModels: [AUIKaraokeRoomPitchModel]?, _ beginTime: NSInteger, _ endTime: NSInteger) -> [AUIKaraokeRoomPitchModel]? {
        guard let pitchModels = pitchModels,
              pitchModels.count > 0,
              beginTime < endTime else {
            return nil
        }
        
        var ret: [AUIKaraokeRoomPitchModel] = []
        for model in pitchModels {
            let begin = model.beginTime
            let end = begin + model.duration
            if begin >= endTime {
                continue
            }
            if end <= beginTime {
                continue
            }
            ret.append(model)
        }
        return ret
    }
    
    private func getOffsetScaleWithPitch(pitch: Int, stdPitchVal: Int) -> Int{
        var offsetScale = -1
        if pitch >= stdPitchVal - (self.config?.pitchHitRange ?? 0) &&
            pitch <= stdPitchVal + (self.config?.pitchHitRange ?? 0) {
            offsetScale = 0
        } else {
            offsetScale = pitch > stdPitchVal ? pitch - stdPitchVal : stdPitchVal - pitch
        }
        return offsetScale
    }
    
    private func updateCurrentSingPitchAndHitPitchModelsIfNeededWithProgress(progress: NSInteger, pitch: Int, stdPitchModelsOnView: [AUIKaraokeRoomPitchModel]?) {
        if pitch < 0 || pitch > 100 {
            return
        }
        
        var hit: Bool = false
        if let stdPitchModelsOnView = stdPitchModelsOnView {
            for stdPitch in stdPitchModelsOnView {
                let contain = !((progress > (stdPitch.beginTime + stdPitch.duration + 60)) || progress < stdPitch.beginTime)
                if contain {
                    var offsetScale = self.getOffsetScaleWithPitch(pitch: pitch, stdPitchVal: stdPitch.value)
                    if self.test_hitAll() {
                        offsetScale = 0
                    }
                    if offsetScale == -1 {
                        self.curSingPitch = 0
                    } else if (offsetScale == 0) {
                        hit = true
                        self.curSingPitch = stdPitch.value
                        self.updateHitPitchModelsAtProgress(progress: progress, pitch: self.curSingPitch, matchedStdPitchModel: stdPitch)
//                        print("[KTV_DEBUG_PITCH]progress:\(progress), real pitch:\(self.curSingPitch), hit count:\(String(describing: self.hitPitchModels?.count))")
                    } else {
                        if pitch != 0 {
                            print("stop here")
                        }
                        var curPitch = pitch
                        if curPitch > self.config?.maxPitch ?? 0 {
                            curPitch = self.config?.maxPitch ?? 0
                        } else if (curPitch < self.config?.minPitch ?? 0) {
                            curPitch = self.config?.minPitch ?? 0
                        }
                        self.curSingPitch = curPitch
                    }
                    self.calculateScore(inputPitch: self.curSingPitch, stdPitch: stdPitch.value)
                    continue
                }
            }
            self.hitInPeriod = hit
        }
    }
    
    private func updateHitPitchModelsAtProgress(progress: NSInteger, pitch: Int, matchedStdPitchModel stdPitchModel: AUIKaraokeRoomPitchModel) {
        self.removeObsoleteHitPitchModelsAtProgress(progress: progress)
        let stdBegin = stdPitchModel.beginTime
        let stdDuration = stdPitchModel.duration
        let stdEnd = stdBegin + stdDuration
        
        let postTolerance = 100
        
        let prev = self.hitPitchModels?.last
        if prev == nil
            || prev!.value != pitch
            || prev!.beginTime + prev!.duration + Int(self.config?.estimatedCallInterval ?? 0) < progress {
            if  stdEnd - progress < postTolerance {
                return
            }
            
            let model = AUIKaraokeRoomPitchModel()
            model.beginTime = max(progress - NSInteger(self.config?.estimatedCallInterval ?? 0), stdBegin)
            let endTime = min(stdEnd, progress)
            model.duration = endTime - model.beginTime
            model.value = pitch
            self.hitPitchModels?.append(model)
            return
        }
        let endTime = min(stdEnd, progress)
        prev?.duration = endTime - (prev?.beginTime ?? 0);
    }
    
    private func removeObsoleteHitPitchModelsAtProgress(progress: NSInteger) {
        
        guard let hitPitchModels = self.hitPitchModels else { return }
        // Find the index where to stop
        var lengthToRemove: Int?
        for (index, obj) in hitPitchModels.enumerated().reversed() {
            if obj.beginTime + obj.duration < self.beginTimeOnViewWithProgress(progress) {
                lengthToRemove = index
                break
            }
        }
        
        // If a valid stop index was found, remove elements up to that index
        if let lengthToRemove = lengthToRemove {
            self.hitPitchModels?.removeSubrange(0..<lengthToRemove)
        }
    }
    
    private func beginTimeOnViewWithProgress(_ progress: NSInteger) -> NSInteger {
      return progress - (self.config?.timeElapsedOnScreen ?? 0)
    }
    
    private func endTimeOnViewWithProgress(_ progress: NSInteger) -> NSInteger {
        return progress + (self.config?.timeToPlayOnScreen ?? 0)
    }
    
    private func createAScoreLabel() -> UILabel! {
        var scoreLabel = self.animationLabels?.last
        if scoreLabel != nil {
            self.animationLabels?.removeLast()
        } else {
            scoreLabel = UILabel()
        }
        scoreLabel!.textColor = self.config?.scoreTextColor
        scoreLabel!.font = self.scoreLabelFont
        scoreLabel!.alpha = 0
        self.addSubview(scoreLabel!)
        
        return scoreLabel
    }
    
    private func calculateSizeForScoreLabel(label: UILabel) -> CGSize {
        let labelRect = label.text?.boundingRect(with: CGSize(width: 100, height: 100), options: NSStringDrawingOptions.usesLineFragmentOrigin , attributes: [NSAttributedString.Key.font : self.scoreLabelFont], context: nil)
        
        let labelW = ceil(labelRect?.size.width ?? 0)
        let labelH = ceil(labelRect?.size.height ?? 0)
        
        return CGSize(width: labelW, height: labelH)
    }
    
    private func isProgressBeforeFirstPitchModel() -> Bool {
        let beginTime = self.getPitchStartTime()
        return self.curProgress < beginTime
    }
    
    private func groupedAnimationMoveOnAxisYFrom(_ fromY: CGFloat, toY: CGFloat) -> CAAnimationGroup {
        let d1 = 0.2
        let d2 = 0.4
        let d3 = 0.4
        let d4 = 0.01
        let d5 = 0.3

        let dTotal = d1 + d2 + d3 + d4 + d5

        let group = CAAnimationGroup()

        let anim1 = self.anim1
        let anim2 = self.anim2
        let anim3 = self.riseAnimationFromY(fromY: fromY, toY: toY, duration: 0.4)
        let anim4 = self.anim4
        let anim5 = self.anim5

        group.animations = [anim1, anim2, anim3, anim4, anim5]
        group.duration = dTotal
        anim1.beginTime = 0
        anim2.beginTime = anim1.duration
        anim3.beginTime = anim2.beginTime + anim2.duration
        anim4.beginTime = anim3.beginTime + anim3.duration
        anim5.beginTime = anim4.beginTime + anim4.duration

        group.repeatCount = 1

        return group
    }

    
    private func appearAnimationWithDuration(duration: CFTimeInterval) -> CAKeyframeAnimation {
        let timingFunc = CAMediaTimingFunction.init(controlPoints:0.25, 0.1, 0.25, 1)
        let animation = CAKeyframeAnimation.init(keyPath: "opacity")
      
        animation.values = [0.0, 1.0]
        animation.duration = duration
        animation.timingFunction = timingFunc
        animation.fillMode = .forwards
        return animation
    }
    
    private func stayAnimationWithOpacity(opacity: CGFloat, duration: CFTimeInterval) -> CABasicAnimation {
        let animation = CABasicAnimation.init(keyPath: "opacity")
        animation.fromValue = opacity;
        animation.toValue = opacity;
        animation.duration = duration;
        return animation;
    }
    
    private func riseAnimationFromY(fromY: CGFloat, toY: CGFloat, duration: CGFloat) -> CAKeyframeAnimation {
        self.riseAnim.values = [fromY, toY]
        self.riseAnim.duration = duration
        return self.riseAnim
    }
    
    private func disappearAnimationWithDuration(duration: CFTimeInterval) -> CAKeyframeAnimation {
        let timingFunc = CAMediaTimingFunction.init(controlPoints: 0.25, 0.1, 0.25, 1)
        let animation = CAKeyframeAnimation.init(keyPath: "opacity")
        animation.values = [1.0, 0.0]
        animation.duration = duration
        animation.timingFunction = timingFunc
        return animation
    }
    
    private func test_hitAll() -> Bool {
        return false
    }
}

extension AUIKaraokeRoomPitchView: AUIKaraokeRoomPitchCanvasProtocol {
    func updatePitchIndicatorPositionWithEndPoint(_ endPoint: CGPoint) {
        let originY = (self.canvas?.frame ?? CGRect.zero).maxY
        let translatedY = endPoint.y + (self.canvas?.frame ?? CGRect.zero).minY
        let translatedEndPoint = CGPoint(x: endPoint.x, y: translatedY)
        
        let w = 6.0
        let h = 7.0
        
        if (self.pitchIndicatorLayer == nil) {
            let layer = CAShapeLayer()
            let triPath = UIBezierPath()
            
            let point1 = translatedEndPoint
            let point2 = CGPoint(x: translatedEndPoint.x - w, y: translatedEndPoint.y - h * 0.5)
            let point3 = CGPoint(x: translatedEndPoint.x - w, y: translatedEndPoint.y + h * 0.5)

            triPath.move(to: point1)
            triPath.addLine(to: point2)
            triPath.addLine(to: point3)
            triPath.close()
          
            layer.fillColor = self.config?.pitchIndicatorColor?.cgColor ?? UIColor.clear.cgColor
            layer.path = triPath.cgPath
            self.layer.addSublayer(layer)

            self.pitchIndicatorLayer = layer
        } else {
            CATransaction.begin()
            CATransaction.setDisableActions(self.hitInPeriod)
            CATransaction.setAnimationDuration(1)
            
            let ty = translatedY - originY
            let translation = CGAffineTransform(translationX: 0, y: ty)
            self.pitchIndicatorLayer?.setAffineTransform(translation)

            CATransaction.commit()
        }
        //记录当前三角形原点位置
//        self.pitchIndicatorOriginPosition = CGPoint(x: translatedEndPoint.x - w, y: translatedY - h * 0.5)
        self.pitchIndicatorOriginPosition = translatedEndPoint;
    }
}


