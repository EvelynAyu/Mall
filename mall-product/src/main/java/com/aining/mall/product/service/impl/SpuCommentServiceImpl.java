package com.aining.mall.product.service.impl;

import com.aining.common.vo.MemberResponseVo;
import com.aining.mall.product.entity.CommentReplayEntity;
import com.aining.mall.product.entity.SpuInfoEntity;
import com.aining.mall.product.service.CommentReplayService;
import com.aining.mall.product.service.SpuInfoDescService;
import com.aining.mall.product.service.SpuInfoService;
import com.aining.mall.product.vo.CommentVo;
import org.apache.http.HttpRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.Query;

import com.aining.mall.product.dao.SpuCommentDao;
import com.aining.mall.product.entity.SpuCommentEntity;
import com.aining.mall.product.service.SpuCommentService;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.aining.common.constant.AuthServerConstant.LOGIN_USER;


@Service("spuCommentService")
public class SpuCommentServiceImpl extends ServiceImpl<SpuCommentDao, SpuCommentEntity> implements SpuCommentService {
    
    @Autowired
    SpuInfoService spuInfoService;

    @Autowired
    CommentReplayService commentReplayService;

    @Autowired
    HttpSession session;
    
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuCommentEntity> page = this.page(
                new Query<SpuCommentEntity>().getPage(params),
                new QueryWrapper<SpuCommentEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveCommnet(Long spuId, String content, Integer commentType) {
        // 获得登陆人的信息
        MemberResponseVo loginUser = (MemberResponseVo) session.getAttribute(LOGIN_USER);
        // 创建CommentEntity并保存到数据库
        SpuCommentEntity spuCommentEntity = new SpuCommentEntity();
        spuCommentEntity.setSpuId(spuId);
        SpuInfoEntity spuInfoEntity = spuInfoService.getById(spuId);
        spuCommentEntity.setSpuName(spuInfoEntity.getSpuName());
        // 保存登陆人的信息
        spuCommentEntity.setMemberNickName(loginUser.getNickname());
        spuCommentEntity.setCreateTime(new Date());
        // 设置图片
//        spuCommentEntity.setResources(resource);
        spuCommentEntity.setContent(content);
        spuCommentEntity.setCommentType(commentType);

        // 调用Service层保存评论
        this.save(spuCommentEntity);
    }

    /**
     * 获取spu全部的comment和每个comment对应的reply
     * @param skuId
     * @return
     */
    @Override
    public List<CommentVo> getSpuAllComment(Long skuId) {
        SpuInfoEntity spuInfoEntity = spuInfoService.getSpuInfoBySkuId(skuId);
        // 获得spuId
        Long spuId = spuInfoEntity.getId();
        // 查出主评论
        List<SpuCommentEntity> spuCommentEntities = this.baseMapper.selectList(new QueryWrapper<SpuCommentEntity>()
                .eq("spu_id", spuId).eq("comment_type",0));
        List<CommentVo> commentVoList = spuCommentEntities.stream().map((spuCommentEntity) -> {
            CommentVo commentVo = new CommentVo();
            BeanUtils.copyProperties(spuCommentEntity, commentVo);
            // 查询当前comment对应的回复
            Long commentId = spuCommentEntity.getId();
            // 根据commentId递归查询comment_reply表得到reply_id
            List<Long> commentReplyIds = commentReplayService.getAllReply(commentId);
            // 根据reply_id查询所有回复的内容
            if(!StringUtils.isEmpty(commentReplyIds) && commentReplyIds.size() != 0){
                List<SpuCommentEntity> commentReplyList = this.baseMapper.selectList(new QueryWrapper<SpuCommentEntity>()
                        .in("id",commentReplyIds));
                commentVo.setCommentReplyList(commentReplyList);
            }
            return commentVo;
        }).collect(Collectors.toList());
        return commentVoList;
    }

    @Override
    public void saveReply(Long commentId, String content, Integer commentType) {
        // 保存reply到SpuComment表中:因为是子回复所以不需要保存商品信息
        SpuCommentEntity spuCommentEntity = new SpuCommentEntity();
        spuCommentEntity.setCreateTime(new Date());
        spuCommentEntity.setCommentType(commentType);
        spuCommentEntity.setContent(content);
        //获取登陆人的信息
        MemberResponseVo loginUser = (MemberResponseVo) session.getAttribute(LOGIN_USER);
        spuCommentEntity.setMemberNickName(loginUser.getNickname());

        // 找到这条评论对应的主人
        SpuCommentEntity commentOwner = this.getById(commentId);
        String memberNickName = commentOwner.getMemberNickName();
        spuCommentEntity.setReplyToWhom(memberNickName);

        this.save(spuCommentEntity);

        // 获得replyId
        Long replyId = spuCommentEntity.getId();

        // 保存reply和CommentId之间到关系
        CommentReplayEntity commentReplayEntity = new CommentReplayEntity();
        commentReplayEntity.setCommentId(commentId);
        commentReplayEntity.setReplyId(replyId);
        commentReplayService.save(commentReplayEntity);
    }

}