/** Copyright 2020 bejson.com */
package com.aining.mall.product.vo.voForSpuSave;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberPrice {

  private Long id;
  private String name;
  private BigDecimal price;

}
