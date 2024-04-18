package com.test.gulimall.product.dao;

import com.test.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-18 18:11:42
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
