package com.aining.mall.product;

import com.aining.mall.product.dao.AttrGroupDao;
import com.aining.mall.product.dao.SkuSaleAttrValueDao;
import com.aining.mall.product.entity.BrandEntity;
import com.aining.mall.product.service.BrandService;
import com.aining.mall.product.service.CategoryService;
import com.aining.mall.product.vo.voForItem.SkuItemSaleAttrVo;
import com.aining.mall.product.vo.voForItem.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@SpringBootTest
@Slf4j
@RunWith(SpringRunner.class)
public class MallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Resource
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Resource
    private AttrGroupDao attrGroupDao;

    @Test
    public void contextLoads() {
        BrandEntity brand = new BrandEntity();
        brand.setName("华为");
        brandService.save(brand);
        System.out.println("保存成功");

        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1));
        list.forEach((item)->{
            System.out.println(item);
        });
    }

    @Test
    public void testFindPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径：{}", Arrays.asList(catelogPath));
    }

    @Test
    public void testStringRedisTemplate(){
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        // 保存
        opsForValue.set("hello", "word_" + UUID.randomUUID().toString());
        // 查询
        String hello = opsForValue.get("hello");
        System.out.println("测试redis:" + hello);

    }

    @Test
    public void redissonTest(){
        System.out.println(redissonClient);

    }

    @Test
    public void test() {
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(11L, 225L);
        attrGroupWithAttrsBySpuId.forEach(System.out::println);
    }


}
