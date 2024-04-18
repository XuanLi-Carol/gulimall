package com.test.gulimall.coupon.dao;

import com.test.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-19 09:47:38
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
