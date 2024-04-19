package com.test.gulimall.product;

import com.alibaba.fastjson.TypeReference;
import com.test.common.to.SkuHasStockTo;
import com.test.common.utils.R;
import com.test.gulimall.product.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
class GulimallProductApplicationTests {

//    @Autowired
//    OSSClient ossClient;

    @Autowired
    private CategoryService categoryService;

    @Test
    public void test(){
        SkuHasStockTo skuHasStockTo = new SkuHasStockTo();
        skuHasStockTo.setSkuId(2l);
        skuHasStockTo.setHasStock(true);
        List<SkuHasStockTo> list = new ArrayList<>();
        list.add(skuHasStockTo);

        R data = R.ok().data(list);

        List<SkuHasStockTo> data1 = data.getData(new TypeReference<List<SkuHasStockTo>>() {});

        System.out.println("==============");
    }

    @Test
    void contextLoads() throws Exception {
    }

}
