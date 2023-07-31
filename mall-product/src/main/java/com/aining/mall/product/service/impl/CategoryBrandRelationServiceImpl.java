package com.aining.mall.product.service.impl;

import com.aining.mall.product.dao.BrandDao;
import com.aining.mall.product.dao.CategoryDao;
import com.aining.mall.product.entity.BrandEntity;
import com.aining.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.Query;

import com.aining.mall.product.dao.CategoryBrandRelationDao;
import com.aining.mall.product.entity.CategoryBrandRelationEntity;
import com.aining.mall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    @Autowired
    BrandDao brandDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationDao categoryBrandRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryBrandRelationEntity> queryCatelogList(Long brandId) {
        List<CategoryBrandRelationEntity> catelogList = this.list(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
        return catelogList;

    }

    /**
     * 前端传参{brandId，categoryId},但categoryBrandRelationEntity中的属性为{brandId，categoryId,brandName, categoryName}
     * 所以需要在后端查询出{brandName, categoryName},保存到categoryBrandRelationEntity中
     * @param categoryBrandRelation
     */
    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();

        // 根据id查询品牌名称和分类名称
        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

        // 设置categoryBrandRelation中的品牌名称和分类名称
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());

        // 将完整的信息保存
        this.save(categoryBrandRelation);
    }

    /**
     * 当brand表中的name字段更新时，此关联表中的name字段也要进行更新
     * @param brandId
     * @param name
     */
    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandId(brandId);
        categoryBrandRelationEntity.setBrandName(name);
        // 根据brandID更新brandName，wrapper规定了更新条件
        this.update(categoryBrandRelationEntity,
                new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        this.baseMapper.updateCategory(catId, name);
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> categoryBrandRelationEntities = categoryBrandRelationDao.selectList(
                                                                new QueryWrapper<CategoryBrandRelationEntity>()
                                                                        .eq("catelog_id", catId));
        List<Long> brandIds = categoryBrandRelationEntities.stream().map((categoryBrandRelationEntity) -> {
            return categoryBrandRelationEntity.getBrandId();
        }).collect(Collectors.toList());
        List<BrandEntity> brandEntityList = brandDao.selectBatchIds(brandIds);
        return brandEntityList;
    }

}