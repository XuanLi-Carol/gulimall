package com.test.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.test.gulimall.product.service.CategoryBrandRelationService;
import com.test.gulimall.product.vo.CatelogLevel2Vo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.common.utils.PageUtils;
import com.test.common.utils.Query;

import com.test.gulimall.product.dao.CategoryDao;
import com.test.gulimall.product.entity.CategoryEntity;
import com.test.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params),new QueryWrapper<>());

        return new PageUtils(page);
    }

    @Override
    @Transactional
//    @CacheEvict(value = {"category"},key = "'getLevel1Categories'")
    @CacheEvict(value = {"category"},allEntries = true)
//    @Caching(evict = {
//            @CacheEvict(value = {"category"},key = "'getLevel1Categories'"),
//            @CacheEvict(value = {"category"},key = "'getCatelogJson'")})
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);

        if(StringUtils.isNotEmpty(category.getName())){
            categoryBrandRelationService.updateCatelog(category.getCatId(),category.getName());
        }

    }

    @Override
    @Cacheable(value = {"category"},key = "#root.method.name")
    public Map<String, List<CatelogLevel2Vo>> getCatelogJson() {
        System.out.println("查询了数据库...");
        List<CategoryEntity> allCategories = this.baseMapper.selectList(null);
        // 1, 查到所有一级分类
        List<CategoryEntity> level1Categories = getCategoriesByParentId(allCategories,0l);

        //2, 封装二级分类数据
        Map<String, List<CatelogLevel2Vo>> result = level1Categories.stream().collect(Collectors.toMap(k -> String.valueOf(k.getCatId()), v -> {
            List<CategoryEntity> level2CategoryEntities = getCategoriesByParentId(allCategories, v.getCatId());
            List<CatelogLevel2Vo> level2List = new ArrayList<>();
            if (level2CategoryEntities != null && !level2CategoryEntities.isEmpty()) {
                level2List = level2CategoryEntities.stream().map(level2 -> {
                    CatelogLevel2Vo catelogLevel2Vo = null;
                    // 查询当前二级分类下所有的三级分类并封装数据
                    List<CategoryEntity> level3CategoryEntities = getCategoriesByParentId(allCategories, level2.getCatId());
                    if (level3CategoryEntities != null && !level3CategoryEntities.isEmpty()) {
                        List<CatelogLevel2Vo.CatelogLevel3Vo> level3List = level3CategoryEntities.stream().map(level3 -> {
                            return new CatelogLevel2Vo.CatelogLevel3Vo(String.valueOf(level2.getCatId()), String.valueOf(level3.getCatId()), level3.getName());
                        }).collect(Collectors.toList());
                        catelogLevel2Vo = new CatelogLevel2Vo(String.valueOf(v.getCatLevel()), String.valueOf(level2.getCatId()), level2.getName(), level3List);
                    }
                    return catelogLevel2Vo;
                }).collect(Collectors.toList());
            }
            return level2List;
        }));
        return result;
    }

    public Map<String, List<CatelogLevel2Vo>> getCatelogJson2() {
        //1, 从缓存中获取数据
        String cacheKey = "catelogJson";
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String catelogJson = ops.get(cacheKey);

        if (StringUtils.isBlank(catelogJson)){
            // 从数据库中获取实时数据
            Map<String, List<CatelogLevel2Vo>> catelogJsonFromDb = getCatelogJsonFromDbWithRedisLock();
            return catelogJsonFromDb;
        }
        System.out.println("命中缓存，不查询数据库...");
        // 缓存中有数据
        return JSON.parseObject(catelogJson,new TypeReference<Map<String, List<CatelogLevel2Vo>>>(){});
    }
    public Map<String, List<CatelogLevel2Vo>> getCatelogJsonFromDbWithRedisLock() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        // 获取分布式锁， 获取锁 + 过期时间 必须时原子操作
        String lock = "lock";
        String value = UUID.randomUUID().toString();
        Boolean getLock = ops.setIfAbsent(lock, value, 300, TimeUnit.SECONDS);
        if (getLock){
            //获取到分布式锁，执行业务逻辑
            System.out.println("获取到分布式锁 ...");
            Map<String, List<CatelogLevel2Vo>> result;
            try {
                result = getCategoryDataFroDb();
            } finally {
                //删除分布式锁，查询锁的具体值和删除锁必须是原子操作
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script,Long.class),Arrays.asList(lock),value);
            }
            return result;
        }else{
            // 获取分布式锁失败，自旋等待锁释放
            System.out.println("获取分布式锁失败， 自旋等待...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {

            }
            return getCatelogJsonFromDbWithLocalLock();
        }

    }
    public Map<String, List<CatelogLevel2Vo>> getCatelogJsonFromDbWithLocalLock() {
        synchronized (this){
            return getCategoryDataFroDb();
        }
    }

    private Map<String, List<CatelogLevel2Vo>> getCategoryDataFroDb() {
        // 1, 再次查缓存
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String catelogJson = ops.get("catelogJson");
        if (StringUtils.isNotBlank(catelogJson)){
            System.out.println("命中缓存，在查数据库之前...");
            return JSON.parseObject(catelogJson,new TypeReference<Map<String, List<CatelogLevel2Vo>>>(){});
        }
        System.out.println("查询了数据库...");
        List<CategoryEntity> allCategories = this.baseMapper.selectList(null);
        // 1, 查到所有一级分类
        List<CategoryEntity> level1Categories = getCategoriesByParentId(allCategories,0l);

        //2, 封装二级分类数据
        Map<String, List<CatelogLevel2Vo>> result = level1Categories.stream().collect(Collectors.toMap(k -> String.valueOf(k.getCatId()), v -> {
            List<CategoryEntity> level2CategoryEntities = getCategoriesByParentId(allCategories, v.getCatId());
            List<CatelogLevel2Vo> level2List = new ArrayList<>();
            if (level2CategoryEntities != null && !level2CategoryEntities.isEmpty()) {
                level2List = level2CategoryEntities.stream().map(level2 -> {
                    CatelogLevel2Vo catelogLevel2Vo = null;
                    // 查询当前二级分类下所有的三级分类并封装数据
                    List<CategoryEntity> level3CategoryEntities = getCategoriesByParentId(allCategories, level2.getCatId());
                    if (level3CategoryEntities != null && !level3CategoryEntities.isEmpty()) {
                        List<CatelogLevel2Vo.CatelogLevel3Vo> level3List = level3CategoryEntities.stream().map(level3 -> {
                            return new CatelogLevel2Vo.CatelogLevel3Vo(String.valueOf(level2.getCatId()), String.valueOf(level3.getCatId()), level3.getName());
                        }).collect(Collectors.toList());
                        catelogLevel2Vo = new CatelogLevel2Vo(String.valueOf(v.getCatLevel()), String.valueOf(level2.getCatId()), level2.getName(), level3List);
                    }
                    return catelogLevel2Vo;
                }).collect(Collectors.toList());
            }
            return level2List;
        }));
        // 3，将数据库中获取的数据放入缓存
        String jsonString = JSON.toJSONString(result);
        ops.set("catelogJson",jsonString);
        return result;
    }

    @Override
    @Cacheable(value = {"category"},key = "#root.method.name")
    public List<CategoryEntity> getLevel1Categories() {
        System.out.println("查询数据库：getLevel1Categories");
        return getCategoriesByParentId(0l);
    }

    private List<CategoryEntity> getCategoriesByParentId(Long parentId){
        return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid",parentId));
    }

    private List<CategoryEntity> getCategoriesByParentId(List<CategoryEntity> all ,Long parentId){
        return all.stream().filter(item -> item.getParentCid().equals(parentId)).collect(Collectors.toList());
    }



    @Override
    public Long[] findCatelogPaths(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        getParentPath(catelogId, paths);
        Collections.reverse(paths);
        return paths.toArray(new Long[paths.size()]);
    }

    private void getParentPath(Long catelogId,List<Long> paths){
        paths.add(catelogId);
        CategoryEntity entity = this.getById(catelogId);
        if(entity.getParentCid() != 0){
            getParentPath(entity.getParentCid(),paths);
        }
    }

    @Override
    public List<CategoryEntity> queryCategoryTree() {
        List<CategoryEntity> allEntities = baseMapper.selectList(null);

        List<CategoryEntity> level1 = allEntities
                .stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0
                ).map((categoryEntity) -> {
                    // 递归拿到当前分类的所有子分类
                    categoryEntity.setChildren(getChildrenEntity(categoryEntity,allEntities));
                    return categoryEntity;
                }).sorted((category1, category2) -> {
                    return (category1.getSort() == null ? 0 : category1.getSort()) -
                            (category2.getSort() == null ? 0 : category2.getSort());
                })
                .collect(Collectors.toList());

        return level1;
    }

    private List<CategoryEntity> getChildrenEntity(CategoryEntity root, List<CategoryEntity> allEntities) {

        List<CategoryEntity> children = allEntities.stream()
                .filter(entity -> entity.getParentCid() == root.getCatId())
                .map((entity) -> {
                    // 递归拿到当前分类的所有子分类
                    entity.setChildren(getChildrenEntity(entity, allEntities));
                    return entity;
                })
                .sorted((category1, category2) -> {
                    return (category1.getSort() == null ? 0 : category1.getSort()) -
                            (category2.getSort() == null ? 0 : category2.getSort());
                }).collect(Collectors.toList());
        return children;
    }

}