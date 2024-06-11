#
# Be sure to run `pod lib lint AUIKaraokeRoom.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'AUIKaraokeRoom'
  s.version          = '1.0.0'
  s.summary          = 'A short description of AUIKaraokeRoom.'

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://github.com/MediaBox-AUIKits/AUIKaraokeRoom'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :text => 'LICENSE' }
  s.author           = { 'aliyunvideo' => 'videosdk@service.aliyun.com' }
  s.source           = { :git => 'https://github.com/MediaBox-AUIKits/AUIKaraokeRoom', :tag =>"v#{s.version}" }
  # s.social_media_url = 'https://twitter.com/<TWITTER_USERNAME>'

  s.ios.deployment_target = '10.0'
  s.static_framework = true
  s.swift_version = '5.0'
  #s.pod_target_xcconfig = {'SWIFT_OBJC_BRIDGING_HEADER' => '${PODS_ROOT}/Headers/Public/AUIKaraokeRoom/AUIKaraokeRoom-Bridging-Header.h','GCC_PREPROCESSOR_DEFINITIONS' => '$(inherited) COCOAPODS=1'}
  s.pod_target_xcconfig = {'GCC_PREPROCESSOR_DEFINITIONS' => '$(inherited) COCOAPODS=1'}

  s.resource = 'Resources/AUIKaraokeRoom.bundle'
  s.source_files = 'Source/**/*.{swift,h,m,mm}'
  s.dependency 'AUIFoundation'
  s.dependency 'AUIMessage'
  s.dependency 'AUIRoomCore'
  s.dependency 'AUIVoiceRoom'
  s.dependency 'SDWebImage'
  s.dependency 'SnapKit'
  s.dependency 'MJRefresh'
  
end
