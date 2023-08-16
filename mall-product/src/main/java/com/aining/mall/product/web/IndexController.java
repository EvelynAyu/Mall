package com.aining.mall.product.web;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/16 20:00
 */

import com.aining.mall.product.entity.CategoryEntity;
import com.aining.mall.product.service.CategoryService;
import com.aining.mall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 页面跳转Controller
 */
@Controller
public class IndexController {

    @Resource
    private CategoryService categoryService;

    @GetMapping(value = {"/","index.html"})
    private String indexPage(Model model){
        // 1. 查出所有一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
        model.addAttribute("categories",categoryEntities);
        return "index";
    }

    //index/json/catalog.json
    @GetMapping(value = "/index/catalog.json")
    @ResponseBody
    public Map<String, List<Catelog2Vo>> getCatalogJson() {

        Map<String, List<Catelog2Vo>> catalogJson = categoryService.getCatalogJson();

        return catalogJson;

    }
}
