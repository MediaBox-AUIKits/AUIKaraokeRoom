package com.aliyuncs.aui.service;

import com.aliyuncs.aui.dto.InvokeResult;
import com.aliyuncs.aui.dto.JoinMember;
import com.aliyuncs.aui.dto.req.*;
import com.aliyuncs.aui.entity.SongInfoEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 播单服务
 *
 * @author chunlei.zcl
 */
public interface SongInfoService extends IService<SongInfoEntity> {

    InvokeResult selectSong(SelectSongRequestDto selectSongRequestDto);


    String playSong(PlaySongRequestDto playSongRequestDto);

    InvokeResult deleteSong(DeleteSongRequestDto deleteSongRequestDto);

    InvokeResult pinSong(PinSongRequestDto pinSongRequestDto);

    List<SongInfoEntity> listSongs(ListSongRequestDto listSongRequestDto);

    InvokeResult joinInSinging(JoinInSingingRequestDto joinInSingingRequestDto);

    InvokeResult leaveSinging(LeaveSingingRequestDto leaveSingingRequestDto);

    List<JoinMember> getSinging(GetSingingRequestDto getSingingRequestDto);
}

