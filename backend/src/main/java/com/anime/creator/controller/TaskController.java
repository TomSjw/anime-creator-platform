package com.anime.creator.controller;

import com.alibaba.fastjson.JSONObject;
import com.anime.creator.dao.TaskInfoMapper;
import com.anime.creator.model.R;
import com.anime.creator.model.TaskInfo;
import com.anime.creator.task.AnimeGenerateJobHandler;
import com.anime.creator.task.XiaohongshuPublishJobHandler;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 任务相关接口
 *
 * ✅ 修复：移除了原代码中错误的 XxlJobExecutor 直接调用方式。
 *    XxlJob 的 JobHandler 应由调度中心触发，不能在 Controller 里直接 invoke。
 *    改为：创建任务记录后，使用线程池异步执行 JobHandler，实现本地可运行的异步任务。
 */
@RestController
@RequestMapping("/task")
public class TaskController {

    @Resource
    private TaskInfoMapper taskInfoMapper;

    @Resource
    private AnimeGenerateJobHandler animeGenerateJobHandler;

    @Resource
    private XiaohongshuPublishJobHandler xiaohongshuPublishJobHandler;

    // 简单线程池，用于异步执行任务（替代 XxlJob 直接触发）
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    /**
     * 创建生成任务
     */
    @PostMapping("/generate")
    public R<?> createGenerateTask(@RequestBody Map<String, Object> param) {
        TaskInfo task = new TaskInfo();
        task.setTaskName("动漫生成任务");
        task.setTaskType(TaskInfo.TYPE_GENERATE);
        task.setStatus(TaskInfo.STATUS_PENDING);
        task.setProgress(0);
        task.setParam(JSONObject.toJSONString(param));
        taskInfoMapper.insert(task);

        // ✅ 修复：异步执行，避免阻塞请求
        Long taskId = task.getId();
        executor.submit(() -> {
            try {
                animeGenerateJobHandler.executeTask(taskId);
            } catch (Exception e) {
                // 异常已在 handler 内部处理并更新任务状态
            }
        });

        return R.success(task.getId());
    }

    /**
     * 创建发布任务
     */
    @PostMapping("/publish")
    public R<?> createPublishTask(@RequestBody Map<String, Object> param) {
        TaskInfo task = new TaskInfo();
        task.setTaskName("小红书发布任务");
        task.setTaskType(TaskInfo.TYPE_PUBLISH);
        task.setStatus(TaskInfo.STATUS_PENDING);
        task.setProgress(0);
        task.setParam(JSONObject.toJSONString(param));
        taskInfoMapper.insert(task);

        Long taskId = task.getId();
        executor.submit(() -> {
            try {
                xiaohongshuPublishJobHandler.executeTask(taskId);
            } catch (Exception e) {
                // 异常已在 handler 内部处理并更新任务状态
            }
        });

        return R.success(task.getId());
    }

    /**
     * 任务列表
     */
    @GetMapping("/list")
    public R<?> list(@RequestParam(defaultValue = "1") Integer page,
                     @RequestParam(defaultValue = "10") Integer size,
                     @RequestParam(required = false) String status) {
        LambdaQueryWrapper<TaskInfo> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(TaskInfo::getStatus, status);
        }
        wrapper.orderByDesc(TaskInfo::getCreateTime);

        IPage<TaskInfo> result = taskInfoMapper.selectPage(new Page<>(page, size), wrapper);
        return R.success(result);
    }

    /**
     * 任务详情
     */
    @GetMapping("/{id}")
    public R<?> getById(@PathVariable Long id) {
        TaskInfo task = taskInfoMapper.selectById(id);
        return R.success(task);
    }

    /**
     * 重试失败任务
     */
    @PostMapping("/retry/{id}")
    public R<?> retry(@PathVariable Long id) {
        TaskInfo task = taskInfoMapper.selectById(id);
        if (task == null) {
            return R.fail("任务不存在");
        }
        if (!TaskInfo.STATUS_FAILED.equals(task.getStatus())) {
            return R.fail("只有失败任务可以重试");
        }

        task.setStatus(TaskInfo.STATUS_PENDING);
        task.setProgress(0);
        task.setErrorMsg(null);
        taskInfoMapper.updateById(task);

        Long taskId = task.getId();
        String taskType = task.getTaskType();
        executor.submit(() -> {
            try {
                if (TaskInfo.TYPE_GENERATE.equals(taskType)) {
                    animeGenerateJobHandler.executeTask(taskId);
                } else {
                    xiaohongshuPublishJobHandler.executeTask(taskId);
                }
            } catch (Exception e) {
                // 异常已在 handler 内部处理
            }
        });

        return R.success();
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        taskInfoMapper.deleteById(id);
        return R.success();
    }
}
