package com.test.gulimall.order.dao;

import com.test.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-19 10:06:16
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
