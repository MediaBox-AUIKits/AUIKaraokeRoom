package com.aliyuncs.aui.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.aui.common.utils.PageUtils;
import com.aliyuncs.aui.common.utils.Result;
import com.aliyuncs.aui.common.utils.ValidatorUtils;
import com.aliyuncs.aui.dto.InvokeResult;
import com.aliyuncs.aui.dto.JoinMember;
import com.aliyuncs.aui.dto.MeetingMemberInfo;
import com.aliyuncs.aui.dto.req.*;
import com.aliyuncs.aui.dto.res.NewImTokenResponseDto;
import com.aliyuncs.aui.dto.res.RoomInfoDto;
import com.aliyuncs.aui.dto.res.RtcAuthTokenResponse;
import com.aliyuncs.aui.entity.SongInfoEntity;
import com.aliyuncs.aui.service.RoomInfoService;
import com.aliyuncs.aui.service.SongInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天室管理的Controller
 *
 * @author chunlei.zcl
 */
@RestController
@RequestMapping("/api/ktv")
@Slf4j
public class RoomInfoController {

    @Resource
    private RoomInfoService roomInfoService;

    @Resource
    private SongInfoService songInfoService;

    /**
     * 获取Im的token
     */
    @RequestMapping("/token")
    public Result getImToken(@RequestBody ImTokenRequestDto imTokenRequestDto) {

        ValidatorUtils.validateEntity(imTokenRequestDto);

        Map<String, Object> result = new HashMap<>();

        NewImTokenResponseDto newImTokenResponseDto = roomInfoService.getNewImToken(imTokenRequestDto);
        if (newImTokenResponseDto != null) {
            result.put("aliyun_im", newImTokenResponseDto);
        }
        return Result.ok(result);
    }

    @RequestMapping("/create")
    public Result createRoomInfo(@RequestBody RoomCreateRequestDto roomCreateRequestDto) {

        ValidatorUtils.validateEntity(roomCreateRequestDto);

        RoomInfoDto roomInfo = roomInfoService.createRoomInfo(roomCreateRequestDto);
        if (roomInfo != null) {
            String jsonStr = JSONObject.toJSONString(roomInfo);
            Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
            Result result = Result.ok();
            result.putAll(map);
            return result;
        }

        return Result.error();
    }

    /**
     * 信息
     */
    @RequestMapping("/get")
    public Result get(@RequestBody RoomGetRequestDto roomGetRequestDto) {

        ValidatorUtils.validateEntity(roomGetRequestDto);

        RoomInfoDto roomInfo = roomInfoService.get(roomGetRequestDto);
        if (roomInfo != null) {
            String jsonStr = JSONObject.toJSONString(roomInfo);
            Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
            Result result = Result.ok();
            result.putAll(map);
            return result;
        }
        return Result.notFound();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public Result list(@RequestBody RoomListRequestDto roomListRequestDto) {

        ValidatorUtils.validateEntity(roomListRequestDto);
        Map<String, Object> map = new HashMap<>();
        PageUtils page = roomInfoService.list(roomListRequestDto);
        if (page != null && CollectionUtils.isNotEmpty(page.getList())) {
            map.put("rooms", page.getList());
        }
        return Result.ok(map);
    }

    @RequestMapping("/dismiss")
    public Result dismiss(@RequestBody RoomUpdateStatusRequestDto roomUpdateStatusRequestDto) {

        ValidatorUtils.validateEntity(roomUpdateStatusRequestDto);
        InvokeResult result = roomInfoService.dismiss(roomUpdateStatusRequestDto);
        Map<String, Object> map = new HashMap<>();
        if (result.isSuccess()) {
            map.put("success", true);
        } else {
            map.put("success", false);
            map.put("reason", result.getReason());
        }
        return Result.ok(map);
    }


    @RequestMapping("/joinMic")
    public Result joinMic(@RequestBody JoinMicRequestDto joinMicRequestDto) {

        ValidatorUtils.validateEntity(joinMicRequestDto);
        if (!joinMicRequestDto.valid()) {
            return Result.invalidParam();
        }

        try {
            MeetingMemberInfo.Members members = roomInfoService.joinMic(joinMicRequestDto);
            if (members != null) {
                String jsonStr = JSONObject.toJSONString(members);
                Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
                Result result = Result.ok();
                result.putAll(map);
                return result;
            }
            return Result.error();
        } catch (RuntimeException e) {
            Map<String, Object> map = new HashMap<>();
            if (e.getMessage().equals("NotAvailable")) {
                map.put("reason", 1);
                map.put("desc", "麦位已满");
            } else if (e.getMessage().equals("AlreadyJoined"))  {
                map.put("reason", 2);
                map.put("desc", "用户已经上麦");
            }
            return Result.ok(map);
        }
    }

    @RequestMapping("/leaveMic")
    public Result leaveMic(@RequestBody LeaveMicRequestDto leaveMicRequestDto) {

        ValidatorUtils.validateEntity(leaveMicRequestDto);
        if (!leaveMicRequestDto.valid()) {
            return Result.invalidParam();
        }

        MeetingMemberInfo.Members members = roomInfoService.leaveMic(leaveMicRequestDto);
        if (members != null) {
            String jsonStr = JSONObject.toJSONString(members);
            Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
            Result result = Result.ok();
            result.putAll(map);
            return result;
        }
        return Result.error();
    }




    @RequestMapping("/update")
    public Result update(@RequestBody RoomUpdateRequestDto roomUpdateRequestDto) {

        ValidatorUtils.validateEntity(roomUpdateRequestDto);
        RoomInfoDto roomInfo = roomInfoService.update(roomUpdateRequestDto);
        if (roomInfo != null) {
            String jsonStr = JSONObject.toJSONString(roomInfo);
            Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
            Result result = Result.ok();
            result.putAll(map);
            return result;
        }
        return Result.error();
    }

    @RequestMapping("/getMeetingInfo")
    public Result getMeetingInfo(@RequestBody MeetingGetRequestDto meetingGetRequestDto) {

        ValidatorUtils.validateEntity(meetingGetRequestDto);
        MeetingMemberInfo.Members members = roomInfoService.getMeetingInfo(meetingGetRequestDto);

        Result result = Result.ok();
        Map<String, Object> map;
        if (members != null) {
            String jsonStr = JSONObject.toJSONString(members);
            map = JSON.parseObject(jsonStr, Map.class);
        } else {
            map = new HashMap<>();
        }
        result.putAll(map);
        return result;
    }

    @RequestMapping("/getRtcAuthToken")
    public Result getRtcAuthToken(@RequestBody RtcAuthTokenRequestDto rtcAuthTokenRequestDto) {

        ValidatorUtils.validateEntity(rtcAuthTokenRequestDto);

        RtcAuthTokenResponse rtcAuthToken = roomInfoService.getRtcAuthToken(rtcAuthTokenRequestDto);
        if (rtcAuthToken != null) {
            String jsonStr = JSONObject.toJSONString(rtcAuthToken);
            Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
            Result result = Result.ok();
            result.putAll(map);
            return result;
        }

        return Result.error();
    }

    @RequestMapping("/selectSong")
    public Result selectSong(@RequestBody SelectSongRequestDto selectSongRequestDto) {

        ValidatorUtils.validateEntity(selectSongRequestDto);

        InvokeResult result  = songInfoService.selectSong(selectSongRequestDto);
        Map<String, Object> map = new HashMap<>();
        if (result.isSuccess()) {
            map.put("success", true);
        } else {
            map.put("success", false);
            if ("ReduplicateSong".equals(result.getReason())) {
                map.put("reason", 1);
                map.put("desc", "重复点歌");
            }
        }
        return Result.ok(map);
    }


    @RequestMapping("/playSong")
    public Result playSong(@RequestBody PlaySongRequestDto playSongRequestDto) {

        ValidatorUtils.validateEntity(playSongRequestDto);

        try {
            String nextSongId = songInfoService.playSong(playSongRequestDto);
            Map<String, Object> map = new HashMap<>();
            map.put("success", true);
            if (StringUtils.isNotEmpty(nextSongId)) {
                map.put("next_song_id", nextSongId);
            }
            return Result.ok(map);
        } catch (RuntimeException e) {
            Map<String, Object> map = new HashMap<>();
            map.put("success", false);
            if (e.getMessage().equals("EMPTY_SONGS")) {
                map.put("reason", 1);
                map.put("desc", "无待播放歌曲");
            } else if (e.getMessage().equals("SONGS_NOT_MATCH"))  {
                map.put("reason", 2);
                map.put("desc", "歌曲不匹配");
            } else if (e.getMessage().equals("NOT_PERMIT"))  {
                map.put("reason", 3);
                map.put("desc", "无权限");
            } else if(e.getMessage().equals("NOT_EMPTY_SONGS")) {
                map.put("reason", 4);
                map.put("desc", "播放歌曲id为空，但仍有待播放歌曲");
            }
            return Result.ok(map);
        }
    }

    @RequestMapping("/deleteSong")
    public Result deleteSong(@RequestBody DeleteSongRequestDto deleteSongRequestDto) {

        ValidatorUtils.validateEntity(deleteSongRequestDto);

        InvokeResult result  = songInfoService.deleteSong(deleteSongRequestDto);
        Map<String, Object> map = new HashMap<>();
        if (result.isSuccess()) {
            map.put("success", true);
        } else {
            map.put("success", false);
            if ("NotFound".equals(result.getReason())) {
                map.put("reason", 1);
                map.put("desc", "无此歌曲");
            } else if ("NOT_PERMIT".equals(result.getReason()))  {
                map.put("reason", 2);
                map.put("desc", "无权限");
            }
        }
        return Result.ok(map);
    }

    @RequestMapping("/pinSong")
    public Result pinSong(@RequestBody PinSongRequestDto pinSongRequestDto) {

        ValidatorUtils.validateEntity(pinSongRequestDto);

        InvokeResult result  = songInfoService.pinSong(pinSongRequestDto);
        Map<String, Object> map = new HashMap<>();
        if (result.isSuccess()) {
            map.put("success", true);
        } else {
            map.put("success", false);
            if ("NotFound".equals(result.getReason())) {
                map.put("reason", 1);
                map.put("desc", "无此歌曲");
            } else if ("NOT_PERMIT".equals(result.getReason()))  {
                map.put("reason", 2);
                map.put("desc", "无权限");
            }
        }
        return Result.ok(map);
    }

    @RequestMapping("/listSongs")
    public Result listSongs(@RequestBody ListSongRequestDto listSongRequestDto) {

        ValidatorUtils.validateEntity(listSongRequestDto);
        Map<String, Object> map = new HashMap<>();
        List<SongInfoEntity> songInfoEntities = songInfoService.listSongs(listSongRequestDto);
        if (CollectionUtils.isNotEmpty(songInfoEntities)) {
            map.put("songs", songInfoEntities);
        }
        return Result.ok(map);
    }

    @RequestMapping("/joinInSinging")
    public Result joinInSinging(@RequestBody JoinInSingingRequestDto joinInSingingRequestDto) {

        ValidatorUtils.validateEntity(joinInSingingRequestDto);

        Map<String, Object> map = new HashMap<>();
        InvokeResult result  = songInfoService.joinInSinging(joinInSingingRequestDto);
        if (result.isSuccess()) {
            map.put("success", true);
        } else {
            map.put("success", false);
            if ("NotFoundPlayingSong".equals(result.getReason())) {
                map.put("reason", 1);
                map.put("desc", "无正在播放歌曲");
            } else if ("NotMatch".equals(result.getReason()))  {
                map.put("reason", 2);
                map.put("desc", "歌曲不匹配");
            } else if ("AlreadyInSinging".equals(result.getReason()))  {
                map.put("reason", 3);
                map.put("desc", "无需重复加入");
            }
        }
        return Result.ok(map);
    }


    @RequestMapping("/leaveSinging")
    public Result leaveSinging(@RequestBody LeaveSingingRequestDto leaveSingingRequestDto) {

        ValidatorUtils.validateEntity(leaveSingingRequestDto);

        Map<String, Object> map = new HashMap<>();
        InvokeResult result  = songInfoService.leaveSinging(leaveSingingRequestDto);
        if (result.isSuccess()) {
            map.put("success", true);
        } else {
            map.put("success", false);
            if ("NotFoundPlayingSong".equals(result.getReason())) {
                map.put("reason", 1);
                map.put("desc", "无正在播放歌曲");
            } else if ("NotMatch".equals(result.getReason()))  {
                map.put("reason", 2);
                map.put("desc", "歌曲不匹配");
            } else if ("NotFound".equals(result.getReason()))  {
                map.put("reason", 3);
                map.put("desc", "未加入合唱");
            }
        }
        return Result.ok(map);
    }

    @RequestMapping("/getSinging")
    public Result getSinging(@RequestBody GetSingingRequestDto getSingingRequestDto) {

        ValidatorUtils.validateEntity(getSingingRequestDto);

        try {
            Map<String, Object> map = new HashMap<>();
            List<JoinMember> joinMemberList = songInfoService.getSinging(getSingingRequestDto);
            if (CollectionUtils.isNotEmpty(joinMemberList)) {
                map.put("members", joinMemberList);
            }
            return Result.ok(map);
        } catch (RuntimeException e) {
            Map<String, Object> map = new HashMap<>();
            map.put("success", false);
            if (e.getMessage().equals("NotFoundPlayingSong")) {
                map.put("reason", 1);
                map.put("desc", "无正在播放歌曲");
            } else if (e.getMessage().equals("NotMatch"))  {
                map.put("reason", 2);
                map.put("desc", "歌曲不匹配");
            }
            return Result.ok(map);
        }
    }

}
