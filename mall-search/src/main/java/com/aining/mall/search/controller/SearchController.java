package com.aining.mall.search.controller;

import com.aining.mall.search.service.MallSearchService;
import com.aining.mall.search.vo.SearchParam;
import com.aining.mall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/19 13:56
 */
@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;

    /**
     * searchParam为检索参数，自动将页面提交过来的所有请求参数封装成我们指定的对象
     * @param param
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model,HttpServletRequest request) {
        param.set_queryString(request.getQueryString());

        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";
    }
}
