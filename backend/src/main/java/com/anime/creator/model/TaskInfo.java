package com.anime.creator.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 任务信息实体
 */
@Data
@TableName("task_info")
public class TaskInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskName;
    private String taskType; // GENERATE, PUBLISH
    private String status; // PENDING, RUNNING, SUCCESS, FAILED
    private Integer progress;
    private String param; // JSON参数
    private String result; // JSON结果
    private String errorMsg;
    private Long resultId; // 关联成果ID
    private Date createTime;
    private Date updateTime;

    // 任务状态常量
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    // 任务类型常量
    public static final String TYPE_GENERATE = "GENERATE";
    public static final String TYPE_PUBLISH = "PUBLISH";
}
