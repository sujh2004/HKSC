package com.hksc.search.controller;

import com.hksc.common.result.Result;
import com.hksc.search.entity.ProductDoc;
import com.hksc.search.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Resource
    private ProductRepository productRepository;

    @Resource
    private org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate elasticsearchTemplate;

    /**
     * ✅ 服务启动时自动初始化ES测试数据
     */
    @PostConstruct
    public void initElasticsearchData() {
        try {
            // 检查ES中是否已有数据
            long count = productRepository.count();
            if (count > 0) {
                System.out.println("✅ ES中已有 " + count + " 条数据，跳过初始化");
                return;
            }

            System.out.println("🔄 开始初始化ES测试数据...");

            // 创建测试数据
            List<ProductDoc> testProducts = new ArrayList<>();

            // iPhone 15 Pro
            ProductDoc iPhone = new ProductDoc();
            iPhone.setId(1L);
            iPhone.setTitle("iPhone 15 Pro");
            iPhone.setPrice(new BigDecimal("8999.00"));
            iPhone.setBrand("Apple");
            iPhone.setSuggest(new Completion(new String[]{"iPhone 15 Pro", "iPhone", "苹果手机"}));
            testProducts.add(iPhone);

            // MacBook Pro
            ProductDoc macbook = new ProductDoc();
            macbook.setId(2L);
            macbook.setTitle("MacBook Pro M3");
            macbook.setPrice(new BigDecimal("14999.00"));
            macbook.setBrand("Apple");
            macbook.setSuggest(new Completion(new String[]{"MacBook Pro M3", "MacBook", "苹果电脑"}));
            testProducts.add(macbook);

            // 小米14
            ProductDoc xiaomi = new ProductDoc();
            xiaomi.setId(3L);
            xiaomi.setTitle("小米14");
            xiaomi.setPrice(new BigDecimal("3999.00"));
            xiaomi.setBrand("Xiaomi");
            xiaomi.setSuggest(new Completion(new String[]{"小米14", "小米手机", "Xiaomi"}));
            testProducts.add(xiaomi);

            // 批量保存
            productRepository.saveAll(testProducts);

            System.out.println("✅ 成功初始化 " + testProducts.size() + " 条商品数据到ES");
        } catch (Exception e) {
            System.err.println("❌ ES数据初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 1. 手动同步一条测试数据 (方便测试)
     */
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody ProductDoc productDoc) {
        // 如果有title，自动设置suggest字段用于自动补全
        if (productDoc.getTitle() != null) {
            productDoc.setSuggest(new Completion(new String[]{productDoc.getTitle()}));
        }
        productRepository.save(productDoc);
        return Result.success(true);
    }

    /**
     * 2. 全文搜索接口
     * URL: /api/search/list?keyword=手机
     */
    @GetMapping("/list")
    public Result<List<ProductDoc>> search(@RequestParam String keyword) {
        // 构建查询条件：标题包含 keyword
        Criteria criteria = new Criteria("title").contains(keyword);
        CriteriaQuery query = new CriteriaQuery(criteria);

        SearchHits<ProductDoc> searchHits = elasticsearchTemplate.search(query, ProductDoc.class);

        // 提取结果
        List<ProductDoc> list = searchHits.stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        return Result.success(list);
    }

    /**
     * 3. 搜索自动补全接口
     * URL: /api/search/suggest?prefix=苹
     * 返回：["苹果手机", "苹果耳机", "苹果电脑"]
     */
    @GetMapping("/suggest")
    public Result<List<String>> suggest(@RequestParam String prefix) {
        // 简化版：直接在title字段上模糊匹配
        // 生产环境应该使用ES的Completion Suggester
        // 使用contains代替startsWith以支持多词搜索
        Criteria criteria = new Criteria("title").contains(prefix);
        CriteriaQuery query = new CriteriaQuery(criteria);

        SearchHits<ProductDoc> searchHits = elasticsearchTemplate.search(query, ProductDoc.class);

        // 提取title作为建议
        List<String> suggestions = searchHits.stream()
                .map(hit -> hit.getContent().getTitle())
                .distinct()
                .limit(10)  // 最多返回10条建议
                .collect(Collectors.toList());

        return Result.success(suggestions);
    }

    /**
     * 4. ✅ 一键初始化ES测试数据
     * URL: /api/search/init
     * 用浏览器直接访问即可
     */
    @GetMapping("/init")
    public Result<String> initTestData() {
        try {
            // 清空旧数据
            productRepository.deleteAll();

            // 创建测试数据
            List<ProductDoc> testProducts = new ArrayList<>();

            // iPhone 15 Pro
            ProductDoc iPhone = new ProductDoc();
            iPhone.setId(1L);
            iPhone.setTitle("iPhone 15 Pro");
            iPhone.setPrice(new BigDecimal("8999.00"));
            iPhone.setBrand("Apple");
            iPhone.setSuggest(new Completion(new String[]{"iPhone 15 Pro", "iPhone", "苹果手机"}));
            testProducts.add(iPhone);

            // MacBook Pro
            ProductDoc macbook = new ProductDoc();
            macbook.setId(2L);
            macbook.setTitle("MacBook Pro M3");
            macbook.setPrice(new BigDecimal("14999.00"));
            macbook.setBrand("Apple");
            macbook.setSuggest(new Completion(new String[]{"MacBook Pro M3", "MacBook", "苹果电脑"}));
            testProducts.add(macbook);

            // 小米14
            ProductDoc xiaomi = new ProductDoc();
            xiaomi.setId(3L);
            xiaomi.setTitle("小米14");
            xiaomi.setPrice(new BigDecimal("3999.00"));
            xiaomi.setBrand("Xiaomi");
            xiaomi.setSuggest(new Completion(new String[]{"小米14", "小米手机", "Xiaomi"}));
            testProducts.add(xiaomi);

            // 批量保存
            productRepository.saveAll(testProducts);

            return Result.success("✅ 成功初始化 " + testProducts.size() + " 条商品数据到ES");
        } catch (Exception e) {
            return Result.error("❌ 初始化失败: " + e.getMessage());
        }
    }
}