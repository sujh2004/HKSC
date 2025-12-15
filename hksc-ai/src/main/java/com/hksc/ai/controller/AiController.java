package com.hksc.ai.controller;

import cn.hutool.http.HttpRequest; // Hutool 工具
import com.fasterxml.jackson.databind.JsonNode; // Jackson
import com.fasterxml.jackson.databind.ObjectMapper; // Jackson
import com.hksc.ai.dto.ProductDTO;
import com.hksc.ai.feign.ProductClient;
import com.hksc.common.result.Result;
import jakarta.annotation.Resource; // Spring 注入
import org.springframework.beans.factory.annotation.Value; // 读取配置
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
    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model:qwen-turbo}")
    private String modelName;

    @Resource
    private ObjectMapper objectMapper; // 记得导入 Jackson 包

    @GetMapping("/generate")
    public Result<String> generateDescription(@RequestParam String keyword) {
        System.out.println("收到 AI 请求: " + keyword + " | 线程: " + Thread.currentThread());

        // 1. 构造 OpenAI 标准请求体
        String prompt = "请为商品“" + keyword + "”写一段电商营销文案，50字以内，语气吸引人。";
        String jsonBody = String.format(
                "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}]}",
                modelName, prompt
        );

        try {
            // 2. 发送请求 (阿里云百炼 OpenAI 兼容地址)
            String responseBody = HttpRequest.post("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .timeout(10000)
                    .execute()
                    .body();

            // 3. 解析结果
            JsonNode rootNode = objectMapper.readTree(responseBody);

            if (rootNode.has("error")) {
                return Result.error("AI调用失败: " + rootNode.path("error").path("message").asText());
            }

            // 提取内容
            String aiText = rootNode.path("choices").get(0).path("message").path("content").asText();
            return Result.success(aiText);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI 服务响应异常");
        }
    }

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