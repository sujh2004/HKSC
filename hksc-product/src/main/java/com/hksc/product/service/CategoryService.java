package com.hksc.product.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hksc.product.entity.Category;
import java.util.List;

public interface CategoryService extends IService<Category> {
    List<Category> listWithTree();
}