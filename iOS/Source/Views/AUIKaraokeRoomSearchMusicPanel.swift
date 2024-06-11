//
//  AUIKaraokeRoomSearchMusicPanel.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/4/15.
//

import UIKit
import SnapKit
import AUIFoundation

open class AUIKaraokeRoomSearchMusicPanel: AVBaseControllPanel {

    private lazy var searchBarContentView: UIView = {
        let view = UIView()
        view.backgroundColor = .clear
        return view
    }()
    
    private lazy var textContentView: UIView = {
        let view = UIView()
        view.backgroundColor = AVTheme.fill_weak
        view.layer.cornerRadius = 15
        view.layer.masksToBounds = true
        return view
    }()
    
    private lazy var placeholder: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_ultraweak
        label.font = AVTheme.regularFont(12)
        label.textAlignment = .left
        label.text = AUIKaraokeRoomBundle.getString("搜索歌曲名称或歌手名")
        return label
    }()
    
    private lazy var textField: UITextField = {
        let view = UITextField()
        view.backgroundColor = .clear
        view.textColor = AVTheme.text_strong
        view.font = AVTheme.regularFont(12)
        view.tintColor = AVTheme.colourful_border_strong
        view.returnKeyType = .search
        view.addTarget(self, action: #selector(textFieldDidChange(_:)), for: .editingChanged)
        view.addTarget(self, action: #selector(requestData), for: .editingDidEndOnExit)
        return view
    }()
    
    private lazy var cancelBtn: UIButton = {
        let btn = UIButton(frame: CGRect.zero)
        btn.titleLabel?.font = AVTheme.regularFont(14)
        btn.setTitle(AUIKaraokeRoomBundle.getString("取消"), for: UIControl.State.normal)
        btn.setTitleColor(AVTheme.text_strong, for: UIControl.State.normal)
        btn.addTarget(self, action: #selector(cancelBtnOnClick(_:)), for: UIControl.Event.touchUpInside)
        return btn
    }()
    
    public lazy var musicListView: AUIKaraokeRoomMusicListView = {
        let view = AUIKaraokeRoomMusicListView(frame: CGRect.zero, musicListType: AUIKaraokeRoomMusicListView.MusicListType.search)
        return view
    }()
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.setupSelfUI()
    }

    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        NSObject.cancelPreviousPerformRequests(withTarget: self)
    }
    
    open override class func panelHeight() -> CGFloat {
        return 570
    }
    
//    public override func show(on onView: UIView) {
//        super.show(on: onView)
//        self.textField.becomeFirstResponder()
//    }
//
//    public override func show(on onView: UIView, with bgType: AVControllPanelBackgroundType) {
//        super.show(on: onView, with: bgType)
//        self.textField.becomeFirstResponder()
//    }
    
    private func setupSelfUI() {
        self.titleView.isHidden = true
        
        self.headerView.addSubview(self.searchBarContentView)
        self.searchBarContentView.snp.makeConstraints{ make in
            make.width.height.top.bottom.equalToSuperview()
        }
        
        self.searchBarContentView.addSubview(self.cancelBtn)
        self.cancelBtn.snp.makeConstraints { make in
            make.left.equalTo(self.searchBarContentView.snp.right).offset(-68)
            make.right.equalToSuperview()
            make.top.bottom.equalToSuperview()
        }
        
        self.searchBarContentView.addSubview(self.textContentView)
        self.textContentView.snp.makeConstraints{ make in
            make.top.equalTo(8)
            make.bottom.equalTo(-8)
            make.left.equalTo(20)
            make.right.equalTo(self.cancelBtn.snp.left).offset(-4)
        }
        
        self.searchBarContentView.addSubview(self.placeholder)
        self.placeholder.snp.makeConstraints { make in
            make.top.bottom.equalTo(self.textContentView)
            make.left.equalTo(self.textContentView.snp.left).offset(16)
            make.right.equalTo(self.textContentView.snp.right).offset(-16)
        }
        
        self.searchBarContentView.addSubview(self.textField)
        self.textField.snp.makeConstraints { make in
            make.top.bottom.equalTo(self.textContentView)
            make.left.equalTo(self.textContentView.snp.left).offset(16)
            make.right.equalTo(self.textContentView.snp.right).offset(-16)
        }
        
        self.contentView.addSubview(self.musicListView)
        self.musicListView.snp.makeConstraints{ make in
            make.top.bottom.width.height.equalToSuperview()
        }
        self.musicListView.scrollListBlock = {[weak self] in
            self?.textField.resignFirstResponder()
        }
    }
    
    public func activeSearchTextInput() {
        self.textField.becomeFirstResponder()
    }
    
    @objc private func cancelBtnOnClick(_ sender: UIButton!) {
        hide()
    }
    
    @objc private func textFieldDidChange(_ textField: UITextField) -> Bool {
        NSObject.cancelPreviousPerformRequests(withTarget: self)
        if textField.text?.count ?? 0 > 0 {
            self.placeholder.isHidden = true
        } else {
            self.placeholder.isHidden = false
        }
        self.perform(#selector(requestData), with: nil, afterDelay: 0.5)
        return true
    }
    
    @objc private func requestData() {
        let whitespaceSet = NSCharacterSet.whitespacesAndNewlines
        let searchStr = self.textField.text?.trimmingCharacters(in: whitespaceSet)
        self.musicListView.searchMusicWithKeyword(searchStr)
    }
}

