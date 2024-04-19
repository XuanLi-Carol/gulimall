package com.test.gulimall.search.service;

import com.test.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ElasticSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
