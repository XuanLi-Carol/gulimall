package com.test.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatelogLevel2Vo {
    private String catalog1Id;

    private String id;
    private String name;

    private List<CatelogLevel3Vo> catalog3List;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CatelogLevel3Vo{
        private String catalog2Id;
        private String id;
        private String name;
    }

}
