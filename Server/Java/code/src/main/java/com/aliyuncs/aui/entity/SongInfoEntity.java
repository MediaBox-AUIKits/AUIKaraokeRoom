package com.aliyuncs.aui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 播单Entity
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("song_infos")
public class SongInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	private String id;

	/**
	 * 创建时间
	 */
	@JsonProperty("created_at")
	private Date createdAt;

	/**
	 * 修改时间
	 */
	@JsonProperty("updated_at")
	private Date updatedAt;

	/**
	 * 房间Id
	 */
	@JsonProperty("room_id")
	private String roomId;

	/**
	 * 歌曲Id
	 */
	@JsonProperty("song_id")
	private String songId;

	/**
	 * 歌曲扩展信息
	 */
	@JsonProperty("song_extends")
	private String songExtends;

	/**
	 * 用户id
	 */
	@JsonProperty("user_id")
	private String userId;

	/**
	 * 用户扩展信息
	 */
	@JsonProperty("user_extends")
	private String userExtends;

	/**
	 * 0: 非置顶, 1: 置顶
	 */
	private boolean top;

	/**
	 * 公告
	 */
	@JsonProperty("top_time")
	private Date topTime;

	/**
	 * 状态 1： 已播放， 2：正在播放, 3:  待播放, 4: 已删除
	 */
	private Integer status;

	/**
	 * 合唱成员信息
	 */
	@JsonProperty("join_members")
	private String joinMembers;


}
