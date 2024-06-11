package com.aliyuncs.aui.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JoinMember
 *
 * @author chunlei.zcl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinMember {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("join_time")
    private Long joinTime;
}
