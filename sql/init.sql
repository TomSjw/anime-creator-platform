-- 私人动漫创作自动化工作网站 - 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS anime_creator DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE anime_creator;

-- 1. 用户表 (仅支持单用户，私人使用)
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(128) NOT NULL COMMENT '密码(BCrypt加密)',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 初始化默认用户: admin / 123456
INSERT INTO `sys_user` (`username`, `password`, `nickname`) VALUES 
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '管理员');

-- 2. AI工具配置表
CREATE TABLE `ai_tool_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tool_name` varchar(64) NOT NULL COMMENT '工具名称(如Stable Diffusion, Midjourney)',
  `tool_type` varchar(32) NOT NULL COMMENT '工具类型(IMAGE/VIDEO)',
  `api_url` varchar(256) NOT NULL COMMENT 'API地址',
  `api_key` varchar(512) DEFAULT NULL COMMENT 'API密钥',
  `priority` int DEFAULT 0 COMMENT '优先级(数字越大越优先)',
  `is_enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI工具配置表';

-- 3. 成果表
CREATE TABLE `creation_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint DEFAULT NULL COMMENT '关联生成任务ID',
  `file_name` varchar(256) NOT NULL COMMENT '文件名称',
  `file_path` varchar(512) NOT NULL COMMENT '文件存储路径',
  `file_type` varchar(32) NOT NULL COMMENT '文件类型(IMAGE/VIDEO)',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `prompt` text COMMENT '生成提示词',
  `style` varchar(64) DEFAULT NULL COMMENT '动漫风格',
  `status` varchar(32) DEFAULT 'PENDING' COMMENT '状态: PENDING(待发布), PUBLISHED(已发布), DISCARDED(废弃)',
  `xiaohongshu_url` varchar(256) DEFAULT NULL COMMENT '小红书发布链接',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='创作成果表';

-- 4. 任务表
CREATE TABLE `task_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name` varchar(128) NOT NULL COMMENT '任务名称',
  `task_type` varchar(32) NOT NULL COMMENT '任务类型: GENERATE(生成), PUBLISH(发布)',
  `status` varchar(32) NOT NULL COMMENT '任务状态: PENDING, RUNNING, SUCCESS, FAILED',
  `progress` int DEFAULT 0 COMMENT '进度(0-100)',
  `param` text COMMENT '任务参数(JSON)',
  `result` text COMMENT '任务结果(JSON)',
  `error_msg` text COMMENT '错误信息',
  `result_id` bigint DEFAULT NULL COMMENT '关联成果ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务信息表';

-- 5. 系统配置表
CREATE TABLE `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(64) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `config_name` varchar(128) DEFAULT NULL COMMENT '配置名称',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_name`, `remark`) VALUES
('storage.path', '/home/user/Desktop/anime-creator-platform/storage', '成果存储路径', '生成的动漫文件存储位置'),
('task.timeout', '300', '任务超时时间', '任务超时时间，单位秒'),
('xiaohongshu.appid', '', '小红书AppID', '小红书开放平台AppID'),
('xiaohongshu.appsecret', '', '小红书AppSecret', '小红书开放平台AppSecret'),
('xiaohongshu.redirectUri', '', '小红书回调地址', '小红书授权回调地址');
