package com.aliyuncs.aui.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * ListSongRequestDto
 *
 * @author chunlei.zcl
 */
@Data
public class ListSongRequestDto {

    @NotBlank(message="roomId不能为空")
    @JsonProperty("room_id")
    private String roomId;

    @JsonProperty("page_num")
    private Integer pageNum;

    @JsonProperty("page_size")
    private Integer pageSize;

}
