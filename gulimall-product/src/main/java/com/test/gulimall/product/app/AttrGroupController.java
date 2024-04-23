package com.test.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.test.gulimall.product.entity.AttrEntity;
import com.test.gulimall.product.service.AttrAttrgroupRelationService;
import com.test.gulimall.product.service.AttrService;
import com.test.gulimall.product.service.CategoryService;
import com.test.gulimall.product.vo.AttrAttrGroupRelationVO;
import com.test.gulimall.product.vo.AttrGroupWithAttrsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.test.gulimall.product.entity.AttrGroupEntity;
import com.test.gulimall.product.service.AttrGroupService;
import com.test.common.utils.PageUtils;
import com.test.common.utils.R;



/**
 * 属性分组
 *
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-18 21:31:37
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    /**
     * /product/attrgroup/225/withattr
     * */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId){
        List<AttrGroupWithAttrsVO> data = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data",data);
    }


    /**
     * product/attrgroup/attr/relation
     * */
    @PostMapping("/attr/relation")
    public R addAttrRelation(@RequestBody List<AttrAttrGroupRelationVO> relationVOS){
        relationService.saveBatch(relationVOS);
        return R.ok();
    }

    /**
     * /{attr_group_id}/attr/relation
     *
     */
    @GetMapping("/{attrgoupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgoupId")Long attrgoupId){
        List<AttrEntity> attrEntities = attrService.getAttrRelations(attrgoupId);
        return R.ok().put("data", attrEntities);
    }

    /**
     * /{attr_group_id}/noattr/relation
     *
     */
    @GetMapping("/{attrgoupId}/noattr/relation")
    public R attrNoRelation(@RequestParam Map<String, Object> params,@PathVariable("attrgoupId")Long attrgoupId){
        PageUtils page = attrService.getNoAttrRelations(params, attrgoupId);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("catelogId") Long catelogId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params,catelogId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        attrGroup.setCatelogPath(categoryService.findCatelogPaths(attrGroup.getCatelogId()));

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    /**
     * product/attrgroup/attr/relation/delete
     * 删除attr 和 attr_group的关联关系
     * */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrAttrGroupRelationVO[] relationVOS){
        attrGroupService.deleteRelation(relationVOS);
        return R.ok();
    }

}
