package com.hksc.user.controller;

import com.hksc.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class TestController {

    @GetMapping("/test")
    public Result<String> test(){
        return Result.success("恭喜!网管转发成功,这里是用户服务(8082");
    }
}
