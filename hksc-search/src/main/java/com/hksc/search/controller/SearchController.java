package com.hksc.search.controller;

import com.hksc.common.result.Result;
import com.hksc.search.entity.ProductDoc;
import com.hksc.search.repository.ProductRepository;
import jakarta.annotation.Resource;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
     * 1. 手动同步一条测试数据 (方便测试)
     */
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody ProductDoc productDoc) {
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
}