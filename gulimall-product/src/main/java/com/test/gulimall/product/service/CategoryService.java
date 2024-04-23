package com.test.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.test.common.utils.PageUtils;
import com.test.gulimall.product.entity.CategoryEntity;
import com.test.gulimall.product.vo.CatelogLevel2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-18 18:11:42
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> queryCategoryTree();

    Long[] findCatelogPaths(Long catelogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getLevel1Categories();

    Map<String, List<CatelogLevel2Vo>> getCatelogJson();

}

