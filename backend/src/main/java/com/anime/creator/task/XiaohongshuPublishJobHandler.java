package com.anime.creator.task;

import com.alibaba.fastjson.JSONObject;
import com.anime.creator.dao.CreationResultMapper;
import com.anime.creator.dao.TaskInfoMapper;
import com.anime.creator.model.CreationResult;
import com.anime.creator.model.TaskInfo;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * 小红书发布任务处理器
 *
 * ✅ 修复1：与 AnimeGenerateJobHandler 相同模式，抽取 executeTask() 方法供 Controller 调用。
 * ✅ 修复2：@XxlJob 方法改为无参，通过 XxlJobHelper.getJobParam() 获取 taskId。
 * ✅ 修复3：模拟发布部分保留 TODO 注释，清晰标明真实接入小红书 API 的位置。
 */
@Component
public class XiaohongshuPublishJobHandler {
    private static final Logger logger = LoggerFactory.getLogger(XiaohongshuPublishJobHandler.class);

    @Resource
    private TaskInfoMapper taskInfoMapper;
    @Resource
    private CreationResultMapper resultMapper;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * XxlJob 调度入口（由调度中心触发）
     * ✅ 修复：无参方法 + XxlJobHelper 获取参数
     */
    @XxlJob("xiaohongshuPublishJob")
    public void xxlJobEntry() throws Exception {
        String param = XxlJobHelper.getJobParam();
        if (param == null || param.trim().isEmpty()) {
            XxlJobHelper.handleFail("参数为空");
            return;
        }
        Long taskId = Long.parseLong(param.trim());
        executeTask(taskId);
    }

    /**
     * 核心执行逻辑（Controller 异步调用 & XxlJob 均走此方法）
     */
    public void executeTask(Long taskId) throws Exception {
        logger.info("XiaohongshuPublishJob 开始执行, taskId={}", taskId);

        TaskInfo task = taskInfoMapper.selectById(taskId);
        if (task == null) {
            logger.error("任务不存在, taskId={}", taskId);
            return;
        }

        try {
            // 1. 更新状态为运行中
            updateTask(task, TaskInfo.STATUS_RUNNING, 10, null);

            // 2. 解析发布参数
            JSONObject param = JSONObject.parseObject(task.getParam());
            Long resultId = param.getLong("resultId");
            String title = param.getString("title");
            String content = param.getString("content");
            String tags = param.getString("tags");

            // 3. 查询成果
            CreationResult result = resultMapper.selectById(resultId);
            if (result == null) {
                throw new RuntimeException("成果不存在, resultId=" + resultId);
            }
            File imageFile = new File(result.getFilePath());
            if (!imageFile.exists()) {
                throw new RuntimeException("成果文件不存在: " + result.getFilePath());
            }

            updateTask(task, null, 30, null);

            // ============================================================
            // TODO: 接入小红书开放平台 API
            // 步骤：
            //   1. 调用 /oauth2/access_token 获取 access_token
            //   2. 调用上传接口将 imageFile 上传，获取 media_id
            //   3. 调用发布接口，传入 title、content、tags、media_id
            // 参考文档：https://partner.xiaohongshu.com/open-doc/server/
            // ============================================================

            // 模拟发布耗时（接入真实API后删除此行）
            Thread.sleep(1500);
            String xhsUrl = "https://www.xiaohongshu.com/discovery/item/placeholder_" + taskId;

            updateTask(task, null, 80, null);

            // 4. 更新成果状态为已发布
            result.setStatus(CreationResult.STATUS_PUBLISHED);
            result.setXiaohongshuUrl(xhsUrl);
            resultMapper.updateById(result);

            // 5. 任务完成
            task.setStatus(TaskInfo.STATUS_SUCCESS);
            task.setProgress(100);
            task.setResult("发布成功: " + xhsUrl);
            taskInfoMapper.updateById(task);

            logger.info("XiaohongshuPublishJob 执行成功, taskId={}, url={}", taskId, xhsUrl);

        } catch (Exception e) {
            logger.error("XiaohongshuPublishJob 执行失败, taskId={}", taskId, e);
            task.setStatus(TaskInfo.STATUS_FAILED);
            task.setErrorMsg(e.getMessage());
            taskInfoMapper.updateById(task);
            throw e;
        }
    }

    private void updateTask(TaskInfo task, String status, Integer progress, String errorMsg) {
        if (status != null) task.setStatus(status);
        if (progress != null) task.setProgress(progress);
        if (errorMsg != null) task.setErrorMsg(errorMsg);
        taskInfoMapper.updateById(task);
    }
}
