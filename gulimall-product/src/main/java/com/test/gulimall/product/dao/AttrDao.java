package com.test.gulimall.product.dao;

import com.test.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-18 18:11:42
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<Long> listSearchableAttrsByIds(@Param("attrIds") List<Long> attrIds);
}
