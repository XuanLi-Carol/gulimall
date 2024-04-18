package com.test.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class SkuReductionTO {

    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private List<MemberPrice> memberPrice;
}
