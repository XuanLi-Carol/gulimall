package com.test.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.test.common.constant.ProductConstant;
import com.test.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.test.gulimall.product.dao.AttrGroupDao;
import com.test.gulimall.product.dao.CategoryDao;
import com.test.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.test.gulimall.product.entity.AttrGroupEntity;
import com.test.gulimall.product.entity.CategoryEntity;
import com.test.gulimall.product.service.CategoryService;
import com.test.gulimall.product.vo.AttrRespVO;
import com.test.gulimall.product.vo.AttrVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.common.utils.PageUtils;
import com.test.common.utils.Query;

import com.test.gulimall.product.dao.AttrDao;
import com.test.gulimall.product.entity.AttrEntity;
import com.test.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<Long> listSearchableAttrsByIds(List<Long> attrIds) {
        return this.baseMapper.listSearchableAttrsByIds(attrIds);
    }

    @Override
    public PageUtils getNoAttrRelations(Map<String, Object> params, Long attrgoupId) {
        //1,获取当前分组下的分类
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgoupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        //2，当前分组(attr_group)下分类（catelog）的所有分组
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> attrGroupIdList = attrGroupEntities.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());

        //3，这些分组关联的关联关系
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao
                .selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", attrGroupIdList));
        List<Long> alreadyRelatedAttrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

        //4， 从当前分类的所有属性中剔除已经有关联关系的属性
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId);
        if(!alreadyRelatedAttrIds.isEmpty()){
            queryWrapper.notIn("attr_id", alreadyRelatedAttrIds);
        }
        String key = (String) params.get("key");
        if(StringUtils.isNotBlank(key)){
            queryWrapper.and((wrapper)->{
                wrapper.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    public List<AttrEntity> getAttrRelations(Long attrgoupId) {
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgoupId));


        if (relationEntities == null || relationEntities.isEmpty()){
            return new ArrayList<>();
        }

        List<Long> attrIds = relationEntities.stream()
                .map(AttrAttrgroupRelationEntity::getAttrId)
                .collect(Collectors.toList());

        return this.listByIds(attrIds);
    }

    @Transactional
    @Override
    public void updateAttr(AttrVO attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.updateById(attrEntity);

        //更新关联关系
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
        attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());

        if(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() == attrEntity.getAttrType()){
            Long count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if (count != null && count > 0){
                attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity,
                        new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attr.getAttrId()));
            }else{
                attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
            }
        }
    }

    @Override
    public AttrRespVO getAttrInfos(Long attrId) {
        AttrRespVO result = new AttrRespVO();

        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity,result);

//        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
        if(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() == attrEntity.getAttrType()){
            AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (relationEntity != null && relationEntity.getAttrGroupId() != null){
                Long attrGroupId = relationEntity.getAttrGroupId();
                result.setAttrGroupId(attrGroupId);
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                if(attrGroupEntity != null){
                    result.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        if (attrEntity != null){
            Long catelogId = attrEntity.getCatelogId();
            Long[] paths = categoryService.findCatelogPaths(catelogId);
            result.setCatelogPath(paths);
//            result.setcat
        }


        return result;
    }

    @Override
    public PageUtils queryBaseAttrListPage(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type","base".equalsIgnoreCase(attrType)? 1:0);

        if(catelogId != 0){
            queryWrapper.eq("catelog_id",catelogId);
        }

        String key = (String) params.get("key");
        if (StringUtils.isNotBlank(key)){
            queryWrapper.and((wrapper)->{
                wrapper.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),queryWrapper);
        PageUtils pageUtils = new PageUtils(page);

        List<AttrEntity> attrEntities = page.getRecords();
        List<AttrRespVO> respVOS = attrEntities.stream().map((attrEntity) -> {
            AttrRespVO attrRespVO = new AttrRespVO();
            BeanUtils.copyProperties(attrEntity, attrRespVO);

            // 获取 catelog_name
            Long categoryId = attrEntity.getCatelogId();
            CategoryEntity categoryEntity = categoryDao.selectById(categoryId);
            if (categoryEntity != null){
                attrRespVO.setCatelogName(categoryEntity.getName());
            }

            // 获取 attrgroup_name
            if (ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() == attrEntity.getAttrType()){
                AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.selectOne(
                        new QueryWrapper<AttrAttrgroupRelationEntity>()
                                .eq("attr_id", attrEntity.getAttrId()));
                if (relationEntity != null && relationEntity.getAttrGroupId() != null){
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    attrRespVO.setGroupName(attrGroupEntity.getAttrGroupName());
//                attrRespVO.setAttrGroupId(attrGroupId);
                }
            }
            return attrRespVO;
        }).collect(Collectors.toList());

        pageUtils.setList(respVOS);

        return pageUtils;
    }

    @Override
    @Transactional
    public void saveAttr(AttrVO attrVO) {
        AttrEntity entity = new AttrEntity();
        BeanUtils.copyProperties(attrVO,entity);
        this.save(entity);

        if(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() == entity.getAttrType() && attrVO.getAttrGroupId() != null){
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attrVO.getAttrGroupId());
            relationEntity.setAttrId(entity.getAttrId());
            attrAttrgroupRelationDao.insert(relationEntity);
        }
    }
}