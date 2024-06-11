//
//  AUIKaraokeRoomMusicListPanel.swift
//  Example
//
//  Created by Bingo on 2024/2/26.
//

import UIKit
import SnapKit
import AUIFoundation

@objcMembers open class AUIKaraokeRoomMusicListPanel: AVBaseControllPanel {
    
    lazy var musicListPanelHeaderView: AUIKaraokeRoomMusicListPanelHeaderView = {
        let view = AUIKaraokeRoomMusicListPanelHeaderView()
        return view
    }()
    
    lazy var searchEntryView: AUIKaraokeRoomSearchMusicEntryView = {
        let view = AUIKaraokeRoomSearchMusicEntryView()
        return view
    }()
    
    public lazy var chartMenuView: AUIKaraokeRoomMusicChartMenuView = {
        let view = AUIKaraokeRoomMusicChartMenuView()
        return view
    }()
    
    public lazy var musicListView: AUIKaraokeRoomMusicListView = {
        let view = AUIKaraokeRoomMusicListView(frame: CGRect.zero, musicListType: AUIKaraokeRoomMusicListView.MusicListType.addMusic)
        return view
    }()
    
    public lazy var addedMusicListView: AUIKaraokeRoomMusicListView = {
        let view = AUIKaraokeRoomMusicListView(frame: CGRect.zero, musicListType: AUIKaraokeRoomMusicListView.MusicListType.addedMusic)
        return view
    }()
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.setupSelfUI()
    }

    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override class func panelHeight() -> CGFloat {
        return 570
    }
    
    private func setupSelfUI() {
        self.titleView.isHidden = true
        
        self.headerView.addSubview(self.musicListPanelHeaderView)
        self.musicListPanelHeaderView.snp.makeConstraints{ make in
            make.width.height.equalToSuperview()
            make.top.bottom.equalToSuperview()
        }
        
        self.contentView.addSubview(self.searchEntryView)
        self.searchEntryView.snp.makeConstraints{ make in
            make.top.width.equalToSuperview()
            make.height.equalTo(46)
        }
        
        self.contentView.addSubview(self.chartMenuView)
        self.chartMenuView.snp.makeConstraints { make in
            make.top.equalTo(self.searchEntryView.snp.bottom)
            make.width.equalToSuperview()
            make.height.equalTo(46)
        }
        self.chartMenuView.didChangeSelectedChart = {[weak self] chartInfo in
            self?.musicListView.requestViewData(chartInfo)
        }
        
        self.contentView.addSubview(self.musicListView)
        self.musicListView.snp.makeConstraints{ make in
            make.top.equalTo(self.chartMenuView.snp.bottom)
            make.width.bottom.equalToSuperview()
        }
        self.musicListView.requestViewData(self.chartMenuView.selectedItem)

        self.contentView.addSubview(self.addedMusicListView)
        self.addedMusicListView.snp.makeConstraints{ make in
            make.top.left.width.height.equalToSuperview()
        }
        self.addedMusicListView.requestViewData(nil)
        
        if self.musicListPanelHeaderView.addMusicSelected ?? true {
            self.musicListView.isHidden = false
            self.searchEntryView.isHidden = false
            self.chartMenuView.isHidden = false
            self.addedMusicListView.isHidden = true
        } else {
            self.musicListView.isHidden = true
            self.searchEntryView.isHidden = true
            self.chartMenuView.isHidden = true
            self.addedMusicListView.isHidden = false
        }
        self.musicListPanelHeaderView.clickBlock = {[weak self] sender in
            if self!.musicListPanelHeaderView.addMusicSelected ?? true {
                self!.musicListView.isHidden = false
                self!.searchEntryView.isHidden = false
                self!.chartMenuView.isHidden = false
                self!.addedMusicListView.isHidden = true
            } else {
                self!.musicListView.isHidden = true
                self!.searchEntryView.isHidden = true
                self!.chartMenuView.isHidden = true
                self!.addedMusicListView.isHidden = false
            }
        }
    }
}




