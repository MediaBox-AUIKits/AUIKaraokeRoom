//
//  AUIKaraokeRoomListViewController.swift
//  Example
//
//  Created by Bingo on 2024/2/20.
//

import UIKit
import AUIFoundation
import AUIVoiceRoom
import AUIRoomCore

@objcMembers open class AUIKaraokeRoomListViewController: AUIVoiceRoomListViewController {
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        self.titleView.text = AUIKaraokeRoomBundle.getString("在线K歌房")
        self.creatBtn.setTitle(AUIKaraokeRoomBundle.getString("创建K歌房"), for: .normal)
        self.creatBtn.clickBlock = {[weak self] sender in
            AUIKaraokeRoomManager.shared.createRoom(currVC: self)
        }
    }
    
    override public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let roomInfo = self.roomList[indexPath.row]
        AUIKaraokeRoomManager.shared.enterRoom(roomId: roomInfo.roomId, currVC: self)
    }
    
    override public func getRoomList(pageNum: Int, pageSize: Int, completed: @escaping ([ARTCVoiceRoomInfo], NSError?) -> Void) {
        ARTCKaraokeRoomController.getKaraokeRoomList(pageNum: pageNum, pageSize: pageSize, completed: completed)
    }
}

