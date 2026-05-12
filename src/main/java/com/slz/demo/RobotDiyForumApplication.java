package com.slz.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.slz.demo.server.mapper")
public class RobotDiyForumApplication {

    public static void main(String[] args) {
        SpringApplication.run(RobotDiyForumApplication.class, args);
    }

}
