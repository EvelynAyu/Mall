package com.aining.mall.product.vo.voForItem;

import com.aining.mall.product.entity.SkuImagesEntity;
import com.aining.mall.product.entity.SkuInfoEntity;
import com.aining.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/20 18:16
 *
 * @Descrition 用于封装详情页，后端返回给页面的信息
 */
@Data
public class SkuItemVo {
    /**
     * sku基本信息的获取  pms_sku_info
     */

    private SkuInfoEntity info;

    private boolean hasStock = true;

    /**
     * sku的图片信息pms_sku_images
     */
    private List<SkuImagesEntity> images;

    /**
     * 获取spu的销售属性组合
     */
    private List<SkuItemSaleAttrVo> saleAttr;

    /**
     * 获取spu的介绍
     */

    private SpuInfoDescEntity desc;

    /**
     * 获取spu的规格参数信息
     */
    private List<SpuItemAttrGroupVo> groupAttrs;
}
