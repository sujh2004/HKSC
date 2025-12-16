package com.hksc.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hksc.order.entity.SeckillSku;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeckillSkuMapper extends BaseMapper<SeckillSku> {
    // MyBatis Plus 会自动提供 update 和 selectById 方法
}