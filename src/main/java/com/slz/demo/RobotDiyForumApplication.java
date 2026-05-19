package com.slz.demo;

import com.tangzc.autotable.springboot.EnableAutoTable;
import io.github.cdimascio.dotenv.Dotenv;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.slz.demo.server.mapper")
@EnableAutoTable
public class RobotDiyForumApplication {

    static {
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .filename(".env")
                .ignoreIfMissing()
                .load();

        if (dotenv.entries().isEmpty()) {
            System.out.println("[WARN] 未找到 .env 文件，使用默认配置");
        } else {
            System.out.println("[INFO] 已加载 .env 文件：" + System.getProperty("user.dir"));
        }

        dotenv.entries().forEach(entry ->
            System.setProperty(entry.getKey(), entry.getValue())
        );
    }

    public static void main(String[] args) {
        SpringApplication.run(RobotDiyForumApplication.class, args);
    }

}
