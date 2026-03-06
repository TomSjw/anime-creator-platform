package com.anime.creator.task;

import com.alibaba.fastjson.JSONObject;
import com.anime.creator.dao.CreationResultMapper;
import com.anime.creator.dao.TaskInfoMapper;
import com.anime.creator.model.CreationResult;
import com.anime.creator.model.TaskInfo;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.context.XxlJobHelper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * 动漫生成任务处理器
 *
 * ✅ 修复1：将核心逻辑抽取到 executeTask(Long taskId)，Controller 可直接调用。
 * ✅ 修复2：@XxlJob 方法通过 XxlJobHelper.getJobParam() 获取参数（原代码方法签名错误）。
 * ✅ 修复3：OkHttpClient 增加超时配置，避免长时间阻塞。
 * ✅ 修复4：Integer 参数获取方式使用默认值重载，避免空指针。
 */
@Component
public class AnimeGenerateJobHandler {
    private static final Logger logger = LoggerFactory.getLogger(AnimeGenerateJobHandler.class);

    @Resource
    private TaskInfoMapper taskInfoMapper;
    @Resource
    private CreationResultMapper resultMapper;

    @Value("${storage.path}")
    private String storagePath;

    // ✅ 修复：OkHttpClient 设置合理超时，SD 生成耗时较长用 120s
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    /**
     * XxlJob 调度入口（由调度中心触发）
     * ✅ 修复：无参方法 + 用 XxlJobHelper.getJobParam() 获取参数，原代码 execute(String param) 签名不兼容
     */
    @XxlJob("animeGenerateJob")
    public void xxlJobEntry() throws Exception {
        // ✅ 正确方式：从 XxlJobHelper 获取调度中心传入的参数
        String param = XxlJobHelper.getJobParam();
        if (param == null || param.trim().isEmpty()) {
            XxlJobHelper.log("参数为空，任务跳过");
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
        logger.info("AnimeGenerateJob 开始执行, taskId={}", taskId);

        TaskInfo task = taskInfoMapper.selectById(taskId);
        if (task == null) {
            logger.error("任务不存在, taskId={}", taskId);
            return;
        }

        try {
            // 1. 更新状态为运行中
            updateTask(task, TaskInfo.STATUS_RUNNING, 10, null);

            // 2. 解析生成参数
            JSONObject param = JSONObject.parseObject(task.getParam());
            String prompt = param.getString("prompt");
            String style = param.getString("style");
            // ✅ 修复：使用 getIntValue 避免空指针，提供默认值
            int width = param.getIntValue("width") > 0 ? param.getIntValue("width") : 512;
            int height = param.getIntValue("height") > 0 ? param.getIntValue("height") : 512;

            // 3. 构造 Stable Diffusion 请求
            JSONObject sdParam = new JSONObject();
            sdParam.put("prompt", prompt + ", " + style + " anime style, high quality");
            sdParam.put("negative_prompt", "ugly, blurry, low quality");
            sdParam.put("width", width);
            sdParam.put("height", height);
            sdParam.put("steps", 20);
            sdParam.put("cfg_scale", 7);

            RequestBody body = RequestBody.create(JSON_TYPE, sdParam.toJSONString());
            Request request = new Request.Builder()
                    .url("http://127.0.0.1:7860/sdapi/v1/txt2img")
                    .post(body)
                    .build();

            updateTask(task, null, 30, null);

            // 4. 调用 SD API
            String imageBase64;
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new RuntimeException("SD API 调用失败: " + response.code() + " " + response.message());
                }
                JSONObject resultJson = JSONObject.parseObject(response.body().string());
                imageBase64 = resultJson.getJSONArray("images").getString(0);
            }

            updateTask(task, null, 70, null);

            // 5. 保存图片到本地
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            String fileName = System.currentTimeMillis() + ".png";
            String filePath = storagePath + File.separator + fileName;
            new File(storagePath).mkdirs();
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(imageBytes);
            }

            updateTask(task, null, 90, null);

            // 6. 保存成果记录
            CreationResult result = new CreationResult();
            result.setTaskId(taskId);
            result.setFileName(fileName);
            result.setFilePath(filePath);
            result.setFileType(CreationResult.TYPE_IMAGE);
            result.setFileSize((long) imageBytes.length);
            result.setPrompt(prompt);
            result.setStyle(style);
            result.setStatus(CreationResult.STATUS_PENDING);
            resultMapper.insert(result);

            // 7. 任务完成
            task.setStatus(TaskInfo.STATUS_SUCCESS);
            task.setProgress(100);
            task.setResultId(result.getId());
            task.setResult("生成成功");
            taskInfoMapper.updateById(task);

            logger.info("AnimeGenerateJob 执行成功, taskId={}, resultId={}", taskId, result.getId());

        } catch (Exception e) {
            logger.error("AnimeGenerateJob 执行失败, taskId={}", taskId, e);
            task.setStatus(TaskInfo.STATUS_FAILED);
            task.setErrorMsg(e.getMessage());
            taskInfoMapper.updateById(task);
            throw e;
        }
    }

    /**
     * 便捷更新任务状态/进度
     */
    private void updateTask(TaskInfo task, String status, Integer progress, String errorMsg) {
        if (status != null) task.setStatus(status);
        if (progress != null) task.setProgress(progress);
        if (errorMsg != null) task.setErrorMsg(errorMsg);
        taskInfoMapper.updateById(task);
    }
}
