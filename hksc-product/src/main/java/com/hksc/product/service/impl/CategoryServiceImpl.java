package com.hksc.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hksc.product.converter.ProductConverter;
import com.hksc.product.entity.Category;
import com.hksc.product.mapper.CategoryMapper;
import com.hksc.product.service.CategoryService;
import com.hksc.product.vo.CategoryVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<CategoryVO> listWithTree() {
        //1. 查出所有分类
        List<Category> all = baseMapper.selectList(null);

        //2. 转换为VO并组装父子结构
        List<CategoryVO> allVOs = all.stream()
                .map(ProductConverter::categoryToVO)
                .collect(Collectors.toList());

        //3. 组装树形结构
        return allVOs.stream()
                .filter(vo -> vo.getParentId() == 0) // 找一级分类
                .map(vo -> {
                    vo.setChildren(getChildrens(vo, allVOs));
                    return vo;
                })
                .sorted((a,b) -> (a.getSort() == null? 0 : a.getSort()) - (b.getSort() == null? 0 : b.getSort()))
                .collect(Collectors.toList());
    }

    private List<CategoryVO> getChildrens(CategoryVO root, List<CategoryVO> all){
        return all.stream()
                .filter(vo -> vo.getParentId().equals(root.getId()))
                .map(vo -> {
                    vo.setChildren(getChildrens(vo, all));
                    return vo;
                })
                .sorted((a,b) -> (a.getSort() == null? 0: a.getSort()) - (b.getSort()==null?0:b.getSort()))
                .collect(Collectors.toList());
    }
}
