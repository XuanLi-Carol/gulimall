package com.test.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.test.common.utils.PageUtils;
import com.test.gulimall.product.entity.BrandEntity;
import com.test.gulimall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-18 18:11:42
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryBrandRelationEntity> catelogList(Long brandId);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);


    void updateBrand(Long brandId, String name);

    void updateCatelog(Long catId, String name);

    List<BrandEntity> getBrandsByCatId(Long catId);
}

