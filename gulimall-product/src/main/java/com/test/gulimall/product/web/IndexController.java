package com.test.gulimall.product.web;

import com.test.gulimall.product.entity.CategoryEntity;
import com.test.gulimall.product.service.CategoryService;
import com.test.gulimall.product.vo.CatelogLevel2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/","index.html"})
    public String index(Model model){
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categories();
        model.addAttribute("categories",categoryEntities);
        //classpath:/templates/ + 返回值 + ".html"
        return "index";
    }

    @GetMapping("/index/catelog.json")
    @ResponseBody
    public Map<String,List<CatelogLevel2Vo>> getCatelogJson(){
        return categoryService.getCatelogJson();
    }
}
