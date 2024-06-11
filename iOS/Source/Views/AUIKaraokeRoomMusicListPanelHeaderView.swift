//
//  AUIKaraokeRoomMusicListPanelHeaderView.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/3/29.
//

import UIKit
import SnapKit
import AUIFoundation

open class AUIKaraokeRoomMusicListPanelHeaderViewSubBtn: UIView {
    
    public enum Alignment {
        case left
        case right
    }
    
    public var alignment: Alignment = .left
    private var _isSelected: Bool = false
    public var isSelected: Bool {
        get {
            return self._isSelected
        }
        set {
            self._isSelected = newValue
            self.layoutSubviews()
        }
    }
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.addSubview(self.titleLabel)
        self.addSubview(self.selectedFlag)
        
        self.addGestureRecognizer(self.tapGesture)
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public lazy var selectedFlag: UIView = {
        let view = UIView()
        view.backgroundColor = AVTheme.text_strong
        return view
    }()
    
    public lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_strong
        label.font = AVTheme.mediumFont(14)
        label.textAlignment = .center
        return label
    }()
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        self.titleLabel.sizeToFit()
        if self.alignment == .left {
            self.titleLabel.snp.makeConstraints { make in
                make.left.equalTo(21)
                make.centerY.equalToSuperview()
            }
        }
        else {
            self.titleLabel.snp.makeConstraints { make in
                make.right.equalToSuperview().offset(-40)
                make.centerY.equalToSuperview()
            }
        }
        self.selectedFlag.snp.makeConstraints { make in
            make.width.equalTo(40)
            make.height.equalTo(2)
            make.centerX.equalTo(self.titleLabel)
            make.bottom.equalToSuperview()
        }
        self.selectedFlag.isHidden = !self._isSelected
    }

    lazy var tapGesture: UITapGestureRecognizer = {
        let ges = UITapGestureRecognizer(target: self, action: #selector(onTap(recognizer:)))
        return ges
    }()
    
    @objc func onTap(recognizer: UIGestureRecognizer) {
        self.clickBlock?(self)
    }
    
    public var clickBlock: ((_ sender: AUIKaraokeRoomMusicListPanelHeaderViewSubBtn)->Void)? = nil
}

open class AUIKaraokeRoomMusicListPanelHeaderView: UIView {
    
    public var addMusicSelected: Bool? = true
    
    public lazy var addMusicBtn: AUIKaraokeRoomMusicListPanelHeaderViewSubBtn = {
        let view = AUIKaraokeRoomMusicListPanelHeaderViewSubBtn()
        view.alignment = .right
        view.titleLabel.text = AUIKaraokeRoomBundle.getString("点歌")
        view.isSelected = true
        view.backgroundColor = .clear
        return view
    }()
    
    public lazy var addedMusicBtn: AUIKaraokeRoomMusicListPanelHeaderViewSubBtn = {
        let view = AUIKaraokeRoomMusicListPanelHeaderViewSubBtn()
        view.alignment = .left
        view.isSelected = false
        view.backgroundColor = .clear
        return view
    }()
    
    public var addedMusicCount: Int = 0 {
        didSet {
            self.addedMusicBtn.titleLabel.text = String(format: "%@ (%d)", AUIKaraokeRoomBundle.getString("已点")!, self.addedMusicCount)
        }
    }
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.addSubview(self.addMusicBtn)
        self.addSubview(self.addedMusicBtn)
        
        self.addMusicBtn.snp.makeConstraints { make in
            make.width.equalToSuperview().multipliedBy(0.5)
            make.left.equalToSuperview()
            make.top.bottom.equalToSuperview()
        }
        self.addedMusicBtn.snp.makeConstraints { make in
            make.width.equalToSuperview().multipliedBy(0.5)
            make.left.equalTo(self.addMusicBtn.snp.right)
            make.top.bottom.equalToSuperview()
        }
        
        self.addMusicBtn.clickBlock = {[weak self] sender in
            if !self!.addMusicBtn.isSelected {
                self!.addMusicBtn.isSelected = true
                self!.addedMusicBtn.isSelected = false
                self!.addMusicSelected = true
                self!.clickBlock?(self!)
            }
        }
        
        self.addedMusicBtn.clickBlock = {[weak self] sender in
            if !self!.addedMusicBtn.isSelected {
                self!.addedMusicBtn.isSelected = true
                self!.addMusicBtn.isSelected = false
                self!.addMusicSelected = false
                self!.clickBlock?(self!)
            }
        }
        
        self.backgroundColor = .clear
        self.addedMusicCount = 0
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public var clickBlock: ((_ sender: AUIKaraokeRoomMusicListPanelHeaderView)->Void)? = nil
}

