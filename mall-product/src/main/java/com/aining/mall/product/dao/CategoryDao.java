package com.aining.mall.product.dao;

import com.aining.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-16 21:44:19
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
