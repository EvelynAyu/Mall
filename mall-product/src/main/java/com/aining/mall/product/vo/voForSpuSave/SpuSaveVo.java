/** Copyright 2020 bejson.com */
package com.aining.mall.product.vo.voForSpuSave;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class SpuSaveVo {
  /**
   * Spu基本信息：对应pms_spu_info
   */
  private String spuName;
  private String spuDescription;
  private Long catalogId;
  private Long brandId;
  private BigDecimal weight;
  private int publishStatus;

  /**
   * Spu的商品介绍图：对应pms_spu_info_desc
   */
  private List<String> decript;

  /**
   * Spu的图片集：对应pms_spu_images
   */
  private List<String> images;

  /**
   * spu对应的规格参数值：对应pms_product_attr_value
   */
  private List<BaseAttrs> baseAttrs;

  /**
   * spu的优惠信息：对应sms_spu_bounds
   */
  private Bounds bounds;

  /**
   * 当前spu对应的所有sku信息
   */
  private List<Skus> skus;


}
