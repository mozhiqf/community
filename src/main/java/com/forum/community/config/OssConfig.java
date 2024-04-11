package com.forum.community.config;

import com.forum.community.util.AliOssProperties;
import com.forum.community.util.AliOssUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfig {
    /**
     * 开始创建阿里云文件上传工具类对象
     *
     * @param aliOssProperties
     * @return
     * @ConditionalOnMissingBean 确保容器里面只有一个工具对象
     */
    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties) {
        return new AliOssUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketNameShare(),
                aliOssProperties.getBucketNameHeader());
    }
}
