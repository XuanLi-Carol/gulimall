package com.test.gulimall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.test.common.to.es.SkuEsModel;
import com.test.gulimall.search.common.ElasticSearchConstant;
import com.test.gulimall.search.service.ElasticSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ElasticSaveServiceImpl implements ElasticSaveService {
    @Autowired
    private ElasticsearchClient esClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {

        BulkRequest.Builder br = new BulkRequest.Builder();

        for (SkuEsModel skuEsModel : skuEsModels) {
            br.operations(op -> op
                    .index(idx -> idx
                            .index("products")
                            .id(String.valueOf(skuEsModel.getSkuId()))
                            .document(skuEsModel)
                    )
            );
        }
        BulkResponse result = esClient.bulk(br.build());

        // Log errors, if any
        if (result.errors()) {
            log.error("Bulk had errors");
            for (BulkResponseItem item: result.items()) {
                if (item.error() != null) {
                    log.error(item.error().reason());
                }
            }
            return false;
        }

        return true;

    }
}
