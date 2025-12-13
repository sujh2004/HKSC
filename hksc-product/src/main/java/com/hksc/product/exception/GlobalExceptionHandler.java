package com.hksc.product.exception;

import com.hksc.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 作用：捕获所有 Controller 抛出的异常，转为统一的 Result JSON 返回
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        // 1. 打印堆栈信息到控制台，方便排查 bug
        log.error("系统异常: ", e);

        // 2. 将异常信息放入 Result 的 message 中返回
        // 这样前端看到的就是 "库存不足"，而不是 "Internal Server Error"
        return Result.error(e.getMessage());
    }
}