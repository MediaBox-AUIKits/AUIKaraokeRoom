//
//  AUIKaraokeRoomChartInfo.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/5/9.
//

import Foundation
import AUIRoomCore

@objcMembers open class AUIKaraokeRoomChartInfo: ARTCKaraokeRoomMusicChartInfo {
    
    public var isSelected: Bool = false
    public var itemSizeWidth: CGFloat = 0
    
    public static func chartInfoWithServerChartInfo(_ serverChartInfo: ARTCKaraokeRoomMusicChartInfo?) -> AUIKaraokeRoomChartInfo? {
        guard let serverChartInfo = serverChartInfo else { return nil }
        let chart = AUIKaraokeRoomChartInfo(chartId: serverChartInfo.chartId)
        chart.chartName = serverChartInfo.chartName
        return chart
    }
    
    public static func chartInfosWithServerChartInfos(_ serverChartInfos: [ARTCKaraokeRoomMusicChartInfo]?) -> [AUIKaraokeRoomChartInfo] {
        var chartInfos = [AUIKaraokeRoomChartInfo]()
        guard let serverChartInfos = serverChartInfos else { return chartInfos }
        for serverChartInfo in serverChartInfos {
            let model = AUIKaraokeRoomChartInfo.chartInfoWithServerChartInfo(serverChartInfo)!
            chartInfos.append(model)
        }
        return chartInfos
    }
}
