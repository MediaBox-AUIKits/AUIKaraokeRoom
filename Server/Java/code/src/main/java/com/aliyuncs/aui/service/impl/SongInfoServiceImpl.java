package com.aliyuncs.aui.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.aui.dao.SongInfoDao;
import com.aliyuncs.aui.dto.InvokeResult;
import com.aliyuncs.aui.dto.JoinMember;
import com.aliyuncs.aui.dto.enums.SongStatus;
import com.aliyuncs.aui.dto.req.*;
import com.aliyuncs.aui.entity.RoomInfoEntity;
import com.aliyuncs.aui.entity.SongInfoEntity;
import com.aliyuncs.aui.service.RoomInfoService;
import com.aliyuncs.aui.service.SongInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 播单服务实现类
 *
 * @author chunlei.zcl
 */
@Service("songInfoService")
@Slf4j
public class SongInfoServiceImpl extends ServiceImpl<SongInfoDao, SongInfoEntity> implements SongInfoService {

    @Resource
    private RoomInfoService roomInfoService;

    @Override
    public InvokeResult selectSong(SelectSongRequestDto selectSongRequestDto) {

        List<SongInfoEntity> songInfoEntities = this.lambdaQuery().eq(SongInfoEntity::getRoomId, selectSongRequestDto.getRoomId())
                .eq(SongInfoEntity::getUserId, selectSongRequestDto.getUserId())
                .eq(SongInfoEntity::getSongId, selectSongRequestDto.getSongId())
                .eq(SongInfoEntity::getStatus, SongStatus.PENDING_PLAY.getVal())
                .list();
        if (CollectionUtils.isNotEmpty(songInfoEntities)) {
            log.warn("selectSongRequestDto:{} Reduplicate Song", JSONObject.toJSONString(selectSongRequestDto));
            return InvokeResult.builder().success(false).reason("ReduplicateSong").build();
        }

        SongInfoEntity songInfoEntity = SongInfoEntity.builder()
                .createdAt(new Date())
                .updatedAt(new Date())
                .roomId(selectSongRequestDto.getRoomId())
                .songId(selectSongRequestDto.getSongId())
                .songExtends(selectSongRequestDto.getSongExtends())
                .userId(selectSongRequestDto.getUserId())
                .userExtends(selectSongRequestDto.getUserExtends())
                .top(false)
                .status(SongStatus.PENDING_PLAY.getVal())
                .build();

        this.save(songInfoEntity);

        return InvokeResult.builder().success(true).build();
    }

    @Override
    public String playSong(PlaySongRequestDto playSongRequestDto) {
        //传入songId为空时，代表最后一首歌播放已结束
        if (StringUtils.isBlank(playSongRequestDto.getSongId())) {
            return finishLastSong(playSongRequestDto);
        }

        //songId不为空时，UserId不能为空
        if (StringUtils.isBlank(playSongRequestDto.getUserId())) {
            throw new RuntimeException("NOT_PERMIT");
        }

        List<SongInfoEntity> records = this.lambdaQuery().eq(SongInfoEntity::getRoomId, playSongRequestDto.getRoomId())
                .eq(SongInfoEntity::getStatus, SongStatus.PENDING_PLAY.getVal())
                .orderByDesc(SongInfoEntity::isTop)
                .orderByDesc(SongInfoEntity::getTopTime)
                .orderByAsc(SongInfoEntity::getId)
                .list();

        if (CollectionUtils.isEmpty(records)) {
            log.warn("songInfoEntityPage.getRecords() is empty.");
            throw new RuntimeException("EMPTY_SONGS");
        }

        SongInfoEntity firstSongInfo = records.get(0);
        if (!firstSongInfo.getSongId().equals(playSongRequestDto.getSongId()) ||
                !firstSongInfo.getUserId().equals(playSongRequestDto.getUserId())) {
            log.warn("songInfoEntityPage.getRecords() is not match.");
            throw new RuntimeException("SONGS_NOT_MATCH");
        }

        RoomInfoEntity roomInfoEntity = roomInfoService.getById(playSongRequestDto.getRoomId());
        if (roomInfoEntity == null) {
            throw new RuntimeException("ROOM_NOT_FOUND");
        }

        SongInfoEntity songInfoEntityWithPlaying = getSongInfoEntityWithPlaying(playSongRequestDto.getRoomId());

        if (!firstSongInfo.getUserId().equals(playSongRequestDto.getOperator())
                && !roomInfoEntity.getAnchorId().equals(playSongRequestDto.getOperator())
                && !(songInfoEntityWithPlaying != null && songInfoEntityWithPlaying.getUserId().equals(playSongRequestDto.getOperator()))) {
            throw new RuntimeException("NOT_PERMIT");
        }

        if (songInfoEntityWithPlaying  != null) {
            updateStatus(songInfoEntityWithPlaying.getId(), SongStatus.PLAYED.getVal());
        }

        updateStatus(firstSongInfo.getId(), SongStatus.PLAYING.getVal());

        String nextSongId = null;
        if (records.size() >= 2) {
            nextSongId = records.get(1).getSongId();
        }
        return nextSongId;
    }

    public String finishLastSong(PlaySongRequestDto playSongRequestDto) {
        List<SongInfoEntity> records = this.lambdaQuery().eq(SongInfoEntity::getRoomId, playSongRequestDto.getRoomId())
                .eq(SongInfoEntity::getStatus, SongStatus.PENDING_PLAY.getVal())
                .list();
        //歌单中不应该存在待播放歌曲
        if (CollectionUtils.isNotEmpty(records)) {
            throw new RuntimeException("NOT_EMPTY_SONGS");
        }

        RoomInfoEntity roomInfoEntity = roomInfoService.getById(playSongRequestDto.getRoomId());
        if (roomInfoEntity == null) {
            throw new RuntimeException("ROOM_NOT_FOUND");
        }

        SongInfoEntity songInfoEntityWithPlaying = getSongInfoEntityWithPlaying(playSongRequestDto.getRoomId());
        if (songInfoEntityWithPlaying != null) {
            updateStatus(songInfoEntityWithPlaying.getId(), SongStatus.PLAYED.getVal());
        }
        return null;
    }

    @Override
    public InvokeResult deleteSong(DeleteSongRequestDto deleteSongRequestDto) {

        List<SongInfoEntity> songInfoEntities = this.lambdaQuery()
                .eq(SongInfoEntity::getRoomId, deleteSongRequestDto.getRoomId())
                .eq(SongInfoEntity::getUserId, deleteSongRequestDto.getUserId())
                .in(SongInfoEntity::getSongId, Arrays.asList(deleteSongRequestDto.getSongIds().split(",")))
                .eq(SongInfoEntity::getStatus, SongStatus.PENDING_PLAY.getVal())
                .list();
        if (CollectionUtils.isEmpty(songInfoEntities)) {
            return InvokeResult.builder().success(false).reason("NotFound").build();
        }

        RoomInfoEntity roomInfoEntity = roomInfoService.getById(deleteSongRequestDto.getRoomId());
        if (roomInfoEntity == null) {
            return InvokeResult.builder().success(false).reason("ROOM_NOT_FOUND").build();
        }

        if (!deleteSongRequestDto.getUserId().equals(deleteSongRequestDto.getOperator())
               && !roomInfoEntity.getAnchorId().equals(deleteSongRequestDto.getOperator())) {
            return InvokeResult.builder().success(false).reason("NOT_PERMIT").build();
        }

        for (SongInfoEntity songInfoEntity : songInfoEntities) {
            updateStatus(songInfoEntity.getId(), SongStatus.DELETED.getVal());
        }
        return InvokeResult.builder().success(true).build();
    }

    @Override
    public InvokeResult pinSong(PinSongRequestDto pinSongRequestDto) {

        List<SongInfoEntity> songInfoEntities = this.lambdaQuery()
                .eq(SongInfoEntity::getRoomId, pinSongRequestDto.getRoomId())
                .eq(SongInfoEntity::getSongId, pinSongRequestDto.getSongId())
                .eq(SongInfoEntity::getStatus, SongStatus.PENDING_PLAY.getVal())
                .list();
        if (CollectionUtils.isEmpty(songInfoEntities)) {
            return InvokeResult.builder().success(false).reason("NotFound").build();
        }

        RoomInfoEntity roomInfoEntity = roomInfoService.getById(pinSongRequestDto.getRoomId());
        if (roomInfoEntity == null) {
            return InvokeResult.builder().success(false).reason("ROOM_NOT_FOUND").build();
        }

        if (!pinSongRequestDto.getUserId().equals(pinSongRequestDto.getOperator())
                && !roomInfoEntity.getAnchorId().equals(pinSongRequestDto.getOperator())) {
            return InvokeResult.builder().success(false).reason("NOT_PERMIT").build();
        }

        SongInfoEntity songInfoEntity = songInfoEntities.get(0);
        songInfoEntity.setTop(true);
        songInfoEntity.setTopTime(new Date());
        songInfoEntity.setUpdatedAt(new Date());

        this.updateById(songInfoEntity);
        return InvokeResult.builder().success(true).build();
    }

    @Override
    public List<SongInfoEntity> listSongs(ListSongRequestDto listSongRequestDto) {

        List<SongInfoEntity> songInfoEntities = this.lambdaQuery().eq(SongInfoEntity::getRoomId, listSongRequestDto.getRoomId())
                .in(SongInfoEntity:: getStatus, Arrays.asList(SongStatus.PLAYING.getVal(), SongStatus.PENDING_PLAY.getVal()))
                .orderByAsc(SongInfoEntity::getStatus)
                .orderByDesc(SongInfoEntity::isTop)
                .orderByDesc(SongInfoEntity::getTopTime)
                .orderByAsc(SongInfoEntity::getId)
                .list();

        if (CollectionUtils.isEmpty(songInfoEntities)) {
            log.warn("songInfoEntities is empty.");
            return null;
        }

        return songInfoEntities;
    }

    @Override
    public InvokeResult joinInSinging(JoinInSingingRequestDto joinInSingingRequestDto) {

        SongInfoEntity songInfoEntityWithPlaying = getSongInfoEntityWithPlaying(joinInSingingRequestDto.getRoomId());
        if (songInfoEntityWithPlaying == null) {
            return InvokeResult.builder().success(false).reason("NotFoundPlayingSong").build();
        }

        if (!songInfoEntityWithPlaying.getSongId().equals(joinInSingingRequestDto.getSongId())) {
            return InvokeResult.builder().success(false).reason("NotMatch").build();
        }

        String joinMembers = songInfoEntityWithPlaying.getJoinMembers();
        List<JoinMember> joinMemberList = JSONObject.parseArray(joinMembers, JoinMember.class);
        if (CollectionUtils.isNotEmpty(joinMemberList)) {
            for (JoinMember joinMember : joinMemberList) {
                if (joinMember.getUserId().equals(joinInSingingRequestDto.getUserId())) {
                    return InvokeResult.builder().success(false).reason("AlreadyInSinging").build();
                }
             }
        } else {
            joinMemberList =  new ArrayList<>();
        }

        joinMemberList.add(JoinMember.builder().userId(joinInSingingRequestDto.getUserId()).joinTime(new Date().getTime()).build());

        SongInfoEntity updateSongInfoEntity = new SongInfoEntity();
        updateSongInfoEntity.setId(songInfoEntityWithPlaying.getId());
        updateSongInfoEntity.setJoinMembers(JSONObject.toJSONString(joinMemberList));
        updateSongInfoEntity.setUpdatedAt(new Date());
        this.updateById(updateSongInfoEntity);

        return InvokeResult.builder().success(true).build();
    }

    @Override
    public InvokeResult leaveSinging(LeaveSingingRequestDto leaveSingingRequestDto) {

        SongInfoEntity songInfoEntityWithPlaying = getSongInfoEntityWithPlaying(leaveSingingRequestDto.getRoomId());
        if (songInfoEntityWithPlaying == null) {
            return InvokeResult.builder().success(false).reason("NotFoundPlayingSong").build();
        }

        if (!songInfoEntityWithPlaying.getSongId().equals(leaveSingingRequestDto.getSongId())) {
            return InvokeResult.builder().success(false).reason("NotMatch").build();
        }

        String joinMembers = songInfoEntityWithPlaying.getJoinMembers();
        if (StringUtils.isEmpty(joinMembers)) {
            return InvokeResult.builder().success(false).reason("NotFound").build();
        }

        List<JoinMember> joinMemberList = JSONObject.parseArray(joinMembers, JoinMember.class);

        Iterator<JoinMember> iterator = joinMemberList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getUserId().equals(leaveSingingRequestDto.getUserId())) {
                iterator.remove();
                SongInfoEntity updateSongInfoEntity = new SongInfoEntity();
                updateSongInfoEntity.setId(songInfoEntityWithPlaying.getId());
                updateSongInfoEntity.setJoinMembers(JSONObject.toJSONString(joinMemberList));
                updateSongInfoEntity.setUpdatedAt(new Date());
                this.updateById(updateSongInfoEntity);
                return InvokeResult.builder().success(true).build();
            }
        }
        return InvokeResult.builder().success(false).reason("NotFound").build();
    }

    @Override
    public List<JoinMember> getSinging(GetSingingRequestDto getSingingRequestDto) {

        SongInfoEntity songInfoEntityWithPlaying = getSongInfoEntityWithPlaying(getSingingRequestDto.getRoomId());
        if (songInfoEntityWithPlaying == null) {
            throw new RuntimeException("NotFoundPlayingSong");
        }

        if (!songInfoEntityWithPlaying.getSongId().equals(getSingingRequestDto.getSongId())) {
            throw new RuntimeException("NotMatch");
        }

        String joinMembers = songInfoEntityWithPlaying.getJoinMembers();
        if (StringUtils.isEmpty(joinMembers)) {
            return Collections.emptyList();
        }

        return JSONObject.parseArray(joinMembers, JoinMember.class);
    }

    private void updateStatus(String id, Integer dstStatus) {

        SongInfoEntity songInfoEntity = new SongInfoEntity();
        songInfoEntity.setId(id);
        songInfoEntity.setStatus(dstStatus);
        songInfoEntity.setUpdatedAt(new Date());
        this.updateById(songInfoEntity);
    }

    private SongInfoEntity getSongInfoEntityWithPlaying(String roomId) {

        List<SongInfoEntity> songInfoEntities = this.lambdaQuery().eq(SongInfoEntity::getRoomId, roomId)
                .eq(SongInfoEntity::getStatus, SongStatus.PLAYING.getVal())
                .list();
        if (CollectionUtils.isEmpty(songInfoEntities)) {
            return null;
        }
        return songInfoEntities.get(0);
    }

}