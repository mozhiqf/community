package com.forum.community.service;

import com.forum.community.dao.DiscussPostMapper;
import com.forum.community.entity.DiscussPost;
import com.forum.community.util.SensitiveFilter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Value("${caffeine.posts.max-size}")
    private int maxSize;
    @Value("${caffeine.posts.expire-second}")
    private int expireSecond;

    //帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;
    //帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    @PostConstruct
    public void init() {
        postListCache = Caffeine.newBuilder().
                maximumSize(maxSize)
                .expireAfterWrite(expireSecond, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key == null || key.length() == 0)
                            throw new IllegalArgumentException("参数不可为空！");
                        String[] param = key.split(":");
                        if (param == null && param.length != 2)
                            throw new IllegalArgumentException("参数不可为空！");
                        Integer offset = Integer.valueOf(param[0]);
                        Integer limit = Integer.valueOf(param[1]);

                        log.debug("访问了数据库");
                        return discussPostMapper.selectDiscussPostsSortedByScore(0, offset, limit);
                    }
                });
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(expireSecond, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        if (key == null) throw new IllegalArgumentException("参数不可为空！");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    /**
     * @param orderMode 0为按时间排序的帖子，1为按热度（score）排序的帖子
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        if (orderMode == 0) {
            log.debug("访问了数据库");
            return discussPostMapper.selectDiscussPosts(userId, offset, limit);
        } else if (orderMode == 1) {
            if (userId == 0)
                return postListCache.get(offset + ":" + limit);
            else {
                log.debug("访问了数据库");
                return discussPostMapper.selectDiscussPostsSortedByScore(userId, offset, limit);
            }
        }
        return null;
    }

    public int findDiscussPostRows(int userId) {
        if (userId == 0) {
            return postRowsCache.get(0);
        }
        log.debug("访问了数据库");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        //转义HTML标记
        /**
         * HtmlUtils.htmlEscape方法的作用是将字符串中的特殊HTML字符转换成相应的HTML实体编码。
         * 这主要是为了防止XSS（跨站脚本）攻击，它可以确保用户输入的内容在页面上显示时，
         * 不会被当作HTML或JavaScript代码执行。
         */
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);

    }

    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateType(int postId, int type) {
        return discussPostMapper.updateType(postId, type);
    }

    public int updateStatus(int postId, int status) {
        return discussPostMapper.updateStatus(postId, status);
    }

    public int updateScore(int postId, double score) {
        return discussPostMapper.updateScore(postId, score);
    }
}
