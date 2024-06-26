package com.test.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.test.common.to.SkuHasStockTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.test.gulimall.ware.entity.WareSkuEntity;
import com.test.gulimall.ware.service.WareSkuService;
import com.test.common.utils.PageUtils;
import com.test.common.utils.R;



/**
 * 商品库存
 *
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-19 10:12:02
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;
    /**
     * 查询skuId是否有库存
     * */
    @PostMapping("/hasstock")
    public R hasStock(@RequestBody List<Long> skuIds){

        List<SkuHasStockTo> data = wareSkuService.getSkuHasStock(skuIds);
        return R.ok().data(data);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
