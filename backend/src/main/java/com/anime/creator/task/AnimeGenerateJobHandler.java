package com.anime.creator.task;

import com.alibaba.fastjson.JSONArray;
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
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * 动漫生成任务处理器（使用豆包 API 生成图片）
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

    @Value("${doubao.api.key}")
    private String doubaoApiKey;

    @Value("${doubao.api.base-url}")
    private String doubaoBaseUrl;

    @Value("${doubao.api.model}")
    private String doubaoModel;

    @Value("${doubao.api.image-size}")
    private String doubaoImageSize;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    /**
     * XxlJob 调度入口
     */
    @XxlJob("animeGenerateJob")
    public void xxlJobEntry() throws Exception {
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

            // 3. 构造豆包 API 请求
            String fullPrompt = prompt + ", " + style + " anime style, high quality";
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", doubaoModel);
            requestBody.put("prompt", fullPrompt);
            requestBody.put("size", doubaoImageSize);
            requestBody.put("n", 1);
            requestBody.put("response_format", "url");

            RequestBody body = RequestBody.create(JSON_TYPE, requestBody.toJSONString());
            Request request = new Request.Builder()
                    .url(doubaoBaseUrl + "/images/generations")
                    .addHeader("Authorization", "Bearer " + doubaoApiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            updateTask(task, null, 30, null);

            // 4. 调用豆包 API
            String imageUrl;
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new RuntimeException("豆包 API 调用失败: " + response.code() + " " + response.message());
                }
                JSONObject resultJson = JSONObject.parseObject(response.body().string());
                JSONArray dataArray = resultJson.getJSONArray("data");
                if (dataArray == null || dataArray.isEmpty()) {
                    throw new RuntimeException("豆包 API 返回数据为空");
                }
                imageUrl = dataArray.getJSONObject(0).getString("url");
                if (imageUrl == null || imageUrl.isEmpty()) {
                    throw new RuntimeException("豆包 API 未返回图片URL");
                }
            }

            logger.info("豆包 API 生成图片成功, imageUrl={}", imageUrl);
            updateTask(task, null, 60, null);

            // 5. 下载图片并保存到本地
            Request downloadRequest = new Request.Builder()
                    .url(imageUrl)
                    .get()
                    .build();

            byte[] imageBytes;
            try (Response downloadResponse = client.newCall(downloadRequest).execute()) {
                if (!downloadResponse.isSuccessful() || downloadResponse.body() == null) {
                    throw new RuntimeException("图片下载失败: " + downloadResponse.code());
                }
                try (InputStream is = downloadResponse.body().byteStream()) {
                    imageBytes = is.readAllBytes();
                }
            }

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

    private void updateTask(TaskInfo task, String status, Integer progress, String errorMsg) {
        if (status != null) task.setStatus(status);
        if (progress != null) task.setProgress(progress);
        if (errorMsg != null) task.setErrorMsg(errorMsg);
        taskInfoMapper.updateById(task);
    }
}
