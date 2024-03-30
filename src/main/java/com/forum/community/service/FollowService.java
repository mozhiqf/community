package com.forum.community.service;

import com.forum.community.entity.User;
import com.forum.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.forum.community.util.CommunityConstant.ENTITY_TYPE_USER;

@Service
public class FollowService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

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
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

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
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

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
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询用户是否关注了当前实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    //查询用户关注的人
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        return getMaps(offset, limit, followeeKey);
    }

    // 查询某用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        return getMaps(offset, limit, followerKey);
    }

    private List<Map<String, Object>> getMaps(int offset, int limit, String key) {
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(key, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(key, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }

}
