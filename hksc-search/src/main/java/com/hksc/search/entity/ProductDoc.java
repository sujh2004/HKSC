package com.hksc.search.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
// indexName = 索引名 (类似数据库表名), createIndex = true (启动时自动创建索引)
@Document(indexName = "product")
public class ProductDoc implements Serializable {

    @Id
    private Long id;

    // type = Text 表示支持分词搜索, analyzer = "standard" (标准分词，中文支持一般但不用装插件)
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Keyword) // Keyword 表示不分词，必须完全匹配
    private String brand;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Keyword)
    private String image;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Integer)
    private Integer stock;
}