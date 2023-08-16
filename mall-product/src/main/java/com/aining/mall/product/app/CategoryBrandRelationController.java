package com.aining.mall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.aining.mall.product.entity.BrandEntity;
import com.aining.mall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.aining.mall.product.entity.CategoryBrandRelationEntity;
import com.aining.mall.product.service.CategoryBrandRelationService;
import com.aining.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-16 22:11:17
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 获取当前brand关联的category list
     */
    @GetMapping("/catelog/list")
    //
    public R catelogList(@RequestParam("brandId") Long brandId){
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.queryCatelogList(brandId);

        return R.ok().put("data", data);
    }

    /**
     * 获取category关联的brand
     */
    @GetMapping("/brands/list")
    public R relationBrandList(@RequestParam(value = "catId") Long catId){
        // 调用service处理前端数据
        List<BrandEntity> brandEntities = categoryBrandRelationService.getBrandsByCatId(catId);
        // 对service返回的数据进行封装，并返回给前端
        List<BrandVo> brandVos = brandEntities.stream().map((brandEntity) -> {
            BrandVo brandVo = new BrandVo();
            // 因为brandVo中的属性名(brandName)和brandEntity中的属性名(name)不同，不能直接用拷贝，只能用set方法设置Vo中的属性值
            brandVo.setBrandId(brandEntity.getBrandId());
            brandVo.setBrandName(brandEntity.getName());
            return brandVo;
        }).collect(Collectors.toList());
        return R.ok().put("data", brandVos);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);
        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
