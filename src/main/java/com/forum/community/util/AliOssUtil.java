package com.forum.community.util;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.alibaba.fastjson2.JSONObject;
import com.forum.community.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.Date;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketNameShare;
    private String bucketNameHeader;



    public JSONObject generatePostSignature(int bucketType,int userId) {
        String bucketName;
        switch (bucketType) {
            case 1:
                bucketName = bucketNameShare;
                break;
            case 2:
                bucketName = bucketNameHeader;
                break;
            default:
                throw new IllegalArgumentException("不存在的bucketType");
        }
        OSS ossClient = null;
        JSONObject response = new JSONObject();
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            long expireTime = 30; // 有效时间，单位秒
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);

            String fileName = CommunityUtil.generateUUID();

            String dir = userId + "/"; // 你的上传目录

            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);
            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            response.put("ossAccessKeyId", accessKeyId);
            response.put("policy", encodedPolicy);
            response.put("signature", postSignature);
            response.put("dir", dir);
            response.put("fileName", fileName);
            //https://mzqf-forum-header.oss-cn-beijing.aliyuncs.com/header/test.txt
            response.put("host", "https://" + bucketName + "." + endpoint);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return response;
    }


    public String upload(byte[] bytes, String objectName, int bucketType) {
        String bucketName;
        switch (bucketType) {
            case 1:
                bucketName = bucketNameShare;
                break;
            case 2:
                bucketName = bucketNameHeader;
                break;
            default:
                throw new IllegalArgumentException("不存在的bucketType");
        }
        String ObjectPath = "header/user/" + objectName;
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }
}
