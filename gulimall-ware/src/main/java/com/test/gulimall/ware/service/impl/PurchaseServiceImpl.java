package com.test.gulimall.ware.service.impl;

import com.test.common.constant.WareConstant;
import com.test.gulimall.ware.entity.PurchaseDetailEntity;
import com.test.gulimall.ware.service.PurchaseDetailService;
import com.test.gulimall.ware.service.WareSkuService;
import com.test.gulimall.ware.vo.MergeVo;
import com.test.gulimall.ware.vo.PurchaseDoneVo;
import com.test.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.common.utils.PageUtils;
import com.test.common.utils.Query;

import com.test.gulimall.ware.dao.PurchaseDao;
import com.test.gulimall.ware.entity.PurchaseEntity;
import com.test.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {

        //2， 修改采购项的状态
        boolean flag = true;
        ArrayList<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item : purchaseDoneVo.getItems()) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item.getItemId());
            if(item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()){
                flag = false;
                purchaseDetailEntity.setStatus(item.getStatus());
            }else {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode());
                //3， 成功采购入库
                PurchaseDetailEntity byId = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(byId.getSkuId(),byId.getWareId(),byId.getSkuNum());
            }
            updates.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(updates);

        //1, 修改采购单的状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseDoneVo.getId());
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISHED.getCode() : WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Transactional
    @Override
    public void reveivePurchase(Long[] ids) {
        // 查询所有purchase id 的信息并修改status 和 update time
        List<PurchaseEntity> purchaseEntities = Arrays.stream(ids)
                .map(this::getById)
                .filter(purchaseEntity -> {
                    if (purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                            purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                        return true;
                    }
                    return false;})
                .peek(purchaseEntity -> {
                    purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
                    purchaseEntity.setUpdateTime(new Date());
                })
                .collect(Collectors.toList());
        // 修改采购单的状态
        this.updateBatchById(purchaseEntities);

        // 修改采购单中所有采购项的状态
        Arrays.stream(ids).forEach(id -> {
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listDetailByPurchaseId(id);
            if(purchaseDetailEntities == null || purchaseDetailEntities.isEmpty()){
                log.warn("这个采购单没有需要采购的采购项");
                return;
            }
            List<PurchaseDetailEntity> detailEntities = purchaseDetailEntities.stream().map(purchaseDetailEntity -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                detailEntity.setId(purchaseDetailEntity.getId());
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return detailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(detailEntities);
        });
    }

    @Transactional
    @Override
    public void mergePurchases(MergeVo mergeVo) {
        Long purchaseId;
        if(mergeVo.getPurchaseId() == null){
            //新建一个采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        }else {
            purchaseId = mergeVo.getPurchaseId();
        }

        List<Long> items = mergeVo.getItems();
        List<PurchaseDetailEntity> purchaseDetailEntities = items.stream().map(purchaseDetailId -> {
            // update purchase detail purchase_id and status
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(purchaseDetailId);
            purchaseDetailEntity.setPurchaseId(purchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        this.purchaseDetailService.updateBatchById(purchaseDetailEntities);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Override
    public PageUtils queryUnreceiveListPage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status",0).or().eq("status",1);
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),queryWrapper);

        return new PageUtils(page);
    }
}