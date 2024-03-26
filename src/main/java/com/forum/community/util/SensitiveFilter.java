package com.forum.community.util;

import lombok.Data;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private TrieNode root = new TrieNode();

    private static final String REPLACEMENT = "***";

    //前缀树
    @Data
    private class TrieNode {
        //敏感词结束标志
        private boolean isKeyWordEnd = false;
        //当前结点指向的子节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public void addSubNode(Character c, TrieNode node) {
            if (c == null || node == null) {
                throw new IllegalArgumentException("Character and TrieNode cannot be null");
            }
            subNodes.put(c, node);
        }

        public TrieNode getSubNode(Character c) {
            if (c == null) {
                throw new IllegalArgumentException("Character cannot be null");
            }
            return subNodes.get(c);
        }
    }

    @PostConstruct
    public void init() {
        try (
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ) {
            String keyWord;
            while ((keyWord = reader.readLine()) != null) {
                this.addKeyWord(keyWord);
            }
        } catch (IOException e) {
            logger.error("敏感词加载失败" + e.getMessage());
        }
    }

    private void addKeyWord(String keyWord) {
        TrieNode temNode = root;
        for (int i = 0; i < keyWord.length(); i++) {
            char c = keyWord.charAt(i);
            TrieNode subNode = temNode.getSubNode(c);
            if (subNode == null) {
                subNode = new TrieNode();
                temNode.addSubNode(c, subNode);
            }
            temNode = subNode;

            if (i == keyWord.length() - 1) {
                temNode.setKeyWordEnd(true);
            }
        }
    }

    /**
     * @param text 未被过滤的
     * @return 过滤后的
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        TrieNode tempNode = root;//前缀树的指针
        int begin = 0;//敏感词起始指针
        int end = 0;//敏感词结束指针

        StringBuilder builder = new StringBuilder();

        while (end < text.length()) {
            char c = text.charAt(end);
            if (isSymbol(c)) {
                if (tempNode == root) {
                    builder.append(c);
                    begin++;
                }
                end++;
                continue;
            }
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                builder.append(text.charAt(begin));
                end = ++begin;
                tempNode = root;
            } else if (tempNode.isKeyWordEnd()) {
                builder.append(REPLACEMENT);
                begin = ++end;
            } else {
                end++;
            }
        }

        builder.append(text.substring(begin));

        return String.valueOf(builder);

    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


}
