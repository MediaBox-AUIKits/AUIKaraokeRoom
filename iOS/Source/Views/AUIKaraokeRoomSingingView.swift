//
//  AUIKaraokeRoomSingingView.swift
//  Example
//
//  Created by Bingo on 2024/2/26.
//

import UIKit
import AUIFoundation
import SnapKit

private class AUIKaraokeRoomSingingHeaderView: UIView {
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = .clear
        self.setupSubViews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupSubViews() {
        self.addSubview(self.musicListEntryBtn)
        self.musicListEntryBtn.snp.makeConstraints { make in
            make.width.equalTo(52)
            make.height.equalTo(24)
            make.top.equalTo(8)
            make.right.equalTo(-14)
        }
        
        self.addSubview(self.musicScoreLabel)
        self.musicScoreLabel.snp.makeConstraints { make in
            make.width.equalTo(48)
            make.height.equalTo(16)
            make.centerY.equalTo(self.musicListEntryBtn)
            make.right.equalTo(self.musicListEntryBtn.snp.left).offset(-8)
        }
        
        self.addSubview(self.musicTitleLabel)
        self.musicTitleLabel.snp.makeConstraints { make in
            make.left.equalTo(14)
            make.right.equalTo(self.musicScoreLabel.snp.left).offset(-8)
            make.height.equalTo(16)
            make.centerY.equalTo(self.musicListEntryBtn)
        }
        
        self.addSubview(self.musicTimeLabel)
        self.musicTimeLabel.snp.makeConstraints { make in
            make.left.equalTo(self.musicTitleLabel)
            make.top.equalTo(self.musicTitleLabel.snp.bottom).offset(1)
            make.width.equalTo(self.musicTitleLabel)
            make.height.equalTo(14)
        }
    }
    
    public lazy var musicTitleLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_strong
        label.font = AVTheme.regularFont(10)
        label.text = AUIKaraokeRoomBundle.getString("MusicTitle")
        label.textAlignment = .left
        return label
    }()
    
    public lazy var musicTimeLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_weak
        label.font = AVTheme.regularFont(9)
        label.text = AUIKaraokeRoomBundle.getString("00:00/00:00")
        label.textAlignment = .left
        return label
    }()
    
    public lazy var musicScoreLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_strong
        label.font = AVTheme.regularFont(10)
        label.text = AUIKaraokeRoomBundle.getString("0 分")
        label.textAlignment = .right
        return label
    }()
    
    public lazy var musicListEntryBtn: AUIKaraokeRoomMusicListEntryView = {
        let btn = AUIKaraokeRoomMusicListEntryView()
        btn.viewStyle = .small
        btn.layer.cornerRadius = 12
        btn.layer.masksToBounds = true
        return btn
    }()
    
    public var score: Int = 0 {
        didSet {
            self.musicScoreLabel.text = "\(self.score) 分"
        }
    }
}

private class AUIKaraokeRoomTrackSwitch : UIView {
    
    public enum TrackMode {
        case backing
        case original
    }
    
    private var _trackMode: TrackMode = .backing
    public var trackMode: TrackMode {
        get {
            return self._trackMode
        }
        set {
            self._trackMode = newValue
            self.updateSubViewsLayout()
        }
    }

    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.addSubview(self.selectedBg)
        
        self.addSubview(self.leftTitleLabel)
        self.leftTitleLabel.snp.makeConstraints { make in
            make.left.equalTo(8)
            make.right.equalTo(self.snp.centerX).offset(-8)
            make.top.bottom.equalToSuperview()
        }
        self.addSubview(self.rightTitleLabel)
        self.rightTitleLabel.snp.makeConstraints { make in
            make.left.equalTo(self.snp.centerX).offset(8)
            make.right.equalToSuperview().offset(-8)
            make.top.bottom.equalToSuperview()
        }
        
        self.addGestureRecognizer(self.tapGesture)
        
        self.backgroundColor = AVTheme.fill_infrared.withAlphaComponent(0.15)
        
        self.updateSubViewsLayout()
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public lazy var selectedBg: UIView = {
        let view = UIView()
        view.backgroundColor = AVTheme.fill_infrared.withAlphaComponent(0.15)
        view.layer.masksToBounds = true
        return view
    }()
    
    public lazy var leftTitleLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.regularFont(10)
        label.text = AUIKaraokeRoomBundle.getString("伴奏")
        return label
    }()
    
    public lazy var rightTitleLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.regularFont(10)
        label.text = AUIKaraokeRoomBundle.getString("原唱")
        return label
    }()
    
    private func updateSubViewsLayout() {
        self.selectedBg.snp.removeConstraints()
        switch self.trackMode {
        case .backing:
            self.leftTitleLabel.textColor = AVTheme.text_strong
            self.rightTitleLabel.textColor =  AVTheme.text_weak
            self.selectedBg.snp.makeConstraints { make in
                make.left.top.bottom.equalToSuperview()
                make.right.equalTo(self.snp.centerX)
            }
            break
        case .original:
            self.leftTitleLabel.textColor = AVTheme.text_weak
            self.rightTitleLabel.textColor =  AVTheme.text_strong
            self.selectedBg.snp.makeConstraints { make in
                make.right.top.bottom.equalToSuperview()
                make.left.equalTo(self.snp.centerX)
            }
            break
        }
    }
    
    private lazy var tapGesture: UITapGestureRecognizer = {
        let ges = UITapGestureRecognizer(target: self, action: #selector(onTap(recognizer:)))
        return ges
    }()
    
    @objc func onTap(recognizer: UIGestureRecognizer) {
        self.clickBlock?(self)
    }
    
    open var clickBlock: ((_ sender: AUIKaraokeRoomTrackSwitch)->Void)? = nil
}

private class AUIKaraokeRoomSingingFooterView: UIView {
    
    public enum AUIKaraokeRoomSingingFooterViewDisplayStyle {
        case leadingSingerStyle
        case joinSingerStyle
        case joinMicAudienceStyle
        case anchorAudienceStyle
        case anchorJoinSingStyle
    }
    
    public var displayStyle: AUIKaraokeRoomSingingFooterViewDisplayStyle = .leadingSingerStyle {
        didSet {
            self.trackSwitch.isHidden = true
            self.playPauseBtn.isHidden = true
            self.skipMusicBtn.isHidden = true
            self.trackSwitch.isHidden = true
            self.quitSingTogetherBtn.isHidden = true
            self.singTogetherBtn.isHidden = true
            switch self.displayStyle {
            case .leadingSingerStyle:
                self.trackSwitch.isHidden = false
                self.playPauseBtn.isHidden = false
                self.skipMusicBtn.isHidden = false
                self.trackSwitch.snp.removeConstraints()
                self.trackSwitch.snp.makeConstraints { make in
                    make.top.equalTo(4)
                    make.right.equalToSuperview().offset(-14)
                    make.width.equalTo(72)
                    make.height.equalTo(24)
                }
                self.skipMusicBtn.snp.removeConstraints()
                self.skipMusicBtn.snp.makeConstraints { make in
                    make.bottom.equalToSuperview().offset(-8)
                    make.left.equalTo(self.playPauseBtn.snp.right).offset(8)
                    make.width.equalTo(52)
                    make.height.equalTo(24)
                }
                break
            case .joinSingerStyle:
                self.trackSwitch.isHidden = false
                self.quitSingTogetherBtn.isHidden = false
                self.trackSwitch.snp.removeConstraints()
                self.trackSwitch.snp.makeConstraints { make in
                    make.bottom.equalToSuperview().offset(-8)
                    make.right.equalTo(self.quitSingTogetherBtn.snp.left).offset(-8)
                    make.width.equalTo(72)
                    make.height.equalTo(24)
                }
                break
            case .joinMicAudienceStyle:
                self.singTogetherBtn.isHidden = false
                break
            case .anchorAudienceStyle:
                self.singTogetherBtn.isHidden = false
                self.skipMusicBtn.isHidden = false
                self.skipMusicBtn.snp.removeConstraints()
                self.skipMusicBtn.snp.makeConstraints { make in
                    make.left.equalTo(14)
                    make.bottom.equalToSuperview().offset(-8)
                    make.width.equalTo(52)
                    make.height.equalTo(24)
                }
                break
            case .anchorJoinSingStyle:
                self.trackSwitch.isHidden = false
                self.quitSingTogetherBtn.isHidden = false
                self.trackSwitch.snp.removeConstraints()
                self.trackSwitch.snp.makeConstraints { make in
                    make.bottom.equalToSuperview().offset(-8)
                    make.right.equalTo(self.quitSingTogetherBtn.snp.left).offset(-8)
                    make.width.equalTo(72)
                    make.height.equalTo(24)
                }
                self.skipMusicBtn.isHidden = false
                self.skipMusicBtn.snp.removeConstraints()
                self.skipMusicBtn.snp.makeConstraints { make in
                    make.left.equalTo(14)
                    make.bottom.equalToSuperview().offset(-8)
                    make.width.equalTo(52)
                    make.height.equalTo(24)
                }
                break
            }
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = .clear
        self.setupSubViews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupSubViews() {
        self.addSubview(self.playPauseBtn)
        self.playPauseBtn.snp.makeConstraints { make in
            make.top.equalTo(4)
            make.left.equalTo(14)
            make.width.equalTo(52)
            make.height.equalTo(24)
        }
        self.playPauseBtn.layer.cornerRadius = 12
        
        self.addSubview(self.skipMusicBtn)
        self.skipMusicBtn.snp.makeConstraints { make in
            make.top.equalTo(4)
            make.left.equalTo(self.playPauseBtn.snp.right).offset(8)
            make.width.equalTo(52)
            make.height.equalTo(24)
        }
        self.skipMusicBtn.layer.cornerRadius = 12

        self.addSubview(self.trackSwitch)
        self.trackSwitch.snp.makeConstraints { make in
            make.right.equalToSuperview().offset(-14)
            make.bottom.equalToSuperview().offset(-8)
            make.width.equalTo(72)
            make.height.equalTo(24)
        }
        self.trackSwitch.layer.cornerRadius = 12
        self.trackSwitch.selectedBg.layer.cornerRadius = 12
        self.trackSwitch.clickBlock = { [weak self] sender in
            if self?.trackSwitch.trackMode == .backing {
                self?.trackSwitch.trackMode = .original
            } else {
                self?.trackSwitch.trackMode = .backing
            }
            self?.clickTrackSwitchBlock?(self?.trackSwitch.trackMode == .backing)
        }
        
        self.addSubview(self.singTogetherBtn)
        self.singTogetherBtn.snp.makeConstraints { make in
            make.right.equalToSuperview().offset(-4)
            make.bottom.equalToSuperview().offset(-8)
            make.width.equalTo(62)
            make.height.equalTo(24)
        }
        self.singTogetherBtn.layer.cornerRadius = 12

        self.addSubview(self.quitSingTogetherBtn)
        self.quitSingTogetherBtn.snp.makeConstraints { make in
            make.right.equalToSuperview().offset(-14)
            make.bottom.equalToSuperview().offset(-8)
            make.width.equalTo(52)
            make.height.equalTo(24)
        }
        self.quitSingTogetherBtn.layer.cornerRadius = 12
        
        self.displayStyle = .leadingSingerStyle
    }
    
    public lazy var playPauseBtn: UIButton = {
        let btn = UIButton(frame: CGRect.zero)
        btn.setTitle(AUIKaraokeRoomBundle.getString("播放"), for: .normal)
        btn.setImage(AUIKaraokeRoomBundle.getCommonImage("ic_audio_play"), for: .normal)
        btn.setTitle(AUIKaraokeRoomBundle.getString("暂停"), for: .selected)
        btn.setImage(AUIKaraokeRoomBundle.getCommonImage("ic_audio_pause_filled"), for: .selected)
        btn.titleLabel?.font = AVTheme.regularFont(10)
        btn.backgroundColor = AVTheme.fill_infrared.withAlphaComponent(0.15)
        btn.contentHorizontalAlignment = .center
        btn.imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 4)
        btn.titleEdgeInsets = UIEdgeInsets(top: 0, left: 4, bottom: 0, right: 0)
        btn.layer.masksToBounds = true
        btn.addTarget(self, action: #selector(playPauseBtnOnClick(_:)), for: UIControl.Event.touchUpInside)
        return btn
    }()
    
    public lazy var skipMusicBtn: UIButton = {
        let btn = UIButton(frame: CGRect.zero)
        btn.setTitle(AUIKaraokeRoomBundle.getString("切歌"), for: .normal)
        btn.setImage(AUIKaraokeRoomBundle.getCommonImage("ic_audio_skip_forward_small"), for: .normal)
        btn.titleLabel?.font = AVTheme.regularFont(10)
        btn.backgroundColor = AVTheme.fill_infrared.withAlphaComponent(0.15)
        btn.contentHorizontalAlignment = .center
        btn.imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 4)
        btn.titleEdgeInsets = UIEdgeInsets(top: 0, left: 4, bottom: 0, right: 0)
        btn.layer.masksToBounds = true
        btn.addTarget(self, action: #selector(skipMusicBtnOnClick(_:)), for: UIControl.Event.touchUpInside)
        return btn
    }()
    
    public lazy var singTogetherBtn: UIButton = {
        let btn = UIButton(frame: CGRect.zero)
        btn.setTitle(AUIKaraokeRoomBundle.getString("一起唱"), for: .normal)
        btn.setImage(AUIKaraokeRoomBundle.getCommonImage("ic_sing_together"), for: .normal)
        btn.titleLabel?.font = AVTheme.regularFont(10)
        btn.backgroundColor = AVTheme.fill_infrared.withAlphaComponent(0.15)
        btn.contentHorizontalAlignment = .center
        btn.imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 4)
        btn.titleEdgeInsets = UIEdgeInsets(top: 0, left: 4, bottom: 0, right: 0)
        btn.layer.masksToBounds = true
        btn.addTarget(self, action: #selector(singTogetherBtnOnClick(_:)), for: UIControl.Event.touchUpInside)
        return btn
    }()
    
    public lazy var quitSingTogetherBtn: UIButton = {
        let btn = UIButton(frame: CGRect.zero)
        btn.setTitle(AUIKaraokeRoomBundle.getString("退出"), for: .normal)
        btn.setImage(AUIKaraokeRoomBundle.getCommonImage("ic_sing_together"), for: .normal)
        btn.titleLabel?.font = AVTheme.regularFont(10)
        btn.backgroundColor = AVTheme.fill_infrared.withAlphaComponent(0.15)
        btn.contentHorizontalAlignment = .center
        btn.imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 4)
        btn.titleEdgeInsets = UIEdgeInsets(top: 0, left: 4, bottom: 0, right: 0)
        btn.layer.masksToBounds = true
        btn.addTarget(self, action: #selector(quitSingTogetherBtnOnClick(_:)), for: UIControl.Event.touchUpInside)
        return btn
    }()
    
    public lazy var trackSwitch: AUIKaraokeRoomTrackSwitch = {
        let view = AUIKaraokeRoomTrackSwitch()
        return view
    }()
    
    @objc private func playPauseBtnOnClick(_ sender: UIButton!) {
        sender.isSelected = !sender.isSelected
        self.clickPlayPauseBtnBlock?(!sender.isSelected)
    }
    
    @objc private func skipMusicBtnOnClick(_ sender: UIButton!) {
        self.clickSkipMusicBtnBlock?()
    }
    
    @objc private func singTogetherBtnOnClick(_ sender: UIButton!) {
        self.clickSingTogetherBlock?()
    }
    
    @objc private func quitSingTogetherBtnOnClick(_ sender: UIButton!) {
        self.clickQuitSingTogetherBlock?()
    }
    
    open var clickPlayPauseBtnBlock: ((_ pause: Bool)->Void)? = nil
    open var clickSkipMusicBtnBlock: (()->Void)? = nil
    open var clickTrackSwitchBlock: ((_ isBackTracking: Bool)->Void)? = nil
    open var clickSingTogetherBlock: (()->Void)? = nil
    open var clickQuitSingTogetherBlock: (()->Void)? = nil
}

private class AUIKaraokeRoomSingingWaitingView: UIView {
    
    public enum AUIKaraokeRoomSingingWaitingViewDisplayType {
        case style1
        case style2
        case style3
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = .clear
        self.setupSubViews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupSubViews() {
        self.addSubview(self.titleLabel)
        self.addSubview(self.countDownLabel)
        self.viewDisplayType = .style1
    }
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_strong
        label.font = AVTheme.regularFont(12)
        label.textAlignment = .center
        return label
    }()
    
    private lazy var countDownLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_weak
        label.font = AVTheme.regularFont(10)
        label.textAlignment = .center
        return label
    }()
    
    public func updateTitle(singerName: String, musicName: String) {
        self.titleLabel.text = "\(singerName) 即将演唱：\(musicName)"
    }

    public var countDownNum: Int = 0 {
        didSet {
            self.countDownLabel.text = "\(self.countDownNum)秒后开始"
        }
    }
    
    public var viewDisplayType: AUIKaraokeRoomSingingWaitingViewDisplayType = .style1 {
        didSet {
            switch self.viewDisplayType {
            case .style1:
                self.titleLabel.isHidden = false
                self.titleLabel.font = AVTheme.regularFont(12)
                self.titleLabel.snp.removeConstraints()
                self.titleLabel.snp.makeConstraints { make in
                    make.top.equalTo(12)
                    make.left.equalTo(0)
                    make.right.equalTo(0)
                    make.height.equalTo(18)
                }
                
                self.countDownLabel.isHidden = false
                self.countDownLabel.snp.removeConstraints()
                self.countDownLabel.snp.makeConstraints { make in
                    make.top.equalTo(self.titleLabel.snp.bottom).offset(4)
                    make.left.equalTo(0)
                    make.right.equalTo(0)
                    make.height.equalTo(16)
                }
                break
            case .style2:
                self.titleLabel.isHidden = false
                self.titleLabel.font = AVTheme.regularFont(12)
                self.titleLabel.snp.removeConstraints()
                self.titleLabel.snp.makeConstraints { make in
                    make.top.equalTo(76)
                    make.left.equalTo(0)
                    make.right.equalTo(0)
                    make.height.equalTo(18)
                }
                
                self.countDownLabel.isHidden = false
                self.countDownLabel.snp.removeConstraints()
                self.countDownLabel.snp.makeConstraints { make in
                    make.top.equalTo(self.titleLabel.snp.bottom).offset(4)
                    make.left.equalTo(0)
                    make.right.equalTo(0)
                    make.height.equalTo(16)
                }
                break
            case .style3:
                self.titleLabel.isHidden = false
                self.titleLabel.font = AVTheme.regularFont(16)
                self.titleLabel.snp.removeConstraints()
                self.titleLabel.snp.makeConstraints { make in
                    make.top.equalTo(96)
                    make.left.equalTo(0)
                    make.right.equalTo(0)
                    make.height.equalTo(24)
                }
                
                self.countDownLabel.isHidden = true
                self.countDownLabel.snp.removeConstraints()
                break
            }
        }
    }
}

@objcMembers open class AUIKaraokeRoomSingingView: UIView {

    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = AVTheme.tsp_fill_weak
        self.setupSubViews()
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupSubViews() {
                
        self.addSubview(self.joinMicToAddMusicTipsLabel)
        self.joinMicToAddMusicTipsLabel.snp.remakeConstraints { make in
            make.top.left.right.bottom.equalToSuperview()
        }
        
        self.addSubview(self.musicListEntryBtn)
        self.musicListEntryBtn.snp.makeConstraints { make in
            make.width.equalTo(80)
            make.height.equalTo(30)
            make.centerX.equalToSuperview()
            make.centerY.equalToSuperview()
        }
        self.musicListEntryBtn.clickBlock = { [weak self] sender in
            self?.musicListEntryClickBlock?(self?.musicListEntryBtn)
        }
        
        self.addSubview(self.headerView)
        self.headerView.snp.makeConstraints { make in
            make.top.left.width.equalToSuperview()
            make.height.equalTo(43)
        }
        self.headerView.musicListEntryBtn.clickBlock = { [weak self] sender in
            self?.musicListEntryClickBlock?(self?.headerView.musicListEntryBtn)
        }
        
        self.addSubview(self.footerView)
        self.footerView.snp.makeConstraints { make in
            make.top.equalTo(self.snp.bottom).offset(-36)
            make.left.width.bottom.equalToSuperview()
        }
        self.footerView.clickPlayPauseBtnBlock = {[weak self] pause in
            self?.playPauseBlock?(pause)
        }
        self.footerView.clickSkipMusicBtnBlock = {[weak self] in
            self?.skipMusicBlock?()
        }
        self.footerView.clickTrackSwitchBlock = {[weak self] isBackTracking in
            self?.trackModeChangeBlock?(isBackTracking)
        }
        self.footerView.clickSingTogetherBlock = {[weak self] in
            guard let self = self else { return }
            self.singTogetherBlock?()
        }
        
        self.footerView.clickQuitSingTogetherBlock = {[weak self] in
            guard let self = self else { return }
            self.quitSingTogetherBlock?()
        }
        
        self.addSubview(self.pitchView)
        self.pitchView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalTo(self.headerView.snp.bottom)
            make.height.equalTo(67)
        }
        
        self.addSubview(self.waitingView)
        self.waitingView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalTo(self.pitchView.snp.bottom)
            make.bottom.equalTo(self.footerView.snp.top)
        }
        self.sendSubviewToBack(self.waitingView)
        self.updateViewDisplay(isJoinMic: false, isAnchor: false, isSinging: false, isLeadingSinger: false, isJoinSinger: false, isWaiting: false)
        
        self.addSubview(self.singerLyricView)
        self.singerLyricView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalTo(self.pitchView.snp.bottom)
            make.height.equalTo(44)
        }
        self.addSubview(self.audienceLyricView)
        self.audienceLyricView.snp.makeConstraints { make in
            make.top.left.bottom.right.equalToSuperview()
        }
        self.sendSubviewToBack(self.audienceLyricView)
    }
    
    private lazy var joinMicToAddMusicTipsLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_weak
        label.font = AVTheme.mediumFont(12)
        label.text = AUIKaraokeRoomBundle.getString("上麦后，可点歌")
        label.textAlignment = .center
        return label
    }()
    
    private lazy var headerView: AUIKaraokeRoomSingingHeaderView = {
        let view = AUIKaraokeRoomSingingHeaderView(frame: CGRect.zero)
        return view
    }()
    
    private lazy var footerView: AUIKaraokeRoomSingingFooterView = {
        let view = AUIKaraokeRoomSingingFooterView(frame: CGRect.zero)
        return view
    }()
    
    private lazy var musicListEntryBtn: AUIKaraokeRoomMusicListEntryView = {
        let btn = AUIKaraokeRoomMusicListEntryView()
        btn.viewStyle = .big
        btn.layer.cornerRadius = 15
        btn.layer.masksToBounds = true
        return btn
    }()
    
    private lazy var pitchView: AUIKaraokeRoomPitchView = {
        let view = AUIKaraokeRoomPitchView(frame: CGRect.zero)
        view.setConfig(nil)
        return view
    }()
    
    private lazy var waitingView: AUIKaraokeRoomSingingWaitingView = {
        let view = AUIKaraokeRoomSingingWaitingView(frame: CGRect.zero)
        return view
    }()
    
    private lazy var singerLyricView: AUIKaraokeRoomMicJoinerLyricView = {
        let lyricModel = AUIKaraokeRoomLyricModel()
        let lyricViewModel = AUIKaraokeRoomLyricViewModel(model: lyricModel)
        let view = AUIKaraokeRoomMicJoinerLyricView(viewModel: lyricViewModel)
        return view
    }()
    
    private lazy var audienceLyricView: AUIKaraokeRoomAudienceLyricView = {
        let lyricModel = AUIKaraokeRoomLyricModel()
        let lyricViewModel = AUIKaraokeRoomLyricViewModel(model: lyricModel)
        let view = AUIKaraokeRoomAudienceLyricView(viewModel: lyricViewModel)
        return view
    }()
    
    open var musicListEntryClickBlock: ((_ sender: Any?)->Void)? = nil
    open var playPauseBlock: ((_ pause: Bool)->Void)? = nil
    open var skipMusicBlock: (()->Void)? = nil
    open var trackModeChangeBlock: ((_ isBackTracking: Bool)->Void)? = nil
    open var singTogetherBlock: (()->Void)? = nil
    open var quitSingTogetherBlock: (()->Void)? = nil
    
    public func updateViewDisplay(isJoinMic: Bool = false, isAnchor: Bool = false, isSinging: Bool = false, isLeadingSinger: Bool = false, isJoinSinger: Bool = false, isWaiting: Bool = false) {
        
        self.musicListEntryBtn.isHidden = true
        self.joinMicToAddMusicTipsLabel.isHidden = true
        self.headerView.isHidden = true
        self.headerView.musicListEntryBtn.isHidden = false
        self.headerView.musicScoreLabel.isHidden = false
        self.footerView.isHidden = true
        self.pitchView.isHidden = true
        self.waitingView.isHidden = true
        self.audienceLyricView.isHidden = true
        self.singerLyricView.isHidden = true
        if isWaiting {
            self.waitingView.isHidden = false
            self.waitingView.snp.removeConstraints()
            if isJoinMic {
                self.headerView.isHidden = false
                self.footerView.isHidden = false
                if isLeadingSinger || isJoinSinger {
                    self.footerView.displayStyle = isLeadingSinger ? .leadingSingerStyle :  isAnchor ? .anchorJoinSingStyle : .joinSingerStyle
                    self.waitingView.viewDisplayType = .style1
                    self.waitingView.snp.makeConstraints { make in
                        make.left.right.equalToSuperview()
                        make.top.equalTo(self.pitchView.snp.bottom)
                        make.bottom.equalTo(self.footerView.snp.top)
                    }
                    self.pitchView.isHidden = false
                } else {
                    self.headerView.musicScoreLabel.isHidden = true
                    self.footerView.displayStyle = isAnchor ? .anchorAudienceStyle : .joinMicAudienceStyle
                    self.waitingView.viewDisplayType = .style2
                    self.waitingView.snp.makeConstraints { make in
                        make.top.bottom.left.right.equalToSuperview()
                    }
                }
            } else {
                self.headerView.isHidden = false
                self.headerView.musicListEntryBtn.isHidden = true
                self.headerView.musicScoreLabel.isHidden = true
                self.waitingView.viewDisplayType = .style3
                self.waitingView.snp.makeConstraints { make in
                    make.top.bottom.left.right.equalToSuperview()
                }
            }
        } else if isSinging {
            if isJoinMic {
                self.headerView.isHidden = false
                self.footerView.isHidden = false
                self.singerLyricView.isHidden = false
                if isLeadingSinger || isJoinSinger {
                    self.footerView.displayStyle = isLeadingSinger ? .leadingSingerStyle : isAnchor ? .anchorJoinSingStyle : .joinSingerStyle
                    self.pitchView.isHidden = false
                    self.singerLyricView.snp.removeConstraints()
                    self.singerLyricView.snp.makeConstraints { make in
                        make.left.right.equalToSuperview()
                        make.top.equalTo(self.pitchView.snp.bottom)
                        make.height.equalTo(44)
                    }
                } else {
                    self.headerView.musicScoreLabel.isHidden = true
                    self.footerView.displayStyle = isAnchor ? .anchorAudienceStyle : .joinMicAudienceStyle
                    self.singerLyricView.snp.removeConstraints()
                    self.singerLyricView.snp.makeConstraints { make in
                        make.height.equalTo(44)
                        make.width.equalToSuperview()
                        make.center.equalToSuperview()
                    }
                }
            } else {
                self.headerView.isHidden = false
                self.headerView.musicScoreLabel.isHidden = true
                self.headerView.musicListEntryBtn.isHidden = true
                self.audienceLyricView.isHidden = false
            }
        } else {
            if isJoinMic {
                self.musicListEntryBtn.isHidden = false
            } else {
                self.joinMicToAddMusicTipsLabel.isHidden = false
            }
        }
    }
    
    public var curPlayingMusic: AUIKaraokeRoomMusicInfo? = nil {
        didSet {
            if let musicInfo = self.curPlayingMusic {
                self.headerView.musicTitleLabel.text = AUIKaraokeRoomBundle.getString("\(musicInfo.artist)《\(musicInfo.songName)》")
                let durationStr = AUIKaraokeRoomViewModel.transToMinSec(musicInfo.duration)
                self.headerView.musicTimeLabel.text = "00:00/\(durationStr)"
                (self.singerLyricView.viewModel as? AUIKaraokeRoomLyricViewModel)?.loadLyric(musicInfo.lyric)
                (self.audienceLyricView.viewModel as? AUIKaraokeRoomLyricViewModel)?.loadLyric(musicInfo.lyric)
                self.pitchView.reset()
                let stdPitch = AUIKaraokeRoomPitchModel.analyzePitchData(json: musicInfo.pitchJson)
                self.pitchView.setStandardPitchModels(stdPitch)
            } else {
                self.headerView.musicTitleLabel.text = AUIKaraokeRoomBundle.getString("MusicTitle")
                self.headerView.musicTimeLabel.text = AUIKaraokeRoomBundle.getString("00:00/00:00")
                (self.singerLyricView.viewModel as? AUIKaraokeRoomLyricViewModel)?.cleanLyric()
                (self.audienceLyricView.viewModel as? AUIKaraokeRoomLyricViewModel)?.cleanLyric()
                self.pitchView.reset()
            }
        }
    }
    
    public var musicPlaying: Bool = false {
        didSet {
            self.footerView.playPauseBtn.isSelected = musicPlaying
        }
    }
    
    public var singerPitch: Int32 = 0
    
    public var singScore: Int {
        get {
            return self.headerView.score
        }
    }
    
    public func updatePlayProgress(progress: Int64, duration: Int) {
        let durationStr = AUIKaraokeRoomViewModel.transToMinSec(duration)
        let progressStr = AUIKaraokeRoomViewModel.transToMinSec(Int(progress))
        self.headerView.musicTimeLabel.text = "\(progressStr)/\(durationStr)"
        if !self.singerLyricView.isHidden {
            self.singerLyricView.updateProgress(UInt32(progress))
        }
        if !self.audienceLyricView.isHidden {
            self.audienceLyricView.updateProgress(UInt32(progress))
        }
        
        let pitch = self.singerPitch
        self.pitchView.setCurrentSongProgress(NSInteger(progress), Int(pitch))
        
        if(self.singerLyricView.isLineFinished()) {
            let lineScore = self.pitchView.updateLineScore()
            self.pitchView.addScore(lineScore)
            self.singerLyricView.resetLineState()
            self.headerView.score += lineScore
        }
    }
    
    weak var timer: Timer?
    private var coundDownNum: Int = 3
    public func startTimerReadyToPlay(singerName: String, musicName: String, completed: @escaping (()->Void)) {
        self.waitingView.updateTitle(singerName: singerName, musicName: musicName)
        self.waitingView.countDownNum = self.coundDownNum
        self.stopCountDownTimer()
        if self.waitingView.viewDisplayType == .style3 {
            return
        }
        weak var weakSelf = self
        DispatchQueue.global().async {
            weakSelf?.coundDownNum = 3
            weakSelf?.timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true, block: {timer in
                let finished = weakSelf?.timerDo(timer) ?? false
                if finished {
                    DispatchQueue.main.async {
                        completed()
                    }
                }
            })
            RunLoop.current.run()
        }
    }
    
    @objc private func timerDo(_ timer: Timer) -> Bool {
        self.coundDownNum -= 1
        if self.coundDownNum == 0 {
            self.timer?.invalidate()
            self.timer = nil
            self.coundDownNum = 3
            return true
        }
        DispatchQueue.main.async {
            self.waitingView.countDownNum = self.coundDownNum
        }
        return false
    }
    
    public func stopCountDownTimer() {
        DispatchQueue.global().async {
            self.timer?.invalidate()
            self.timer = nil
            self.coundDownNum = 3
        }
    }
    
    public func resetToNormalState() {
        self.stopCountDownTimer()
        self.headerView.score = 0
        self.curPlayingMusic = nil
        self.singerPitch = 0
        self.musicPlaying = false
        self.resumeTrackMode()
    }
    
    public func resumeTrackMode() {
        self.footerView.trackSwitch.trackMode = .backing
    }
}
