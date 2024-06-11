//
//  AUIKaraokeRoomMusicListEntryView.swift
//  AUIKaraokeRoom
//
//  Created by alibaba-inc on 2024/3/27.
//

import UIKit
import SnapKit
import AUIFoundation

@objcMembers open class AUIKaraokeRoomMusicListEntryView: UIView {
    
    public enum MusicListEntryViewStyle {
        case small
        case big
    }
    
    private var _viewStyle: MusicListEntryViewStyle = .big
    public var viewStyle: MusicListEntryViewStyle {
        get {
            return self._viewStyle
        }
        set {
            self._viewStyle = newValue
            self.updateSubViewsLayout()
        }
    }

    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.addSubview(self.iconView)
        self.addSubview(self.titleLabel)
        
        self.addGestureRecognizer(self.tapGesture)
        
        self.backgroundColor = AVTheme.colourful_fill_strong
        self.titleLabel.text = AUIKaraokeRoomBundle.getString("点歌")
        
        self.updateSubViewsLayout()
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public lazy var iconView: UIImageView = {
        let view = UIImageView()
        view.image = AUIKaraokeRoomBundle.getCommonImage("ic_diange_16px")
        view.backgroundColor = .clear
        return view
    }()
    
    public lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_strong
        label.font = AVTheme.mediumFont(14)
        label.textAlignment = .center
        return label
    }()
    
    private func updateSubViewsLayout() {
        self.iconView.snp.removeConstraints()
        self.titleLabel.snp.removeConstraints()
        switch self.viewStyle {
        case .small:
            self.iconView.snp.makeConstraints { make in
                make.width.height.equalTo(12)
                make.left.equalTo(8)
                make.centerY.equalToSuperview()
            }
            self.titleLabel.snp.makeConstraints { make in
                make.left.equalTo(self.iconView.snp.right)
                make.right.equalToSuperview().offset(-8)
                make.top.bottom.equalToSuperview()
            }
            self.titleLabel.font = AVTheme.mediumFont(10)
         break
        case .big:
            self.iconView.snp.makeConstraints { make in
                make.width.height.equalTo(16)
                make.left.equalTo(16)
                make.centerY.equalToSuperview()
            }
            self.titleLabel.snp.makeConstraints { make in
                make.left.equalTo(self.iconView.snp.right)
                make.right.equalToSuperview().offset(-16)
                make.top.bottom.equalToSuperview()
            }
            self.titleLabel.font = AVTheme.mediumFont(14)
        }
    }
    
    private lazy var tapGesture: UITapGestureRecognizer = {
        let ges = UITapGestureRecognizer(target: self, action: #selector(onTap(recognizer:)))
        return ges
    }()
    
    @objc func onTap(recognizer: UIGestureRecognizer) {
        self.clickBlock?(self)
    }
    
    open var clickBlock: ((_ sender: AUIKaraokeRoomMusicListEntryView)->Void)? = nil
}
