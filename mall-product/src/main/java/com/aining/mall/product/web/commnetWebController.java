package com.aining.mall.product.web;

import com.aining.mall.product.app.SpuCommentController;
import com.aining.mall.product.entity.SpuCommentEntity;
import com.aining.mall.product.entity.SpuInfoEntity;
import com.aining.mall.product.service.SpuCommentService;
import com.aining.mall.product.service.SpuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/9/4 17:50
 */

@RestController
public class commnetWebController {

    @Autowired
    SpuCommentService spuCommentService;

    @Autowired
    SpuInfoService spuInfoService;

    @PostMapping("/submitComment")
    public ResponseEntity<String> submitComment(
            @RequestParam("spuId") Long spuId,
            @RequestParam("content") String content,
            @RequestParam("commentType") Integer commentType
    ) {
        spuCommentService.saveCommnet(spuId,content,commentType);
        // 返回响应
        return ResponseEntity.ok("Comment submitted successfully!");
    }

    @PostMapping("/submitReply")
    public ResponseEntity<String> submitReply(@RequestBody Map<String, Object> requestBody){
        // 从requestBody中提取需要的数据
        Long commentId = Long.parseLong(requestBody.get("commentId").toString());
        String content = (String) requestBody.get("content");
        int commentType = Integer.parseInt(requestBody.get("commentType").toString());

        if(content != null && !content.isEmpty()){
            spuCommentService.saveReply(commentId,content,commentType);
        }
        return ResponseEntity.ok("Reply submitted successfully!");
    }

}
