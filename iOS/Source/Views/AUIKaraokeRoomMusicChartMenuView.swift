//
//  AUIKaraokeRoomMusicChartMenuView.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/4/8.
//

import UIKit
import SnapKit
import AUIFoundation

private class AUIKaraokeRoomMusicChartCell: UICollectionViewCell {
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.mediumFont(12)
        label.textColor = AVTheme.text_strong
        label.textAlignment = .center
        return label
    }()
    
    private var _itmeInfo: AUIKaraokeRoomChartInfo? = nil
    public var itemInfo: AUIKaraokeRoomChartInfo? {
        didSet {
            self._itmeInfo = itemInfo
            self.titleLabel.text = itemInfo?.chartName
            self.titleLabel.textColor = itemInfo?.isSelected ?? false ? AVTheme.text_strong : AVTheme.text_weak
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.contentView.addSubview(self.titleLabel)
        
        self.titleLabel.snp.makeConstraints { make in
            make.width.height.equalToSuperview()
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    static func sizeWidthWithItemInfo(_ itemInfo: AUIKaraokeRoomChartInfo?) -> CGFloat {
        if itemInfo != nil && !itemInfo!.chartName.isEmpty{
            let attributes = [NSAttributedString.Key.font: AVTheme.mediumFont(12)]
            let size = (itemInfo!.chartName as NSString).size(withAttributes: attributes)
            return size.width
        } else {
            return 0
        }
    }
}

public class AUIKaraokeRoomMusicChartMenuView: UIView {
    
    public var didChangeSelectedChart: ((_ chartInfo: AUIKaraokeRoomChartInfo) -> Void)? = nil
    
    private lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .horizontal
        let view = UICollectionView(frame: CGRect.zero, collectionViewLayout: layout)
        view.scrollsToTop = false
        view.delegate = self
        view.dataSource = self
        view.bounces = true
        view.alwaysBounceHorizontal = false
        view.showsHorizontalScrollIndicator = false
        view.backgroundColor = UIColor.clear
        view.register(AUIKaraokeRoomMusicChartCell.self, forCellWithReuseIdentifier: "AUIKaraokeRoomMusicChartCell")
        return view
    }()
    
    public var selectedItem: AUIKaraokeRoomChartInfo? {
        get {
            if self.itemList != nil && self.itemList!.count > 0 {
                for item in self.itemList! {
                    if item.isSelected {
                        return item
                    }
                }
            }
            return nil
        }
    }
    
    public var itemList:[AUIKaraokeRoomChartInfo]? = []{
        didSet{
            if self.itemList != nil && self.itemList!.count > 0 {
                self.itemList![0].isSelected = true
                self.didChangeSelectedChart?(self.itemList![0])
            }
            self.collectionView.reloadData()
        }
    }
    
    private lazy var bottomLine: UIView = {
        let view = UIView(frame: CGRect.zero)
        view.backgroundColor = AVTheme.fill_weak
        return view
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.setupSelfUI()
    }
    
    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupSelfUI() {
        self.addSubview(self.collectionView)
        self.collectionView.snp.makeConstraints { make in
            make.width.height.equalToSuperview()
        }
        self.addSubview(self.bottomLine)
        self.bottomLine.snp.makeConstraints { make in
            make.width.bottom.equalToSuperview()
            make.height.equalTo(1)
        }
    }
}

extension AUIKaraokeRoomMusicChartMenuView : UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.itemList?.count ?? 0
    }
    
    public func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = self.collectionView.dequeueReusableCell(withReuseIdentifier: "AUIKaraokeRoomMusicChartCell", for: indexPath) as! AUIKaraokeRoomMusicChartCell
        cell.itemInfo = self.itemList![indexPath.row]
        return cell
    }
    
    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        let itemInfo = self.itemList![indexPath.row]
        if itemInfo.itemSizeWidth < 1 {
            itemInfo.itemSizeWidth = AUIKaraokeRoomMusicChartCell.sizeWidthWithItemInfo(itemInfo);
        }
        return CGSize(width: itemInfo.itemSizeWidth + 16 * 2, height: collectionView.bounds.height)
    }
    
    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        return UIEdgeInsets(top: 0, left: 5, bottom: 0, right: 5)
    }
    
    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 0
    }
    
    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 0
    }
    
    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        for (index, obj) in self.itemList!.enumerated() {
            if (obj.isSelected && index != indexPath.row) {
                obj.isSelected = false
                break
            }
        }
        let itemInfo = self.itemList![indexPath.row]
        itemInfo.isSelected = true
        self.didChangeSelectedChart?(itemInfo)
        collectionView.reloadData()
        collectionView.scrollToItem(at: indexPath, at: .centeredHorizontally, animated: true)
    }
}
