package com.test.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import com.test.gulimall.product.entity.BrandEntity;
import com.test.gulimall.product.vo.BrandVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.test.gulimall.product.entity.CategoryBrandRelationEntity;
import com.test.gulimall.product.service.CategoryBrandRelationService;
import com.test.common.utils.PageUtils;
import com.test.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-18 21:31:37
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /***
     *
     * /brands/list
     * 查找当前catId下所有的品牌
     *
     * */

    @RequestMapping("/brands/list")
    public R relationBrandList(@RequestParam Long catId){
        List<BrandEntity> brandEntities = categoryBrandRelationService.getBrandsByCatId(catId);

        List<BrandVO> data = brandEntities.stream().map((brandEntity) -> {
            BrandVO brandVO = new BrandVO();
            brandVO.setBrandId(brandEntity.getBrandId());
            brandVO.setBrandName(brandEntity.getName());
            return brandVO;
        }).collect(Collectors.toList());

        return R.ok().put("data", data);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 获取当前品牌关联的所有分类列表
     */
    @GetMapping("/catelog/list")
    public R listCatelogsByBrandId(@RequestParam Long brandId){
        List<CategoryBrandRelationEntity> list = categoryBrandRelationService.catelogList(brandId);

        return R.ok().put("data", list);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
