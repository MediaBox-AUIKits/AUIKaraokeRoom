//
//  AUIKaraokeRoomMusicListView.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/4/10.
//

import UIKit
import SnapKit
import AUIFoundation
import MJRefresh

private class AUIKaraokeRoomMusicCellAddBtn: UIButton {
    override var isSelected: Bool {
        didSet {
            if self.isSelected {
                self.backgroundColor = .clear
            } else {
                self.backgroundColor = UIColor.av_color(withHexString: "#00BCD4")
            }
        }
    }
}

private class AUIKaraokeRoomMusicCell: UICollectionViewCell {
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.regularFont(12)
        label.textColor = AVTheme.text_strong
        label.textAlignment = .left
        return label
    }()
    
    private lazy var subTitleLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.regularFont(10)
        label.textColor = AVTheme.text_ultraweak
        label.textAlignment = .left
        return label
    }()
    
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.regularFont(8)
        label.textColor = AVTheme.text_ultraweak
        label.textAlignment = .left
        return label
    }()
    
    private lazy var mediaImageView: UIImageView = {
        let imageView = UIImageView(frame: CGRect.zero)
        imageView.backgroundColor = .clear
        imageView.layer.cornerRadius = 4
        imageView.layer.masksToBounds = true
        return imageView
    }()
    
    public lazy var addMusicBtn: AUIKaraokeRoomMusicCellAddBtn = {
        let btn = AUIKaraokeRoomMusicCellAddBtn(frame: CGRect.zero)
        btn.titleLabel?.font = AVTheme.mediumFont(12)
        btn.setTitle(AUIKaraokeRoomBundle.getString("点歌"), for: UIControl.State.normal)
        btn.setTitle(AUIKaraokeRoomBundle.getString("已点"), for: UIControl.State.selected)
        btn.setTitleColor(AVTheme.text_strong, for: UIControl.State.normal)
        btn.setTitleColor(UIColor.av_color(withHexString: "#00BCD4"), for: UIControl.State.selected)
        btn.layer.borderColor = UIColor.av_color(withHexString: "#00BCD4").cgColor
        btn.layer.cornerRadius = 12
        btn.layer.borderWidth = 1
        btn.layer.masksToBounds = true
        btn.addTarget(self, action: #selector(addMusicBtnOnClick(_:)), for: UIControl.Event.touchUpInside)
        btn.isUserInteractionEnabled = false
        return btn
    }()
    
    public var itemInfo: AUIKaraokeRoomMusicInfo? {
        didSet {
            self.titleLabel.text = self.itemInfo?.songName
            self.subTitleLabel.text = self.itemInfo?.artist
            self.descLabel.text = AUIKaraokeRoomViewModel.transToMinSec(self.itemInfo?.duration ?? 0)
            self.mediaImageView.sd_setImage(with: URL(string: self.itemInfo?.albumImg ?? ""), placeholderImage: AUIKaraokeRoomBundle.getCommonImage("ic_default_avatar"))
            self.updateAddMusicBtnDisplay()
            self.itemInfo?.addedStateUpdate = {[weak self] addedState in
                guard let self = self else { return }
                self.updateAddMusicBtnDisplay()
            }
        }
    }
    
    private func updateAddMusicBtnDisplay() {
        switch self.itemInfo?.addedState {
            case .added:
                self.addMusicBtn.isSelected = true
                break
            case .downloading:
                self.addMusicBtn.isSelected = false
                self.addMusicBtn.setTitle(AUIKaraokeRoomBundle.getString("下载中"), for: UIControl.State.normal)
                break
            case .normal:
                self.addMusicBtn.isSelected = false
                self.addMusicBtn.setTitle(AUIKaraokeRoomBundle.getString("点歌"), for: UIControl.State.normal)
                break
            default:
                break
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.contentView.addSubview(self.mediaImageView)
        self.contentView.addSubview(self.titleLabel)
        self.contentView.addSubview(self.subTitleLabel)
        self.contentView.addSubview(self.descLabel)
        self.contentView.addSubview(self.addMusicBtn)
        
        self.mediaImageView.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.width.height.equalTo(50)
            make.centerY.equalToSuperview()
        }
        
        self.addMusicBtn.snp.makeConstraints { make in
            make.width.equalTo(52)
            make.height.equalTo(24)
            make.right.equalToSuperview().offset(-30)
            make.centerY.equalToSuperview()
        }
        
        self.titleLabel.snp.makeConstraints{ make in
            make.left.equalTo(self.mediaImageView.snp.right).offset(12)
            make.top.equalTo(self.mediaImageView)
            make.right.equalTo(self.addMusicBtn.snp.left).offset(-43)
            make.height.equalTo(18)
        }
        
        self.subTitleLabel.snp.makeConstraints { make in
            make.left.equalTo(self.titleLabel)
            make.top.equalTo(self.titleLabel.snp.bottom)
            make.width.equalTo(self.titleLabel)
            make.height.equalTo(16)
        }
        
        self.descLabel.snp.makeConstraints { make in
            make.left.equalTo(self.subTitleLabel)
            make.top.equalTo(self.subTitleLabel.snp.bottom).offset(4)
            make.width.equalTo(self.subTitleLabel)
            make.height.equalTo(12)
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc private func addMusicBtnOnClick(_ sender: UIButton!) {
        sender.isSelected = !sender.isSelected
    }
}

private class AUIKaraokeRoomAddedMusicCell: UICollectionViewCell {
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.regularFont(12)
        label.textColor = AVTheme.text_strong
        label.textAlignment = .left
        return label
    }()
    
    private lazy var subTitleLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.regularFont(10)
        label.textColor = AVTheme.text_ultraweak
        label.textAlignment = .left
        return label
    }()
    
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.regularFont(8)
        label.textColor = AVTheme.text_ultraweak
        label.textAlignment = .left
        return label
    }()
    
    private lazy var mediaImageView: UIImageView = {
        let imageView = UIImageView(frame: CGRect.zero)
        imageView.backgroundColor = .clear
        imageView.layer.cornerRadius = 4
        imageView.layer.masksToBounds = true
        return imageView
    }()
    
    private lazy var cutMusicBtn: UIButton = {
        let btn = UIButton(frame: CGRect.zero)
        btn.setImage(AUIKaraokeRoomBundle.getCommonImage("ic_audio_skip_forward"), for: UIControl.State.normal)
        btn.addTarget(self, action: #selector(cutMusicBtnOnClick(_:)), for: UIControl.Event.touchUpInside)
        btn.backgroundColor = .clear
        return btn
    }()
    
    private lazy var playStatusBtn: UIButton = {
        let btn = UIButton(frame: CGRect.zero)
        btn.setImage(AUIKaraokeRoomBundle.getCommonImage("ic_audio_play_status"), for: UIControl.State.normal)
        btn.backgroundColor = .clear
        btn.isUserInteractionEnabled = false
        return btn
    }()
    
    private lazy var pinToTopBtn: UIButton = {
        let btn = UIButton(frame: CGRect.zero)
        btn.setImage(AUIKaraokeRoomBundle.getCommonImage("ic_choose_item"), for: UIControl.State.normal)
        btn.addTarget(self, action: #selector(pinToTopBtnOnClick(_:)), for: UIControl.Event.touchUpInside)
        btn.backgroundColor = .clear
        return btn
    }()
    
    private lazy var deleteBtn: UIButton = {
        let btn = UIButton(frame: CGRect.zero)
        btn.setImage(AUIKaraokeRoomBundle.getCommonImage("ic_trash_can"), for: UIControl.State.normal)
        btn.addTarget(self, action: #selector(deleteBtnOnClick(_:)), for: UIControl.Event.touchUpInside)
        btn.backgroundColor = .clear
        return btn
    }()
    
    private lazy var bottomLine: UIView = {
        let view = UIView(frame: CGRect.zero)
        view.backgroundColor = AVTheme.fill_weak
        return view
    }()
    
    public var itemInfo: AUIKaraokeRoomMusicInfo? {
        didSet {
            guard let itemInfo = self.itemInfo else { return }
            self.titleLabel.text = itemInfo.songName
            self.subTitleLabel.text = "\(itemInfo.userExtendInfo.micseatIndex + 1)号麦 \(itemInfo.userExtendInfo.userNick) 点歌"
            self.descLabel.text = "原唱 \(itemInfo.artist)"
            self.mediaImageView.sd_setImage(with: URL(string: itemInfo.albumImg), placeholderImage: AUIKaraokeRoomBundle.getCommonImage("ic_default_avatar"))
            self.cutMusicBtn.isHidden = true
            self.playStatusBtn.isHidden = true
            self.pinToTopBtn.isHidden = true
            self.deleteBtn.isHidden = true
            self.bottomLine.isHidden = true
            
            if itemInfo.itemIndex == -1 {
                self.cutMusicBtn.isHidden = false
                self.playStatusBtn.isHidden = false
                self.bottomLine.isHidden = false
            } else if (itemInfo.itemIndex == 0) {
                self.deleteBtn.isHidden = false
            } else {
                self.pinToTopBtn.isHidden = false
                self.deleteBtn.isHidden = false
            }
            
            if !itemInfo.singUserIsAnchor {
                if !itemInfo.singUserIsMe {
                    self.pinToTopBtn.isHidden = true
                    self.deleteBtn.isHidden = true
                    self.cutMusicBtn.isHidden = true
                }
            }
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.contentView.addSubview(self.mediaImageView)
        self.contentView.addSubview(self.titleLabel)
        self.contentView.addSubview(self.subTitleLabel)
        self.contentView.addSubview(self.descLabel)
        self.contentView.addSubview(self.cutMusicBtn)
        self.contentView.addSubview(self.playStatusBtn)
        self.contentView.addSubview(self.pinToTopBtn)
        self.contentView.addSubview(self.deleteBtn)
        self.contentView.addSubview(self.bottomLine)
        
        self.mediaImageView.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.width.height.equalTo(50)
            make.centerY.equalToSuperview()
        }
        
        self.playStatusBtn.snp.makeConstraints{ make in
            make.width.height.equalTo(16 + 12)
            make.right.equalTo(-18)
            make.centerY.equalToSuperview()
        }
        
        self.deleteBtn.snp.makeConstraints{ make in
            make.width.height.equalTo(self.playStatusBtn)
            make.top.left.equalTo(self.playStatusBtn)
        }
        
        self.cutMusicBtn.snp.makeConstraints { make in
            make.width.height.equalTo(self.playStatusBtn)
            make.top.equalTo(self.playStatusBtn)
            make.right.equalTo(self.playStatusBtn.snp.left)
        }
        
        self.pinToTopBtn.snp.makeConstraints{ make in
            make.width.height.equalTo(self.cutMusicBtn)
            make.top.left.equalTo(self.cutMusicBtn)
        }
        
        self.titleLabel.snp.makeConstraints{ make in
            make.left.equalTo(self.mediaImageView.snp.right).offset(12)
            make.top.equalTo(self.mediaImageView)
            make.right.equalTo(self.pinToTopBtn.snp.left).offset(-25)
            make.height.equalTo(18)
        }
        
        self.subTitleLabel.snp.makeConstraints { make in
            make.left.equalTo(self.titleLabel)
            make.top.equalTo(self.titleLabel.snp.bottom)
            make.width.equalTo(self.titleLabel)
            make.height.equalTo(16)
        }
        
        self.descLabel.snp.makeConstraints { make in
            make.left.equalTo(self.subTitleLabel)
            make.top.equalTo(self.subTitleLabel.snp.bottom).offset(4)
            make.width.equalTo(self.subTitleLabel)
            make.height.equalTo(12)
        }
        
        self.bottomLine.snp.makeConstraints { make in
            make.width.bottom.equalToSuperview()
            make.height.equalTo(1)
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public var skipMusicBlock: ((_ musicInfo: AUIKaraokeRoomMusicInfo?)->Void)? = nil
    @objc private func cutMusicBtnOnClick(_ sender: UIButton!) {
        self.skipMusicBlock?(self.itemInfo)
    }
    
    public var pinMusicBlock: ((_ musicInfo: AUIKaraokeRoomMusicInfo?)->Void)? = nil
    @objc private func pinToTopBtnOnClick(_ sender: UIButton!) {
        self.pinMusicBlock?(self.itemInfo)
    }
    
    public var deleteMusicBlock: ((_ musicInfo: AUIKaraokeRoomMusicInfo?)->Void)? = nil
    @objc private func deleteBtnOnClick(_ sender: UIButton!) {
        self.deleteMusicBlock?(self.itemInfo)
    }
}

public class AUIKaraokeRoomMusicListView: UIView {
    
    public enum MusicListType {
        case addMusic
        case addedMusic
        case search
    }
    
    private var musicListType: MusicListType? = .addMusic
    
    private var dataPage: Int = 0
    private let dataPageSize: Int = 20
    
    public var requestDataBlock: ((_ chartId: String, _ page: Int, _ pageSize: Int, _ completed: @escaping (_ musicList: [AUIKaraokeRoomMusicInfo]?, _ error: NSError?)->Void)->Void)? = nil
    private var chartInfo: AUIKaraokeRoomChartInfo? = nil
    
    public var searchMusicBlock: ((_ searchKeyword: String, _ page: Int, _ pageSize: Int, _ completed: @escaping (_ musicList: [AUIKaraokeRoomMusicInfo]?, _ error: NSError?)->Void)->Void)? = nil
    private var searchKeyword: String? = nil

    private lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .vertical
        let view = UICollectionView(frame: CGRect.zero, collectionViewLayout: layout)
        view.scrollsToTop = false
        view.delegate = self
        view.dataSource = self
        view.bounces = true
        view.alwaysBounceHorizontal = false
        view.showsHorizontalScrollIndicator = false
        view.backgroundColor = UIColor.clear
        switch self.musicListType {
        case .addedMusic:
            view.register(AUIKaraokeRoomAddedMusicCell.self, forCellWithReuseIdentifier: "AUIKaraokeRoomAddedMusicCell")
            break
        case .addMusic, .search, .none:
            view.register(AUIKaraokeRoomMusicCell.self, forCellWithReuseIdentifier: "AUIKaraokeRoomMusicCell")
        }
        return view
    }()
    
    public var itemList:[AUIKaraokeRoomMusicInfo]? = []{
        didSet{
            self.refreshViewData()
        }
    }
    
    private lazy var currentPlayView: AUIKaraokeRoomAddedMusicCell = {
        let view = AUIKaraokeRoomAddedMusicCell(frame: CGRect.zero)
        return view
    }()
    
    public var currentPlayItem: AUIKaraokeRoomMusicInfo? = nil
    
    private lazy var emptyPlaceHolder: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_ultraweak
        label.font = AVTheme.regularFont(14)
        label.textAlignment = .center
        label.text = AUIKaraokeRoomBundle.getString("还没有点播的歌曲哦")
        return label
    }()
    
    public var addMusicBlock: ((_ musicInfo: AUIKaraokeRoomMusicInfo)->Void)? = nil
    public var skipMusicBlock: ((_ musicInfo: AUIKaraokeRoomMusicInfo)->Void)? = nil
    public var pinMusicBlock: ((_ musicInfo: AUIKaraokeRoomMusicInfo)->Void)? = nil
    public var deleteMusicBlock: ((_ musicInfo: AUIKaraokeRoomMusicInfo)->Void)? = nil
    public var scrollListBlock: (()->Void)? = nil

    public init(frame: CGRect, musicListType: MusicListType) {
        super.init(frame: frame)
        self.musicListType = musicListType
        self.setupSelfUI()
    }
    
    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupSelfUI() {
        self.addSubview(self.collectionView)
        if self.musicListType == .addMusic {
            self.setupRefreshHeader()
        }
        if self.musicListType == .addMusic || self.musicListType == .search {
            self.setupLoadMoreFooter()
        }

        if self.musicListType == .addedMusic {
            self.addSubview(self.emptyPlaceHolder)
            self.sendSubviewToBack(self.emptyPlaceHolder)
            self.emptyPlaceHolder.isHidden = true
            self.emptyPlaceHolder.sizeToFit()
            self.emptyPlaceHolder.snp.makeConstraints{ make in
                make.top.equalTo(90)
                make.left.width.equalToSuperview()
            }
            
            self.addSubview(self.currentPlayView)
            self.currentPlayView.snp.makeConstraints { make in
                make.top.equalTo(12)
                make.left.width.equalToSuperview()
                make.height.equalTo(70)
            }
            
            self.collectionView.snp.makeConstraints { make in
                make.top.equalTo(self.currentPlayView.snp.bottom)
                make.left.width.bottom.equalToSuperview()
            }
        }
        else {
            self.collectionView.snp.makeConstraints { make in
                make.left.top.width.height.equalToSuperview()
            }
        }
    }
    
    private func setupRefreshHeader() {
        let header = MJRefreshNormalHeader(refreshingTarget: self, refreshingAction: #selector(refreshHeaderDo))
        header.lastUpdatedTimeLabel?.isHidden = true
        header.loadingView?.style = .gray
        header.stateLabel?.font = AVTheme.regularFont(14)
        header.stateLabel?.textColor = AVTheme.text_weak
        self.collectionView.mj_header = header
    }
    
    private func setupLoadMoreFooter() {
        let footer = MJRefreshAutoNormalFooter(refreshingTarget: self, refreshingAction: #selector(loadMoreFooterDo))
        footer.loadingView?.style = .gray
        footer.stateLabel?.font = AVTheme.regularFont(14)
        footer.stateLabel?.textColor = AVTheme.text_weak
        footer.setTitle("", for: .noMoreData)
        footer.setTitle("", for: .idle)
        self.collectionView.mj_footer = footer
    }
    
    @objc private func refreshHeaderDo() {
        self.refreshMusicList()
    }

    @objc private func loadMoreFooterDo() {
        if self.musicListType == .search {
            self.searchMoreMusic()
        } else {
            self.loadMoreMusicList()
        }
    }
    
    private func refreshMusicList() {
        if self.collectionView.mj_footer!.isRefreshing {
            self.collectionView.mj_header!.endRefreshing()
            return
        }
        if let chartInfo = self.chartInfo {
            self.requestDataBlock?(chartInfo.chartId, 1, self.dataPageSize, {[weak self] musicList, error in
                if self?.chartInfo != chartInfo {
                    return
                }
                self?.collectionView.mj_header!.endRefreshing()
                if error != nil {
                    return
                }
                self?.itemList = musicList
                if let musicList = musicList {
                    if musicList.isEmpty || musicList.count < (self?.dataPageSize ?? 0) {
//                        self?.collectionView.mj_footer!.endRefreshingWithNoMoreData()
                        self?.collectionView.mj_footer!.endRefreshing()
                    } else {
                        self?.collectionView.mj_footer!.endRefreshing()
                    }
                } else {
//                    self?.collectionView.mj_footer!.endRefreshingWithNoMoreData()
                    self?.collectionView.mj_footer!.endRefreshing()
                }
                self?.dataPage = 2
            })
        } else {
            self.collectionView.mj_header!.endRefreshing()
            self.collectionView.mj_footer!.endRefreshingWithNoMoreData()
            self.itemList = nil
            self.dataPage = 1
        }
    }
    
    private func loadMoreMusicList() {
        if self.collectionView.mj_header!.isRefreshing {
            self.collectionView.mj_footer!.endRefreshing()
            return
        }
        
        if self.dataPage == 1 {
            self.collectionView.mj_footer?.endRefreshing()
            return
        }
        
        if let chartInfo = self.chartInfo {
            self.requestDataBlock?(chartInfo.chartId, self.dataPage, self.dataPageSize, {[weak self] musicList, error in
                if self?.chartInfo != chartInfo {
                    return
                }
                self?.collectionView.mj_footer!.endRefreshing()
                if error != nil {
                    return
                }
                if let musicList = musicList {
                    self?.itemList?.append(contentsOf: musicList)
                    self?.dataPage += 1
                    if musicList.isEmpty || musicList.count < (self?.dataPageSize ?? 0) {
//                        self?.collectionView.mj_footer?.endRefreshingWithNoMoreData()
                        self?.collectionView.mj_footer!.endRefreshing()
                    }
                } else {
//                    self?.collectionView.mj_footer?.endRefreshingWithNoMoreData()
                    self?.collectionView.mj_footer!.endRefreshing()
                }
            })
        }
    }
    
    public func requestViewData(_ chartInfo: AUIKaraokeRoomChartInfo?) {
        self.collectionView.mj_header?.endRefreshing()
        self.collectionView.mj_footer?.endRefreshingWithNoMoreData()
        if (chartInfo != nil && self.chartInfo != nil && !(chartInfo!.chartId.isEqual(self.chartInfo!.chartId)))
            || (chartInfo == nil || self.chartInfo == nil) {
            self.itemList = nil
        }
        self.chartInfo = chartInfo
        self.collectionView.mj_header?.beginRefreshing()
    }
    
    public func updateMusicInfos(musicInfos: [AUIKaraokeRoomMusicInfo]?) {
        self.itemList = musicInfos
        self.parseCurrentPlayItem()
    }
    
    private func parseCurrentPlayItem() {
        if (self.musicListType == .addedMusic) {
            if self.itemList?.count ?? 0 > 0 {
                self.currentPlayItem = self.itemList?[0]
                self.currentPlayItem?.itemIndex = -1
                self.itemList?.removeFirst()
            } else {
                self.currentPlayItem = nil
            }
            self.refreshViewData()
        }
    }
    
    private func refreshViewData() {
        self.collectionView.reloadData()
        
        if self.musicListType == .addedMusic {
            if self.currentPlayItem != nil {
                self.currentPlayView.isHidden = false
                self.currentPlayView.itemInfo = self.currentPlayItem
                self.currentPlayView.skipMusicBlock = {[weak self] itemInfo in
                    if let itemInfo = itemInfo {
                        self?.skipMusicBlock?(itemInfo)
                    }
                }
            } else {
                self.currentPlayView.isHidden = true
            }
            if self.itemList?.count ?? 0 > 0 {
                self.collectionView.isHidden = false
            } else {
                self.collectionView.isHidden = true
            }
            self.emptyPlaceHolder.isHidden = !(self.collectionView.isHidden && self.currentPlayView.isHidden)
        }
    }
}

extension AUIKaraokeRoomMusicListView : UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.itemList?.count ?? 0
    }
    
    public func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        switch self.musicListType {
        case .addedMusic:
            let cell = self.collectionView.dequeueReusableCell(withReuseIdentifier: "AUIKaraokeRoomAddedMusicCell", for: indexPath) as! AUIKaraokeRoomAddedMusicCell
            let itemInfo = self.itemList![indexPath.row]
            itemInfo.itemIndex = indexPath.row
            cell.itemInfo = itemInfo
            cell.deleteMusicBlock = {[weak self] itemInfo in
                guard let self = self else { return }
                if let itemInfo = itemInfo {
                    self.deleteMusicBlock?(itemInfo)
                }
            }
            cell.pinMusicBlock = {[weak self] itemInfo in
                guard let self = self else { return }
                if let itemInfo = itemInfo {
                    self.pinMusicBlock?(itemInfo)
                }
            }
            return cell
        case .addMusic, .search, .none:
            let cell = self.collectionView.dequeueReusableCell(withReuseIdentifier: "AUIKaraokeRoomMusicCell", for: indexPath) as! AUIKaraokeRoomMusicCell
            cell.itemInfo = self.itemList![indexPath.row]
            return cell
        }
    }
    
    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: collectionView.bounds.size.width, height: 70)
    }
    
    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        return UIEdgeInsets(top: 12, left: 0, bottom: 0, right: 0)
    }
    
    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 0
    }
    
    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 0
    }
    
    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        switch self.musicListType {
        case .addedMusic:
            break
        case .addMusic, .search, .none:
            let itemInfo = self.itemList![indexPath.row]
            if itemInfo.addedState == .normal {
                self.addMusicBlock?(itemInfo)
            }
        }
    }
    
    public func scrollViewWillBeginDragging(_ scrollView: UIScrollView) {
        self.scrollListBlock?()
    }
}

extension AUIKaraokeRoomMusicListView {
        
    public func searchMusicWithKeyword(_ keyword: String?) {
        self.collectionView.mj_footer?.endRefreshingWithNoMoreData()
        if !(keyword != nil
            && self.searchKeyword != nil
            && (keyword!.isEqual(self.searchKeyword!))){
            self.itemList = nil
        }
        self.searchKeyword = keyword
        
        if self.searchKeyword != nil && !self.searchKeyword!.isEmpty {
            self.searchMusicBlock?(keyword!, 1, self.dataPageSize, {[weak self] musicList, error in
                if !keyword!.isEqual(self?.searchKeyword) {
                    return
                }
                if error != nil {
                    return
                }
                self?.itemList = musicList
                if let musicList = musicList {
                    if musicList.isEmpty || musicList.count < (self?.dataPageSize ?? 0) {
//                        self?.collectionView.mj_footer!.endRefreshingWithNoMoreData()
                        self?.collectionView.mj_footer!.endRefreshing()
                    } else {
                        self?.collectionView.mj_footer!.endRefreshing()
                    }
                } else {
//                    self?.collectionView.mj_footer!.endRefreshingWithNoMoreData()
                    self?.collectionView.mj_footer!.endRefreshing()
                }
                self?.dataPage = 2
            })
        } else {
            self.dataPage = 1
        }
    }
    
    private func searchMoreMusic() {
        if self.dataPage == 1 {
            self.collectionView.mj_footer?.endRefreshing()
            return
        }
        
        if let searchKeyword = self.searchKeyword {
            self.searchMusicBlock?(searchKeyword, self.dataPage, self.dataPageSize, {[weak self] musicList, error in
                if !searchKeyword.isEqual(self?.searchKeyword) {
                    return
                }
                self?.collectionView.mj_footer!.endRefreshing()
                if error != nil {
                    return
                }
                if let musicList = musicList {
                    self?.itemList?.append(contentsOf: musicList)
                    self?.dataPage += 1
                    if musicList.isEmpty || musicList.count < (self?.dataPageSize ?? 0) {
//                        self?.collectionView.mj_footer?.endRefreshingWithNoMoreData()
                        self?.collectionView.mj_footer!.endRefreshing()
                    }
                } else {
//                    self?.collectionView.mj_footer?.endRefreshingWithNoMoreData()
                    self?.collectionView.mj_footer!.endRefreshing()
                }
            })
        }
    }
}
