package com.test.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.test.common.utils.PageUtils;
import com.test.gulimall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-19 10:06:16
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

