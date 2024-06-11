//
//  AUIKaraokeRoomController.swift
//  AUIKaraokeRoom
//
//  Created by Bingo on 2024/3/4.
//

import UIKit
import AUIRoomCore

// 房间管理
extension ARTCKaraokeRoomController {
    
    // 创建房间
    public static func createKaraokeRoom(roomName: String, completed: @escaping (_ roomInfo: ARTCKaraokeRoomInfo?, _ error: NSError?)->Void) {
        
        guard let currentUser = ARTCRoomService.currrentUser else {
            completed(nil, ARTCRoomError.createError(.Common, "请先设置登录用户"))
            return
        }
        
        AUIKaraokeRoomManager.shared.getRoomServiceInterface().createRoom(roomId: nil, roomName: roomName, user: currentUser) { roomData, error in
            let roomInfo = ARTCKaraokeRoomInfo(data: roomData)
            completed(roomInfo, error)
        }
        
    }
    
    // 获取房间列表
    public static func getKaraokeRoomList(pageNum: Int, pageSize: Int, completed: @escaping (_ roomInfoList: [ARTCKaraokeRoomInfo], _ error: NSError?)->Void) {
        
        AUIKaraokeRoomManager.shared.getRoomServiceInterface().getRoomList!(user: ARTCRoomService.currrentUser ?? ARTCRoomUser(""), pageNum: pageNum, pageSize: pageSize) { roomDataList, error in
            var array = [ARTCKaraokeRoomInfo]()
            roomDataList?.forEach({ roomData in
                let roomInfo = ARTCKaraokeRoomInfo(data: roomData)
                if let roomInfo = roomInfo {
                    array.append(roomInfo)
                }
            })
            completed(array, error)
        }
    }
    
    // 获取房间详情
    public static func getKaraokeRoomDetail(roomId: String, completed: @escaping (_ roomInfo: ARTCKaraokeRoomInfo?, _ error: NSError?)->Void) {
        
        AUIKaraokeRoomManager.shared.getRoomServiceInterface().getRoomDetail!(roomId: roomId, user: ARTCRoomService.currrentUser ?? ARTCRoomUser("")) { roomData, error in
            let roomInfo = ARTCKaraokeRoomInfo(data: roomData)
            completed(roomInfo, error)
        }
        
    }
}
