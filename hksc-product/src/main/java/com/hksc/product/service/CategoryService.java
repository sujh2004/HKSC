package com.hksc.product.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hksc.product.entity.Category;
import com.hksc.product.vo.CategoryVO;
import java.util.List;

public interface CategoryService extends IService<Category> {
    List<CategoryVO> listWithTree();
}