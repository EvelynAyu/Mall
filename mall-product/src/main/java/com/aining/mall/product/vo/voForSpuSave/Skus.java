/** Copyright 2020 bejson.com */
package com.aining.mall.product.vo.voForSpuSave;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Skus {
  /**
   * sku的销售属性
   */
  private String skuName;
  private BigDecimal price;
  private String skuTitle;
  private String skuSubtitle;
  /**
   * sku的图片信息：pms_sku_images
   */
  private List<Images> images;
  private List<String> descar;
  /**
   * sku的销售属性值：pms_sku_sale_attr_value
   */
  private List<Attr> attr;

  /**
   * sku的优惠及满减信息：sms_sku_ladder
   */
  private int fullCount;
  private BigDecimal discount;

  private int countStatus;

  /**
   * sku的满减信息：sms_sku_full_reduction
   */
  private BigDecimal fullPrice;
  private BigDecimal reducePrice;


  private int priceStatus;
  private List<MemberPrice> memberPrice;

}
