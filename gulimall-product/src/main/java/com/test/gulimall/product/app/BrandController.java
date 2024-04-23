package com.test.gulimall.product.app;

import java.util.Arrays;
import java.util.Map;


import com.test.common.validator.group.AddGroup;
import com.test.common.validator.group.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.test.gulimall.product.entity.BrandEntity;
import com.test.gulimall.product.service.BrandService;
import com.test.common.utils.PageUtils;
import com.test.common.utils.R;


/**
 * 品牌
 *
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-18 21:31:37
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand /** ,BindingResult result*/){
//        if (result.hasErrors()){
//            Map<String,String> map = new HashMap<>();
//            result.getFieldErrors().forEach((item)->{
//                map.put(item.getField(),item.getDefaultMessage());
//            });
//            return R.error(400,"参数校验失败").put("data",map);
//        }else{
//            brandService.save(brand);
//        }
        brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated({UpdateGroup.class}) @RequestBody BrandEntity brand){
//		brandService.updateById(brand);
		brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
