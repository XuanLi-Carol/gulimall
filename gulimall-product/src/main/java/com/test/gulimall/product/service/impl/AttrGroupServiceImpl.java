package com.test.gulimall.product.service.impl;

import com.test.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.test.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.test.gulimall.product.entity.AttrEntity;
import com.test.gulimall.product.service.AttrService;
import com.test.gulimall.product.vo.AttrAttrGroupRelationVO;
import com.test.gulimall.product.vo.AttrGroupWithAttrsVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.common.utils.PageUtils;
import com.test.common.utils.Query;

import com.test.gulimall.product.dao.AttrGroupDao;
import com.test.gulimall.product.entity.AttrGroupEntity;
import com.test.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Autowired
    private AttrService attrService;

    @Override
    public void deleteRelation(AttrAttrGroupRelationVO[] relationVOS) {
        List<AttrAttrgroupRelationEntity> relationEntities = Arrays.stream(relationVOS).map((relationVO) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(relationVO, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(relationEntities);

    }

    @Override
    public List<AttrGroupWithAttrsVO> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntities = this.baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        return attrGroupEntities.stream().map(attrGroupEntity -> {
            AttrGroupWithAttrsVO attrGroupWithAttrsVO = new AttrGroupWithAttrsVO();
            BeanUtils.copyProperties(attrGroupEntity,attrGroupWithAttrsVO);
            List<AttrEntity> attrs = attrService.getAttrRelations(attrGroupEntity.getAttrGroupId());
            attrGroupWithAttrsVO.setAttrs(attrs);
            return attrGroupWithAttrsVO;
        }).collect(Collectors.toList());
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        IPage<AttrGroupEntity> page = null;
        QueryWrapper<AttrGroupEntity> wapper = new QueryWrapper<AttrGroupEntity>();
        String key = (String) params.get("key");
        if(StringUtils.isNotBlank(key)){
            wapper.and((obj)->{
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }
        if(catelogId == 0){
            page = this.page(new Query<AttrGroupEntity>().getPage(params), wapper);
        }else{
            wapper.eq("catelog_id", catelogId);
            page = this.page(new Query<AttrGroupEntity>().getPage(params),wapper);
        }
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

}