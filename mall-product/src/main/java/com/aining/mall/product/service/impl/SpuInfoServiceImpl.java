package com.aining.mall.product.service.impl;

import com.aining.common.to.SkuReductionTo;
import com.aining.common.to.SpuBoundsTo;
import com.aining.common.utils.R;
import com.aining.mall.product.entity.*;
import com.aining.mall.product.feign.CouponFeignService;
import com.aining.mall.product.service.*;
import com.aining.mall.product.vo.voForSpuSave.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.Query;

import com.aining.mall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    SpuImagesService spuImagesService;
    @Autowired
    AttrService attrService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    // 远程调用
    @Autowired
    CouponFeignService couponFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    //TODO 事务的处理:高级部分完善
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuInfoVo) {
        //1. 保存spu基本信息：pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfoVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        // 这里也可以直接用this.save(spuInfoEntity),然而为了接口复用，将保存spu基本信息的方法抽取出来
        this.saveBaseSpuInfo(spuInfoEntity);

        //2. 保存spu的商品介绍图片：pms_spu_info_desc
        List<String> spuInfoVoDecript = spuInfoVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        // 在pms_spu_info_desc表中，spu_id字段不自增，在上一步保存了spu基本信息之后才能拿到spu_id
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",",spuInfoVoDecript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        //3. 保存Spu的图片集：pms_spu_images
        List<String> images = spuInfoVo.getImages();
        spuImagesService.saveSpuImages(spuInfoEntity.getId(), images);

        //4， 保存Spu的规格参数值：pms_product_attr_value
        List<BaseAttrs> spuInfoVoBaseAttrs = spuInfoVo.getBaseAttrs();
        List<ProductAttrValueEntity> valueEntities = spuInfoVoBaseAttrs.stream().map((spuInfoVoBaseAttr) -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setSpuId(spuInfoEntity.getId());
            valueEntity.setAttrId(spuInfoVoBaseAttr.getAttrId());

            // 查询属性名字
            AttrEntity attrEntity = attrService.getById(spuInfoVoBaseAttr.getAttrId());
            valueEntity.setAttrName(attrEntity.getAttrName());

            valueEntity.setAttrValue(spuInfoVoBaseAttr.getAttrValues());
            valueEntity.setQuickShow(spuInfoVoBaseAttr.getShowDesc());
            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttrValue(valueEntities);

        // 5. 保存积分信息bounds
        Bounds bounds = spuInfoVo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundsTo);
        if(r.getCode() != 0){
            log.error("远程保存spu积分信息失败");
        }

        //6. 保存sku的信息
        List<Skus> skuVos = spuInfoVo.getSkus();
        // 6.1 Sku的基本信息：pms_sku_info
        skuVos.forEach(skuVo -> {
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(skuVo, skuInfoEntity);
            skuInfoEntity.setSpuId(spuInfoEntity.getId());
            skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
            skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
            // 设置默认图片:每个skuVo都有对应的图片
            List<Images> skuVoImages = skuVo.getImages();
            String defaultImage = "";
            for(Images skuVoImage: skuVoImages){
                if(skuVoImage.getDefaultImg() == 1){
                    defaultImage = skuVoImage.getImgUrl();
                }
            }
            skuInfoEntity.setSkuDefaultImg(defaultImage);
            skuInfoEntity.setSaleCount(0L);
            skuInfoService.saveSkuInfo(skuInfoEntity);

            // 保存完成之后，sku的主键id就可获得
            Long skuId = skuInfoEntity.getSkuId();
            // 5.2 保存sku的图片信息 pms_sku_images
            List<Images> skuImages = skuVo.getImages();
            List<SkuImagesEntity> skuImagesEntities = skuImages.stream().filter((skuImagesEntity)->{
                return !StringUtils.isEmpty(skuImagesEntity.getImgUrl());
            }).map((skuImage) -> {
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                skuImagesEntity.setSkuId(skuId);
                BeanUtils.copyProperties(skuImage, skuImagesEntity);
                return skuImagesEntity;
                // 只有被选中的图片才有图片路径，才能保存到数据库中,没有设置的图片不要保存到数据库中
            }).collect(Collectors.toList());
            skuImagesService.saveBatch(skuImagesEntities);


            // 5.3 sku的销售属性值：pms_sku_sale_attr_value
            List<Attr> attrs = skuVo.getAttr();
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map((attr) -> {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                skuSaleAttrValueEntity.setSkuId(skuId);
                BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                return skuSaleAttrValueEntity;
            }).collect(Collectors.toList());
            skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

            /**
             * 远程调用部分
             */
            // 5.4 sku bounds/coupon信息（涉及跨数据库:mall_sms -> sms_spu_bounds/sms_sku_ladder/sms_sku_full_reduction/sms_member_price
            // 5.4.2 保存sku的满减信息
            SkuReductionTo skuReductionTo = new SkuReductionTo();
            skuReductionTo.setSkuId(skuId);
            BeanUtils.copyProperties(skuVo, skuReductionTo);

            // 从sku中拿到memberpriceList,拷贝memberprice。这里不用BeanUtils的原因是To和Do的memberPrice的泛型不一样，会导致拷贝失败
            List<MemberPrice> memberPriceList = skuVo.getMemberPrice();
            List<com.aining.common.to.MemberPrice> memberPriceToList = memberPriceList.stream().map((memberPrice) -> {
                com.aining.common.to.MemberPrice memberPriceTo = new com.aining.common.to.MemberPrice();
                BeanUtils.copyProperties(memberPrice, memberPriceTo);
                return memberPriceTo;
            }).collect(Collectors.toList());
            skuReductionTo.setMemberPrice(memberPriceToList);

            // 只有当有满减信息/满件优惠时才保存数据
            if(skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                if (r1.getCode() != 0) {
                    log.error("远程保存sku满减信息失败");
                }
            }
        });
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.save(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("id",key).or().like("spu_name",key);
            });
        }

        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }

        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) &&!"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId) &&!"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

}