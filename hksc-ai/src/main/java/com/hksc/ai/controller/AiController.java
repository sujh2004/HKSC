package com.hksc.ai.controller;

import com.hksc.ai.dto.ProductDTO;
import com.hksc.ai.feign.ProductClient;
import com.hksc.common.result.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private ProductClient productClient;

    /**
     * 接口1: 模拟调用大模型生成商品文案
     */
    @GetMapping("/generate")
    public Result<String> generateDescription(@RequestParam String keyword) {

        // 1. 获取当前线程信息
        Thread currentThread = Thread.currentThread();
        System.out.println("收到请求: " + keyword + " | 处理线程: " + currentThread);

        try {
            // 2. 模拟耗时操作 (比如请求 OpenAI API 需要 3 秒)
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3. 模拟返回 AI 生成的文案
        String aiText = String.format(
                "【AI 智能推荐】这款 %s 采用了2025年最前沿的设计理念，" +
                        "融合了极致的工艺与人性化的功能。无论是自用还是送礼，" +
                        "它都能彰显您不凡的品味。限时特惠，此时不买更待何时？",
                keyword
        );

        return Result.success(aiText);
    } // 👈 generateDescription 方法在这里结束

    // ----------------------------------------------------------------------

    /**
     * 接口2: AI 猜你喜欢 (协同过滤 + 聚合查询)
     */
    @GetMapping("/recommend")
    public Result<List<ProductDTO>> recommend(@RequestParam Long userId) {

        System.out.println("正在为用户 " + userId + " 计算推荐结果... 线程: " + Thread.currentThread());

        // 1. 模拟“协同过滤”算法计算 (耗时 500ms)
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 2. 模拟算出推荐商品的 ID 列表
        // 这里写死推荐 ID=1 的商品（假设你数据库里肯定有 id=1 的商品）
        List<Long> recommendIds = List.of(1L);

        // 3. 远程调用 Product 服务获取详情
        List<ProductDTO> resultList = new ArrayList<>();

        for (Long pid : recommendIds) {
            try {
                // Feign 调用
                Result<ProductDTO> remoteRes = productClient.getProduct(pid);
                if (remoteRes.getCode() == 200 && remoteRes.getData() != null) {
                    resultList.add(remoteRes.getData());
                }
            } catch (Exception e) {
                System.err.println("获取商品 " + pid + " 失败，跳过推荐");
                // 打印堆栈以便排查 feign 报错
                e.printStackTrace();
            }
        }

        return Result.success(resultList);
    }
}