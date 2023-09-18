package com.aining.mall.product.service.impl;

import com.aining.common.utils.R;
import com.aining.common.vo.MemberResponseVo;
import com.aining.mall.product.entity.SkuImagesEntity;
import com.aining.mall.product.entity.SpuInfoDescEntity;
import com.aining.mall.product.feign.OrderFeignService;
import com.aining.mall.product.service.*;
import com.aining.mall.product.vo.voForItem.SkuItemSaleAttrVo;
import com.aining.mall.product.vo.voForItem.SkuItemVo;
import com.aining.mall.product.vo.voForItem.SpuItemAttrGroupVo;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.Query;

import com.aining.mall.product.dao.SkuInfoDao;
import com.aining.mall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import static com.aining.common.constant.AuthServerConstant.LOGIN_USER;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Resource
    private SkuImagesService skuImagesService;

    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private AttrGroupService attrGroupService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    private ThreadPoolExecutor executor;

    @Resource
    private OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.save(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        /**
         * key:
         * catelogId: 0
         * brandId: 0
         * min: 0
         * max: 0
         */
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            queryWrapper.and((wrapper)->{
                wrapper.eq("sku_id",key).or().like("sku_name",key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId)&&!"0".equalsIgnoreCase(catelogId)){

            queryWrapper.eq("catalog_id",catelogId);
        }

        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId)&&!"0".equalsIgnoreCase(brandId)){
            queryWrapper.eq("brand_id",brandId);
        }

        String min = (String) params.get("min");
        if(!StringUtils.isEmpty(min)){
            queryWrapper.ge("price",min);
        }

        String max = (String) params.get("max");
        if(!StringUtils.isEmpty(max)){
            try{
                BigDecimal bigDecimal = new BigDecimal(max);
                if(bigDecimal.compareTo(new BigDecimal("0"))==1){
                    queryWrapper.le("price",max);
                }
            }catch (Exception e){

            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);

    }

    @Override
    public List<SkuInfoEntity> getSkuBySpuId(Long spuId) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<SkuInfoEntity>().eq("spu_id",spuId);
        // 查询spuid查询所有的sku
        List<SkuInfoEntity> skuInfoEntityList = this.list(queryWrapper);
        return skuInfoEntityList;
    }

    /**
     * 详情页：封装数据库返回给前端
     * @param skuId
     * @return
     */
    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        // 异步编排
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            /**
             * 查询sku基本信息的获取  pms_sku_info
             */
            SkuInfoEntity skuInfoEntity = getById(skuId);
            skuItemVo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, executor);

        /**
         * 获取spu销售属性组合
         */
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((skuInfoEntity) -> {
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrBySpuId(skuInfoEntity.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);

        /**
         * 获取spu介绍
         */
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((skuInfoEntity) -> {
            // 通过skuId查询spuId
            Long spuId = skuInfoEntity.getSpuId();
            // 通过spuId查询desc
            SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(spuId);
            skuItemVo.setDesc(spuInfoDesc);
        }, executor);

        /**
         * 获取spu的规格参数信息
         */
        CompletableFuture<Void> groupAttrFuture = infoFuture.thenAcceptAsync((skuInfoEntity) -> {
            // 获取当前spu的三级分类id
            Long catalogId = skuInfoEntity.getCatalogId();
            Long spuId = skuInfoEntity.getSpuId();
            List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithattrsBySpuId(spuId, catalogId);
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, executor);


        /**
         * 获取sku图片
         */
        CompletableFuture<Void> imgFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> imagesEntities = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(imagesEntities);
        }, executor);

        // 等待所有任务都完成之后才返回结果
        CompletableFuture.allOf(saleAttrFuture,descFuture,groupAttrFuture,imgFuture).get();

        return skuItemVo;
    }

    @Override
    public boolean isPurchased(Long skuId) {
        SkuInfoEntity skuInfoEntity = this.getById(skuId);
        Long spuId = skuInfoEntity.getSpuId();
        String checkPurchase = orderFeignService.checkPurchase(spuId);
        boolean buyOrNot = Boolean.parseBoolean(checkPurchase);
        return buyOrNot;
    }


}