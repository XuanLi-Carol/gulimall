package com.test.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.test.gulimall.product.entity.ProductAttrValueEntity;
import com.test.gulimall.product.service.ProductAttrValueService;
import com.test.gulimall.product.vo.AttrRespVO;
import com.test.gulimall.product.vo.AttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.test.gulimall.product.service.AttrService;
import com.test.common.utils.PageUtils;
import com.test.common.utils.R;



/**
 * 商品属性
 *
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-18 21:31:37
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    /**
     * update/10
     * */

    @PostMapping("/update/{spuId}")
    public R update(@PathVariable("spuId")Long spuId,@RequestBody List<ProductAttrValueEntity> attrValueEntities){
        productAttrValueService.updateBatch(spuId,attrValueEntities);
        return R.ok();
    }

    /**
     * base/listforspu/10
     * */

    @GetMapping("/base/listforspu/{spuId}")
    public R listforspu(@PathVariable("spuId")Long spuId){
        List<ProductAttrValueEntity> data = productAttrValueService.baseAttrListForSpu(spuId);
        return R.ok().put("data",data);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * product/attr/base/list/0
     * */
    @RequestMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,@PathVariable("catelogId")Long catelogId,@PathVariable("attrType")String attrType){
        PageUtils page = attrService.queryBaseAttrListPage(params,catelogId,attrType);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
//		AttrEntity attr = attrService.getById(attrId);
		AttrRespVO attr = attrService.getAttrInfos(attrId);

        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVO attrVO){
//		attrService.save(attr);
		attrService.saveAttr(attrVO);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVO attr){
//		attrService.updateById(attr);
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
