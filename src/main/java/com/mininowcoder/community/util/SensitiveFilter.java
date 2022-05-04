package com.mininowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by FeiPan on 2022/4/23.
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 敏感词的替换符
    private static final String REPLACEMENT = "***";
    // 根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 将keyword添加到前缀树中
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词失败：" + e.getMessage());
        }
    }

    // 过滤敏感词
    public String filterSensitiveWords(String text) {
        if (StringUtils.isBlank(text))
            return null;

        // 指针1-指向字典树的根节点
        TrieNode tempNode = rootNode;
        // 指针2、3-遍历字符串
        int lo = 0, hi = 0;
        StringBuilder sb = new StringBuilder();

        while (hi < text.length()) {
            char c = text.charAt(hi);
            // 跳过特殊符号
            if(isSymbol(c)){
                // 如果指针1指向根结点，将此符号计入结果
                if(tempNode==rootNode){
                    sb.append(c);
                    lo++;
                }
                hi++;
                continue;
            }
            // 检查子节点
            tempNode = tempNode.getSubNode(c);
            if(tempNode==null){
                // 以lo开头的词不是敏感词
                sb.append(text.charAt(lo));
                // 判断下一个字符
                hi = ++lo;
                tempNode = rootNode;
            }else if(tempNode.isKeywordEnd()){
                // 发现敏感词，将lo-hi之间的字符替换
                sb.append(REPLACEMENT);
                lo = ++hi;
                tempNode = rootNode;
            }else{
                hi++;
            }
        }
        sb.append(text.substring(lo));
        return sb.toString();
    }

    //判断是否为特殊符号
    private boolean isSymbol(Character c) {
        // 0x2E80-0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // 将敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode temNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = temNode.getSubNode(c);
            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                temNode.addSubNode(c, subNode);
            }
            // 指向子节点，进入下一轮循环
            temNode = subNode;
            if (i == keyword.length() - 1) {
                temNode.setKeywordEnd(true);
            }
        }
    }

    // 前缀树
    private class TrieNode {
        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点（key-子节点中的字符，value-子节点对象）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
