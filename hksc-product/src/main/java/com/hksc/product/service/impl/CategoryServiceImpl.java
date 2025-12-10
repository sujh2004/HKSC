package com.hksc.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hksc.product.entity.Category;
import com.hksc.product.mapper.CategoryMapper;
import com.hksc.product.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> listWithTree() {
        //1. 查出所有分类
        List<Category> all = baseMapper.selectList(null);

        //组装父子结构
        return all.stream()
                .filter(category -> category.getParentId() == 0) // 找一级分类
                .map(menu -> {
                    menu.setChildren(getChildrens(menu, all));
                    return menu;
                })
                .sorted((a,b) -> (a.getSort() == null? 0 : a.getSort()) - (b.getSort() == null? 0 : b.getSort()))
                .collect(Collectors.toList());
    }

    private List<Category> getChildrens(Category root,List<Category> all){
        return all.stream()
                .filter(category -> category.getParentId().equals(root.getId()))
                .map(category -> {
                    category.setChildren(getChildrens(category, all));

                    return category;
                })
                .sorted((a,b) -> (a.getSort() == null? 0: a.getSort()) - (b.getSort()==null?0:b.getSort()))
                .collect(Collectors.toList());
    }
}
