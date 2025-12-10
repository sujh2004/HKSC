package com.hksc.product.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hksc.common.result.Result;
import com.hksc.product.entity.Product;
import com.hksc.product.mapper.ProductMapper;
import com.hksc.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductMapper productMapper;

    @GetMapping("/list")
    public Result<Page<Product>> list(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer limit,
                                      @RequestParam(required = false) String keyword){
        Page<Product> p = new Page<>(page,limit);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), Product::getTitle, keyword);
        wrapper.eq(Product::getStatus,1);
        productMapper.selectPage(p,wrapper);
        return Result.success(p);
    }

    //详情
    @GetMapping("/detail/{id}")
    public Result<Product> detail(@PathVariable Long id){
        return Result.success(productMapper.selectById(id));
    }
}
