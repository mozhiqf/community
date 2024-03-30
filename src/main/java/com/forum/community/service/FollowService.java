package com.forum.community.service;

import com.forum.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class FollowService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 关注功能需要把被关注的实体存到关注者的关注列表里
     * 也需要把关注者存入被关注者的粉丝列表里
     */
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //关注列表
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                //粉丝列表
                String followerKey = RedisKeyUtil.getFollowerKey(entityId, entityType);

                operations.multi();
                //关注列表
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                //粉丝列表
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //关注列表
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                //粉丝列表
                String followerKey = RedisKeyUtil.getFollowerKey(entityId, entityType);

                operations.multi();
                //关注列表
                operations.opsForZSet().remove(followeeKey, entityId);
                //粉丝列表
                operations.opsForZSet().remove(followerKey, userId);
                return operations.exec();
            }
        });
    }

    //查询用户关注的实体的数量
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询实体的粉丝数量
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityId, entityType);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询用户是否关注了当前实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

}
