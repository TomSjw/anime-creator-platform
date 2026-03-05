package com.anime.creator.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 创作成果实体
 */
@Data
@TableName("creation_result")
public class CreationResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String fileName;
    private String filePath;
    private String fileType; // IMAGE, VIDEO
    private Long fileSize;
    private String prompt;
    private String style;
    private String status; // PENDING, PUBLISHED, DISCARDED
    private String xiaohongshuUrl;
    private Date createTime;

    // 状态常量
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_DISCARDED = "DISCARDED";

    // 类型常量
    public static final String TYPE_IMAGE = "IMAGE";
    public static final String TYPE_VIDEO = "VIDEO";
}
