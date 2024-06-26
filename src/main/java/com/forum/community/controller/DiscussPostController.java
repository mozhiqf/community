package com.forum.community.controller;

import com.forum.community.entity.*;
import com.forum.community.event.EventProducer;
import com.forum.community.service.CommentService;
import com.forum.community.service.DiscussPostService;
import com.forum.community.service.LikeService;
import com.forum.community.service.UserService;
import com.forum.community.util.CommunityConstant;
import com.forum.community.util.CommunityUtil;
import com.forum.community.util.HostHolder;
import com.forum.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.forum.community.util.CommunityConstant.ENTITY_TYPE_COMMENT;
import static com.forum.community.util.CommunityConstant.ENTITY_TYPE_POST;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {


    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录！");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());

        discussPostService.addDiscussPost(post);
        //触发发帖事件
        Event event = new Event().setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());

        eventProducer.fireEvent(event);

        //计算帖子的分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        return CommunityUtil.getJSONString(0, "发布成功");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        //帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        //作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        //帖子的点赞
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        //评论
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());
        List<Comment> comments =
                commentService.findCommentByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());

        //评论列表
        List<Map<String, Object>> commentList = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                //评论的各种属性
                Map<String, Object> commentAttributes = new HashMap<>();
                commentAttributes.put("comment", comment);
                commentAttributes.put("user", userService.findUserById(comment.getUserId()));

                like(comment, commentAttributes);

                //该评论的回复
                List<Comment> replys = commentService.findCommentByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String, Object>> replyList = new ArrayList<>();
                if (replys != null) {
                    for (Comment reply : replys) {
                        Map<String, Object> replyAttributes = new HashMap<>();
                        replyAttributes.put("reply", reply);
                        replyAttributes.put("user", userService.findUserById(reply.getUserId()));

                        like(reply, replyAttributes);

                        //回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyAttributes.put("target", target);
                        replyList.add(replyAttributes);
                    }
                }
                commentAttributes.put("replys", replyList);

                int count = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentAttributes.put("replyCount", count);

                commentList.add(commentAttributes);
            }
            model.addAttribute("comments", commentList);
        }
        return "/site/discuss-detail";
    }

    private void like(Comment reply, Map<String, Object> replyAttributes) {
        long likeCount;
        int likeStatus;
        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
        replyAttributes.put("likeCount", likeCount);
        likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
        replyAttributes.put("likeStatus", likeStatus);
    }

    //置顶
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);

        //触发发帖事件
        Event event = new Event().setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    //加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setTopWonderful(int id) {
        discussPostService.updateStatus(id, 1);

        //触发发帖事件
        Event event = new Event().setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        //计算帖子的分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    //删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setTopDelete(int id) {
        discussPostService.updateStatus(id, 2);

        //触发删帖事件
        Event event = new Event().setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

}
