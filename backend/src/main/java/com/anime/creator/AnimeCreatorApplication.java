package com.anime.creator;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 私人动漫创作自动化工作网站 - 后端启动类
 */
@SpringBootApplication
@MapperScan("com.anime.creator.dao")
public class AnimeCreatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnimeCreatorApplication.class, args);
    }
}
