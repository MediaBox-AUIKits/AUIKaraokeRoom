//
//  AUIKaraokeRoomSingCompleteView.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/5/16.
//

import Foundation
import AUIFoundation
import SnapKit

@objcMembers open class AUIKaraokeRoomSingingCompleteView: UIView {
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = .black.withAlphaComponent(0.5)
        self.setupSubViews()
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupSubViews() {
        self.addSubview(self.backgroundView)
        self.backgroundView.snp.makeConstraints { make in
            make.width.equalTo(225)
            make.height.equalTo(234)
            make.centerX.equalToSuperview()
            make.centerY.equalToSuperview().offset(-100)
        }
        
        self.backgroundView.addSubview(self.scoreTitleLabel)
        self.scoreTitleLabel.snp.makeConstraints { make in
            make.top.equalTo(104)
            make.left.equalTo(107)
            make.width.equalTo(32)
            make.height.equalTo(24)
        }
        
        self.backgroundView.addSubview(self.scoreValueLabel)
        self.scoreValueLabel.snp.makeConstraints { make in
            make.top.equalTo(self.scoreTitleLabel.snp.bottom).offset(15)
            make.width.equalTo(205)
            make.height.equalTo(24)
            make.centerX.equalTo(self.scoreTitleLabel)
        }
        
        self.addSubview(self.finishBtn)
        self.finishBtn.snp.makeConstraints { make in
            make.top.equalTo(self.backgroundView.snp.bottom).offset(17)
            make.left.equalTo(self.backgroundView.snp.left).offset(71)
            make.width.equalTo(96)
            make.height.equalTo(36)
        }
        self.finishBtn.layer.cornerRadius = 18
    }
    
    private lazy var backgroundView: UIImageView = {
        let view = UIImageView()
        view.backgroundColor = .clear
        view.image = AUIKaraokeRoomBundle.getCommonImage("img_pingfen")
        return view
    }()
    
    private lazy var scoreTitleLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_strong
        label.font = AVTheme.regularFont(16)
        label.text = AUIKaraokeRoomBundle.getString("评分")
        label.textAlignment = .center
        return label
    }()
    
    private lazy var scoreValueLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_strong
        label.font = AVTheme.regularFont(24)
        label.text = AUIKaraokeRoomBundle.getString("0")
        label.textAlignment = .center
        return label
    }()
    
    private lazy var finishBtn: UIButton = {
        let btn = UIButton(frame: CGRect.zero)
        btn.titleLabel?.font = AVTheme.regularFont(16)
        btn.setTitle(AUIKaraokeRoomBundle.getString("好的"), for: .normal)
        btn.backgroundColor = UIColor.av_color(withHexString: "#00BCD4")
        btn.addTarget(self, action: #selector(finishBtnOnClick(_:)), for: UIControl.Event.touchUpInside)
        btn.layer.masksToBounds = true
        return btn
    }()
    
    public var score: Int = 0 {
        didSet {
            self.scoreValueLabel.text = "\(self.score)"
        }
    }
    
    public var clickFinishBlock: (()->Void)? = nil
    @objc private func finishBtnOnClick(_ sender: UIButton!) {
        self.clickFinishBlock?()
    }
}
