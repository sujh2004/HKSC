package com.hksc.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hksc.order.entity.SeckillStock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeckillStockMapper extends BaseMapper<SeckillStock> {
    // MyBatis Plus 会自动提供 update 和 selectById 方法
}