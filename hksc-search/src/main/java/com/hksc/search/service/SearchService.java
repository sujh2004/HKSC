package com.hksc.search.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hksc.common.result.Result;
import com.hksc.search.entity.ProductDoc;
import com.hksc.search.feign.ProductClient;
import com.hksc.search.repository.ProductRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {
    @Resource
    private ProductRepository productRepository;
    @Resource
    private ProductClient productClient;

    // 上架同步: 把商品存入 ES
    public void syncData() {
        // 1. 远程调用商品服务，获取数据 (这里演示取第一页 100 条)
        Result<Object> result = productClient.list(1, 100);

        // 2. 解析数据 (JSON转对象，略显麻烦，这里简化逻辑)
        // 真实项目中建议把 ProductDTO 放到 Common 包
        List<ProductDoc> docs = new ArrayList<>();

        // ... 解析逻辑 ...
        // 由于解析 LinkedHashMap 比较繁琐，我们假设你手动录入或后续完善
        // 这里为了跑通流程，我们模拟一条数据
        ProductDoc doc = new ProductDoc();
        doc.setId(1L);
        doc.setTitle("iPhone 15 Pro Max 钛金属");
        doc.setPrice(new BigDecimal("9999"));
        doc.setBrand("Apple");
        docs.add(doc);

        // 3. 批量保存到 ES
        productRepository.saveAll(docs);
    }

    // 搜索逻辑
    public Page<ProductDoc> search(String keyword, int page, int size) {
        // 使用 Spring Data ES 的简单查询
        // 只要 title 包含 keyword
        return null; // 下面我们在 Controller 直接用 Repository 查
    }
}