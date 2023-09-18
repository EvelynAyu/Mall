package com.aining.mall.product.service.impl;

import com.aining.common.constant.ProductConstant;
import com.aining.common.to.SkuReductionTo;
import com.aining.common.to.SpuBoundsTo;
import com.aining.common.to.es.SkuESModel;
import com.aining.common.utils.R;
import com.aining.common.vo.SkuHasStockVo;
import com.aining.mall.product.entity.*;
import com.aining.mall.product.feign.SearchFeignService;
import com.aining.mall.product.feign.WareFeignService;
import com.aining.mall.product.service.*;
import com.aining.mall.product.vo.voForSpuSave.*;
import com.alibaba.fastjson.TypeReference;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
//    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 商品上架方法
     * @param spuId
     */
    @Override
    public void up(Long spuId) {

        // 1、查出当前spuId对应的所有sku信息,品牌的名字
        List<SkuInfoEntity> skus = skuInfoService.getSkuBySpuId(spuId);

        // TODO 4.查询当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        // 返回可以被检索的属性
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        // 转换为Set集合
        Set<Long> idSet = searchAttrIds.stream().collect(Collectors.toSet());

        // 收集attrsList数据，与esModel属性对拷
        List<SkuESModel.Attrs> attrsList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuESModel.Attrs attrs = new SkuESModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());

        // TODO 发送远程调用，库存系统查询是否有库存 hasStock
        List<Long> skuIdList = skus.stream()
                .map(SkuInfoEntity::getSkuId)
                .collect(Collectors.toList());
        // 发送远程调用:远程调用可能失败，需要try-catch
        Map<Long, Boolean> stockMap = null;
        try {
            R skuHasStock = wareFeignService.getSkusStock(skuIdList);
            //
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {};
            stockMap = skuHasStock.getData(typeReference).stream()
                    .collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
        } catch (Exception e) {
            log.error("库存服务查询异常：原因{}",e);
        }

        // 2. 封装每个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuESModel> skuESModels = skus.stream().map((sku) -> {
            // 组装需要的数据
            SkuESModel esModel = new SkuESModel();
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());

            // 3. 查询品牌和分类的名字信息
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandId(brandEntity.getBrandId());
            esModel.setBrandImg(brandEntity.getLogo());

            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            esModel.setCatalogId(categoryEntity.getCatId());
            esModel.setCatalogName(categoryEntity.getName());

            //设置检索属性
            esModel.setAttrs(attrsList);

            // 通过skuId查询是否有库存：true/false并设置库存信息
            if (finalStockMap == null) {
                esModel.setHasStock(true);
            } else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }
            // 其他属性对拷
            BeanUtils.copyProperties(sku, esModel);

            // TODO 热度评分 hasScore
            esModel.setHotScore(0L);

            return esModel;
        }).collect(Collectors.toList());

        //TODO 5、将数据发给es进行保存：mall-search
        R r = searchFeignService.productStatusUp(skuESModels);

        if (r.getCode() == 0) {
            //远程调用成功
            //TODO 6、修改当前spu的状态
            this.baseMapper.updaSpuStatus(spuId, ProductConstant.ProductStatusEnum.SPU_UP.getCode());
        } else {
            //远程调用失败
            //TODO 7、重复调用？接口幂等性:重试机制
        }
    }


    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);

        Long spuId = skuInfoEntity.getSpuId();

        //再通过spuId查询spuInfo信息表里的数据
        SpuInfoEntity spuInfoEntity = this.baseMapper.selectById(spuId);

        //查询品牌表的数据获取品牌名
        BrandEntity brandEntity = brandService.getById(spuInfoEntity.getBrandId());
        spuInfoEntity.setBrandName(brandEntity.getName());

        return spuInfoEntity;
    }



}