package com.anime.creator.controller;

import com.anime.creator.dao.TaskInfoMapper;
import com.anime.creator.model.R;
import com.anime.creator.model.TaskInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.executor.XxlJobExecutor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 任务相关接口
 */
@RestController
@RequestMapping("/task")
public class TaskController {

    @Resource
    private TaskInfoMapper taskInfoMapper;

    /**
     * 创建生成任务
     */
    @PostMapping("/generate")
    public R<?> createGenerateTask(@RequestBody Map<String, Object> param) {
        try {
            TaskInfo task = new TaskInfo();
            task.setTaskName("动漫生成任务");
            task.setTaskType(TaskInfo.TYPE_GENERATE);
            task.setStatus(TaskInfo.STATUS_PENDING);
            task.setProgress(0);
            task.setParam(com.alibaba.fastjson.JSONObject.toJSONString(param));

            taskInfoMapper.insert(task);

            // 触发xxl-job任务执行
            XxlJobExecutor.registJobHandler("animeGenerateJob");
            ReturnT<String> triggerResult = XxlJobExecutor.getInstance().getJobHandlerRepository().getJobHandler("animeGenerateJob")
                    .execute(task.getId().toString());

            return R.success(task.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("创建任务失败: " + e.getMessage());
        }
    }

    /**
     * 创建发布任务
     */
    @PostMapping("/publish")
    public R<?> createPublishTask(@RequestBody Map<String, Object> param) {
        try {
            TaskInfo task = new TaskInfo();
            task.setTaskName("小红书发布任务");
            task.setTaskType(TaskInfo.TYPE_PUBLISH);
            task.setStatus(TaskInfo.STATUS_PENDING);
            task.setProgress(0);
            task.setParam(com.alibaba.fastjson.JSONObject.toJSONString(param));

            taskInfoMapper.insert(task);

            // 触发xxl-job任务执行
            XxlJobExecutor.registJobHandler("xiaohongshuPublishJob");
            XxlJobExecutor.getInstance().getJobHandlerRepository().getJobHandler("xiaohongshuPublishJob")
                    .execute(task.getId().toString());

            return R.success(task.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("创建任务失败: " + e.getMessage());
        }
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
        try {
            TaskInfo task = taskInfoMapper.selectById(id);
            if (task == null) {
                return R.fail("任务不存在");
            }
            if (!TaskInfo.STATUS_FAILED.equals(task.getStatus())) {
                return R.fail("只有失败任务可以重试");
            }

            // 重置任务状态
            task.setStatus(TaskInfo.STATUS_PENDING);
            task.setProgress(0);
            task.setErrorMsg(null);
            taskInfoMapper.updateById(task);

            // 重新触发任务
            if (TaskInfo.TYPE_GENERATE.equals(task.getTaskType())) {
                XxlJobExecutor.getInstance().getJobHandlerRepository().getJobHandler("animeGenerateJob")
                        .execute(task.getId().toString());
            } else {
                XxlJobExecutor.getInstance().getJobHandlerRepository().getJobHandler("xiaohongshuPublishJob")
                        .execute(task.getId().toString());
            }

            return R.success();
        } catch (Exception e) {
            return R.fail("重试失败: " + e.getMessage());
        }
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
