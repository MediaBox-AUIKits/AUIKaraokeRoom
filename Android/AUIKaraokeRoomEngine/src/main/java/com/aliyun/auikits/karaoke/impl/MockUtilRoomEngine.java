//package com.aliyun.auikits.ktvroom.impl;
//
//import android.content.Context;
//import android.content.Intent;
//import android.net.Uri;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.alivc.rtc.AliMusicContentCenter;
//import com.alivc.rtc.AliMusicContentCenterEventListener;
//import com.aliyun.auikits.ktvroom.bean.KTVChartInfo;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class MockUtilRoomEngine {
//    public static boolean isMockEnable() {
//        return false;
//    }
//
//    public static List<KTVChartInfo> mockKTVChartInfoLIst(int count) {
//        List<KTVChartInfo> chartInfoList = new ArrayList<>();
//
//        for (int i = 0; i < count; i++) {
//            chartInfoList.add(mockKTVChartInfo(i));
//        }
//
//        return chartInfoList;
//    }
//
//    public static KTVChartInfo mockKTVChartInfo(int index) {
//        KTVChartInfo ktvChartInfo = new KTVChartInfo();
//
//        ktvChartInfo.chartId = "chartid" + index;
//        ktvChartInfo.chartName = "热门榜单"+index;
//
//        return ktvChartInfo;
//    }
//
//    public static List<AliMusicContentCenter.MusicInfo> mockMusicInfoList(int count) {
//        List<AliMusicContentCenter.MusicInfo> musicInfoList = new ArrayList<>();
//
//        for (int i = 0; i < count; i++) {
//            musicInfoList.add(mockMusicInfo(""));
//        }
//        return musicInfoList;
//    }
//
//    static int sSongIdIndex = 1;
//    public static AliMusicContentCenter.MusicInfo mockMusicInfo(String songId) {
//        AliMusicContentCenter.MusicInfo musicInfo;
//        final String songIdPrefix = "songid";
//        int index = 1;
//        if (!TextUtils.isEmpty(songId)) {
//            index = Integer.parseInt(songId.replace(songIdPrefix, ""));
//        } else {
//            index = sSongIdIndex;
//            sSongIdIndex++;
//        }
//        musicInfo = new AliMusicContentCenter.MusicInfo();
//        musicInfo.songID = "songid" + index;
//        musicInfo.songName = "后来" + index;
//        musicInfo.singerName = "刘若英";
//        musicInfo.albumImg = "https://image.uisdc.com/wp-content/uploads/2018/12/uisdc-jl-20181224-36.jpg";
//        musicInfo.duration = 309000;
//        musicInfo.accompanyDuration = 309000;
//        musicInfo.lyricType = AliMusicContentCenter.LyricType.LyricKrc;
//        return musicInfo;
//    }
//
//    public static String mockLyric() {
//        return "[ar:刘若英]\n" +
//                "[ti:后来]\n" +
//                "[0,2364](0,1442)后(1442,201)来 (1643,181)（Later） - (1824,180)刘(2004,181)若(2185,179)英\n" +
//                "[2364,920](2364,180)词：(2544,181)玉(2725,180)城(2905,180)千(3085,200)春\n" +
//                "[3284,907](3284,200)曲：(3484,180)玉(3664,160)城(3824,160)千(3984,207)春\n" +
//                "[4191,1746](4191,199)改(4390,200)编(4590,180)词：(4770,342)施(5112,324)人(5436,500)诚\n" +
//                "[12062,6145](12062,701)后(12763,1101)来 (13864,299)我(14163,360)总(14523,320)算(14843,400)学(15243,500)会(15743,1020)了(16763,340)如(17103,360)何(17463,340)去(17803,404)爱\n" +
//                "[18207,5629](18207,244)可(18451,380)惜(18831,900)你(19731,221)早(19952,240)已(20192,279)远(20471,621)去(21092,384)消(21476,380)失(21856,421)在(22277,479)人(22756,1080)海\n" +
//                "[24275,6306](24275,298)后(24573,1541)来(26114,340)终(26454,360)于(26814,421)在(27235,239)眼(27474,520)泪(27994,1240)中(29234,445)明(29679,902)白\n" +
//                "[30581,5520](30581,213)有(30794,318)些(31112,1000)人(32112,200)一(32312,340)旦(32652,380)错(33032,381)过(33413,361)就(33774,440)不(34214,1888)再\n" +
//                "[38161,3518](38161,308)栀(38469,253)子(38722,809)花(39531,284)白(39815,484)花(40299,1380)瓣\n" +
//                "[43961,4729](43961,374)落(44335,488)在(44823,520)我(45343,380)蓝(45723,376)色(46099,331)百(46430,429)褶(46859,420)裙(47279,1411)上\n" +
//                "[49711,5920](49711,1521)爱(51232,1160)你 (52392,400)你(52792,1119)轻(53911,420)声(54331,1300)说\n" +
//                "[56530,4588](56530,374)我(56904,351)低(57255,380)下(57635,639)头(58274,301)闻(58575,259)见(58834,244)一(59078,512)阵(59590,460)芬(60050,1068)芳\n" +
//                "[62538,3660](62538,358)那(62896,429)个(63325,459)永(63784,405)恒(64189,414)的(64603,488)夜(65091,1107)晚\n" +
//                "[66198,6955](66198,438)十(66636,484)七(67120,400)岁(67520,545)仲(68065,1120)夏 (69185,200)你(69385,374)吻(69759,372)我(70131,660)的(70791,200)那(70991,480)个(71471,481)夜(71952,1202)晚\n" +
//                "[74974,6197](74974,256)让(75230,482)我(75712,199)往(75911,460)后(76371,343)的(76714,620)时(77334,1076)光 (78410,381)每(78791,361)当(79152,503)有(79655,745)感(80400,771)叹\n" +
//                "[81171,4380](81171,464)总(81635,226)想(81861,837)起(82698,247)当(82945,320)天(83265,368)的(83633,647)星(84280,1272)光\n" +
//                "[87352,3655](87352,231)那(87583,438)时(88021,680)候(88701,361)的(89062,500)爱(89562,1444)情\n" +
//                "[93162,4542](93162,448)为(93610,481)什(94091,420)么(94511,419)就(94930,342)能(95272,297)那(95569,491)样(96060,532)简(96592,1112)单\n" +
//                "[98700,6140](98700,350)而(99050,247)又(99297,340)是(99637,233)为(99870,520)什(100390,1170)么 (101560,440)人(102000,880)年(102880,614)少(103494,1346)时\n" +
//                "[105466,4916](105466,489)一(105955,420)定(106375,261)要(106636,559)让(107195,382)深(107577,400)爱(107977,344)的(108321,401)人(108722,502)受(109224,1158)伤\n" +
//                "[111061,4248](111061,674)在(111735,407)这(112142,199)相(112341,403)似(112744,403)的(113147,481)深(113628,641)夜(114269,1040)里\n" +
//                "[115769,6862](115769,261)你(116030,403)是(116433,400)否(116833,438)一(117271,1087)样 (118358,216)也(118574,364)在(118938,554)静(119492,499)静(119991,220)追(120211,480)悔(120691,620)感(121311,1320)伤\n" +
//                "[124053,6674](124053,362)如(124415,465)果(124880,201)当(125081,460)时(125541,339)我(125880,742)们(126622,1119)能 (127741,385)不(128126,209)那(128335,560)么(128895,740)倔(129635,1092)强\n" +
//                "[130727,4316](130727,197)现(130924,251)在(131175,726)也(131901,219)不(132120,311)那(132431,286)么(132717,715)遗(133432,1611)憾\n" +
//                "[135517,2789](135517,188)你(135705,389)都(136094,308)如(136402,359)何(136761,354)回(137115,439)忆(137554,752)我\n" +
//                "[138306,3096](138306,342)带(138648,246)着(138894,265)笑(139159,240)或(139399,320)是(139719,341)很(140060,380)沉(140440,962)默\n" +
//                "[141402,5869](141402,288)这(141690,311)些(142001,239)年(142240,825)来(143065,215)有(143280,205)没(143485,255)有(143740,259)人(143999,440)能(144439,311)让(144750,409)你(145159,311)不(145470,401)寂(145871,1399)寞\n" +
//                "[147271,6676](147271,1018)后(148289,996)来 (149285,296)我(149581,290)总(149871,465)算(150336,302)学(150638,613)会(151251,1188)了(152439,345)如(152784,462)何(153246,281)去(153527,420)爱\n" +
//                "[153947,5491](153947,221)可(154168,249)惜(154417,841)你(155258,200)早(155458,340)已(155798,195)远(155993,767)去(156760,334)消(157094,326)失(157420,395)在(157815,462)人(158277,1161)海\n" +
//                "[159808,6442](159808,716)后(160524,1221)来 (161745,233)终(161978,420)于(162398,399)在(162797,200)眼(162997,571)泪(163568,1200)中(164768,464)明(165232,1018)白\n" +
//                "[166250,6036](166250,201)有(166451,199)些(166650,1051)人(167701,210)一(167911,367)旦(168278,199)错(168477,457)过(168934,395)就(169329,390)不(169719,2566)再\n" +
//                "[197215,2681](197215,211)你(197426,239)都(197665,236)如(197901,379)何(198280,295)回(198575,506)忆(199081,814)我\n" +
//                "[199896,3066](199896,213)带(200109,316)着(200425,221)笑(200646,225)或(200871,344)是(201215,327)很(201542,440)沉(201982,980)默\n" +
//                "[202962,5868](202962,185)这(203147,212)些(203359,303)年(203662,901)来(204563,192)有(204755,210)没(204965,249)有(205214,220)人(205434,477)能(205911,384)让(206295,399)你(206694,321)不(207015,415)寂(207430,1400)寞\n" +
//                "[208830,6500](208830,1028)后(209858,993)来 (210851,327)我(211178,320)总(211498,421)算(211919,216)学(212135,695)会(212830,1082)了(213912,278)如(214190,400)何(214590,350)去(214940,390)爱\n" +
//                "[215330,5539](215330,260)可(215590,367)惜(215957,940)你(216897,215)早(217112,207)已(217319,296)远(217615,605)去(218220,259)消(218479,375)失(218854,385)在(219239,536)人(219775,1094)海\n" +
//                "[221269,6385](221269,907)后(222176,1040)来 (223216,215)终(223431,430)于(223861,432)在(224293,219)眼(224512,519)泪(225031,1271)中(226302,444)明(226746,908)白\n" +
//                "[227654,5773](227654,217)有(227871,306)些(228177,1038)人(229215,214)一(229429,334)旦(229763,195)错(229958,506)过(230464,327)就(230791,421)不(231212,2216)再\n" +
//                "[233427,6534](233427,1023)后(234450,953)来 (235403,287)我(235690,416)总(236106,385)算(236491,215)学(236706,672)会(237378,1120)了(238498,233)如(238731,424)何(239155,406)去(239561,400)爱\n" +
//                "[239961,5439](239961,235)可(240196,362)惜(240558,917)你(241475,185)早(241660,220)已(241880,322)远(242202,613)去(242815,228)消(243043,453)失(243496,420)在(243916,536)人(244452,948)海\n" +
//                "[245859,6369](245859,847)后(246706,1071)来 (247777,220)终(247997,452)于(248449,420)在(248869,241)眼(249110,573)泪(249683,1200)中(250883,464)明(251347,881)白\n" +
//                "[252228,5459](252228,213)有(252441,331)些(252772,1080)人(253852,200)一(254052,290)旦(254342,213)错(254555,460)过(255015,333)就(255348,521)不(255869,1818)再\n" +
//                "[258874,5869](258874,1172)永(260046,493)远(260539,712)不(261251,446)会(261697,386)再(262083,741)重(262824,1919)来\n" +
//                "[264743,13317](264743,209)有(264952,269)一(265221,287)个(265508,385)男(265893,863)孩(266756,348)爱(267104,715)着(267819,433)那(268252,4568)个(272820,647)女(273467,4593)孩\n";
//    }
//
//    public static String mockPitch() {
//        return "{\"midiList\":[{\"start\":11.9,\"end\":12.24,\"pitch\":66,\"velocity\":100},{\"start\":12.24,\"end\":12.66,\"pitch\":64,\"velocity\":100},{\"start\":12.66,\"end\":13.28,\"pitch\":62,\"velocity\":100},{\"start\":13.86,\"end\":14.2,\"pitch\":62,\"velocity\":100},{\"start\":14.24,\"end\":14.58,\"pitch\":62,\"velocity\":100},{\"start\":14.58,\"end\":14.96,\"pitch\":64,\"velocity\":100},{\"start\":14.96,\"end\":15.36,\"pitch\":66,\"velocity\":100},{\"start\":15.36,\"end\":15.74,\"pitch\":67,\"velocity\":100},{\"start\":15.74,\"end\":16.36,\"pitch\":66,\"velocity\":100},{\"start\":16.88,\"end\":17.22,\"pitch\":67,\"velocity\":100},{\"start\":17.22,\"end\":17.62,\"pitch\":64,\"velocity\":100},{\"start\":17.62,\"end\":17.86,\"pitch\":62,\"velocity\":100},{\"start\":17.86,\"end\":18.36,\"pitch\":64,\"velocity\":100},{\"start\":18.36,\"end\":19.34,\"pitch\":62,\"velocity\":100},{\"start\":19.8,\"end\":20.18,\"pitch\":59,\"velocity\":100},{\"start\":20.18,\"end\":20.4,\"pitch\":61,\"velocity\":100},{\"start\":20.4,\"end\":20.88,\"pitch\":62,\"velocity\":100},{\"start\":21.22,\"end\":21.56,\"pitch\":62,\"velocity\":100},{\"start\":21.56,\"end\":21.92,\"pitch\":64,\"velocity\":100},{\"start\":21.92,\"end\":22.34,\"pitch\":66,\"velocity\":100},{\"start\":22.34,\"end\":22.68,\"pitch\":61,\"velocity\":100},{\"start\":22.68,\"end\":23.34,\"pitch\":62,\"velocity\":100},{\"start\":24.22,\"end\":24.62,\"pitch\":62,\"velocity\":100},{\"start\":24.62,\"end\":24.96,\"pitch\":61,\"velocity\":100},{\"start\":24.96,\"end\":25.7,\"pitch\":59,\"velocity\":100},{\"start\":26.14,\"end\":26.52,\"pitch\":71,\"velocity\":100},{\"start\":26.52,\"end\":26.9,\"pitch\":69,\"velocity\":100},{\"start\":26.9,\"end\":27.28,\"pitch\":67,\"velocity\":100},{\"start\":27.28,\"end\":27.7,\"pitch\":66,\"velocity\":100},{\"start\":27.7,\"end\":28.04,\"pitch\":67,\"velocity\":100},{\"start\":28.04,\"end\":29.1,\"pitch\":69,\"velocity\":100},{\"start\":29.2,\"end\":29.56,\"pitch\":61,\"velocity\":100},{\"start\":29.56,\"end\":30.08,\"pitch\":62,\"velocity\":100},{\"start\":30.62,\"end\":30.94,\"pitch\":66,\"velocity\":100},{\"start\":30.98,\"end\":31.18,\"pitch\":66,\"velocity\":100},{\"start\":31.18,\"end\":31.64,\"pitch\":67,\"velocity\":100},{\"start\":32.24,\"end\":32.42,\"pitch\":67,\"velocity\":100},{\"start\":32.46,\"end\":32.62,\"pitch\":67,\"velocity\":100},{\"start\":32.62,\"end\":33.04,\"pitch\":66,\"velocity\":100},{\"start\":33.04,\"end\":33.42,\"pitch\":64,\"velocity\":100},{\"start\":33.42,\"end\":33.8,\"pitch\":62,\"velocity\":100},{\"start\":33.8,\"end\":34.1,\"pitch\":61,\"velocity\":100},{\"start\":34.1,\"end\":34.34,\"pitch\":64,\"velocity\":100},{\"start\":34.34,\"end\":35.4,\"pitch\":62,\"velocity\":100},{\"start\":38.06,\"end\":38.44,\"pitch\":62,\"velocity\":100},{\"start\":38.44,\"end\":38.68,\"pitch\":61,\"velocity\":100},{\"start\":38.68,\"end\":39.34,\"pitch\":62,\"velocity\":100},{\"start\":39.62,\"end\":39.98,\"pitch\":54,\"velocity\":100},{\"start\":39.98,\"end\":40.26,\"pitch\":55,\"velocity\":100},{\"start\":40.26,\"end\":40.92,\"pitch\":57,\"velocity\":100},{\"start\":44.24,\"end\":44.58,\"pitch\":59,\"velocity\":100},{\"start\":44.62,\"end\":44.82,\"pitch\":59,\"velocity\":100},{\"start\":44.86,\"end\":45.36,\"pitch\":59,\"velocity\":100},{\"start\":45.36,\"end\":45.76,\"pitch\":57,\"velocity\":100},{\"start\":45.76,\"end\":46.1,\"pitch\":55,\"velocity\":100},{\"start\":46.1,\"end\":46.54,\"pitch\":59,\"velocity\":100},{\"start\":46.54,\"end\":46.86,\"pitch\":56,\"velocity\":100},{\"start\":46.86,\"end\":47.22,\"pitch\":52,\"velocity\":100},{\"start\":47.22,\"end\":48.1,\"pitch\":54,\"velocity\":100},{\"start\":49.66,\"end\":50.68,\"pitch\":55,\"velocity\":100},{\"start\":50.68,\"end\":51.82,\"pitch\":57,\"velocity\":100},{\"start\":52.32,\"end\":52.7,\"pitch\":54,\"velocity\":100},{\"start\":52.7,\"end\":53.56,\"pitch\":57,\"velocity\":100},{\"start\":53.82,\"end\":54.22,\"pitch\":59,\"velocity\":100},{\"start\":54.26,\"end\":55.22,\"pitch\":59,\"velocity\":100},{\"start\":56.56,\"end\":56.92,\"pitch\":55,\"velocity\":100},{\"start\":56.92,\"end\":57.3,\"pitch\":59,\"velocity\":100},{\"start\":57.34,\"end\":57.68,\"pitch\":59,\"velocity\":100},{\"start\":57.68,\"end\":58.16,\"pitch\":62,\"velocity\":100},{\"start\":58.46,\"end\":58.62,\"pitch\":62,\"velocity\":100},{\"start\":58.62,\"end\":58.84,\"pitch\":64,\"velocity\":100},{\"start\":58.84,\"end\":59.18,\"pitch\":61,\"velocity\":100},{\"start\":59.18,\"end\":59.56,\"pitch\":59,\"velocity\":100},{\"start\":59.6,\"end\":59.98,\"pitch\":59,\"velocity\":100},{\"start\":59.98,\"end\":60.66,\"pitch\":57,\"velocity\":100},{\"start\":62.7,\"end\":63.08,\"pitch\":62,\"velocity\":100},{\"start\":63.08,\"end\":63.3,\"pitch\":61,\"velocity\":100},{\"start\":63.3,\"end\":63.84,\"pitch\":62,\"velocity\":100},{\"start\":63.88,\"end\":64.22,\"pitch\":62,\"velocity\":100},{\"start\":64.22,\"end\":64.62,\"pitch\":54,\"velocity\":100},{\"start\":64.66,\"end\":65,\"pitch\":54,\"velocity\":100},{\"start\":65,\"end\":65.84,\"pitch\":57,\"velocity\":100},{\"start\":66.54,\"end\":66.86,\"pitch\":59,\"velocity\":100},{\"start\":66.86,\"end\":67.28,\"pitch\":57,\"velocity\":100},{\"start\":67.28,\"end\":67.68,\"pitch\":55,\"velocity\":100},{\"start\":67.68,\"end\":68.06,\"pitch\":54,\"velocity\":100},{\"start\":68.06,\"end\":68.58,\"pitch\":55,\"velocity\":100},{\"start\":69.24,\"end\":69.62,\"pitch\":55,\"velocity\":100},{\"start\":69.62,\"end\":70.42,\"pitch\":58,\"velocity\":100},{\"start\":70.74,\"end\":71.12,\"pitch\":57,\"velocity\":100},{\"start\":71.12,\"end\":71.42,\"pitch\":55,\"velocity\":100},{\"start\":71.42,\"end\":72.42,\"pitch\":57,\"velocity\":100},{\"start\":75.02,\"end\":75.4,\"pitch\":59,\"velocity\":100},{\"start\":75.4,\"end\":75.78,\"pitch\":61,\"velocity\":100},{\"start\":75.78,\"end\":76.16,\"pitch\":62,\"velocity\":100},{\"start\":76.2,\"end\":76.54,\"pitch\":62,\"velocity\":100},{\"start\":76.58,\"end\":76.92,\"pitch\":62,\"velocity\":100},{\"start\":76.92,\"end\":77.3,\"pitch\":64,\"velocity\":100},{\"start\":77.3,\"end\":78,\"pitch\":66,\"velocity\":100},{\"start\":78.48,\"end\":78.78,\"pitch\":66,\"velocity\":100},{\"start\":78.78,\"end\":79.18,\"pitch\":67,\"velocity\":100},{\"start\":79.18,\"end\":79.56,\"pitch\":66,\"velocity\":100},{\"start\":79.56,\"end\":79.84,\"pitch\":64,\"velocity\":100},{\"start\":79.84,\"end\":80.14,\"pitch\":62,\"velocity\":100},{\"start\":80.14,\"end\":80.88,\"pitch\":64,\"velocity\":100},{\"start\":81.4,\"end\":81.68,\"pitch\":59,\"velocity\":100},{\"start\":81.72,\"end\":81.94,\"pitch\":59,\"velocity\":100},{\"start\":81.94,\"end\":82.36,\"pitch\":62,\"velocity\":100},{\"start\":82.7,\"end\":83.02,\"pitch\":61,\"velocity\":100},{\"start\":83.08,\"end\":83.28,\"pitch\":62,\"velocity\":100},{\"start\":83.28,\"end\":83.6,\"pitch\":64,\"velocity\":100},{\"start\":83.64,\"end\":84.08,\"pitch\":64,\"velocity\":100},{\"start\":84.08,\"end\":85.2,\"pitch\":62,\"velocity\":100},{\"start\":87.36,\"end\":87.68,\"pitch\":62,\"velocity\":100},{\"start\":87.68,\"end\":87.94,\"pitch\":61,\"velocity\":100},{\"start\":87.94,\"end\":88.74,\"pitch\":62,\"velocity\":100},{\"start\":88.84,\"end\":89.2,\"pitch\":54,\"velocity\":100},{\"start\":89.2,\"end\":89.62,\"pitch\":55,\"velocity\":100},{\"start\":89.62,\"end\":90.5,\"pitch\":57,\"velocity\":100},{\"start\":93.48,\"end\":93.82,\"pitch\":59,\"velocity\":100},{\"start\":93.86,\"end\":94.02,\"pitch\":59,\"velocity\":100},{\"start\":94.06,\"end\":94.5,\"pitch\":59,\"velocity\":100},{\"start\":94.6,\"end\":95,\"pitch\":57,\"velocity\":100},{\"start\":95,\"end\":95.36,\"pitch\":55,\"velocity\":100},{\"start\":95.36,\"end\":95.76,\"pitch\":59,\"velocity\":100},{\"start\":95.76,\"end\":96.14,\"pitch\":57,\"velocity\":100},{\"start\":96.14,\"end\":96.54,\"pitch\":52,\"velocity\":100},{\"start\":96.58,\"end\":96.74,\"pitch\":55,\"velocity\":100},{\"start\":96.74,\"end\":97.2,\"pitch\":54,\"velocity\":100},{\"start\":98.48,\"end\":98.84,\"pitch\":55,\"velocity\":100},{\"start\":98.88,\"end\":99.24,\"pitch\":55,\"velocity\":100},{\"start\":99.28,\"end\":100,\"pitch\":55,\"velocity\":100},{\"start\":100,\"end\":100.38,\"pitch\":57,\"velocity\":100},{\"start\":100.42,\"end\":100.96,\"pitch\":57,\"velocity\":100},{\"start\":101.56,\"end\":101.62,\"pitch\":55,\"velocity\":100},{\"start\":101.62,\"end\":101.9,\"pitch\":54,\"velocity\":100},{\"start\":101.94,\"end\":102.72,\"pitch\":57,\"velocity\":100},{\"start\":103.04,\"end\":103.44,\"pitch\":59,\"velocity\":100},{\"start\":103.48,\"end\":104.32,\"pitch\":59,\"velocity\":100},{\"start\":105.78,\"end\":106.14,\"pitch\":55,\"velocity\":100},{\"start\":106.14,\"end\":106.52,\"pitch\":59,\"velocity\":100},{\"start\":106.56,\"end\":106.94,\"pitch\":59,\"velocity\":100},{\"start\":106.94,\"end\":107.3,\"pitch\":62,\"velocity\":100},{\"start\":107.34,\"end\":107.68,\"pitch\":62,\"velocity\":100},{\"start\":107.68,\"end\":108.02,\"pitch\":64,\"velocity\":100},{\"start\":108.02,\"end\":108.44,\"pitch\":61,\"velocity\":100},{\"start\":108.44,\"end\":108.84,\"pitch\":59,\"velocity\":100},{\"start\":108.88,\"end\":109.2,\"pitch\":59,\"velocity\":100},{\"start\":109.2,\"end\":109.82,\"pitch\":57,\"velocity\":100},{\"start\":111.56,\"end\":111.92,\"pitch\":62,\"velocity\":100},{\"start\":111.96,\"end\":112.3,\"pitch\":62,\"velocity\":100},{\"start\":112.3,\"end\":112.68,\"pitch\":61,\"velocity\":100},{\"start\":112.68,\"end\":113.08,\"pitch\":62,\"velocity\":100},{\"start\":113.12,\"end\":113.46,\"pitch\":62,\"velocity\":100},{\"start\":113.46,\"end\":113.86,\"pitch\":54,\"velocity\":100},{\"start\":113.86,\"end\":114.24,\"pitch\":55,\"velocity\":100},{\"start\":114.24,\"end\":115.1,\"pitch\":57,\"velocity\":100},{\"start\":115.78,\"end\":116.16,\"pitch\":59,\"velocity\":100},{\"start\":116.16,\"end\":116.5,\"pitch\":57,\"velocity\":100},{\"start\":116.5,\"end\":116.92,\"pitch\":55,\"velocity\":100},{\"start\":116.92,\"end\":117.28,\"pitch\":54,\"velocity\":100},{\"start\":117.32,\"end\":117.94,\"pitch\":55,\"velocity\":100},{\"start\":118.44,\"end\":118.6,\"pitch\":54,\"velocity\":100},{\"start\":118.6,\"end\":118.92,\"pitch\":55,\"velocity\":100},{\"start\":118.92,\"end\":120,\"pitch\":58,\"velocity\":100},{\"start\":120,\"end\":120.38,\"pitch\":57,\"velocity\":100},{\"start\":120.38,\"end\":120.74,\"pitch\":55,\"velocity\":100},{\"start\":120.74,\"end\":121.2,\"pitch\":58,\"velocity\":100},{\"start\":121.2,\"end\":122.18,\"pitch\":57,\"velocity\":100},{\"start\":124.24,\"end\":124.62,\"pitch\":59,\"velocity\":100},{\"start\":124.62,\"end\":125,\"pitch\":61,\"velocity\":100},{\"start\":125,\"end\":125.42,\"pitch\":62,\"velocity\":100},{\"start\":125.46,\"end\":125.78,\"pitch\":62,\"velocity\":100},{\"start\":125.82,\"end\":126.16,\"pitch\":62,\"velocity\":100},{\"start\":126.16,\"end\":126.56,\"pitch\":64,\"velocity\":100},{\"start\":126.56,\"end\":126.6,\"pitch\":66,\"velocity\":100},{\"start\":126.6,\"end\":126.7,\"pitch\":67,\"velocity\":100},{\"start\":126.7,\"end\":127.1,\"pitch\":66,\"velocity\":100},{\"start\":127.72,\"end\":128.06,\"pitch\":66,\"velocity\":100},{\"start\":128.06,\"end\":128.4,\"pitch\":67,\"velocity\":100},{\"start\":128.4,\"end\":128.82,\"pitch\":66,\"velocity\":100},{\"start\":128.82,\"end\":129.2,\"pitch\":64,\"velocity\":100},{\"start\":129.2,\"end\":129.56,\"pitch\":62,\"velocity\":100},{\"start\":129.56,\"end\":130.16,\"pitch\":64,\"velocity\":100},{\"start\":130.66,\"end\":131.18,\"pitch\":59,\"velocity\":100},{\"start\":131.18,\"end\":131.58,\"pitch\":61,\"velocity\":100},{\"start\":131.92,\"end\":132.26,\"pitch\":61,\"velocity\":100},{\"start\":132.26,\"end\":132.48,\"pitch\":62,\"velocity\":100},{\"start\":132.48,\"end\":132.68,\"pitch\":64,\"velocity\":100},{\"start\":132.72,\"end\":133.2,\"pitch\":64,\"velocity\":100},{\"start\":133.2,\"end\":134.52,\"pitch\":62,\"velocity\":100},{\"start\":135.4,\"end\":135.54,\"pitch\":59,\"velocity\":100},{\"start\":135.58,\"end\":135.76,\"pitch\":59,\"velocity\":100},{\"start\":135.76,\"end\":136.14,\"pitch\":67,\"velocity\":100},{\"start\":136.14,\"end\":136.52,\"pitch\":66,\"velocity\":100},{\"start\":136.52,\"end\":136.92,\"pitch\":67,\"velocity\":100},{\"start\":136.92,\"end\":137.68,\"pitch\":62,\"velocity\":100},{\"start\":138.32,\"end\":138.46,\"pitch\":59,\"velocity\":100},{\"start\":138.5,\"end\":138.62,\"pitch\":59,\"velocity\":100},{\"start\":138.66,\"end\":138.82,\"pitch\":59,\"velocity\":100},{\"start\":138.82,\"end\":139.2,\"pitch\":69,\"velocity\":100},{\"start\":139.2,\"end\":139.58,\"pitch\":67,\"velocity\":100},{\"start\":139.58,\"end\":139.94,\"pitch\":69,\"velocity\":100},{\"start\":139.94,\"end\":140.18,\"pitch\":67,\"velocity\":100},{\"start\":140.18,\"end\":140.74,\"pitch\":66,\"velocity\":100},{\"start\":141.36,\"end\":141.54,\"pitch\":66,\"velocity\":100},{\"start\":141.58,\"end\":141.72,\"pitch\":66,\"velocity\":100},{\"start\":141.72,\"end\":141.94,\"pitch\":64,\"velocity\":100},{\"start\":141.94,\"end\":142.52,\"pitch\":62,\"velocity\":100},{\"start\":142.94,\"end\":143.08,\"pitch\":59,\"velocity\":100},{\"start\":143.12,\"end\":143.28,\"pitch\":59,\"velocity\":100},{\"start\":143.28,\"end\":143.46,\"pitch\":61,\"velocity\":100},{\"start\":143.46,\"end\":143.84,\"pitch\":62,\"velocity\":100},{\"start\":143.84,\"end\":144.22,\"pitch\":59,\"velocity\":100},{\"start\":144.22,\"end\":144.6,\"pitch\":62,\"velocity\":100},{\"start\":144.6,\"end\":144.96,\"pitch\":66,\"velocity\":100},{\"start\":145,\"end\":145.36,\"pitch\":66,\"velocity\":100},{\"start\":145.36,\"end\":146.56,\"pitch\":64,\"velocity\":100},{\"start\":147.3,\"end\":147.64,\"pitch\":66,\"velocity\":100},{\"start\":147.64,\"end\":148.04,\"pitch\":64,\"velocity\":100},{\"start\":148.04,\"end\":148.76,\"pitch\":62,\"velocity\":100},{\"start\":149.24,\"end\":149.6,\"pitch\":62,\"velocity\":100},{\"start\":149.64,\"end\":149.98,\"pitch\":62,\"velocity\":100},{\"start\":149.98,\"end\":150.34,\"pitch\":64,\"velocity\":100},{\"start\":150.34,\"end\":150.76,\"pitch\":66,\"velocity\":100},{\"start\":150.76,\"end\":151.14,\"pitch\":67,\"velocity\":100},{\"start\":151.14,\"end\":151.68,\"pitch\":66,\"velocity\":100},{\"start\":152.32,\"end\":152.64,\"pitch\":67,\"velocity\":100},{\"start\":152.64,\"end\":153.04,\"pitch\":64,\"velocity\":100},{\"start\":153.04,\"end\":153.24,\"pitch\":62,\"velocity\":100},{\"start\":153.28,\"end\":153.8,\"pitch\":64,\"velocity\":100},{\"start\":153.8,\"end\":154.06,\"pitch\":61,\"velocity\":100},{\"start\":154.06,\"end\":154.8,\"pitch\":62,\"velocity\":100},{\"start\":155.24,\"end\":155.62,\"pitch\":59,\"velocity\":100},{\"start\":155.62,\"end\":155.9,\"pitch\":61,\"velocity\":100},{\"start\":155.9,\"end\":156.18,\"pitch\":62,\"velocity\":100},{\"start\":156.54,\"end\":156.92,\"pitch\":62,\"velocity\":100},{\"start\":156.92,\"end\":157.26,\"pitch\":64,\"velocity\":100},{\"start\":157.26,\"end\":157.7,\"pitch\":66,\"velocity\":100},{\"start\":157.7,\"end\":158.82,\"pitch\":61,\"velocity\":100},{\"start\":159.62,\"end\":160,\"pitch\":62,\"velocity\":100},{\"start\":160,\"end\":160.36,\"pitch\":61,\"velocity\":100},{\"start\":160.36,\"end\":161.12,\"pitch\":59,\"velocity\":100},{\"start\":161.54,\"end\":161.9,\"pitch\":71,\"velocity\":100},{\"start\":161.9,\"end\":162.26,\"pitch\":69,\"velocity\":100},{\"start\":162.26,\"end\":162.66,\"pitch\":67,\"velocity\":100},{\"start\":162.66,\"end\":163.08,\"pitch\":66,\"velocity\":100},{\"start\":163.08,\"end\":163.4,\"pitch\":67,\"velocity\":100},{\"start\":163.4,\"end\":164.46,\"pitch\":69,\"velocity\":100},{\"start\":164.62,\"end\":164.68,\"pitch\":62,\"velocity\":100},{\"start\":164.68,\"end\":164.94,\"pitch\":61,\"velocity\":100},{\"start\":164.94,\"end\":165.62,\"pitch\":62,\"velocity\":100},{\"start\":166.04,\"end\":166.56,\"pitch\":66,\"velocity\":100},{\"start\":166.56,\"end\":167.16,\"pitch\":67,\"velocity\":100},{\"start\":167.68,\"end\":167.84,\"pitch\":67,\"velocity\":100},{\"start\":167.88,\"end\":168.02,\"pitch\":67,\"velocity\":100},{\"start\":168.02,\"end\":168.46,\"pitch\":66,\"velocity\":100},{\"start\":168.46,\"end\":168.82,\"pitch\":64,\"velocity\":100},{\"start\":168.82,\"end\":169.2,\"pitch\":62,\"velocity\":100},{\"start\":169.2,\"end\":169.4,\"pitch\":61,\"velocity\":100},{\"start\":169.46,\"end\":169.68,\"pitch\":64,\"velocity\":100},{\"start\":169.68,\"end\":171.48,\"pitch\":62,\"velocity\":100},{\"start\":196.9,\"end\":197.28,\"pitch\":59,\"velocity\":100},{\"start\":197.28,\"end\":197.66,\"pitch\":67,\"velocity\":100},{\"start\":197.66,\"end\":198.06,\"pitch\":66,\"velocity\":100},{\"start\":198.06,\"end\":198.46,\"pitch\":67,\"velocity\":100},{\"start\":198.46,\"end\":199.18,\"pitch\":62,\"velocity\":100},{\"start\":199.84,\"end\":200,\"pitch\":59,\"velocity\":100},{\"start\":200.04,\"end\":200.16,\"pitch\":59,\"velocity\":100},{\"start\":200.2,\"end\":200.36,\"pitch\":59,\"velocity\":100},{\"start\":200.36,\"end\":200.74,\"pitch\":69,\"velocity\":100},{\"start\":200.74,\"end\":201.14,\"pitch\":67,\"velocity\":100},{\"start\":201.18,\"end\":201.5,\"pitch\":69,\"velocity\":100},{\"start\":201.5,\"end\":201.7,\"pitch\":67,\"velocity\":100},{\"start\":201.7,\"end\":202.32,\"pitch\":66,\"velocity\":100},{\"start\":202.92,\"end\":203.06,\"pitch\":66,\"velocity\":100},{\"start\":203.1,\"end\":203.26,\"pitch\":66,\"velocity\":100},{\"start\":203.26,\"end\":203.46,\"pitch\":64,\"velocity\":100},{\"start\":203.46,\"end\":204,\"pitch\":62,\"velocity\":100},{\"start\":204.46,\"end\":204.8,\"pitch\":59,\"velocity\":100},{\"start\":204.8,\"end\":205.34,\"pitch\":61,\"velocity\":100},{\"start\":205.34,\"end\":205.78,\"pitch\":59,\"velocity\":100},{\"start\":205.78,\"end\":206.16,\"pitch\":62,\"velocity\":100},{\"start\":206.16,\"end\":206.44,\"pitch\":66,\"velocity\":100},{\"start\":206.56,\"end\":206.9,\"pitch\":66,\"velocity\":100},{\"start\":206.9,\"end\":207.16,\"pitch\":64,\"velocity\":100},{\"start\":207.2,\"end\":208.34,\"pitch\":64,\"velocity\":100},{\"start\":208.84,\"end\":209.22,\"pitch\":66,\"velocity\":100},{\"start\":209.22,\"end\":209.6,\"pitch\":64,\"velocity\":100},{\"start\":209.6,\"end\":210.34,\"pitch\":62,\"velocity\":100},{\"start\":210.8,\"end\":211.14,\"pitch\":62,\"velocity\":100},{\"start\":211.18,\"end\":211.54,\"pitch\":62,\"velocity\":100},{\"start\":211.54,\"end\":211.9,\"pitch\":64,\"velocity\":100},{\"start\":211.9,\"end\":212.32,\"pitch\":66,\"velocity\":100},{\"start\":212.32,\"end\":212.68,\"pitch\":67,\"velocity\":100},{\"start\":212.68,\"end\":213.38,\"pitch\":66,\"velocity\":100},{\"start\":213.86,\"end\":214.22,\"pitch\":67,\"velocity\":100},{\"start\":214.22,\"end\":214.6,\"pitch\":64,\"velocity\":100},{\"start\":214.6,\"end\":214.86,\"pitch\":62,\"velocity\":100},{\"start\":214.86,\"end\":215.32,\"pitch\":64,\"velocity\":100},{\"start\":215.32,\"end\":215.58,\"pitch\":61,\"velocity\":100},{\"start\":215.58,\"end\":216.36,\"pitch\":62,\"velocity\":100},{\"start\":216.76,\"end\":217.12,\"pitch\":59,\"velocity\":100},{\"start\":217.12,\"end\":217.38,\"pitch\":62,\"velocity\":100},{\"start\":217.42,\"end\":217.8,\"pitch\":62,\"velocity\":100},{\"start\":218.12,\"end\":218.48,\"pitch\":62,\"velocity\":100},{\"start\":218.48,\"end\":218.84,\"pitch\":64,\"velocity\":100},{\"start\":218.84,\"end\":219.26,\"pitch\":66,\"velocity\":100},{\"start\":219.26,\"end\":219.62,\"pitch\":61,\"velocity\":100},{\"start\":219.66,\"end\":220.32,\"pitch\":61,\"velocity\":100},{\"start\":221.16,\"end\":221.52,\"pitch\":62,\"velocity\":100},{\"start\":221.52,\"end\":221.9,\"pitch\":61,\"velocity\":100},{\"start\":221.9,\"end\":222.64,\"pitch\":59,\"velocity\":100},{\"start\":223.08,\"end\":223.44,\"pitch\":71,\"velocity\":100},{\"start\":223.44,\"end\":223.82,\"pitch\":69,\"velocity\":100},{\"start\":223.82,\"end\":224.22,\"pitch\":67,\"velocity\":100},{\"start\":224.22,\"end\":224.6,\"pitch\":66,\"velocity\":100},{\"start\":224.6,\"end\":224.96,\"pitch\":67,\"velocity\":100},{\"start\":224.96,\"end\":226.06,\"pitch\":69,\"velocity\":100},{\"start\":226.14,\"end\":226.52,\"pitch\":62,\"velocity\":100},{\"start\":226.56,\"end\":227.18,\"pitch\":62,\"velocity\":100},{\"start\":227.56,\"end\":227.88,\"pitch\":66,\"velocity\":100},{\"start\":227.92,\"end\":228.08,\"pitch\":66,\"velocity\":100},{\"start\":228.08,\"end\":228.78,\"pitch\":67,\"velocity\":100},{\"start\":229.26,\"end\":229.42,\"pitch\":67,\"velocity\":100},{\"start\":229.46,\"end\":229.6,\"pitch\":67,\"velocity\":100},{\"start\":229.6,\"end\":229.98,\"pitch\":66,\"velocity\":100},{\"start\":229.98,\"end\":230.36,\"pitch\":64,\"velocity\":100},{\"start\":230.36,\"end\":230.74,\"pitch\":62,\"velocity\":100},{\"start\":230.74,\"end\":230.96,\"pitch\":61,\"velocity\":100},{\"start\":230.96,\"end\":231.2,\"pitch\":64,\"velocity\":100},{\"start\":231.2,\"end\":232.5,\"pitch\":62,\"velocity\":100},{\"start\":233.44,\"end\":233.84,\"pitch\":66,\"velocity\":100},{\"start\":233.84,\"end\":234.2,\"pitch\":64,\"velocity\":100},{\"start\":234.2,\"end\":234.98,\"pitch\":62,\"velocity\":100},{\"start\":235.4,\"end\":235.76,\"pitch\":62,\"velocity\":100},{\"start\":235.8,\"end\":236.14,\"pitch\":62,\"velocity\":100},{\"start\":236.14,\"end\":236.5,\"pitch\":64,\"velocity\":100},{\"start\":236.5,\"end\":236.92,\"pitch\":66,\"velocity\":100},{\"start\":236.92,\"end\":237.32,\"pitch\":67,\"velocity\":100},{\"start\":237.32,\"end\":237.94,\"pitch\":66,\"velocity\":100},{\"start\":238.46,\"end\":238.82,\"pitch\":67,\"velocity\":100},{\"start\":238.82,\"end\":239.22,\"pitch\":64,\"velocity\":100},{\"start\":239.22,\"end\":239.44,\"pitch\":62,\"velocity\":100},{\"start\":239.44,\"end\":239.98,\"pitch\":64,\"velocity\":100},{\"start\":239.98,\"end\":240.94,\"pitch\":62,\"velocity\":100},{\"start\":241.38,\"end\":241.74,\"pitch\":59,\"velocity\":100},{\"start\":241.74,\"end\":241.98,\"pitch\":61,\"velocity\":100},{\"start\":241.98,\"end\":242.44,\"pitch\":62,\"velocity\":100},{\"start\":242.74,\"end\":243.1,\"pitch\":62,\"velocity\":100},{\"start\":243.1,\"end\":243.46,\"pitch\":64,\"velocity\":100},{\"start\":243.46,\"end\":243.86,\"pitch\":66,\"velocity\":100},{\"start\":243.86,\"end\":244.94,\"pitch\":61,\"velocity\":100},{\"start\":245.78,\"end\":246.14,\"pitch\":62,\"velocity\":100},{\"start\":246.14,\"end\":246.48,\"pitch\":61,\"velocity\":100},{\"start\":246.48,\"end\":247.26,\"pitch\":59,\"velocity\":100},{\"start\":247.7,\"end\":248.06,\"pitch\":71,\"velocity\":100},{\"start\":248.06,\"end\":248.42,\"pitch\":69,\"velocity\":100},{\"start\":248.42,\"end\":248.84,\"pitch\":67,\"velocity\":100},{\"start\":248.84,\"end\":249.22,\"pitch\":66,\"velocity\":100},{\"start\":249.22,\"end\":249.58,\"pitch\":67,\"velocity\":100},{\"start\":249.58,\"end\":250.7,\"pitch\":69,\"velocity\":100},{\"start\":250.78,\"end\":251.02,\"pitch\":61,\"velocity\":100},{\"start\":251.14,\"end\":251.4,\"pitch\":64,\"velocity\":100},{\"start\":251.4,\"end\":251.86,\"pitch\":62,\"velocity\":100},{\"start\":252.18,\"end\":252.7,\"pitch\":66,\"velocity\":100},{\"start\":252.7,\"end\":253.34,\"pitch\":67,\"velocity\":100},{\"start\":253.86,\"end\":254.18,\"pitch\":67,\"velocity\":100},{\"start\":254.18,\"end\":254.6,\"pitch\":66,\"velocity\":100},{\"start\":254.6,\"end\":254.96,\"pitch\":64,\"velocity\":100},{\"start\":254.96,\"end\":255.58,\"pitch\":62,\"velocity\":100},{\"start\":255.58,\"end\":255.78,\"pitch\":64,\"velocity\":100},{\"start\":255.78,\"end\":257.38,\"pitch\":62,\"velocity\":100},{\"start\":258.88,\"end\":259.56,\"pitch\":57,\"velocity\":100},{\"start\":260.04,\"end\":260.36,\"pitch\":62,\"velocity\":100},{\"start\":260.4,\"end\":260.78,\"pitch\":62,\"velocity\":100},{\"start\":260.78,\"end\":261.12,\"pitch\":57,\"velocity\":100},{\"start\":261.16,\"end\":261.46,\"pitch\":57,\"velocity\":100},{\"start\":261.52,\"end\":261.9,\"pitch\":69,\"velocity\":100},{\"start\":261.9,\"end\":262.46,\"pitch\":67,\"velocity\":100},{\"start\":262.5,\"end\":264.02,\"pitch\":67,\"velocity\":100},{\"start\":264.6,\"end\":264.96,\"pitch\":62,\"velocity\":100},{\"start\":264.96,\"end\":265.34,\"pitch\":67,\"velocity\":100},{\"start\":265.38,\"end\":265.56,\"pitch\":67,\"velocity\":100},{\"start\":265.6,\"end\":266.16,\"pitch\":67,\"velocity\":100},{\"start\":266.56,\"end\":267.16,\"pitch\":67,\"velocity\":100},{\"start\":267.16,\"end\":267.72,\"pitch\":66,\"velocity\":100},{\"start\":267.72,\"end\":268.26,\"pitch\":64,\"velocity\":100},{\"start\":268.26,\"end\":269.34,\"pitch\":62,\"velocity\":100},{\"start\":272.4,\"end\":273.06,\"pitch\":61,\"velocity\":100},{\"start\":273.14,\"end\":273.6,\"pitch\":64,\"velocity\":100},{\"start\":273.6,\"end\":277.1,\"pitch\":62,\"velocity\":100}],\"confidence\":0.7598156}";
//    }
//
//    public static AliMusicContentCenter mockARTCKaraokeRoomMusicLibrary() {
//        return new AliMusicContentCenter() {
//            private int mRequestId = 1;
//            private static final String REQUEST_ID_STR = "request_id_";
//
//            private String genRequestId() {
//                mRequestId++;
//                return REQUEST_ID_STR+mRequestId;
//            }
//            @Override
//            public int initialize(AliMusicContentCenterEventListener observer, AliMusicContentCenterConfiguration config, String extras) {
//                return 0;
//            }
//
//            @Override
//            public void unInitialize() {
//
//            }
//
//            @Override
//            public int registerEventHandler(AliMusicContentCenterEventListener observer) {
//                return 0;
//            }
//
//            @Override
//            public void unRegisterEventHandler() {
//
//            }
//
//            @Override
//            public String getSongInfo(String songId) {
//                return genRequestId();
//            }
//
//            @Override
//            public String getSongResource(String songId) {
//                return genRequestId();
//            }
//
//            @Override
//            public String getSongStandardPitch(String songId) {
//                return genRequestId();
//            }
//
//            @Override
//            public MusicCacheInfo[] getCaches() {
//                return new MusicCacheInfo[0];
//            }
//
//            @Override
//            public MusicCacheState getCacheState(String songId) {
//                return null;
//            }
//
//            @Override
//            public int removeCacheBySongId(String songId) {
//                return 0;
//            }
//
//            @Override
//            public int removeAllCache() {
//                return 0;
//            }
//
//            @Override
//            public String getLyric(String songId) {
//                return genRequestId();
//            }
//
//            @Override
//            public String getMusicCharts() {
//                return genRequestId();
//            }
//
//            @Override
//            public String getMusicCollectionByChartId(String chartId, int page, int pageSize, String jsonOption) {
//                return genRequestId();
//            }
//
//            @Override
//            public String searchMusic(String keyWord, int[] vendorId, int preferVendor, int page, int pageSize, String jsonOption) {
//                return genRequestId();
//            }
//        };
//    }
//
//    private static Uri sMockMusicFileUri;
//    public static boolean initMockMusicFile(Context context) {
//        try {
//            File tempFile = File.createTempFile("temp", null, context.getExternalCacheDir());
//            sMockMusicFileUri = Uri.fromFile(tempFile);
//
//            InputStream inputStream = context.getAssets().open("27FF35E3ABD743.mp3");
//
//            OutputStream outputStream = new FileOutputStream(tempFile);
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = inputStream.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, length);
//            }
//
//            inputStream.close();
//            outputStream.close();
//            Log.i("initMockMusicFile", sMockMusicFileUri.toString());
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    public static Uri getMockMusicFileUri() {
//        return sMockMusicFileUri;
//    }
//}
