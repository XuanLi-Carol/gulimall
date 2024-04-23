package com.test.gulimall.product;

import com.alibaba.fastjson.TypeReference;
import com.test.common.to.SkuHasStockTo;
import com.test.common.utils.R;
import com.test.gulimall.product.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void testRedisson()throws Exception{
        System.out.println(redissonClient);
        RLock lock = redissonClient.getLock("lock");

        RSemaphore park = redissonClient.getSemaphore("park");
        park.trySetPermits(5);

        park.acquire();
        park.release();

        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        try {
            door.await();
        } catch (InterruptedException e) {

        }

        door.countDown();
    }

    @Test
    void contextLoads() throws Exception {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        // 保存数据
//        ops.set("hello", "world:" + UUID.randomUUID());
        Boolean setIfAbsent = ops.setIfAbsent("hello2", "world: " + UUID.randomUUID());
        System.out.println("setIfAbsent result: " + setIfAbsent);

        // 查询数据
        String hello = ops.get("hello2");
        System.out.println("hello2：" + hello);
    }

}
