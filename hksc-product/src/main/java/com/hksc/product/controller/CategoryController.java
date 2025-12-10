package com.hksc.product.controller;

import com.hksc.common.result.Result;
import com.hksc.product.entity.Category;
import com.hksc.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/tree")
    public Result<List<Category>> tree(){
        return Result.success(categoryService.listWithTree());
    }
}
