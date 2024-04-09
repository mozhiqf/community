package com.forum.community.quartz;

import com.forum.community.entity.DiscussPost;
import com.forum.community.service.DiscussPostService;
import com.forum.community.service.ElasticsearchService;
import com.forum.community.service.LikeService;
import com.forum.community.util.CommunityConstant;
import com.forum.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private LikeService likeService;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private DiscussPostService discussPostService;

    //起始时间
    public static final Date beginEpoch;

    static {
        try {
            beginEpoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2010-1-1 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("起始时间初始化失败！", e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0) {
            logger.info("没有需要刷新的帖子，任务取消");
            return;
        }

        logger.info("*热帖刷新任务开始*" + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
        logger.info("*热帖刷新任务完毕*" + operations.size());

    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if (post == null) {
            logger.info("帖子：【" + postId + "】不存在！");
        }

        //是否是精华帖
        boolean wonderful = post.getStatus() == 1;
        //评论数量
        int commentCount = post.getCommentCount();
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        //计算权重
        double weight = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        //计算分数
        double score = Math.log10(Math.max(weight, 1)) +
                ((post.getCreateTime().getTime() - beginEpoch.getTime()) * 1.0) / (TimeUnit.DAYS.toMillis(1));
        //更新帖子分数
        discussPostService.updateScore(postId, score);
        //同步搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
