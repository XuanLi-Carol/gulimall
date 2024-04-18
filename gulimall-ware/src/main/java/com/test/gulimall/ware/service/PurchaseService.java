package com.test.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.test.common.utils.PageUtils;
import com.test.gulimall.ware.entity.PurchaseEntity;
import com.test.gulimall.ware.vo.MergeVo;
import com.test.gulimall.ware.vo.PurchaseDoneVo;

import java.util.Map;

/**
 * 采购信息
 *
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-19 10:12:02
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryUnreceiveListPage(Map<String, Object> params);

    void mergePurchases(MergeVo mergeVo);

    void reveivePurchase(Long[] ids);

    void done(PurchaseDoneVo purchaseDoneVo);
}

