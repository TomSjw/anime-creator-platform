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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

/**
 * 动漫生成任务处理器
 * 由Xxl-Job调度执行异步生成任务
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

    private OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @XxlJob("animeGenerateJob")
    public void execute(String param) throws Exception {
        logger.info("Anime Generate Job start. param:{}", param);
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

            // 2. 解析生成参数
            JSONObject generateParam = JSONObject.parseObject(task.getParam());
            String prompt = generateParam.getString("prompt");
            String style = generateParam.getString("style");
            Integer width = generateParam.getInteger("width", 512);
            Integer height = generateParam.getInteger("height", 512);
            Long toolId = generateParam.getLong("toolId");

            // TODO: 这里根据toolId获取AI工具配置，调用对应AI API
            // 示例：调用Stable Diffusion API
            JSONObject sdParam = new JSONObject();
            sdParam.put("prompt", prompt + ", " + style + " anime style");
            sdParam.put("width", width);
            sdParam.put("height", height);
            sdParam.put("steps", 20);

            RequestBody body = RequestBody.create(JSON, sdParam.toJSONString());
            Request request = new Request.Builder()
                    .url("http://127.0.0.1:7860/sdapi/v1/txt2img") // SD WebUI API地址
                    .post(body)
                    .build();

            task.setProgress(30);
            taskInfoMapper.updateById(task);

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("AI API调用失败: " + response.message());
            }

            JSONObject resultJson = JSONObject.parseObject(response.body().string());
            String imageBase64 = resultJson.getJSONArray("images").getString(0);

            task.setProgress(70);
            taskInfoMapper.updateById(task);

            // 3. 保存图片到本地存储
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            String fileName = System.currentTimeMillis() + ".png";
            String filePath = storagePath + "/" + fileName;
            // 确保目录存在
            new File(storagePath).mkdirs();
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(imageBytes);
            }

            task.setProgress(90);
            taskInfoMapper.updateById(task);

            // 4. 保存成果记录
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

            // 5. 更新任务状态
            task.setStatus(TaskInfo.STATUS_SUCCESS);
            task.setProgress(100);
            task.setResultId(result.getId());
            task.setResult("生成成功");
            taskInfoMapper.updateById(task);

            logger.info("Anime Generate Job success. taskId:{}", taskId);

        } catch (Exception e) {
            logger.error("Anime Generate Job error. taskId:{}", taskId, e);
            task.setStatus(TaskInfo.STATUS_FAILED);
            task.setErrorMsg(e.getMessage());
            taskInfoMapper.updateById(task);
            throw e;
        }
    }
}
