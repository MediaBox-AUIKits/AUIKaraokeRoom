//
//  AUIKaraokeRoomManager.swift
//  Example
//
//  Created by Bingo on 2024/3/6.
//

import UIKit
import AUIFoundation
import AUIRoomCore

public let KaraokeRoomServerDomain = "你的AppServer域名"

@objcMembers  public class AUIKaraokeRoomManager: NSObject {

    public static let shared = AUIKaraokeRoomManager()
    
    override init() {
        
    }
    
    private var roomServiceInterface: ARTCRoomServiceInterface? = nil
    
    public func getRoomServiceInterface() -> ARTCRoomServiceInterface {
        return self.roomServiceInterface!
    }
    
    public func getRoomAppServer() -> ARTCKaraokeRoomAppServer {
        return (self.roomServiceInterface as? ARTCRoomServiceImpl)!.roomAppServer as! ARTCKaraokeRoomAppServer
    }
    
    public func setup(currentUser: ARTCRoomUser, serverAuth: String) {
        
        ARTCRoomService.currrentUser = currentUser
        ARTCRoomMessageService.logout { error in
            let roomAppServer = ARTCKaraokeRoomAppServer(KaraokeRoomServerDomain)
            roomAppServer.serverAuth = serverAuth
            self.roomServiceInterface = ARTCRoomServiceImpl(roomAppServer)
        }
    }
    
    public func isInRoom() -> Bool {
        return ARTCRoomService.isInRoom
    }
    
    public func createRoom(currVC: UIViewController? = nil, completed: (()->Void)? = nil) {
        
        AVDeviceAuth.checkMicAuth { auth in
            if auth == false {
                return
            }
            
            let topVC = currVC ?? UIViewController.av_top()
            let hud = AVProgressHUD.showAdded(to: topVC.view, animated: true)
            hud.iconType = .loading
            hud.labelText = "创建房间中..."
            ARTCRoomMessageService.login(server: self.getRoomAppServer()) { error in
                if let error = error {
                    hud.hide(animated: false)
                    AVToastView.show("创建房间失败：登录失败（\(error.artcMessage)）", view: topVC.view, position: .mid)
                    return
                }
                
                ARTCKaraokeRoomController.createKaraokeRoom(roomName: "\(ARTCRoomService.currrentUser!.userNick)的K歌房") { roomInfo, error in
                    hud.hide(animated: false)
                    if let error = error {
                        AVToastView.show("创建房间失败：\(error.artcMessage)", view: topVC.view, position: .mid)
                        return
                    }
                    if let roomInfo = roomInfo {
                        let controller = ARTCKaraokeRoomController(ktvRoomInfo: roomInfo)
                        controller.roomService = self.getRoomServiceInterface()
                        let viewController = AUIKaraokeRoomViewController(ktvRoomController: controller)
                        viewController.show(topVC: topVC)
                    }
                    else {
                        AVToastView.show("创建房间失败：未知错误", view: topVC.view, position: .mid)
                    }
                }
            }
        }
        
    }
    
    public func enterRoom(roomId: String, currVC: UIViewController? = nil, completed: (()->Void)? = nil) {
        let topVC = currVC ?? UIViewController.av_top()
        let hud = AVProgressHUD.showAdded(to: topVC.view, animated: true)
        hud.iconType = .loading
        hud.labelText = "进入房间中..."
        ARTCRoomMessageService.login(server: self.getRoomAppServer()) { error in
            if let error = error {
                hud.hide(animated: false)
                AVToastView.show("进入房间失败：登录失败（\(error.artcMessage)）", view: topVC.view, position: .mid)
                return
            }
            
            ARTCKaraokeRoomController.getKaraokeRoomDetail(roomId: roomId) { roomInfo, error in
                hud.hide(animated: false)
                if let error = error {
                    AVToastView.show("进入房间失败：无法获取房间详情（\(error.artcMessage)）", view: topVC.view, position: .mid)
                    return
                }
                if let roomInfo = roomInfo {
                    let controller = ARTCKaraokeRoomController(ktvRoomInfo: roomInfo)
                    controller.roomService = self.getRoomServiceInterface()
                    let viewController = AUIKaraokeRoomViewController(ktvRoomController: controller)
                    viewController.show(topVC: topVC)
                }
                else {
                    AVToastView.show("进入房间失败：未知错误", view: topVC.view, position: .mid)
                }
            }
        }
    }
}
