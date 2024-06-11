//
//  AUIKaraokeRoomPitchModel.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/4/18.
//

import Foundation
import AUIFoundation

class AUIKaraokeRoomPitchModel: AUIKaraokeRoomModel {
    
    /**
     * 音高线开始时间
     */
    public var beginTime: NSInteger = 0
    
    /**
     * 音高线持续时间
     */
    public var duration: NSInteger = 0
    
    /**
     * 音高值
     */
    public var value: Int = 0
    
    /**
     * 将音高线原始数据转为控件数据模型.
     *
     * @param json 从 从 AliRTCSDK获取的音高线原始数据
     *
     * @return 音高线数据模型数组, 可通过 -[AUIKaraokeRoomPitchView setStandardPitchModels:] 设置音高线 UI 控件的标准音高线.
     */
    public static func analyzePitchData(json: Any?) -> [AUIKaraokeRoomPitchModel]? {
        return self.analyzePitchData(json: json, beginTime: 0, endTime: NSInteger(INT32_MAX), krcFormatOffset: 0)
    }
    
    /**
     * 将音高线原始数据转为控件数据模型.
     * 根据 beginTime 和 endTime 对音高线进行截断
     *
     * @param json 从 AliRTCSDK获取的音高线原始数据.
     * @param beginTime 需要截断音高线数据的开始时间.
     * @param endTime 需要截断音高线数据的结束时间.
     *
     * @return 音高线数据模型数组, 可通过 -[AUIKaraokeRoomPitchView setStandardPitchModels:] 设置音高线 UI 控件的标准音高线.
     */
    public static func analyzePitchData(json: Any?, beginTime: NSInteger, endTime: NSInteger) -> [AUIKaraokeRoomPitchModel]? {
        return self.analyzePitchData(json: json, beginTime: beginTime, endTime: endTime, krcFormatOffset: 0)
    }
    
    /**
     * 将高潮片段资源对应的音高线原始数据转为控件数据模型.
     *
     * @param json 从 AliRTCSDK获取的音高线原始数据.
     * @param segmentBegin 高潮片段开始时间（该字段在请求高潮片段资源时返回）
     * @param segmentEnd 高潮片段结束时间（该字段在请求高潮片段资源时返回）
     * @param preludeDuration 高潮片段前奏时间（该字段在请求高潮片段资源时返回）
     * @param krcFormatOffset krc歌词对歌曲的偏移量（该字段在 krc 歌词模型数据中获取）
     */
    public static func analyzeAccompanimentClipPitchData(json: Any?, segmentBegin: NSInteger, segmentEnd: NSInteger, preludeDuration: NSInteger, krcFormatOffset: NSInteger) -> [AUIKaraokeRoomPitchModel]? {
        let beginTime = segmentBegin + preludeDuration - krcFormatOffset
        let endTime = segmentEnd - krcFormatOffset
        return self.analyzePitchData(json: json, beginTime: beginTime, endTime: endTime)
    }
}

extension AUIKaraokeRoomPitchModel {
    private static func analyzePitchData(json: Any?, beginTime: NSInteger, endTime: NSInteger, krcFormatOffset: NSInteger) -> [AUIKaraokeRoomPitchModel]? {
#if false
        guard let json = json,
              let rsp = self.dictionaryWithJSON(json),
              let dataDict = rsp["data"] as? [String: Any],
              let pitchArray = dataDict["pitch"] as? [[String: Any]] else { return nil }
#else
        guard let json = json,
              let rsp = self.dictionaryWithJSON(json),
              let pitchArray = rsp["midiList"] as? [[String: Any]] else { return nil }
#endif
        var pitchModels = [AUIKaraokeRoomPitchModel]()
        for dict in pitchArray {
            let model = self.pitchModelWithDict(dict)!
            model.beginTime += Int(krcFormatOffset)
            if model.beginTime > endTime {
                continue
            }
            if model.beginTime + model.duration < beginTime {
                continue
            }
            pitchModels.append(model)
        }
        if pitchModels.count > 0 {
            self.trimModelinPlace(pitchModels.first, from: Int(beginTime), to: Int(endTime))
            self.trimModelinPlace(pitchModels.last, from: Int(beginTime), to: Int(endTime))
        }
                
        return pitchModels
    }
    
    private static func trimModelinPlace(_ model: AUIKaraokeRoomPitchModel?, from beginTime: Int, to endTime: Int) {
        guard let model = model else { return }
        
        if model.beginTime < beginTime {
            model.duration -= (beginTime - model.beginTime)
            model.beginTime = beginTime
        }
        
        if (model.beginTime + model.duration > endTime) {
            model.duration = endTime - model.beginTime
        }
    }

    private static func analyzePitchData(_ json: Any?, krcFormatOffset: NSInteger) -> [AUIKaraokeRoomPitchModel]? {
        guard let json = json,
              let rsp = self.dictionaryWithJSON(json),
              let dataDict = rsp["data"] as? [String: Any],
              let pitchArray = dataDict["pitch"] as? [[String: Any]] else { return nil }

        var pitchModels = [AUIKaraokeRoomPitchModel]()
        for dict in pitchArray {
            let model = self.pitchModelWithDict(dict)!
            model.beginTime += Int(krcFormatOffset)
            pitchModels.append(model)
        }
        return pitchModels
    }

    private static func pitchModelWithDict(_ dict: [String: Any]?) -> AUIKaraokeRoomPitchModel? {
        guard let dict = dict,
              !dict.isEmpty else { return nil }
#if false

        let pitch = AUIKaraokeRoomPitchModel()
        pitch.beginTime = dict["begin_time"] as? Int ?? 0
        pitch.duration = dict["duration"] as? Int ?? 0
        pitch.value = dict["value"] as? Int ?? 0
#else
        let start = dict["start"] as? Int ?? 0
        let end = dict["end"] as? Int ?? 0

        let pitch = AUIKaraokeRoomPitchModel()
        pitch.beginTime = Int(start)
        pitch.duration = Int((end - start))
        pitch.value = dict["pitch"] as? Int ?? 0
#endif
        return pitch
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
}

// MARK: - PitchViewStyleConfig

class AUIKaraokeRoomPitchViewConfig: NSObject {
    
    /**
     * 音高等级数
     * 默认配置 20
     * 不建议修改
     */
    public var pitchNum: Int = 20
    
    /**
     * 最大音高值
     * 默认配置 90
     * 不建议修改
     */
    public var maxPitch: Int = 90
    
    /**
     * 最小音高值
     * 默认配置 10
     * 不建议修改
     */
    public var minPitch: Int = 10
    
    /**
     * 控件开始至竖线这一段表示的时间, 单位 ms
     * 默认配置 1175
     * 不建议修改
     */
    public var timeElapsedOnScreen: Int = 1175
    
    /**
     * 竖线至控件末尾这一段表示的时间, 单位 ms
     * 默认配置 2750
     * 不建议修改
     */
    public var timeToPlayOnScreen: Int = 2750
    
    /**
     * 调用 [AUIKaraokeRoomPitchView setCurrentSongProgress: pitch:] 方法的大致时间间隔, 单位 ms
     */
    public var estimatedCallInterval: CGFloat = 60
    
    /**
     *  演唱得分难度
     *  取值范围[0-100]，默认值为 10
     *  值越小难度越低，演唱者越容易得高分
     */
    public var scoreLevel: Int = 10
    
    /**
     *  演唱评分偏移量
     *  取值范围 [-100,100]，默认值为 0
     *  最终得分会在计算时在原有得分基础上加上偏移量
     */
    public var scoreCompensationOffsetLevel: Int = 0
    
    /**
     *  音高线击中判定范围
     *  取值范围 [0-3]，默认值为 1
     *  输入pitch与标准pitch之间差值小于pitchHitRange，则会被判定为演唱准确
     */
    public var pitchHitRange: Int = 1
    
    /**
     * 背景颜色
     */
    public var backgroundColor: UIColor? = nil
    
    /**
     * 五线谱横线颜色
     */
    public var staffColor: UIColor? = nil
    
    /**
     * 竖线颜色
     */
    public var verticalLineColor: UIColor? = nil
    
    /**
     * 标准音调颜色
     */
    public var standardRectColor: UIColor? = nil
    
    /**
     * 击中音调颜色
     */
    public var hitRectColor: UIColor? = nil
    
    /**
     * 音调指示器颜色
     */
    public var pitchIndicatorColor: UIColor? = nil
    
    /**
     * 分数文本颜色
     */
    public var scoreTextColor: UIColor? = nil
    
    /**
     * 默认配置
     */
    public static func defaultConfig() -> Self {
        let config = AUIKaraokeRoomPitchViewConfig();
        
        config.backgroundColor = .clear
        config.staffColor = AVTheme.fill_infrared.withAlphaComponent(0.15)
        config.verticalLineColor = AVTheme.fill_infrared.withAlphaComponent(0.15)
        config.standardRectColor = AVTheme.fill_infrared.withAlphaComponent(0.4)
        config.hitRectColor = UIColor.av_color(withHexString: "#00BCD4")
        config.pitchIndicatorColor = AVTheme.text_strong
        config.scoreTextColor = AVTheme.text_strong
        
        return config as! Self
    }
}
