package com.hksc.search.repository;

import com.hksc.search.entity.ProductDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

// 继承 ElasticsearchRepository，Spring Data 会自动帮你实现 CRUD
public interface ProductRepository extends ElasticsearchRepository<ProductDoc, Long> {
}