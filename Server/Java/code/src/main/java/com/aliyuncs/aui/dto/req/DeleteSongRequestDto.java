package com.aliyuncs.aui.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * DeleteSongRequestDto
 *
 * @author chunlei.zcl
 */
@Data
public class DeleteSongRequestDto {

    @NotBlank(message="roomId不能为空")
    @JsonProperty("room_id")
    private String roomId;

    @NotBlank(message="userId不能为空")
    @JsonProperty("user_id")
    private String userId;

    @NotBlank(message="songId不能为空")
    @JsonProperty("song_ids")
    private String songIds;

    @NotBlank(message="operator不能为空")
    private String operator;
}
