package com.aining.mall.order.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/7/27 15:53
 */

@Configuration
@EnableTransactionManagement
@MapperScan("com.aining.mall.order.dao")
public class MybatisConfig {
    // 引入分页插件
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置请求页面大于最大页后的操作，true调回首页，false继续请求。默认为false
        paginationInterceptor.setOverflow(true);
        // 设置最大单页限制数量，默认500条，-1不受限制
        paginationInterceptor.setLimit(100);
        return paginationInterceptor;

    }
}
