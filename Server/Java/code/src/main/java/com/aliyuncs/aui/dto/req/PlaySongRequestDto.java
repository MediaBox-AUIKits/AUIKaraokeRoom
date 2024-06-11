package com.aliyuncs.aui.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * PlaySongRequestDto
 *
 * @author chunlei.zcl
 */
@Data
public class PlaySongRequestDto {

    @NotBlank(message="roomId不能为空")
    @JsonProperty("room_id")
    private String roomId;


    @JsonProperty("user_id")
    private String userId;


    @JsonProperty("song_id")
    private String songId;

    @NotBlank(message="operator不能为空")
    private String operator;
}
