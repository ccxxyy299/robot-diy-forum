package com.slz.demo;

import com.tangzc.autotable.springboot.EnableAutoTable;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.slz.demo.server.mapper")
@EnableAutoTable
public class RobotDiyForumApplication {

    public static void main(String[] args) {
        SpringApplication.run(RobotDiyForumApplication.class, args);
    }

}
