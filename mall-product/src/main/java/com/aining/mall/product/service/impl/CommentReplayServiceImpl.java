package com.aining.mall.product.service.impl;

import com.aining.mall.product.entity.SpuCommentEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.Query;

import com.aining.mall.product.dao.CommentReplayDao;
import com.aining.mall.product.entity.CommentReplayEntity;
import com.aining.mall.product.service.CommentReplayService;


@Service("commentReplayService")
public class CommentReplayServiceImpl extends ServiceImpl<CommentReplayDao, CommentReplayEntity> implements CommentReplayService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CommentReplayEntity> page = this.page(
                new Query<CommentReplayEntity>().getPage(params),
                new QueryWrapper<CommentReplayEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据一个commentId找到它下面所有的回复信息，一个主评论可能有多人回复，一个回复又会有其他的人来回复，递归查询,查出所有相关的id
     * @param commentId
     * @return
     */
    @Override
    public List<Long> getAllReply(Long commentId) {
        ArrayList<Long> replyList = new ArrayList<>();
        recursiveGetReply(replyList,commentId);
        return replyList;
    }

    private void recursiveGetReply(ArrayList<Long> replyList,Long commentId){
        List<CommentReplayEntity> commentReplayEntityList = this.baseMapper.selectList(
                new QueryWrapper<CommentReplayEntity>().eq("comment_id",commentId));
        if(commentReplayEntityList == null || commentReplayEntityList.size() == 0){
            return;
        }
        for(CommentReplayEntity commentReplayEntity:commentReplayEntityList){
            replyList.add(commentReplayEntity.getReplyId());
            recursiveGetReply(replyList, commentReplayEntity.getReplyId());
        }
    }
}
