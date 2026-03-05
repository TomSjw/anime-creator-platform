package com.anime.creator.controller;

import com.anime.creator.dao.CreationResultMapper;
import com.anime.creator.model.CreationResult;
import com.anime.creator.model.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 成果管理接口
 */
@RestController
@RequestMapping("/result")
public class ResultController {

    @Resource
    private CreationResultMapper resultMapper;

    @Value("${storage.path}")
    private String storagePath;

    /**
     * 成果列表
     */
    @GetMapping("/list")
    public R<?> list(@RequestParam(defaultValue = "1") Integer page,
                     @RequestParam(defaultValue = "12") Integer size,
                     @RequestParam(required = false) String fileType,
                     @RequestParam(required = false) String status,
                     @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<CreationResult> wrapper = new LambdaQueryWrapper<>();
        if (fileType != null && !fileType.isEmpty()) {
            wrapper.eq(CreationResult::getFileType, fileType);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(CreationResult::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(CreationResult::getPrompt, keyword);
        }
        wrapper.orderByDesc(CreationResult::getCreateTime);

        IPage<CreationResult> result = resultMapper.selectPage(new Page<>(page, size), wrapper);
        return R.success(result);
    }

    /**
     * 成果详情
     */
    @GetMapping("/{id}")
    public R<?> getById(@PathVariable Long id) {
        CreationResult result = resultMapper.selectById(id);
        return R.success(result);
    }

    /**
     * 删除成果
     */
    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) throws IOException {
        CreationResult result = resultMapper.selectById(id);
        if (result != null) {
            // 删除本地文件
            File file = new File(result.getFilePath());
            if (file.exists()) {
                file.delete();
            }
            // 删除数据库记录
            resultMapper.deleteById(id);
        }
        return R.success();
    }

    /**
     * 更新成果状态
     */
    @PutMapping("/{id}/status")
    public R<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        CreationResult result = new CreationResult();
        result.setId(id);
        result.setStatus(status);
        resultMapper.updateById(result);
        return R.success();
    }

    /**
     * 预览成果文件
     */
    @GetMapping("/file/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long id) throws IOException {
        CreationResult result = resultMapper.selectById(id);
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Path path = Paths.get(result.getFilePath());
        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        byte[] data = Files.readAllBytes(path);
        HttpHeaders headers = new HttpHeaders();
        if (CreationResult.TYPE_IMAGE.equals(result.getFileType())) {
            headers.setContentType(MediaType.IMAGE_PNG);
        } else {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
