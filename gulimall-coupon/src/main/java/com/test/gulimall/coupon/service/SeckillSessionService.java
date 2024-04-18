package com.test.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.test.common.utils.PageUtils;
import com.test.gulimall.coupon.entity.SeckillSessionEntity;

import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-19 09:47:38
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

