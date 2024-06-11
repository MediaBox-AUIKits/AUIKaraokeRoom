//
//  AUIKaraokeRoomView.swift
//  AUIKaraokeRoom
//
//  Created by aliyun on 2024/5/27.
//

import Foundation
import SnapKit

protocol AUIKaraokeRoomViewProtocol: NSObjectProtocol {
    var viewModel: AUIKaraokeRoomViewModel? { get }
    init(viewModel: AUIKaraokeRoomViewModel?)
    func initSubView()
    func updateSubViews()
}

class AUIKaraokeRoomView: UIView, AUIKaraokeRoomViewProtocol {
    var viewModel: AUIKaraokeRoomViewModel?

    required init(viewModel: AUIKaraokeRoomViewModel?) {
        self.viewModel = viewModel
        super.init(frame: .zero)
        self.viewModel?.dataDidUpdateBlock = { [weak self] in
            self?.updateSubViews()
        }
        self.initSubView()
        self.updateSubViews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func initSubView() {
    }
    
    func updateSubViews() {
    }
}
