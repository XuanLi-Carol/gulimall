package com.test.gulimall.product.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.common.utils.PageUtils;
import com.test.common.utils.Query;

import com.test.gulimall.product.dao.SkuInfoDao;
import com.test.gulimall.product.entity.SkuInfoEntity;
import com.test.gulimall.product.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        /**
         * key: 华为
         * catelogId: 225
         * brandId: 6
         * min: 0
         * max: 0
         * */
        QueryWrapper<SkuInfoEntity> queryMapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(StringUtils.isNotEmpty(key)){
            queryMapper.and(wrapper -> {
                wrapper.eq("sku_id",key).or().like("sku_name",key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if(StringUtils.isNotEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            queryMapper.eq("catalog_id",catelogId);
        }
        String brandId = (String) params.get("brandId");
        if(StringUtils.isNotEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            queryMapper.eq("brand_id",brandId);
        }
        String min = (String) params.get("min");
        if(StringUtils.isNotEmpty(min)){
            queryMapper.gt("price",min);
        }
        String max = (String) params.get("max");
        if(StringUtils.isNotEmpty(max)){
            try {
                BigDecimal maxBigDecimal = new BigDecimal(max);
                if (maxBigDecimal.compareTo(new BigDecimal("0")) > 0){
                    queryMapper.lt("price",max);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), queryMapper);

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }
}