package com.test.gulimall.product.service.impl;

import com.test.gulimall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.common.utils.PageUtils;
import com.test.common.utils.Query;

import com.test.gulimall.product.dao.CategoryDao;
import com.test.gulimall.product.entity.CategoryEntity;
import com.test.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params),new QueryWrapper<>());

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);

        if(StringUtils.isNotEmpty(category.getName())){
            categoryBrandRelationService.updateCatelog(category.getCatId(),category.getName());
        }

    }

    @Override
    public Long[] findCatelogPaths(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        getParentPath(catelogId, paths);
        Collections.reverse(paths);
        return paths.toArray(new Long[paths.size()]);
    }

    private void getParentPath(Long catelogId,List<Long> paths){
        paths.add(catelogId);
        CategoryEntity entity = this.getById(catelogId);
        if(entity.getParentCid() != 0){
            getParentPath(entity.getParentCid(),paths);
        }
    }

    @Override
    public List<CategoryEntity> queryCategoryTree() {
        List<CategoryEntity> allEntities = baseMapper.selectList(null);

        List<CategoryEntity> level1 = allEntities
                .stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0
                ).map((categoryEntity) -> {
                    // 递归拿到当前分类的所有子分类
                    categoryEntity.setChildren(getChildrenEntity(categoryEntity,allEntities));
                    return categoryEntity;
                }).sorted((category1, category2) -> {
                    return (category1.getSort() == null ? 0 : category1.getSort()) -
                            (category2.getSort() == null ? 0 : category2.getSort());
                })
                .collect(Collectors.toList());

        return level1;
    }

    private List<CategoryEntity> getChildrenEntity(CategoryEntity root, List<CategoryEntity> allEntities) {

        List<CategoryEntity> children = allEntities.stream()
                .filter(entity -> entity.getParentCid() == root.getCatId())
                .map((entity) -> {
                    // 递归拿到当前分类的所有子分类
                    entity.setChildren(getChildrenEntity(entity, allEntities));
                    return entity;
                })
                .sorted((category1, category2) -> {
                    return (category1.getSort() == null ? 0 : category1.getSort()) -
                            (category2.getSort() == null ? 0 : category2.getSort());
                }).collect(Collectors.toList());
        return children;
    }

}