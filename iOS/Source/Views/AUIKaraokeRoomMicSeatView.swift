//
//  AUIKaraokeRoomSingingView.swift
//  Example
//
//  Created by Bingo on 2024/2/26.
//

import UIKit
import AUIFoundation
import SnapKit
import AUIRoomCore
import AUIVoiceRoom

private var singerRoleButtonKey: UInt8 = 0 // 用作关联对象的key
extension AUIVoiceRoomMicSeatCell {
    
    var singerRoleButton: AVBlockButton? {
        get {
            return objc_getAssociatedObject(self, &singerRoleButtonKey) as? AVBlockButton
        }
        set {
            objc_setAssociatedObject(self, &singerRoleButtonKey, newValue, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)
        }
    }
    
    public func setupSingerRoleButton() {
        let btn = AVBlockButton()
        btn.setBackgroundImage(AUIKaraokeRoomBundle.getCommonImage("ic_main_singer"), for: .normal)
        btn.setBackgroundImage(AUIKaraokeRoomBundle.getCommonImage("ic_join_singer"), for: .selected)
        btn.setTitle("主唱", for: .normal)
        btn.setTitle("伴唱", for: .selected)
        btn.titleLabel?.font = AVTheme.mediumFont(7)
        btn.layer.cornerRadius = 6
        btn.layer.masksToBounds = true
        self.addSubview(btn)
        btn.snp.makeConstraints { make in
            make.width.equalTo(22)
            make.height.equalTo(12)
            make.centerX.equalTo(self.avatarView)
            make.top.equalTo(self.nameLabel.snp.bottom)
        }
        self.singerRoleButton = btn
    }
}


extension AUIVoiceRoomMicSeatView {
    public func updateSingerRole(uid: String, singerRole: ARTCKaraokeRoomSingerRole) {
        if let seatCell = self.findMicSeatCell(uid: uid) {
            if singerRole == .Audience {
                seatCell.singerRoleButton?.isHidden = true
            }
            else {
                if seatCell.singerRoleButton == nil {
                    seatCell.setupSingerRoleButton()
                }
                seatCell.singerRoleButton?.isHidden = false
                seatCell.singerRoleButton?.isSelected = singerRole == .JoinSinger
            }
        }
    }
    
    public func resetSingerRole() {
        self.anchorSeatView.singerRoleButton?.isHidden = true
        for seatCell in self.seatViewList {
            seatCell.singerRoleButton?.isHidden = true
        }
    }
}
