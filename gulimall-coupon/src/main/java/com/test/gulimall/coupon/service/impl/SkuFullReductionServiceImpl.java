package com.test.gulimall.coupon.service.impl;

import com.test.common.to.MemberPrice;
import com.test.common.to.SkuReductionTO;
import com.test.gulimall.coupon.entity.MemberPriceEntity;
import com.test.gulimall.coupon.entity.SkuLadderEntity;
import com.test.gulimall.coupon.service.MemberPriceService;
import com.test.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.common.utils.PageUtils;
import com.test.common.utils.Query;

import com.test.gulimall.coupon.dao.SkuFullReductionDao;
import com.test.gulimall.coupon.entity.SkuFullReductionEntity;
import com.test.gulimall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public void saveSkuReduction(SkuReductionTO skuReductionTO) {
        // 保存sms_sku_ladder` 满减打折信息
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTO.getSkuId());
        skuLadderEntity.setDiscount(skuReductionTO.getDiscount());
        skuLadderEntity.setFullCount(skuReductionTO.getFullCount());
        skuLadderEntity.setAddOther(skuReductionTO.getCountStatus());
        if(skuReductionTO.getFullCount() > 0){
            this.skuLadderService.save(skuLadderEntity);
        }

        // 保存`sms_sku_full_reduction` 满减信息
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        skuFullReductionEntity.setSkuId(skuReductionTO.getSkuId());
        skuFullReductionEntity.setFullPrice(skuReductionTO.getFullPrice());
        skuFullReductionEntity.setReducePrice(skuReductionTO.getReducePrice());
        skuFullReductionEntity.setAddOther(skuReductionTO.getCountStatus());
        if(skuReductionTO.getFullPrice().compareTo(new BigDecimal("0")) > 0){
            this.save(skuFullReductionEntity);
        }

        // 保存`sms_member_price` 会员价格信息
        List<MemberPrice> memberPrice = skuReductionTO.getMemberPrice();
        List<MemberPriceEntity> memberPriceEntities = memberPrice.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuReductionTO.getSkuId());
            memberPriceEntity.setMemberPrice(item.getPrice());
            memberPriceEntity.setMemberLevelId(item.getId());
            memberPriceEntity.setMemberLevelName(item.getName());
            return memberPriceEntity;
        }).filter(item -> {
            return item.getMemberPrice().compareTo(new BigDecimal("0")) > 0;
        }).collect(Collectors.toList());
        this.memberPriceService.saveBatch(memberPriceEntities);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

}