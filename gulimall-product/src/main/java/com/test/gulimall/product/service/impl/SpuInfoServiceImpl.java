package com.test.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.test.common.constant.ProductConstant;
import com.test.common.to.SkuHasStockTo;
import com.test.common.to.SkuReductionTO;
import com.test.common.to.SpuBoundTo;
import com.test.common.to.es.SkuEsModel;
import com.test.common.utils.R;
import com.test.gulimall.product.entity.*;
import com.test.gulimall.product.service.*;
import com.test.gulimall.product.service.feign.CouponFeignService;
import com.test.gulimall.product.service.feign.SearchFeignService;
import com.test.gulimall.product.service.feign.WareFeignService;
import com.test.gulimall.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.common.utils.PageUtils;
import com.test.common.utils.Query;

import com.test.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    private void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public void up(Long spuId) {
        //1， 查到spuId下所有的skuinfo
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);

        /**
         * attrs :
         * private Long attrId;
         * private String attrName;
         * private String attrValue;
         * 根据 spuId 查到所有可检索的属性
         * */
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = productAttrValueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<Long> searchableAttrIds = attrService.listSearchableAttrsByIds(attrIds);
        Set<Long> searchableIdsSet = new HashSet<>(searchableAttrIds);
        List<SkuEsModel.Attrs> attrs = productAttrValueEntities.stream()
                .filter(productAttrValueEntity -> searchableIdsSet.contains(productAttrValueEntity.getAttrId())).map(productAttrValueEntity -> {
                    SkuEsModel.Attrs  attr= new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(productAttrValueEntity, attr);
                    return attr;
                }).collect(Collectors.toList());


        // 调用远程仓库服务检查是否有库存 hasStock
        List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        Map<Long, Boolean> skuHasStock = null;
        try {
            R listR = wareFeignService.hasStock(skuIds);
            skuHasStock = listR.getData(new TypeReference<List<SkuHasStockTo>>() {}).stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
        } catch (Exception e) {
            log.error("远程查询仓库失败，原因：{}",e);
        }


        Map<Long, Boolean> finalSkuHasStock = skuHasStock;
        List<SkuEsModel> upProducts = skuInfoEntities.stream().map(skuInfoEntity -> {
            SkuEsModel product = new SkuEsModel();
            BeanUtils.copyProperties(skuInfoEntity,product);

            // skuPrice
            product.setSkuPrice(skuInfoEntity.getPrice());
            // skuImg
            product.setSkuImg(skuInfoEntity.getSkuDefaultImg());
            //设置 hasStock
            product.setHasStock(finalSkuHasStock.get(skuInfoEntity.getSkuId()));

            // TODO: 热度评分，暂时默认给0  hotScore
            product.setHotScore(0l);
            // brandName;
            // brandImg;
            BrandEntity brandEntity = brandService.getById(skuInfoEntity.getBrandId());
            product.setBrandName(brandEntity.getName());
            product.setBrandImg(brandEntity.getLogo());
            // catalogName;
            CategoryEntity categoryEntity = categoryService.getById(skuInfoEntity.getCatalogId());
            product.setCatalogName(categoryEntity.getName());
            //set sttrs
            product.setAttrs(attrs);
            return product;
        }).collect(Collectors.toList());

        // TODO: 将数据发送给es进行保存： gulimall-search
        R upResult = searchFeignService.productStatusUp(upProducts);
        if(upResult.getCode() == 0){
            // 上架成功，修改spuinfo的publish_status
            this.baseMapper.updatePublishStatus(spuId, ProductConstant.ProductUpStatusEnum.SPU_UP.getCode());
        }else{
            // 上架失败
            this.baseMapper.updatePublishStatus(spuId, ProductConstant.ProductUpStatusEnum.SPU_DOWN.getCode());
        }

    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        /**
         * status:
         * key:
         * brandId: 0
         * catelogId: 0
         * */
        String key = (String) params.get("key");
        if(StringUtils.isNotEmpty(key)){
            wrapper.and(obj -> {
                obj.eq("id",key).or().like("spu_name",key);
            });
        }

        String status = (String) params.get("status");
        if(StringUtils.isNotEmpty(status)){
            wrapper.eq("publish_status",status);
        }

        String brandId = (String) params.get("brandId");
        if(StringUtils.isNotEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if(StringUtils.isNotEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVO spuInfo) {
        //1, 保存spu的基本信息  `pms_spu_info`
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        Long spuId = spuInfoEntity.getId();

        //2，保存spu的描述图片信息 `pms_spu_info_desc`
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescEntity.setDecript(String.join(",", spuInfo.getDecript()));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        //3，保存spu的图片信息 `pms_spu_images`
        spuImagesService.saveSpuImages(spuId, spuInfo.getImages());

        //4，保存spu的规格参数信息 `pms_product_attr_value`
        List<BaseAttrs> baseAttrs = spuInfo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(baseAttr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setSpuId(spuId);
            productAttrValueEntity.setAttrId(baseAttr.getAttrId());
            productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
            productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
            AttrEntity attrEntity = attrService.getById(baseAttr.getAttrId());
            productAttrValueEntity.setAttrName(attrEntity.getAttrName());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttrs(productAttrValueEntities);

        //5， 保存spu的积分信息  gulimall_sms -> `sms_spu_bounds`
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        Bounds bounds = spuInfo.getBounds();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuId);
        R saveSpuBoundsR = couponFeignService.saveSpuBounds(spuBoundTo);
        if (saveSpuBoundsR.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        //6, 保存spu的skus部分信息
        List<Skus> skus = spuInfo.getSkus();

        if (skus != null && !skus.isEmpty()) {
            for (Skus sku : skus) {
                AtomicReference<String> defaultImageUrl = new AtomicReference<>("");
                for (Images image : sku.getImages()) {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    if (image.getDefaultImg() == 1) {
                        defaultImageUrl.set(image.getImgUrl());
                    }
                }
                //6.1 sku的基本信息 `pms_sku_info`
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setSpuId(spuId);
                skuInfoEntity.setBrandId(spuInfo.getBrandId());
                skuInfoEntity.setCatalogId(spuInfo.getCatalogId());
                skuInfoEntity.setSaleCount(0l);
                skuInfoEntity.setSkuDefaultImg(defaultImageUrl.get());
                skuInfoService.saveSkuInfo(skuInfoEntity);
//                skuInfoService.save(skuInfoEntity);

                //6.2 sku的图片信息 `pms_sku_images`
                List<SkuImagesEntity> skuImagesEntities = sku.getImages().stream()
                        .filter(image -> {return StringUtils.isNotEmpty(image.getImgUrl());
                }).map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                    skuImagesEntity.setImgUrl(image.getImgUrl());
                    skuImagesEntity.setDefaultImg(image.getDefaultImg());
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);

                //6.3 sku的销售属性信息 `pms_sku_sale_attr_value`
                List<Attr> attrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //6.4 sku的优惠，满减信息  gulimall_sms -> `sms_sku_ladder`，`sms_sku_full_reduction`
                SkuReductionTO skuReductionTO = new SkuReductionTO();
                BeanUtils.copyProperties(sku, skuReductionTO);
                skuReductionTO.setSkuId(skuInfoEntity.getSkuId());
                if(skuReductionTO.getFullCount() >0 && skuReductionTO.getFullPrice().compareTo(new BigDecimal("0")) > 0){
                    R saveSkuReductionR = couponFeignService.saveSkuReduction(skuReductionTO);
                    if (saveSkuReductionR.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            }
        }
    }
}