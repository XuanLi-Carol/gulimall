package com.test.gulimall.product.service.impl;

import com.test.gulimall.product.entity.CategoryEntity;
import com.test.gulimall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.common.utils.PageUtils;
import com.test.common.utils.Query;

import com.test.gulimall.product.dao.BrandDao;
import com.test.gulimall.product.entity.BrandEntity;
import com.test.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    @Transactional
    public void updateDetail(BrandEntity brand) {
        // 更新品牌本身
        this.updateById(brand);

        if (StringUtils.isNotEmpty(brand.getName())){
            // 级联更新其他冗余字段
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());
            // TODO 级联更新更多冗余字段
        }
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(key)){
            wrapper.eq("brand_id",key).or().like("name",key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}