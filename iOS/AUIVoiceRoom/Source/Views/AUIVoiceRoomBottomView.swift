//
//  AUIVoiceRoomBottomView.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/2/21.
//

import UIKit
import SnapKit
import AUIFoundation

open class AUIVoiceRoomBottomView: UIView {
    
    public convenience init(enableBgMusic: Bool, enableSpeakerBtn: Bool = false) {
        self.init(frame: CGRect.zero, enableBgMusic: enableBgMusic, enableSpeakerBtn: enableSpeakerBtn)
    }
    
    private init(frame: CGRect, enableBgMusic: Bool, enableSpeakerBtn: Bool = false) {
        super.init(frame: frame)
        
        self.addSubview(self.mixerBtn)
        self.mixerBtn.snp.makeConstraints { make in
            make.right.equalToSuperview().offset(-14)
            make.top.equalToSuperview()
            make.width.height.equalTo(44)
        }
        
//        self.addSubview(self.soundEffectBtn)
//        self.soundEffectBtn.snp.makeConstraints { make in
//            make.right.equalTo(self.mixerBtn.snp.left)
//            make.top.equalToSuperview()
//            make.width.height.equalTo(44)
//        }
        
        if enableBgMusic {
            let btn = AVBlockButton()
            btn.imageEdgeInsets = UIEdgeInsets(top: 6, left: 6, bottom: 6, right: 6)
            btn.setImage(AUIVoiceRoomBundle.getCommonImage("ic_bg_music"), for: .normal)
            self.addSubview(btn)
            btn.snp.makeConstraints { make in
                make.right.equalTo(self.mixerBtn.snp.left)
                make.top.equalToSuperview()
                make.width.height.equalTo(44)
            }
            self.bgMusicBtn = btn
        }
        
        self.addSubview(self.switchMicrophoneBtn)
        self.switchMicrophoneBtn.snp.makeConstraints { make in
            make.right.equalTo(self.bgMusicBtn != nil ? self.bgMusicBtn!.snp.left : self.mixerBtn.snp.left)
            make.top.equalToSuperview()
            make.width.height.equalTo(44)
        }
        
        if enableSpeakerBtn {
            let btn = AVBlockButton()
            btn.imageEdgeInsets = UIEdgeInsets(top: 6, left: 6, bottom: 6, right: 6)
            btn.setImage(AUIVoiceRoomBundle.getCommonImage("ic_speaker"), for: .normal)
            btn.setImage(AUIVoiceRoomBundle.getCommonImage("ic_speaker_selected"), for: .selected)
            btn.setImage(AUIVoiceRoomBundle.getCommonImage("ic_speaker_disabled"), for: .disabled)
            btn.isEnabled = true
            self.addSubview(btn)
            btn.snp.makeConstraints { make in
                make.right.equalTo(self.switchMicrophoneBtn.snp.left)
                make.top.equalToSuperview()
                make.width.height.equalTo(44)
            }
            self.switchSpeakerBtn = btn
        }
        
        self.addSubview(self.commentTextField)
        self.commentTextField.snp.remakeConstraints { make in
            make.left.equalToSuperview().offset(20)
            make.right.equalTo((self.switchSpeakerBtn ?? self.switchMicrophoneBtn).snp.left).offset(-6)
            make.height.equalTo(32)
            make.top.equalToSuperview().offset(6)
        }
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        let view = super.hitTest(point, with: event)
        if self.commentTextField.isFirstResponder && view != self.commentTextField {
            self.commentTextField.resignFirstResponder()
        }
        return view
    }
    
    public var switchSpeakerBtn: AVBlockButton? = nil
    
    public lazy var switchMicrophoneBtn: AVBlockButton = {
        let btn = AVBlockButton()
        btn.imageEdgeInsets = UIEdgeInsets(top: 6, left: 6, bottom: 6, right: 6)
        btn.setImage(AUIVoiceRoomBundle.getCommonImage("ic_microphone"), for: .normal)
        btn.setImage(AUIVoiceRoomBundle.getCommonImage("ic_microphone_selected"), for: .selected)
        btn.setImage(AUIVoiceRoomBundle.getCommonImage("ic_microphone_disabled"), for: .disabled)
        btn.isEnabled = false
        return btn
    }()
    
    public lazy var mixerBtn: AVBlockButton = {
        let btn = AVBlockButton()
        btn.imageEdgeInsets = UIEdgeInsets(top: 6, left: 6, bottom: 6, right: 6)
        btn.setImage(AUIVoiceRoomBundle.getCommonImage("ic_mixer"), for: .normal)
        btn.setImage(AUIVoiceRoomBundle.getCommonImage("ic_mixer_disabled"), for: .disabled)
        btn.isEnabled = false
        return btn
    }()
    
    public lazy var soundEffectBtn: AVBlockButton = {
        let btn = AVBlockButton()
        btn.imageEdgeInsets = UIEdgeInsets(top: 6, left: 6, bottom: 6, right: 6)
        btn.setImage(AUIVoiceRoomBundle.getCommonImage("ic_sound_effect"), for: .normal)
        btn.setImage(AUIVoiceRoomBundle.getCommonImage("ic_sound_effect_disabled"), for: .disabled)
        btn.isEnabled = false
        return btn
    }()
    
    public var bgMusicBtn: AVBlockButton? = nil
    
    public lazy var commentTextField: AVCommentTextField = {
        let view = AVCommentTextField()
        view.layer.masksToBounds = true
        view.layer.cornerRadius = 16
        view.font = AVTheme.regularFont(12)
        view.backgroundColorForEdit = AVTheme.fill_weak
        view.willEditBlock = {[weak self] sender, keyboardFrame in
            self?.onCommentStartEdit(keyboardFrame: keyboardFrame)
        }
        view.endEditBlock = {[weak self] sender in
            self?.onCommentEndEdit()
        }
        return view
    }()
    
    func onCommentStartEdit(keyboardFrame: CGRect) {
        self.backgroundColor = AVTheme.bg_weak
        self.commentTextField.snp.remakeConstraints { make in
            make.left.equalToSuperview().offset(16)
            make.right.equalToSuperview().offset(-16)
            make.height.equalTo(32)
            make.top.equalToSuperview().offset(6)
        }
        self.transform = CGAffineTransform(translationX: 0, y: -keyboardFrame.height + self.av_height - 44)
    }
    
    func onCommentEndEdit() {
        self.backgroundColor = .clear
        self.commentTextField.snp.remakeConstraints { make in
            make.left.equalToSuperview().offset(20)
            make.right.equalTo((self.switchSpeakerBtn ?? self.switchMicrophoneBtn).snp.left).offset(-6)
            make.height.equalTo(32)
            make.top.equalToSuperview().offset(6)
        }
        self.transform = CGAffineTransform.identity
    }
}
