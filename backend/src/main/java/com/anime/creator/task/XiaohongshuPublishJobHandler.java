package com.anime.creator.task;

import com.anime.creator.dao.CreationResultMapper;
import com.anime.creator.dao.TaskInfoMapper;
import com.anime.creator.model.CreationResult;
import com.anime.creator.model.TaskInfo;
import com.alibaba.fastjson.JSONObject;
import com.xxl.job.core.handler.annotation.XxlJob;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * 小红书发布任务处理器
 * 由Xxl-Job调度执行异步发布任务
 */
@Component
public class XiaohongshuPublishJobHandler {
    private static final Logger logger = LoggerFactory.getLogger(XiaohongshuPublishJobHandler.class);

    @Resource
    private TaskInfoMapper taskInfoMapper;
    @Resource
    private CreationResultMapper resultMapper;

    private OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @XxlJob("xiaohongshuPublishJob")
    public void execute(String param) throws Exception {
        logger.info("Xiaohongshu Publish Job start. param:{}", param);
        Long taskId = Long.parseLong(param);
        TaskInfo task = taskInfoMapper.selectById(taskId);
        if (task == null) {
            logger.error("Task not found. taskId:{}", taskId);
            return;
        }

        try {
            // 1. 更新任务状态为运行中
            task.setStatus(TaskInfo.STATUS_RUNNING);
            task.setProgress(10);
            taskInfoMapper.updateById(task);

            // 2. 解析发布参数
            JSONObject publishParam = JSONObject.parseObject(task.getParam());
            Long resultId = publishParam.getLong("resultId");
            String title = publishParam.getString("title");
            String content = publishParam.getString("content");
            String tags = publishParam.getString("tags");

            CreationResult result = resultMapper.selectById(resultId);
            if (result == null) {
                throw new RuntimeException("成果不存在");
            }

            File imageFile = new File(result.getFilePath());
            if (!imageFile.exists()) {
                throw new RuntimeException("成果文件不存在");
            }

            task.setProgress(30);
            taskInfoMapper.updateById(task);

            // TODO: 调用小红书开放平台API
            // 步骤：
            // 1. 获取access_token
            // 2. 上传图片/视频到小红书
            // 3. 发布笔记
            // 具体API文档参考：https://partner.xiaohongshu.com/open-doc/server/

            // 模拟发布成功
            Thread.sleep(1000);
            String xhsUrl = "https://www.xiaohongshu.com/discovery/item/xxxxxx";

            task.setProgress(70);
            taskInfoMapper.updateById(task);

            // 3. 更新成果状态
            result.setStatus(CreationResult.STATUS_PUBLISHED);
            result.setXiaohongshuUrl(xhsUrl);
            resultMapper.updateById(result);

            task.setProgress(90);
            taskInfoMapper.updateById(task);

            // 4. 更新任务状态
            task.setStatus(TaskInfo.STATUS_SUCCESS);
            task.setProgress(100);
            task.setResult("发布成功: " + xhsUrl);
            taskInfoMapper.updateById(task);

            logger.info("Xiaohongshu Publish Job success. taskId:{}", taskId);

        } catch (Exception e) {
            logger.error("Xiaohongshu Publish Job error. taskId:{}", taskId, e);
            task.setStatus(TaskInfo.STATUS_FAILED);
            task.setErrorMsg(e.getMessage());
            taskInfoMapper.updateById(task);
            throw e;
        }
    }
}
