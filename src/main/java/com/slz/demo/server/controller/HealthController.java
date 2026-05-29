package com.slz.demo.server.controller;

import com.slz.demo.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查
 */
@RestController
public class HealthController {

    /**
     * 健康检查
     * @return
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("ok");
    }
}
