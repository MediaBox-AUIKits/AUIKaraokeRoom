package com.aliyuncs.aui.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * JoinInSingingRequestDto
 *
 * @author chunlei.zcl
 */
@Data
public class LeaveSingingRequestDto {

    @NotBlank(message="roomId不能为空")
    @JsonProperty("room_id")
    private String roomId;

    @NotBlank(message="userId不能为空")
    @JsonProperty("user_id")
    private String userId;

    @NotBlank(message="songId不能为空")
    @JsonProperty("song_id")
    private String songId;
}
