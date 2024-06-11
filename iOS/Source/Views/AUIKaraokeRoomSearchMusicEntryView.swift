//
//  AUIKaraokeRoomSearchMusicEntryView.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/4/11.
//

import UIKit
import SnapKit
import AUIFoundation

open class AUIKaraokeRoomSearchMusicEntryView: UIView {
    
    public lazy var contentView: UIView = {
        let view = UIView()
        view.backgroundColor = AVTheme.fill_weak
        view.layer.cornerRadius = 15
        view.layer.masksToBounds = true
        view.addGestureRecognizer(self.tapGesture)
        return view
    }()
    
    public lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_ultraweak
        label.font = AVTheme.regularFont(12)
        label.textAlignment = .left
        return label
    }()
    
    lazy var tapGesture: UITapGestureRecognizer = {
        let ges = UITapGestureRecognizer(target: self, action: #selector(onTap(recognizer:)))
        return ges
    }()
    
    open var clickBlock: ((_ sender: AUIKaraokeRoomSearchMusicEntryView)->Void)? = nil
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.addSubview(self.contentView)
        self.contentView.addSubview(self.titleLabel)
        
        self.contentView.snp.makeConstraints { make in
            make.top.equalTo(8)
            make.bottom.equalTo(-8)
            make.left.equalTo(20)
            make.right.equalTo(-20)
        }
        self.titleLabel.snp.makeConstraints { make in
            make.top.equalTo(6)
            make.bottom.equalTo(-6)
            make.left.equalTo(16)
            make.right.equalTo(-16)
        }
                
        self.backgroundColor = .clear
        self.titleLabel.text = AUIKaraokeRoomBundle.getString("搜索歌曲名称或歌手名")
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func onTap(recognizer: UIGestureRecognizer) {
        self.clickBlock?(self)
    }
}
