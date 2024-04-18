package com.test.gulimall.product;

import com.test.gulimall.product.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class GulimallProductApplicationTests {

//    @Autowired
//    OSSClient ossClient;

    @Autowired
    private CategoryService categoryService;

    @Test
    public void test(){
        Long[] paths = categoryService.findCatelogPaths(225l);
        System.out.println("=========="+ Arrays.asList(paths));

    }

    @Test
    void contextLoads() throws Exception {
    }

}
