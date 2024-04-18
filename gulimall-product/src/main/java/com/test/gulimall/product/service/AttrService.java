package com.test.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.test.common.utils.PageUtils;
import com.test.gulimall.product.entity.AttrEntity;
import com.test.gulimall.product.vo.AttrRespVO;
import com.test.gulimall.product.vo.AttrVO;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-18 18:11:42
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVO attrVO);

    PageUtils queryBaseAttrListPage(Map<String, Object> params, Long catelogId, String attrType);

    AttrRespVO getAttrInfos(Long attrId);

    void updateAttr(AttrVO attr);

    List<AttrEntity> getAttrRelations(Long attrgoupId);

    PageUtils getNoAttrRelations(Map<String, Object> params, Long attrgoupId);
}

