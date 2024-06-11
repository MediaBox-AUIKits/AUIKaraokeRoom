//
//  AUIKaraokeRoomViewModel.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/5/6.
//

import Foundation

class AUIKaraokeRoomViewModel: NSObject {
    public var dataDidUpdateBlock: (() -> Void)? = nil
    public var model: Any? = nil
    
    public override init() {
        model = NSObject()
        super.init()
        self.initViewModel()
    }
    
    public init(model: NSObject?) {
        self.model = model ?? NSObject()
        super.init()
        self.initViewModel()
    }
    
    public func initViewModel() {
        
    }
    
    public static func transToMinSec(_ timeInSec: Int) -> String {
        var minutes = 0
        var seconds = 0
        var minutesStr = ""
        var secondsStr = ""
                
        minutes = min(timeInSec / 1000 / 60, 99)
        minutesStr = minutes > 9 ? "\(minutes)" : "0\(minutes)"
        
        seconds = timeInSec / 1000 % 60
        secondsStr = seconds > 9 ? "\(seconds)" : "0\(seconds)"
        
        return "\(minutesStr):\(secondsStr)"
    }
}
