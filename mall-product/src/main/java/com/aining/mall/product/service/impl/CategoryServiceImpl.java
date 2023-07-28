package com.aining.mall.product.service.impl;

import com.aining.mall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.Query;

import com.aining.mall.product.dao.CategoryDao;
import com.aining.mall.product.entity.CategoryEntity;
import com.aining.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询数据库中的分类，并且以树形结构组装父子分类并返回
     * @return
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. 查询所有分类数据
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);
        // 2. 组装父子树形结构
        // 2.1 找到所有的一级分类
        List<CategoryEntity> levelMenu1 = categoryEntityList.stream().filter(categoryEntity ->
            categoryEntity.getParentCid() == 0
        ).map((categoryCur)->{
            // 将当前菜单的自分类保存进去
            categoryCur.setChildren(getChildrens(categoryCur, categoryEntityList));
            return categoryCur;
        }).sorted((categoryCur1, categoryCur2)->{
            return (categoryCur1.getSort() == null?0:categoryCur1.getSort()) - (categoryCur2.getSort() == null?0:categoryCur2.getSort());
        }).collect(Collectors.toList());


        return  levelMenu1;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 检查当前删除的菜单是否被别的地方引用

        // 批量删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        findParentPath(catelogId, paths);
        Collections.reverse(paths);
        return (Long[]) paths.toArray(new Long[paths.size()]);
    }
    private void findParentPath(Long catelogId, List<Long> paths){
        // 1. 收集当前节点的id
        paths.add(catelogId);
        CategoryEntity category = this.getById(catelogId);
        if(category.getParentCid()!= 0){
            findParentPath(category.getParentCid(), paths);
        }
//        return paths;
    }

    /**
     * 级联更新所有关联的数据
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        // 更新category表格
        this.updateById(category);
        // 同步更新其他关联表中的数据
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }



    /**
     * 递归查找当前菜单的子菜单
     */
    public List<CategoryEntity> getChildrens(CategoryEntity categoryCur, List<CategoryEntity> categoryEntityList){
        List<CategoryEntity> childrens = categoryEntityList.stream().filter(categoryEntity -> {
            // 当菜单的父id为categoryCur的id(catId),则该菜单为categoryCurd的子菜单
            return categoryEntity.getParentCid().equals(categoryCur.getCatId());
        }).map(categoryEntity -> {
            // 找到子菜单的子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, categoryEntityList));
            return categoryEntity;
        }).sorted((categoryCur1, categoryCur2) -> {
            return (categoryCur1.getSort() == null?0:categoryCur1.getSort()) - (categoryCur2.getSort() == null?0:categoryCur2.getSort());
        }).collect(Collectors.toList());

        return childrens;
    }

}