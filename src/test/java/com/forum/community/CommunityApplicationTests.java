package com.forum.community;

import com.forum.community.util.AliOssProperties;
import com.forum.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter() {
        String text = "这里可以赌博,可以嫖娼,可以吸毒,可以开票,哈哈哈!";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "这里可以☆赌☆博☆,可以☆嫖☆娼☆,可以☆吸☆毒☆,可以☆开☆票☆,哈哈哈!";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }

    @Autowired
    private AliOssProperties aliOssProperties;

    @Test
    public void testProperties() {
        System.out.println(aliOssProperties.getEndpoint());
        System.out.println(aliOssProperties.getAccessKeyId());
        System.out.println(aliOssProperties.getAccessKeySecret());
        System.out.println(aliOssProperties.getBucketNameShare());
        System.out.println(aliOssProperties.getBucketNameHeader());
    }
}