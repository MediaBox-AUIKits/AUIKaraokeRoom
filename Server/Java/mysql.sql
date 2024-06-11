CREATE TABLE `room_infos` (
  `id` char(32) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `title` varchar(256) DEFAULT NULL,
  `extends` mediumtext,
  `status` tinyint DEFAULT NULL,
  `chat_id` char(32) DEFAULT NULL,
  `notice` varchar(256) DEFAULT NULL,
  `cover_url` varchar(256) DEFAULT NULL,
  `anchor_id` varchar(256) DEFAULT NULL,
  `anchor_nick` varchar(256) DEFAULT NULL,
  `meeting_info` mediumtext,
  `show_code` int NOT NULL,
  `started_at` datetime DEFAULT NULL,
  `stopped_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_create_at` (`created_at`),
  UNIQUE KEY `udx_show_code`(`show_code`)
) ENGINE=InnoDB;



CREATE TABLE `song_infos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `room_id` char(32)  NOT NULL,
  `song_id` varchar(256) NOT NULL,
  `song_extends` mediumtext  DEFAULT NULL,
  `user_id` varchar(256) NOT NULL,
  `user_extends` mediumtext DEFAULT NULL,
  `top` tinyint NOT NULL DEFAULT 0 COMMENT '0: 非置顶, 1: 置顶',
  `top_time` datetime DEFAULT NULL,
  `status` tinyint NOT NULL,
  `join_members`  mediumtext  DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_top_time` (`top`, `top_time`),
  KEY `idx_create_at` (`created_at`),
  KEY `udx_room_id`(`room_id`)
) ENGINE=InnoDB;