package com.test.gulimall.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.AggregateBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.AvgAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonReader;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private ElasticsearchClient esClient;

    /**
     * 测试聚合
     * */
    @Test
    void agg() throws IOException {
        String searchText = "mill";
        Query addressQuery = MatchQuery.of(
                q -> q.field("address")
                        .query(searchText))
                ._toQuery();

        SearchResponse<Void> response = esClient.search(s -> s
                .index("bank")
                .size(0)
//                .query(addressQuery)
                .aggregations("ageAgg",
                        a -> a.terms(t -> t
                                .field("age")
                        )
                                .aggregations("balanceAgeAvg",
                                        a2-> a2
                                                .avg(b -> b
                                                        .field("balance")
                                                )))
                .size(10)
                .aggregations("balanceAvg",
                        a -> a.avg(avg -> avg
                                .field("balance"))
                ), Void.class);

        List<LongTermsBucket> buckets = response.aggregations()
                .get("ageAgg")
                .lterms()
                .buckets().array();

        for (LongTermsBucket bucket: buckets) {
            System.out.println("There are " + bucket.docCount() +
                    " accounts under " + bucket.key() + " age");
        }

        double balanceAvg = response.aggregations().get("balanceAvg").avg().value();
        System.out.println("The average balance is: " + balanceAvg);

    }

    /**
     * 检索数据
     * */
    @Test
    void search() throws IOException {
        String searchText = "mill";
        SearchResponse<BankAccount> response  = esClient.search(s -> s
                        .index("bank")
                        .query(q ->
                                q.match(v ->
                                        v.field("address")
                                                .query(searchText)))
                , BankAccount.class);

        TotalHits total = response.hits().total();
        boolean isExactResult = total.relation() == TotalHitsRelation.Eq;

        if (isExactResult) {
            System.out.println("There are " + total.value() + " results");
        } else {
            System.out.println("There are more than " + total.value() + " results");
        }

        List<Hit<BankAccount>> hits = response.hits().hits();
        for (Hit<BankAccount> hit: hits) {
            BankAccount bankAccount = hit.source();
            System.out.println("Found name " + bankAccount.getFirstname() + ", score " + hit.score());
        }

    }

    /**
     * 测试保存数据
     * 也可以更新
     * */
    @Test
    void index() throws IOException {
        User user = new User();
        user.setUsername("lisi");
        user.setGender("F");
        user.setAge(18);
//        String json = new ObjectMapper().writer().writeValueAsString(user);
//        Reader input = new StringReader(json);
//        IndexRequest<JsonData> request = IndexRequest.of(i -> i
//                .index("users")
//                .id("1")
//                .withJson(input)
//        );
        IndexRequest<User> request = IndexRequest.of(i ->
                i.index("users")
                        .id("2")
                        .document(user));

        IndexResponse response = esClient.index(request);

        System.out.println("Indexed with version " + response.version());
    }

    @Data
    class User{
        private String username;
        private String gender;
        private int age;
    }

    @Data
    static class BankAccount{
        //"account_number" : 1,
        //          "balance" : 39225,
        //          "firstname" : "Amber",
        //          "lastname" : "Duke",
        //          "age" : 32,
        //          "gender" : "M",
        //          "address" : "880 Holmes Lane",
        //          "employer" : "Pyrami",
        //          "email" : "amberduke@pyrami.com",
        //          "city" : "Brogan",
        //          "state" : "IL"
        private Long account_number;
        private double balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

    @Test
    void contextLoads() {
        System.out.println(esClient);
    }

}
